package cn.iocoder.yudao.module.swm.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHandleRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface SwmHandleRecordMapper extends BaseMapperX<SwmHandleRecordDO> {

    List<SwmHandleRecordDO> findList(@Param("recordName") String recordName,
                                      @Param("warningId") String warningId,
                                      @Param("warningRecord") String warningRecord,
                                      @Param("warningContent") String warningContent,
                                      @Param("handler") String handler,
                                      @Param("handleStatus") String handleStatus,
                                      @Param("beginAlarmTime") Date beginAlarmTime,
                                      @Param("endAlarmTime") Date endAlarmTime,
                                      @Param("beginHandleTime") Date beginHandleTime,
                                      @Param("endHandleTime") Date endHandleTime);

    List<SwmHandleRecordDO> latestHandleRecord(@Param("limit") Integer limit);
}
