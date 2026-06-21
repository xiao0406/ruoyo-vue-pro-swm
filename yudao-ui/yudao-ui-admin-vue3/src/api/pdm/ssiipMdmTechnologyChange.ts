/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author wly
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { adminPath, apiUrl, ctxAdminPath, mdmPath } = useGlobSetting();

export interface SsiipMdmTechnologyChange extends BasicModel<SsiipMdmTechnologyChange> {
  planDate?: string; // 日期时间
  projectName?: string; // 项目名称
  projectId?: string; // 项目id
  phaseName?: string; // 分部名称
  phaseId?: string; // 分部id
  changeType?: string; // 变更类型（0 项目部变更）
  isClaimant?: string; // 是否索赔（0 是 1 否）
  changeMethod?: string; // 变更处理方式（0 变更核定单 1 更换加工图）
  setHeadCode?: string; // 放样负责人编码
  setHeadName?: string; // 放样负责人名称
  prodDeptCode?: string; // 生产部负责人编码
  prodDeptName?: string; // 生产部负责人名称
  businessCode?: string; // 商务负责人编码
  businessName?: string; // 商务负责人名称
  changeCheckFile?: string; // 变更核定单文件
  changeContent?: string; // 变更处理内容
  changeClaimantOver?: string; // 变更索赔概述
  planCopeHours?: string; // 方案处理工时
  newPartWeight?: number; // 新增零件重量
  scrapPartWeight?: number; // 报废零件重量
  usePartWeight?: number; // 再次利用零件重量
  setHandleHours?: string; // 放样处理耗时
  composeHandleHours?: string; // 排版处理耗时
  claimantMoney?: string; // 索赔费用概算
  claimantAckFile?: string; // 索赔确认函
  projectChangeFile?: string; // 项目变更确认函
  markEntityPicture?: string; // 构件实物图片
}

export const ssiipMdmTechnologyChangeList = (params?: SsiipMdmTechnologyChange | any) =>
  request.get<SsiipMdmTechnologyChange>({
    url: adminPath + '/ssiip/ssiipMdmTechnologyChange/list',
    params,
  });

// 流程from的接口
export const ssiipMdmTechnologyChangeForm = (params?: SsiipMdmTechnologyChange | any) =>
  request.get<SsiipMdmTechnologyChange>({
    url: mdmPath + '/mdm/technologyChange/form',
    params,
  });

export const ssiipMdmTechnologyChangeDelete = (params?: SsiipMdmTechnologyChange | any) =>
  request.get<SsiipMdmTechnologyChange>({
    url: adminPath + '/ssiip/ssiipMdmTechnologyChange/delete',
    params,
  });

// 文件上传
export const uploadFile = (params: any) =>
  request.uploadFile({ url: apiUrl + '/m/oss/fileUpload/single' }, params);

// 提交审批
export const fmsIntegrationPlanBApprovalRoam = (params: any, data: any) =>
  request.postJson<any>({
    url: mdmPath + '/mdm/technologyChange/save',
    params,
    data,
  });
// 提交审批 测试 ---
export const fmsIntegrationPlanBApprovalRoamTest = (params: any, data: any) =>
  request.postJson<any>({
    url: mdmPath + '/mdm/ssiip/ssiipMdmProjectLayout/save',
    params,
    data,
  });

// 查询
export const mdmTechnologyChangeGetSetHead = (params?: SsiipMdmTechnologyChange | any) =>
  request.get<SsiipMdmTechnologyChange>({
    url: mdmPath + '/mdm/technologyChange/getSetHead',
    params,
  });

export const ssiipMdmTechnologyChangeSave = (params?: any, data?: SsiipMdmTechnologyChange | any) =>
  request.postJson<SsiipMdmTechnologyChange>({
    url: mdmPath + '/mdm/technologyChange/save',
    params,
    data,
  });

// 分页
export const ssiipMdmTechnologyChangeListData = (params?: SsiipMdmTechnologyChange | any) =>
  request.post<Page<SsiipMdmTechnologyChange>>({
    url: mdmPath + '/mdm/technologyChange/pageList',
    params,
  });

// 导出
export const exportData = (params?: SsiipMdmTechnologyChange | any) =>
  request.post<any>({ url: mdmPath + '/mdm/technologyChange/exportData', params });

// 删除
export const ssiipMdmTechnologyDelete = (params?: any, data?: SsiipMdmTechnologyChange | any) =>
  request.postJson<SsiipMdmTechnologyChange>({
    url: mdmPath + '/mdm/technologyChange/delete',
    params,
    data,
  });

// 分页
export const ssipMdmChangeApplyChangeListData = (params?: SsiipMdmTechnologyChange | any) =>
  request.post<Page<SsiipMdmTechnologyChange>>({
    url: mdmPath + '/mdm/ssipMdmChangeApply/changeListData',
    params,
  });
