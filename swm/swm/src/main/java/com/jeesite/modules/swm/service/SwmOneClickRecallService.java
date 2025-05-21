/**
 * 一键召回记录表服务接口
 * @author zwf
 * @date 2024-05-30
 */
package com.jeesite.modules.swm.service;

import java.util.List;

import com.jeesite.common.entity.Page;
import com.jeesite.modules.swm.entity.SwmOneClickRecall;

/**
 * 一键召回记录表服务接口
 * 
 * @author zwf
 */
public interface SwmOneClickRecallService {

    /**
     * 获取单条数据
     */
    SwmOneClickRecall get(String id);
    
    /**
     * 获取单条数据
     */
    SwmOneClickRecall get(SwmOneClickRecall oneClickRecall);

    /**
     * 查询分页数据
     */
    Page<SwmOneClickRecall> findPage(SwmOneClickRecall oneClickRecall);
    
    /**
     * 查询分页数据（带页面参数）
     */
    Page<SwmOneClickRecall> findPage(Page<SwmOneClickRecall> page, SwmOneClickRecall oneClickRecall);

    /**
     * 查询列表数据
     */
    List<SwmOneClickRecall> findList(SwmOneClickRecall oneClickRecall);

    /**
     * 保存数据
     */
    void save(SwmOneClickRecall oneClickRecall);

    /**
     * 删除数据
     */
    void delete(SwmOneClickRecall oneClickRecall);
    
    /**
     * 批量删除数据
     */
    void deleteAll(String[] ids);
} 