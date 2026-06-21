/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author 李鹏
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { mdmPath, fmsPath, adminPath } = useGlobSetting();

export interface SsiipMdmAuxiliaryMaterialPurchase
  extends BasicModel<SsiipMdmAuxiliaryMaterialPurchase> {
  projectId?: string; // 项目编号
  projectName?: string; // 项目名称
  phaseName?: string; // 区域分部
  chiefEngineerCode?: string; // 项目总工ID
  chiefEngineerName?: string; // 项目总工
  purchaseDate?: string; // 提料日期
  procuringAgency?: string; // 采购方
  auxiliaryMaterialCategory?: string; // 辅材类别
  purchaseCategory?: string; // 采购类型
  isWorkshopSupplement?: string; // 是否为车间增补
  lossManagerCode?: string; // 损耗负责人ID
  lossManagerName?: string; // 损耗负责人
}

export const ssiipMdmAuxiliaryMaterialPurchaseList = (
  params?: SsiipMdmAuxiliaryMaterialPurchase | any,
) =>
  request.get<SsiipMdmAuxiliaryMaterialPurchase>({
    url: mdmPath + '/mdm/ssiipMdmAuxiliaryMaterialPurchase/list',
    params,
  });

export const ssiipMdmAuxiliaryMaterialPurchaseListData = (
  params?: SsiipMdmAuxiliaryMaterialPurchase | any,
) =>
  request.post<Page<SsiipMdmAuxiliaryMaterialPurchase>>({
    url: mdmPath + '/mdm/ssiipMdmAuxiliaryMaterialPurchase/pageList',
    params,
  });
export const ssiipMdmAuxiliaryMaterialPurchaseListDataExp = (
  params?: SsiipMdmAuxiliaryMaterialPurchase | any,
) =>
  request.post<Page<SsiipMdmAuxiliaryMaterialPurchase>>({
    url: mdmPath + '/mdm/ssiipMdmAuxiliaryMaterialPurchase/listExport',
    params,
  });

export const ssiipMdmAuxiliaryMaterialPurchaseForm = (
  params?: SsiipMdmAuxiliaryMaterialPurchase | any,
) =>
  request.get<SsiipMdmAuxiliaryMaterialPurchase>({
    url: mdmPath + '/mdm/ssiipMdmAuxiliaryMaterialPurchase/form',
    params,
  });

export const ssiipMdmAuxiliaryMaterialPurchaseSave = (
  params?: any,
  data?: SsiipMdmAuxiliaryMaterialPurchase | any,
) =>
  request.postJson<SsiipMdmAuxiliaryMaterialPurchase>({
    url: mdmPath + '/mdm/ssiipMdmAuxiliaryMaterialPurchase/save',
    params,
    data,
  });

export const ssiipMdmAuxiliaryMaterialPurchaseDelete = (
  params?: SsiipMdmAuxiliaryMaterialPurchase | any,
) =>
  request.get<SsiipMdmAuxiliaryMaterialPurchase>({
    url: mdmPath + '/mdm/ssiipMdmAuxiliaryMaterialPurchase/delete',
    params,
  });

/* 公用接口模块 级联接口 */
// 物料类别
export const pdmGetMaterialType = (params?: SsiipMdmAuxiliaryMaterialPurchase | any) =>
  request.get<SsiipMdmAuxiliaryMaterialPurchase>({
    url: mdmPath + '/mdm/etl/etlBdMarbasclass/getMaterialType',
    params,
  });
// 物料列表
export const pdmGetMaterielName = (params?: SsiipMdmAuxiliaryMaterialPurchase | any) =>
  request.get<SsiipMdmAuxiliaryMaterialPurchase>({
    url: mdmPath + '/mdm/etl/etlBdMarbasclass/getMaterielName',
    params,
  });
