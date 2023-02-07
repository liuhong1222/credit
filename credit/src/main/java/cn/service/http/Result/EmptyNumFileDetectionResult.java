package cn.service.http.Result;

import cn.enums.MobileReportGroupEnum;
import com.google.common.collect.ListMultimap;

import java.util.Map;
import java.util.Set;

/**
 * @since 2018/5/7
 */
public class EmptyNumFileDetectionResult {

    /**
     * 手机号及状态码map
     */
    private ListMultimap<MobileReportGroupEnum,String> data;

    /**
     * 新状态码set
     */
    private Set<String> newDelivrdSet;


    /**
     * 状态组别号-手机号
     * @return
     */
    public ListMultimap<MobileReportGroupEnum,String> getData() {
        return data;
    }


    public void setData(ListMultimap<MobileReportGroupEnum,String> data) {
        this.data = data;
    }


    public Set<String> getNewDelivrdSet() {
        return newDelivrdSet;
    }


    public void setNewDelivrdSet(Set<String> newDelivrdSet) {
        this.newDelivrdSet = newDelivrdSet;
    }
}
