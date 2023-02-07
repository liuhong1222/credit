package cn.service.http.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 2018/11/26
 */
public class EmptyNumListRespDataNew {
    private List<EmptyNumListRespDataMobileNew> data = new ArrayList<EmptyNumListRespDataMobileNew>();

    public List<EmptyNumListRespDataMobileNew> getData() {
        return data;
    }

    public void setData(List<EmptyNumListRespDataMobileNew> data) {
        this.data = data;
    }
}
