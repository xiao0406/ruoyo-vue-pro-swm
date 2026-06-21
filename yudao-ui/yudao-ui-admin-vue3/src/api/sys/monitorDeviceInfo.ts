/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author 冼国文
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page, TreeDataModel } from '../model/baseModel';

const { adminPath } = useGlobSetting();

export interface MonitorDeviceInfo extends BasicModel<MonitorDeviceInfo> {
  officeCode: string; // 机构编码
  deviceType: string; // 设备类型：海康、大华,数据字典 sys_device_type
  code: string; // 设备编号，随机生成
  name: string; // 设备名称
  rtspPort: string; // rtmp直播流端口
  adminUser: string; // 设备管理用户
  adminPassword: string; // 设备管理用户密码
  rtspUri: string; // 设备资源路径
  channel: string; // 直播通道
  subtype: string; // 码流：主码流、辅码
  loadSource: string; // 加载路径
  streamUrl: string; // 直播流获取路径
}

// 列表
export const monitorDeviceInfoListData = (params?: MonitorDeviceInfo | any) =>
  request.post<Page<MonitorDeviceInfo>>({
    url: '/swm/api/public/swm/monitorDeviceInfo/listData',
    params,
  });

// 删除
export const monitorDeviceInfoDelete = (params?: MonitorDeviceInfo | any) =>
  request.post<any>({
    url: '/swm/api/public/swm/monitorDeviceInfo/delete',
    params,
  });

// 获取机构树结构a
export const companyTreeData = () =>
  request.get<MonitorDeviceInfo>({ url: '/swm/common/options/companies' });

// 获取单条信息
export const monitorDeviceInfoForm = (params?: MonitorDeviceInfo | any) =>
  request.get<MonitorDeviceInfo>({ url: '/swm/api/public/swm/monitorDeviceInfo/form', params });

// 获取角色列表
export const roleTreeData = () =>
  request.get<TreeDataModel[]>({ url: '/swm/api/public/swm/monitorDeviceInfo/roleTreeData' });

// 保存编辑
export const monitorDeviceInfoSave = (params?: MonitorDeviceInfo | any) =>
  request.post<any>({
    url: '/swm/api/public/swm/monitorDeviceInfo/save',
    params,
  });

// 保存角色分配
export const roleAccrEdit = (params?: any) =>
  request.post<any>({
    url: adminPath + '/sys/monitorDeviceInfo/roleAccredit',
    params,
  });

// 获取科工机构
export const getSubRegions = (params) =>
  request.post({
    url: adminPath + '/sys/monitorDeviceInfo/getSubRegions',
    params,
  });

// 海康监控设备
export const getCameras = (params) =>
  request.postJson({
    url: adminPath + '/sys/monitorDeviceInfo/getCameras',
    params,
  });
