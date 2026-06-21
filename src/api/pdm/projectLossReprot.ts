import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { mdmPath } = useGlobSetting();

export interface projectLossReprot extends BasicModel<projectLossReprot> {
  projectName?: string; // 项目名称
  lossProType?: string; // 项目类型(loss_pro_type 字典表)
  fullKg?: number; // 整板发料重量
  remainKg?: number; // 余料发料重量
  generateKg?: number; // 生成余量重量
  projectKg?: number; // 项目构件净重
  totalKg?: number; // 排版损耗:(整板发料重量+余量发料重量-生成余量重量-项目构件净重)/项目构件净重
  fileUrl?: string; // 文件url
  remark?: string; // 备注
}
export const projectLossReprotListData = (params?: projectLossReprot | any) =>
  request.post<Page<projectLossReprot>>({
    url: mdmPath + '/mdm/modules/projectLoss/projectLossReprot',
    params,
  });
//导出
export const projectLossReprotExcelExport = (params?: projectLossReprot | any) =>
  request.post<projectLossReprot>({
    url: mdmPath + '/mdm/modules/projectLoss/lossExcelExport',
    params,
  });
// 报表年度
export const projectLossAnnualReport = (params?: projectLossReprot | any) =>
  request.post<Page<projectLossReprot>>({
    url: mdmPath + '/mdm/modules/projectLoss/projectLossReprotYears',
    params,
  });
// 报表月度
export const projectLossMonthlyReport = (params?: projectLossReprot | any) =>
  request.post<Page<projectLossReprot>>({
    url: mdmPath + '/mdm/modules/projectLoss/projectLossReprotMonth',
    params,
  });

//
export const projectLossUseOfSurplusMaterial = (params?: projectLossReprot | any) =>
  request.post<Page<projectLossReprot>>({
    url: mdmPath + '/mdm/modules/projectLoss/useOfSurplusMaterial',
    params,
  });
