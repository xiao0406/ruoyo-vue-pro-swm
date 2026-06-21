/**
 * @author Shawn
 * @date 2025-09-20
 */
import request from '@/config/axios';
import { Page } from '@/api/model/baseModel';

export interface ExternalCoordinateDataInfo {
  time: string;
  address: string;
  mapId: number | null;
  x: number | null;
  y: number | null;
  orgCd: string;
  timeStr: string;
  type: string;
  appId: string;
  warningId: number | null;
  originalX: number | null;
  originalY: number | null;
  scaleX: number | null;
  scaleY: number | null;
  nearestBeacon: string;
  usedBeacons: string;
  elderId: string;
  idCard: string;
  timeText: string;
}

export interface ExternalCoordinateDataQuery {
  elderId?: string | string[];
  idCard?: string;
  type?: string;
  mapId?: number;
  warningId?: number;
  startTime?: string;
  endTime?: string;
  sortOrder?: string;
  pageNo?: number;
  pageSize?: number;
  timeRange?: string[];
}

export function getExternalCoordinateDataList(params: ExternalCoordinateDataQuery) {
  return request.get<Page<ExternalCoordinateDataInfo>>({
    url: '/swm/externalCoordinateData/listData',
    params,
  });
}

export function exportExternalCoordinateData(params: ExternalCoordinateDataQuery) {
  return request.post({
    url: '/swm/externalCoordinateData/exportData',
    params,
    responseType: 'blob',
  });
}
