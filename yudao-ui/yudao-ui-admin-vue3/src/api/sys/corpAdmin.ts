/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author ThinkGem
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { Page, TreeDataModel } from '../model/baseModel';
import { User } from './user';

const { adminPath } = useGlobSetting();

export const corpAdminList = (params?: User | any) =>
  request.get<User>({ url: adminPath + '/sys/corpAdmin/list', params });

export const corpAdminListData = (params?: User | any) =>
  request.post<Page<User>>({ url: adminPath + '/sys/corpAdmin/listData', params });

export const corpAdminForm = (params?: User | any) =>
  request.get<User>({ url: adminPath + '/sys/corpAdmin/form', params });

export const corpAdminSave = (params?: any, data?: User | any) =>
  request.postJson<User>({ url: adminPath + '/sys/corpAdmin/save', params, data });

export const corpAdminDisable = (params?: User | any) =>
  request.get<User>({ url: adminPath + '/sys/corpAdmin/disable', params });

export const corpAdminEnable = (params?: User | any) =>
  request.get<User>({ url: adminPath + '/sys/corpAdmin/enable', params });

export const resetpwd = (params?: User | any) =>
  request.get<User>({ url: adminPath + '/sys/corpAdmin/resetpwd', params });

export const corpAdminDelete = (params?: User | any) =>
  request.get<User>({ url: adminPath + '/sys/corpAdmin/delete', params });

export const corpAdminTreeData = (params?: any) =>
  request.get<TreeDataModel[]>({ url: adminPath + '/sys/corpAdmin/treeData', params });

export const switchCorp = (corpCode: string) =>
  request.get<User>({
    url: adminPath + '/sys/corpAdmin/switch/' + corpCode,
  });

// 获取租户列表
export const getCorpList = () => request.postJson({ url: adminPath + '/sys/corpAdmin/getCorpList' });
