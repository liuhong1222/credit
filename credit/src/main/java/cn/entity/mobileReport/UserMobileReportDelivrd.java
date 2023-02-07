package cn.entity.mobileReport;

import java.io.Serializable;
import java.util.Date;

/**
 * 默认手机状态列表
 *
 * @since 2018/5/8
 */
public class UserMobileReportDelivrd implements Serializable {

    private static final long serialVersionUID = 9052730450897191860L;

    private String            id;

    /**
     * 用户Id
     */
    private String            userId;

    /**
     * 状态码
     */
    private String            delivrd;

    /**
     * 类型编码 实号:real 空号:empty 沉默号:silence 停机:outService
     */
    private String            typeCode;

    /**
     * 组别编码 实号1组:real_one 实号2组:real_two
     */
    private String            groupCode;

    /**
     * 组别id
     */
    private String            groupId;

    /**
     * 前置百分比
     */
    private Integer           frontPercent;

    /**
     * 是否删除 0未删除 1已删除
     */
    private Integer           isDeleted;

    /**
     * 创建时间
     */
    private Date              createTime;

    /**
     * 更新时间
     */
    private Date              updateTime;


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getUserId() {
        return userId;
    }


    public void setUserId(String userId) {
        this.userId = userId;
    }


    public String getDelivrd() {
        return delivrd;
    }


    public void setDelivrd(String delivrd) {
        this.delivrd = delivrd;
    }


    public String getTypeCode() {
        return typeCode;
    }


    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }


    public String getGroupCode() {
        return groupCode;
    }


    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }


    public String getGroupId() {
        return groupId;
    }


    public void setGroupId(String groupId) {
        this.groupId = groupId;
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


    public Date getCreateTime() {
        return createTime;
    }


    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }


    public Date getUpdateTime() {
        return updateTime;
    }


    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
