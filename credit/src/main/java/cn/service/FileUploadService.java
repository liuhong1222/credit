package cn.service;

import cn.entity.FileUpload;
import main.java.cn.service.FileUploadBusService;

public interface FileUploadService  extends FileUploadBusService{

    FileUpload findByOne(String id);

    FileUpload save(FileUpload fileUpload);
}
