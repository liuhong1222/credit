package cn.service.impl;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import cn.entity.*;
import cn.service.*;
import cn.utils.*;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import cn.redis.RedisClient;
import cn.service.without.SettlementService;
import main.java.cn.common.BackResult;
import main.java.cn.common.RedisKeys;
import main.java.cn.common.ResultCode;
import main.java.cn.domain.AgentWebSiteDomain;
import main.java.cn.domain.CvsFilePathDomain;
import main.java.cn.domain.RunTestDomian;
import main.java.cn.domain.page.PageDomain;
import main.java.cn.sms.util.ChuangLanSmsUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisException;

@Service
public class ForeignServiceImpl implements ForeignService {

    private final static Logger logger = LoggerFactory.getLogger(ForeignServiceImpl.class);
    @Value("${server.port}")
    private String port;

    @Value("${loadfilePath}")
    private String loadfilePath;

    @Autowired
    private JedisPool jedisPool;

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private CvsFilePathService cvsFilePathService;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private BigDataTestService bigDataTestService;

    @Autowired
    DetectionChannelService detectionChannelService;
    
    @Value("${withUserProviderService}")
	private String withUserProviderService;
	
	@Value("${getResultPwdUrl}")
	private String getResultPwdUrl;
	
	@Value("${withUserProviderServiceNoClient}")
	private String withUserProviderServiceNoClient;
	
	@Value("${getAgentInfoUrl}")
	private String getAgentInfoUrl;

    @Deprecated
    @Override
    public BackResult<RunTestDomian> runTheTest(String fileUrl, String userId, String timestamp,
                                                String mobile) {
        BackResult<RunTestDomian> result = new BackResult<RunTestDomian>();
        return result;
    }


    @Override
    public BackResult<List<CvsFilePathDomain>> findByUserId(String userId) {

        BackResult<List<CvsFilePathDomain>> result = new BackResult<List<CvsFilePathDomain>>();

        List<CvsFilePathDomain> list = new ArrayList<CvsFilePathDomain>();

        try {
            List<CvsFilePath> listCvsFilePath = cvsFilePathService.findByUserId(userId);

            if (CommonUtils.isNotEmpty(listCvsFilePath)) {
                result.setResultMsg("改用户没有订单信息");
            }

            for (CvsFilePath cvsFilePath : listCvsFilePath) {
                CvsFilePathDomain domain = new CvsFilePathDomain();
                BeanUtils.copyProperties(cvsFilePath, domain);
                list.add(domain);
            }

            result.setResultObj(list);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("客户ID：[" + userId + "]查询下载列表系统异常：" + e.getMessage());
            result.setResultCode(ResultCode.RESULT_FAILED);
            result.setResultMsg("客户ID：[" + userId + "]查询下载列表系统异常：" + e.getMessage());
        }

        return result;
    }


    @Override
    public BackResult<Boolean> deleteCvsByIds(String ids, String userId) {

        logger.info("用户ID：【" + userId + "】执行删除下载记录");

        BackResult<Boolean> result = new BackResult<Boolean>();

        try {
            cvsFilePathService.deleteByIds(ids);
            result.setResultObj(Boolean.TRUE);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("客户ID：[" + userId + "]执行删除下载记录系统异常：" + e.getMessage());
            result.setResultCode(ResultCode.RESULT_FAILED);
            result.setResultMsg("客户ID：[" + userId + "]查询下载列表系统异常：" + e.getMessage());
            result.setResultObj(Boolean.FALSE);
        }
        return result;
    }

