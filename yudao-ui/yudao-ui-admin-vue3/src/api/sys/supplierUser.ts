import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';

const { adminPath } = useGlobSetting();

// 获取供应商用户列表
export const getSupplierUserList = (params) =>
  request.get({ url: adminPath + '/sys/supplier/getUser', params });

// 删除
export const deleteSupplierUser = (params) =>
  request.delete({ url: adminPath + `/sys/supplier/delUser?userCode=${params.userCode}` });

// 保存、修改
export const saveOrUpdateSupplierUser = (data) =>
  request.postJson({ url: adminPath + '/sys/supplier/saveUser', data });

// 启用
export const enableSupplierUser = (params) =>
  request.get({ url: adminPath + '/sys/supplier/enableUser', params });

// 停用
export const disableSupplierUser = (params) =>
  request.get({ url: adminPath + '/sys/supplier/deactivateUser', params });

// 重置密码
export const resetSupplierUserPwd = (params) =>
  request.get({ url: adminPath + '/sys/supplier/resetPwd', params });

// 获取详情
export const getSupplierUserDetail = (params) =>
  request.get({ url: adminPath + '/sys/supplier/from', params });
