/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author ThinkGem
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { Page, TreeDataModel } from '../model/baseModel';
import { User } from './user';
import { UploadApiResult } from './upload';
import { UploadFileParams } from '/#/axios';

export interface Leader {
  codes?: string;
}

const { apiUrl, adminPath } = useGlobSetting();

export interface EmpUser extends User {
  employee?: any;
}

export const empUserList = (params?: EmpUser | any) =>
  request.get<EmpUser>({ url: adminPath + '/sys/empUser/list', params });

export const empUserListData = (params?: EmpUser | any) =>
  request.post<Page<EmpUser>>({ url: adminPath + '/sys/empUser/listData', params });

export const empUserForm = (params?: EmpUser | any) =>
  request.get<EmpUser>({ url: adminPath + '/sys/empUser/form', params });

export const empUserSave = (params?: any, data?: EmpUser | any) =>
  request.postJson<EmpUser>({ url: adminPath + '/sys/empUser/save', params, data });

export const checkEmpNo = (oldEmpNo: string, empNo: string) =>
  request.get<EmpUser>({
    url: adminPath + '/sys/empUser/checkEmpNo',
    params: { oldEmpNo, 'employee.empNo': empNo },
  });

export const empUserImportData = (
  params: UploadFileParams,
  onUploadProgress: (progressEvent: ProgressEvent) => void,
) =>
  request.uploadFile<UploadApiResult>(
    {
      url: apiUrl + adminPath + '/sys/empUser/importData',
      onUploadProgress,
    },
    params,
  );
export const empUserDisable = (params?: EmpUser | any) =>
  request.get<EmpUser>({ url: adminPath + '/sys/empUser/disable', params });

export const empUserEnable = (params?: EmpUser | any) =>
  request.get<EmpUser>({ url: adminPath + '/sys/empUser/enable', params });

export const resetpwd = (params?: EmpUser | any) =>
  request.get<EmpUser>({ url: adminPath + '/sys/empUser/resetpwd', params });

export const empUserDelete = (params?: EmpUser | any) =>
  request.get<EmpUser>({ url: adminPath + '/sys/empUser/delete', params });

export const formAuthDataScope = (params?: EmpUser | any) =>
  request.get<EmpUser>({ url: adminPath + '/sys/empUser/formAuthDataScope', params });

export const ctrlDataTreeData = (params?: any) => {
  const { url, ...params2 } = params;
  return request.get<EmpUser>({ url: adminPath + url, params: params2 });
};

export const ctrlDataTreeDataMdm = (params?: any) => {
  const { url, ...params2 } = params;
  return request.get<EmpUser>({ url: adminPath + url, params: params2 });
};

export const saveAuthDataScope = (params?: EmpUser | any) =>
  request.post<EmpUser>({ url: adminPath + '/sys/empUser/saveAuthDataScope', params });

export const empUserTreeData = (params?: any) =>
  request.get<TreeDataModel[]>({ url: adminPath + '/sys/empUser/treeData', params });

// 负责人列表查询
export const leaderListData = (params?: Leader) =>
  request.post<Leader>({ url: adminPath + '/sys/empUser/listForDictData', params });
// 车间列表查询
export const officeWorkshopListData = (params?: any) =>
  request.post({ url: adminPath + '/sys/office/listOfficeWorkshop', params });
