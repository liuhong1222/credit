package cn.service;

import cn.entity.DetectionProgress;

/**
 * @since 2018/6/13
 */
public interface DetectionProgressService {

    DetectionProgress findOneByUserIdAndFileUploadId(Long userId, String fileUploadId);

    int updateStatusByUserIdAndFileUploadId(Long userId,String fileUploadId,Integer status);

    int updateDetectedCountById(String id,Long detectedCount);

    DetectionProgress insert(Long userId,String fileUploadId,Integer type,Long totalCount,Long detectedCount,Integer status);
}
