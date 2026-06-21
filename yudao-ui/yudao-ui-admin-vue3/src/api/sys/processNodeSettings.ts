import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';

const { adminPath } = useGlobSetting();

// 获流程节点
export const processNodeSettingsList = (params) =>
  request.get({ url: adminPath + '/ZjBpmConfig/pageList', params });

// 回显流程节点
export const processNodeSettingsDetail = (params) =>
  request.get({ url: adminPath + '/ZjBpmConfig/get', params });

// 修改流程节点
export const processNodeSettingsUpdate = (data) =>
  request.postJson({ url: adminPath + '/ZjBpmConfig/updateNodeStatus', data });

// 获取流程
export const processNodeSettingsFlow = (params) =>
  request.get({ url: adminPath + '/ZjBpmConfig/bpmFlowDict', params });

// 新增流程
export const processNodeSettingsAdd = (params) =>
  request.post({ url: adminPath + '/ZjBpmConfig/save', params });

// 删除流程
export const processNodeSettingsDelete = (params) =>
  request.get({ url: adminPath + '/ZjBpmConfig/delete', params });
