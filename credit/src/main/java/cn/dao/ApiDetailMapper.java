package cn.dao;

import org.apache.ibatis.annotations.Mapper;

import cn.entity.ApiDetail;

@Mapper
public interface ApiDetailMapper {
		
	int saveOne(ApiDetail apiDetail);
}
