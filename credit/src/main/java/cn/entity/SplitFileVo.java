package cn.entity;

import java.io.File;
import java.util.List;


/**
 * 文件分割vo
 * @author liuh
 * @date 2021年12月4日
 */
public class SplitFileVo {

	private File[] fileList;
	
	private Integer errorCounts = 0;
	
	private List<String> mobileList ;
	
	public SplitFileVo() {}
	
	public SplitFileVo(File[] fileList,Integer errorCounts,List<String> mobileList) {
		this.fileList = fileList;
		this.errorCounts = errorCounts;
		this.mobileList = mobileList;
	}

	public File[] getFileList() {
		return fileList;
	}

	public void setFileList(File[] fileList) {
		this.fileList = fileList;
	}

	public Integer getErrorCounts() {
		return errorCounts;
	}

	public void setErrorCounts(Integer errorCounts) {
		this.errorCounts = errorCounts;
	}

	public List<String> getMobileList() {
		return mobileList;
	}

	public void setMobileList(List<String> mobileList) {
		this.mobileList = mobileList;
	}
	
	
}
