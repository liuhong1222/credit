package cn.service.impl;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ListMultimap;
import cn.entity.CvsFilePath;
import cn.entity.FileUpload;
import cn.entity.MobileNumberSection;
import cn.entity.TxtFileContent;
import cn.enums.MobileReportGroupEnum;
import cn.enums.TxtSuffixEnum;
import cn.redis.DistributedLockWrapper;
import cn.redis.RedisClient;
import cn.service.BigDataTestService;
import cn.service.CvsFilePathService;
import cn.service.EmptyRedisService;
import cn.service.FileService;
import cn.service.MobileNumberSectionService;
import cn.service.NumberCheckLogService;
import cn.service.http.Result.EmptyNumFileDetectionResult;
import cn.service.without.BigDataHttpService;
import cn.service.without.SettlementService;
import cn.thread.ThreadExecutorService;
import cn.utils.CommonUtils;
import cn.utils.DateUtil;
import cn.utils.DateUtils;
import cn.utils.FileUtils;
import cn.utils.TxtFileUtil;
import cn.utils.UUIDTool;
import main.java.cn.common.BackResult;
import main.java.cn.common.RedisKeys;
import main.java.cn.common.ResultCode;
import main.java.cn.domain.AgentWebSiteDomain;
import main.java.cn.domain.NumberCheckLogDomain;
import main.java.cn.domain.RunTestDomian;
import main.java.cn.sms.util.ChuangLanSmsUtil;
import main.java.cn.sms.util.SmallSmsUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class BigDataTestServiceImpl implements BigDataTestService {

	private final static Logger logger = LoggerFactory.getLogger(BigDataTestServiceImpl.class);

    @Value("${server.port}")
    private String port;

    @Value("${loadfilePath}")
    private String loadfilePath;
	
	@Autowired
    private RedisClient redisClient;
	
	@Autowired
    private CvsFilePathService cvsFilePathService;
	
	@Autowired
    private JedisPool jedisPool;
	
	@Autowired
    private SettlementService settlementService;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Value("${withUserProviderServiceNoClient}")
	private String withUserProviderService;

	@Value("${getAgentInfoUrl}")
	private String getAgentInfoUrl;

    @Autowired
    private BigDataHttpService bigDataHttpService;

    @Autowired
    private ThreadExecutorService threadExecutorService;

    @Autowired
    private MobileNumberSectionService mobileNumberSectionService;
    
    @Autowired
    private NumberCheckLogService numberCheckLogService;
    
    @Autowired
    private EmptyRedisService emptyRedisService;
    
    @Autowired
    private FileService fileService;
    
    /**
     * 空号文件检测每次请求数量
     */
    private final static Integer BIG_DATA_API_REQUEST_SIZE = 2000;
    
    private final static String DEFAULT_CHARSET = "utf-8";
    
	/**
     * web 上传文件空号检测 最新版本
     *
     * @param fileUpload 文件对象
     * @param userId     用户id
     * @param mobile     手机号码
     * @param source     来源
     * @return
     */
	@Override
	public BackResult<RunTestDomian> theTestNew(FileUpload fileUpload, String userId, String mobile,
                                              String source,String startLine) {
		int expire = 60 * 60 * 1000 * 3; // 超时时间 （3小时）
		int mobileCount = fileUpload.getFileRows();
		// redis锁的唯一标识
        String succeedClearingCountkey = RedisKeys.getInstance().getkhSucceedClearingCountkey(userId, fileUpload.getId()); // 获取空号检测
        // 全部执行完毕生成文件使用
        String exceptionkey = RedisKeys.getInstance().getkhExceptionkey(userId,fileUpload.getId()); // 线程执行全局异常key
        // （根据文件获取的总条数）
        String succeedTestCountkey = RedisKeys.getInstance().getkhSucceedTestCountkey(userId,fileUpload.getId()); // 获取空号检测
        // 定义基础rediskey
        String lockName = RedisKeys.getInstance().getkhTheTestFunKey(mobile, fileUpload.getId()); // 锁名
        DistributedLockWrapper lock = new DistributedLockWrapper(jedisPool, lockName);
        try {
            // 加锁
            String identifier = lock.lockWithTimeout(lockName, 800L, expire);
            if (null == identifier) {
                return new BackResult<RunTestDomian>(ResultCode.RESULT_SUCCEED, "已提交检测，请勿重复提交", new RunTestDomian("1", 0, fileUpload.getId()));
            }
                        
            logger.info("----------用户编号：[" + userId + "]开始执行空号检索事件");
            // redis初始化
            emptyRedisService.initRedisKey(userId, fileUpload.getId(), expire, fileUpload.getFileRows(), identifier);
            
            File file = new File(fileUpload.getFileUploadUrl());
            if (!file.isFile() || !file.exists()) {
            	redisClient.set(exceptionkey, ResultCode.RESULT_FAILED, expire);            
                lock.releaseLock();
                return new BackResult<RunTestDomian>("999999", "上传文件不存在或已删除", new RunTestDomian("9", 0, fileUpload.getId()));
            }
            
            TxtFileContent txtFileContent = fileService.getValidMobileListByTxt(fileUpload.getFileUploadUrl());
            //获取文件编码格式
            String fileEncoding = txtFileContent.getFileCode();
            mobileCount = txtFileContent.getMobileCounts();
            
            //判断列表为空则直接返回
            if (mobileCount == 0) {
                redisClient.set(exceptionkey, ResultCode.RESULT_FAILED, expire);            
                lock.releaseLock();
                return new BackResult<RunTestDomian>("999999", "无有效手机号码", new RunTestDomian("3", 0, fileUpload.getId()));
            }
            
            // 需要计费的总条数存入redis
            redisClient.set(succeedClearingCountkey, String.valueOf(mobileCount), expire * 2);
            // 冻结余额
            settlementService.freeWaccount(redisClient, expire, Boolean.TRUE,fileUpload.getId(), userId,mobileCount);
            // 保存默认号码到redis
            saveDefaultMobileToRedis(userId, fileUpload.getId(), txtFileContent.getMobileList());
            // 检测逻辑
            emptyNumFileDetectionByTxtNew(mobileCount, userId,succeedTestCountkey, expire,
                    source, mobile, lock,fileUpload, fileEncoding,startLine);
            
            lock.releaseLock();
            return new BackResult<RunTestDomian>(ResultCode.RESULT_SUCCEED, "任务执行中", new RunTestDomian("1", getPauseSecond(mobileCount), fileUpload.getId()));
		} catch (Exception e) {
			lock.releaseLock();
			redisClient.set(exceptionkey, ResultCode.RESULT_FAILED, expire);
            settlementService.freeWaccount(redisClient, expire, Boolean.FALSE, fileUpload.getId(), userId,mobileCount);
            return new BackResult<RunTestDomian>("999999", "系统异常", new RunTestDomian("3",0, fileUpload.getId()));
		}

    }
    
    /**
     * 空号文件批量检查，大数据通道
     */
    private synchronized Future<?> emptyNumFileDetectionByTxtNew(int mobileCount, String userId,
                                                              String succeedTestCountkey,
                                                              Integer expire,
                                                              String source,
                                                              String mobile, DistributedLockWrapper lock,
                                                              FileUpload fileUpload, String fileEncoding,String startLine) {
    	// 创建一个线程
        Runnable run = new Runnable() {
            @Override
            public void run() {
                String uid = fileUpload.getId();
                String exceptionkey = RedisKeys.getInstance().getkhExceptionkey(userId, uid); // 线程执行全局异常key
                Jedis jedis = null;
                int startCounts = StringUtils.isBlank(startLine)?0:Integer.parseInt(startLine);
                try {
                    jedis = jedisPool.getResource();
                    // 出现异常终止检测
                    String exceptions = jedis.get(exceptionkey);
                    if (exceptions.equals(ResultCode.RESULT_SUCCEED)) {
                        long beginTime = System.currentTimeMillis();                            
                        logger.info(">>>>>>>>>>>>>>> 空号文件开始检查 uid:" + uid + "，数量:" + mobileCount + ">>>>>>>>>>>>>>>");                
                        //批次数    分批检测手机号状态，每次2000个
                        int batchCount = (mobileCount-startCounts) / BIG_DATA_API_REQUEST_SIZE;
                        for (int i = 0; i < (batchCount + 1); i++) {
                            int fromIndex = BIG_DATA_API_REQUEST_SIZE * i + 1 + startCounts;
                            int toIndex = fromIndex + BIG_DATA_API_REQUEST_SIZE - 1;
                            if (toIndex > mobileCount) {
                                toIndex = mobileCount;
                            }
                            List<String> mobileSubList = TxtFileUtil.readTxt(getTxtPath(fileUpload, TxtSuffixEnum.ALL), fileEncoding, fromIndex, BIG_DATA_API_REQUEST_SIZE);
                            //根据检测状态码列表判断手机号状态
                            EmptyNumFileDetectionResult detectionResult = bigDataHttpService.emptyNumFileDetectionNew(mobileSubList,userId);
                            if (detectionResult != null && detectionResult.getData() != null) {
                                ListMultimap<MobileReportGroupEnum, String> group = detectionResult.getData();
                                //数据分组存入文本
                                saveGroupList(fileUpload, group);
                                //获取部分检测好的号码存入redis用于前端显示
                                saveDateToRedis(userId,uid,group);
                                //没有返回结果，验证号码段
                                Set<String> noResultMobileSet = new HashSet<>(group.get(MobileReportGroupEnum.FileDetection.NO_RESULT));
                                saveNoResultMobileSet(fileUpload, noResultMobileSet);
                            }else{
                            	//大数据返回为空重试
                            	logger.error(">>>>>>>>>>>>>>> 空号文件检测异常，大数据接口第一次调用返回为空， uid:" + uid + "，当前进度:" + toIndex + "/" + mobileCount + ">>>>>>>>>>>>>>>");                                	
                            	//根据检测状态码列表判断手机号状态
                            	EmptyNumFileDetectionResult reDetectionResult = bigDataHttpService.emptyNumFileDetectionNew(mobileSubList,userId);
                            	if(reDetectionResult == null || reDetectionResult.getData() == null){
                            		//第二次重试失败则这批号码直接放到沉默号
                            		logger.error(">>>>>>>>>>>>>>> 空号文件检测异常，大数据接口第二次重试调用返回为空， uid:" + uid + "，当前进度:" + toIndex + "/" + mobileCount + ">>>>>>>>>>>>>>>"); 
                            		TxtFileUtil.saveTxt(mobileSubList, getTxtPath(fileUpload, TxtSuffixEnum.SILENCE_ONE), DEFAULT_CHARSET, true);
                            	}else{
                                    ListMultimap<MobileReportGroupEnum, String> group = reDetectionResult.getData();
                                    //数据分组存入文本
                                    saveGroupList(fileUpload, group);  
                                    //获取部分检测好的号码存入redis用于前端显示
                                    saveDateToRedis(userId,uid,group);
                                    //没有返回结果，验证号码段
                                    Set<String> noResultMobileSet = new HashSet<>(group.get(MobileReportGroupEnum.FileDetection.NO_RESULT));
                                    saveNoResultMobileSet(fileUpload, noResultMobileSet);
                            	}                              	                                	
                            }
                            
                            //等待1秒
                            Thread.sleep(10);
                            logger.info(">>>>>>>>>>>>>>> 空号文件检测中， uid:" + uid + "，当前进度:" + toIndex + "/" + mobileCount + ">>>>>>>>>>>>>>>");
                            // 成功检测条数累加
                            redisClient.set(succeedTestCountkey, String.valueOf(toIndex), expire);
                        }
                        
                        //完成后等待30秒
                        Thread.sleep(20 * 1000);
                        logger.info(">>>>>>>>>>>>>>> 空号文件结束检查，等待生成txt，uid:" + uid + "，数量:" + mobileCount + "，用时:" + (System.currentTimeMillis() - beginTime) + " >>>>>>>>>>>>>>>");
                        //生成结果文件
                        generateResultFiles(fileUpload, userId, source, mobile, String.valueOf(mobileCount), expire);
                        logger.info(">>>>>>>>>>>>>>> 空号文件完成任务，uid:" + uid + "，数量:" + mobileCount + "，用时:" + (System.currentTimeMillis() - beginTime) + " >>>>>>>>>>>>>>>");
                    }

                } catch (Exception e) {
                    logger.error("{}, 执行空号在线检测异常，uid:{},info:",userId,fileUpload.getId(), e);
                    redisClient.set(exceptionkey, ResultCode.RESULT_FAILED, expire);
                    settlementService.freeWaccount(redisClient, expire, Boolean.FALSE, uid,userId,mobileCount);
                } finally {
                    // 返还到连接池
                    jedis.close();
                    if (lock != null) {
                        lock.releaseLock();
                    }
                }
            }
        };
        // 加入线程池开始执行
        return threadExecutorService.execute(run);
    }
    
    /**
     * 生成结果报表
     */
    private synchronized void generateResultFiles(FileUpload fileUpload, String userId, String source, String mobile,
                                                  String succeedClearingCount, Integer expire) {
        String uid = fileUpload.getId();
        String fileName = fileUpload.getFileName();
        try {
            String khTheRunkey = RedisKeys.getInstance().getkhTheRunkey(userId, uid);
            // 删除临时文件
            TxtFileUtil.deleteFile(getTxtPath(fileUpload, TxtSuffixEnum.ALL));

            CvsFilePath cvsFilePath = new CvsFilePath();
            cvsFilePath.setUserId(userId);
            int totalCount = 0;

            List<File> list = new ArrayList<File>();
            String timeTemp = String.valueOf(System.currentTimeMillis());
            String subFilePath = DateUtils.getDate() + "/" + userId + "/" + timeTemp + "/";
            String filePath = loadfilePath + subFilePath;
            //生成实号报表
            TxtFileUtil.appendTxt(getTxtPath(fileUpload, TxtSuffixEnum.REAL_ONE), filePath + "实号.txt", filePath, DEFAULT_CHARSET, true);
            TxtFileUtil.deleteFile(getTxtPath(fileUpload, TxtSuffixEnum.REAL_ONE));
            TxtFileUtil.appendTxt(getTxtPath(fileUpload, TxtSuffixEnum.REAL_TWO), filePath + "实号.txt", filePath, DEFAULT_CHARSET, true);
            TxtFileUtil.deleteFile(getTxtPath(fileUpload, TxtSuffixEnum.REAL_TWO));
            TxtFileUtil.appendTxt(getTxtPath(fileUpload, TxtSuffixEnum.REAL_THREE), filePath + "实号.txt", filePath, DEFAULT_CHARSET, true);
            TxtFileUtil.deleteFile(getTxtPath(fileUpload, TxtSuffixEnum.REAL_THREE));
            int countReal = TxtFileUtil.countLines(filePath + "实号.txt", DEFAULT_CHARSET);
            if (countReal > 0) {
            	totalCount += countReal;
                logger.info("----------实号总条数：" + countReal);
                list.add(new File(filePath + "实号.txt"));
                cvsFilePath.setThereCount(countReal);
                cvsFilePath.setThereFilePath(subFilePath + "实号.txt");
                cvsFilePath.setThereFileSize(FileUtils.getFileSize(filePath + "实号.txt"));
            }
            // 生成空号报表
            TxtFileUtil.appendTxt(getTxtPath(fileUpload, TxtSuffixEnum.KONG_ONE), filePath + "空号.txt", filePath, DEFAULT_CHARSET, true);
            TxtFileUtil.deleteFile(getTxtPath(fileUpload, TxtSuffixEnum.KONG_ONE));
            TxtFileUtil.appendTxt(getTxtPath(fileUpload, TxtSuffixEnum.KONG_TWO), filePath + "空号.txt", filePath, DEFAULT_CHARSET, true);
            TxtFileUtil.deleteFile(getTxtPath(fileUpload, TxtSuffixEnum.KONG_TWO));
            TxtFileUtil.appendTxt(getTxtPath(fileUpload, TxtSuffixEnum.KONG_THREE), filePath + "空号.txt", filePath, DEFAULT_CHARSET, true);
            TxtFileUtil.deleteFile(getTxtPath(fileUpload, TxtSuffixEnum.KONG_THREE));
            int countEmpty = TxtFileUtil.countLines(filePath + "空号.txt", DEFAULT_CHARSET);
            if (countEmpty > 0) {
            	totalCount += countEmpty;
                logger.info("----------空号总条数：" + countEmpty);
                list.add(new File(filePath + "空号.txt"));
                cvsFilePath.setSixCount(countEmpty);
                cvsFilePath.setSixFilePath(subFilePath + "空号.txt");
                cvsFilePath.setSixFileSize(FileUtils.getFileSize(filePath + "空号.txt"));
            }
            // 生成沉默号报表
            TxtFileUtil.appendTxt(getTxtPath(fileUpload, TxtSuffixEnum.SILENCE_ONE), filePath + "沉默号.txt", filePath, DEFAULT_CHARSET, true);
            TxtFileUtil.deleteFile(getTxtPath(fileUpload, TxtSuffixEnum.SILENCE_ONE));
            int countSilence = TxtFileUtil.countLines(filePath + "沉默号.txt", DEFAULT_CHARSET);
            if (countSilence > 0) {
            	totalCount += countSilence;
                logger.info("----------沉默号总条数：" + countSilence);
                list.add(new File(filePath + "沉默号.txt"));
                cvsFilePath.setUnknownSize(countSilence);
                cvsFilePath.setUnknownFilePath(subFilePath + "沉默号.txt");
                cvsFilePath.setUnknownFileSize(FileUtils.getFileSize(filePath + "沉默号.txt"));
            }
            // 生成风险号报表
            TxtFileUtil.appendTxt(getTxtPath(fileUpload, TxtSuffixEnum.SHUTDOWN_ONE), filePath + "风险号.txt", filePath, DEFAULT_CHARSET, true);
            TxtFileUtil.deleteFile(getTxtPath(fileUpload, TxtSuffixEnum.SHUTDOWN_ONE));
            int countShutdown = TxtFileUtil.countLines(filePath + "风险号.txt", DEFAULT_CHARSET);
            if (countShutdown > 0) {
            	totalCount += countShutdown;
                logger.info("----------风险号总条数：" + countShutdown);
                list.add(new File(filePath + "风险号.txt"));
                cvsFilePath.setShutCount(countShutdown);
                cvsFilePath.setShutFilePath(subFilePath + "风险号.txt");
                cvsFilePath.setShutFileSize(FileUtils.getFileSize(filePath + "风险号.txt"));
            }
            // 报表文件打包
            if (!CollectionUtils.isEmpty(list)) {
                String zipName = fileName + ".zip";
                FileUtils.createZip(list, filePath + zipName);
                cvsFilePath.setZipName(zipName);
                cvsFilePath.setZipPath((subFilePath + zipName));
                cvsFilePath.setZipSize(FileUtils.getFileSize(filePath + zipName));
            }
            
            cvsFilePath.setCreateTime(new Date());
            cvsFilePath.setFileCode(uid);
            cvsFilePath.setTotalCount(totalCount);
            cvsFilePath.setCreateDate(DateUtils.getNowDate());
            if (CommonUtils.isNotString(cvsFilePath.getThereFilePath())
                    && CommonUtils.isNotString(cvsFilePath.getSixFilePath())
                    && CommonUtils.isNotString(cvsFilePath.getUnknownFilePath())) {
            	if(CommonUtils.isNumeric(mobile)) {
            		sendMessage(source, Boolean.FALSE, mobile, userId,getAgentInfoByCreUserId(userId));
            	}
                String exceptionkey = RedisKeys.getInstance().getkhExceptionkey(userId, uid); // 线程执行全局异常key
                redisClient.set(exceptionkey, ResultCode.RESULT_FAILED, expire);
            } else {
            	cvsFilePathService.saveCvsFilePath(cvsFilePath);
            	
            	NumberCheckLogDomain wcd = new NumberCheckLogDomain();
        		wcd.setUserId(Long.valueOf(userId));
        		wcd.setId(UUIDTool.getInstance().getUUID());
        		wcd.setDayInt(DateUtil.formateDateToInt(new Date()));
        		wcd.setConsumptionNum("");
        		wcd.setCommitCount(fileUpload.getFileRows().longValue());
        		wcd.setFileName(cvsFilePath.getZipName());
        		wcd.setCount(Integer.valueOf(cvsFilePath.getTotalCount()).longValue()); // 条数
        		wcd.setRealCount(Integer.valueOf(cvsFilePath.getThereCount()).longValue());
        		wcd.setEmptyCount(Integer.valueOf(cvsFilePath.getSixCount()).longValue());
        		wcd.setSilentCount(Integer.valueOf(cvsFilePath.getUnknownSize()).longValue());
        		wcd.setRiskCount(Integer.valueOf(cvsFilePath.getShutCount()).longValue());
        		wcd.setStatus(1);
        		wcd.setCreateTime(new Date());
        		wcd.setUpdateTime(new Date());	            	
            	numberCheckLogService.saveNumberCheckLog(wcd);
                settlementService.webConsumeAccount(userId, succeedClearingCount, redisClient, uid);
                if(CommonUtils.isNumeric(mobile) && isSendMessage(userId)) {
                	sendMessage(source, Boolean.TRUE, mobile, userId,getAgentInfoByCreUserId(userId));
                }

                // 初始化 设置程序运行结束
                redisClient.set(khTheRunkey, ResultCode.RESULT_FAILED, expire);
                logger.info("----------用户编号：[" + userId + "]结束执行空号检索事件 事件结束时间："
                        + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            }

        } catch (Exception e) {
            String exceptionkey = RedisKeys.getInstance().getkhExceptionkey(userId, uid); // 线程执行全局异常key
            redisClient.set(exceptionkey, ResultCode.RESULT_FAILED, expire);
            settlementService.freeWaccount(redisClient, expire, Boolean.FALSE, uid, userId,Integer.valueOf(succeedClearingCount));
            logger.error("{},空号在线检测完成，分析结果文件异常，uid:{},info:",userId,uid,e);
        }

    }
    
    private Boolean isSendMessage(String userId) {
    	String mainFlag = redisClient.get("kh:sendMessage:main");
    	if (StringUtils.isNotBlank(mainFlag) && "close".equals(mainFlag)) {
			return false;
		}
    	
    	String userFlag = redisClient.get("kh:sendMessage:" + userId);
    	if (StringUtils.isNotBlank(userFlag) && "close".equals(userFlag)) {
			return false;
		}
    	
    	return true;
    }
    
    //验证号码段
    private void saveNoResultMobileSet(FileUpload fileUpload, Set<String> noResultMobileSet) throws IOException {
        if (!CollectionUtils.isEmpty(noResultMobileSet)) {
            Map<String, String> mobileAndNumberSectionMap = new HashMap<>();
            Set<String> numberSectionSet = new HashSet<>();
            //转换成号码段对应map
            for (String m : noResultMobileSet) {
                numberSectionSet.add(m.substring(0, 7));
                mobileAndNumberSectionMap.put(m, m.substring(0, 7));
            }
            //查询号码段记录
            List<MobileNumberSection> listByNumberSections = mobileNumberSectionService.findListByNumberSections(new ArrayList<>(numberSectionSet));
            Set<String> resultNumberSectionSet = new HashSet<>();
            for (MobileNumberSection obileNumberSection : listByNumberSections) {
                resultNumberSectionSet.add(obileNumberSection.getNumberSection());
            }
            List<String> silenceOneList = new ArrayList<>();
            List<String> kongThreeList = new ArrayList<>();
            for (String m : noResultMobileSet) {
                if (resultNumberSectionSet.contains(mobileAndNumberSectionMap.get(m))) {
                    silenceOneList.add(m);
                } else {
                    //不包含在号码段，则为空号
                    kongThreeList.add(m);
                }
            }
            if (!CollectionUtils.isEmpty(silenceOneList)) {
                TxtFileUtil.saveTxt(silenceOneList, getTxtPath(fileUpload, TxtSuffixEnum.SILENCE_ONE), DEFAULT_CHARSET, true);
            }
            if (!CollectionUtils.isEmpty(kongThreeList)) {
                TxtFileUtil.saveTxt(kongThreeList, getTxtPath(fileUpload, TxtSuffixEnum.KONG_THREE), DEFAULT_CHARSET, true);
            }
        }
    }
    
    /**
     * 数据分组存入对应的文本
     */
    private void saveOneGroupList(String filePath,
                                  ListMultimap<MobileReportGroupEnum, String> group,
                                  MobileReportGroupEnum.FileDetection oneFileDetectionEnum) throws IOException {
        List<String> oneDelivrdMobileList = group.get(oneFileDetectionEnum);
        TxtFileUtil.saveTxt(oneDelivrdMobileList, filePath, DEFAULT_CHARSET, true);
    }

    /**
     * 数据分组存入文本
     */
    private void saveGroupList(FileUpload fileUpload, ListMultimap<MobileReportGroupEnum, String> group) throws IOException {
        //获取实号1组
        saveOneGroupList(getTxtPath(fileUpload, TxtSuffixEnum.REAL_ONE), group, MobileReportGroupEnum.FileDetection.REAL_ONE);
        //获取实号2组
        saveOneGroupList(getTxtPath(fileUpload, TxtSuffixEnum.REAL_TWO), group, MobileReportGroupEnum.FileDetection.REAL_TWO);
        //获取实号3组
        saveOneGroupList(getTxtPath(fileUpload, TxtSuffixEnum.REAL_THREE), group, MobileReportGroupEnum.FileDetection.REAL_THREE);
        //获取空号1组
        saveOneGroupList(getTxtPath(fileUpload, TxtSuffixEnum.KONG_ONE), group, MobileReportGroupEnum.FileDetection.EMPTY_ONE);
        //获取空号2组
        saveOneGroupList(getTxtPath(fileUpload, TxtSuffixEnum.KONG_TWO), group, MobileReportGroupEnum.FileDetection.EMPTY_TWO);
        //获取空号3组
        saveOneGroupList(getTxtPath(fileUpload, TxtSuffixEnum.KONG_THREE), group, MobileReportGroupEnum.FileDetection.EMPTY_THREE);
        //沉默号
        saveOneGroupList(getTxtPath(fileUpload, TxtSuffixEnum.SILENCE_ONE), group, MobileReportGroupEnum.FileDetection.SILENCE);
        //停机号
        saveOneGroupList(getTxtPath(fileUpload, TxtSuffixEnum.KONG_THREE), group, MobileReportGroupEnum.FileDetection.OUT_SERVICE);
        //未知状态 为沉默号
        saveOneGroupList(getTxtPath(fileUpload, TxtSuffixEnum.SILENCE_ONE), group, MobileReportGroupEnum.FileDetection.UNKNOWN);
        //关机号
        saveOneGroupList(getTxtPath(fileUpload, TxtSuffixEnum.SHUTDOWN_ONE), group, MobileReportGroupEnum.FileDetection.SHUT);
    }    
    
  //异步发送短信
    ExecutorService executorService = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    /**
     * 发送短信 通知
     *
     * @param source 来源
     * @param fag    true 发送成功检测完成短信 false 发送检测失败短信
     */
    public void sendMessage(final String source, final Boolean fag, final String mobile,
                            final String userId,final AgentWebSiteDomain agentInfo) {
    	if(StringUtils.isNotBlank(agentInfo.getAgentId()) && !"0".equals(agentInfo.getAgentId())) {
    		executorService.submit(new Runnable() {
                @Override
                public void run() {
                    sendMessageProc(source, fag, mobile, userId,agentInfo);
                }
            });
    	}        
    }


    /**
     * 发送短信 通知
     *
     * @param source 来源
     * @param fag    true 发送成功检测完成短信 false 发送检测失败短信
     */
    public void sendMessageProc(String source, Boolean fag, String mobile, String userId,AgentWebSiteDomain agentInfo) {

        if (fag) {
            if ("pc1.0".equals(source)) {
                // 发送短信
            	SmallSmsUtil.getInstance().sendSmsByMobileForTest(mobile,agentInfo);
            } else if ("cl_sr1.0".equals(source)) {
            	SmallSmsUtil.getInstance().sendSmsByMobileForTest(mobile,agentInfo);
            } else {
                // 发送短信
                ChuangLanSmsUtil.getInstance().sendSmsByMobileForZZTTest(mobile);
            }
        } else {
            if ("pc1.0".equals(source)) {
                // 发送短信
            	SmallSmsUtil.getInstance().sendSmsByMobileForTestEx(mobile,agentInfo);
            } else if ("cl_sr1.0".equals(source)) {
                // 发送短信
            	SmallSmsUtil.getInstance().sendSmsByMobileForTestEx(mobile,agentInfo);
            } else {
                // 异常发送短信
                ChuangLanSmsUtil.getInstance().sendSmsByMobileForTestZZtEx(mobile);
            }
        }

    }
    
    /**
	 * 解析字符串
	 *
	 * @return
	 */
	public static JSONObject getParamsJsonString(String params) {
		JSONObject resultJson = new JSONObject();
		String[] paramList = params.replace("{", "").replace("}", "").split(",");
		if (paramList != null && paramList.length > 0) {
			for (String param : paramList) {
				String paramname = param.substring(0, param.indexOf("=")).trim();
				String paramvalue = param.substring(param.indexOf("=") + 1, param.length()).trim();
				resultJson.put(paramname, paramvalue);
			}
		}
		return resultJson;
	}
    
    private AgentWebSiteDomain getAgentInfoByCreUserId(String creUserId){
    	AgentWebSiteDomain result = new AgentWebSiteDomain();
		String url = withUserProviderService + getAgentInfoUrl + "?creUserId=" + creUserId;
		JSONObject resultConsume = restTemplate.getForObject(url, JSONObject.class);		
		if (null != resultConsume && resultConsume.getString("resultCode").equals(ResultCode.RESULT_SUCCEED)) {
			JSONObject resultObj  = new JSONObject();
			String tempStr = resultConsume.getString("resultObj");
			if(tempStr.contains("=")){
				resultObj = getParamsJsonString(tempStr);
			}else{
				resultObj  = JSONObject.parseObject(resultConsume.getString("resultObj"));
			}			
			result.setAgentId(resultObj.getString("agentId"));
			result.setDomain(resultObj.getString("domain"));
			result.setSmsSign(resultObj.getString("smsSign"));
		}
		return result;		
	}
    
    private int getPauseSecond(int size){
    	int result = 2;
    	if(size >= 6000 && size < 12000){
    		result = 3;
    	}else if(size >= 12000 && size < 50000){
    		result = 4;
    	}else if(size >= 50000 && size < 500000){
    		result = 5;
    	}else if(size >= 3000 && size < 6000){
    		result = 2;
    	}else{
    		result = 6;
    	}
		return result;    	
    }
    
    

    //获取文本路径
    private String getTxtPath(FileUpload fileUpload, TxtSuffixEnum txtSuffixEnum) {
        return fileUpload.getFileUploadUrl() + "_" + txtSuffixEnum.getTxtSuffix();
    }
    
    /**
     * 保存部分检测好的结果存到redis用于前端展示
     */
    private void saveDateToRedis(String userId,String fileCode,ListMultimap<MobileReportGroupEnum, String> group){
    	JSONArray result = new JSONArray();
    	List<String> realOne = group.get(MobileReportGroupEnum.FileDetection.REAL_ONE);
    	List<String> realTwo = group.get(MobileReportGroupEnum.FileDetection.REAL_TWO);
    	List<String> emptyOne = group.get(MobileReportGroupEnum.FileDetection.EMPTY_ONE);
    	List<String> emptyTwo = group.get(MobileReportGroupEnum.FileDetection.EMPTY_TWO);
    	List<String> shut = group.get(MobileReportGroupEnum.FileDetection.SHUT);
    	List<String> silence = group.get(MobileReportGroupEnum.FileDetection.NO_RESULT);
    	//放入7个风险号
    	if(shut != null && shut.size() >= 0){
    		for(String mobile : shut){
        		JSONObject param = new JSONObject();
        		param.put("mobile", mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
        		param.put("color", "red");
        		result.add(param);
        		if(result.size()>7){
        			break;
        		}    		
        	}
    	}
    	
    	//放入7个沉默号
    	if(silence != null && silence.size() >= 0){
    		for(String mobile : silence){
        		JSONObject param = new JSONObject();
        		param.put("mobile", mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
        		param.put("color", "yellow");
        		result.add(param);
        		if(result.size()>14){
        			break;
        		}    		
        	}
    	}
    	
    	//放入8个空号
    	if(emptyOne != null && emptyOne.size() >= 0){
    		for(String mobile : emptyOne){
        		JSONObject param = new JSONObject();
        		param.put("mobile", mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
        		param.put("color", "gray");
        		result.add(param);
        		if(result.size()>22){
        			break;
        		}    		
        	}
    	}
    	
    	//放入50个实号
    	if(realOne != null && realOne.size() >= 0){
    		for(String mobile : realOne){
        		JSONObject param = new JSONObject();
        		param.put("mobile", mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
        		param.put("color", "blue");
        		result.add(param);
        		if(result.size()>72){
        			break;
        		}    		
        	}
    	}
    	
    	if(result.size()<72){
    		if(realTwo != null && realTwo.size() >= 0){
    			for(String mobile : realTwo){
        			JSONObject param = new JSONObject();
            		param.put("mobile", mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
            		param.put("color", "blue");
            		result.add(param);
            		if(result.size()>72){
            			break;
            		}    		
            	}
        	}    		
    	}
    	
    	if(result.size()<72){
    		if(emptyTwo != null && emptyTwo.size() >= 0){
    			for(String mobile : emptyTwo){
        			JSONObject param = new JSONObject();
            		param.put("mobile", mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
            		param.put("color", "gray");
            		result.add(param);
            		if(result.size()>72){
            			break;
            		}    		
            	}
        	}    		
    	}
    	
    	if(result.size()<72){
    		if(silence != null && silence.size() >= 0){
    			for(String mobile : silence){
        			JSONObject param = new JSONObject();
            		param.put("mobile", mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
            		param.put("color", "yellow");
            		result.add(param);
            		if(result.size()>72){
            			break;
            		}    		
            	}
        	}    		
    	}
    	
    	try {
    		if(result.size()>0){
    			redisClient.set(RedisKeys.getInstance().getMobileDisplayWebkey(userId, fileCode), result.toJSONString(), 60 * 10);
    		}			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("用户【" +userId+ "】保存文件：" + fileCode + "检测号码到redis异常,异常信息为：" + e.getMessage());
		}
    }
    
    private void saveDefaultMobileToRedis(String userId,String fileCode,List<String> mobileList){
    	JSONArray resultList = new JSONArray();
    	for(String mobile: mobileList){    		
    		JSONObject param = new JSONObject();
    		param.put("mobile", mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
    		param.put("color", (Long.parseLong(mobile))%6==0?"yellow":"blue");
    		resultList.add(param);
    	}
    	//数据保存到redis
    	redisClient.set(RedisKeys.getInstance().getMobileDisplayWebkey(userId, fileCode), resultList.toJSONString(), 60 * 10);
    }
}
