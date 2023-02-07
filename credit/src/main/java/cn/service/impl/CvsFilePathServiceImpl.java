package cn.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import cn.dao.CvsFilePathMapper;
import cn.entity.CvsFilePath;
import cn.service.CvsFilePathService;
import cn.utils.DateUtils;

@Service
public class CvsFilePathServiceImpl implements CvsFilePathService{

	private final static Logger logger = LoggerFactory.getLogger(CvsFilePathServiceImpl.class);
	
	@Autowired
	private CvsFilePathMapper cvsFilePathMapper;
	
	@Override
	public List<CvsFilePath> findByUserId(String userId) {
		return cvsFilePathMapper.findByUserId(userId);
	}

	@Override
	public void deleteByIds(String ids) {
		if (ids.length() >= 2) {
			if (",".equals(ids.substring(ids.length()-1, ids.length()))) {
				ids = ids.substring(0, ids.length()-1);
			}
		}
		
		int count = cvsFilePathMapper.deleteByIds(ids);
		logger.info("删除检测记录成功，记录id:{},删除条数为:{}",ids,count);
	}

	@Override
	public Page<CvsFilePath> getPageByUserId(int pageNo, int pageSize, String userId) {
		Pageable pageable = new PageRequest(pageNo - 1, pageSize);
		List<CvsFilePath> list = cvsFilePathMapper.findByUserId(userId);
		
		int totalElements = list.size();
		int fromIndex = pageable.getPageSize()*pageable.getPageNumber();
        int toIndex = pageable.getPageSize()*(pageable.getPageNumber()+1);
        if(toIndex>totalElements) toIndex = totalElements;
        
        List<CvsFilePath> resultList = list.subList(fromIndex,toIndex);
		Page<CvsFilePath> page = new PageImpl<>(resultList,pageable,totalElements);
		return page;
	}
	
	@Override
	public Page<CvsFilePath> getPageByUserIdNew(int pageNo, int pageSize, String userId,Date startDate,Date endDate) {
		Pageable pageable = new PageRequest(pageNo - 1, pageSize);
		Map<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("userId", userId);
		paraMap.put("startDate", DateUtils.formatDate(startDate));
		paraMap.put("endDate", DateUtils.formatDate(endDate));
		List<CvsFilePath> list  = cvsFilePathMapper.getCvsFilePathByTime(paraMap);
		
		int totalElements = list.size();
		int fromIndex = pageable.getPageSize()*pageable.getPageNumber();
        int toIndex = pageable.getPageSize()*(pageable.getPageNumber()+1);
        if(toIndex>totalElements) toIndex = totalElements;
        
        List<CvsFilePath> resultList = list.subList(fromIndex,toIndex);
		Page<CvsFilePath> page = new PageImpl<>(resultList,pageable,totalElements);
		return page;
	}

	@Override
	public List<CvsFilePath> getTxtZipByIds(String ids) {
		if (ids.length() >= 2) {
			if (",".equals(ids.substring(ids.length()-1, ids.length()))) {
				ids = ids.substring(0, ids.length()-1);
			}
		}
		return cvsFilePathMapper.getTxtZipByIds(ids);
	}

	@Override
	public List<CvsFilePath> getCvsFilePathByTime(String userId, Date startDate, Date endDate) {	
		Map<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("userId", userId);
		paraMap.put("startDate", DateUtils.formatDate(startDate));
		paraMap.put("endDate", DateUtils.formatDate(endDate));
		return cvsFilePathMapper.getCvsFilePathByTime(paraMap);
	}

	@Override
	public void deleteCvsByTime(String userId, Date startDate, Date endDate) {
		Map<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("userId", userId);
		paraMap.put("startDate", DateUtils.formatDate(startDate));
		paraMap.put("endDate", DateUtils.formatDate(endDate));
		int count = cvsFilePathMapper.deleteCvsByTime(paraMap);
		logger.info("删除检测记录成功，用户id:{},开始时间:{},结束时间:{},删除条数:{}",userId,
				DateUtils.formatDate(startDate),DateUtils.formatDate(endDate),count);		
	}

	@Override
	public CvsFilePath findById(String id) {
		return cvsFilePathMapper.findById(id);
	}

	@Override
	public CvsFilePath findByIdAndFileCode(String userId, String fileCode) {
		Map<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("userId", userId);
		paraMap.put("fileCode", fileCode);
		return cvsFilePathMapper.findByIdAndFileCode(paraMap);
	}

	@Override
	public int saveCvsFilePath(CvsFilePath cvsFilePath) {
		int count = cvsFilePathMapper.saveCvsFilePath(cvsFilePath);
		logger.info("保存检测记录成功,用户id:{},fileCOde:{},保存条数:{}",cvsFilePath.getUserId(),cvsFilePath.getFileCode(),count);
		return count;
	}
	
	

}
