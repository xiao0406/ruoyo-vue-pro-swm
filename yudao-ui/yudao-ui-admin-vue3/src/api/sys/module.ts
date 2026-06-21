/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author ThinkGem
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { adminPath } = useGlobSetting();

export interface Module extends BasicModel<Module> {
  moduleCode?: string; // 模块编码
  moduleName?: string; // 模块名称
  description?: string; // 模块描述
  mainClassName?: string; // 主类全名
  currentVersion?: string; // 当前版本
  upgradeInfo?: string; // 升级信息
}

export interface RoleRecord {
  isNewRecord?: string;
  menuIcon?: string; // 菜单图标
  weight?: number; // 菜单权重
  menuNameRaw?: string; // 名称
  treeSort?: string | number; // 排序信息
  menuType?: string; // 菜单类型
  menuCode?: string; // 菜单编码
  isShow?: string;
  moduleCodes?: number; // 模块
  sysCode?: string | number; // 系统代码
  extend?: {
    extendS1: string;
    extendS2: string;
    extendS3: string;
    extendS4: string;
    extendS5: string;
    extendS6: string;
    extendS7: string;
    extendS8: string;
  }; // 扩展
}

export const moduleList = (params?: Module | any) =>
  request.get<Module>({ url: adminPath + '/sys/module/list', params });

export const moduleListData = (params?: Module | any) =>
  request.post<Page<Module>>({ url: adminPath + '/sys/module/listData', params });

export const moduleForm = (params?: Module | any) =>
  request.get<Module>({ url: adminPath + '/sys/module/form', params });

export const moduleSave = (params?: any, data?: Module | any) =>
  request.postJson<Module>({ url: adminPath + '/sys/module/save', params, data });

export const moduleDisable = (params?: Module | any) =>
  request.get<Module>({ url: adminPath + '/sys/module/disable', params });

export const moduleEnable = (params?: Module | any) =>
  request.get<Module>({ url: adminPath + '/sys/module/enable', params });

export const moduleDelete = (params?: Module | any) =>
  request.get<Module>({ url: adminPath + '/sys/module/delete', params });
