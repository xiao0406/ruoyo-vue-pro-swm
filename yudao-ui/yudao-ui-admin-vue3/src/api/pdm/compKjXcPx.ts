/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author wxy
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { mdmPath, apiUrl } = useGlobSetting();

export interface CompKjXcPx extends BasicModel<CompKjXcPx> {
  compType?: string; // 类别(字典表)
  compKjType?: string; // 科技类别(字典表)
  compXcType?: string; // 宣传类别(字典表)
  author?: string; // 作者讲师
  fileUrl?: string; // 附件
  title?: string; // 内容名称
  remark?: string; // 备注
  content?: string; // 培训内容
  compDate?: string; // 日期时间
  location?: string; // 培训地点
  method?: string; // 培训方式
}

export const compKjXcPxList = (params?: CompKjXcPx | any) =>
  request.get<CompKjXcPx>({ url: mdmPath + '/mdm/modules/compKjXcPx/list', params });

export const compKjXcPxListData = (params?: CompKjXcPx | any) =>
  request.post<Page<CompKjXcPx>>({ url: mdmPath + '/mdm/modules/compKjXcPx/pageList', params });

export const compKjXcPxForm = (params?: CompKjXcPx | any) =>
  request.get<CompKjXcPx>({ url: mdmPath + '/mdm/modules/compKjXcPx/form', params });

export const compKjXcPxSave = (params?: any, data?: CompKjXcPx | any) =>
  request.postJson<CompKjXcPx>({ url: mdmPath + '/mdm/modules/compKjXcPx/save', params, data });

export const compKjXcPxDelete = (params?: CompKjXcPx | any) =>
  request.get<CompKjXcPx>({ url: mdmPath + '/mdm/modules/compKjXcPx/delete', params });
//导出
export const compKjXcPxExcelExport = (params?: CompKjXcPx | any) =>
  request.post<CompKjXcPx>({
    url: mdmPath + '/mdm/modules/compKjXcPx/compKjXcPxExcelExport',
    params,
  });
// 文件上传
export const uploadFile = (params: any) =>
  request.uploadFile({ url: apiUrl + '/m/oss/fileUpload/single' }, params);
