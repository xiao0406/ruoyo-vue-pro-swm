package com.jeesite.modules.vo;

import lombok.Data;

/**
 * 推送中建通消息参数封装
 */
@Data
public class SwmAlarmConfigDetailVO {
    /**消息内容*/
    private String content;
    /**推送配置*/
    private String mainKey;
}
