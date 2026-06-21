/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author 李鹏
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { adminPath } = useGlobSetting();

export interface SsiipMdmPaintPurchase extends BasicModel<SsiipMdmPaintPurchase> {
  purchaseId?: string; // 采购主键
  paintClass?: string; // 油漆类别
  paintClassCod?: string; // 油漆类别编码
  paintName?: string; // 油漆名称
  paintNameCode?: string; // 油漆名称编码
  paintSpecification?: string; // 油漆规格（kg或L/组）
  paintColour?: string; // 油漆颜色
  paintContentZinc?: number; // 油漆含锌量
  paintBrand?: string; // 油漆品牌
  paintBrandCode?: string; // 油漆品牌编码
  inventoryArea?: number; // 清单面积（m2）
  deductionArea?: number; // 扣减面积（m2）
  lossCoefficient?: number; // 损耗系数
  purchaseArea?: number; // 采购面积（m2）
  filmThickness?: number; // 漆膜厚度（um）
  theoreticalCoatingRate?: number; // 理论涂布率（100um）
  coatingRate?: number; // 涂布率（100um）
  paintDeductAmount?: number; // 扣减油漆量（L）
  purchasedAmount?: number; // 采购量（kg或L）
  totalPurchasingGroups?: number; // 总采购组数
  siteDemandGroups?: number; // 现场需求组数
}

export const ssiipMdmPaintPurchaseList = (params?: SsiipMdmPaintPurchase | any) =>
  request.get<SsiipMdmPaintPurchase>({
    url: adminPath + '/mdm/ssiipMdmPaintPurchase/list',
    params,
  });

export const ssiipMdmPaintPurchaseListData = (params?: SsiipMdmPaintPurchase | any) =>
  request.post<Page<SsiipMdmPaintPurchase>>({
    url: adminPath + '/mdm/ssiipMdmPaintPurchase/pageList',
    params,
  });

export const ssiipMdmPaintPurchaseForm = (params?: SsiipMdmPaintPurchase | any) =>
  request.get<SsiipMdmPaintPurchase>({
    url: adminPath + '/mdm/ssiipMdmPaintPurchase/form',
    params,
  });

export const ssiipMdmPaintPurchaseSave = (params?: any, data?: SsiipMdmPaintPurchase | any) =>
  request.postJson<SsiipMdmPaintPurchase>({
    url: adminPath + '/mdm/ssiipMdmPaintPurchase/save',
    params,
    data,
  });

export const ssiipMdmPaintPurchaseDelete = (params?: SsiipMdmPaintPurchase | any) =>
  request.get<SsiipMdmPaintPurchase>({
    url: adminPath + '/mdm/ssiipMdmPaintPurchase/delete',
    params,
  });
