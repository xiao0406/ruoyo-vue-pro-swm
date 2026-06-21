/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author 钟胜
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { adminPath, mdmPath } = useGlobSetting();

export interface SsiipMdmSteelMaterial extends BasicModel<SsiipMdmSteelMaterial> {
  projectId?: string; // 项目id
  projectName?: string; // 项目名称
  phase?: string; // 分部工程
  pickMaterialType?: string; // 摘料类型pick_material_type字典
  markType?: string; // 构件类型
  addReason?: string; // 增补原因
  projectNetWeight?: number; // 工程量净重(kg)
  projectGrossWeight?: number; // 工程量毛重(kg)
  netGrossPercent?: string; // 毛净重比
  lossReason?: string; // 超损原因
  requestCompleteDate?: string; // 要求完成时间
  purchaseType?: string; // 采购方purchase_type字典
  raiseMaterialType?: string; // 提料类型raise_material_type字典
  raiseMaterialWay?: string; // 提料方式raise_material_way字典
  materialShareFlag?: string; // 提料资料是否已放共享sys_yes_no字典
  materialStartFlag?: string; // 是否启动提料sys_yes_no字典
  raiseMaterialTime?: string; // 提料时间
  markProduceDate?: string; // 构件开始制造日期
  materialUsername?: string; // 提料任务分配姓名
  materialUserId?: string; // 提料任务分配id
  planWeight?: number; // 需用量(kg)
  planLossWeight?: number; // 理论扣减量(kg)
  actualWeight?: number; // 实际代用量(kg)
  ncSizingWeight?: number; // 定尺：业财提交量(kg)
  ncUnsizingWeight?: number; // 不定尺：业财提交量(kg)
  purchaseWeight?: number; // 采购净重(kg)
  purchaseLossPercent?: string; // 采购损耗
  raiseLossPercent?: string; // 提料损耗
  raiseDate?: string; // 日期时间
  purchaseAttachment?: string; // 采购附件
  paperMaterialAttachment?: string; // 纸板提料单
}

export const ssiipMdmSteelMaterialList = (params?: SsiipMdmSteelMaterial | any) =>
  request.get<SsiipMdmSteelMaterial>({
    url: adminPath + '/pdm/ssiipMdmSteelMaterial/list',
    params,
  });

export const ssiipMdmSteelMaterialListData = (params?: SsiipMdmSteelMaterial | any) =>
  request.post<Page<SsiipMdmSteelMaterial>>({
    url: mdmPath + '/mdm/ssiipMdmSteelMaterial/pageList',
    params,
  });

export const ssiipMdmSteelMaterialForm = (params?: SsiipMdmSteelMaterial | any) =>
  request.get<SsiipMdmSteelMaterial>({
    url: mdmPath + '/mdm/ssiipMdmSteelMaterial/form',
    params,
  });

export const ssiipMdmSteelMaterialSave = (params?: any, data?: SsiipMdmSteelMaterial | any) =>
  request.postJson<SsiipMdmSteelMaterial>({
    url: mdmPath + '/mdm/ssiipMdmSteelMaterial/save',
    params,
    data,
  });

export const ssiipMdmSteelMaterialDelete = (params?: SsiipMdmSteelMaterial | any) =>
  request.get<SsiipMdmSteelMaterial>({
    url: mdmPath + '/mdm/ssiipMdmSteelMaterial/delete',
    params,
  });

// 导出
export const exportData = (params?: SsiipMdmSteelMaterial | any) =>
  request.post<any>({ url: mdmPath + '/mdm/ssiipMdmSteelMaterial/exportData', params });
