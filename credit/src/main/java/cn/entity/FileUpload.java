package cn.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件上传信息存储类
 */
public class FileUpload implements Serializable {

	private static final long serialVersionUID = -26881528425986560L;

	private String id;

	private String userId; // 用户id

	private String fileName; // 备用生成检测结果包的时候采用这个名字

	private Integer fileRows; // 需要检测的总条数参考

	private String fileUploadUrl; // 上传文件地址

	private Date createTime;

	private int isDeleted = 0;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the fileRows
	 */
	public Integer getFileRows() {
		return fileRows;
	}

	/**
	 * @param fileRows the fileRows to set
	 */
	public void setFileRows(Integer fileRows) {
		this.fileRows = fileRows;
	}

	/**
	 * @return the fileUploadUrl
	 */
	public String getFileUploadUrl() {
		return fileUploadUrl;
	}

	/**
	 * @param fileUploadUrl the fileUploadUrl to set
	 */
	public void setFileUploadUrl(String fileUploadUrl) {
		this.fileUploadUrl = fileUploadUrl;
	}

	/**
	 * @return the createTime
	 */
	public Date getCreateTime() {
		return createTime;
	}

	/**
	 * @param createTime the createTime to set
	 */
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	/**
	 * @return the isDeleted
	 */
	public int getIsDeleted() {
		return isDeleted;
	}

	/**
	 * @param isDeleted the isDeleted to set
	 */
	public void setIsDeleted(int isDeleted) {
		this.isDeleted = isDeleted;
	}

}
