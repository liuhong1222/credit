package cn.dao;

import org.apache.ibatis.annotations.Mapper;

import main.java.cn.domain.NumberCheckLogDomain;

@Mapper
public interface CosumptionLogMapper {
	
	int saveNumberCheckConsumption(NumberCheckLogDomain numberCheckLogDomain);
}
