package cn.service;

import main.java.cn.common.BackResult;
import main.java.cn.domain.CvsFilePathDomain;

public interface FileDownLoadService {

	BackResult<CvsFilePathDomain> getCvsFilePathByFileCode(String userId, String id,String fileCode);

}
