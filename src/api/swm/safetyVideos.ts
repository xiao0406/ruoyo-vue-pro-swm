import request from '@/config/axios';

export interface SwmSafetyFileManage {
  coverUrl?: string; // 视频封面url地址
  title?: string; // 视频标题
  type?: string; // 视频分类
  jobType?: string; // 工种
  duration?: string; // 视频时长
  pushDate?: string; // 推送日期
  pushStatus?: string; // 推送状态
  fileUrl?: string; // 文件url地址
}

export const swmSafetyFileManageListData = (params) =>
  request.post({
    url: '/swm/swmSafetyFileManage/pageList',
    params,
  });

export const swmSafetyFileManageForm = (params) =>
  request.get({ url: '/swm/swmSafetyFileManage/form', params });

export const swmSafetyFileManageSave = (params) =>
  request.postJson({
    url: '/swm/swmSafetyFileManage/save',
    params,
  });

export const swmSafetyFileManageDelete = (params) =>
  request.get({ url: '/swm/swmSafetyFileManage/delete', params });

// 目标工种
export const swmSafetyFileManageJobTypeList = (params) =>
  request.post({
    url: '/swm/swmSafetyFileManage/jobTypeList',
    params,
  });
