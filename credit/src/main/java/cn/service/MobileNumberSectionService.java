package cn.service;

import cn.entity.MobileNumberSection;

import java.util.List;

public interface MobileNumberSectionService {

    MobileNumberSection findByNumberSection(String numberSection);

    List<MobileNumberSection> findListByNumberSections(List<String> numberSections);
}
