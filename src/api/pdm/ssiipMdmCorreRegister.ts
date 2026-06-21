/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author wxy
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { adminPath, mdmPath } = useGlobSetting();

export interface SsiipMdmCorreRegister extends BasicModel<SsiipMdmCorreRegister> {
  receiveDate?: string; // 接收日期
  correName?: string; // 函件名称
  projectCode?: string; // 项目编码
  projectId?: string; // 项目id
  projectName?: string; // 项目名称
  correNumber?: string; // 函件编号
  receiveDocNumber?: string; // 收文编号
  correContent?: string; // 函件内容
  opinion?: string; // 批示意见
  responsiblePersonCode?: string; // 责任人编码
  responsiblePersonName?: string; // 责任人名称
  outputItem?: string; // 处理销项
  fileUrl?: string; // 附件
  manufacture?: string; // 制造单位
}

export const ssiipMdmCorreRegisterList = (params?: SsiipMdmCorreRegister | any) =>
  request.get<SsiipMdmCorreRegister>({
    url: mdmPath + '/ssiip/ssiipMdmCorreRegister/list',
    params,
  });

export const ssiipMdmCorreRegisterListData = (params?: SsiipMdmCorreRegister | any) =>
  request.post<Page<SsiipMdmCorreRegister>>({
    url: mdmPath + '/ssiip/ssiipMdmCorreRegister/pageList',
    params,
  });

export const ssiipMdmCorreRegisterForm = (params?: SsiipMdmCorreRegister | any) =>
  request.get<SsiipMdmCorreRegister>({
    url: mdmPath + '/ssiip/ssiipMdmCorreRegister/form',
    params,
  });

export const ssiipMdmCorreRegisterSave = (params?: any, data?: SsiipMdmCorreRegister | any) =>
  request.postJson<SsiipMdmCorreRegister>({
    url: mdmPath + '/ssiip/ssiipMdmCorreRegister/save',
    params,
    data,
  });

export const ssiipMdmCorreRegisterDelete = (params?: SsiipMdmCorreRegister | any) =>
  request.get<SsiipMdmCorreRegister>({
    url: mdmPath + '/ssiip/ssiipMdmCorreRegister/delete',
    params,
  });
