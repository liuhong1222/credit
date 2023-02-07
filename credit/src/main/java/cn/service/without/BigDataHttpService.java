package cn.service.without;

import cn.service.http.Result.EmptyNumFileDetectionResult;

import java.util.List;

/**
 * @since 2018/5/4
 */
public interface BigDataHttpService {
    
    EmptyNumFileDetectionResult emptyNumFileDetectionNew(List<String> mobileList,String userId)throws Exception;
}
