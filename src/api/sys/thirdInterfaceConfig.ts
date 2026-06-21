/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author wly
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { adminPath } = useGlobSetting();

export interface ThirdInterfaceConfig extends BasicModel<ThirdInterfaceConfig> {
  providerName?: string; // 接入商名称
  developType?: string; // 开发类型
  protocol?: string; // 协议
  appCode?: string; // appKey
  secret?: string; // secret
  pushDeploy?: string; // 推送配置（暂缓中间表）
  projectCode?: string; // 项目授权（暂缓中间表）
  lifeTime?: string; // 有效时间
}

export const thirdInterfaceConfigList = (params?: ThirdInterfaceConfig | any) =>
  request.get<ThirdInterfaceConfig>({ url: adminPath + '/sys/thirdInterfaceConfig/list', params });

export const thirdInterfaceConfigListData = (params?: ThirdInterfaceConfig | any) =>
  request.post<Page<ThirdInterfaceConfig>>({
    url: adminPath + '/sys/thirdInterfaceConfig/pageList',
    params,
  });

export const thirdInterfaceConfigForm = (params?: ThirdInterfaceConfig | any) =>
  request.get<ThirdInterfaceConfig>({ url: adminPath + '/sys/thirdInterfaceConfig/form', params });

export const thirdInterfaceConfigSave = (params?: any, data?: ThirdInterfaceConfig | any) =>
  request.postJson<ThirdInterfaceConfig>({
    url: adminPath + '/sys/thirdInterfaceConfig/save',
    params,
    data,
  });

export const thirdInterfaceConfigDelete = (params?: ThirdInterfaceConfig | any) =>
  request.get<ThirdInterfaceConfig>({
    url: adminPath + '/sys/thirdInterfaceConfig/delete',
    params,
  });

// 获取appKey
export const thirdInterfaceConfigGetAppKey = (params?: ThirdInterfaceConfig | any) =>
  request.get<ThirdInterfaceConfig>({
    url: adminPath + '/sys/thirdInterfaceConfig/getAppKey',
    params,
  });

// 获取secret
export const thirdInterfaceConfigGetSecret = (params?: ThirdInterfaceConfig | any) =>
  request.get<ThirdInterfaceConfig>({
    url: adminPath + '/sys/thirdInterfaceConfig/getSecret',
    params,
  });
