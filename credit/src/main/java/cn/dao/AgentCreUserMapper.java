package cn.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import cn.entity.AgentCreUser;

@Mapper
public interface AgentCreUserMapper {
		
	AgentCreUser findOneByCreUserId(@Param("creUserId") String creUserId);
}
