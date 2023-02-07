package cn.service;

import java.util.List;
import java.util.Map;

import cn.entity.CvsFilePath;
import main.java.cn.common.BackResult;
import main.java.cn.domain.CvsFilePathExport;

public interface ReportService{
	
	BackResult<List<Map<String,String>>> getTestHistoryReport(String userId,String month);
	
	BackResult<List<CvsFilePath>> getCvsFilePathByTime(String userId,String startDate,String endDate);
	
	BackResult<String> batchDownloadFile(String userId,String ids);
	
	BackResult<List<CvsFilePathExport>> cvsFilePathExport(String userId,String startDate,String endDate);
}
