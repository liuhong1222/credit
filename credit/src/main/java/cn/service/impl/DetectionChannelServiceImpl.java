package cn.service.impl;

import cn.enums.DetectionChannelEnum;
import cn.redis.RedisClient;
import cn.redis.RedisKeyList;
import cn.service.DetectionChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @since 2018/8/3
 */
@Service
public class DetectionChannelServiceImpl implements DetectionChannelService {

    private final static Logger logger = LoggerFactory.getLogger(DetectionChannelServiceImpl.class);

    @Autowired
    private RedisClient redisClient;

    @Override
    public DetectionChannelEnum getDetectionChannel() {
        String channelValue = null;
        try {
            channelValue = redisClient.get(RedisKeyList.CREDIT_SERVICE_DETECTION_CHANNEL_KEY);
        } catch (Exception e) {
            logger.error("redis获取检测通道失败", e);
        }
        DetectionChannelEnum channelEnum = DetectionChannelEnum.getEnumByValue(channelValue);
        if (channelEnum == null) {
            channelEnum = DetectionChannelEnum.BIG_DATA_DETECTION_CHANNEL;
        }
        return channelEnum;
    }
}
