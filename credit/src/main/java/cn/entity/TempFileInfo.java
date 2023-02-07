package cn.entity;

import java.io.Serializable;

/**
 * 临时文件
 */
public class TempFileInfo implements Serializable {

	private static final long serialVersionUID = -6377031129028804314L;
	
	private String tableName;//表名

	private Integer fileRows; // 需要检测的总条数参考

	private String fileUploadUrl; // 上传文件地址

	public Integer getFileRows() {
		return fileRows;
	}

	public void setFileRows(Integer fileRows) {
		this.fileRows = fileRows;
	}

	public String getFileUploadUrl() {
		return fileUploadUrl;
	}

	public void setFileUploadUrl(String fileUploadUrl) {
		this.fileUploadUrl = fileUploadUrl;
	}
	
	public TempFileInfo(String tableName, Integer fileRows,String fileUploadUrl){
		this.setTableName(tableName);
		this.setFileRows(fileRows);
		this.setFileUploadUrl(fileUploadUrl);
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
}
