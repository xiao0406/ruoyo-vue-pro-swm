/**
 * 隐患处置API接口
 * @author Shawn
 * @date 2025-05-21
 */
import request from '@/config/axios';

enum Api {
  GET = '/swm/dangerDisposal/get',
  LIST = '/swm/dangerDisposal/list',
  SAVE = '/swm/dangerDisposal/save',
  DELETE = '/swm/dangerDisposal/delete',
  LIST_BY_HIDDEN_DANGER_ID = '/swm/dangerDisposal/listByHiddenDangerId',
}

/**
 * 隐患处置信息参数接口
 */
export interface DangerDisposalParams {
  hiddenDangerId?: string;
  dangerName?: string;
  location?: string;
  disposalTime?: string;
  disposer?: string;
  pageSize?: number;
  pageNo?: number;
}

/**
 * 隐患处置信息数据模型
 */
export interface DangerDisposalModel {
  id: string;
  hiddenDangerId: string;
  dangerName: string;
  location: string;
  disposalTime: string;
  disposalUser: string;
  disposalMethod: string;
  disposalStatus: string;
  disposalContent: string;
  attachment: string;
  createBy: string;
  createDate: string;
  updateBy: string;
  updateDate: string;
}

/**
 * 获取隐患处置信息
 */
export const getDangerDisposal = (params?: Recordable) => {
  return request.get({ url: Api.GET, params });
};

/**
 * 获取隐患处置信息列表
 */
export const getDangerDisposalList = (params?: Recordable) => {
  return request.get({ url: Api.LIST, params });
};

/**
 * 保存隐患处置信息
 */
export const saveDangerDisposal = (params?: Recordable) => {
  return request.post({ url: Api.SAVE, params });
};

/**
 * 删除隐患处置信息
 */
export const deleteDangerDisposal = (params?: Recordable) => {
  return request.post({ url: Api.DELETE, params });
};

/**
 * 根据隐患ID查询处置记录
 */
export const listByHiddenDangerId = (params?: Recordable) => {
  return request.get({ url: Api.LIST_BY_HIDDEN_DANGER_ID, params });
};
