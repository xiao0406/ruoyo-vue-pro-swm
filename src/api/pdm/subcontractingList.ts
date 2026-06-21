/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author wxy
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { mdmPath } = useGlobSetting();

export const ssiipMdmTechnologyPlanDetailSummarySheet = (params?: any | any) =>
  request.post<Page<any>>({
    url: mdmPath + '/mdm/ssiipMdmTechnologyPlanDetail/summarySheet',
    params,
  });

// 工艺分包表导出
export const summarySheetExport = (params?: any | any) =>
  request.post<any>({
    url: mdmPath + '/mdm/ssiipMdmTechnologyPlanDetail/summarySheetExport',
    params,
  });

// 辅材报量导出
export const auxiliaryExport = (params?: any | any) =>
  request.post<any>({
    url: mdmPath + '/mdm/ssiipMdmTechnologyPlanDetail/auxiliaryExport',
    params,
  });

// 工艺报量表导出
export const processMeasurementExport = (params?: any | any) =>
  request.post<any>({
    url: mdmPath + '/mdm/ssiipMdmTechnologyPlanDetail/processMeasurementExport',
    params,
  });

// 油漆报量导出
export const paintPurchaseExport = (params?: any | any) =>
  request.post<any>({
    url: mdmPath + '/mdm/ssiipMdmTechnologyPlanDetail/paintPurchaseExport',
    params,
  });
