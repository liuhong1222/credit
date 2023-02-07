package cn.service.without.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;

import cn.redis.RedisClient;
import cn.service.without.SettlementService;
import cn.utils.CommonUtils;
import main.java.cn.common.RedisKeys;
import main.java.cn.common.ResultCode;

@Service
public class SettlementServiceImpl implements SettlementService {

	@Autowired
	private RestTemplate restTemplate;

	@Value("${withUserProviderService}")
	private String withUserProviderService;

	@Value("${consumeAccountUrl}")
	private String consumeAccountUrl;

	private final static Logger logger = LoggerFactory.getLogger(SettlementServiceImpl.class);

	@Override
	public Boolean webConsumeAccount(String userId, String count,RedisClient redisClient,String uid) {
		try {

			int expire = 60 * 60 * 1000 * 3; // 超时时间 （3小时）
			
			String url = withUserProviderService + consumeAccountUrl + "?creUserId=" + userId + "&count=" + count;

			logger.info("用户id：" + userId + "发起消费空号检测条数操作，本次消费：" + count + "条！请求地址：" + url);

			JSONObject resultConsume = restTemplate.getForObject(url, JSONObject.class);
			
			if (null != resultConsume && resultConsume.getString("resultCode").equals(ResultCode.RESULT_SUCCEED) && resultConsume.getBoolean("resultObj")) {
				logger.info("用户id：" + userId + "发起消费空号检测条数操作，本次消费：" + count + "条！消费成功！");
				
				// 解冻账户余额
  				this.freeWaccount(redisClient, expire, Boolean.FALSE, uid, userId,Integer.valueOf(count));
				
				// 清空redis中的数量
  				redisClient.remove(RedisKeys.getInstance().getkhSucceedClearingCountkey(userId,uid));
  				
				return Boolean.TRUE;
			}
			
			logger.info("用户id：" + userId + "发起消费空号检测条数操作，本次消费：" + count + "条！请求记账失败！" + "请求结果：" + resultConsume.toString());
			return Boolean.FALSE;
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("用户id：" + userId + "发起消费空号检测条数操作，出现系统异常：" + e.getMessage());
		}
		
		return Boolean.FALSE;
	}

	@Override
	public Boolean freeWaccount(RedisClient redisClient,int expire, Boolean fag, String uid, String userId,int mobileCount) {		
		String wAccountKey = RedisKeys.getInstance().getAcountKey(userId); // 冻结的上传文件空号账户条数
		// 冻结余额
		String freezeAccount = redisClient.get(wAccountKey);
		if (fag) {
			if (CommonUtils.isNotString(freezeAccount)) {
				// 新增
				redisClient.set(wAccountKey, String.valueOf(mobileCount), expire);
				logger.info("用户id:[" + userId + "]文件code:[" + uid + "]本次冻结账户：" + mobileCount + "条！");
			} else {
				Integer account = Integer.valueOf(freezeAccount);
				redisClient.incrBy(wAccountKey, mobileCount);
				logger.info("用户id:[" + userId + "]文件code:[" + uid + "]本次冻结账户：" + (mobileCount + account) + "条！");
			}
		} else {
			if (!CommonUtils.isNotString(freezeAccount)) {
				// 解冻
				Integer account = Integer.valueOf(freezeAccount);
				redisClient.decrBy(wAccountKey, mobileCount);
				logger.info("用户id:[" + userId + "]文件code:[" + uid + "]本次解冻账户：" + mobileCount + "条！剩余待解冻：" + (account - mobileCount));
			}
		}

		return Boolean.TRUE;
	}

}
