package cn.entity;

import java.io.Serializable;
import java.util.Date;

public class CvsFilePath implements Serializable{

	private static final long serialVersionUID = -8820457928658267590L;

	private int id;

	private String userId;
	
	private Date createDate;
	
	private String fileCode; 
	
	private String thereFilePath;

	private String thereFileSize;

	private int thereCount = 0;

	private String sixFilePath;

	private String sixFileSize;

	private int sixCount = 0;
	
	private String shutFilePath;
	
	private String shutFileSize;
	
	private int shutCount = 0;
	
	private String unknownFilePath; 
	
	private String unknownFileSize;

	private int unknownSize = 0;

	private String zipName;

	private String zipPath;

	private String zipSize;
	
	private int totalCount = 0;

	private int isDeleted = 0;
	
	private Date createTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getFileCode() {
		return fileCode;
	}

	public void setFileCode(String fileCode) {
		this.fileCode = fileCode;
	}

	public String getThereFilePath() {
		return thereFilePath;
	}

	public void setThereFilePath(String thereFilePath) {
		this.thereFilePath = thereFilePath;
	}

	public String getThereFileSize() {
		return thereFileSize;
	}

	public void setThereFileSize(String thereFileSize) {
		this.thereFileSize = thereFileSize;
	}

	public int getThereCount() {
		return thereCount;
	}

	public void setThereCount(int thereCount) {
		this.thereCount = thereCount;
	}

	public String getSixFilePath() {
		return sixFilePath;
	}

	public void setSixFilePath(String sixFilePath) {
		this.sixFilePath = sixFilePath;
	}

	public String getSixFileSize() {
		return sixFileSize;
	}

	public void setSixFileSize(String sixFileSize) {
		this.sixFileSize = sixFileSize;
	}

	public int getSixCount() {
		return sixCount;
	}

	public void setSixCount(int sixCount) {
		this.sixCount = sixCount;
	}

	public String getShutFilePath() {
		return shutFilePath;
	}

	public void setShutFilePath(String shutFilePath) {
		this.shutFilePath = shutFilePath;
	}

	public String getShutFileSize() {
		return shutFileSize;
	}

	public void setShutFileSize(String shutFileSize) {
		this.shutFileSize = shutFileSize;
	}

	public int getShutCount() {
		return shutCount;
	}

	public void setShutCount(int shutCount) {
		this.shutCount = shutCount;
	}

	public String getUnknownFilePath() {
		return unknownFilePath;
	}

	public void setUnknownFilePath(String unknownFilePath) {
		this.unknownFilePath = unknownFilePath;
	}

	public String getUnknownFileSize() {
		return unknownFileSize;
	}

	public void setUnknownFileSize(String unknownFileSize) {
		this.unknownFileSize = unknownFileSize;
	}

	public int getUnknownSize() {
		return unknownSize;
	}

	public void setUnknownSize(int unknownSize) {
		this.unknownSize = unknownSize;
	}

	public String getZipName() {
		return zipName;
	}

	public void setZipName(String zipName) {
		this.zipName = zipName;
	}

	public String getZipPath() {
		return zipPath;
	}

	public void setZipPath(String zipPath) {
		this.zipPath = zipPath;
	}

	public String getZipSize() {
		return zipSize;
	}

	public void setZipSize(String zipSize) {
		this.zipSize = zipSize;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public int getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(int isDeleted) {
		this.isDeleted = isDeleted;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}	
}
