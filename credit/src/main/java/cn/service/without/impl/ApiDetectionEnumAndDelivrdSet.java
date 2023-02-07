package cn.service.without.impl;

import cn.enums.MobileReportGroupEnum;

import java.util.Date;
import java.util.Set;

/**
 * api检测返回错误状态，解析结果类
 * @since 2018/7/3
 */
public class ApiDetectionEnumAndDelivrdSet {
    /**
     * 文件检测结果组别枚举
     */
    private MobileReportGroupEnum.ApiDetection ad;
    /**
     * api检测结果组别枚举
     */
    private Date date;
    /**
     * 检测状态码set
     */
    private Set<String> newDelivrdSet;

    /**
     * 检测结果 0：有正常结果 1：状态未知 2：没有结果
     */
    private int detectedStatus;


    public MobileReportGroupEnum.ApiDetection getAd() {
        return ad;
    }

    public void setAd(MobileReportGroupEnum.ApiDetection ad) {
        this.ad = ad;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Set<String> getNewDelivrdSet() {
        return newDelivrdSet;
    }


    public void setNewDelivrdSet(Set<String> newDelivrdSet) {
        this.newDelivrdSet = newDelivrdSet;
    }


    public int getDetectedStatus() {
        return detectedStatus;
    }


    public void setDetectedStatus(int detectedStatus) {
        this.detectedStatus = detectedStatus;
    }
}
