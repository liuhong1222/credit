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
		//θΏεη»ζ
		BackResult<List<Map<String, String>>> result = new BackResult<List<Map<String, String>>>();
		List<Map<String, String>> resultList = new ArrayList<>();
		Date startDate = DateUtils.getCurrentMonthFirstDay(DateUtils.parseDate(month+"-01"));
		Date endDate = DateUtils.addDay(DateUtils.getCurrentMonthLastDay(DateUtils.parseDate(month+"-01")), 1);
		//θ·εζ£ζ΅θ?°ε½
		List<CvsFilePath> cvsFilePathList = cvsFilePathService.getCvsFilePathByTime(userId, startDate, endDate);
		if(CollectionUtils.isEmpty(cvsFilePathList)){
			return result;
		}
		//θ·εζΆι΄ζ?΅δΉι΄ηε¨ι¨ζ₯ζ
		List<Date> dateLIst = new ArrayList<Date>();
		try {
			dateLIst = DateUtils.dateSplit(startDate, DateUtils.getCurrentMonthLastDay(DateUtils.parseDate(month+"-01")));
			for(Date date : dateLIst){
				Map<String, String> param = new HashMap<>();
				param.put("date", DateUtils.formatDate(date).substring(8, 10));
				//θ·εθ―₯ζ₯ζδΈηζζθ?°ε½
				List<CvsFilePath> tempList = cvsFilePathList.stream().filter(a -> a.getCreateTime().getTime()>=date.getTime()
						&& a.getCreateTime().getTime()<DateUtils.addDay(date, 1).getTime()).collect(Collectors.toList());
				//ε?ε·ζ‘ζ°
				long realCounts = 0;
				//η©Ίε·ζ‘ζ°
				long emptyCounts = 0;
				//ι£ι©ζ‘ζ°
				long shutCounts = 0;
				//ζ²ι»ζ‘ζ°
				long silenceCounts = 0;
				//ιεθ?°ε½οΌθ?‘η?θ―₯ε€©ζ£ζ΅η»ζ
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
			result.setResultMsg("θ·εζ°ζ?εΌεΈΈ,θ―·θη³»ε?’ζε€η");
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
		//ε­η¬¦δΈ²θ½¬ζ₯ζ
		Date sd = DateUtils.parseDate(startDate);
		Date ed = DateUtils.addDay(DateUtils.parseDate(endDate), 1);
		//θ·εζ£ζ΅θ?°ε½
		List<CvsFilePath> cvsFilePathList = cvsFilePathService.getCvsFilePathByTime(userId, sd, ed);
		if(CollectionUtils.isEmpty(cvsFilePathList)){
			result.setResultCode(ResultCode.RESULT_DATA_EXCEPTIONS);
			result.setResultMsg("ζ₯ζ ζ°ζ?");
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
			result.setResultMsg("ζ₯ζ ζ°ζ?");
			return result;
		}
		//ζδ»Άεθ‘¨
		List<File> list = new ArrayList<File>();
		for(CvsFilePath cfp : resultList){
			list.add(new File(loadfilePath + (StringUtils.isBlank(cfp.getZipPath())?"0":cfp.getZipPath())));
		}
		String subFilePath = DateUtils.getDate() + "/" + userId + "/" + String.valueOf(System.currentTimeMillis()) + "/";
		String filePath = loadfilePath + subFilePath;
		//δΈθ½½ηεηΌ©εεη§°
		String zipName = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime()) + ".zip";
		//θ·εεηΌ©εε ε―ηε―η 
        String resultPwd = getResultPwdByCreUserId(userId);        
        // ζ£ζ΅ζ―ε¦ε­ε¨η?ε½
		if (!new File(loadfilePath + subFilePath + zipName).getParentFile().exists()) {
			new File(loadfilePath + subFilePath + zipName).getParentFile().mkdirs();
		}
		//ζ―ε¦ιθ¦ε―Ήζ£ζ΅η»ζε ε―
        if(StringUtils.isBlank(resultPwd)){
        	FileUtils.batchCreateZip(list, filePath + zipName);
        }else{
        	//ε―ΉεηΌ©εθΏθ‘ε ε―
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
			//ε­η¬¦δΈ²θ½¬ζ₯ζ
			Date sd = DateUtils.parseDate(startDate);
			Date ed = DateUtils.addDay(DateUtils.parseDate(endDate), 1);
			//θ·εδΈθ½½ζδ»ΆηδΏ‘ζ―
			tempList = cvsFilePathService.getCvsFilePathByTime(userId, sd, ed);
		}		
        if(CollectionUtils.isEmpty(tempList)){
			result.setResultCode(ResultCode.RESULT_DATA_EXCEPTIONS);
			result.setResultMsg("ζ₯ζ ζ°ζ?");
			return result;
		}
        //ζ°ζ?η»ζθ½¬ζ’
        for(CvsFilePath cfp : tempList){
        	CvsFilePathExport cfpe = new CvsFilePathExport();
        	cfpe.setZipName(cfp.getZipName());
        	cfpe.setZipSize(cfp.getZipSize());
        	cfpe.setCreateTime(DateUtils.formatDateByDet(cfp.getCreateTime()));
        	cfpe.setThereCount(cfp.getThereCount()+"");
        	cfpe.setUnknownSize(cfp.getUnknownSize()+"");
        	cfpe.setSixCount(cfp.getSixCount()+"");
        	cfpe.setShutCount(cfp.getShutCount()+"");
        	//θ?‘η?ζ»ζ‘ζ°
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
