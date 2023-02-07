package cn.enums;

public enum StatusCode {
	Success("0", "success"), 
	InvalidToken("1", "invalid token"), 
	ParameterError("2", "parameter error"), 
	ServerError("500", "server error");

	private String error;

	private String desc;

	private StatusCode(String error, String desc) {
		this.error = error;
		this.desc = desc;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

}
