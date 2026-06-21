/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author wxy
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { mdmPath, apiUrl } = useGlobSetting();

export interface ProjectLoss extends BasicModel<ProjectLoss> {
  projectName?: string; // 项目名称
  lossProType?: string; // 项目类型(loss_pro_type 字典表)
  fullKg?: number; // 整板发料重量
  remainKg?: number; // 余料发料重量
  generateKg?: number; // 生成余量重量
  projectKg?: number; // 项目构件净重
  totalKg?: number; // 排版损耗:(整板发料重量+余量发料重量-生成余量重量-项目构件净重)/项目构件净重
  fileUrl?: string; // 文件url
  remark?: string; // 备注
}

export const projectLossList = (params?: ProjectLoss | any) =>
  request.get<ProjectLoss>({ url: mdmPath + '/mdm/modules/projectLoss/list', params });

export const projectLossListData = (params?: ProjectLoss | any) =>
  request.post<Page<ProjectLoss>>({ url: mdmPath + '/mdm/modules/projectLoss/pageList', params });

export const projectLossForm = (params?: ProjectLoss | any) =>
  request.get<ProjectLoss>({ url: mdmPath + '/mdm/modules/projectLoss/form', params });

export const projectLossSave = (params?: any, data?: ProjectLoss | any) =>
  request.postJson<ProjectLoss>({ url: mdmPath + '/mdm/modules/projectLoss/save', params, data });

export const projectLossDelete = (params?: ProjectLoss | any) =>
  request.get<ProjectLoss>({ url: mdmPath + '/mdm/modules/projectLoss/delete', params });
//导出
export const projectLossExcelExport = (params?: ProjectLoss | any) =>
  request.post<ProjectLoss>({
    url: mdmPath + '/mdm/modules/projectLoss/projectLossExcelExport',
    params,
  });
// 文件上传
export const uploadFile = (params: any) =>
  request.uploadFile({ url: apiUrl + '/m/oss/fileUpload/single' }, params);
