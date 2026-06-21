/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author wxy
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { mdmPath, apiUrl } = useGlobSetting();

export interface SsiipMdmProjectLayout extends BasicModel<SsiipMdmProjectLayout> {
  responPerson?: string; // 责任人
  responUnit?: string; // 责任单位(字典)
  dateTime?: string; // 日期时间
  region?: string; // 区域分布
  projectId?: string; // 项目id
  lossAmount?: string; // 损失量(保存list)list:describe:描述;programme:方案;lossAmount:损失量
  fileUrl?: string; // 文件url
  remark?: string; // 备注
}

export const ssiipMdmProjectLayoutList = (params?: SsiipMdmProjectLayout | any) =>
  request.get<SsiipMdmProjectLayout>({
    url: mdmPath + '/mdm/ssiipMdmProjectLayout/list',
    params,
  });

export const ssiipMdmProjectLayoutListData = (params?: SsiipMdmProjectLayout | any) =>
  request.post<Page<SsiipMdmProjectLayout>>({
    url: mdmPath + '/mdm/ssiipMdmProjectLayout/pageList',
    params,
  });

export const ssiipMdmProjectLayoutForm = (params?: SsiipMdmProjectLayout | any) =>
  request.get<SsiipMdmProjectLayout>({
    url: mdmPath + '/mdm/ssiipMdmProjectLayout/form',
    params,
  });

export const ssiipMdmProjectLayoutSave = (params?: any, data?: SsiipMdmProjectLayout | any) =>
  request.postJson<SsiipMdmProjectLayout>({
    url: mdmPath + '/mdm/ssiipMdmProjectLayout/save',
    params,
    data,
  });

export const ssiipMdmProjectLayoutDelete = (params?: SsiipMdmProjectLayout | any) =>
  request.get<SsiipMdmProjectLayout>({
    url: mdmPath + '/mdm/ssiipMdmProjectLayout/delete',
    params,
  });
//导出
export const ssiipMdmProjectLayoutExport = (params?: SsiipMdmProjectLayout | any) =>
  request.post<SsiipMdmProjectLayout>({
    url: mdmPath + '/mdm/ssiipMdmProjectLayout/export',
    params,
  });
// 文件上传
export const uploadFile = (params: any) =>
  request.uploadFile({ url: apiUrl + '/m/oss/fileUpload/single' }, params);
export const pdmGetWfNodeFieldConfig = (params?: SsiipMdmProjectLayout | any) =>
  request.post<SsiipMdmProjectLayout>({
    url: mdmPath + '/mdm/workFlowConfig/wfNodeFieldConfig',
    // params,
    data: params,
  });
/*  
  表单-- 初始 数据配置信息
  @params
  wfKey: auxiliary_material
*/
export const pdmGetStartFieldConfig = (data?: SsiipMdmProjectLayout | any) =>
  request.post<SsiipMdmProjectLayout>({
    url: mdmPath + '/mdm/workFlowConfig/wfStartNodeConfig',
    data,
  });
export const pdmGetEndNodeConfig = (data?: SsiipMdmProjectLayout | any) =>
  request.post<SsiipMdmProjectLayout>({
    url: mdmPath + '/mdm/workFlowConfig/wfEndNodeConfig',
    data,
  });
