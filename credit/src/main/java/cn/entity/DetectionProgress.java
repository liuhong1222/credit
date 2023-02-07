package cn.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 检测进度表
 * @since 2018/6/13
 */
public class DetectionProgress  implements Serializable {

    private static final long serialVersionUID = -2826033789791349585L;

    public static final Integer DETECTING_STATUS = 0;
    public static final Integer SUCCESS_STATUS = 1;
    public static final Integer FAILURE_STATUS = 2;
    public static final Integer TO_TERMINATE_STATUS = 3;
    public static final Integer TERMINATED_STATUS = 4;
    public static final Integer TO_PAUSE_STATUS = 5;
    public static final Integer PAUSE_STATUS = 6;

    private int id;

    /**
     * 用户ID
      */
    private Long userId;

    /**
     * 文件id
     */
    private String fileUploadId;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 总检测条数
     */
    private Long totalCount;

    /**
     * 已检测条数
     */
    private Long detectedCount;

    /**
     * 状态  0处理中 1处理成功 2处理失败 3请求终止 4已终止
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFileUploadId() {
        return fileUploadId;
    }

    public void setFileUploadId(String fileUploadId) {
        this.fileUploadId = fileUploadId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getDetectedCount() {
        return detectedCount;
    }

    public void setDetectedCount(Long detectedCount) {
        this.detectedCount = detectedCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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
