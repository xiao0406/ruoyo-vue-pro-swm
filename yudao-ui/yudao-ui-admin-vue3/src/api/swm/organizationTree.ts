/**
 * 组织树数据接口
 * @author zwf
 * @date 2025-05-28
 */
import request from '@/config/axios';

/**
 * 组织树节点接口
 */
export interface OrgTreeNode {
  id: string;
  parentId: string;
  value: string;
  title: string;
  key: string;
  nodeType: 'office' | 'workshop' | 'prodLine' | 'workGroup' | 'worker';
  children?: OrgTreeNode[];
  isLeaf?: boolean;
  idCard?: string; // 身份证号码，仅worker节点有此字段
}

/**
 * 获取组织树数据
 * @param nodeType 节点类型: 'root'|'office'|'workshop'|'prodLine'|'workGroup'
 * @param parentId 父节点ID，如果nodeType为'root'则可不传
 * @returns 组织树节点数组
 */
export function fetchOrgTreeData(nodeType: string, parentId?: string) {
  return request.get<OrgTreeNode[]>({
    url: '/swm/organizationTree/getNodes',
    params: {
      nodeType,
      parentId,
    },
  });
}

export function organizationTreeGetNodes(params) {
  return request.get({
    url: '/swm/organizationTree/getNodes',
    params,
  });
}
