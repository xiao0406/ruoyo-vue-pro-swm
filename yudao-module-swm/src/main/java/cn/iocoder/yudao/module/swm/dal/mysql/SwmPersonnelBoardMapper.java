package cn.iocoder.yudao.module.swm.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonnelBoardDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SwmPersonnelBoardMapper extends BaseMapperX<SwmPersonnelBoardDO> {

    List<String> findLeisureAreaIds();

    List<SwmPersonnelBoardDO> findList(@Param("name") String name,
                                        @Param("organization") String organization,
                                        @Param("workshop") String workshop,
                                        @Param("process") String process,
                                        @Param("team") String team,
                                        @Param("deviceIds") List<String> deviceIds,
                                        @Param("personnelStatus") String personnelStatus,
                                        @Param("timeType") String timeType,
                                        @Param("timeValue") String timeValue);
}