    @Override
    public BackResult<PageDomain<CvsFilePathDomain>> getPageByUserId(int pageNo, int pageSize,
                                                                     String userId) {
        BackResult<PageDomain<CvsFilePathDomain>> result = new BackResult<PageDomain<CvsFilePathDomain>>();

        PageDomain<CvsFilePathDomain> pageDomain = new PageDomain<CvsFilePathDomain>();

        try {
            Page<CvsFilePath> page = cvsFilePathService.getPageByUserId(pageNo, pageSize, userId);

            if (null != page) {
                pageDomain.setTotalNumber(Integer.valueOf(String.valueOf(page.getTotalElements())));
                pageDomain.setTotalPages(page.getTotalPages());
                pageDomain.setNumPerPage(pageSize);
                pageDomain.setCurrentPage(pageNo);

                if (!CommonUtils.isNotEmpty(page.getContent())) {

                    List<CvsFilePathDomain> listDomian = new ArrayList<CvsFilePathDomain>();
                    for (CvsFilePath mobileTestLog : page.getContent()) {
                        CvsFilePathDomain domain = new CvsFilePathDomain();
                        BeanUtils.copyProperties(mobileTestLog, domain);
                        listDomian.add(domain);
                    }

                    pageDomain.setTlist(listDomian);
                }

            }

            result.setResultObj(pageDomain);
            result.setResultMsg("获取成功");

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取实号检测下载列表异常：" + e.getMessage());
            result.setResultMsg("系统异常");
            result.setResultCode(ResultCode.RESULT_FAILED);
        }
        return result;
    }
    
    @Override
    public BackResult<PageDomain<CvsFilePathDomain>> getCVSPageByUserIdNew(int pageNo, int pageSize,
                                                                     String userId,String startDate,String endDate) {
        BackResult<PageDomain<CvsFilePathDomain>> result = new BackResult<PageDomain<CvsFilePathDomain>>();

        PageDomain<CvsFilePathDomain> pageDomain = new PageDomain<CvsFilePathDomain>();

        try {
        	Date sd = DateUtils.parseDate(startDate);
        	Date ed = DateUtils.addDay(DateUtils.parseDate(endDate), 1);
            Page<CvsFilePath> page = cvsFilePathService.getPageByUserIdNew(pageNo, pageSize, userId,sd,ed);

            if (null != page) {
                pageDomain.setTotalNumber(Integer.valueOf(String.valueOf(page.getTotalElements())));
                pageDomain.setTotalPages(page.getTotalPages());
                pageDomain.setNumPerPage(pageSize);
                pageDomain.setCurrentPage(pageNo);

                if (!CommonUtils.isNotEmpty(page.getContent())) {

                    List<CvsFilePathDomain> listDomian = new ArrayList<CvsFilePathDomain>();
                    for (CvsFilePath mobileTestLog : page.getContent()) {
                        CvsFilePathDomain domain = new CvsFilePathDomain();
                        BeanUtils.copyProperties(mobileTestLog, domain);
                        listDomian.add(domain);
                    }

                    pageDomain.setTlist(listDomian);
                }

            }

            result.setResultObj(pageDomain);
            result.setResultMsg("获取成功");

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取实号检测下载列表异常：" + e.getMessage());
            result.setResultMsg("系统异常");
            result.setResultCode(ResultCode.RESULT_FAILED);
        }
        return result;
    }

    /**
     * 清空条数注销锁
     *
     * @param userId
     * @param mobile
     */
    private void clearLockAndCountForRun(String userId, String mobile, String uid) {
        String lockName = RedisKeys.getInstance().getkhTheTestFunKey(mobile, uid);
        String KhTestCountKey = RedisKeys.getInstance().getKhTestCountKey(userId, uid);
        String succeedTestCountkey = RedisKeys.getInstance().getkhSucceedTestCountkey(userId, uid);
        String redisLockIdentifier = RedisKeys.getInstance().getkhRedisLockIdentifier(userId, uid);
        String identifier = redisClient.get(redisLockIdentifier);
        String generateResultskey = RedisKeys.getInstance().getkhGenerateResultskey(userId, uid); // 空号检测线程key
        String exceptionkey = RedisKeys.getInstance().getkhExceptionkey(userId, uid); // 线程执行全局异常key
        // String realOneListkey =
        // RedisKeys.getInstance().getkhRealOneListtkey(userId); // 实号列表
        // String realTwoListkey =
        // RedisKeys.getInstance().getkhRealTwoListtkey(userId); // 实号列表
        // String realThreeListkey =
        // RedisKeys.getInstance().getkhRealThreeListtkey(userId); // 实号列表
        // String kongOneListtkey =
        // RedisKeys.getInstance().getkhKongOneListtkey(userId); // 空号列表
        // String kongTwoListtkey =
        // RedisKeys.getInstance().getkhKongTwoListtkey(userId); // 空号列表
        // String kongThreeListtkey =
        // RedisKeys.getInstance().getkhKongThreeListtkey(userId); // 空号列表
        // String kongFourListtkey =
        // RedisKeys.getInstance().getkhKongFourListtkey(userId); // 空号列表
        // String kongFiveListtkey =
        // RedisKeys.getInstance().getkhKongFiveListtkey(userId);// 空号列表
        // String silenceListtkey =
        // RedisKeys.getInstance().getkhSilenceListtkey(userId); // 沉默号列表
        String khTheRunkey = RedisKeys.getInstance().getkhTheRunkey(userId, uid); // 程序是否运行结束
        // 清空 记录到redis的条数
        redisClient.remove(KhTestCountKey);
        redisClient.remove(succeedTestCountkey);
        redisClient.remove(generateResultskey);
        redisClient.remove(exceptionkey);
        // redisClient.remove(realOneListkey);
        // redisClient.remove(realTwoListkey);
        // redisClient.remove(realThreeListkey);
        // redisClient.remove(kongOneListtkey);
        // redisClient.remove(kongTwoListtkey);
        // redisClient.remove(kongThreeListtkey);
        // redisClient.remove(kongFourListtkey);
        // redisClient.remove(kongFiveListtkey);
        // redisClient.remove(silenceListtkey);
        redisClient.remove(khTheRunkey);
        this.releaseLock(lockName, identifier); // 注销锁
        redisClient.remove(identifier);
    }


