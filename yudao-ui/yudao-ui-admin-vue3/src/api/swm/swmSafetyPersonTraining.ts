import request from '@/config/axios';
import { BasicModel, Page } from '../model/baseModel';

export interface SwmSafetyPersonTraining extends BasicModel<SwmSafetyPersonTraining> {
  safetyManageId?: string; // swm_safety_manage主键id
  identityCard?: string; // 身份证号码
  phoneNumber?: string; // 手机号码
  completeStatus?: string; // 完成状态
  completeDate?: string; // 完成时间
  progress?: string; // 视频进度
}

export const swmSafetyPersonTrainingList = (params?: SwmSafetyPersonTraining | any) =>
  request.get<SwmSafetyPersonTraining>({ url: '/swm/swmSafetyPersonTraining/list', params });

export const swmSafetyPersonTrainingListData = (params?: SwmSafetyPersonTraining | any) =>
  request.post<Page<SwmSafetyPersonTraining>>({ url: '/swm/swmSafetyPersonTraining/pageList', params });

export const swmSafetyPersonTrainingForm = (params?: SwmSafetyPersonTraining | any) =>
  request.get<SwmSafetyPersonTraining>({ url: '/swm/swmSafetyPersonTraining/form', params });

export const swmSafetyPersonTrainingSave = (params?: any, data?: SwmSafetyPersonTraining | any) =>
  request.postJson<SwmSafetyPersonTraining>({ url: '/swm/swmSafetyPersonTraining/save', params, data });

export const swmSafetyPersonTrainingDelete = (params?: SwmSafetyPersonTraining | any) =>
  request.get<SwmSafetyPersonTraining>({ url: '/swm/swmSafetyPersonTraining/delete', params });
