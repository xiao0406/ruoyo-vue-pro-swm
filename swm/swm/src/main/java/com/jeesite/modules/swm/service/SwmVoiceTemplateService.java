/**
 * 语音模板表服务接口
 * @author auto
 * @date 2024-05-29
 */
package com.jeesite.modules.swm.service;

import java.util.List;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.entity.SwmVoiceTemplate;

/**
 * 语音模板表服务接口
 * 
 * @author auto
 */
public interface SwmVoiceTemplateService {

    /**
     * 获取单条数据
     */
    SwmVoiceTemplate get(String id);
    
    /**
     * 获取单条数据
     */
    SwmVoiceTemplate get(SwmVoiceTemplate voiceTemplate);

    /**
     * 查询分页数据
     */
    Page<SwmVoiceTemplate> findPage(SwmVoiceTemplate voiceTemplate);
    
    /**
     * 查询分页数据（带页面参数）
     */
    Page<SwmVoiceTemplate> findPage(Page<SwmVoiceTemplate> page, SwmVoiceTemplate voiceTemplate);

    /**
     * 查询列表数据
     */
    List<SwmVoiceTemplate> findList(SwmVoiceTemplate voiceTemplate);

    /**
     * 保存数据
     */
    void save(SwmVoiceTemplate voiceTemplate);

    /**
     * 删除数据
     */
    void delete(SwmVoiceTemplate voiceTemplate);

    /**
     * 根据模板代码获取模板
     */
    SwmVoiceTemplate getByTemplateCode(String templateCode);
} 