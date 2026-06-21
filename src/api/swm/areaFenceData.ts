import request from '@/config/axios';
import { Page } from '@/api/model/baseModel';

export interface AreaFenceDataInfo {
  time: string;
  x: number | null;
  y: number | null;
  areaName: string;
  areaId: string;
  remarks: string;
  areaType: string;
  deviceId: string;
  idCard: string;
  timeText: string;
}

export interface AreaFenceDataQuery {
  deviceId?: string | string[];
  idCard?: string | string[];
  startTime?: string;
  endTime?: string;
  sortOrder?: string;
  pageNo?: number;
  pageSize?: number;
  timeRange?: string[];
}

export function getAreaFenceDataList(params: AreaFenceDataQuery) {
  return request.get<Page<AreaFenceDataInfo>>({
    url: '/swm/areaFenceData/listData',
    params,
  });
}

export function exportAreaFenceData(params: AreaFenceDataQuery) {
  return request.post({
    url: '/swm/areaFenceData/exportData',
    params,
    responseType: 'blob',
  });
}
