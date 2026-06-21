/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author wxy
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { mdmPath } = useGlobSetting();

export interface SsiipMdmCompLayoutQuantity extends BasicModel<SsiipMdmCompLayoutQuantity> {
  month?: string; // 月份
  monthNumber?: number; // 月排版量(吨)
  personNum?: number; // 当月人数
  effectNum?: number; // 人均工效(吨/人) :月排版量/当月人数
  remark?: string; // 备注
}

export const ssiipMdmCompLayoutQuantityList = (params?: SsiipMdmCompLayoutQuantity | any) =>
  request.get<SsiipMdmCompLayoutQuantity>({
    url: mdmPath + '/mdm/ssiipMdmCompLayoutQuantity/list',
    params,
  });

export const ssiipMdmCompLayoutQuantityListData = (params?: SsiipMdmCompLayoutQuantity | any) =>
  request.post<Page<SsiipMdmCompLayoutQuantity>>({
    url: mdmPath + '/mdm/ssiipMdmCompLayoutQuantity/pageList',
    params,
  });

export const ssiipMdmCompLayoutQuantityForm = (params?: SsiipMdmCompLayoutQuantity | any) =>
  request.get<SsiipMdmCompLayoutQuantity>({
    url: mdmPath + '/mdm/ssiipMdmCompLayoutQuantity/form',
    params,
  });

export const ssiipMdmCompLayoutQuantitySave = (
  params?: any,
  data?: SsiipMdmCompLayoutQuantity | any,
) =>
  request.postJson<SsiipMdmCompLayoutQuantity>({
    url: mdmPath + '/mdm/ssiipMdmCompLayoutQuantity/save',
    params,
    data,
  });

export const ssiipMdmCompLayoutQuantityDelete = (params?: SsiipMdmCompLayoutQuantity | any) =>
  request.get<SsiipMdmCompLayoutQuantity>({
    url: mdmPath + '/mdm/ssiipMdmCompLayoutQuantity/delete',
    params,
  });

//导出
export const ssiipMdmCompLayoutQuantityExport = (params?: SsiipMdmCompLayoutQuantity | any) =>
  request.post<SsiipMdmCompLayoutQuantity>({
    url: mdmPath + '/mdm/ssiipMdmCompLayoutQuantity/export',
    params,
  });
