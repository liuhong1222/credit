package cn.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import cn.entity.ApiDetail;
import cn.utils.DateUtils;
import main.java.cn.common.ApiResult;
import main.java.cn.common.BackResultWanShu;
import main.java.cn.common.ResultCode;
import main.java.cn.domain.MobileInfoDomain;

@Service
public class EmptyApiService {

	private final static Logger logger = LoggerFactory.getLogger(EmptyApiService.class);
	
	@Autowired
	private ExtrenInterfaceService extrenInterfaceService;
	
	@Autowired
	private ApiDetailService apiDetailService;
	
	public ApiResult batchUcheck(String mobiles,Integer creUserId) {
		Long st = System.currentTimeMillis();
		List<String> strList = new ArrayList<String>(Arrays.asList(mobiles.split(",")));
		BackResultWanShu<List<MobileInfoDomain>> apiDomain = extrenInterfaceService.getMobileResultByApi(strList, creUserId.toString());
		if (apiDomain == null) {
			return ApiResult.failed(ResultCode.RESULT_BUSINESS_EXCEPTIONS, "业务异常");
		}
		
		if (!("000000".equals(apiDomain.getResultCode()) || "200".equals(apiDomain.getResultCode()))) {
			logger.error("{}， 下游通道调用失败，info:{}",creUserId,JSON.toJSONString(apiDomain));
			return ApiResult.failed(ResultCode.RESULT_BUSINESS_EXCEPTIONS, "业务异常");
		}
		
//		ApiDetail apiDetail = new ApiDetail();
//		apiDetail.setInvokeTime(Long.valueOf(DateUtils.getDateTime()));
//		apiDetail.setCommitCount(strList.length);
//		apiDetail.setSuccessCount(apiDomain.getChargeCounts());
//		apiDetail.setUserId(creUserId);
//		apiDetailService.saveOne(apiDetail);
		logger.info("{}, 下游通道调用成功，计费条数为：{}，耗时：{}",creUserId,apiDomain.getChargeCounts(),(System.currentTimeMillis()-st));
		
		st = null;
		strList = null;
		
		return ApiResult.success(apiDomain.getResultObj(), apiDomain.getChargeCounts());
	}
}
