/**
 * 语音模板表服务实现类
 * @author zwf
 * @date 2024-05-29
 */
package com.jeesite.modules.swm.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmVoiceTemplateDao;
import com.jeesite.modules.swm.entity.SwmVoiceTemplate;
import com.jeesite.modules.swm.service.SwmVoiceTemplateService;

/**
 * 语音模板表服务实现类
 * 
 * @author zwf
 */
@Service
@Transactional(readOnly = true)
public class SwmVoiceTemplateServiceImpl extends CrudService<SwmVoiceTemplateDao, SwmVoiceTemplate> implements SwmVoiceTemplateService {

    @Autowired
    private SwmVoiceTemplateDao swmVoiceTemplateDao;

    @Override
    public SwmVoiceTemplate get(String id) {
        return super.get(id);
    }
    
    @Override
    public SwmVoiceTemplate get(SwmVoiceTemplate voiceTemplate) {
        return super.get(voiceTemplate);
    }

    /**
     * 查询分页数据
     * @param voiceTemplate
     * @return
     */
    public Page<SwmVoiceTemplate> findPage(SwmVoiceTemplate voiceTemplate) {
        return super.findPage(voiceTemplate);
    }
    
    @Override
    public Page<SwmVoiceTemplate> findPage(Page<SwmVoiceTemplate> page, SwmVoiceTemplate voiceTemplate) {
        // 设置分页参数
        voiceTemplate.setPage(page);
        // 执行查询
        return this.findPage(voiceTemplate);
    }

    @Override
    public List<SwmVoiceTemplate> findList(SwmVoiceTemplate voiceTemplate) {
        return super.findList(voiceTemplate);
    }

    @Override
    @Transactional(readOnly = false)
    public void save(SwmVoiceTemplate voiceTemplate) {
        super.save(voiceTemplate);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(SwmVoiceTemplate voiceTemplate) {
        super.delete(voiceTemplate);
    }
    
    @Override
    @Transactional(readOnly = false)
    public void deletePhysical(SwmVoiceTemplate voiceTemplate) {
        swmVoiceTemplateDao.deletePhysical(voiceTemplate);
    }

    @Override
    public SwmVoiceTemplate getByTemplateCode(String templateCode) {
        if (templateCode == null || templateCode.isEmpty()) {
            return null;
        }
        SwmVoiceTemplate voiceTemplate = new SwmVoiceTemplate();
        voiceTemplate.setTemplateCode(templateCode);
        List<SwmVoiceTemplate> list = findList(voiceTemplate);
        return list.isEmpty() ? null : list.get(0);
    }
    
    @Override
    public Page<SwmVoiceTemplate> findPageWithoutStatusFilter(Page<SwmVoiceTemplate> page, SwmVoiceTemplate voiceTemplate) {
        // 设置分页参数
        voiceTemplate.setPage(page);
        // 调用DAO直接查询，不过滤status
        page.setList(swmVoiceTemplateDao.findListWithoutStatusFilter(voiceTemplate));
        return page;
    }
    
    @Override
    public List<SwmVoiceTemplate> findListWithStatusZero(SwmVoiceTemplate voiceTemplate) {
        // 调用DAO查询状态为0的数据
        return swmVoiceTemplateDao.findListWithStatusZero(voiceTemplate);
    }
} 