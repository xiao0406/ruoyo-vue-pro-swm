import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel } from '../model/baseModel';

const { adminPath } = useGlobSetting();

export interface codingRule extends BasicModel<codingRule> {
  id: string; // 主键
  isNewRecord: boolean;
  status?: string; // 状态（0启用，1禁用）
  moudleCode?: string; // 模块编码
  moudleName?: string; // 模块名称
  codeRule?: string; // 规则编码
  tableMain?: string; // 数据库表名称
}

// 获取列表数据
export const codingRuleList = (params?: codingRule | any) =>
  request.get({ url: adminPath + '/sys/rules/listData', params });

// 删除
export const codingRuleDelete = (params?: codingRule | any) =>
  request.delete({ url: adminPath + '/sys/rules/delete?id=' + params.id });

// 获取主表表名
export const codingRuleTableMain = (params?: codingRule | any) =>
  request.get({ url: adminPath + '/sys/rules/getTables', params });

// 新增
export const codingRuleSave = (params?: codingRule | any) =>
  request.post({ url: adminPath + '/sys/rules/save', params });

// 修改状态
export const codingRuleUpdateStatus = (params?: codingRule | any) =>
  request.get({ url: adminPath + '/sys/rules/deactivateOrEnable', params });

// 详情回显
export const codingRuleDetail = (params?: codingRule | any) =>
  request.get({ url: adminPath + '/sys/rules/form', params });
