/**
 * 语音模板表DAO接口
 * @author zwf
 * @date 2024-05-29
 */
package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmVoiceTemplate;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 语音模板表DAO接口
 * 
 * @author zwf
 */
@MyBatisDao
public interface SwmVoiceTemplateDao extends CrudDao<SwmVoiceTemplate> {
    
    /**
     * 查询列表数据（不过滤status）
     */
    List<SwmVoiceTemplate> findListWithoutStatusFilter(SwmVoiceTemplate voiceTemplate);
    
    /**
     * 查询状态为0的所有数据
     */
    List<SwmVoiceTemplate> findListWithStatusZero(SwmVoiceTemplate voiceTemplate);
    
    /**
     * 物理删除数据
     */
    void deletePhysical(SwmVoiceTemplate voiceTemplate);
    
    /**
     * 更新状态
     */
    void updateStatus(@Param("id") String id, @Param("status") String status);
    
    /**
     * 根据ID列表批量查询语音模板
     * @param ids 语音模板ID列表
     * @return 语音模板列表
     */
    List<SwmVoiceTemplate> findByIds(@Param("ids") List<String> ids);
} 