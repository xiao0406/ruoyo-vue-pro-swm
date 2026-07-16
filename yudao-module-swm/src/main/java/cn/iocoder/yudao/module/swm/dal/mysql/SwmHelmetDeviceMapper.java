package cn.iocoder.yudao.module.swm.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHelmetDeviceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SwmHelmetDeviceMapper extends BaseMapperX<SwmHelmetDeviceDO> {

    List<SwmHelmetDeviceDO> findAvailableHelmets(@Param("keyword") String keyword);

    SwmHelmetDeviceDO getByDeviceId(@Param("deviceId") String deviceId);

    void clearDeviceAssignment(@Param("deviceId") String deviceId);

    List<SwmHelmetDeviceDO> findHelmetDeviceListWithRelations(@Param("deviceId") String deviceId,
                                                               @Param("helmetType") String helmetType,
                                                               @Param("assignedPerson") String assignedPerson,
                                                               @Param("personName") String personName,
                                                               @Param("imei") String imei,
                                                               @Param("deviceColor") String deviceColor,
                                                               @Param("powerOnStatus") String powerOnStatus,
                                                               @Param("deviceOnlist") List<String> deviceOnlist);

    Long findHelmetDeviceCountWithRelations(@Param("deviceId") String deviceId,
                                             @Param("helmetType") String helmetType,
                                             @Param("assignedPerson") String assignedPerson,
                                             @Param("personName") String personName);

    List<SwmHelmetDeviceDO> findHelmetDeviceListByDeviceIds(@Param("deviceIdList") List<String> deviceIdList,
                                                             @Param("helmetType") String helmetType,
                                                             @Param("assignedPerson") String assignedPerson,
                                                             @Param("personName") String personName);

    List<SwmHelmetDeviceDO> findDeviceTenantMapping(@Param("tenantId") Long tenantId,
                                                    @Param("random") Integer random);

    List<SwmHelmetDeviceDO> findListInit(@Param("random") Integer random);

    List<SwmHelmetDeviceDO> findHelmetDeviceListByIds(@Param("list") List<String> list);

    List<String> findDeviceIdsByDeviceSource(@Param("deviceSource") String deviceSource,
                                              @Param("random") Integer random);

    Long selectCount(@Param("deviceId") String deviceId,
                      @Param("helmetType") String helmetType,
                      @Param("assignedPerson") String assignedPerson,
                      @Param("personName") String personName,
                      @Param("imei") String imei,
                      @Param("deviceColor") String deviceColor,
                      @Param("powerOnStatus") String powerOnStatus,
                      @Param("deviceOnlist") List<String> deviceOnlist);
}

