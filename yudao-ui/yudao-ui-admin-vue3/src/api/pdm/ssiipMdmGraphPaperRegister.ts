/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author 李鹏
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { mdmPath, apiUrl, adminPath } = useGlobSetting();

export interface SsiipMdmGraphPaperRegister extends BasicModel<SsiipMdmGraphPaperRegister> {
  projectId?: string; // 项目编号
  projectName?: string; // 项目名称
  contractId?: string; // 工程任务ID
  contractName?: string; // 工程任务名称
  phaseId?: string; // 区域分部ID
  phaseName?: string; // 区域分部名称
  paperArrivalTime?: string; // 来图时间
  paperPackageName?: string; // 图纸文件名
  paperSharePath?: string; // 图纸共享盘路径
  steelPurchasingPlan?: string; // 对应钢材采购计划
  isOutsource?: string; // 外协双包任务
  taskName?: string; // 任务单名称
  taskNum?: string; // 任务编号
  totalNetWeight?: number; // 来图净总重(kg)
  totalGrossWeight?: number; // 来图毛总重(kg)
  isPaperPrint?: string; // 是否有构件图下发车间
  confirmationMode?: string; // 确认方式
  paperSendTime?: string; // 发图时间
  auxiliaryMaterial?: string; // 辅材提料需求
  amChargerCode?: string; // 辅材负责人ID
  amChargerName?: string; // 辅材负责人
  abChargerCode?: string; // 锚杆负责人ID
  abChargerName?: string; // 锚杆负责人
  opChargerCode?: string; // 油漆负责人ID
  opChargerName?: string; // 油漆负责人
}

//图纸登记钢材计划选项值获取
export const ssiipMdmGraphPaperRegisterSteelPlan = (params?: any) =>
  request.get<SsiipMdmGraphPaperRegister>({
    url: mdmPath + '/mdm/ycPuPulan/pageList',
    params,
  });

export const ssiipMdmGraphPaperRegisterList = (params?: SsiipMdmGraphPaperRegister | any) =>
  request.get<SsiipMdmGraphPaperRegister>({
    url: mdmPath + '/mdm/ssiipMdmGraphPaperRegister/list',
    params,
  });

// 分页
export const ssiipMdmGraphPaperRegisterListData = (params?: SsiipMdmGraphPaperRegister | any) =>
  request.post<Page<SsiipMdmGraphPaperRegister>>({
    url: mdmPath + '/mdm/ssiipMdmGraphPaperRegister/pageList',
    params,
  });

// 获取流程相关数据
export const ssiipMdmComponentPaperForm = (params?: SsiipMdmGraphPaperRegister | any) =>
  request.get<SsiipMdmGraphPaperRegister>({
    // url: adminPath + '/mdm/ssiipMdmComponentPaper/form',
    url: mdmPath + '/mdm/ssiipMdmGraphPaperRegister/form',
    params,
  });

export const ssiipMdmGraphPaperRegisterForm = (params?: SsiipMdmGraphPaperRegister | any) =>
  request.get<SsiipMdmGraphPaperRegister>({
    url: mdmPath + '/mdm/ssiipMdmGraphPaperRegister/form',
    params,
  });

// 保存
export const ssiipMdmGraphPaperRegisterSave = (
  params?: any,
  data?: SsiipMdmGraphPaperRegister | any,
) =>
  request.postJson<SsiipMdmGraphPaperRegister>({
    url: mdmPath + '/mdm/ssiipMdmGraphPaperRegister/save',
    params,
    data,
  });

// 删除
export const ssiipMdmGraphPaperRegisterDelete = (params?: SsiipMdmGraphPaperRegister | any) =>
  request.get<SsiipMdmGraphPaperRegister>({
    url: mdmPath + '/mdm/ssiipMdmGraphPaperRegister/delete',
    params,
  });

// 导出
export const ssiipMdmGraphPaperRegisterexportData = (params?: SsiipMdmGraphPaperRegister | any) =>
  request.post<any>({ url: mdmPath + '/mdm/ssiipMdmGraphPaperRegister/listExport', params });

// 删除
export const apiGetTaskNum = (params?: SsiipMdmGraphPaperRegister | any) =>
  request.get<SsiipMdmGraphPaperRegister>({
    url: mdmPath + '/mdm/ssiipMdmGraphPaperRegister/getTaskNum',
    params,
  });
// 文件上传
export const uploadFile = (params: any) =>
  request.uploadFile({ url: apiUrl + '/m/oss/fileUpload/single' }, params);

// 构件图纸列表查询http://localhost:8981/js/a/idGen/next
export const ssiipMdmGraphPaperListData = (params?: SsiipMdmGraphPaperRegister | any) =>
  request.post<Page<SsiipMdmGraphPaperRegister>>({
    url: mdmPath + '/mdm/ssiipMdmComponentPaper/pageList',
    params,
  });
export const ssiipMdmGraphPaperID = (params?: SsiipMdmGraphPaperRegister | any) =>
  request.post<Page<SsiipMdmGraphPaperRegister>>({
    url: adminPath + '/idGen/next',
    params,
  });
// 删除
export const paperDelete = (params?: SsiipMdmGraphPaperRegister | any) =>
  request.post<SsiipMdmGraphPaperRegister>({
    url: mdmPath + '/mdm/ssiipMdmComponentPaper/delete',
    params,
  });
//  负责人分页查询
export const ssiipMdmProjectLeaderListData = (params?: any | any) =>
  request.post<Page<any>>({
    url: mdmPath + '/mdm/projectHead/pageList',
    params,
  });
export const pdmGetWfNodeFieldConfig = (params?: SsiipMdmGraphPaperRegister | any) =>
  request.post<SsiipMdmGraphPaperRegister>({
    url: mdmPath + '/mdm/workFlowConfig/wfNodeFieldConfig',
    // params,
    data: params,
  });
/*
  表单-- 初始 数据配置信息
  @params
  wfKey: auxiliary_material
*/
export const pdmGetStartFieldConfig = (data?: SsiipMdmGraphPaperRegister | any) =>
  request.post<SsiipMdmGraphPaperRegister>({
    url: mdmPath + '/mdm/workFlowConfig/wfStartNodeConfig',
    data,
  });
export const pdmGetEndNodeConfig = (data?: SsiipMdmGraphPaperRegister | any) =>
  request.post<SsiipMdmGraphPaperRegister>({
    url: mdmPath + '/mdm/workFlowConfig/wfEndNodeConfig',
    data,
  });

//列表
export const ssiipMdmPaintInfoLedgerPageList = (params?: any) =>
  request.postJson({
    url: mdmPath + '/mdm/ssiipMdmPaintInfoLedger/pageList',
    params,
  });
