package cn.service.http.Response;

/**
 * @since 2018/5/4
 */
public class EmptyNumListResp {

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
	private EmptyNumListRespData result;

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

	public EmptyNumListRespData getResult() {
		return result;
	}

	public void setResult(EmptyNumListRespData result) {
		this.result = result;
	}

}
