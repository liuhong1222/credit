package cn.enums;

import org.apache.commons.lang.StringUtils;

/**
 * @since 2018/8/4
 */
public enum DetectionChannelEnum {
    /**
     * 大数据通道
     */
    BIG_DATA_DETECTION_CHANNEL("0"),

    /**
     * mongodb通道
     */
    MONGODB_DETECTION_CHANNEL("1"),
    ;

    public String getChannelValue() {
        return channelValue;
    }

    private String channelValue;


    DetectionChannelEnum(String channelValue) {
        this.channelValue = channelValue;
    }

    public static DetectionChannelEnum getEnumByValue(String channelValue) {
        if (StringUtils.isEmpty(channelValue)) {
            return null;
        }
        for (DetectionChannelEnum value : DetectionChannelEnum.values()) {
            if (value.getChannelValue().equals(channelValue)) {
                return value;
            }
        }
        return null;
    }

}
