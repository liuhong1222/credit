package cn.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import com.alibaba.fastjson.JSONObject;

import cn.redis.RedisClient;
import cn.service.ContractService;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import main.java.cn.common.BackResult;
import main.java.cn.common.ResultCode;
import main.java.cn.until.RedisKeyUtil;

@Service
public class ContractServiceImpl implements ContractService {

	private final static Logger logger = LoggerFactory.getLogger(ContractServiceImpl.class);

	@Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
	
	@Value("${fielUrlNas}")
    private String fielUrlNas;
	
	@Autowired
    private RedisClient redisClient;
	
	@Override
	public BackResult<String> getPdfFileByHtml(String userId) {
		BackResult<String> result = new BackResult<String>();
		String param = redisClient.get(RedisKeyUtil.getContrasctDataKey(userId));
		String htmlFileName = fielUrlNas + userId + "_" + System.currentTimeMillis() + ".html";
		File file = new File(htmlFileName);
		File parent = file.getParentFile();
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}
	
		FileWriter fw = null;
	    try {
	    	  file.createNewFile();
	    	  fw = new FileWriter(htmlFileName);
	          freeMarkerConfigurer.setTemplateLoaderPath("classpath:templates");
	          Template template = freeMarkerConfigurer.getConfiguration().getTemplate("contract.flt");
	          freeMarkerConfigurer.setDefaultEncoding("utf-8");	          
	          template.process(JSONObject.parse(param), fw);
	          fw.flush();
	    } catch (IOException e) {
			e.printStackTrace();
			logger.error("用户【" + userId + "】生成交易合同异常，异常信息为：" + e.getMessage());
			result.setResultCode(ResultCode.RESULT_FAILED);
			result.setResultMsg("用户生成合同异常");
			return result;
		} catch (TemplateException e) {
			e.printStackTrace();
			logger.error("用户【" + userId + "】生成交易合同异常，异常信息为：" + e.getMessage());
			result.setResultCode(ResultCode.RESULT_FAILED);
			result.setResultMsg("用户生成合同异常");
			return result;
		}finally {
	      if(fw != null) {
	        try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("用户【" + userId + "】生成交易合同异常，异常信息为：" + e.getMessage());
				result.setResultCode(ResultCode.RESULT_FAILED);
				result.setResultMsg("用户生成合同异常");
				return result;
			}
	      }
	    }
	    
	    result.setResultObj(htmlFileName);
		return result;
	}

	
}
