package cn.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.service.FileDownLoadService;
import cn.service.FileUploadService;
import cn.service.ForeignService;
import main.java.cn.common.BackResult;
import main.java.cn.domain.CvsFilePathDomain;
import main.java.cn.domain.FileUploadDomain;
import main.java.cn.domain.RunTestDomian;
import main.java.cn.domain.page.PageDomain;

@RestController
@RequestMapping("/credit")
public class CreditController {

	@Autowired
	private ForeignService foreignService;
	
	@Autowired
	private FileUploadService fileUploadService;
	
	@Autowired
	private FileDownLoadService fileDownLoadService;
	
	@RequestMapping(value = "/runTheTest", method = RequestMethod.GET)
	public BackResult<RunTestDomian> runTheTest(HttpServletRequest request, HttpServletResponse response,String fileUrl,String userId, String timestamp,String mobile) {
		return foreignService.runTheTest(fileUrl, userId,timestamp,mobile);
	}
	
	@RequestMapping(value = "/theTest", method = RequestMethod.POST)
	public BackResult<RunTestDomian> theTest(String code,String userId, String source,String mobile,String startLine,String type) {
		return foreignService.theTest(code, userId, mobile, source, startLine,type);
	}
	
	@RequestMapping(value = "/findByUserId", method = RequestMethod.GET)
	public BackResult<List<CvsFilePathDomain>> findByUserId(HttpServletRequest request, HttpServletResponse response,String userId) {
		return foreignService.findByUserId(userId);
	}
	
	@RequestMapping(value = "/deleteCvsByIds", method = RequestMethod.GET)
	public BackResult<Boolean> deleteCvsByIds(HttpServletRequest request, HttpServletResponse response,String ids,String userId){
		return foreignService.deleteCvsByIds(ids, userId);
	}
	
	@RequestMapping(value = "/deleteCvsByTime", method = RequestMethod.GET)
	public BackResult<Boolean> deleteCvsByTime(HttpServletRequest request, HttpServletResponse response,String userId,String ids){
		return foreignService.deleteCvsByIds(userId,ids);
	}
	
	@RequestMapping(value = "/getTxtZipByIds", method = RequestMethod.POST)
	public BackResult<String> getTxtZipByIds(HttpServletRequest request, HttpServletResponse response,String ids,String userId){
		return foreignService.getTxtZipByIds(ids, userId);
	}

	@RequestMapping(value = "/getCVSPageByUserIdNew", method = RequestMethod.POST)
	public BackResult<PageDomain<CvsFilePathDomain>> getCVSPageByUserIdNew(int pageNo, int pageSize, String userId,String startDate,String endDate){
		return foreignService.getCVSPageByUserIdNew(pageNo, pageSize, userId,startDate,endDate);
	}
	
	@RequestMapping(value = "/getCVSPageByUserId", method = RequestMethod.POST)
	public BackResult<PageDomain<CvsFilePathDomain>> getPageByUserId(int pageNo, int pageSize, String userId){
		return foreignService.getPageByUserId(pageNo, pageSize, userId);
	}
	
	@RequestMapping(value = "/saveFileUpload", method = RequestMethod.POST)
	public BackResult<FileUploadDomain> saveFileUpload(@RequestBody FileUploadDomain domain){
		return fileUploadService.save(domain);
	}
	
	@RequestMapping(value = "/findFileUploadById", method = RequestMethod.POST)
	public BackResult<FileUploadDomain> findFileUploadById(String id){
		return fileUploadService.findById(id);
	}
	
	@RequestMapping(value = "/getCvsFilePathByFileCode", method = RequestMethod.POST)
	public BackResult<CvsFilePathDomain> getCvsFilePathByFileCode(HttpServletRequest request, HttpServletResponse response,String userId,String id,String fileCode) {
		return fileDownLoadService.getCvsFilePathByFileCode(userId,id,fileCode);
	}
}
