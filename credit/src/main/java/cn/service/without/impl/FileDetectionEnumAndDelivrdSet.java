package cn.service.without.impl;

import cn.enums.MobileReportGroupEnum;

import java.util.Set;

/**
 * 文件检测返回错误状态，解析结果类
 * @since 2018/7/3
 */

public class FileDetectionEnumAndDelivrdSet {
    /**
     * 文件检测结果组别枚举
     */
    private MobileReportGroupEnum.FileDetection fd;
    /**
     * 检测状态码set
     */
    private Set<String> newDelivrdSet;

    /**
     * 检测结果 0：有正常结果 1：状态未知 2：没有结果
     */
    private int detectedStatus;


    public MobileReportGroupEnum.FileDetection getFd() {
        return fd;
    }


    public void setFd(MobileReportGroupEnum.FileDetection fd) {
        this.fd = fd;
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
