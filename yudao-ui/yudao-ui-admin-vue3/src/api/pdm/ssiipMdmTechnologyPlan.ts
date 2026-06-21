/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author 钟胜
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { adminPath, mdmPath } = useGlobSetting();

export interface SsiipMdmTechnologyPlan extends BasicModel<SsiipMdmTechnologyPlan> {
  paperId?: string; // 图纸登记id
  assemblyJigFlag?: number; // 是否为工装胎架(0否，1是)
  taskName?: string; // 任务单名称
  taskNum?: string; // 任务编号
  bigPieceTechnicianId?: string; // 大件工艺员id
  bigPieceTechnicianName?: string; // 大件工艺员姓名
  smallPieceTechnicianId?: string; // 小件工艺员id
  smallPieceTechnicianName?: string; // 小件工艺员姓名
  bigPieceTypesetterId?: string; // 大件排版员id
  bigPieceTypesetterName?: string; // 大件排版员姓名
  smallPieceTypesetterId?: string; // 小件排版员id
  smallPieceTypesetterName?: string; // 小件排版员姓名
  steelCenterFlag?: number; // 是否有钢板加工中心(0否，1是)
  settingOutShareFolder?: string; // 放样共享文件夹日期
  markParkSuffix?: string; // 零件号后缀
  actTaskId?: string; // 当前流程任务ID
  actTask?: string; // 当前流程任务
  actTaskHandler?: string; // 当前负责人
  ssiipMdmTechnologyPlanDetailList?: any[]; // 子表列表
}

export const ssiipMdmTechnologyPlanList = (params?: SsiipMdmTechnologyPlan | any) =>
  request.get<SsiipMdmTechnologyPlan>({
    url: adminPath + '/pdm/ssiipMdmTechnologyPlan/list',
    params,
  });

export const ssiipMdmTechnologyPlanListData = (params?: SsiipMdmTechnologyPlan | any) =>
  request.post<Page<SsiipMdmTechnologyPlan>>({
    url: mdmPath + '/mdm/ssiipMdmTechnologyPlan/pageList',
    params,
  });

export const ssiipMdmTechnologyPlanForm = (params?: SsiipMdmTechnologyPlan | any) =>
  request.get<SsiipMdmTechnologyPlan>({
    url: mdmPath + '/mdm/ssiipMdmTechnologyPlan/form',
    params,
  });

// 保存
export const ssiipMdmTechnologyPlanSave = (params?: any, data?: SsiipMdmTechnologyPlan | any) =>
  request.postJson<SsiipMdmTechnologyPlan>({
    url: mdmPath + '/mdm/ssiipMdmTechnologyPlan/save',
    params,
    data,
  });

export const ssiipMdmTechnologyPlanDelete = (params?: SsiipMdmTechnologyPlan | any) =>
  request.get<SsiipMdmTechnologyPlan>({
    url: mdmPath + '/mdm/ssiipMdmTechnologyPlan/delete',
    params,
  });

// 提交审批
export const fmsIntegrationPlanBApprovalRoam = (params: any, data: any) =>
  request.postJson<any>({
    url: mdmPath + '/mdm/ssiipMdmTechnologyPlan/save',
    params,
    data,
  });

// 导出
export const exportData = (params?: SsiipMdmTechnologyPlan | any) =>
  request.post<any>({ url: mdmPath + '/mdm/ssiipMdmTechnologyPlan/export', params });
