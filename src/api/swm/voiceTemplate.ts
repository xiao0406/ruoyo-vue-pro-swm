import request from '@/config/axios';

enum Api {
  GetVoiceTemplateList = '/swm/swmVoiceTemplate/listData',
  SaveVoiceTemplate = '/swm/swmVoiceTemplate/save',
  DeleteVoiceTemplate = '/swm/swmVoiceTemplate/delete',
  GetActiveVoiceTemplates = '/swm/swmVoiceTemplate/findStatusZero',
  UpdateStatus = '/swm/swmVoiceTemplate/updateStatus',
}

/**
 * 获取语音模板列表
 */
export const getVoiceTemplateList = (params?: any) => {
  return request.get({ url: Api.GetVoiceTemplateList, params });
};

/**
 * 获取状态为启用(0)的语音模板列表
 */
export const getActiveVoiceTemplates = () => {
  return request.get({ url: Api.GetActiveVoiceTemplates });
};

/**
 * 新增语音模板
 */
export const addVoiceTemplate = (data: any) => {
  return request.post({ url: Api.SaveVoiceTemplate, data });
};

/**
 * 更新语音模板
 */
export const updateVoiceTemplate = (data: any) => {
  return request.post({ url: Api.SaveVoiceTemplate, data });
};

/**
 * 更新模板状态
 */
export const updateTemplateStatus = (id: string, status: string) => {
  return request.post({ url: Api.UpdateStatus, params: { id, status } });
};

/**
 * 删除语音模板
 */
export const deleteVoiceTemplate = (params: { id: string }) => {
  return request.delete({ url: Api.DeleteVoiceTemplate + '?id=' + params.id });
};

/**
 * 语音模板数据类型
 */
export interface VoiceTemplateItem {
  label?: string;
  value?: string;
  id?: string;
  templateName?: string;
  templateCode?: string;
  content?: string;
  voiceText?: string;
  language?: string;
  pushMethod?: string;
  pushFrequency?: string;
  remarks?: string;
  status?: string;
  createDate?: string;
  updateDate?: string;
  createBy?: string;
  updateBy?: string;
  isNewRecord?: boolean;
}
