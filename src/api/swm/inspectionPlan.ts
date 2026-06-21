import request from '@/config/axios';

/**
 * 巡检计划API接口
 *
 * @author Shawn
 * @version 2025-05-21
 */

// 接口前缀
const apiPrefix = '/swm/swmInspectionPlan';

// 查询分页数据
export const getInspectionPlanListByPage = (params?: any) =>
  request.get<any>({ url: `${apiPrefix}/listData`, params });

// 获取单条数据
export const getInspectionPlan = (params?: any) =>
  request.get<any>({ url: `${apiPrefix}/get`, params });

// 获取巡检计划下拉选择数据
export const getInspectionPlanSelectList = (params?: any) =>
  request.get<any>({ url: `${apiPrefix}/selectData`, params });

// 保存数据
export const saveInspectionPlan = (params?: any) =>
  request.post<any>({ url: `${apiPrefix}/save`, params });

// 删除数据
export const deleteInspectionPlan = (params?: any) =>
  request.post<any>({ url: `${apiPrefix}/delete`, params });

// 开启巡检计划
export const openInspectionPlan = (params?: any) =>
  request.post<any>({ url: `${apiPrefix}/openPlan`, params });

// 暂停巡检计划
export const pauseInspectionPlan = (params?: any) =>
  request.post<any>({ url: `${apiPrefix}/pausePlan`, params });
