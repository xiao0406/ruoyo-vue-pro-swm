/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author 李鹏
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { mdmPath } = useGlobSetting();

export interface SsiipMdmCotterPurchase extends BasicModel<SsiipMdmCotterPurchase> {
  purchaseId?: string; // 采购主键
  materialClass?: string; // 物资类别
  materialClassCode?: string; // 物资类别编码
  materialName?: string; // 物资名称
  materialCode?: string; // 物资编码
  specification?: string; // 规格
  textureName?: string; // 材质
  textureCode?: string; // 材质编码
  brandName?: string; // 品牌
  brandCode?: string; // 品牌编码
  theoreticalQuantity?: number; // 理论数量
  deductionQuantity?: number; // 扣减数量
  recheckQuantity?: number; // 复检量
  ullage?: number; // 损耗
  actualPurchase?: number; // 实际采购数量
}

export const ssiipMdmCotterPurchaseList = (params?: SsiipMdmCotterPurchase | any) =>
  request.get<SsiipMdmCotterPurchase>({
    url: mdmPath + '/mdm/ssiipMdmCotterPurchase/list',
    params,
  });

export const ssiipMdmCotterPurchaseListData = (params?: SsiipMdmCotterPurchase | any) =>
  request.post<Page<SsiipMdmCotterPurchase>>({
    url: mdmPath + '/mdm/ssiipMdmCotterPurchase/pageList',
    params,
  });

export const ssiipMdmCotterPurchaseForm = (params?: SsiipMdmCotterPurchase | any) =>
  request.get<SsiipMdmCotterPurchase>({
    url: mdmPath + '/mdm/ssiipMdmCotterPurchase/form',
    params,
  });

export const ssiipMdmCotterPurchaseSave = (params?: any, data?: SsiipMdmCotterPurchase | any) =>
  request.postJson<SsiipMdmCotterPurchase>({
    url: mdmPath + '/mdm/ssiipMdmCotterPurchase/save',
    params,
    data,
  });

export const ssiipMdmCotterPurchaseDelete = (params?: SsiipMdmCotterPurchase | any) =>
  request.get<SsiipMdmCotterPurchase>({
    url: mdmPath + '/mdm/ssiipMdmCotterPurchase/delete',
    params,
  });
