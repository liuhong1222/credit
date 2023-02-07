package cn.dao;

import org.apache.ibatis.annotations.Mapper;

import cn.entity.FileUpload;

@Mapper
public interface FileUploadMapper {
	
	FileUpload findById(String id);
	
	int saveFileUpload(FileUpload fileUpload);
}
