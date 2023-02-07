package cn.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import cn.entity.SplitFileVo;
import cn.entity.TxtFileContent;
import cn.enums.TxtSuffixEnum;
import cn.service.impl.ForeignServiceImpl;
import cn.utils.CommonUtils;
import cn.utils.EncodingDetect;
import cn.utils.TxtFileUtil;
import cn.utils.UUIDTool;
import main.java.cn.common.RedisKeys;

@Service
public class FileService {
	
	private final static Logger log = LoggerFactory.getLogger(ForeignServiceImpl.class);

	public TxtFileContent getValidMobileListByTxt(String fileUrl) {
		File file = new File(fileUrl); 
		if(file.length() <= 1048576 * 6) {
			return getValidMobileListBySmallTxt(fileUrl);
		}
		
		// 分割文件
		String tempOrders = UUIDTool.getInstance().getUUID();
        SplitFileVo splitFileVo = TxtFileUtil.splitFileByError(file, (int)(((file.length() / (1048576 * 10)) + 1) * 8),tempOrders);
        // 合并文件且计算文件号码数量
    	int fileCount = TxtFileUtil.distinctNew(splitFileVo.getFileList(), getTxtPath(fileUrl, TxtSuffixEnum.ALL), (int)(((file.length() / (1048576 * 10)) + 1) * 8));
        
    	//获取文件编码格式
    	String fileCode = EncodingDetect.getJavaEncode(fileUrl);
    	TxtFileContent result =  new TxtFileContent();
    	result.setFileCode(fileCode);
        result.setMobileCounts(fileCount);
        result.setErrorCounts(splitFileVo.getErrorCounts());
        result.setMobileList(splitFileVo.getMobileList());
		return result;
	}
	
	private String getTxtPath(String fileUrl, TxtSuffixEnum txtSuffixEnum) {
        return fileUrl + "_" + txtSuffixEnum.getTxtSuffix();
    }
	
	public TxtFileContent getValidMobileListBySmallTxt(String fileUrl) {
		TxtFileContent result =  new TxtFileContent();
        File file = new File(fileUrl);        
        BufferedReader br = null;
        Integer errorCounts = 0;
        List<String> tempList = new ArrayList<String>();
		HashSet<String> temeSet = new HashSet<String>();
		//获取文件编码格式
    	String fileCode = EncodingDetect.getJavaEncode(fileUrl);
        try {
	        if (file.isFile() && file.exists()) {
	        	result.setFileCode(fileCode);
	        	//读取文件
	            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), fileCode));				
	            String lineTxt = null;	
	            while ((lineTxt = br.readLine()) != null) {	
	                if (StringUtils.isBlank(lineTxt)) {
	                    continue;
	                }
	                // 去掉字符串中的所有空格
	                lineTxt = lineTxt.trim().replace(" ", "").replace("　", "");
	                // 验证是否为正常的１１位有效数字
	                if (!CommonUtils.isMobile(lineTxt)) {
	                	errorCounts++;
	                    continue;
	                }
	                
	                temeSet.add(lineTxt);
	            }	 
	            
	            //保存全部的号码到缓存
	            tempList = new ArrayList<String>(temeSet);
	            TxtFileUtil.saveTxt(tempList, getTxtPath(fileUrl, TxtSuffixEnum.ALL), fileCode, false);
	        }
        } catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			log.error(fileUrl+"----------文件编码格式转换异常：", e);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			log.error(fileUrl+"----------文件未找到：", e);
		} catch (IOException e) {
			e.printStackTrace();
			log.error(fileUrl+"----------文件流读取异常：", e);
		}finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                log.error(fileUrl+"----------文件流关闭异常：", e);
            }

        }

        result.setMobileList(tempList.subList(0, tempList.size() <= 72?tempList.size():72));
        result.setMobileCounts(tempList.size());
        result.setErrorCounts(errorCounts);
		return result;
	}
}
