/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author wxy
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { mdmPath, apiUrl } = useGlobSetting();

export interface SsiipMdmTechnicalMarketing extends BasicModel<SsiipMdmTechnicalMarketing> {
  marketType?: string; // 营销类型(字典)
  projectId?: string; // 项目id
  projectName?: string; // 项目名称(输入框)
  matters?: string; // 具体事宜
  startTime?: string; // 开始时间
  endTime?: string; // 结束时间
  duration?: number; // 持续时间
  fileUrl?: string; // 附件
  remark?: string; // 备注
}

export const ssiipMdmTechnicalMarketingList = (params?: SsiipMdmTechnicalMarketing | any) =>
  request.get<SsiipMdmTechnicalMarketing>({
    url: mdmPath + '/mdm/ssiipMdmTechnicalMarketing/list',
    params,
  });

export const ssiipMdmTechnicalMarketingListData = (params?: SsiipMdmTechnicalMarketing | any) =>
  request.post<Page<SsiipMdmTechnicalMarketing>>({
    url: mdmPath + '/mdm/ssiipMdmTechnicalMarketing/pageList',
    params,
  });

export const ssiipMdmTechnicalMarketingForm = (params?: SsiipMdmTechnicalMarketing | any) =>
  request.get<SsiipMdmTechnicalMarketing>({
    url: mdmPath + '/mdm/ssiipMdmTechnicalMarketing/form',
    params,
  });

export const ssiipMdmTechnicalMarketingSave = (
  params?: any,
  data?: SsiipMdmTechnicalMarketing | any,
) =>
  request.postJson<SsiipMdmTechnicalMarketing>({
    url: mdmPath + '/mdm/ssiipMdmTechnicalMarketing/save',
    params,
    data,
  });

export const ssiipMdmTechnicalMarketingDelete = (params?: SsiipMdmTechnicalMarketing | any) =>
  request.get<SsiipMdmTechnicalMarketing>({
    url: mdmPath + '/mdm/ssiipMdmTechnicalMarketing/delete',
    params,
  });
//导出
export const ssiipMdmTechnicalMarketingExport = (params?: SsiipMdmTechnicalMarketing | any) =>
  request.post<SsiipMdmTechnicalMarketing>({
    url: mdmPath + '/mdm/ssiipMdmTechnicalMarketing/export',
    params,
  });
// 文件上传
export const uploadFile = (params: any) =>
  request.uploadFile({ url: apiUrl + '/m/oss/fileUpload/single' }, params);
