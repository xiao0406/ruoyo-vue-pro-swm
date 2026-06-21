/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author ThinkGem
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { TreeDataModel, TreeModel } from '../model/baseModel';

const { adminPath } = useGlobSetting();

export interface DictData extends TreeModel<DictData> {
  dictCode?: string; // 字典编码
  dictLabel?: string; // 字典标签
  dictValue?: string; // 字典键值
  dictIcon?: string; // 字典图标
  dictType?: string; // 字典类型
  isSys?: string; // 系统内置（1是 0否）
  description?: string; // 字典描述
  cssStyle?: string; // css样式（如：color:red)
  cssClass?: string; // css类名（如：red）
}

export interface DictDataTree extends TreeDataModel {
  icon?: string; // 字典图标
  cssStyle?: string; // css样式（如：color:red)
  cssClass?: string; // css类名（如：red）
}

export const dictDataList = (params?: DictData | any) =>
  request.get<DictData>({ url: '/swm/swmDictData/list', params });

export const dictDataListData = (params?: DictData | any) =>
  request.post<DictData[]>({ url: '/swm/swmDictData/listData', params });

export const dictDataForm = (params?: DictData | any) =>
  request.get<DictData>({ url: '/swm/swmDictData/form', params });

export const dictDataCreateNextNode = (params?: DictData | any) =>
  request.get<DictData>({ url: '/swm/swmDictData/createNextNode', params });

export const dictDataSave = (params?: any, data?: DictData | any) =>
  request.postJson<DictData>({ url: '/swm/swmDictData/save', params, data });

export const dictDataDelete = (params?: DictData | any) =>
  request.get<DictData>({ url: '/swm/swmDictData/delete', params });

export const dictDataTreeData = (params?: any) =>
  request.get<DictDataTree[]>({ url: '/swm/swmDictData/treeData', params });
