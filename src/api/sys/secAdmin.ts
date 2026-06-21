/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author ThinkGem
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { Page } from '../model/baseModel';
import { User } from './user';

const { adminPath } = useGlobSetting();

export const secAdminList = (params?: User | any) =>
  request.get<User>({ url: adminPath + '/sys/secAdmin/list', params });

export const secAdminListData = (params?: User | any) =>
  request.post<Page<User>>({ url: adminPath + '/sys/secAdmin/listData', params });

export const secAdminForm = (params?: User | any) =>
  request.get<User>({ url: adminPath + '/sys/secAdmin/form', params });

export const secAdminSave = (params?: any) =>
  request.post<User>({ url: adminPath + '/sys/secAdmin/save', params });

export const secAdminDelete = (params?: User | any) =>
  request.get<User>({ url: adminPath + '/sys/secAdmin/delete', params });
