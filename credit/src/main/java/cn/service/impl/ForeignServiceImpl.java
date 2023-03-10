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
                result.setResultMsg("???????????????????????????");
            }

            for (CvsFilePath cvsFilePath : listCvsFilePath) {
                CvsFilePathDomain domain = new CvsFilePathDomain();
                BeanUtils.copyProperties(cvsFilePath, domain);
                list.add(domain);
            }

            result.setResultObj(list);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("??????ID???[" + userId + "]?????????????????????????????????" + e.getMessage());
            result.setResultCode(ResultCode.RESULT_FAILED);
            result.setResultMsg("??????ID???[" + userId + "]?????????????????????????????????" + e.getMessage());
        }

        return result;
    }


    @Override
    public BackResult<Boolean> deleteCvsByIds(String ids, String userId) {

        logger.info("??????ID??????" + userId + "???????????????????????????");

        BackResult<Boolean> result = new BackResult<Boolean>();

        try {
            cvsFilePathService.deleteByIds(ids);
            result.setResultObj(Boolean.TRUE);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("??????ID???[" + userId + "]???????????????????????????????????????" + e.getMessage());
            result.setResultCode(ResultCode.RESULT_FAILED);
            result.setResultMsg("??????ID???[" + userId + "]?????????????????????????????????" + e.getMessage());
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
            result.setResultMsg("????????????");

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("???????????????????????????????????????" + e.getMessage());
            result.setResultMsg("????????????");
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
            result.setResultMsg("????????????");

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("???????????????????????????????????????" + e.getMessage());
            result.setResultMsg("????????????");
            result.setResultCode(ResultCode.RESULT_FAILED);
        }
        return result;
    }

    /**
     * ?????????????????????
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
        String generateResultskey = RedisKeys.getInstance().getkhGenerateResultskey(userId, uid); // ??????????????????key
        String exceptionkey = RedisKeys.getInstance().getkhExceptionkey(userId, uid); // ????????????????????????key
        // String realOneListkey =
        // RedisKeys.getInstance().getkhRealOneListtkey(userId); // ????????????
        // String realTwoListkey =
        // RedisKeys.getInstance().getkhRealTwoListtkey(userId); // ????????????
        // String realThreeListkey =
        // RedisKeys.getInstance().getkhRealThreeListtkey(userId); // ????????????
        // String kongOneListtkey =
        // RedisKeys.getInstance().getkhKongOneListtkey(userId); // ????????????
        // String kongTwoListtkey =
        // RedisKeys.getInstance().getkhKongTwoListtkey(userId); // ????????????
        // String kongThreeListtkey =
        // RedisKeys.getInstance().getkhKongThreeListtkey(userId); // ????????????
        // String kongFourListtkey =
        // RedisKeys.getInstance().getkhKongFourListtkey(userId); // ????????????
        // String kongFiveListtkey =
        // RedisKeys.getInstance().getkhKongFiveListtkey(userId);// ????????????
        // String silenceListtkey =
        // RedisKeys.getInstance().getkhSilenceListtkey(userId); // ???????????????
        String khTheRunkey = RedisKeys.getInstance().getkhTheRunkey(userId, uid); // ????????????????????????
        // ?????? ?????????redis?????????
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
        this.releaseLock(lockName, identifier); // ?????????
        redisClient.remove(identifier);
    }


    /**
     * ?????????
     *
     * @param lockName   ??????key
     * @param identifier ??????????????????
     * @return
     */
    private boolean releaseLock(String lockName, String identifier) {
        Jedis conn = null;
        String lockKey = "lock:" + lockName;
        boolean retFlag = false;
        try {
            conn = jedisPool.getResource();
            while (true) {
                // ??????lock?????????????????????
                conn.watch(lockKey);
                // ?????????????????????value???????????????????????????????????????????????????????????????
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
                        "????????????????????????????????????????????????????????????");
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

        // ????????????????????????
        String exceptionkey = RedisKeys.getInstance().getkhExceptionkey(userId, uid); // ????????????????????????key

        int expire = 60 * 60 * 1000 * 3; // ???????????? ???3?????????

        try {
            String KhTestCountKey = RedisKeys.getInstance().getKhTestCountKey(userId, uid); // ??????????????????
            // ????????????????????????key
            // ????????????????????????????????????
            String succeedTestCountkey = RedisKeys.getInstance().getkhSucceedTestCountkey(userId,
                    uid); // ??????????????????
            // ???????????????????????????????????????????????????????????????????????????
            String KhTestCount = redisClient.get(KhTestCountKey);

            String exceptions = redisClient.get(exceptionkey);
            //if (exceptions != null && exceptions.equals(ResultCode.RESULT_FAILED)) {
            if (StringUtils.isNotBlank(exceptions) && exceptions.equals(ResultCode.RESULT_FAILED)) {
                this.clearLockAndCountForRun(userId, userName, uid);
                //this.sendMessage(source, Boolean.FALSE, mobile, userId);
                result.setResultMsg("????????????");
                runTestDomian.setRunCount(0);
                runTestDomian.setStatus("3"); // ????????????
                return result;
            }

            if (!CommonUtils.isNotString(KhTestCount)) {
                String succeedTestCount = redisClient.get(succeedTestCountkey);
                succeedTestCount = !CommonUtils.isNotString(succeedTestCount) ? succeedTestCount
                        : "0";
                runTestDomian.setRunCount(Integer.valueOf(succeedTestCount.toString())); // ????????????????????????
                runTestDomian.setMobiles(
                        FileUtils.getFileMenu(fileUrl, Integer.parseInt(startLine), 100)); // ????????????????????????????????????
                logger.info(
                        "----------????????????????????????: ???" + KhTestCount + "?????????????????????????????????:" + succeedTestCount);

                String khTheRunkey = RedisKeys.getInstance().getkhTheRunkey(userId, uid); // ????????????????????????
                String khTheRun = redisClient.get(khTheRunkey);

                if (khTheRun != null && khTheRun.equals(ResultCode.RESULT_SUCCEED)) {
                    result.setResultMsg("???????????????");
                    runTestDomian.setStatus("1"); // 1????????? 2???????????? 3????????????
                } else {
                    result.setResultMsg("??????????????????");
                    runTestDomian.setStatus("2"); // 1????????? 2???????????? 3????????????
                    this.clearLockAndCountForRun(userId, userName, uid);
                }

            } else {
                result.setResultMsg("??????????????????????????????????????????");
                runTestDomian.setRunCount(0);
                runTestDomian.setStatus("6"); // ????????????????????????
            }

            runTestDomian.setCode(uid);
            result.setResultObj(runTestDomian);
        } catch (Exception e) {
            logger.error("----------??????ID???[" + userId + "]???????????????????????????????????????",e);
            redisClient.set(exceptionkey, ResultCode.RESULT_FAILED, expire);
        }

        return result;
    }
    
    //??????????????????
    ExecutorService executorService = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Override
    public BackResult<String> getTxtZipByIds(String ids, String userId) {
        logger.info("??????ID??????" + userId + "????????????????????????");
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
                logger.error("??????ID???[" + userId + "]?????????????????????????????????????????????????????????");
                result.setResultCode(ResultCode.RESULT_FAILED);
                result.setResultMsg("??????ID???[" + userId + "]???????????????????????????????????????????????????");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("??????ID???[" + userId + "]???????????????????????????????????????" + e.getMessage());
            result.setResultCode(ResultCode.RESULT_FAILED);
            result.setResultMsg("??????ID???[" + userId + "]??????????????????????????????");
        }

        return result;
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
            // ????????????
            ChuangLanSmsUtil.getInstance().sendSmsByMobileForTest(mobile,agentInfo);
        } else {
            // ????????????
            ChuangLanSmsUtil.getInstance().sendSmsByMobileForTestEx(mobile,agentInfo);            
        }

    }
}
