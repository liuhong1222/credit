package cn.service.without.impl;

import cn.service.ExtrenInterfaceService;
import cn.service.http.BaseHttpConfig;
import cn.service.http.BaseOkHttpService;
import cn.service.http.Result.EmptyNumFileDetectionResult;
import cn.service.without.BigDataHttpService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * @since 2018/5/4
 */
@Service
public class BigDataHttpServiceImpl extends BaseOkHttpService
        implements InitializingBean, BigDataHttpService {

    @Autowired
    private BaseHttpConfig baseConfig;
    
    @Autowired
    private ExtrenInterfaceService extrenInterfaceService;

    /**
     * /**空号文件检测-新版本
     *
     * @param mobileList
     * @param baseDelivrdMap
     * @param userDelivrdMap
     * @return
     * @throws Exception
     */
    @Override
    public EmptyNumFileDetectionResult emptyNumFileDetectionNew(List<String> mobileList,String userId)
            throws Exception {

        EmptyNumFileDetectionResult result = new EmptyNumFileDetectionResult();        
        result.setData(extrenInterfaceService.getMobileResult(mobileList, userId));
        result.setNewDelivrdSet(null);
        return result;
    }
    
    @Override
    public BaseHttpConfig getBaseConfig() {
        return this.baseConfig;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        super.initClient();
    }
}
