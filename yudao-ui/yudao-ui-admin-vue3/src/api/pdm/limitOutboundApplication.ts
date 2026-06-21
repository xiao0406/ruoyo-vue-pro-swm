/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author wxy
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { mdmPath } = useGlobSetting();

export interface ProjectBridgeManage extends BasicModel<ProjectBridgeManage> {
  bussinessType?: string; // 业务类型(字典表)
  projectId?: string; // 项目id
  connected?: string; // 联次
  pictureName?: string; // 图名
  engineer?: number; // 工程量(KG)
  dutyPerson?: string; // 责任人
  overDate?: string; // 完成时间
  remark?: string; // 备注
  isHelp?: string; // 是否外协(0:是 1:否)
}

export const projectBridgeManageDelete = (params?: ProjectBridgeManage | any) =>
  request.post<ProjectBridgeManage>({
    url: mdmPath + '/mdm/project/projectBridgeManage/delete',
    params,
  });

export const projectBridgeManageSave = (params?: any, data?: ProjectBridgeManage | any) =>
  request.postJson<ProjectBridgeManage>({
    url: mdmPath + '/mdm/project/projectBridgeManage/save',
    params,
    data,
  });

export const projectProjectBridgeExcelExport = (params?: any, data?: ProjectBridgeManage | any) =>
  request.post<ProjectBridgeManage>({
    url: mdmPath + '/mdm/project/projectBridgeManage/projectBridgeExcelExport',
    params,
    data,
  });

export const outboundApplicationFindPage = (params?: ProjectBridgeManage | any) =>
  request.post<Page<ProjectBridgeManage>>({
    url: mdmPath + '/mdm/outboundApplication/findPage',
    params,
  });
export const outboundApplicationFindDetailsList = (params?: ProjectBridgeManage | any) =>
  request.post<Page<ProjectBridgeManage>>({
    url: mdmPath + '/mdm/outboundApplication/findDetailsList',
    params,
  });
