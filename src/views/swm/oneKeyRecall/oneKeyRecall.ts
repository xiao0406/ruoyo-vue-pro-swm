import { TreeItem } from '@/components/swm/Tree';

// 人员数据接口
export interface PersonInfo {
  id: string;
  name: string;
  personType?: string;
  gender?: string;
  company?: string;
  department?: string;
  workProcess?: string;
  team?: string;
  jobType?: string;
  safetyHelmetId?: string;
  safetyEducation?: string;
  identityCard?: string;
  phoneNumber?: string;
  status?: string;
  createBy?: string;
  createDate?: string;
  updateBy?: string;
  updateDate?: string;
  remarks?: string;
  personnelStatus?: string;
}

// 树节点接口
export interface OrgTreeNode extends TreeItem {
  id: string;
  name: string;
  key: string;
  title: string;
  children?: OrgTreeNode[];
}

// 公司节点
export type CompanyNode = OrgTreeNode;

// 部门节点
export type DepartmentNode = OrgTreeNode;

// 工序节点
export type ProcessNode = OrgTreeNode;

// 班组节点
export type TeamNode = OrgTreeNode;

// 人员节点
export interface PersonNode extends OrgTreeNode {
  jobType?: string;
  isLeaf: boolean;
}
