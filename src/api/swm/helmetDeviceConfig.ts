import request from '@/config/axios';

enum Api {
  saveConfig = '/swm/swmHelmetDeviceConfig/saveConfig',
}

/**
 * 保存或更新头盔设备配置
 * @param params 配置参数
 */
export const saveHelmetDeviceConfig = (params: any) => {
  return request.postJson({
    url: Api.saveConfig,
    data: params,
  });
};
