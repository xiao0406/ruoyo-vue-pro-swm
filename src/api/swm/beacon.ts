/**
 * @author Shawn
 */
import request from '@/config/axios';

// 接口前缀
const apiPrefix = '/swm/swmBeaconStation';

/**
 * 信标基站数据接口
 */
export interface BeaconStationParams {
  beaconId?: string;
  major?: string;
  minor?: string;
  deviceName?: string;
  beaconType?: string;
  controlType?: string;
  location?: string;
  mapCoord?: string;
  gpsCoord?: string;
  beaconStatus?: string;
  deployStatus?: string;
  pageSize?: number;
  pageNo?: number;
}

/**
 * 信标基站数据模型
 */
export interface BeaconStationModel {
  id: string;
  beaconId: string;
  major: string;
  minor: string;
  deviceName: string;
  beaconType: string;
  controlType: string;
  location: string;
  mapCoord: string;
  gpsCoord: string;
  beaconStatus: string;
  beaconStatusText: string;
  deployStatus: string;
  deployStatusText: string;
  createBy: string;
  createDate: string;
  updateBy: string;
  updateDate: string;
  remarks: string;
}

/**
 * 下拉框选项接口
 */
export interface SelectOption {
  value: string;
  label: string;
  location?: string;
}

/**
 * 获取信标基站列表
 */
export const getBeaconList = (params?: BeaconStationParams) =>
  request.post({ url: `${apiPrefix}/listData`, params });

/**
 * 获取全部信标基站列表(用于地图显示)
 *
 * @author Shawn
 * @date 2025-01-14
 */
export const getAllBeaconList = (params?: BeaconStationParams) =>
  request.get({ url: `${apiPrefix}/listAll`, params });

/**
 * 获取单个信标基站
 */
export const getBeacon = (id: string) => request.get({ url: `${apiPrefix}/form`, params: { id } });

/**
 * 根据MAC地址获取基站
 */
export const getBeaconByBeaconId = (beaconId: string) =>
  request.get({ url: `${apiPrefix}/getByBeaconId`, params: { beaconId } });

/**
 * 保存信标基站
 */
export const saveBeacon = (data: BeaconStationModel) =>
  request.postJson({ url: `${apiPrefix}/save`, data });

/**
 * 删除信标基站
 */
export const deleteBeacon = (id: string) =>
  request.post({ url: `${apiPrefix}/delete`, params: { id } });

/**
 * 批量删除信标基站
 */
export const batchDeleteBeacon = (ids: string) =>
  request.post({ url: `${apiPrefix}/deleteAll`, params: { ids } });

/**
 * 根据位置查询信标基站
 */
export const findBeaconByLocation = (location: string) =>
  request.get({ url: `${apiPrefix}/findByLocation`, params: { location } });

/**
 * 查询在线的信标基站
 */
export const findOnlineBeacons = () => request.get({ url: `${apiPrefix}/findOnlineBeacons` });

/**
 * 获取信标基站列表用于下拉框选择
 */
export const getBeaconSelectList = () =>
  request.get<SelectOption[]>({ url: `${apiPrefix}/listForSelect` });

/**
 * 获取危险源类型的信标基站列表用于下拉框选择
 *
 * @author Shawn
 * @date 2025-07-27
 */
export const getDangerousSourceBeaconSelectList = () =>
  request.get<SelectOption[]>({ url: `${apiPrefix}/listDangerousSourceForSelect` });

/**
 * 获取可用的危险源类型信标基站列表(排除已使用的)
 *
 * @author Shawn
 * @date 2025-07-27
 * @param excludeHazardSourceId 要排除的危险源ID（编辑时传入当前记录ID）
 */
export const getAvailableDangerousSourceBeaconSelectList = (excludeHazardSourceId?: string) =>
  request.get<SelectOption[]>({
    url: `${apiPrefix}/listAvailableDangerousSourceForSelect`,
    params: excludeHazardSourceId ? { excludeHazardSourceId } : {},
  });
/**
 * 获取区域人员准入名单,
 * @param  {String } areaId 区域id
 * @param  {String } personName 人员名称 模糊搜索
 */
export const getAreaAccessList = (data) =>
  request.get<SelectOption[]>({ url: `/swm/swmArea/getAreaAccessList`, params: data });
/**
 * 获取算法需要的信标格式
 */
export const getBeaconLocationForMap = () =>
  request.get<SelectOption[]>({ url: `/swm/swmBeaconStation/getBeaconLocationForMap` });
