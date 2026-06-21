import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
const { mdmPath } = useGlobSetting();

export const technologyPlanList = (params?: any) =>
  request.post({
    url: mdmPath + '/mdm/technologyGraphReport/technologyPlanList',
    params,
  });
// 导出
export const exportData = (params?: any) =>
  request.post<any>({ url: mdmPath + '/mdm/ssiipMdmTechnologyPlan/export', params });
