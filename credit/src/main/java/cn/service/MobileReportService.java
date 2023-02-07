package cn.service;

import cn.entity.mobileReport.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @since 2018/5/9
 */
public interface MobileReportService {

    /**
     * 查询默认手机状态类别表
     *
     * @return
     */
    List<MobileReportGroup> findReportGroupsByNotDeleted();


    /**
     * 查询默认手机状态列表
     *
     * @return
     */
    List<MobileReportDelivrd> findReportDelivrdsByNotDeleted();

    /**
     * 获取状态代码和前置百分比map
     *
     * @return
     */
    Map<String, Integer> getBaseGroupCodeFrontPercentMap();

    /**
     * 获取状态码map
     *
     * @return
     */
    Map<String, MobileReportDelivrd> getBaseDelivrdMap() throws Exception;


    /**
     * 查询用户所属手机状态类别表
     *
     * @param userId
     * @return
     */
    List<UserMobileReportGroup> findUserReportGroupsByUserIdAndNotDeleted(String userId);


    /**
     * 根据userId查询用户所属手机状态列表
     *
     * @param userId
     * @return
     */
    List<UserMobileReportDelivrd> findUserReportDelivrdsByUserIdAndNotDeleted(String userId);


    /**
     * 根据userId和groupCode查询用户所属手机状态列表
     *
     * @param userId
     * @param groupCode
     * @return
     */
    List<UserMobileReportDelivrd> findUserReportDelivrdsByUserIdAndGroupCodeAndNotDeleted(String userId,
                                                                                          String groupCode);

    /**
     * 获取用户状态代码和前置百分比map
     *
     * @param userId
     * @return
     */
    Map<String, Integer> getUserGroupCodeFrontPercentMap(String userId);

    /**
     * 获取用户所属状态码map
     *
     * @param userId
     * @return
     */
    Map<String, UserMobileReportDelivrd> getUserDelivrdMap(String userId);


    /**
     * 手机检测，间隔多少天内的数据为有效
     *
     * @param userId
     * @return
     */
    UserMobileReportIntervalDays findUserMobileReportIntervalDays(String userId);

}
