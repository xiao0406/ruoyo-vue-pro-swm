/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author 冼国文
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { Page, BasicModel } from '../model/baseModel';

const { fmsPath } = useGlobSetting();

// 工号详情
export interface NumberInfo extends BasicModel<NumberInfo> {
  finishCount: string; // 完成件数数量
  totalCount: string; // 总件数
  taskHandCount: string; // 在手任务量
  taskHandWeight: string; // 在手构件重量
  workCode: string; // 工号
}

// 工作总结
export interface WorkSummary {
  sumTaskHandCount: string; // 在手任务量
  sumTaskHandWeight: string; // 在手任务量重量
  sumTaskHandWork: string; // 在手任务量_工号数量
}

// 我的待办
export interface ToDoInfo extends BasicModel<ToDoInfo> {
  receiveUserName: string; // 接受者用户姓名
  sendUserName: string; // 发送者用户姓名
  sendDate: string; // 发送时间
  pushReturnContent: string; // 推送返回的内容信息
}

// 产量
export interface OutputInfo {
  percentageComplete: number; // 月完成率
  daylyProductionOutput: number; // 日产值
  monthlyProductionOutput: number; // 月产值
  monthOnMonth: number; // 月产值增长
}

// 产量
export interface ProductionValueInfo {
  percentageComplete: number; // 月完成率
  daylyProductionValue: number; // 日产值
  monthlyProductionValue: number; // 月产值
  monthOnMonth: number; // 月产值增长
}

export const showWorkDetail = (params: any) =>
  request.post<Page<NumberInfo>>({
    url: fmsPath + '/fms/fmsMyWorkDetail/showWorkDetail',
    params,
  });

export const showSumWorkDetail = () =>
  request.post<WorkSummary>({
    url: fmsPath + '/fms/fmsMyWorkDetail/showSumWorkDetail',
  });

export const unreadMsg = () =>
  request.post<ToDoInfo[]>({
    url: fmsPath + '/fms/fmsMyWorkDetail/unreadMsg',
  });

// 产量
export const outputStatistics = () =>
  request.post<OutputInfo>({
    url: fmsPath + '/fms/fmsMyWorkDetail/outputStatistics',
  });

// 产值
export const productionValueStatistics = () =>
  request.post<ProductionValueInfo>({
    url: fmsPath + '/fms/fmsMyWorkDetail/productionValueStatistics',
  });