    /**
     * 释放锁
     *
     * @param lockName   锁的key
     * @param identifier 释放锁的标识
     * @return
     */
    private boolean releaseLock(String lockName, String identifier) {
        Jedis conn = null;
        String lockKey = "lock:" + lockName;
        boolean retFlag = false;
        try {
            conn = jedisPool.getResource();
            while (true) {
                // 监视lock，准备开始事务
                conn.watch(lockKey);
                // 通过前面返回的value值判断是不是该锁，若是该锁，则删除，释放锁
                if (identifier.equals(conn.get(lockKey))) {
                    Transaction transaction = conn.multi();
                    transaction.del(lockKey);
                    List<Object> results = transaction.exec();
                    if (results == null) {
                        continue;
                    }
                    retFlag = true;
                }
                conn.unwatch();
                break;
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return retFlag;
    }


    @Override
    public BackResult<RunTestDomian> theTest(String fileUrl, String userId, String userName,
                                             String source, String startLine, String type) {
        FileUpload fileUpload = fileUploadService.findByOne(fileUrl);        
        if (null == fileUpload) {
        	String fileUploadStr = redisClient.get(RedisKeys.getInstance().getWanShuFileUploadkey(fileUrl));
        	if(StringUtils.isBlank(fileUploadStr)){
        		return new BackResult<RunTestDomian>(ResultCode.RESULT_DATA_EXCEPTIONS,
                        "文件检测异常，没有检测到可以检测的文件！");
        	}
        	fileUpload = JSONObject.parseObject(fileUploadStr,FileUpload.class);            
        }
        
        if (type.equals("1")) {
            return bigDataTestService.theTestNew(fileUpload, userId, userName, source,startLine);
        } else {
            return findTheTestRunStatus(userId, startLine, fileUpload.getFileUploadUrl(), source,
            		userName, fileUpload.getId());
        }

    }

    private BackResult<RunTestDomian> findTheTestRunStatus(String userId, String startLine,
                                                           String fileUrl, String source,
                                                           String userName, String uid) {

        RunTestDomian runTestDomian = new RunTestDomian();
        BackResult<RunTestDomian> result = new BackResult<RunTestDomian>();

        // 出现异常终止检测
        String exceptionkey = RedisKeys.getInstance().getkhExceptionkey(userId, uid); // 线程执行全局异常key

        int expire = 60 * 60 * 1000 * 3; // 超时时间 （3小时）

        try {
            String KhTestCountKey = RedisKeys.getInstance().getKhTestCountKey(userId, uid); // 获取空号检测
            // 需要检测的总条数key
            // （根据文件获取的总条数）
            String succeedTestCountkey = RedisKeys.getInstance().getkhSucceedTestCountkey(userId,
                    uid); // 获取空号检测
            // 已经成功检测的总条数（运行中，不考虑不计费的条数）
            String KhTestCount = redisClient.get(KhTestCountKey);

            String exceptions = redisClient.get(exceptionkey);
            //if (exceptions != null && exceptions.equals(ResultCode.RESULT_FAILED)) {
            if (StringUtils.isNotBlank(exceptions) && exceptions.equals(ResultCode.RESULT_FAILED)) {
                this.clearLockAndCountForRun(userId, userName, uid);
                //this.sendMessage(source, Boolean.FALSE, mobile, userId);
                result.setResultMsg("系统异常");
                runTestDomian.setRunCount(0);
                runTestDomian.setStatus("3"); // 系统异常
                return result;
            }

            if (!CommonUtils.isNotString(KhTestCount)) {
                String succeedTestCount = redisClient.get(succeedTestCountkey);
                succeedTestCount = !CommonUtils.isNotString(succeedTestCount) ? succeedTestCount
                        : "0";
                runTestDomian.setRunCount(Integer.valueOf(succeedTestCount.toString())); // 设置运行的总条数
                runTestDomian.setMobiles(
                        FileUtils.getFileMenu(fileUrl, Integer.parseInt(startLine), 100)); // 设置已经检测了的手机号码
                logger.info(
                        "----------需要检测的总条数: 【" + KhTestCount + "】，已经检测完成的条数:" + succeedTestCount);

                String khTheRunkey = RedisKeys.getInstance().getkhTheRunkey(userId, uid); // 程序是否运行结束
                String khTheRun = redisClient.get(khTheRunkey);

                if (khTheRun != null && khTheRun.equals(ResultCode.RESULT_SUCCEED)) {
                    result.setResultMsg("任务执行中");
                    runTestDomian.setStatus("1"); // 1执行中 2执行结束 3执行异常
                } else {
                    result.setResultMsg("任务执行结束");
                    runTestDomian.setStatus("2"); // 1执行中 2执行结束 3执行异常
                    this.clearLockAndCountForRun(userId, userName, uid);
                }

            } else {
                result.setResultMsg("该账户没有正在检测的程序进程");
                runTestDomian.setRunCount(0);
                runTestDomian.setStatus("6"); // 没有在执行的检测
            }

            runTestDomian.setCode(uid);
            result.setResultObj(runTestDomian);
        } catch (Exception e) {
            logger.error("----------客户ID：[" + userId + "]执行号码检测出现系统异常：",e);
            redisClient.set(exceptionkey, ResultCode.RESULT_FAILED, expire);
        }

        return result;
    }
    
    //异步发送短信
    ExecutorService executorService = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Override
    public BackResult<String> getTxtZipByIds(String ids, String userId) {
        logger.info("用户ID：【" + userId + "执行批量下载记录");
        BackResult<String> result = new BackResult<String>();
        List<File> list = new ArrayList<File>();
        try {
            List<CvsFilePath> cfpList = cvsFilePathService.getTxtZipByIds(ids);
            if (cfpList != null && cfpList.size() > 0) {
                for (CvsFilePath cfp : cfpList) {
                    String zipPath = cfp.getZipPath();
                    String filePath = loadfilePath + zipPath;
                    list.add(new File(filePath));
                }

                String subZipsPath = DateUtils.getDate() + "/" + userId + "/" + System.currentTimeMillis() + ".zip";
                String zipsPath = loadfilePath + subZipsPath;
                FileUtils.createZip(list, zipsPath);
                result.setResultObj(subZipsPath);
            } else {
                logger.error("客户ID：[" + userId + "]执行批量下载记录失败，没有可下载的文件");
                result.setResultCode(ResultCode.RESULT_FAILED);
                result.setResultMsg("客户ID：[" + userId + "]批量下载列表失败，没有可下载的文件");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("客户ID：[" + userId + "]执行批量下载记录系统异常：" + e.getMessage());
            result.setResultCode(ResultCode.RESULT_FAILED);
            result.setResultMsg("客户ID：[" + userId + "]批量下载列表系统异常");
        }

        return result;
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
            // 发送短信
            ChuangLanSmsUtil.getInstance().sendSmsByMobileForTest(mobile,agentInfo);
        } else {
            // 发送短信
            ChuangLanSmsUtil.getInstance().sendSmsByMobileForTestEx(mobile,agentInfo);            
        }

    }
}
