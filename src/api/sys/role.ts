/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author ThinkGem
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { Page, TreeDataModel, BasicModel } from '../model/baseModel';

const { adminPath } = useGlobSetting();

export interface Role extends BasicModel<Role> {
  roleCode?: string; // 角色编码
  roleName?: string; // 角色名称
  roleType?: string; // 角色分类（高管、中层、基层、其它）
  roleSort?: number; // 角色排序（升序）
  isSys?: string; // 系统内置（1是 0否）
  userType?: string; // 用户类型（employee员工 member会员）
  dataScope?: string; // 数据范围设置（0未设置  1全部数据 2自定义数据）
  bizScope?: string; // 适应业务范围（不同的功能，不同的数据权限支持）
  extend?: any; // 扩展字段
  viewCode?: string; // 角色代码

  userCode?: string; // 根据用户编号查询授权的角色列表
}

export const roleList = (params?: Role | any) =>
  request.get<Role>({ url: adminPath + '/sys/role/list', params });

export const roleListData = (params?: Role | any) =>
  request.post<Page<Role>>({ url: adminPath + '/sys/role/listData', params });

export const roleTableList = (params?: Role | any) =>
  request.post<Page<Role>>({ url: adminPath + '/sys/roleSystem/listDataRole', params });

export const roleForm = (params?: Role | any) =>
  request.get<Role>({ url: adminPath + '/sys/role/form', params });

export const menuTreeData = (params?: any) =>
  request.get<Recordable>({ url: adminPath + '/sys/role/menuTreeData', params });

export const roleSave = (params?: any, data?: Role | any) =>
  request.postJson<Role>({ url: adminPath + '/sys/role/save', params, data });

export const checkRoleName = (oldRoleName: string, roleName: string) =>
  request.get<Role>({
    url: adminPath + '/sys/role/checkRoleName',
    params: { oldRoleName, roleName },
  });

export const roleDisable = (params?: Role | any) =>
  request.get<Role>({ url: adminPath + '/sys/role/disable', params });

export const roleEnable = (params?: Role | any) =>
  request.get<Role>({ url: adminPath + '/sys/role/enable', params });

export const roleDelete = (params?: Role | any) =>
  request.get<Role>({ url: adminPath + '/sys/role/delete', params });

export const formAuthDataScope = (params?: Role | any) =>
  request.get<Role>({ url: adminPath + '/sys/role/formAuthDataScope', params });

export const ctrlDataTreeData = (params?: any) => {
  const { url, ...params2 } = params;
  return request.get<Role>({ url: adminPath + url, params: params2 });
};

export const saveAuthDataScope = (params?: Role | any) =>
  request.post<Role>({ url: adminPath + '/sys/role/saveAuthDataScope', params });

export const roleTreeData = (params?: any) =>
  request.get<TreeDataModel[]>({ url: adminPath + '/sys/role/treeData', params });

export const sysTreeDict = (params?: Role | any) =>
  request.post<Role>({ url: adminPath + '/sys/layout/treeDictAll', params });

export const roleOfficePageList = (params?: Role | any) =>
  request.post<Role>({ url: adminPath + '/sys/roleOffice/pageList', params });

export const sysRoleList = (params?: Role | any) =>
  request.post<Role>({ url: adminPath + '/sys/roleSystem/sysList', params });

export const sysRoleListForm = (params?: Role | any) =>
  request.post<Role>({ url: adminPath + '/sys/roleOffice/form', params });

export const officeGetFactory = (params?: Role | any) =>
  request.post<Role>({ url: adminPath + '/sys/office/getFactory', params });

export const officeSave = (data?: Role | any) =>
  request.postJson<Role>({ url: adminPath + '/sys/roleOffice/save', data });

export const sysRoleListSave = (params?: Role | any) =>
  request.post<Role>({ url: adminPath + '/sys/roleSystem/save', params });

// 获取角色下的用户
export const roleUserList = (params?: Role | any) =>
  request.get<Role>({ url: adminPath + '/sys/sysUser/selectByRoleCode', params });
// request.get<Role>({ url: '/sys/sysUser/selectByRoleCode', params });

// 获取不属于角色下的用户
export const roleUserListNot = (params?: Role | any) =>
  request.get<Role>({ url: adminPath + '/sys/sysUser/selectNotRoleCode', params });

// 批量新增角色用户
export const roleUserSave = (params: any, data: Role | any) =>
  request.postJson<Role>({ url: adminPath + '/sys/sysUser/insertRoleAndUser', params, data });

// 批量删除角色用户
export const roleUserDelete = (params: any, data: Role | any) =>
  request.postJson<Role>({ url: adminPath + '/sys/sysUser/deleteRoleCode', params, data });

export const formAuthUser = (params?: Role | any) =>
  request.get<Role>({ url: adminPath + '/sys/role/formAuthUser', params });

export const saveAuthUser = (params?: Role | any) =>
  request.post<Role>({ url: adminPath + '/sys/role/saveAuthUser', params });

export const deleteAuthUser = (params?: Role | any) =>
  request.post<Role>({ url: adminPath + '/sys/role/deleteAuthUser', params });
