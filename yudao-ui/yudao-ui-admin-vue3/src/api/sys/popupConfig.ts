import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';

const { adminPath } = useGlobSetting();

// 获取弹窗列表数据
export const codingPopConfigList = (params?: any) =>
  request.post({ url: adminPath + '/sys/popConfig/pageList', params });

// 删除列表数据
export const popupDeletList = (params?: any) =>
  request.post({ url: adminPath + '/sys/popConfig/delete', params });

// 编辑弹窗列表数据
export const popupEditList = (params?: any, data?: any) =>
  request.postJson({ url: adminPath + '/sys/popConfig/update', params, data });

// 新增数据
export const popupNewaddList = (params?: any, data?: any) =>
  request.postJson({ url: adminPath + '/sys/popConfig/save', params, data });

// 获取单个数据详情
export const popupListDetail = (params?: any, data?: any) =>
  request.post({ url: adminPath + '/sys/popConfig/get', params, data });
//
export const getfindByCode = (params?: any) => {
  return request.post({ url: adminPath + '/sys/popConfig/findByCode', params });
};
// 动态路径不带前缀
export const getRelect = (params, method: 'POST' | 'GET') => {
  switch (method) {
    case 'POST':
      return request.post({ url: adminPath.replace('/a', '') + params.url, params });
    case 'GET':
      return request.get({ url: adminPath.replace('/a', '') + params.url, params });
  }
};

// 动态路径不带前缀
export const printFile = (params?: any) =>
  request.postJson({
    url: adminPath.replace('/a', '') + '/r/report/groupDivideReport/generatePdf',
    data: params,
  });
