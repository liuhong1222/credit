package cn.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import cn.entity.mobileReport.MobileReportDelivrd;
import cn.entity.mobileReport.MobileReportGroup;

@Mapper
public interface MobileReportMapper {
	
	List<MobileReportGroup> findMobileReportGroupAll();
	
	List<MobileReportDelivrd> findMobileReportDelivrdAll();
}
