/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author 钟胜
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { mdmPath } = useGlobSetting();

export interface SsiipMdmTechnologyPlanDetail extends BasicModel<SsiipMdmTechnologyPlanDetail> {
  planId?: string; // 工艺计划id
  packageName?: string; // 分包名称
  packageWeight?: number; // 分包重量(kg)
  packagePieceType?: number; // 分包大小件(0小件，1大件）
  materialArrivePercent?: string; // 材料到场情况(%)
  manufacture?: string; // 制造单位
  manufactureName?: string; // 制造单位名称
  prodLineId?: string; // 工段id
  prodLineName?: string; // 工段名称
  allocateTime?: string; // 任务分配时间
  deadlineTime?: string; // 工期时间
  typesetTime?: string; // 要求排版图发放时间
  technicianId?: string; // 工艺员id
  technicianName?: string; // 工艺员姓名
  technologyPlanCompleteTime?: string; // 要求完成时间(工艺)
  settingOutWeight?: number; // 重量(kg)(放样)
  typesetWeight?: number; // 重量(kg)(排版)
  settingOutTypesetDiff?: number; // 排版与放样量差值
  markForm?: string; // 构件形式
  technologyActualCompleteTime?: string; // 工艺完成时间
  technologyActualCompleteMonth?: string; // 工艺完成月份
  typesetterId?: string; // 排版员id
  typesetterName?: string; // 排版员姓名
  typesetPlanCompleteTime?: string; // 要求完成时间(排版)
  typesetType?: string; // 排版类型
  lossCalcFlag?: number; // 是否参与损耗计算
  cuttingLength?: number; // 切割长度
  fullPageWeight?: number; // 整版发料种类(kg)
  surplusWeight?: number; // 余料发料重量(kg)
  wasteWeight?: number; // 生成余料重量(kg)
  wastePercent?: string; // 损耗(%)
  typesetActualCompleteTime?: string; // 排版完成时间
  typesetActualCompleteMonth?: string; // 排版完成月份
  otherRemark?: string; // 其他说明
  lackMaterialRemark?: string; // 缺料备注
  excessLossRemark?: string; // 备注(超损说明)
  graphProcWeightDiff?: number; // 吨位差量(来图量-工艺量)
  phaseId?: string; // 区域分部ID
  phaseName?: string; // 区域分部名称
  technologyNeed?: string; // 工艺需求
  loftsmanId?: string; // 放样员
  loftsmanName?: string; // 放样员
  loftsPlanDate?: string; // 放样计划时间
  loftsCompleteDate?: string; // 放样完成时间
  loftsCompleteMonth?: string; // 放样完成月份
  loftGroup?: string; // 放样组
  typesetActualOrder?: string; // 排版顺序
  typesetActualPlanDate?: string; // 排版计划时间
  missTonnage?: string; // 缺料吨位
  drawDistributionStatus?: string; // 图纸下发情况
  isGraphDeliver?: string; // 图纸是否下发
  statement?: string; // 特情说明
  manufacturSection?: string; // 制造工段
  made?: string; // 连接夹板是否制作
  fileNumber?: string; // 工艺文件编号
  recordDate?: string; // 登记日期
  recordMonth?: string; // 报量月份
  openSituation?: string; // 辅材开设情况
  cuttArea?: string; // 开料面积
  cuttDate?: string; // 开料日期
  paintArea?: string; // 现场油漆面积
}

export const ssiipMdmTechnologyPlanDetailList = (params?: SsiipMdmTechnologyPlanDetail | any) =>
  request.get<SsiipMdmTechnologyPlanDetail>({
    url: mdmPath + '/mdm/ssiipMdmTechnologyPlanDetail/list',
    params,
  });

export const ssiipMdmTechnologyPlanDetailListData = (params?: SsiipMdmTechnologyPlanDetail | any) =>
  request.post<Page<SsiipMdmTechnologyPlanDetail>>({
    url: mdmPath + '/mdm/ssiipMdmTechnologyPlanDetail/pageList',
    params,
  });

export const ssiipMdmTechnologyPlanDetailForm = (params?: SsiipMdmTechnologyPlanDetail | any) =>
  request.get<SsiipMdmTechnologyPlanDetail>({
    url: mdmPath + '/mdm/ssiipMdmTechnologyPlanDetail/form',
    params: params,
  });

export const ssiipMdmTechnologyPlanDetailSave = (params?: SsiipMdmTechnologyPlanDetail | any) =>
  request.postJson<SsiipMdmTechnologyPlanDetail>({
    url: mdmPath + '/mdm/ssiipMdmTechnologyPlanDetail/save',
    params,
  });

export const ssiipMdmTechnologyPlanDetailDelete = (params?: SsiipMdmTechnologyPlanDetail | any) =>
  request.get<SsiipMdmTechnologyPlanDetail>({
    url: mdmPath + '/mdm/ssiipMdmTechnologyPlanDetail/delete',
    params,
  });

//获取月份
export const ssiipMdmTechnologyPlanDetailGetMonth = (params?: string | any) =>
  request.post<SsiipMdmTechnologyPlanDetail>({
    url: mdmPath + '/mdm/ssiipMdmTechnologyPlanDetail/getMonth',
    params,
  });

export const ssiipMdmTechnologyPlanDetailsaveEntity = (params, data?: string | any) =>
  // request.post<SsiipMdmTechnologyPlanDetail>({
  request.postJson<SsiipMdmTechnologyPlanDetail>({
    url: mdmPath + '/mdm/ssiipMdmTechnologyPlanDetail/saveEntity',
    params,
    data,
  });

export const ssiipMdmTechnologyPlanDetailDisplayPermission = (
  params?: SsiipMdmTechnologyPlanDetail | any,
) =>
  request.post<SsiipMdmTechnologyPlanDetail>({
    url: mdmPath + '/mdm/ssiipMdmTechnologyPlanDetail/displayPermission',
    params,
  });

// 油漆信息导出
export const ssiipPaintInformationExport = (params?: SsiipMdmTechnologyPlanDetail | any) =>
  request.post<Page<SsiipMdmTechnologyPlanDetail>>({
    url: mdmPath + '/mdm/ssiipMdmTechnologyPlanDetail/paintInfoExport',
    params,
  });

export const ssiipAuxiliaryMaterialsExport = (params?: SsiipMdmTechnologyPlanDetail | any) =>
  request.post<Page<SsiipMdmTechnologyPlanDetail>>({
    url: mdmPath + '/mdm/ssiipMdmTechnologyPlanDetail/auxiliaryInfoExport',
    params,
  });

export const outboundApplicationFindDetailsList = (data?: SsiipMdmTechnologyPlanDetail | any) =>
  request.post<Page<SsiipMdmTechnologyPlanDetail>>({
    url: mdmPath + '/mdm/outboundApplication/findDetailsList',
    data,
  });

export const outboundApplicationFindList = (data?: SsiipMdmTechnologyPlanDetail | any) =>
  request.post<Page<SsiipMdmTechnologyPlanDetail>>({
    url: mdmPath + '/mdm/outboundApplication/findList',
    data,
  });

//列表
export const ssiipMdmPaintInfoLedgerPageList = (params?: any) =>
  request.postJson({
    url: mdmPath + '/mdm/ssiipMdmPaintInfoLedger/pageList',
    params,
  });
