/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author wxy
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { mdmPath } = useGlobSetting();

export interface SsiipMdmBusSysInput extends BasicModel<SsiipMdmBusSysInput> {
  projectId?: string; // 项目id
  engineerDistribution?: string; // 工程分布
  partCondition?: string; // 零件情况(字典表)
  remark?: string; // 备注
  ssiipMdmBusConditionList?: any[]; // 子表列表
}

export const ssiipMdmBusSysInputList = (params?: SsiipMdmBusSysInput | any) =>
  request.get<SsiipMdmBusSysInput>({
    url: mdmPath + '/mdm/ssiipMdmBusSysInput/list',
    params,
  });

export const ssiipMdmBusSysInputListData = (params?: SsiipMdmBusSysInput | any) =>
  request.post<Page<SsiipMdmBusSysInput>>({
    url: mdmPath + '/mdm/ssiipMdmBusSysInput/pageList',
    params,
  });

export const ssiipMdmBusSysInputForm = (params?: SsiipMdmBusSysInput | any) =>
  request.get<SsiipMdmBusSysInput>({
    url: mdmPath + '/mdm/ssiipMdmBusSysInput/form',
    params,
  });

export const ssiipMdmBusSysInputSave = (params?: any, data?: SsiipMdmBusSysInput | any) =>
  request.postJson<SsiipMdmBusSysInput>({
    url: mdmPath + '/mdm/ssiipMdmBusSysInput/save',
    params,
    data,
  });

export const ssiipMdmBusSysInputDisable = (params?: SsiipMdmBusSysInput | any) =>
  request.get<SsiipMdmBusSysInput>({
    url: mdmPath + '/mdm/ssiipMdmBusSysInput/disable',
    params,
  });

export const ssiipMdmBusSysInputEnable = (params?: SsiipMdmBusSysInput | any) =>
  request.get<SsiipMdmBusSysInput>({
    url: mdmPath + '/mdm/ssiipMdmBusSysInput/enable',
    params,
  });

export const ssiipMdmBusSysInputDelete = (params?: SsiipMdmBusSysInput | any) =>
  request.get<SsiipMdmBusSysInput>({
    url: mdmPath + '/mdm/ssiipMdmBusSysInput/delete',
    params,
  });
//导出
export const ssiipMdmBusSysInputExport = (params?: SsiipMdmBusSysInput | any) =>
  request.post<SsiipMdmBusSysInput>({
    url: mdmPath + '/mdm/ssiipMdmBusSysInput/export',
    params,
  });
export const pdmGetWfNodeFieldConfig = (params?: SsiipMdmBusSysInput | any) =>
  request.post<SsiipMdmBusSysInput>({
    url: mdmPath + '/mdm/workFlowConfig/wfNodeFieldConfig',
    // params,
    data: params,
  });
/*  
  表单-- 初始 数据配置信息
  @params
  wfKey: auxiliary_material
*/
export const pdmGetStartFieldConfig = (data?: SsiipMdmBusSysInput | any) =>
  request.post<SsiipMdmBusSysInput>({
    url: mdmPath + '/mdm/workFlowConfig/wfStartNodeConfig',
    data,
  });
export const pdmGetEndNodeConfig = (data?: SsiipMdmBusSysInput | any) =>
  request.post<SsiipMdmBusSysInput>({
    url: mdmPath + '/mdm/workFlowConfig/wfEndNodeConfig',
    data,
  });
