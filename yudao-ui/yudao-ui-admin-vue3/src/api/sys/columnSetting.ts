import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';

const { adminPath } = useGlobSetting();

// 保存列设置
export const saveColumn = (data) =>
  request.postJson({ url: adminPath + '/sys/formRecord/save', data });

// 获取列设置
export const getColumn = (params) =>
  request.get({ url: adminPath + '/sys/formRecord/getListFrom', params });
