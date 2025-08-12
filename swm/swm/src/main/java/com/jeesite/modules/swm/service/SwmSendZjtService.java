package com.jeesite.modules.swm.service;

import cn.hutool.core.collection.CollectionUtil;
import com.jeesite.common.lang.ObjectUtils;
import com.jeesite.common.service.ServiceException;
import com.jeesite.modules.api.SwmSendZjtServiceApi;
import com.jeesite.modules.api.ZJTMessageServiceApi;
import com.jeesite.modules.model.bo.ZJTMessageBo;
import com.jeesite.modules.swm.dao.SwmAlarmConfigDetailDao;
import com.jeesite.modules.swm.entity.SwmAlarmConfig;
import com.jeesite.modules.swm.entity.SwmAlarmConfigDetail;
import com.jeesite.modules.vo.SwmAlarmConfigDetailVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * 推送中建通service
 * @author gjl
 * @version 2025-8-11
 */
@Service
@Transactional(readOnly = true)
public class SwmSendZjtService implements SwmSendZjtServiceApi {
    @Resource
    private SwmAlarmConfigDetailDao detailDao;
    @Resource
    private ZJTMessageServiceApi zjtMessageServiceApi;
    /**
     * 推送中建通
     * @param detail
     *使用json方便传参
     */
    @Override
    public void send(SwmAlarmConfigDetailVO detail) {
        //判断参数
        if (ObjectUtils.isEmpty(detail)){
            throw new ServiceException("参数不能为空！");
        }
        if (ObjectUtils.isEmpty(detail.getMainKey())){
            throw new ServiceException("报警配置不能为空！");
        }
        if (ObjectUtils.isEmpty(detail.getContent())){
            throw new ServiceException("推送内容不能为空！");
        }
        SwmAlarmConfig config = detailDao.getByAlarmKey(detail.getMainKey());
        //根据报警配置key获取人员信息
        if (ObjectUtils.isEmpty(config)){
            throw new ServiceException("未找到对应的报警配置！");
        }
        if ("1".equals(config.getIsSendZjt())){
            throw new ServiceException("该报警配置已关闭推送！");
        }
        SwmAlarmConfigDetail configDetail = new SwmAlarmConfigDetail();
        configDetail.setMainId(config.getId());
        SwmAlarmConfigDetail byEntity = detailDao.getByEntity(configDetail);
        if (ObjectUtils.isEmpty(byEntity)){
            throw new ServiceException("请先配置推送人员信息！");
        }
        //配置中建通消息体
        sendZjt(byEntity,detail);
    }

    /***
     * 推送中建通消息封装
     * @param byEntity
     * @param detail
     */
    public void sendZjt(SwmAlarmConfigDetail byEntity,SwmAlarmConfigDetailVO detail){
        // 封装消息体(公共参数)
        ZJTMessageBo zjtMessageBo1 = new ZJTMessageBo();
        zjtMessageBo1.setMsgType("text");
        zjtMessageBo1.setTerminal(Arrays.asList("QW"));
        zjtMessageBo1.setUserType("QW");
        zjtMessageBo1.setDigitalSignature(0);
        List<String> userCodes = new ArrayList<>();
        //根据角色，人员查询人员编码
        if (ObjectUtils.isNotEmpty(byEntity.getRoles())){
            List<String> users = detailDao.findByRoleIds(Arrays.asList(String.join(",", byEntity.getRoles())));
            if (CollectionUtil.isNotEmpty(users)){
                userCodes.addAll(users);
            }
        }
        if (ObjectUtils.isNotEmpty(byEntity.getUsers())){
            List<String>  users =  detailDao.findByUserIds(  Arrays.asList(String.join(",", byEntity.getUsers())));
            if (CollectionUtil.isNotEmpty(users)){
                userCodes.addAll(users);
            }
        }
        zjtMessageBo1.setUsers(userCodes);
        if (CollectionUtil.isNotEmpty(userCodes)){
            List<String> uniqueList = new ArrayList<>(new HashSet<>(userCodes));
            zjtMessageBo1.setContent(detail.getContent());
            zjtMessageBo1.setUsers(uniqueList);
            //发送推送消息
            zjtMessageServiceApi.sendMessage(zjtMessageBo1);
        }
    }
}
