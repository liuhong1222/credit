package cn.service.impl;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import cn.dao.FileUploadMapper;
import cn.entity.FileUpload;
import cn.redis.RedisClient;
import cn.service.FileUploadService;
import cn.utils.UUIDTool;
import main.java.cn.common.BackResult;
import main.java.cn.common.RedisKeys;
import main.java.cn.common.ResultCode;
import main.java.cn.domain.FileUploadDomain;

@Service
public class FileUploadServiceImpl implements FileUploadService {

	private final static Logger logger = LoggerFactory.getLogger(FileUploadServiceImpl.class);

	@Autowired
	private FileUploadMapper fileUploadMapper;

	@Autowired
    private RedisClient redisClient;
	
	@Override
	public FileUpload findByOne(String id) {
		return fileUploadMapper.findById(id);
	}

	@Override
	public FileUpload save(FileUpload fileUpload) {
		fileUpload.setId(UUIDTool.getInstance().getUUID());
		fileUpload.setCreateTime(new Date());
		fileUpload.setIsDeleted(0);
		int count = fileUploadMapper.saveFileUpload(fileUpload);
		logger.info("insert FileUpload data,userId:{},fileName:{},insert count:{}",fileUpload.getUserId(),fileUpload.getFileName(),count);
		return fileUpload;
	}

	@Override
	public BackResult<FileUploadDomain> findById(String arg0) {

		BackResult<FileUploadDomain> result = new BackResult<FileUploadDomain>();

		try {
			FileUploadDomain domain = new FileUploadDomain();

			FileUpload fileUpload = this.findByOne(arg0);
			if (null == fileUpload) {
	        	//获取保存在redis里的文件上传记录-万数
	        	String fileUploadStr = redisClient.get(RedisKeys.getInstance().getWanShuFileUploadkey(arg0));
	        	if(StringUtils.isBlank(fileUploadStr)){
	        		return new BackResult<FileUploadDomain>(ResultCode.RESULT_DATA_EXCEPTIONS,
	                        "文件检测异常，没有检测到可以检测的文件！");
	        	}
	        	//字符串转对象
	        	fileUpload = JSONObject.parseObject(fileUploadStr,FileUpload.class);            
	        }
			BeanUtils.copyProperties(fileUpload, domain);

			result.setResultObj(domain);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("系统异常：" + e.getMessage());
			result = new BackResult<FileUploadDomain>(ResultCode.RESULT_FAILED, "系统异常");
		}

		return result;
	}

	@Override
	public BackResult<FileUploadDomain> save(FileUploadDomain arg0) {

		BackResult<FileUploadDomain> result = new BackResult<FileUploadDomain>();

		try {
			
			FileUploadDomain domain = new FileUploadDomain();
			
			FileUpload fileUpload = new FileUpload();
			
			BeanUtils.copyProperties(arg0, fileUpload);
			
			fileUpload = this.save(fileUpload);
			
			BeanUtils.copyProperties(fileUpload, domain);
			
			result.setResultObj(domain);
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("系统异常：" + e.getMessage());
			result = new BackResult<FileUploadDomain>(ResultCode.RESULT_FAILED, "系统异常");
		}

		return result;
	}
}
