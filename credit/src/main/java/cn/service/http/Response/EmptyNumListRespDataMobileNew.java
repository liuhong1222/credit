package cn.service.http.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 2018/11/26
 */
public class EmptyNumListRespDataMobileNew {

    /**
     * 状态码列表, 返回最新的5个
     */
    private List<EmptyNumListRespRUENew> codeList = new ArrayList<EmptyNumListRespRUENew>();

    /**
     * 手机号
     */
    private String m;

	public List<EmptyNumListRespRUENew> getCodeList() {
		return codeList;
	}

	public void setCodeList(List<EmptyNumListRespRUENew> codeList) {
		this.codeList = codeList;
	}

	public String getM() {
		return m;
	}

	public void setM(String m) {
		this.m = m;
	}
}
