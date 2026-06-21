/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author 李鹏
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { adminPath } = useGlobSetting();

export interface SsiipMdmDiluentPurchase extends BasicModel<SsiipMdmDiluentPurchase> {
  purchaseId?: string; // 采购主键
  diluentClass?: string; // 稀释剂种类
  diluentClassCode?: string; // 稀释剂种类编码
  diluent?: string; // 稀释剂种类
  diluentCode?: string; // 稀释剂种类编码
  specification?: string; // 规格（kg或L/组）
  purchasedAmount?: number; // 采购量（kg或L-国产10%；国外15%）
  totalPurchasingGroups?: number; // 总采购组数
  siteDemandGroups?: number; // 现场需求组数
}

export const ssiipMdmDiluentPurchaseList = (params?: SsiipMdmDiluentPurchase | any) =>
  request.get<SsiipMdmDiluentPurchase>({
    url: adminPath + '/mdm/ssiipMdmDiluentPurchase/list',
    params,
  });

export const ssiipMdmDiluentPurchaseListData = (params?: SsiipMdmDiluentPurchase | any) =>
  request.post<Page<SsiipMdmDiluentPurchase>>({
    url: adminPath + '/mdm/ssiipMdmDiluentPurchase/pageList',
    params,
  });

export const ssiipMdmDiluentPurchaseForm = (params?: SsiipMdmDiluentPurchase | any) =>
  request.get<SsiipMdmDiluentPurchase>({
    url: adminPath + '/mdm/ssiipMdmDiluentPurchase/form',
    params,
  });

export const ssiipMdmDiluentPurchaseSave = (params?: any, data?: SsiipMdmDiluentPurchase | any) =>
  request.postJson<SsiipMdmDiluentPurchase>({
    url: adminPath + '/mdm/ssiipMdmDiluentPurchase/save',
    params,
    data,
  });

export const ssiipMdmDiluentPurchaseDelete = (params?: SsiipMdmDiluentPurchase | any) =>
  request.get<SsiipMdmDiluentPurchase>({
    url: adminPath + '/mdm/ssiipMdmDiluentPurchase/delete',
    params,
  });
