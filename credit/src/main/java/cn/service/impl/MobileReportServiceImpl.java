package cn.service.impl;

import cn.dao.MobileReportMapper;
import cn.entity.mobileReport.*;
import cn.redis.RedisClient;
import cn.redis.RedisKeyList;
import cn.service.MobileReportService;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import javax.annotation.PostConstruct;

/**
 * @since 2018/5/10
 */
@Service
public class MobileReportServiceImpl implements MobileReportService {
    private final static Logger logger = LoggerFactory
            .getLogger(MobileReportServiceImpl.class);

    @Autowired
    private MobileReportMapper mobileReportMapper;

    @Autowired
    private RedisClient redisClient;
    
    private List<MobileReportGroup> mobileReportGroupList = new ArrayList<MobileReportGroup>();
    
    private List<MobileReportDelivrd> mobileReportDelivrdList = new ArrayList<MobileReportDelivrd>();

    @PostConstruct
    private void findMobileReportGroupList() {
    	mobileReportGroupList = mobileReportMapper.findMobileReportGroupAll();
    }
    
    @PostConstruct
    private void findMobileReportDelivrdList() {
    	mobileReportDelivrdList = mobileReportMapper.findMobileReportDelivrdAll();
    }
    
    /**
     * 查询默认手机状态类别表
     *
     * @return
     */
    @Override
    public List<MobileReportGroup> findReportGroupsByNotDeleted() {
        return mobileReportGroupList;
    }


    /**
     * 查询默认手机状态列表
     *
     * @return
     */
    @Override
    public List<MobileReportDelivrd> findReportDelivrdsByNotDeleted() {
        return mobileReportDelivrdList;
    }


    /**
     * 获取状态代码和前置百分比map
     *
     * @return
     */
    @Override
    public Map<String, Integer> getBaseGroupCodeFrontPercentMap() {
        Map<String, Integer> baseGroupCodeFrontPercentMap = new HashMap<>();
        List<MobileReportGroup> mobileReportGroupList = findReportGroupsByNotDeleted();
        if (!CollectionUtils.isEmpty(mobileReportGroupList)) {
            //组别码会去重  非常重要
            for (MobileReportGroup mobileReportGroup : mobileReportGroupList) {
                baseGroupCodeFrontPercentMap.put(mobileReportGroup.getGroupCode(),mobileReportGroup.getFrontPercent());
            }
        }
        return baseGroupCodeFrontPercentMap;
    }


    /**
     * 获取状态码map
     *
     * @return
     */
    @Override
    public Map<String, MobileReportDelivrd> getBaseDelivrdMap() throws Exception {
        Map<String, MobileReportDelivrd> redisBaseDelivrdMap = (Map<String, MobileReportDelivrd>)redisClient.getObject(RedisKeyList.MOBILE_REPORT_DELIVRD_MAP);
        if (!CollectionUtils.isEmpty(redisBaseDelivrdMap)){
            return redisBaseDelivrdMap;
        }
        //默认手机状态列表
        List<MobileReportDelivrd> baseDelivrdList = findReportDelivrdsByNotDeleted();
        //默认手机状态map
        Map<String, MobileReportDelivrd> baseDelivrdMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(baseDelivrdList)) {
            //状态码会去重  非常重要
            for (MobileReportDelivrd mobileReportDelivrd : baseDelivrdList) {
                if (StringUtils.isNotBlank(mobileReportDelivrd.getTypeCode())){
                    baseDelivrdMap.put(mobileReportDelivrd.getDelivrd(),mobileReportDelivrd);
                }
            }
//            baseDelivrdMap = baseDelivrdList.stream().collect(Collectors.toMap(
//                    MobileReportDelivrd::getDelivrd, mobileReportDelivrd -> mobileReportDelivrd));
        }
        try{
            redisClient.setObject(RedisKeyList.MOBILE_REPORT_DELIVRD_MAP,baseDelivrdMap,60*10);
        }
        catch (Exception e) {
            logger.error("redis写入错误",e);
        }

        return baseDelivrdMap;
    }


    /**
     * 查询用户所属手机状态类别表
     *
     * @param userId
     * @return
     */
    @Override
    public List<UserMobileReportGroup> findUserReportGroupsByUserIdAndNotDeleted(String userId) {
        return null;
    }


    /**
     * 根据userId查询用户所属手机状态列表
     *
     * @param userId
     * @return
     */
    @Override
    public List<UserMobileReportDelivrd> findUserReportDelivrdsByUserIdAndNotDeleted(String userId) {
        return null;
    }


    /**
     * 根据userId和groupCode查询用户所属手机状态列表
     *
     * @param userId
     * @param groupCode
     * @return
     */
    @Override
    public List<UserMobileReportDelivrd> findUserReportDelivrdsByUserIdAndGroupCodeAndNotDeleted(String userId,
                                                                                                 String groupCode) {
        return null;
    }


    /**
     * 获取用户状态代码和前置百分比map
     *
     * @param userId
     * @return
     */
    @Override
    public Map<String, Integer> getUserGroupCodeFrontPercentMap(String userId) {
        Map<String, Integer> userGroupCodeFrontPercentMap = new HashMap<>();
        List<UserMobileReportGroup> userMobileReportGroupList = findUserReportGroupsByUserIdAndNotDeleted(userId);
        if (!CollectionUtils.isEmpty(userMobileReportGroupList)) {
            //组别码会去重  非常重要
            for (UserMobileReportGroup userMobileReportGroup : userMobileReportGroupList) {
                userGroupCodeFrontPercentMap.put(userMobileReportGroup.getGroupCode(),userMobileReportGroup.getFrontPercent());
            }
//            userGroupCodeFrontPercentMap = userMobileReportGroupList.stream().collect(Collectors.toMap(
//                    UserMobileReportGroup::getGroupCode, UserMobileReportGroup::getFrontPercent));
        }
        return userGroupCodeFrontPercentMap;
    }

    /**
     * 获取用户所属手机状态码map
     *
     * @param userId
     * @return
     */
    @Override
    public Map<String, UserMobileReportDelivrd> getUserDelivrdMap(String userId) {
        //用户所属手机状态列表
        List<UserMobileReportDelivrd> userDelivrdList = findUserReportDelivrdsByUserIdAndNotDeleted(userId);
        //用户所属手机状态map
        Map<String, UserMobileReportDelivrd> userDelivrdMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(userDelivrdList)) {
            //状态码会去重  非常重要
            for (UserMobileReportDelivrd userMobileReportDelivrd : userDelivrdList) {
                userDelivrdMap.put(userMobileReportDelivrd.getDelivrd(),userMobileReportDelivrd);
            }
//            userDelivrdMap = userDelivrdList.stream()
//                    .collect(Collectors.toMap(UserMobileReportDelivrd::getDelivrd,
//                            userMobileReportDelivrd -> userMobileReportDelivrd));
        }
        return userDelivrdMap;
    }

    /**
     * 手机检测，间隔多少天内的数据为有效
     *
     * @param userId
     * @return
     */
    @Override
    public UserMobileReportIntervalDays findUserMobileReportIntervalDays(String userId) {
        return null;
    }

   

}
