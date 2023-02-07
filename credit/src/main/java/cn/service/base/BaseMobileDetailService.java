package cn.service.base;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public interface BaseMobileDetailService<T, ID extends Serializable> {

	/**
	 * 根据手机号码和时间段查询 默认reportTime降序
	 * @param mobile
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	List<T> findByMobileAndReportTime(String mobile, Date startTime, Date endTime);
	
	/**
	 * 根据手机号码查询
	 * @param mobile
	 * @return
	 */
	List<T> findByMobile(String mobile);
}
