/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author wxy
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { adminPath, mdmPath } = useGlobSetting();

export interface ProjectReplay extends BasicModel<ProjectReplay> {
  projectId?: string; // 项目id
  projectType?: string; // 项目类型(字典表)
  ndDescribe?: string; // 工艺难点描述
  solveMeasure?: string; // 工艺解决措施
  fileUrl?: string; // 附件
  summarizedBy?: string; // 总结人
  remark?: string; // 备注
}

export const projectReplayList = (params?: ProjectReplay | any) =>
  request.get<ProjectReplay>({ url: adminPath + '/modules/projectReplay/list', params });

export const projectReplayListData = (params?: ProjectReplay | any) =>
  request.post<Page<ProjectReplay>>({
    url: mdmPath + '/mdm/modules/projectReplay/pageList',
    params,
  });

export const projectReplayForm = (params?: ProjectReplay | any) =>
  request.get<ProjectReplay>({ url: adminPath + '/modules/projectReplay/form', params });

export const projectReplaySave = (params?: any, data?: ProjectReplay | any) =>
  request.postJson<ProjectReplay>({
    url: mdmPath + '/mdm/modules/projectReplay/save',
    params,
    data,
  });

export const projectReplayDelete = (params?: ProjectReplay | any) =>
  request.get<ProjectReplay>({ url: mdmPath + '/mdm/modules/projectReplay/delete', params });

// -------------------------------------------------------------------------------------------------

// 放量排放接口汇总

export const completeTechnologyCountListData = (params?: ProjectReplay | any) =>
  request.post<Page<ProjectReplay>>({
    url: mdmPath + '/mdm/technologyGraphReport/completeTechnologyCount',
    params,
  });

export const fmsExportData = (params?: any | any) =>
  request.get<any>({
    url: mdmPath + '/mdm/technologyGraphReport/completeTechnologyCountExport',
    params,
  });

//  -----
export const completeTypesetCountListData = (params?: ProjectReplay | any) =>
  request.post<Page<ProjectReplay>>({
    url: mdmPath + '/mdm/technologyGraphReport/completeTypesetCount',
    params,
  });
export const completeTypesetCountExport = (params?: any | any) =>
  request.get<any>({
    url: mdmPath + '/mdm/technologyGraphReport/completeTypesetCountExport',
    params,
  });

//  -----
export const completeTypesetCountByEmployeeListData = (params?: ProjectReplay | any) =>
  request.post<Page<ProjectReplay>>({
    url: mdmPath + '/mdm/technologyGraphReport/completeTypesetCountByEmployee',
    params,
  });

export const completeTypesetCountByEmployeeExportExport = (params?: any | any) =>
  request.post<any>({
    url: mdmPath + '/mdm/technologyGraphReport/lossCountExport',
    params,
  });

// export const completeTypesetCountByEmployeeExportExport = (params?: any | any) =>
//   request.get<any>({
//     url: mdmPath + '/mdm/technologyGraphReport/lossCountExport',
//     params,
//   });

export const completeTypesetCountByEmployeeGetMeterHead = (params?: any | any) =>
  request.get<any>({
    url: mdmPath + '/mdm/technologyGraphReport/completeTypesetCountByEmployeeHeader',
    params,
  });

//  -----
export const lossCountListData = (params?: ProjectReplay | any) =>
  request.post<Page<ProjectReplay>>({
    url: mdmPath + '/mdm/technologyGraphReport/lossCount',
    params,
  });

// -----
export const typesetAndTaskCountListData = (data?: ProjectReplay | any) =>
  request.post<Page<ProjectReplay>>({
    url: mdmPath + '/mdm/technologyGraphReport/typesetAndTaskCount',
    data,
  });

// ----油漆
export const paintReportDataList = (data?: ProjectReplay | any) =>
  request.post<Page<ProjectReplay>>({
    url: mdmPath + '/mdm/ssiipMdmAuxiliaryMaterialPurchase/paintReport',
    data,
  });

export const fmsExportpaintReportData = (params?: any | any) =>
  request.get<any>({
    url: mdmPath + '/mdm/ssiipMdmAuxiliaryMaterialPurchase/paintReportExport',
    params,
  });

// ----栓钉
export const cotterReportDataList = (data?: ProjectReplay | any) =>
  request.post<Page<ProjectReplay>>({
    url: mdmPath + '/mdm/ssiipMdmAuxiliaryMaterialPurchase/cotterReport',
    data,
  });

export const cotterReportExport = (params?: any | any) =>
  request.get<any>({
    url: mdmPath + '/mdm/ssiipMdmAuxiliaryMaterialPurchase/cotterReportExport',
    params,
  });

// ---复盘导出

export const DoubleQuotationExportData = (params?: any | any) =>
  request.get<any>({
    url: mdmPath + '/mdm/modules/projectReplay/projectReplayExcelExport',
    params,
  });
