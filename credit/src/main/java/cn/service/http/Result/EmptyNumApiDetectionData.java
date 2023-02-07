package cn.service.http.Result;

import cn.enums.MobileReportGroupEnum;

import java.util.Date;

/**
 * 空号api检测单个结果数据
 * @since 2018/5/16
 */
public class EmptyNumApiDetectionData {

    private String mobile;

    private Date date;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
