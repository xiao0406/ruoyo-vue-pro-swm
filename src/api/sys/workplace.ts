import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
// import { BasicModel, Page } from '../model/baseModel';

const { fmsPath } = useGlobSetting();

// 查询参数
export interface QueryParam {
  dimensionCode: string;
}

// 产量
export interface Yield {
  daylyOutput: number;
  monthOnMonth: number;
  monthlyOutput: number;
  percentageComplete: number;
}

// 产值
export interface Output {
  percentageComplete: number;
  daylyProductionValue: number;
  monthlyProductionValue: number;
  monthOnMonth: number;
}

// 构件类型

// 产量统计
export interface OutputInfo {
  id: string;
  showName: string;
  showValue: number;
  showValue1: number;
  width?: number;
}
export interface ProjectOutputInfo {
  dimensionCode: string;
  statisticalDimensions: OutputInfo[];
}
export interface ProjectOutput {
  barGraph: ProjectOutputInfo;
}

// 产量
export const fmsWorkplaceYield = () =>
  request.post<Yield>({ url: fmsPath + '/fms/homePage/outputStatistics' });

// 产值
export const fmsWorkplaceOutput = () =>
  request.post<Output>({ url: fmsPath + '/fms/homePage/productionValueStatistics' });

// 构建类型
export const fmsWorkplaceComponentType = (params: QueryParam) =>
  request.post<any>({ url: fmsPath + '/fms/homePage/componentTypeStatistics', params });

// 项目产量统计
export const fmsWorkplaceProjectOutput = (params: QueryParam) =>
  request.post<ProjectOutput>({
    url: fmsPath + '/fms/homePage/projectOutputStatistics',
    params,
  });

// 产量波动趋势
export const fmsWorkplaceFluctuation = (params: any) =>
  request.post<any>({ url: fmsPath + '/fms/homePage/trendOfYieldFluctuation', params });
