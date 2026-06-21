import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
const { mdmPath } = useGlobSetting();

export const steelReportList = (params?: any) =>
  request.post({
    url: mdmPath + '/mdm/technologyGraphReport/steelMaterialCount',
    params,
  });
// 导出
export const steelReportExport = (params?: any) =>
  request.post<any>({
    url: mdmPath + '/mdm/technologyGraphReport/steelMaterialCountExport',
    params,
  });
