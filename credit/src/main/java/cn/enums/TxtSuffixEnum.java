package cn.enums;

/**
 * 文件后缀名枚举
 * 
 */
/**
 * 文件后缀名枚举
 */
public enum TxtSuffixEnum {
    /**
     * 所有号码
     */
    ALL("all.txt"),
    /**
     * 实号一组
     */
    REAL_ONE("realOne.txt"),
    /**
     * 实号二组
     */
    REAL_TWO("realTwo.txt"),
    /**
     * 实号三组
     */
    REAL_THREE("realThree.txt"),
    /**
     * 空号一组
     */
    KONG_ONE("kongOne.txt"),
    /**
     * 空号二组
     */
    KONG_TWO("kongTwo.txt"),
    /**
     * 空号三组
     */
    KONG_THREE("kongThree.txt"),
    /**
     * 沉默号
     */
    SILENCE_ONE("silenceOne.txt"),
    /**
     * 停机号
     */
    OUT_SERVICE_ONE("outServiceOne.txt"),

    /**
     * 关机号
     */
    SHUTDOWN_ONE("shutdownOne.txt"),;
    private String txtSuffix;

    TxtSuffixEnum(String txtSuffix) {
        this.txtSuffix = txtSuffix;
    }

    public String getTxtSuffix() {
        return txtSuffix;
    }
}
