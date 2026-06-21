import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
const { mdmPath } = useGlobSetting();

// 获取项目类型
export const getProjectType = (params?: any) =>
  request.post({
    url: mdmPath + '/mdm/projectHead/pageList',
    params,
  });

//保存
export const addPaintInfo = (data: any, params?: any) =>
  request.postJson({
    url: mdmPath + '/mdm/ssiipMdmPaintInfoLedger/save',
    params,
    data,
  });

//列表
export const getTableList = (params?: any) =>
  request.postJson({
    url: mdmPath + '/mdm/ssiipMdmPaintInfoLedger/pageList',
    params,
  });

// 获取详情
export const getDetailsById = (params?: any) =>
  request.postJson({
    url: mdmPath + '/mdm/ssiipMdmPaintInfoLedger/detail',
    params,
  });

// 删除
export const delDataById = (params?: any) =>
  request.postJson({
    url: mdmPath + '/mdm/ssiipMdmPaintInfoLedger/delete',
    params,
  });
