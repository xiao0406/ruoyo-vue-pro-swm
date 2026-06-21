/**
 * @author Shawn
 * @date 2025-09-20
 */
import request from '@/config/axios';
import { Page } from '@/api/model/baseModel';

/**
 * TDengine原始消息日志接口
 */
export interface RawMessageLogInfo {
  time: string;
  sessionId: string;
  messageTime: number;
  messageContent: string;
  originalLength: number;
  isTruncated: number;
  deviceId: string;
  timeText: string;
  isTruncatedText: string;
}

/**
 * 查询参数接口
 */
export interface RawMessageLogQuery {
  deviceId?: string;
  startTime?: string;
  endTime?: string;
  pageNo?: number;
  pageSize?: number;
}

/**
 * 分页查询原始消息日志列表
 */
export function getRawMessageLogList(params: RawMessageLogQuery) {
  return request.get<Page<RawMessageLogInfo>>({
    url: '/swm/swmRawMessageLog/listData',
    params,
  });
}

/**
 * 导出原始消息日志
 */
export function exportRawMessageLog(params: RawMessageLogQuery) {
  return request.post({
    url: '/swm/swmRawMessageLog/exportData',
    params,
    responseType: 'blob',
  });
}
