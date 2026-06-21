import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';

const { adminPath } = useGlobSetting();

// 获取系统更新日志列表
export const getSystemUpdateLogList = (params) =>
  request.get({ url: adminPath + '/sys/updateLog/pageList', params });

// 删除
export const deleteSystemUpdateLog = (params) =>
  request.delete({ url: adminPath + `/sys/updateLog/delete?id=${params.id}` });

// 保存
export const saveSystemUpdateLog = (params) =>
  request.post({
    url: adminPath + '/sys/updateLog/save',
    params,
  });

// 弹窗展示数据
export const readLogs = (params) =>
  request.get({
    url: adminPath + '/sys/updateLog/readLogs',
    params,
  });

// 读取
export const batchRead = (params) =>
  request.post({
    url: adminPath + '/sys/updateLog/batchRead',
    params,
  });

// 判断是否有新版本日志
export const unreadLogs = () =>
  request.get({
    url: adminPath + '/sys/updateLog/unreadLogs',
  });
