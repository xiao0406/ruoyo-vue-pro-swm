/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author ThinkGem
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { adminPath } = useGlobSetting();

export interface Config extends BasicModel<Config> {
  configName?: string; // 名称
  configKey?: string; // 参数键
  configValue?: string; // 参数值
  isSys?: string; // 系统内置（1是 0否）
}

export const configList = (params?: Config | any) =>
  request.get<Config>({ url: adminPath + '/sys/config/list', params });

export const configListData = (params?: Config | any) =>
  request.post<Page<Config>>({ url: adminPath + '/sys/config/listData', params });

export const configForm = (params?: Config | any) =>
  request.get<Config>({ url: adminPath + '/sys/config/form', params });

export const checkConfigKey = (oldConfigKey: string, configKey: string) =>
  request.get<Config>({
    url: adminPath + '/sys/config/checkConfigKey',
    params: { oldConfigKey, configKey },
  });

export const configSave = (params?: any, data?: Config | any) =>
  request.postJson<Config>({ url: adminPath + '/sys/config/save', params, data });

export const configDelete = (params?: Config | any) =>
  request.get<Config>({ url: adminPath + '/sys/config/delete', params });
