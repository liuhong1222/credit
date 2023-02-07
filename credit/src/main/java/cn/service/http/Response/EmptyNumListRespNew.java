package cn.service.http.Response;

/**
 * @since 2018/11/26
 */
public class EmptyNumListRespNew {

	/**
	 * 返回码  0:成功  1:token校验失败  2:输入参数错误 500:服务器获取数据异常
	 */
	private String error;
	
	/**
	 * 返回消息
	 */
	private String desc;

	/**
	 * 日志id
	 */
	private String logID;

	
	/**
	 * data数据
	 */
	private EmptyNumListRespDataNew result;

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

	public String getLogID() {
		return logID;
	}

	public void setLogID(String logID) {
		this.logID = logID;
	}

	public EmptyNumListRespDataNew getResult() {
		return result;
	}

	public void setResult(EmptyNumListRespDataNew result) {
		this.result = result;
	}

}
