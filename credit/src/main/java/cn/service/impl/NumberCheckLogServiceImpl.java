package cn.service.impl;

import cn.dao.CosumptionLogMapper;
import cn.service.NumberCheckLogService;
import main.java.cn.domain.NumberCheckLogDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @since 2018/6/13
 */
@Service
public class NumberCheckLogServiceImpl implements NumberCheckLogService {

	private final static Logger logger = LoggerFactory.getLogger(CvsFilePathServiceImpl.class);
	
    @Autowired
    private CosumptionLogMapper cosumptionLogMapper;

	@Override
	public int saveNumberCheckLog(NumberCheckLogDomain numberCheckLogDomain) {
		return cosumptionLogMapper.saveNumberCheckConsumption(numberCheckLogDomain);
	}
}
