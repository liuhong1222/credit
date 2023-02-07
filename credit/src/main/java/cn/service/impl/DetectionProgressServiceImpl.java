package cn.service.impl;

import cn.dao.DetectionProgressMapper;
import cn.entity.DetectionProgress;
import cn.service.DetectionProgressService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 2018/6/13
 */
@Service
public class DetectionProgressServiceImpl implements DetectionProgressService {

	private final static Logger logger = LoggerFactory.getLogger(CvsFilePathServiceImpl.class);
	
    @Autowired
    private DetectionProgressMapper detectionProgressMapper;

    @Override
    public DetectionProgress findOneByUserIdAndFileUploadId(Long userId, String fileUploadId) {
    	Map<String, Object> paraMap = new HashMap<String, Object>();
    	paraMap.put("userId", userId);
    	paraMap.put("fileUploadId", fileUploadId);
        return detectionProgressMapper.findByUserIdAndFileUploadId(paraMap);
    }

    @Override
    public int updateStatusByUserIdAndFileUploadId(Long userId, String fileUploadId,Integer status) {
    	Map<String, Object> paraMap = new HashMap<String, Object>();
    	paraMap.put("userId", userId);
    	paraMap.put("fileUploadId", fileUploadId);
    	paraMap.put("status", status);
    	int count = detectionProgressMapper.updateStatusByUserIdAndFileUploadId(paraMap);
    	logger.info("{} update file detection status, fileCode:{},status:{},update count:{} ",userId,fileUploadId,status,count);
    	return count;
    }

    @Override
    public int updateDetectedCountById(String id, Long detectedCount) {
    	Map<String, Object> paraMap = new HashMap<String, Object>();
    	paraMap.put("id", id);
    	paraMap.put("detectedCount", detectedCount);
    	int count = detectionProgressMapper.updateDetectedCountById(paraMap);
    	logger.info("detection id:{},update detection count:{},update count:{}",id,detectedCount,count);
    	return count;
    }

    @Override
    public DetectionProgress insert(Long userId, String fileUploadId, Integer type, Long totalCount, Long detectedCount, Integer status) {
        //新建检查进度记录
        DetectionProgress detectionProgress = new DetectionProgress();
        detectionProgress.setUserId(userId);
        detectionProgress.setFileUploadId(fileUploadId);
        detectionProgress.setType(type);
        detectionProgress.setTotalCount(totalCount);
        detectionProgress.setDetectedCount(detectedCount);
        detectionProgress.setStatus(status);
        detectionProgress.setCreateTime(new Date());
        detectionProgress.setUpdateTime(new Date());
        int count = detectionProgressMapper.saveDetectionProgress(detectionProgress);
        logger.info("insert detection data, userId:{},fileCode:{},insert count:{},return id:{}",userId,fileUploadId,count,detectionProgress.getId());
        return detectionProgress;
    }


}
