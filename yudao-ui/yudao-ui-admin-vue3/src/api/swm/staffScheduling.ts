/**
 * @author Swm
 * @date 2023-07-01
 * 人员排班相关接口
 */
import request from '@/config/axios';

/**
 * 人员信息接口
 */
export interface PersonInfo {
  id: string;
  name: string;
  identityCard: string; // 身份证号
}

/**
 * 根据节点类型和ID获取人员列表
 * @param nodeType 节点类型: office, workshop, prodLine, workGroup
 * @param id 节点ID
 */
export function getPersonsByNodeType(nodeType: string, id: string) {
  return request.get<{
    list: PersonInfo[];
    success: boolean;
  }>({
    url: '/swm/staffScheduling/getPersonsByNodeType',
    params: { nodeType, id },
  });
}

/**
 * 批量获取多个节点的人员列表
 * @param nodeTypes 节点类型数组 ['office', 'workshop', 'prodLine', 'workGroup']
 * @param ids 节点ID数组 ['id1', 'id2', 'id3']
 */
export function batchGetPersonsByNodeTypes(nodeTypes: string[], ids: string[]) {
  return request.get<{
    list: PersonInfo[];
    success: boolean;
  }>({
    url: '/swm/staffScheduling/batchGetPersonsByNodeTypes',
    params: {
      nodeTypes: nodeTypes.join(','),
      ids: ids.join(','),
    },
  });
}
