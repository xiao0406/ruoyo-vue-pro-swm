package cn.iocoder.yudao.module.swm.mq.producer.entity;

import cn.hutool.core.util.IdUtil;
import lombok.Data;
import java.io.Serializable;
import java.util.Map;

/**
 * 人员变更消息实体
 */
@Data
public class PersonChangeMsg implements Serializable {
    private static final long serialVersionUID = 1L;

    // 唯一消息ID（保证幂等）
    private String msgId = IdUtil.simpleUUID();
    // 人员ID
    private String personId;
    // 操作类型：ADD/EDIT/BIND_HELMET/UNBIND_HELMET/DELETE/IMPORT
    private String operateType;
    // 变更数据（JSON格式，存储变更前后的字段）
    private Map<String, Object> changeData;
    // 操作时间
    private Long operateTime = System.currentTimeMillis();
}
