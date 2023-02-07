package cn.entity.mobileReport;

import java.io.Serializable;

/**
 * @since 2018/5/12
 */
public class UserMobileReportIntervalDays implements Serializable {

    private static final long serialVersionUID = 8818155543171704236L;
    /**
     * 用户Id
     */
    private String            userId;

    /**
     * 间隔多少天内的数据为有效
     */
    private Integer           intervalDays;


    public String getUserId() {
        return userId;
    }


    public void setUserId(String userId) {
        this.userId = userId;
    }


    public Integer getIntervalDays() {
        return intervalDays;
    }


    public void setIntervalDays(Integer intervalDays) {
        this.intervalDays = intervalDays;
    }
}
