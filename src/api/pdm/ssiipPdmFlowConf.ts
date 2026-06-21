import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { mdmPath } = useGlobSetting();

export interface ZjSysWfConf extends BasicModel<ZjSysWfConf> {
  wfKey?: string; // 工作流key
  wfName?: string; // 工作流名称
  bizKey?: string; // 业务key
  bizName?: string; // 业务名称
}

export interface ZjSysWfTaskConf extends BasicModel<ZjSysWfTaskConf> {
  mainId?: string; // 上级主键id
  wfNodeKey?: string; // 工作流节点key
  wfNodeName?: string; // 工作流节点name
  nodeConfJson?: string; // 工作流节点配置详情
}
export const zjSysWfConfList = (params?: ZjSysWfConf | any) =>
  request.get<ZjSysWfConf>({ url: mdmPath + '/mdm/zjSysWfConf/list', params });

export const zjSysWfConfListData = (params?: ZjSysWfConf | any) =>
  request.post<Page<ZjSysWfConf>>({ url: mdmPath + '/mdm/zjSysWfConf/pageList', params });

export const zjSysWfConfForm = (params?: ZjSysWfConf | any) =>
  request.get<ZjSysWfConf>({ url: mdmPath + '/mdm/zjSysWfConf/form', params });

export const zjSysWfConfSave = (params?: any, data?: ZjSysWfConf | any) =>
  request.postJson<ZjSysWfConf>({ url: mdmPath + '/mdm/zjSysWfConf/save', params, data });

export const zjSysWfConfDelete = (params?: ZjSysWfConf | any) =>
  request.get<ZjSysWfConf>({ url: mdmPath + '/mdm/zjSysWfConf/delete', params });

export const zjSysWfTaskConfList = (params?: ZjSysWfTaskConf | any) =>
  request.get<ZjSysWfTaskConf>({ url: mdmPath + '/mdm/zjSysWfTaskConf/list', params });

export const zjSysWfTaskConfListData = (params?: ZjSysWfTaskConf | any) =>
  request.post<Page<ZjSysWfTaskConf>>({ url: mdmPath + '/mdm/zjSysWfTaskConf/pageList', params });

export const zjSysWfTaskConfForm = (params?: ZjSysWfTaskConf | any) =>
  request.get<ZjSysWfTaskConf>({ url: mdmPath + '/mdm/zjSysWfTaskConf/form', params });

export const zjSysWfTaskConfSave = (params?: any) =>
  request.post<ZjSysWfTaskConf>({ url: mdmPath + '/mdm/zjSysWfTaskConf/save', params });

export const zjSysWfTaskConfDelete = (params?: ZjSysWfTaskConf | any) =>
  request.get<ZjSysWfTaskConf>({ url: mdmPath + '/mdm/zjSysWfTaskConf/delete', params });
/* 字典接口 */
export const pdmFlowWorkFlowDict = (params?: any, data?: any) =>
  request.post<any>({ url: mdmPath + '/mdm/workFlowConfig/workFlowDict', params, data });
export const pdmFlowEntityDict = (params?: any, data?: any) =>
  request.post<any>({ url: mdmPath + '/mdm/workFlowConfig/entityDict', params, data });

/* 流程节点 字典 */
export const pdmWfNodesDict = (params?: any, data?: any) =>
  request.post<any>({ url: mdmPath + '/mdm/workFlowConfig/wfNodes', params, data });

/* 流程业务字段 */
export const pdmWfNodesBizFields = (params?: any, data?: any) =>
  request.post<any>({ url: mdmPath + '/mdm/workFlowConfig/bizFields', params, data });