// 规格列表
export const pdmGetMaterielSpec = (params?: SsiipMdmAuxiliaryMaterialPurchase | any) =>
  request.get<SsiipMdmAuxiliaryMaterialPurchase>({
    url: mdmPath + '/mdm/etl/etlBdMarbasclass/getMaterielSpec',
    params,
  });
/* 物料材质 物料品牌   两个并行 */
// 物料材质
export const pdmGetMaterialTexture = (params?: SsiipMdmAuxiliaryMaterialPurchase | any) =>
  request.get<SsiipMdmAuxiliaryMaterialPurchase>({
    url: mdmPath + '/mdm/etl/etlBdMarbasclass/getMaterialTexture',
    params,
  });
// 物料品牌
export const pdmGetMaterialBrand = (params?: SsiipMdmAuxiliaryMaterialPurchase | any) =>
  request.get<SsiipMdmAuxiliaryMaterialPurchase>({
    url: mdmPath + '/mdm/etl/etlBdMarbasclass/getMaterialBrand',
    params,
  });
/*  
  表单-- 流程 数据配置信息
  @params
  procInsId: 1725426320221155328
     taskId: 1725428014609371136
*/
export const pdmGetWfNodeFieldConfig = (params?: SsiipMdmAuxiliaryMaterialPurchase | any) =>
  request.post<SsiipMdmAuxiliaryMaterialPurchase>({
    url: mdmPath + '/mdm/workFlowConfig/wfNodeFieldConfig',
    // params,
    data: params,
  });

export const pdmGetWfNodeFieldConfigBim = (params?: SsiipMdmAuxiliaryMaterialPurchase | any) =>
  request.post<SsiipMdmAuxiliaryMaterialPurchase>({
    url: mdmPath + '/mdm/workFlowConfig/wfNodeFieldConfigBim',
    // params,
    data: params,
  });
/*
  表单-- 初始 数据配置信息
  @params
  wfKey: auxiliary_material
*/
//头部
export const pdmGetStartFieldConfig = (data?: SsiipMdmAuxiliaryMaterialPurchase | any) =>
  request.post<SsiipMdmAuxiliaryMaterialPurchase>({
    url: mdmPath + '/mdm/workFlowConfig/wfStartNodeConfig',
    data,
  });

// 尾部
export const pdmGetwfEndNodeConfig = (data?: SsiipMdmAuxiliaryMaterialPurchase | any) =>
  request.post<SsiipMdmAuxiliaryMaterialPurchase>({
    url: mdmPath + '/mdm/workFlowConfig/wfEndNodeConfig',
    data,
  });

// 物料品牌
export const popUpNotificationGetEmployee = (params?: SsiipMdmAuxiliaryMaterialPurchase | any) =>
  request.get<SsiipMdmAuxiliaryMaterialPurchase>({
    url: adminPath + '/sys/popUpNotification/getEmployee',
    params,
  });

// 在详情获取流程bpm
export const dmsGetBpm = (params?: any) =>
  request.get<any>({ url: '/i/app/dmsDeviceFaultMaintenance/getBpm', params });

// 查询 编码接口
export const ssiipMdmTechnologyPlanDetailGetFileNumber = (
  data?: SsiipMdmAuxiliaryMaterialPurchase | any,
) =>
  request.post<SsiipMdmAuxiliaryMaterialPurchase>({
    url: mdmPath + '/mdm/ssiipMdmTechnologyPlanDetail/getFileNumber',
    data,
  });

export const getMaterialTree = (data?: SsiipMdmAuxiliaryMaterialPurchase | any) =>
  request.get<SsiipMdmAuxiliaryMaterialPurchase>({
    url: mdmPath + '/mdm/etl/etlBdMarbasclass/findPrincipalChildren',
    data,
  });
export const getMaterialList = (data?: SsiipMdmAuxiliaryMaterialPurchase | any) =>
  request.get<SsiipMdmAuxiliaryMaterialPurchase>({
    url: mdmPath + '/etl/etlBdMaterial/pkMarbasClassFindList',
    data,
  });
