package cn.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cn.entity.MobileNumberSection;
import cn.service.MobileNumberSectionService;

@Service
public class MobileNumberSectionServiceImpl implements MobileNumberSectionService {

    public static final String REDIS_MOBILE_NUMBER_SECTION_SET = "REDIS_MOBILE_NUMBER_SECTION_SET";

    @Value("${mobile.section.file}")
    private String filePath;

    private Map<String, MobileNumberSection> mobileNumberSectionMap = new HashMap<String, MobileNumberSection>();

    @Override
    public MobileNumberSection findByNumberSection(String numberSection) {
        return mobileNumberSectionMap.get(numberSection);
    }

    @Override
    public List<MobileNumberSection> findListByNumberSections(List<String> numberSections) {
    	List<MobileNumberSection> list = new ArrayList<MobileNumberSection>();
    	for(String numberSection : numberSections) {
    		if (mobileNumberSectionMap.get(numberSection) != null) {
    			list.add(mobileNumberSectionMap.get(numberSection));
			}    		
    	}
        return list;
    }

    @PostConstruct
    public void init() throws Exception{
        File file = new File(filePath);

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            // 读取直到最后一行
            String line = "";
            while ((line = br.readLine()) != null) {
                // 把一行数据分割成多个字段
                String []item = line.split(",");
                if (item.length == 9) {
                	MobileNumberSection mobileNumberSection = new MobileNumberSection();
                	mobileNumberSection.setAreaCode(item[0]);
                	mobileNumberSection.setNumberSection(item[1]);
                	mobileNumberSection.setProvince(item[2]);
                	mobileNumberSection.setCity(item[3]);
                	mobileNumberSection.setCityCode(item[4]);
                	mobileNumberSection.setPrefix(item[5]);
                	mobileNumberSection.setIsp(item[6]);
                	mobileNumberSection.setPostCode(item[7]);
                	mobileNumberSection.setMobilePhoneType(item[8]);
                	mobileNumberSectionMap.put(item[1], mobileNumberSection);
                }
            }
            br.close();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
