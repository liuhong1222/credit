package cn.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.dao.ApiDetailMapper;
import cn.entity.ApiDetail;

@Service
public class ApiDetailService {

	private final static Logger logger = LoggerFactory.getLogger(ApiDetailService.class);
	
	private ExecutorService executor = Executors.newFixedThreadPool(10);
	
	@Autowired
	private ApiDetailMapper apiDetailMapper;
	
	public void saveOne(ApiDetail apiDetail) {
		executor.execute(new Runnable() {
			
			@Override
			public void run() {
				apiDetailMapper.saveOne(apiDetail);
			}
		});
	}
}
