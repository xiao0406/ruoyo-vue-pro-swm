/**
 * 危险源API接口
 * @author Shawn
 * @date 2025-05-20
 */
import request from '@/config/axios';

// 接口前缀
const apiPrefix = '/swm/swmHazardSource';

/**
 * 危险源参数接口
 */
export interface HazardSourceParams {
  hazardName?: string;
  location?: string;
  beaconIdentifier?: string;
  beaconTag?: string;
  isPatrolIncluded?: string;
  hazardStatus?: string;
  pageSize?: number;
  pageNo?: number;
}

/**
 * 危险源数据模型
 */
export interface HazardSourceModel {
  id: string;
  hazardName: string;
  hazardCategory: string;
  hazardCategoryText: string;
  location: string;
  beaconIdentifier: string;
  isPatrolIncluded: string;
  isPatrolIncludedText: string;
  patrolRecordSummary: string;
  registrationTime: string;
  hazardStatus: string;
  hazardStatusText: string;
  createBy: string;
  createDate: string;
  updateBy: string;
  updateDate: string;
  remarks: string;
  voiceTemplateId?: string;
  voiceTemplateText?: string;
  beaconTag?: string;
  isDraft?: string;
  personWhiteList?: string[]; // 前端使用的多选值
  filterIdentityCard?: string; // 提交给接口的身份证字段
  filterPersonnel?: string; // 提交给接口的人员名称字段
}

/**
 * ID参数接口
 */
export interface IdParams {
  id: string;
}

/**
 * 获取危险源列表
 */
export const getHazardSourceList = (params?: HazardSourceParams) =>
  request.post({ url: `${apiPrefix}/listData`, params });

/**
 * 获取单个危险源
 */
export const getHazardSource = (params: IdParams) =>
  request.get({ url: `${apiPrefix}/form`, params });

/**
 * 保存危险源
 */
export const saveHazardSource = (data: HazardSourceModel) =>
  request.post({ url: `${apiPrefix}/save`, params: data });

/**
 * 暂存危险源
 */
export const tempSaveHazardSource = (data: HazardSourceModel) =>
  request.post({
    url: `${apiPrefix}/tempSave`,
    data, // 使用data而不是params，确保数据作为JSON发送
    headers: {
      'Content-Type': 'application/json',
    },
  });

/**
 * 删除危险源
 */
export const deleteHazardSource = (params: IdParams) =>
  request.post({ url: `${apiPrefix}/delete`, params });

/**
 * 根据MAC地址查询危险源
 */
export const findHazardSourceByBeacon = (beaconIdentifier: string) =>
  request.get({ url: `${apiPrefix}/findByBeacon`, params: { beaconIdentifier } });

/**
 * 获取危险源巡检记录
 */
export const getHazardSourceInspectionRecords = (hazardSourceId: string) =>
  request.get({ url: `${apiPrefix}/inspectionRecords`, params: { hazardSourceId } });

/**
 * 获取未加入巡检的危险源列表
 */
export const getNotPatrolledHazardSourceList = () =>
  request.get({ url: `${apiPrefix}/notPatrolledList` });
