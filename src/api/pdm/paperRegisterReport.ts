import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
const { mdmPath } = useGlobSetting();
//  项目来图量统计;
export const paperRegister = (params?: any) =>
  request.post({
    url: mdmPath + '/mdm/ssiipMdmGraphPaperRegister/pageList',
    params,
  });
// // 导出
// export const paperRegisterExport = (params?: any) =>
//   request.post<any>({ url: mdmPath + '/mdm/ssiipMdmGraphPaperRegister/listExport', params });
// 导出
export const paperRegisterExport = (params?: any) =>
  request.post<any>({
    url: mdmPath + '/mdm/ssiipMdmGraphPaperRegister/listExportStatement',
    params,
  });

export const ssiipMdmComponentPaperForm = (params?: any | any) =>
  request.get<any>({
    // url: adminPath + '/mdm/ssiipMdmComponentPaper/form',
    url: mdmPath + '/mdm/ssiipMdmGraphPaperRegister/form',
    params,
  });
