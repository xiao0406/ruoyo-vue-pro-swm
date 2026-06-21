import request from '@/config/axios';

/**
 * 获取统计数据
 */
export const getDataScreeOverview = () => {
  return request.get({ url: '/swm/dashboard/overview' });
};
/**
 * 获取近七天报警信息
 */
export const getWarningRecordsForPast7Days = (params: any) => {
  return request.get({ url: '/swm/dashboard/warningRecordsForPast7Days', params });
};
export const getWarningRecordsForPast7DaysNew = (params: any) => {
  return request.get({ url: '/swm/dashboard/warningRecordsForPast7DaysNew', params });
};
/**
 * 获取语音模板
 */
export const getFindStatusZero = () => {
  return request.get({ url: '/swm/swmVoiceTemplate/findStatusZero' });
};
/**
 * 确定召回
 */
export const swmOneClickRecall = (params: any) => {
  return request.post({
    url: '/iot/oneKeyRecall/broadcast/selected/post',
    params: {
      selectedTargets: JSON.stringify(params.selectedTargets),
      templateContent: params.templateContent,
      voiceText: params.voiceText,
      originalTreeData: JSON.stringify(params.originalTreeData || []),
    },
  });
};
/**
 * 单个身份证号召回接口
 */
export const broadcastidcard = (params: any) => {
  return request.post({
    url: '/iot/oneKeyRecall/broadcast/idcard',
    params: params,
  });
};
/**
 * IOT模块全体广播API函数
 */
export const sendBroadcastToAll = (templateContent: string, voiceText?: string) => {
  return request.post({
    url: '/iot/oneKeyRecall/broadcast/all',
    params: {
      templateContent: templateContent,
      voiceText: voiceText,
    },
  });
};
/**
 * 人员轨迹查询接口
 * @param { String } idCard 身份证Id
 * @param { String } startDateTime 开始时间
 * @param { String } endDateTime 结束时间
 * @param { String } personId 人员id
 */
export const getPersonTrajectoryByDateTime = (params: object) => {
  return request.get({
    url: '/swm/personTrack/getPersonTrajectoryByDateTime',
    params: params,
  });
};
/**
 * 人员轨迹区域
 * @param { String } idCard 身份证Id
 * @param { String } startDateTime 开始时间
 * @param { String } endDateTime 结束时间
 */
export const getAreaFenceDataByIdCardByDateTime = (params: object) => {
  return request.get({
    url: '/swm/personTrack/getAreaFenceDataByIdCardByDateTime',
    params: params,
  });
};

// 维构  人员轨迹列表
export const getMqttDevicePositionTrajectory = (params: object) => {
  return request.get({
    url: '/swm/personTrack/getMqttDevicePositionTrajectory',
    params: params,
  });
};
/**
 * 获取监控视频
 */
export const getOfficeDeviceList = (params: object) => {
  return request.get({
    url: '/swm/api/public/swm/monitorDeviceInfo/officeDeviceList',
    params: params,
  });
};
/**
 * 劳动力管理
     * 工种
    jobType?: string;
     * 组织编码
    officeCode?: string;
     * 车间ID
    positionArchiveId?: string;
     * 产线ID
    prodLineId?: string;
     * 班组ID
    workGroupId?: string;
 */
export const getdashboardData = (params: object) => {
  return request.get({
    url: '/swm/dashboard/attendance/dashboard',
    params: params,
  });
};
/**
 * @description 保存或新增区域
 * "id": "区域ID（编辑时传入）",
    "areaName": "区域名称",
    "areaType": "区域类型",
    "voicePrompt": "语音提示",
    "filePath": "文件路径",
    "beaconIds": ["beacon_id_1", "beacon_id_2", "beacon_id_3"],
    "beaconColor": "#FF0000",
    "isEdit": false
 */
export const saveAreaWithBeaconIds = (data: string) => {
  return request.postJson({
    url: '/swm/swmArea/saveAreaWithBeaconIds',
    params: data,
  });
};
/**
 * 获取规定设备，规定时间内的全部路径
 */
export const getUserAllPath = (deviceId: string, startTime: string, endTime: string) => {
  return request.get({
    url: `/iot/api/external-coordinate/query?deviceId=${deviceId}&startTime=${startTime}&endTime=${endTime}`,
  });
};
