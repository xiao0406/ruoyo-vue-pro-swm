/**
 * 人员追踪API接口
 * @author Shawn
 * @date 2025-01-14
 */
import request from '@/config/axios';

/**
 * 组织树节点接口
 */
export interface PersonTrackTreeNode {
  key: string;
  title: string;
  nodeType: 'factory' | 'workshop' | 'team' | 'person';
  children?: PersonTrackTreeNode[];
  // 人员相关信息（当nodeType为person时使用）
  personInfo?: {
    id: string; // 修改为string类型，因为数据库中ID是varchar类型
    name: string;
    workType: string;
    organization: string;
    workShop: string;
    teamGroup: string;
    workHours: string;
    attendanceStatus: string;
    idCard: string;
  };
}

/**
 * 人员位置信息接口
 */
export interface PersonPosition {
  x: number;
  y: number;
  id: string; // 修改为string类型，因为数据库中ID是varchar类型
  name: string;
  workType: string;
  organization: string;
  workShop: string;
  teamGroup: string;
  workHours: string;
  attendanceStatus: string;
  idCard: string;
  // 兼容后端返回的字段名
  identityCard?: string; // 后端实际返回的身份证字段
  attendance?: string; // 后端实际返回的考勤状态字段
  hasRealLocation?: boolean; // 是否有真实位置

  // 颜色信息字段 2025/06/24 Shawn 添加
  personTypeColor?: string; // 人员类型颜色
  workTypeColor?: string; // 工种颜色
  workerArchiveColor?: string; // 工人档案颜色
  officeCodeColor?: string; // 组织编码颜色
  positionArchiveColor?: string; // 车间颜色
  workGroupColor?: string; // 班组颜色
  prodLineColor?: string; // 产线颜色

  // ID字段 2025/06/24 Shawn 添加
  workerArchiveId?: string; // 工人档案ID
  officeCode?: string; // 组织编码
  positionArchiveId?: string; // 车间ID
  workGroupId?: string; // 班组ID
  prodLineId?: string; // 产线ID
}

/**
 * 底图信息接口
 */
export interface SiteMapInfo {
  id: string;
  mapName: string;
  projectId: string;
  filePath: string; // JSON字符串，包含文件上传的详细信息
  mapSize: string;
  scale: string;
  url: string; // 解析后的图片URL（通常是直接访问的URL）
}

/**
 * filePath字段解析后的结构
 */
export interface FilePathData {
  result: string;
  fileName: string;
  previewUrl: string; // 预览URL，如：fileUpload/preview?objectName=common/xxx.jpg
  message: string;
  url: string; // 直接访问URL，如：http://ip:port/swm/common/xxx.jpg
  fileId: string;
}

/**
 * 获取人员追踪组织树数据
 * @param displayType 展示类型：'按车间展示' | '按班组展示' | '按人员类型展示' | '按工种展示'
 * @returns 组织树节点数组
 */
export function getPersonTrackTree(displayType: string) {
  return request.get<{
    success: boolean;
    data: PersonTrackTreeNode[];
    message: string;
  }>({
    url: '/swm/personTrack/getOrgTree',
    params: {
      displayType,
    },
  });
}

/**
 * 获取人员位置数据
 * @param params 查询参数
 * @returns 人员位置数组
 */
export function getPersonPositions(params?: {
  organizationKey?: string;
  searchName?: string;
  displayType?: string;
  personTypeList?: string[];
}) {
  return request.get<{
    success: boolean;
    data: PersonPosition[];
    total: number;
    message: string;
  }>({
    url: '/swm/personTrack/getPersonPositions',
    params,
  });
}
export function getPersonPositionsNew(params) {
  return request.postJson({
    url: '/swm/personTrack/getPersonPositionsNew',
    params,
  });
}
// 维构 获取人员定位信息列表
export function getMqttPersonPositions(params) {
  return request.postJson({
    url: '/swm/personTrack/getMqttPersonPositions',
    params,
  });
}
/**
 * 搜索人员
 * @param name 人员姓名
 * @returns 人员位置数组
 */
export function searchPersonByName(name: string) {
  return request.get<{
    success: boolean;
    data: PersonPosition[];
    total: number;
    message: string;
  }>({
    url: '/swm/personTrack/searchPerson',
    params: { name },
  });
}

/**
 * 获取人员轨迹数据
 * @param params 查询参数（新增idCard必填参数）
 * @returns 轨迹数据
 */
export function getPersonTrajectory(params: {
  personId?: string; // 修改为string类型
  idCard: string;
  startDate?: string;
  endDate?: string;
  startTime?: number;
  endTime?: number;
}) {
  return request.get<{
    success: boolean;
    data: {
      trajectoryPoints: PersonPosition[];
      timelineEvents: Array<{
        time: string;
        name: string;
        content: string;
        type: 1 | 2 | 3; // 1-正常 2-报警 3-警告
      }>;
    };
    message: string;
  }>({
    url: '/swm/personTrack/getPersonTrajectory',
    params,
  });
}

/**
 * 根据身份证查询区域围栏数据
 * @param params 查询参数
 * @returns 区域围栏数据
 */
export function getAreaFenceDataByIdCard(params: {
  idCard: string;
  startDate?: string;
  endDate?: string;
  startTime?: number;
  endTime?: number;
}) {
  return request.get<{
    success: boolean;
    data: Array<{
      time: string;
      area_name: string;
    }>;
    deviceId: string;
    deviceIdLast8: string;
    total: number;
    message: string;
  }>({
    url: '/swm/personTrack/getAreaFenceDataByIdCard',
    params,
  });
}

/**
 * 根据身份证获取人员基本信息
 * @param identityCard 身份证号
 * @returns 人员基本信息
 */
export function getPersonByIdCard(identityCard: string) {
  return request.get<{
    success: boolean;
    data: {
      name: string;
      company: string;
      department: string;
      prodLine: string;
      team: string;
      identityCard: string;
    } | null;
    message: string;
  }>({
    url: '/swm/swmPerson/getActivePersonFromCache',
    params: { identityCard },
  });
}

/**
 * 获取启用的底图信息
 * @returns 启用的底图信息
 */
export function getEnabledSiteMap() {
  return request.get<{
    success: boolean;
    data: SiteMapInfo;
    message: string;
  }>({
    url: '/swm/swmSiteMapManagement/getEnabledMap',
  });
}

/**
 * 获取是否启动3d视图
 * @returns 获取是否启动3d视图
 */
export function getEnabledMap() {
  return request.get<{
    success: boolean;
    data: SiteMapInfo;
    message: string;
  }>({
    url: '/swm/swmSiteMapManagement/getEnabledMap',
  });
}
