/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author wxy
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { mdmPath, apiUrl } = useGlobSetting();

export interface SsiipMdmProjectMaterialCreate extends BasicModel<SsiipMdmProjectMaterialCreate> {
  projectId?: string; // 项目id
  materialCategory?: string; // 创校类别
  illustrate?: string; // 创效说明
  materialLimit?: number; // 预计创效额（元）
  confirmDate?: string; // 确认日期
  fileUrl?: string; // 文件
  remark?: string; // 备注
}

export const ssiipMdmProjectMaterialCreateList = (params?: SsiipMdmProjectMaterialCreate | any) =>
  request.get<SsiipMdmProjectMaterialCreate>({
    url: mdmPath + '/mdm/ssiipMdmProjectMaterialCreate/list',
    params,
  });

export const ssiipMdmProjectMaterialCreateListData = (
  params?: SsiipMdmProjectMaterialCreate | any,
) =>
  request.post<Page<SsiipMdmProjectMaterialCreate>>({
    url: mdmPath + '/mdm/ssiipMdmProjectMaterialCreate/pageList',
    params,
  });

export const ssiipMdmProjectMaterialCreateForm = (params?: SsiipMdmProjectMaterialCreate | any) =>
  request.get<SsiipMdmProjectMaterialCreate>({
    url: mdmPath + '/mdm/ssiipMdmProjectMaterialCreate/form',
    params,
  });

export const ssiipMdmProjectMaterialCreateSave = (
  params?: any,
  data?: SsiipMdmProjectMaterialCreate | any,
) =>
  request.postJson<SsiipMdmProjectMaterialCreate>({
    url: mdmPath + '/mdm/ssiipMdmProjectMaterialCreate/save',
    params,
    data,
  });

export const ssiipMdmProjectMaterialCreateDelete = (params?: SsiipMdmProjectMaterialCreate | any) =>
  request.get<SsiipMdmProjectMaterialCreate>({
    url: mdmPath + '/mdm/ssiipMdmProjectMaterialCreate/delete',
    params,
  });

//导出
export const ssiipMdmProjectMaterialCreateExport = (params?: SsiipMdmProjectMaterialCreate | any) =>
  request.post<SsiipMdmProjectMaterialCreate>({
    url: mdmPath + '/mdm/ssiipMdmProjectMaterialCreate/excelExport',
    params,
  });
// 文件上传
export const uploadFile = (params: any) =>
  request.uploadFile({ url: apiUrl + '/m/oss/fileUpload/single' }, params);
export const pdmGetWfNodeFieldConfig = (params?: SsiipMdmProjectMaterialCreate | any) =>
  request.post<SsiipMdmProjectMaterialCreate>({
    url: mdmPath + '/mdm/workFlowConfig/wfNodeFieldConfig',
    // params,
    data: params,
  });
/*  
  表单-- 初始 数据配置信息
  @params
  wfKey: auxiliary_material
*/
export const pdmGetStartFieldConfig = (data?: SsiipMdmProjectMaterialCreate | any) =>
  request.post<SsiipMdmProjectMaterialCreate>({
    url: mdmPath + '/mdm/workFlowConfig/wfStartNodeConfig',
    data,
  });
export const pdmGetEndNodeConfig = (data?: SsiipMdmProjectMaterialCreate | any) =>
  request.post<SsiipMdmProjectMaterialCreate>({
    url: mdmPath + '/mdm/workFlowConfig/wfEndNodeConfig',
    data,
  });
