package cn.service.impl;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;

import cn.entity.CvsFilePath;
import cn.service.CvsFilePathService;
import cn.service.ReportService;
import cn.utils.DateUtil;
import cn.utils.DateUtils;
import cn.utils.FileUtils;
import main.java.cn.common.BackResult;
import main.java.cn.common.ResultCode;
import main.java.cn.domain.CvsFilePathDomain;
import main.java.cn.domain.CvsFilePathExport;

@Service
public class ReportServiceImpl implements ReportService {

	private final static Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);
	
	@Autowired
    private CvsFilePathService cvsFilePathService;
	
	@Value("${loadfilePath}")
	private String loadfilePath;
	
	@Value("${withUserProviderService}")
	private String withUserProviderService;
	
	@Value("${getResultPwdUrl}")
	private String getResultPwdUrl;
	
	@Autowired
	private RestTemplate restTemplate;

	@Override
	public BackResult<List<Map<String, String>>> getTestHistoryReport(String userId, String month) {
		//返回结果
		BackResult<List<Map<String, String>>> result = new BackResult<List<Map<String, String>>>();
		List<Map<String, String>> resultList = new ArrayList<>();
		Date startDate = DateUtils.getCurrentMonthFirstDay(DateUtils.parseDate(month+"-01"));
		Date endDate = DateUtils.addDay(DateUtils.getCurrentMonthLastDay(DateUtils.parseDate(month+"-01")), 1);
		//获取检测记录
		List<CvsFilePath> cvsFilePathList = cvsFilePathService.getCvsFilePathByTime(userId, startDate, endDate);
		if(CollectionUtils.isEmpty(cvsFilePathList)){
			return result;
		}
		//获取时间段之间的全部日期
		List<Date> dateLIst = new ArrayList<Date>();
		try {
			dateLIst = DateUtils.dateSplit(startDate, DateUtils.getCurrentMonthLastDay(DateUtils.parseDate(month+"-01")));
			for(Date date : dateLIst){
				Map<String, String> param = new HashMap<>();
				param.put("date", DateUtils.formatDate(date).substring(8, 10));
				//获取该日期下的所有记录
				List<CvsFilePath> tempList = cvsFilePathList.stream().filter(a -> a.getCreateTime().getTime()>=date.getTime()
						&& a.getCreateTime().getTime()<DateUtils.addDay(date, 1).getTime()).collect(Collectors.toList());
				//实号条数
				long realCounts = 0;
				//空号条数
				long emptyCounts = 0;
				//风险条数
				long shutCounts = 0;
				//沉默条数
				long silenceCounts = 0;
				//遍历记录，计算该天检测结果
				if(CollectionUtils.isNotEmpty(cvsFilePathList)){
					for(CvsFilePath cvsFilePath : tempList){
						realCounts += cvsFilePath.getThereCount();
						emptyCounts += cvsFilePath.getSixCount();
						silenceCounts += cvsFilePath.getUnknownSize();
						shutCounts += cvsFilePath.getShutCount();
					}
				}				
				
				param.put("real", realCounts+"");
				param.put("empty", emptyCounts+"");
				param.put("shut", shutCounts+"");
				param.put("silence", silenceCounts+"");
				param.put("total", realCounts + emptyCounts + shutCounts + silenceCounts + "");
				resultList.add(param);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.setResultCode(ResultCode.RESULT_FAILED);
			result.setResultMsg("获取数据异常,请联系客服处理");
			return result;
		}
		
		Collections.sort(resultList, new DateSort());
		result.setResultObj(resultList);
		return result;
	}	
	
	private static class DateSort implements Comparator<Map<String, String>> {

        @Override
        public int compare(Map<String, String> o1, Map<String, String> o2) {
                return o1.get("date").compareTo(o2.get("date"));
        }

    }

	@Override
	public BackResult<List<CvsFilePath>> getCvsFilePathByTime(String userId, String startDate, String endDate) {
		BackResult<List<CvsFilePath>> result = new BackResult<List<CvsFilePath>>();
		//字符串转日期
		Date sd = DateUtils.parseDate(startDate);
		Date ed = DateUtils.addDay(DateUtils.parseDate(endDate), 1);
		//获取检测记录
		List<CvsFilePath> cvsFilePathList = cvsFilePathService.getCvsFilePathByTime(userId, sd, ed);
		if(CollectionUtils.isEmpty(cvsFilePathList)){
			result.setResultCode(ResultCode.RESULT_DATA_EXCEPTIONS);
			result.setResultMsg("查无数据");
			return result;
		}
		
		result.setResultObj(cvsFilePathList);
		return result;
	}

	@Override
	public BackResult<String> batchDownloadFile(String userId, String ids) {
		BackResult<String> result = new BackResult<String>();
		List<CvsFilePath> resultList = cvsFilePathService.getTxtZipByIds(ids);
        if(CollectionUtils.isEmpty(resultList)){
			result.setResultCode(ResultCode.RESULT_DATA_EXCEPTIONS);
			result.setResultMsg("查无数据");
			return result;
		}
		//文件列表
		List<File> list = new ArrayList<File>();
		for(CvsFilePath cfp : resultList){
			list.add(new File(loadfilePath + (StringUtils.isBlank(cfp.getZipPath())?"0":cfp.getZipPath())));
		}
		String subFilePath = DateUtils.getDate() + "/" + userId + "/" + String.valueOf(System.currentTimeMillis()) + "/";
		String filePath = loadfilePath + subFilePath;
		//下载的压缩包名称
		String zipName = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime()) + ".zip";
		//获取压缩包加密的密码
        String resultPwd = getResultPwdByCreUserId(userId);        
        // 检测是否存在目录
		if (!new File(loadfilePath + subFilePath + zipName).getParentFile().exists()) {
			new File(loadfilePath + subFilePath + zipName).getParentFile().mkdirs();
		}
		//是否需要对检测结果加密
        if(StringUtils.isBlank(resultPwd)){
        	FileUtils.batchCreateZip(list, filePath + zipName);
        }else{
        	//对压缩包进行加密
        	FileUtils.createZipByPassword(list, filePath + zipName,resultPwd);
        }
        
        result.setResultObj(filePath + zipName);
		return result;
	}
	
	private String getResultPwdByCreUserId(String creUserId){
    	String result = "";
		String url = withUserProviderService + getResultPwdUrl + "?creUserId=" + creUserId;
		JSONObject resultConsume = restTemplate.getForObject(url, JSONObject.class);	
		if (null != resultConsume && resultConsume.getString("resultCode").equals(ResultCode.RESULT_SUCCEED)) {
			if(StringUtils.isNotBlank(resultConsume.getString("resultObj"))){
				result = resultConsume.getString("resultObj");
			}
		}
		return result;		
	}

	@Override
	public BackResult<List<CvsFilePathExport>> cvsFilePathExport(String userId, String startDate, String endDate) {
		BackResult<List<CvsFilePathExport>> result = new BackResult<List<CvsFilePathExport>>();
		List<CvsFilePathExport> resultList = new ArrayList<>();
		List<CvsFilePath> tempList = new ArrayList<>();
		if(StringUtils.isBlank(startDate) && StringUtils.isBlank(endDate)){
			tempList = cvsFilePathService.findByUserId(userId);
		}else{
			//字符串转日期
			Date sd = DateUtils.parseDate(startDate);
			Date ed = DateUtils.addDay(DateUtils.parseDate(endDate), 1);
			//获取下载文件的信息
			tempList = cvsFilePathService.getCvsFilePathByTime(userId, sd, ed);
		}		
        if(CollectionUtils.isEmpty(tempList)){
			result.setResultCode(ResultCode.RESULT_DATA_EXCEPTIONS);
			result.setResultMsg("查无数据");
			return result;
		}
        //数据结构转换
        for(CvsFilePath cfp : tempList){
        	CvsFilePathExport cfpe = new CvsFilePathExport();
        	cfpe.setZipName(cfp.getZipName());
        	cfpe.setZipSize(cfp.getZipSize());
        	cfpe.setCreateTime(DateUtils.formatDateByDet(cfp.getCreateTime()));
        	cfpe.setThereCount(cfp.getThereCount()+"");
        	cfpe.setUnknownSize(cfp.getUnknownSize()+"");
        	cfpe.setSixCount(cfp.getSixCount()+"");
        	cfpe.setShutCount(cfp.getShutCount()+"");
        	//计算总条数
        	int totalCount = cfp.getThereCount() + 
        			cfp.getUnknownSize()+ 
        					cfp.getSixCount() + 
        							cfp.getShutCount();
        	cfpe.setTotalCount(totalCount + "");
        	resultList.add(cfpe);
        }
        
        result.setResultObj(resultList);
		return result;
	}
}
