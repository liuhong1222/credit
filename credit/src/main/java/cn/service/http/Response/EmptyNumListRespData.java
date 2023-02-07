package cn.service.http.Response;

import java.util.List;

/**
 * @since 2018/5/7
 */
public class EmptyNumListRespData {
    private List<EmptyNumListRespDataMobile> data;

    public List<EmptyNumListRespDataMobile> getData() {
        return data;
    }

    public void setData(List<EmptyNumListRespDataMobile> data) {
        this.data = data;
    }
}
