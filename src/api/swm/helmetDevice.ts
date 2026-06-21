/**
 * @author Shawn
 */
import request from '@/config/axios';

// 接口前缀
const apiPrefix = '/swm/swmHelmetDevice';

// 获取安全帽设备列表（分页）
export const getHelmetDeviceList = (params?: any) =>
  request.get({ url: `${apiPrefix}/list`, params });

// 获取单个安全帽设备
export const getHelmetDevice = (id: string) =>
  request.get({ url: `${apiPrefix}/get`, params: { id } });

// 根据头盔编号获取设备
export const getByDeviceId = (deviceId: string) =>
  request.get({ url: `${apiPrefix}/getByDeviceId`, params: { deviceId } });

// 保存安全帽设备数据
export const saveHelmetDevice = (data: any) => request.postJson({ url: `${apiPrefix}/save`, data });

// 删除安全帽设备
export const deleteHelmetDevice = (id: string) =>
  request.post({ url: `${apiPrefix}/delete`, params: { id } });

// 保存设备参数配置
export const saveDeviceConfig = (data: any) =>
  request.postJson({ url: `${apiPrefix}/saveDeviceConfig`, data });

// 获取设备参数配置
export const getDeviceConfig = (deviceId: string) =>
  request.get({ url: `${apiPrefix}/getDeviceConfig`, params: { deviceId } });

// 发送协议到设备（IoT接口）
export const sendProtocolToDevice = (deviceId: string, message: string) =>
  request.postJson({
    url: `/iot/tcp/send/${deviceId}`,
    data: { message },
    timeout: 5000, // 5秒超时
  });

// 批量删除安全帽设备
export const deleteAllHelmetDevices = (ids: string) =>
  request.post({ url: `${apiPrefix}/deleteAll`, params: { ids } });

// 更新头盔电量
export const updateBattery = (deviceId: string, batteryLevel: number) =>
  request.post({
    url: `${apiPrefix}/updateBattery`,
    params: { deviceId, batteryLevel },
  });

// 绑定人员
export const assignPerson = (deviceId: string, personId: string, personName: string) =>
  request.post({
    url: `${apiPrefix}/assignPerson`,
    params: { deviceId, personId, personName },
  });

// 解绑人员
export const unassignPerson = (deviceId: string) =>
  request.post({
    url: `${apiPrefix}/unassignPerson`,
    params: { deviceId },
  });

// 查询可用的安全帽列表
export const findAvailableHelmets = (keyword?: string) =>
  request.get({
    url: `${apiPrefix}/findAvailableHelmets`,
    params: { keyword },
  });

// 批量提醒充电
export const batchChargeReminder = (deviceIds: string[], message?: string) =>
  request.postJson({
    url: `${apiPrefix}/batchChargeReminder`,
    data: { deviceIds, message },
  });

// 发送语音模板到设备 - 使用新的一键召回接口
export const sendVoiceToDevices = (deviceIds: string[], templateId = '1936227999566286848') => {
  // 使用新的批量设备号+模板ID召回接口
  return request.post({
    url: '/iot/oneKeyRecall/broadcast/devices/template',
    data: {
      deviceIds: JSON.stringify(deviceIds), // 设备号列表（JSON字符串格式）
      templateId: templateId, // 语音模板ID
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
  });
};

// 根据绑定人员查询设备
export const findHelmetsByAssignedPerson = (assignedPerson: string) => {
  return request.get({ url: `${apiPrefix}/findByAssignedPerson`, params: { assignedPerson } });
};

// 根据所属车间查询设备
export const findHelmetsByWorkshop = (assignedWorkshop: string) => {
  return request.get({ url: `${apiPrefix}/findByWorkshop`, params: { assignedWorkshop } });
};

// 根据所属工序查询设备
export const findHelmetsByProcess = (assignedProcess: string) => {
  return request.get({ url: `${apiPrefix}/findByProcess`, params: { assignedProcess } });
};

// 根据所属班组查询设备
export const findHelmetsByTeam = (assignedTeam: string) => {
  return request.get({ url: `${apiPrefix}/findByTeam`, params: { assignedTeam } });
};

// 获取安全帽使用记录
export const getHelmetUsageRecords = (deviceId: string) => {
  return request.get({ url: `${apiPrefix}/getHelmetUsageRecords`, params: { deviceId } });
};
