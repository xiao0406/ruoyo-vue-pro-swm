import request from '@/config/axios';

const apiPrefix = '/swm/swmArea';

export interface AreaModel {
  id?: string;
  areaName?: string;
  areaType?: string;
  workShop?: string;
  voicePrompt?: string;
  filePath?: string;
  status?: string;
  remarks?: string;
}

export interface AreaParams {
  areaName?: string;
  areaType?: string;
  workShop?: string;
  status?: string;
}

/**
 * 获取全部区域列表(用于地图显示)
 *
 * @author Shawn
 * @date 2025-01-14
 */
export const getAllAreaList = (params?: AreaParams) =>
  request.get({ url: `${apiPrefix}/listAll`, params });

/**
 * 获取区域列表
 */
export const getAreaList = (params?: AreaParams) =>
  request.post({ url: `${apiPrefix}/listData`, params });

/**
 * 保存区域
 */
export const saveArea = (data: AreaModel) => request.postJson({ url: `${apiPrefix}/save`, data });

/**
 * 删除区域
 */
export const deleteArea = (id: string) =>
  request.post({ url: `${apiPrefix}/delete`, params: { id } });

/**
 * 保存区域并更新信标
 */
export const saveAreaWithBeacons = (data: any) =>
  request.postJson({ url: `${apiPrefix}/saveAreaWithBeacons`, data });

/**
 * 获取区域对应的信标坐标列表
 */
export const getAreaBeaconCoordinates = (areaId: string) =>
  request.get({ url: `${apiPrefix}/getAreaBeaconCoordinates`, params: { areaId } });

/**
 * 删除区域并清空相关信标
 *
 * @author Shawn
 * @date 2025-01-14
 */
export const deleteAreaWithBeacons = (id: string) =>
  request.postJson({ url: `${apiPrefix}/deleteAreaWithBeacons`, data: { id } });

/**
 * 检查信标坐标是否已被其他区域使用
 *
 * @author Shawn
 * @date 2025-01-14
 */
export const checkBeaconConflicts = (data: { coordinateList: string; currentAreaId?: string }) =>
  request.postJson({ url: `${apiPrefix}/checkBeaconConflicts`, data });

/**
 * 获取区域选项列表（用于下拉选择）
 *
 * @author Shawn
 * @date 2025-01-14
 */
export const getAreaOptions = () => request.get({ url: `${apiPrefix}/getAreaOptions` });

/**
 * 批量更新区域下所有信标的颜色
 *
 * @author Shawn
 * @date 2025-01-16
 */
export const updateAreaBeaconColors = (data: {
  areaId: string;
  beaconColor: string;
  remarks?: string;
}) => request.postJson({ url: `${apiPrefix}/updateAreaBeaconColors`, data });
