package cn.entity.mobileReport;

import java.io.Serializable;

/**
 * 默认手机状态类别表
 *
 * @since 2018/5/9
 */
public class MobileReportGroup implements Serializable {

    private static final long serialVersionUID = 4390080843995043734L;

    private String            id;

    /**
     * 组别编码 实号1组:real_one 实号2组:real_two
     */
    private String            groupCode;

    /**
     * 类型编码 实号:real 空号:empty 沉默号:silence 停机:out_service  关机:power_off
     */
    private String            typeCode;

    /**
     * 前置百分比
     */
    private Integer           frontPercent;

    /**
     * 是否删除 0未删除 1已删除
     */
    private Integer           isDeleted;


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getGroupCode() {
        return groupCode;
    }


    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }


    public String getTypeCode() {
        return typeCode;
    }


    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }


    public Integer getFrontPercent() {
        return frontPercent;
    }


    public void setFrontPercent(Integer frontPercent) {
        this.frontPercent = frontPercent;
    }


    public Integer getIsDeleted() {
        return isDeleted;
    }


    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}
