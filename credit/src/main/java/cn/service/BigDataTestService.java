package cn.service;

import cn.entity.FileUpload;
import main.java.cn.common.BackResult;
import main.java.cn.domain.RunTestDomian;

/**
 * 断点续检
 * @author ChuangLan
 *
 */
public interface BigDataTestService{
	
	BackResult<RunTestDomian> theTestNew(FileUpload fileUpload, String userId, String mobile,
            String source,String startLine);
}
