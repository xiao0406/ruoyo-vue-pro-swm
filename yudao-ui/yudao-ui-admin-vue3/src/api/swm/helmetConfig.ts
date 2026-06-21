import request from '@/config/axios';

enum Api {
  ListData = '/swm/swmHelmetConfig/listData',
  GetDetail = '/swm/swmHelmetConfig/getDetail',
  Save = '/swm/swmHelmetConfig/save',
  Delete = '/swm/swmHelmetConfig/delete',
  FindSubitemsByParentId = '/swm/swmHelmetSubitem/findByParentId',
  SaveSubitem = '/swm/swmHelmetSubitem/save',
  BeaconListData = '/swm/swmBeaconColorConfig/listData',
  BeaconGetDetail = '/swm/swmBeaconColorConfig/getDetail',
  BeaconSave = '/swm/swmBeaconColorConfig/save',
  BeaconDelete = '/swm/swmBeaconColorConfig/delete',
  GetWorkshopData = '/swm/swmHelmetConfig/getWorkshopData',
  GetWorkshopLineGroupData = '/swm/swmHelmetConfig/getWorkshopLineGroupData',
  GetPersonTypeEnumData = '/swm/swmHelmetConfig/getPersonTypeEnumData',
  GetWorkTypeEnumData = '/swm/swmHelmetConfig/getWorkTypeEnumData',
}

/**
 * 获取安全帽颜色配置列表
 */
export function getHelmetConfigList(params?: any) {
  return request.get({ url: Api.ListData, params });
}

/**
 * 获取安全帽颜色配置详情
 */
export function getHelmetConfigDetail(params: { id: string }) {
  return request.get({ url: Api.GetDetail, params });
}

/**
 * 保存安全帽颜色配置
 */
export function saveHelmetConfig(data: any) {
  return request.post({ url: Api.Save, data });
}

/**
 * 删除安全帽颜色配置
 */
export function deleteHelmetConfig(params: { id: string }) {
  return request.delete({ url: Api.Delete + '?id=' + params.id });
}

/**
 * 获取安全帽子项列表
 */
export function getHelmetSubitemsByParentId(params: { parentId: string }) {
  return request.get({ url: Api.FindSubitemsByParentId, params });
}

/**
 * 保存安全帽子项
 */
export function saveHelmetSubitem(data: {
  id?: string;
  parentId: string;
  subitemName: string;
  color: string;
  key?: string;
}) {
  return request.post({ url: Api.SaveSubitem, data });
}

/**
 * 获取信标颜色配置列表
 */
export function getBeaconColorConfigList(params?: any) {
  return request.get({ url: Api.BeaconListData, params });
}

/**
 * 获取信标颜色配置详情
 */
export function getBeaconColorConfigDetail(params: { id: string }) {
  return request.get({ url: Api.BeaconGetDetail, params });
}

/**
 * 保存信标颜色配置
 */
export function saveBeaconColorConfig(data: any) {
  return request.post({ url: Api.BeaconSave, data });
}

/**
 * 获取车间数据
 */
export function getWorkshopData() {
  return request.get({ url: Api.GetWorkshopData });
}

/**
 * 获取车间-产线-班组数据
 *
 * @author Shawn
 * @date 2025/06/24
 * @description 获取车间-产线-班组数据，用于颜色配置
 */
export function getWorkshopLineGroupData() {
  return request.get({ url: Api.GetWorkshopLineGroupData });
}

/**
 * 获取人员类型字典数据
 *
 * @author Shawn
 * @date 2025/06/24
 * @description 获取字典类型为person_type_enum的字典数据，用于"按人员类型展示"的颜色配置
 */
export function getPersonTypeEnumData() {
  return request.get({ url: Api.GetPersonTypeEnumData });
}

/**
 * 获取工种数据
 *
 * @author Shawn
 * @date 2025/06/24
 * @description 获取 swm_work_type 表中的工种数据，用于"按工种展示"的颜色配置
 */
export function getWorkTypeEnumData() {
  return request.get({ url: Api.GetWorkTypeEnumData });
}

/**
 * 删除信标颜色配置
 */
export function deleteBeaconColorConfig(params: { id: string }) {
  return request.delete({ url: Api.BeaconDelete + '?id=' + params.id });
}
