package cn.service.http.Response;

import java.util.List;

/**
 * @since 2018/5/7
 */
public class EmptyNumListRespDataMobile {

    /**
     * real number, 实号，返回最新的三个
     */
    private List<EmptyNumListRespRUE> r;

    /**
     *unknown number，未知号码，返回最新的三个
     */
    private List<EmptyNumListRespRUE> u;

    /**
     * error number，错误号码，返回最新的三个
     */
    //private List<EmptyNumListRespRUE> e;

    /**
     * 手机号
     */
    private String m;

    public List<EmptyNumListRespRUE> getR() {
        return r;
    }

    public void setR(List<EmptyNumListRespRUE> r) {
        this.r = r;
    }

    public List<EmptyNumListRespRUE> getU() {
        return u;
    }

    public void setU(List<EmptyNumListRespRUE> u) {
        this.u = u;
    }

//    public List<EmptyNumListRespRUE> getE() {
//        return e;
//    }
//
//    public void setE(List<EmptyNumListRespRUE> e) {
//        this.e = e;
//    }

    public String getM() {
        return m;
    }

    public void setM(String m) {
        this.m = m;
    }
}
