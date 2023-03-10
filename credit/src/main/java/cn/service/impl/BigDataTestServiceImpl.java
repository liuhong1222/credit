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
     * ????????????????????????????????????
     */
    private final static Integer BIG_DATA_API_REQUEST_SIZE = 2000;
    
    private final static String DEFAULT_CHARSET = "utf-8";
    
	/**
     * web ???????????????????????? ????????????
     *
     * @param fileUpload ????????????
     * @param userId     ??????id
     * @param mobile     ????????????
     * @param source     ??????
     * @return
     */
	@Override
	public BackResult<RunTestDomian> theTestNew(FileUpload fileUpload, String userId, String mobile,
                                              String source,String startLine) {
		int expire = 60 * 60 * 1000 * 3; // ???????????? ???3?????????
		int mobileCount = fileUpload.getFileRows();
		// redis??????????????????
        String succeedClearingCountkey = RedisKeys.getInstance().getkhSucceedClearingCountkey(userId, fileUpload.getId()); // ??????????????????
        // ????????????????????????????????????
        String exceptionkey = RedisKeys.getInstance().getkhExceptionkey(userId,fileUpload.getId()); // ????????????????????????key
        // ????????????????????????????????????
        String succeedTestCountkey = RedisKeys.getInstance().getkhSucceedTestCountkey(userId,fileUpload.getId()); // ??????????????????
        // ????????????rediskey
        String lockName = RedisKeys.getInstance().getkhTheTestFunKey(mobile, fileUpload.getId()); // ??????
        DistributedLockWrapper lock = new DistributedLockWrapper(jedisPool, lockName);
        try {
            // ??????
            String identifier = lock.lockWithTimeout(lockName, 800L, expire);
            if (null == identifier) {
                return new BackResult<RunTestDomian>(ResultCode.RESULT_SUCCEED, "????????????????????????????????????", new RunTestDomian("1", 0, fileUpload.getId()));
            }
                        
            logger.info("----------???????????????[" + userId + "]??????????????????????????????");
            // redis?????????
            emptyRedisService.initRedisKey(userId, fileUpload.getId(), expire, fileUpload.getFileRows(), identifier);
            
            File file = new File(fileUpload.getFileUploadUrl());
            if (!file.isFile() || !file.exists()) {
            	redisClient.set(exceptionkey, ResultCode.RESULT_FAILED, expire);            
                lock.releaseLock();
                return new BackResult<RunTestDomian>("999999", "?????????????????????????????????", new RunTestDomian("9", 0, fileUpload.getId()));
            }
            
            TxtFileContent txtFileContent = fileService.getValidMobileListByTxt(fileUpload.getFileUploadUrl());
            //????????????????????????
            String fileEncoding = txtFileContent.getFileCode();
            mobileCount = txtFileContent.getMobileCounts();
            
            //?????????????????????????????????
            if (mobileCount == 0) {
                redisClient.set(exceptionkey, ResultCode.RESULT_FAILED, expire);            
                lock.releaseLock();
                return new BackResult<RunTestDomian>("999999", "?????????????????????", new RunTestDomian("3", 0, fileUpload.getId()));
            }
            
            // ??????????????????????????????redis
            redisClient.set(succeedClearingCountkey, String.valueOf(mobileCount), expire * 2);
            // ????????????
            settlementService.freeWaccount(redisClient, expire, Boolean.TRUE,fileUpload.getId(), userId,mobileCount);
            // ?????????????????????redis
            saveDefaultMobileToRedis(userId, fileUpload.getId(), txtFileContent.getMobileList());
            // ????????????
            emptyNumFileDetectionByTxtNew(mobileCount, userId,succeedTestCountkey, expire,
                    source, mobile, lock,fileUpload, fileEncoding,startLine);
            
            lock.releaseLock();
            return new BackResult<RunTestDomian>(ResultCode.RESULT_SUCCEED, "???????????????", new RunTestDomian("1", getPauseSecond(mobileCount), fileUpload.getId()));
		} catch (Exception e) {
			lock.releaseLock();
			redisClient.set(exceptionkey, ResultCode.RESULT_FAILED, expire);
            settlementService.freeWaccount(redisClient, expire, Boolean.FALSE, fileUpload.getId(), userId,mobileCount);
            return new BackResult<RunTestDomian>("999999", "????????????", new RunTestDomian("3",0, fileUpload.getId()));
		}

    }
    
    /**
     * ??????????????????????????????????????????
     */
    private synchronized Future<?> emptyNumFileDetectionByTxtNew(int mobileCount, String userId,
                                                              String succeedTestCountkey,
                                                              Integer expire,
                                                              String source,
                                                              String mobile, DistributedLockWrapper lock,
                                                              FileUpload fileUpload, String fileEncoding,String startLine) {
    	// ??????????????????
        Runnable run = new Runnable() {
            @Override
            public void run() {
                String uid = fileUpload.getId();
                String exceptionkey = RedisKeys.getInstance().getkhExceptionkey(userId, uid); // ????????????????????????key
                Jedis jedis = null;
                int startCounts = StringUtils.isBlank(startLine)?0:Integer.parseInt(startLine);
                try {
                    jedis = jedisPool.getResource();
                    // ????????????????????????
                    String exceptions = jedis.get(exceptionkey);
                    if (exceptions.equals(ResultCode.RESULT_SUCCEED)) {
                        long beginTime = System.currentTimeMillis();                            
                        logger.info(">>>>>>>>>>>>>>> ???????????????????????? uid:" + uid + "?????????:" + mobileCount + ">>>>>>>>>>>>>>>");                
                        //?????????    ????????????????????????????????????2000???
                        int batchCount = (mobileCount-startCounts) / BIG_DATA_API_REQUEST_SIZE;
                        for (int i = 0; i < (batchCount + 1); i++) {
                            int fromIndex = BIG_DATA_API_REQUEST_SIZE * i + 1 + startCounts;
                            int toIndex = fromIndex + BIG_DATA_API_REQUEST_SIZE - 1;
                            if (toIndex > mobileCount) {
                                toIndex = mobileCount;
                            }
                            List<String> mobileSubList = TxtFileUtil.readTxt(getTxtPath(fileUpload, TxtSuffixEnum.ALL), fileEncoding, fromIndex, BIG_DATA_API_REQUEST_SIZE);
                            //????????????????????????????????????????????????
                            EmptyNumFileDetectionResult detectionResult = bigDataHttpService.emptyNumFileDetectionNew(mobileSubList,userId);
                            if (detectionResult != null && detectionResult.getData() != null) {
                                ListMultimap<MobileReportGroupEnum, String> group = detectionResult.getData();
                                //????????????????????????
                                saveGroupList(fileUpload, group);
                                //????????????????????????????????????redis??????????????????
                                saveDateToRedis(userId,uid,group);
                                //????????????????????????????????????
                                Set<String> noResultMobileSet = new HashSet<>(group.get(MobileReportGroupEnum.FileDetection.NO_RESULT));
                                saveNoResultMobileSet(fileUpload, noResultMobileSet);
                            }else{
                            	//???????????????????????????
                            	logger.error(">>>>>>>>>>>>>>> ???????????????????????????????????????????????????????????????????????? uid:" + uid + "???????????????:" + toIndex + "/" + mobileCount + ">>>>>>>>>>>>>>>");                                	
                            	//????????????????????????????????????????????????
                            	EmptyNumFileDetectionResult reDetectionResult = bigDataHttpService.emptyNumFileDetectionNew(mobileSubList,userId);
                            	if(reDetectionResult == null || reDetectionResult.getData() == null){
                            		//?????????????????????????????????????????????????????????
                            		logger.error(">>>>>>>>>>>>>>> ?????????????????????????????????????????????????????????????????????????????? uid:" + uid + "???????????????:" + toIndex + "/" + mobileCount + ">>>>>>>>>>>>>>>"); 
                            		TxtFileUtil.saveTxt(mobileSubList, getTxtPath(fileUpload, TxtSuffixEnum.SILENCE_ONE), DEFAULT_CHARSET, true);
                            	}else{
                                    ListMultimap<MobileReportGroupEnum, String> group = reDetectionResult.getData();
                                    //????????????????????????
                                    saveGroupList(fileUpload, group);  
                                    //????????????????????????????????????redis??????????????????
                                    saveDateToRedis(userId,uid,group);
                                    //????????????????????????????????????
                                    Set<String> noResultMobileSet = new HashSet<>(group.get(MobileReportGroupEnum.FileDetection.NO_RESULT));
                                    saveNoResultMobileSet(fileUpload, noResultMobileSet);
                            	}                              	                                	
                            }
                            
                            //??????1???
                            Thread.sleep(10);
                            logger.info(">>>>>>>>>>>>>>> ???????????????????????? uid:" + uid + "???????????????:" + toIndex + "/" + mobileCount + ">>>>>>>>>>>>>>>");
                            // ????????????????????????
                            redisClient.set(succeedTestCountkey, String.valueOf(toIndex), expire);
                        }
                        
                        //???????????????30???
                        Thread.sleep(20 * 1000);
                        logger.info(">>>>>>>>>>>>>>> ???????????????????????????????????????txt???uid:" + uid + "?????????:" + mobileCount + "?????????:" + (System.currentTimeMillis() - beginTime) + " >>>>>>>>>>>>>>>");
                        //??????????????????
                        generateResultFiles(fileUpload, userId, source, mobile, String.valueOf(mobileCount), expire);
                        logger.info(">>>>>>>>>>>>>>> ???????????????????????????uid:" + uid + "?????????:" + mobileCount + "?????????:" + (System.currentTimeMillis() - beginTime) + " >>>>>>>>>>>>>>>");
                    }

                } catch (Exception e) {
                    logger.error("{}, ?????????????????????????????????uid:{},info:",userId,fileUpload.getId(), e);
                    redisClient.set(exceptionkey, ResultCode.RESULT_FAILED, expire);
                    settlementService.freeWaccount(redisClient, expire, Boolean.FALSE, uid,userId,mobileCount);
                } finally {
                    // ??????????????????
                    jedis.close();
                    if (lock != null) {
                        lock.releaseLock();
                    }
                }
            }
        };
        // ???????????????????????????
        return threadExecutorService.execute(run);
    }
    
    /**
     * ??????????????????
     */
    private synchronized void generateResultFiles(FileUpload fileUpload, String userId, String source, String mobile,
                                                  String succeedClearingCount, Integer expire) {
        String uid = fileUpload.getId();
        String fileName = fileUpload.getFileName();
        try {
            String khTheRunkey = RedisKeys.getInstance().getkhTheRunkey(userId, uid);
            // ??????????????????
            TxtFileUtil.deleteFile(getTxtPath(fileUpload, TxtSuffixEnum.ALL));

            CvsFilePath cvsFilePath = new CvsFilePath();
            cvsFilePath.setUserId(userId);
            int totalCount = 0;

            List<File> list = new ArrayList<File>();
            String timeTemp = String.valueOf(System.currentTimeMillis());
            String subFilePath = DateUtils.getDate() + "/" + userId + "/" + timeTemp + "/";
            String filePath = loadfilePath + subFilePath;
            //??????????????????
            TxtFileUtil.appendTxt(getTxtPath(fileUpload, TxtSuffixEnum.REAL_ONE), filePath + "??????.txt", filePath, DEFAULT_CHARSET, true);
            TxtFileUtil.deleteFile(getTxtPath(fileUpload, TxtSuffixEnum.REAL_ONE));
            TxtFileUtil.appendTxt(getTxtPath(fileUpload, TxtSuffixEnum.REAL_TWO), filePath + "??????.txt", filePath, DEFAULT_CHARSET, true);
            TxtFileUtil.deleteFile(getTxtPath(fileUpload, TxtSuffixEnum.REAL_TWO));
            TxtFileUtil.appendTxt(getTxtPath(fileUpload, TxtSuffixEnum.REAL_THREE), filePath + "??????.txt", filePath, DEFAULT_CHARSET, true);
            TxtFileUtil.deleteFile(getTxtPath(fileUpload, TxtSuffixEnum.REAL_THREE));
            int countReal = TxtFileUtil.countLines(filePath + "??????.txt", DEFAULT_CHARSET);
            if (countReal > 0) {
            	totalCount += countReal;
                logger.info("----------??????????????????" + countReal);
                list.add(new File(filePath + "??????.txt"));
                cvsFilePath.setThereCount(countReal);
                cvsFilePath.setThereFilePath(subFilePath + "??????.txt");
                cvsFilePath.setThereFileSize(FileUtils.getFileSize(filePath + "??????.txt"));
            }
            // ??????????????????
            TxtFileUtil.appendTxt(getTxtPath(fileUpload, TxtSuffixEnum.KONG_ONE), filePath + "??????.txt", filePath, DEFAULT_CHARSET, true);
            TxtFileUtil.deleteFile(getTxtPath(fileUpload, TxtSuffixEnum.KONG_ONE));
            TxtFileUtil.appendTxt(getTxtPath(fileUpload, TxtSuffixEnum.KONG_TWO), filePath + "??????.txt", filePath, DEFAULT_CHARSET, true);
            TxtFileUtil.deleteFile(getTxtPath(fileUpload, TxtSuffixEnum.KONG_TWO));
            TxtFileUtil.appendTxt(getTxtPath(fileUpload, TxtSuffixEnum.KONG_THREE), filePath + "??????.txt", filePath, DEFAULT_CHARSET, true);
            TxtFileUtil.deleteFile(getTxtPath(fileUpload, TxtSuffixEnum.KONG_THREE));
            int countEmpty = TxtFileUtil.countLines(filePath + "??????.txt", DEFAULT_CHARSET);
            if (countEmpty > 0) {
            	totalCount += countEmpty;
                logger.info("----------??????????????????" + countEmpty);
                list.add(new File(filePath + "??????.txt"));
                cvsFilePath.setSixCount(countEmpty);
                cvsFilePath.setSixFilePath(subFilePath + "??????.txt");
                cvsFilePath.setSixFileSize(FileUtils.getFileSize(filePath + "??????.txt"));
            }
            // ?????????????????????
            TxtFileUtil.appendTxt(getTxtPath(fileUpload, TxtSuffixEnum.SILENCE_ONE), filePath + "?????????.txt", filePath, DEFAULT_CHARSET, true);
            TxtFileUtil.deleteFile(getTxtPath(fileUpload, TxtSuffixEnum.SILENCE_ONE));
            int countSilence = TxtFileUtil.countLines(filePath + "?????????.txt", DEFAULT_CHARSET);
            if (countSilence > 0) {
            	totalCount += countSilence;
                logger.info("----------?????????????????????" + countSilence);
                list.add(new File(filePath + "?????????.txt"));
                cvsFilePath.setUnknownSize(countSilence);
                cvsFilePath.setUnknownFilePath(subFilePath + "?????????.txt");
                cvsFilePath.setUnknownFileSize(FileUtils.getFileSize(filePath + "?????????.txt"));
            }
            // ?????????????????????
            TxtFileUtil.appendTxt(getTxtPath(fileUpload, TxtSuffixEnum.SHUTDOWN_ONE), filePath + "?????????.txt", filePath, DEFAULT_CHARSET, true);
            TxtFileUtil.deleteFile(getTxtPath(fileUpload, TxtSuffixEnum.SHUTDOWN_ONE));
            int countShutdown = TxtFileUtil.countLines(filePath + "?????????.txt", DEFAULT_CHARSET);
            if (countShutdown > 0) {
            	totalCount += countShutdown;
                logger.info("----------?????????????????????" + countShutdown);
                list.add(new File(filePath + "?????????.txt"));
                cvsFilePath.setShutCount(countShutdown);
                cvsFilePath.setShutFilePath(subFilePath + "?????????.txt");
                cvsFilePath.setShutFileSize(FileUtils.getFileSize(filePath + "?????????.txt"));
            }
            // ??????????????????
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
                String exceptionkey = RedisKeys.getInstance().getkhExceptionkey(userId, uid); // ????????????????????????key
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
        		wcd.setCount(Integer.valueOf(cvsFilePath.getTotalCount()).longValue()); // ??????
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

                // ????????? ????????????????????????
                redisClient.set(khTheRunkey, ResultCode.RESULT_FAILED, expire);
                logger.info("----------???????????????[" + userId + "]?????????????????????????????? ?????????????????????"
                        + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            }

        } catch (Exception e) {
            String exceptionkey = RedisKeys.getInstance().getkhExceptionkey(userId, uid); // ????????????????????????key
            redisClient.set(exceptionkey, ResultCode.RESULT_FAILED, expire);
            settlementService.freeWaccount(redisClient, expire, Boolean.FALSE, uid, userId,Integer.valueOf(succeedClearingCount));
            logger.error("{},??????????????????????????????????????????????????????uid:{},info:",userId,uid,e);
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
    
    //???????????????
    private void saveNoResultMobileSet(FileUpload fileUpload, Set<String> noResultMobileSet) throws IOException {
        if (!CollectionUtils.isEmpty(noResultMobileSet)) {
            Map<String, String> mobileAndNumberSectionMap = new HashMap<>();
            Set<String> numberSectionSet = new HashSet<>();
            //????????????????????????map
            for (String m : noResultMobileSet) {
                numberSectionSet.add(m.substring(0, 7));
                mobileAndNumberSectionMap.put(m, m.substring(0, 7));
            }
            //?????????????????????
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
                    //????????????????????????????????????
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
     * ?????????????????????????????????
     */
    private void saveOneGroupList(String filePath,
                                  ListMultimap<MobileReportGroupEnum, String> group,
                                  MobileReportGroupEnum.FileDetection oneFileDetectionEnum) throws IOException {
        List<String> oneDelivrdMobileList = group.get(oneFileDetectionEnum);
        TxtFileUtil.saveTxt(oneDelivrdMobileList, filePath, DEFAULT_CHARSET, true);
    }

    /**
     * ????????????????????????
     */
    private void saveGroupList(FileUpload fileUpload, ListMultimap<MobileReportGroupEnum, String> group) throws IOException {
        //????????????1???
        saveOneGroupList(getTxtPath(fileUpload, TxtSuffixEnum.REAL_ONE), group, MobileReportGroupEnum.FileDetection.REAL_ONE);
        //????????????2???
        saveOneGroupList(getTxtPath(fileUpload, TxtSuffixEnum.REAL_TWO), group, MobileReportGroupEnum.FileDetection.REAL_TWO);
        //????????????3???
        saveOneGroupList(getTxtPath(fileUpload, TxtSuffixEnum.REAL_THREE), group, MobileReportGroupEnum.FileDetection.REAL_THREE);
        //????????????1???
        saveOneGroupList(getTxtPath(fileUpload, TxtSuffixEnum.KONG_ONE), group, MobileReportGroupEnum.FileDetection.EMPTY_ONE);
        //????????????2???
        saveOneGroupList(getTxtPath(fileUpload, TxtSuffixEnum.KONG_TWO), group, MobileReportGroupEnum.FileDetection.EMPTY_TWO);
        //????????????3???
        saveOneGroupList(getTxtPath(fileUpload, TxtSuffixEnum.KONG_THREE), group, MobileReportGroupEnum.FileDetection.EMPTY_THREE);
        //?????????
        saveOneGroupList(getTxtPath(fileUpload, TxtSuffixEnum.SILENCE_ONE), group, MobileReportGroupEnum.FileDetection.SILENCE);
        //?????????
        saveOneGroupList(getTxtPath(fileUpload, TxtSuffixEnum.KONG_THREE), group, MobileReportGroupEnum.FileDetection.OUT_SERVICE);
        //???????????? ????????????
        saveOneGroupList(getTxtPath(fileUpload, TxtSuffixEnum.SILENCE_ONE), group, MobileReportGroupEnum.FileDetection.UNKNOWN);
        //?????????
        saveOneGroupList(getTxtPath(fileUpload, TxtSuffixEnum.SHUTDOWN_ONE), group, MobileReportGroupEnum.FileDetection.SHUT);
    }    
    
  //??????????????????
    ExecutorService executorService = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    /**
     * ???????????? ??????
     *
     * @param source ??????
     * @param fag    true ?????????????????????????????? false ????????????????????????
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
     * ???????????? ??????
     *
     * @param source ??????
     * @param fag    true ?????????????????????????????? false ????????????????????????
     */
    public void sendMessageProc(String source, Boolean fag, String mobile, String userId,AgentWebSiteDomain agentInfo) {

        if (fag) {
            if ("pc1.0".equals(source)) {
                // ????????????
            	SmallSmsUtil.getInstance().sendSmsByMobileForTest(mobile,agentInfo);
            } else if ("cl_sr1.0".equals(source)) {
            	SmallSmsUtil.getInstance().sendSmsByMobileForTest(mobile,agentInfo);
            } else {
                // ????????????
                ChuangLanSmsUtil.getInstance().sendSmsByMobileForZZTTest(mobile);
            }
        } else {
            if ("pc1.0".equals(source)) {
                // ????????????
            	SmallSmsUtil.getInstance().sendSmsByMobileForTestEx(mobile,agentInfo);
            } else if ("cl_sr1.0".equals(source)) {
                // ????????????
            	SmallSmsUtil.getInstance().sendSmsByMobileForTestEx(mobile,agentInfo);
            } else {
                // ??????????????????
                ChuangLanSmsUtil.getInstance().sendSmsByMobileForTestZZtEx(mobile);
            }
        }

    }
    
    /**
	 * ???????????????
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
    
    

    //??????????????????
    private String getTxtPath(FileUpload fileUpload, TxtSuffixEnum txtSuffixEnum) {
        return fileUpload.getFileUploadUrl() + "_" + txtSuffixEnum.getTxtSuffix();
    }
    
    /**
     * ????????????????????????????????????redis??????????????????
     */
    private void saveDateToRedis(String userId,String fileCode,ListMultimap<MobileReportGroupEnum, String> group){
    	JSONArray result = new JSONArray();
    	List<String> realOne = group.get(MobileReportGroupEnum.FileDetection.REAL_ONE);
    	List<String> realTwo = group.get(MobileReportGroupEnum.FileDetection.REAL_TWO);
    	List<String> emptyOne = group.get(MobileReportGroupEnum.FileDetection.EMPTY_ONE);
    	List<String> emptyTwo = group.get(MobileReportGroupEnum.FileDetection.EMPTY_TWO);
    	List<String> shut = group.get(MobileReportGroupEnum.FileDetection.SHUT);
    	List<String> silence = group.get(MobileReportGroupEnum.FileDetection.NO_RESULT);
    	//??????7????????????
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
    	
    	//??????7????????????
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
    	
    	//??????8?????????
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
    	
    	//??????50?????????
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
			logger.error("?????????" +userId+ "??????????????????" + fileCode + "???????????????redis??????,??????????????????" + e.getMessage());
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
    	//???????????????redis
    	redisClient.set(RedisKeys.getInstance().getMobileDisplayWebkey(userId, fileCode), resultList.toJSONString(), 60 * 10);
    }
}
