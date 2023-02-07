package cn.entity;

import com.alibaba.fastjson.JSONObject;

public class PhoneNumberInfo implements Comparable<PhoneNumberInfo> {

	private String createTime;

	private String mobile;

	private String report;

	private String status;

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getReport() {
		return report;
	}

	public void setReport(String report) {
		this.report = report;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public void setAttribute(String attributeName, String attributeValue) {
		switch (attributeName) {
		case "CREATE_TIME":
			this.setCreateTime(attributeValue);
			break;
		case "REPORT_TIME":
				this.setCreateTime(attributeValue);
				break;
		case "REPORT":
			this.setReport(attributeValue);
			break;
		default:
			break;
		}
	}
	
	@Override
	public String toString() {
		JSONObject jo = new JSONObject();
		
		jo.put("ct", getCreateTime());
		jo.put("rp", getReport());
		
		return jo.toString();
	}

	@Override
	public int compareTo(PhoneNumberInfo o) {
		if (o == null || o.getCreateTime() == null) {
			return 1;
		}
		
		if (getCreateTime() == null) {
			return -1;
		}
		
		int compare = o.getCreateTime().compareTo(getCreateTime());
		
		return compare;
	}
	
}
