package cn.iocoder.yudao.module.iot.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.TcpSendMessageLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TcpSendMessageLogMapper extends BaseMapperX<TcpSendMessageLogDO> {

}
