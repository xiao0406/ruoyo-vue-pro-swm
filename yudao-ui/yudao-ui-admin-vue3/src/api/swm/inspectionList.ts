/**
 * 巡检列表API
 *
 * @author Shawn
 * @version 2025-05-22
 */
import request from '@/config/axios';

// 接口前缀
const apiPrefix = '/swm/swmInspectionList';

// 查询分页数据
export const getInspectionListByPage = (params?: any) =>
  request.get<any>({ url: `${apiPrefix}/listData`, params });

// 获取单条数据
export const getInspectionList = (params?: any) =>
  request.get<any>({ url: `${apiPrefix}/form`, params });

// 保存数据
export const saveInspectionList = (params?: any) =>
  request.post<any>({ url: `${apiPrefix}/save`, params });

// 删除数据
export const deleteInspectionList = (params?: any) =>
  request.post<any>({ url: `${apiPrefix}/delete`, params });

// 获取巡检附件列表
export const getInspectionFileList = (params?: any) =>
  request.get<any>({ url: `${apiPrefix}/fileList`, params });

// 开始巡检任务
export const startInspectionTask = (params?: any) =>
  request.postJson<any>({ url: `${apiPrefix}/startTask`, params });

// 完成巡检任务
export const completeInspectionTask = (params?: any) =>
  request.postJson<any>({ url: `${apiPrefix}/completeTask`, params });
