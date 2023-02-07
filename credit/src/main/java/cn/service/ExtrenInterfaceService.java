package cn.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import cn.enums.MobileReportGroupEnum;
import cn.redis.RedisClient;
import cn.service.http.BaseHttpConfig;
import cn.service.http.BaseOkHttpService;
import cn.utils.DateUtils;
import main.java.cn.common.BackResultWanShu;
import main.java.cn.domain.MobileInfoDomain;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class ExtrenInterfaceService extends BaseOkHttpService implements InitializingBean{
	
	@Value("${externUrl}")
    private String externUrl;
	
	@Value("${externUrl_zxm}")
    private String externUrl_zxm;
	
	@Value("${invoke.zxm.flag}")
    private boolean invokeZxmFlag;

	private Map<String, Long> mobileCountMap = new HashMap<String, Long>();
	
	@Autowired
	private AgentCreUserService agentCreUserService;
	
	@Autowired
	private RedisClient redisClient;
	
	@Autowired
    private BaseHttpConfig baseConfig;
	
	private final static Logger logger = LoggerFactory.getLogger(ExtrenInterfaceService.class);
	
	public ListMultimap<MobileReportGroupEnum, String> getMobileResult(List<String> mobileList,String userId) {
		if(invokeZxmFlag) {
			return getMobileResultByZxm(mobileList, userId);
		}
		
		ListMultimap<MobileReportGroupEnum, String> resultListMultimap = ArrayListMultimap.create();
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("appID", "clSource_ATD800");
		paramMap.put("mobiles", StringUtils.join(mobileList, ","));
		paramMap.put("userId", "dais002");
		String response = null;
		try {
			response = post( paramMap);
			if (StringUtils.isBlank(response)) {
				return handleError(mobileList);
			}
		} catch (Exception e) {
			logger.error("{}，在线检测异常，info:",userId,e);
			return handleError(mobileList);
		}
		
		
		JSONObject jsonObject = JSONObject.parseObject(response);
		if (!"000000".equals(jsonObject.getString("resultCode"))) {
			return handleError(mobileList);
		}
		
		if (jsonObject.getString("resultObj") == null || JSONArray.parseArray(jsonObject.getString("resultObj")).size() <= 0) {
			return handleError(mobileList);
		}
		
		Map<String, String> mobileResultMap = transJsonArrayToMap(JSONArray.parseArray(jsonObject.getString("resultObj")));
		Integer agentIdInteger = agentCreUserService.getAgentIdByUserId(userId);
		for(String mobile:mobileList) {
//			if (agentIdInteger > 1 && getMobileCount(userId) % getNumberInteger()==0) {
//				resultListMultimap.put(MobileReportGroupEnum.FileDetection.SILENCE, mobile);
//				continue;
//			}
			
			if (mobileResultMap.containsKey(mobile)) {
				resultListMultimap.put(getFileDetection(mobileResultMap.get(mobile)), mobile);
				continue;
			}
			
			resultListMultimap.put(MobileReportGroupEnum.FileDetection.NO_RESULT, mobile);
		}
		
		return resultListMultimap;
	}
	
	public ListMultimap<MobileReportGroupEnum, String> getMobileResultByZxm(List<String> mobileList,String userId) {
		ListMultimap<MobileReportGroupEnum, String> resultListMultimap = ArrayListMultimap.create();
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("appId", "0kifegd1");
		paramMap.put("mobiles", StringUtils.join(mobileList, ","));
		paramMap.put("appKey", "edh31xld");
		String response = null;
		try {
			response = postZxm( paramMap);
			if (StringUtils.isBlank(response)) {
				return handleError(mobileList);
			}
		} catch (Exception e) {
			logger.error("{}，在线检测异常，info:",userId,e);
			return handleError(mobileList);
		}
		
		
		JSONObject jsonObject = JSONObject.parseObject(response);
		if (!"200".equals(jsonObject.getString("code"))) {
			return handleError(mobileList);
		}
		
		if (jsonObject.getString("data") == null || JSONArray.parseArray(jsonObject.getString("data")).size() <= 0) {
			return handleError(mobileList);
		}
		
		Map<String, String> mobileResultMap = transJsonArrayToMap(JSONArray.parseArray(jsonObject.getString("data")));
		Integer agentIdInteger = agentCreUserService.getAgentIdByUserId(userId);
		for(String mobile:mobileList) {
//			if (agentIdInteger > 1 && getMobileCount(userId) % getNumberInteger()==0) {
//				resultListMultimap.put(MobileReportGroupEnum.FileDetection.SILENCE, mobile);
//				continue;
//			}
			
			if (mobileResultMap.containsKey(mobile)) {
				resultListMultimap.put(getFileDetection(mobileResultMap.get(mobile)), mobile);
				continue;
			}
			
			resultListMultimap.put(MobileReportGroupEnum.FileDetection.NO_RESULT, mobile);
		}
		
		return resultListMultimap;
	}
	
	private Long getMobileCount(String userId) {
		String dateString = DateUtils.getDate();
		Long countLong = 0L;
		if (mobileCountMap.containsKey(dateString+userId)) {
			countLong = mobileCountMap.get(dateString+userId)+1;
			mobileCountMap.put(dateString+userId,countLong);
		}else {
			countLong += 1;
			mobileCountMap.put(dateString+userId, countLong);
		}
		
		return countLong;
	}
	
	public BackResultWanShu<List<MobileInfoDomain>> getMobileResultByApi(List<String> mobileList,String userId) {
		if(invokeZxmFlag) {
			return getMobileResultByApiZxm(mobileList, userId);
		}
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("appID", "clSource_ATD800");
		paramMap.put("mobiles", StringUtils.join(mobileList, ","));
		paramMap.put("userId", "dais002");
		
		try {
			String response = post(paramMap);
			if (StringUtils.isBlank(response)) {
				logger.error("{}， 下游通道调用失败，返回结果为空",userId);
				return null;
			}		
			
//			paramMap = null;
//			response = null;
			
			return JSONObject.parseObject(response,BackResultWanShu.class);
		} catch (Exception e) {
			logger.error("{}， 下游通道调用异常，info:",userId,e);
			return null;
		}
		
	}
	
	private BackResultWanShu<List<MobileInfoDomain>> getMobileResultByApiZxm(List<String> mobileList,String userId){
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("appId", "0kifegd1");
		paramMap.put("mobiles", StringUtils.join(mobileList, ","));
		paramMap.put("appKey", "edh31xld");
		
		try {
			String response = postZxm(paramMap);
			if (StringUtils.isBlank(response)) {
				logger.error("{}， 下游通道调用失败，返回结果为空",userId);
				return null;
			}		
			
			JSONObject jsonObject = JSONObject.parseObject(response);
			return new BackResultWanShu(jsonObject.getString("code"),jsonObject.getString("msg"),
					StringUtils.isBlank(jsonObject.getString("data"))?null:JSONArray.parseArray(jsonObject.getString("data"), MobileInfoDomain.class));
		} catch (Exception e) {
			logger.error("{}， 下游通道调用异常，info:",userId,e);
			return null;
		}
	}
	
    private String post(Map<String, String> paramMap)throws Exception {
    	long start=System.currentTimeMillis();
    	FormBody.Builder formBodyBuilder = new FormBody.Builder();
        Set<String> keySet = paramMap.keySet();
        for(String key:keySet) {
            String value = paramMap.get(key);
            formBodyBuilder.add(key,value);
        }
        
        FormBody formBody = formBodyBuilder.build();
        Request.Builder rBuilder = new Request.Builder();
        Request requestPost = rBuilder.url(externUrl).post(formBody).build();
        Response res = null;
        try{
        	res = client.newCall(requestPost).execute();
            if (res.isSuccessful()) {
            	return res.body().string();
            } else {
            	String bodyString = res.body().string();
				logger.error("doput for url: {} is failed, code:{}, body:{}", externUrl, res.code(), bodyString);
				return bodyString;
            }
        } catch (Exception e) {
           logger.error("doput for url: {} is exception,info:",externUrl,e);
           return null;
        }finally {
        	if(res!=null){
				res.close();
			}
        	logger.info( "调用上游接口耗时:{}",(System.currentTimeMillis()-start));
		}        
    }
    
    private String postZxm(Map<String, String> paramMap)throws Exception {
    	long start=System.currentTimeMillis();
    	FormBody.Builder formBodyBuilder = new FormBody.Builder();
        Set<String> keySet = paramMap.keySet();
        for(String key:keySet) {
            String value = paramMap.get(key);
            formBodyBuilder.add(key,value);
        }
        
        FormBody formBody = formBodyBuilder.build();
        Request.Builder rBuilder = new Request.Builder();
        Request requestPost = rBuilder.url(externUrl_zxm).post(formBody).build();
                
        Response res = null;
        try{
        	res = client.newCall(requestPost).execute();
            if (res.isSuccessful()) {
            	return res.body().string();
            } else {
            	String bodyString = res.body().string();
				logger.error("doput for url: {} is failed, code:{}, body:{}", externUrl, res.code(), bodyString);
				return bodyString;
            }
        } catch (Exception e) {
           logger.error("doput for url: {} is exception,info:",externUrl,e);
           return null;
        }finally {
        	if(res!=null){
				res.close();
			}
        	logger.info( "调用上游接口耗时:{}",(System.currentTimeMillis()-start));
		}        
    }
	
	private ListMultimap<MobileReportGroupEnum, String> handleError(List<String> mobileList){
		ListMultimap<MobileReportGroupEnum, String> resultListMultimap = ArrayListMultimap.create();
		for(String mobile:mobileList) {
			resultListMultimap.put(MobileReportGroupEnum.FileDetection.NO_RESULT, mobile);
		}
		
		return resultListMultimap;
	}
	
	private MobileReportGroupEnum.FileDetection getFileDetection(String status){
		if ("0".equals(status)) {
			return MobileReportGroupEnum.FileDetection.EMPTY_ONE;
		}else if("1".equals(status)){
			return MobileReportGroupEnum.FileDetection.REAL_ONE;
		}else if("2".equals(status)){
			return MobileReportGroupEnum.FileDetection.OUT_SERVICE;
		}else if("3".equals(status)){
			return MobileReportGroupEnum.FileDetection.NO_RESULT;
		}else if("5".equals(status)){
			return MobileReportGroupEnum.FileDetection.SHUT;
		}else {
			return MobileReportGroupEnum.FileDetection.SILENCE;
		}
		
	}
	
	private Map<String, String> transJsonArrayToMap(JSONArray jsonArray){
		Map<String, String> resultMap = new HashMap<>();
		jsonArray.forEach(json -> {
			JSONObject jsonObject = (JSONObject)json;
			resultMap.put(jsonObject.getString("mobile"), jsonObject.getString("status"));
		});
		
		return resultMap;
	}
	
	private Integer getNumberInteger() {
		Integer temp = 3333;
		String redisString = redisClient.get("ds:gc:num");
		if (StringUtils.isNotBlank(redisString)) {
			return Integer.valueOf(redisString);
		}
		
		return temp;
	}

	@Override
	public BaseHttpConfig getBaseConfig() {
		return this.baseConfig;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.initClient();
	}
}
