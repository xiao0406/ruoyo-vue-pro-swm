import request from '@/config/axios';
import type { Page } from '@/api/model/baseModel';

export interface AlarmLightInfo {
  id: string;
  lightName: string;
  snCode: string;
  enableAlarm: string;
  enableAlarmText?: string;
  configCount?: number;
  alarmTypeNames?: string;
  createDate?: string;
  updateDate?: string;
  remarks?: string;
}

export interface AlarmLightConfigInfo {
  id: string;
  lightId: string;
  alarmConfigId: string;
  voiceTemplateId?: string;
  alarmName?: string;
  templateName?: string;
  remarks?: string;
}

export interface AlarmConfigInfo {
  id: string;
  alarmName: string;
  alarmKey: string;
  enableAlarm: string;
}

export interface VoiceTemplateInfo {
  id: string;
  templateName: string;
  templateCode: string;
  content: string;
}

// 报警灯管理API
export function getAlarmLightList(params?: any) {
  return request.get<Page<AlarmLightInfo>>({
    url: '/swm/swmAlarmLight/listData',
    params,
  });
}

export function getAlarmLightDetail(id: string) {
  return request.get<AlarmLightInfo>({
    url: '/swm/swmAlarmLight/form',
    params: { id },
  });
}

export function saveAlarmLight(params: AlarmLightInfo) {
  return request.post<string>({
    url: '/swm/swmAlarmLight/save',
    params,
  });
}

export function deleteAlarmLight(id: string) {
  return request.post<string>({
    url: '/swm/swmAlarmLight/delete',
    params: { id },
  });
}

// 报警灯配置管理API
export function getAlarmLightConfigList(lightId: string) {
  return request.get<{ list: AlarmLightConfigInfo[] }>({
    url: '/swm/swmAlarmLight/configList',
    params: { lightId },
  });
}

export function saveAlarmLightConfig(params: AlarmLightConfigInfo) {
  return request.post<string>({
    url: '/swm/swmAlarmLight/saveConfig',
    params,
  });
}

export function deleteAlarmLightConfig(id: string) {
  return request.post<string>({
    url: '/swm/swmAlarmLight/deleteConfig',
    params: { id },
  });
}

// 基础数据API - 报警类型
export function getAlarmConfigList(params?: any) {
  return request.get<Page<AlarmConfigInfo>>({
    url: '/swm/alarmConfig/list',
    params: { ...params, status: '0' },
  });
}

// 基础数据API - 语音模板
export function getVoiceTemplateList(params?: any) {
  return request.get<Page<VoiceTemplateInfo>>({
    url: '/swm/swmVoiceTemplate/listData',
    params: { ...params, status: '0' },
  });
}
