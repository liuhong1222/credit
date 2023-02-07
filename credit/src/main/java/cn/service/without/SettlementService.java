package cn.service.without;

import cn.redis.RedisClient;

public interface SettlementService {
	/**
	 * 调用 user-provider-service /userAccount/consumeAccount 结算上传文件检测的条数
	 * @param userId
	 * @param count
	 * @return
	 */
	Boolean webConsumeAccount(String userId,String count,RedisClient redisClient,String uid);
	
	/**
	 * 冻结或者解冻上传文件检测账户余额
	 * 
	 * @param wAccountKey
	 * @param expire
	 * @param count
	 * @param fag
	 *            true冻结 false 解冻
	 */
	Boolean freeWaccount(RedisClient redisClient,int expire, Boolean fag, String uid, String userId,int mobileCount);
}
