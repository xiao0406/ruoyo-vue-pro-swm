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
  propertyName?: string; // 名称
  propertyKey?: string; // 参数键
  propertyValue?: string; // 参数值
  isSys?: string; // 系统内置（1是 0否）
}

export const propertyList = (params?: Config | any) =>
  request.get<Config>({ url: adminPath + '/sys/property/list', params });

export const propertyListData = (params?: Config | any) =>
  request.post<Page<Config>>({ url: adminPath + '/sys/layout/listData', params });
export const propertyDataSave = (params?: Config | any) =>
  request.postJson<Page<Config>>({ url: adminPath + '/sys/layout/save', params });
export const propertyDataDelete = (params?: Config | any) =>
  request.post<Page<Config>>({
    url: adminPath + '/sys/layout/delete',
    params,
  });
