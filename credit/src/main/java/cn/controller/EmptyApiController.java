package cn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.service.EmptyApiService;
import main.java.cn.common.ApiResult;

@RestController
@RequestMapping("/empty/api")
public class EmptyApiController {

	@Autowired
	private EmptyApiService emptyApiService;
	
	@RequestMapping(value = "/batchUcheck", method = RequestMethod.POST)
	public ApiResult batchUcheck(String mobiles,Integer creUserId) {
		return emptyApiService.batchUcheck(mobiles,creUserId);
	}
}
