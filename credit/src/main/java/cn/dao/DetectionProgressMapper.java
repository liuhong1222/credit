package cn.dao;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import cn.entity.DetectionProgress;

@Mapper
public interface DetectionProgressMapper {
	
	DetectionProgress findByUserIdAndFileUploadId(Map<String,Object> paramMap);
	
	int updateStatusByUserIdAndFileUploadId(Map<String,Object> paramMap);
	
	int updateDetectedCountById(Map<String,Object> paramMap);
	
	int saveDetectionProgress(DetectionProgress detectionProgress);
}
