package cn.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import cn.dao.AgentCreUserMapper;
import cn.entity.AgentCreUser;
import cn.redis.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AgentCreUserService {

	@Autowired
	private RedisClient redisClient;
	
	@Autowired
	private AgentCreUserMapper agentCreUserMapper;
	
	public static final String AGENT_ID_KEY = "kh:agentId:";
	
	private final static Logger logger = LoggerFactory.getLogger(AgentCreUserService.class);
	
	public Integer getAgentIdByUserId(String userId) {
		String rediString = redisClient.get(AGENT_ID_KEY+userId);
		if (StringUtils.isBlank(rediString)) {
			AgentCreUser agentCreUser = agentCreUserMapper.findOneByCreUserId(userId);
			if (agentCreUser == null) {
				return 0;
			}
			
			logger.info("{}, info:{}",userId,JSON.toJSONString(agentCreUser));
			redisClient.set(AGENT_ID_KEY+userId, agentCreUser.getAgentId().toString(), 60 * 30);
			return Integer.valueOf(agentCreUser.getAgentId().toString());
		}
		
		return Integer.valueOf(rediString);
	}
}
