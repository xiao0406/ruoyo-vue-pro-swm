import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
const { mdmPath } = useGlobSetting();
//技术营销数据
export const technicalMarketingList = (params?: any) =>
  request.post({
    url: mdmPath + '/mdm/ssiipMdmTechnicalMarketing/pageList',
    params,
  });
export const technicalMarketingExport = (params?: any) =>
  request.post<any>({
    url: mdmPath + '/mdm/ssiipMdmTechnicalMarketing/statementExport',
    params,
  });
// 项目工艺创效
export const projectWorkCreationList = (params?: any) =>
  request.post({
    url: mdmPath + '/mdm/project/projectWorkCreation/pageList',
    params,
  });

export const workCreationExport = (params?: any) =>
  request.post<any>({
    url: mdmPath + '/mdm/project/projectWorkCreation/statementExport',
    params,
  });
// 技术变更
export const technologyChangeList = (params?: any) =>
  request.post({
    url: mdmPath + '/mdm/technologyChange/pageList',
    params,
  });

export const technologyChangeExport = (params?: any) =>
  request.post<any>({
    url: mdmPath + '/mdm/technologyChange/statementExport',
    params,
  });
// 深化放样排版
export const projectLayoutList = (params?: any) =>
  request.post({
    url: mdmPath + '/mdm/ssiipMdmProjectLayout/pageList',
    params,
  });

export const layoutExport = (params?: any) =>
  request.post<any>({
    url: mdmPath + '/mdm/ssiipMdmProjectLayout/statementExport',
    params,
  });
