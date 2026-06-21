import request from '@/config/axios';

enum Api {
  GetAlarmConfigList = '/swm/alarmConfig/list',
  GetAlarmConfig = '/swm/alarmConfig/get',
  SaveAlarmConfig = '/swm/alarmConfig/save',
  DeleteAlarmConfig = '/swm/alarmConfig/delete',
  SetAlarmConfig = '/swm/swmAlarmConfigDetail/save',
  GetConfigDetail = '/swm/swmAlarmConfigDetail/getByMainId',
}

/**
 * @description: 获取报警配置列表
 */
export const getAlarmConfigList = () => {
  return request.get({
    url: Api.GetAlarmConfigList,
  });
};

/**
 * @description: 获取单个报警配置
 */
export const getAlarmConfig = (id: string) => {
  return request.get({
    url: Api.GetAlarmConfig,
    params: { id },
  });
};

/**
 * @description: 保存报警配置（新增或更新）
 */
export const saveAlarmConfig = (data) => {
  return request.post({
    url: Api.SaveAlarmConfig,
    data,
  });
};

/**
 * @description: 删除报警配置
 */
export const deleteAlarmConfig = (id: string) => {
  return request.post({
    url: Api.DeleteAlarmConfig,
    params: { id },
  });
};
/**
 * @description: 保存推送配置
 */
export const setAlarmConfig = (data) => {
  return request.post({
    url: Api.SetAlarmConfig,
    data,
  });
};
/**
 * @description: 获取单个推送配置
 */
export const getConfigDetail = (id: string) => {
  return request.get({
    url: Api.GetConfigDetail,
    params: { mainId: id },
  });
};
