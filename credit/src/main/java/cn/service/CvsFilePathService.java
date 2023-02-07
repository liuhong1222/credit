package cn.service;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;

import cn.entity.CvsFilePath;

public interface CvsFilePathService {
	
	List<CvsFilePath> findByUserId(String userId);

	void deleteByIds(String ids);
	
	void deleteCvsByTime(String userId,Date startDate,Date endDate);
	
	List<CvsFilePath> getTxtZipByIds(String ids);
	
	public Page<CvsFilePath> getPageByUserId(int pageNo, int pageSize, String userId);
	
	public Page<CvsFilePath> getPageByUserIdNew(int pageNo, int pageSize, String userId,Date startDate,Date endDate);
	
	List<CvsFilePath> getCvsFilePathByTime(String userId,Date startDate,Date endDate);
	
	CvsFilePath findById(String id);
	
	CvsFilePath findByIdAndFileCode(String userId,String fileCode);
	
	int saveCvsFilePath(CvsFilePath cvsFilePath);
}
