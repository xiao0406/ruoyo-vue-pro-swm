/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author wangning
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { fmsPath } = useGlobSetting();

export interface FmsProdLine extends BasicModel<FmsProdLine> {
  prodLineName?: string; // 产线名称
  prodLineCode?: string; // 产线编码
  headId?: string; // 负责人id
  headName?: string; // 负责人名称
  workShopId?: string; // 所属车间id
  workShopName?: string; // 所属车间
}
/**
 * 产线分页
 */
export const getPageList = (params?: FmsProdLine | any) =>
  request.get<FmsProdLine>({ url: '/swm/fmsProdLine/pageList', params });
/**
 * 产线新增
 */
export const addProdLine = (params?: FmsProdLine | any) =>
  request.post<Page<FmsProdLine>>({ url: '/swm/fmsProdLine/save', params });

/**
 * 根据名称生成编码接口
 */
export const getCode = (params?: FmsProdLine | any) =>
  request.get<Page<FmsProdLine>>({ url: '/swm/fmsProdLine/getCode', params });

/**
 * 车间分页
 */
export const getWorkShopPageList = (params?: FmsProdLine | any) =>
  request.get<FmsProdLine>({ url: '/swm/fmsPositionArchive/pageList', params });
/**
 * 车间新增
 */
export const addWorkShopProdLine = (params?: FmsProdLine | any) =>
  request.post<Page<FmsProdLine>>({ url: '/swm/fmsPositionArchive/save', params });

/**
 * 班组分页
 */
export const getWorkGroupPageList = (params?: FmsProdLine | any) =>
  request.get<FmsProdLine>({ url: '/swm/fmsWorkGroup/pageList', params });
/**
 * 班组新增
 */
export const addWorkGroup = (params?: FmsProdLine | any) =>
  request.post<Page<FmsProdLine>>({ url: '/swm/fmsWorkGroup/save', params });
