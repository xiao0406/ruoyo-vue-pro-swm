import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
const { mdmPath } = useGlobSetting();

export const auxiliaryReportList = (params?: any) =>
  request.post({
    url: mdmPath + '/mdm/ssiipMdmAuxiliaryMaterialPurchase/pageList',
    params,
  });
// 导出
export const auxiliaryReportExport = (params?: any) =>
  request.post<any>({
    url: mdmPath + '/mdm/ssiipMdmAuxiliaryMaterialPurchase/statementExport',
    params,
  });
