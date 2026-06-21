import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel } from '../model/baseModel';

const { adminPath } = useGlobSetting();

export interface bimfaceConfig extends BasicModel<bimfaceConfig> {
  id: string; // 主键
  isNewRecord: boolean;
  homeConfiguration?: string; //归属配置暂时固定为bim
  appKey?: string; //私有部署appkey
  appSecret?: string; //私有部appsecret
  key?: string; //正式环境key
  secret?: string; //正式环境secret
  apiHost?: string; //私有化地址
  fileHost?: string; //文件上传地址
  privatization?: '0' | '1'; //是否私有化(0是，1否）
  callback?: string; //回调地址
}

// 获取分页列表数据
export const getBimfaceList = (params?: bimfaceConfig | any) =>
  request.get({ url: adminPath + '/sys/bimConfiguration/listData', params });

// 新增配置
export const addBimface = (params?: bimfaceConfig | any) =>
  request.post<bimfaceConfig>({ url: adminPath + '/sys/bimConfiguration/sava', params });
