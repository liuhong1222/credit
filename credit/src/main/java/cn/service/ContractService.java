package cn.service;

import main.java.cn.common.BackResult;

public interface ContractService{
	
	BackResult<String> getPdfFileByHtml(String userId);
}
