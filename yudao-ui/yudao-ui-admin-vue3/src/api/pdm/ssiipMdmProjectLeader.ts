/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author 李鹏
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { adminPath, mdmPath } = useGlobSetting();

// export interface SsiipMdmGraphPaperRegister extends BasicModel<SsiipMdmGraphPaperRegister> {
//   projectId?: string; // 项目编号
//   projectName?: string; // 项目名称
//   contractId?: string; // 工程任务ID
//   contractName?: string; // 工程任务名称
//   phaseId?: string; // 区域分部ID
//   phaseName?: string; // 区域分部名称
//   paperArrivalTime?: string; // 来图时间
//   paperPackageName?: string; // 图纸文件名
//   paperSharePath?: string; // 图纸共享盘路径
//   steelPurchasingPlan?: string; // 对应钢材采购计划
//   isOutsource?: string; // 外协双包任务
//   taskName?: string; // 任务单名称
//   taskNum?: string; // 任务编号
//   totalNetWeight?: number; // 来图净总重(kg)
//   totalGrossWeight?: number; // 来图毛总重(kg)
//   isPaperPrint?: string; // 是否有构件图下发车间
//   confirmationMode?: string; // 确认方式
//   paperSendTime?: string; // 发图时间
//   auxiliaryMaterial?: string; // 辅材提料需求
//   amChargerCode?: string; // 辅材负责人ID
//   amChargerName?: string; // 辅材负责人
//   abChargerCode?: string; // 锚杆负责人ID
//   abChargerName?: string; // 锚杆负责人
//   opChargerCode?: string; // 油漆负责人ID
//   opChargerName?: string; // 油漆负责人
// }

// export const ssiipMdmGraphPaperRegisterList = (params?: SsiipMdmGraphPaperRegister | any) =>
//   request.get<SsiipMdmGraphPaperRegister>({
//     url: adminPath + '/pdm/ssiipMdmGraphPaperRegister/list',
//     params,
//   });

// export const ssiipMdmGraphPaperRegisterForm = (params?: SsiipMdmGraphPaperRegister | any) =>
//   request.get<SsiipMdmGraphPaperRegister>({
//     url: adminPath + '/pdm/ssiipMdmGraphPaperRegister/form',
//     params,
//   });

// export const ssiipMdmGraphPaperRegisterSave = (
//   params?: any,
//   data?: SsiipMdmGraphPaperRegister | any,
// ) =>
//   request.postJson<SsiipMdmGraphPaperRegister>({
//     url: adminPath + '/pdm/ssiipMdmGraphPaperRegister/save',
//     params,
//     data,
//   });

// export const ssiipMdmGraphPaperRegisterDelete = (params?: SsiipMdmGraphPaperRegister | any) =>
//   request.get<SsiipMdmGraphPaperRegister>({
//     url: adminPath + '/pdm/ssiipMdmGraphPaperRegister/delete',
//     params,
//   });

//  分页查询
export const ssiipMdmProjectLeaderListData = (params?: any | any) =>
  request.post<Page<any>>({
    url: mdmPath + '/mdm/projectHead/pageList',
    params,
  });

//保存
export const ssiipMdmGraphPaperRegisterSaveData = (params?: any | any) =>
  request.post<Page<any>>({
    url: mdmPath + '/mdm/projectHead/save',
    params,
  });
//导出
export const projectHeadExport = (params?: any) =>
  request.post<any>({
    url: mdmPath + '/mdm/projectHead/export',
    params,
  });
