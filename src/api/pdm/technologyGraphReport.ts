import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
const { mdmPath } = useGlobSetting();
//  项目来图量统计;
export const graphCount = (params?: any) =>
  request.post({
    url: mdmPath + '/mdm/technologyGraphReport/graphCount ',
    params,
  });
// 导出;
export const graphCountExport = (params?: any) =>
  request.post({
    url: mdmPath + '/mdm/technologyGraphReport/graphCountExport',
    params,
  });
