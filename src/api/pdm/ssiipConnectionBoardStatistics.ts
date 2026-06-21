import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
const { mdmPath } = useGlobSetting();

//保存
export const addLinkClampInformation = (data: any, params?: any) =>
  request.postJson({
    url: mdmPath + '/mdm/SsiipMdmSplintInfo/save',
    params,
    data,
  });

//列表
export const getTableList = (params?: any) =>
  request.postJson({
    url: mdmPath + '/mdm/SsiipMdmSplintInfo/pageList',
    params,
  });

// 获取详情
export const getDetailsById = (params?: any) =>
  request.postJson({
    url: mdmPath + '/mdm/SsiipMdmSplintInfo/get',
    params,
  });

// 删除
export const delDataById = (params?: any) =>
  request.postJson({
    url: mdmPath + '/mdm/SsiipMdmSplintInfo/delete',
    params,
  });
