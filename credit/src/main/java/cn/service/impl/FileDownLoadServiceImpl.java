package cn.service.impl;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.entity.CvsFilePath;
import cn.service.CvsFilePathService;
import cn.service.FileDownLoadService;
import main.java.cn.common.BackResult;
import main.java.cn.common.ResultCode;
import main.java.cn.domain.CvsFilePathDomain;

@Service
public class FileDownLoadServiceImpl implements FileDownLoadService {

	private final static Logger logger = LoggerFactory.getLogger(FileDownLoadServiceImpl.class);

	@Autowired
	private CvsFilePathService cvsFilePathService;

	@Override
	public BackResult<CvsFilePathDomain> getCvsFilePathByFileCode(String userId, String id,String fileCode) {
		BackResult<CvsFilePathDomain> result = new BackResult<CvsFilePathDomain>();
		CvsFilePathDomain cfpd = new CvsFilePathDomain();
		//根据记录id查询文件检测结果
		if(StringUtils.isNotBlank(id)){
			CvsFilePath cfp = cvsFilePathService.findById(id);
			if(cfp == null){
				result.setResultCode(ResultCode.RESULT_FAILED);
				result.setResultMsg("下载失败，需要下载的文件为空");
				return result;
			}
			
			BeanUtils.copyProperties(cfp, cfpd);
			result.setResultObj(cfpd);
			return result;
		}
		//根据文件code查询文件检测结果
		if(StringUtils.isNotBlank(fileCode)){
			CvsFilePath cfp = cvsFilePathService.findByIdAndFileCode(userId, fileCode);
			if(cfp == null){
				result.setResultCode(ResultCode.RESULT_FAILED);
				result.setResultMsg("下载失败，需要下载的文件为空");
				return result;
			}
			
			BeanUtils.copyProperties(cfp, cfpd);
			result.setResultObj(cfpd);
			return result;
		}
		
		result.setResultCode(ResultCode.RESULT_PARAM_EXCEPTIONS);
		result.setResultMsg("检测记录id和文件code不能全为空 ");
		return result;
	}

	
}
