/**
 * @author Shawn
 * @date 2025-10-02
 */
import request from '@/config/axios';
import { Page } from '@/api/model/baseModel';

/**
 * TDengine 下发指令到设备日志接口
 */
export interface TcpDeviceCommandLogInfo {
  time: string;
  sendMessage: string;
  sendStatus: string;
  errorMessage: string;
  identityCard: string;
  personName: string;
  deviceId: string;
  timeText: string;
}

/**
 * 查询参数接口
 */
export interface TcpDeviceCommandLogQuery {
  deviceId?: string;
  identityCard?: string;
  startTime?: string;
  endTime?: string;
  sortOrder?: string;
  pageNo?: number;
  pageSize?: number;
}

/**
 * 分页查询下发指令到设备日志列表
 */
export function getTcpDeviceCommandLogList(params: TcpDeviceCommandLogQuery) {
  return request.get<Page<TcpDeviceCommandLogInfo>>({
    url: '/swm/swmTcpDeviceCommandLog/listData',
    params,
  });
}

/**
 * 导出下发指令到设备日志
 */
export function exportTcpDeviceCommandLog(params: TcpDeviceCommandLogQuery) {
  return request.post({
    url: '/swm/swmTcpDeviceCommandLog/exportData',
    params,
    responseType: 'blob',
  });
}

/**
 * 向指定设备下发自定义TCP指令
 */
export function sendTcpDeviceCommand(deviceId: string, message: string) {
  return request.post(
    {
      url: `/iot/tcp/sendMessage/${deviceId}`,
      data: message,
      headers: {
        'Content-Type': 'text/plain;charset=UTF-8',
      },
    },
    { isTransformResponse: false },
  );
}
