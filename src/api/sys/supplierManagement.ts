import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';

const { adminPath } = useGlobSetting();

// 获取供应商树列表
export const supplierTreeData = (params) =>
  request.get({ url: adminPath + '/sys/supplier/treeOfficeData', params });

// 获取供应商
export const supplierList = (params) =>
  request.get({ url: adminPath + '/sys/supplier/listOfficeData', params });

// 保存、修改
export const supplierSaveOrUpdate = (data) =>
  request.postJson({ url: adminPath + '/sys/supplier/saveOffice', data });

// 删除
export const supplierDelete = (params) =>
  request.delete({ url: adminPath + `/sys/supplier/delOffice?officeCode=${params.officeCode}` });

// 启用
export const supplierEnable = (params) =>
  request.get({ url: adminPath + '/sys/supplier/enableOffice', params });

// 停用
export const supplierDisable = (params) =>
  request.get({ url: adminPath + '/sys/supplier/deactivateOffice', params });
