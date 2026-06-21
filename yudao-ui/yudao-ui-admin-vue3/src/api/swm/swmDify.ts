import request from '@/config/axios';
import { BasicModel, Page } from '../model/baseModel';

export interface SwmDify extends BasicModel<SwmDify> {
  date?: string; // 请求时间
  projectName?: string; // 项目名称
  safetyIndex?: number; // 安全合规指数
  workerIndex?: number; // 人员活动指数
  teamActualHours?: number; // 班组的有效作业时长
  text?: string; // 文本
  manufacture?: string; // 制造单位
  remarks?: string; // PDF file name
}

export const swmDifyList = (params?: SwmDify | any) =>
  request.get<SwmDify>({ url: '/swm/swmDify/list', params });

export const swmDifyListData = (params?: SwmDify | any) =>
  request.post<Page<SwmDify>>({ url: '/swm/swmDify/pageList', params });

export const swmDifyForm = (params?: SwmDify | any) =>
  request.get<SwmDify>({ url: '/swm/swmDify/form', params });

export const swmDifyDownloadReport = (params: { objectName: string }) =>
  request.get(
    {
      url: '/swm/fileUpload/preview',
      params,
      responseType: 'blob',
    },
    {
      isTransformResponse: false,
    },
  );

export const swmDifySave = (params?: any, data?: SwmDify | any) =>
  request.postJson<SwmDify>({ url: '/swm/swmDify/save', params, data });

export const swmDifyDelete = (params?: SwmDify | any) =>
  request.get<SwmDify>({ url: '/swm/swmDify/delete', params });
