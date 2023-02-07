package cn.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.entity.CvsFilePath;
import cn.service.ReportService;
import main.java.cn.common.BackResult;
import main.java.cn.domain.CvsFilePathExport;

@RestController
@RequestMapping("/report")
public class ReportController {

	@Autowired
	private ReportService reportService;
	
	@RequestMapping(value = "/getTestHistoryReport", method = RequestMethod.POST)
	public BackResult<List<Map<String,String>>> getTestHistoryReport(HttpServletRequest request, HttpServletResponse response,String month,String userId) {
		return reportService.getTestHistoryReport(userId,month);
	}
	
	@RequestMapping(value = "/getCvsFilePathByTime", method = RequestMethod.POST)
	public BackResult<List<CvsFilePath>> getCvsFilePathByTime(HttpServletRequest request, HttpServletResponse response,String userId,String startDate,String endDate) {
		return reportService.getCvsFilePathByTime(userId,startDate,endDate);
	}
	
	@RequestMapping(value = "/batchDownloadFile", method = RequestMethod.POST)
	public BackResult<String> batchDownloadFile(HttpServletRequest request, HttpServletResponse response,String userId,String ids) {
		return reportService.batchDownloadFile(userId,ids);
	}
	
	@RequestMapping(value = "/cvsFilePathExport", method = RequestMethod.POST)
	public BackResult<List<CvsFilePathExport>> cvsFilePathExport(HttpServletRequest request, HttpServletResponse response,String userId,String startDate,String endDate) {
		return reportService.cvsFilePathExport(userId,startDate,endDate);
	}
}
