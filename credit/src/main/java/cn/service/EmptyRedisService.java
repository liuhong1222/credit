package cn.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.redis.RedisClient;
import main.java.cn.common.RedisKeys;
import main.java.cn.common.ResultCode;

@Service
public class EmptyRedisService {
	
	@Autowired
	private RedisClient redisClient;

	public void initRedisKey(String userId,String fileUploadId,int expire,int fileRows,String identifier) {
		String KhTestCountKey = RedisKeys.getInstance().getKhTestCountKey(userId,fileUploadId); // 获取空号检测
        // 需要检测的总条数key
        // （根据文件获取的总条数）
        String succeedTestCountkey = RedisKeys.getInstance().getkhSucceedTestCountkey(userId,fileUploadId); // 获取空号检测
        // 已经成功检测的总条数（运行中，不考虑不计费的条数）
        String redisLockIdentifier = RedisKeys.getInstance().getkhRedisLockIdentifier(userId,fileUploadId); // 获取空号检测
        // 已经成功检测的总条数（运行结束需要记账的总条数）
        String generateResultskey = RedisKeys.getInstance().getkhGenerateResultskey(userId,fileUploadId); // 空号检测线程key
        // 多线程执行是
        // 全部执行完毕生成文件使用
        String exceptionkey = RedisKeys.getInstance().getkhExceptionkey(userId,fileUploadId); // 线程执行全局异常key
        // 程序是否运行结束
        String khTheRunkey = RedisKeys.getInstance().getkhTheRunkey(userId,fileUploadId); // 程序是否运行结束
        
        // 将标识存入redis
        redisClient.set(redisLockIdentifier, identifier, expire);

        // 初始化条数 需要进行检测的条数 检测一条 条数 + 1 累加
        redisClient.set(succeedTestCountkey, String.valueOf(0), expire);

        // 初始化条数 需要进行需要检测的总条数
        redisClient.set(KhTestCountKey, String.valueOf(fileRows), expire);

        // 初始化条数 多线程检测的线程数
        redisClient.set(generateResultskey, String.valueOf(0).toString(), expire);

        // 初始化 线程执行全局异常key
        redisClient.set(exceptionkey, ResultCode.RESULT_SUCCEED, expire);

        // 初始化 程序是否运行结束key
        redisClient.set(khTheRunkey, ResultCode.RESULT_SUCCEED, expire);
	}
}
