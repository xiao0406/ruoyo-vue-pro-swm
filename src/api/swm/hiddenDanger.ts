/**
 * 隐患信息API接口
 * @author Shawn
 * @date 2025-05-16
 */
import request from '@/config/axios';

// 接口前缀
const apiPrefix = '/swm/hiddenDanger';

/**
 * 隐患信息参数接口
 */
export interface HiddenDangerParams {
  dangerName?: string;
  location?: string;
  inspectionPlanId?: string;
  isBeaconDeployed?: string;
  isHandled?: string;
  status?: string;
  pageSize?: number;
  pageNo?: number;
}

/**
 * 隐患信息数据模型
 */
export interface HiddenDangerModel {
  id: string;
  dangerName: string;
  location: string;
  inspectionPlanId: string;
  inspectionPlanName?: string;
  isBeaconDeployed: string;
  isBeaconDeployedText: string;
  isHandled: string;
  isHandledText: string;
  status: string;
  statusText: string;
  createBy: string;
  createDate: string;
  updateBy: string;
  updateDate: string;
  remarks: string;
}

/**
 * ID参数接口
 */
export interface IdParams {
  id: string;
}

/**
 * 获取隐患信息列表
 */
export const getHiddenDangerList = (params?: HiddenDangerParams) =>
  request.post({ url: `${apiPrefix}/listData`, params });

/**
 * 获取单个隐患信息
 */
export const getHiddenDanger = (params: IdParams) =>
  request.get({ url: `${apiPrefix}/form`, params });

/**
 * 保存隐患信息
 */
export const saveHiddenDanger = (data: HiddenDangerModel) =>
  request.post({ url: `${apiPrefix}/save`, params: data });

/**
 * 删除隐患信息
 */
export const deleteHiddenDanger = (params: IdParams) =>
  request.post({ url: `${apiPrefix}/delete`, params });
