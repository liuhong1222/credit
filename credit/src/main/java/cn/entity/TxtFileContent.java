package cn.entity;

import java.io.Serializable;
import java.util.List;


public class TxtFileContent implements Serializable{

	private static final long serialVersionUID = -624925120405918745L;

	private String fileCode;//文件编码格式
	
	private Integer errorCounts;
	
	private Integer mobileCounts;//有效号码列表
	
	private List<String> mobileList;

	public String getFileCode() {
		return fileCode;
	}

	public void setFileCode(String fileCode) {
		this.fileCode = fileCode;
	}

	public Integer getErrorCounts() {
		return errorCounts;
	}

	public void setErrorCounts(Integer errorCounts) {
		this.errorCounts = errorCounts;
	}

	public Integer getMobileCounts() {
		return mobileCounts;
	}

	public void setMobileCounts(Integer mobileCounts) {
		this.mobileCounts = mobileCounts;
	}

	public List<String> getMobileList() {
		return mobileList;
	}

	public void setMobileList(List<String> mobileList) {
		this.mobileList = mobileList;
	}
	
	
}
