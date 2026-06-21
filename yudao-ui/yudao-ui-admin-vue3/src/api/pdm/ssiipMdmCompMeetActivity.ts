/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author wxy
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { mdmPath, apiUrl } = useGlobSetting();

export interface SsiipMdmCompMeetActivity extends BasicModel<SsiipMdmCompMeetActivity> {
  activityType?: string; // 活动类型(字典表)
  host?: string; // 主持人
  theme?: string; // 会议主题
  meetContent?: string; // 会议内容简述
  person?: string; // 参与人员
  activityContent?: string; // 活动内容
  activityLocation?: string; // 活动地点
  dateTime?: string; // 日期时间
  meetLocation?: string; // 会议地点
  fileUrl?: string; // 附件url
  remark?: string; // 备注
}

export const ssiipMdmCompMeetActivityList = (params?: SsiipMdmCompMeetActivity | any) =>
  request.get<SsiipMdmCompMeetActivity>({
    url: mdmPath + '/mdm/ssiipMdmCompMeetActivity/list',
    params,
  });

export const ssiipMdmCompMeetActivityListData = (params?: SsiipMdmCompMeetActivity | any) =>
  request.post<Page<SsiipMdmCompMeetActivity>>({
    url: mdmPath + '/mdm/ssiipMdmCompMeetActivity/pageList',
    params,
  });

export const ssiipMdmCompMeetActivitySave = (params?: any, data?: SsiipMdmCompMeetActivity | any) =>
  request.postJson<SsiipMdmCompMeetActivity>({
    url: mdmPath + '/mdm/ssiipMdmCompMeetActivity/save',
    params,
    data,
  });

export const ssiipMdmCompMeetActivityDelete = (params?: SsiipMdmCompMeetActivity | any) =>
  request.get<SsiipMdmCompMeetActivity>({
    url: mdmPath + '/mdm/ssiipMdmCompMeetActivity/delete',
    params,
  });

export const ssiipMdmCompMeetActivityExport = (params?: SsiipMdmCompMeetActivity | any) =>
  request.post<SsiipMdmCompMeetActivity>({
    url: mdmPath + '/mdm/ssiipMdmCompMeetActivity/export',
    params,
  });
// 文件上传
export const uploadFile = (params: any) =>
  request.uploadFile({ url: apiUrl + '/m/oss/fileUpload/single' }, params);
