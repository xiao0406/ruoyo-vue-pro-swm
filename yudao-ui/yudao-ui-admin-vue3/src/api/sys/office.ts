/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author ThinkGem
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { TreeDataModel, TreeModel } from '../model/baseModel';

const { adminPath } = useGlobSetting();

export interface Office extends TreeModel<Office> {
  officeCode?: string; // 机构编码
  viewCode?: string; // 机构代码
  officeName?: string; // 机构名称
  fullName?: string; // 机构全称
  officeType?: string; // 机构类型
  leader?: string; // 负责人
  phone?: string; // 办公电话
  address?: string; // 联系地址
  zipCode?: string; // 邮政编码
  email?: string; // 电子邮箱
  extend?: any; // 扩展字段

  companyCode?: string; // 根据公司查询机构，组织机构所属公司
}

export const officeList = (params?: Office | any) =>
  request.get<Office>({ url: adminPath + '/sys/office/list', params });

export const officeListData = (params?: Office | any) =>
  request.post<Office[]>({ url: adminPath + '/sys/office/listData', params });

export const officeForm = (params?: Office | any) =>
  request.get<Office>({ url: adminPath + '/sys/office/form', params });

export const officeCreateNextNode = (params?: Office | any) =>
  request.get<Office>({ url: adminPath + '/sys/office/createNextNode', params });

export const officeSave = (params?: any, data?: Office | any) =>
  request.postJson<Office>({ url: adminPath + '/sys/office/save', params, data });

export const officeDisable = (params?: Office | any) =>
  request.post<Office>({ url: adminPath + '/sys/office/updateParendCode', params });

export const officeEnable = (params?: Office | any) =>
  request.post<Office>({ url: adminPath + '/sys/office/updateParendCodeDisPlay', params });

export const officeDelete = (params?: Office | any) =>
  request.get<Office>({ url: adminPath + '/sys/office/delete', params });

export const officeTreeData = (params?: any) =>
  request.get<TreeDataModel[]>({ url: adminPath + '/sys/office/treeData', params });

export const officeTreeDataMdmEmployee = (params?: any) =>
  // request.get<TreeDataModel[]>({ url: mdmPath + '/mdm/office/treeDataEmployee', params });
  request.get<TreeDataModel[]>({ url: adminPath + '/sys/office/treeDataEmployee', params });

// 查询机构下单位类型为工厂（大区）的字典列表数据
export const listForRegionDictData = (params?: any) =>
  request.get({ url: adminPath + '/sys/office/listForRegionDictData', params });
// 查询机构下属车间字典列表数据
export const officeWorkshopListData = (params?: any) =>
  request.post({ url: adminPath + '/sys/office/listOfficeWorkshop', params });
