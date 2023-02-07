package cn.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import cn.entity.CvsFilePath;

@Mapper
public interface CvsFilePathMapper {
	
	List<CvsFilePath> findByUserId(String userId);
	
	int deleteByIds(String ids);
	
	int deleteCvsByTime(Map<String, String> paraMap);
	
	List<CvsFilePath> getTxtZipByIds(String ids);
	
	List<CvsFilePath> getCvsFilePathByTime(Map<String, String> paraMap);
	
	CvsFilePath findById(String id);
	
	CvsFilePath findByIdAndFileCode(Map<String, String> paraMap);
	
	int saveCvsFilePath(CvsFilePath cvsFilePath);
}
