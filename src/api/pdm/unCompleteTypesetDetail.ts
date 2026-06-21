import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';

const { mdmPath } = useGlobSetting();
//未完成工艺放样任务
export const unCompleteTechnologyCount = (params?: any) =>
  request.post({
    url: mdmPath + '/mdm/technologyGraphReport/unCompleteTechnologyCount',
    params,
  });
// 未完成工艺排版任务;

export const unCompleteTypesetCount = (params?: any) =>
  request.post({
    url: mdmPath + '/mdm/technologyGraphReport/unCompleteTypesetCount',
    params,
  });
//未完成工艺任务明细

export const unCompleteTechnologyDetailData = (params?: any) =>
  request.post({
    url: mdmPath + '/mdm/technologyGraphReport/unCompleteTechnologyDetail',
    params,
  });
export const unCompleteTypesetDetailData = (params?: any) =>
  request.post({
    url: mdmPath + '/mdm/technologyGraphReport/unCompleteTypesetDetail',
    params,
  });
//未完成排版任务明细导出
export const unCompleteTypesetDetailExport = (params?: any) =>
  request.post({
    url: mdmPath + '/mdm/technologyGraphReport/unCompleteTypesetDetailExport ',
    params,
  });
//未完成工艺任务明细导出
export const unCompleteTechnologyDetailExport = (params?: any) =>
  request.post({
    url: mdmPath + '/mdm/technologyGraphReport/unCompleteTechnologyDetailExport',
    params,
  });
