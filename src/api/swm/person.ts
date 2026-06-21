/**
 * @author Shawn
 * @date 2025-05-13
 */
import request from '@/config/axios';
import { Page } from '@/api/model/baseModel';

/**
 * 人员信息接口
 */
export interface PersonInfo {
  id: string;
  name: string;
  personType: string;
  gender: string;
  company: string;
  department: string;
  prodLine: string; // 产线
  workProcess: string;
  team: string;
  jobType: string;
  safetyHelmetId: string;
  safetyEducation: string;
  identityCard?: string; // 身份证号码
  phoneNumber?: string; // 手机号码
  status: string;
  personnelStatus: string;
  helmetReturned?: string; // 是否归还安全帽
  departureType?: string; // 离职类型
  departureReason?: string; // 离职原因
  isExternalPersonnel?: string; // 是否厂内员工
  createBy: string;
  createDate: string;
  updateBy: string;
  updateDate: string;
  remarks: string;
}

/**
 * 分页查询人员列表
 */
export function getPersonList(params) {
  return request.get<Page<PersonInfo>>({
    url: '/swm/swmPerson/listData',
    params,
  });
}

// 导出
export function exportPersonnelList(params) {
  return request.get({
    url: '/swm/swmPerson/export',
    params,
  });
}

/**
 * 获取人员类型为1的人员列表
 */
export function getTypeOnePersonList() {
  return request.get<{
    list: PersonInfo[];
    success: boolean;
  }>({
    url: '/swm/swmPerson/listData',
    params: {
      personType: '1', // 查询personType为1的人员
      pageSize: 1000, // 设置较大的页面大小确保获取所有数据
    },
  });
}

/**
 * 获取单个人员信息
 */
export function getPerson(id: string) {
  return request.get<PersonInfo>({
    url: '/swm/swmPerson/form',
    params: { id },
  });
}

/**
 * 保存人员信息
 */
export function savePerson(params) {
  return request.post({
    url: '/swm/swmPerson/save',
    params,
  });
}

/**
 * 删除人员信息
 */
export function deletePerson(id: string) {
  return request.post({
    url: '/swm/swmPerson/delete',
    params: { id },
  });
}

/**
 * 批量删除人员信息
 */
export function batchDeletePerson(ids: string) {
  return request.post({
    url: '/swm/swmPerson/deleteAll',
    params: { ids },
  });
}

/**
 * 下载人员导入模板
 */
export function downloadPersonTemplate() {
  return request.get(
    {
      url: '/swm/swmPerson/importTemplate',
      responseType: 'blob',
    },
    { isReturnNativeResponse: true },
  );
}

/**
 * 下载人员导入模板（包含组织架构数据）
 */
export function downloadEnhancedPersonTemplate() {
  return request.get(
    {
      url: '/swm/swmPerson/importTemplateEnhanced',
      responseType: 'blob',
    },
    { isReturnNativeResponse: true },
  );
}

/**
 * 导入人员Excel
 */
export function importPersonExcel(params: FormData) {
  return request.post(
    {
      url: '/swm/swmPerson/importExcel',
      params,
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    },
    { isTransformResponse: false },
  );
}

/**
 * 导入人员Excel（支持组织架构验证）
 */
export function importPersonExcelEnhanced(params: FormData) {
  return request.post(
    {
      url: '/swm/swmPerson/importExcelEnhanced',
      params,
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    },
    { isTransformResponse: false },
  );
}

/**
 * 批量保存人员
 */
export function batchSavePerson(params) {
  return request.post({
    url: '/swm/swmPerson/batchSave',
    params,
  });
}

/**
 * 获取人员状态和安全教育枚举选项
 */
export function getPersonEnumOptions() {
  return request.get({
    url: '/swm/swmPerson/enumOptions',
  });
}

/**
 * 处理人员离职
 */
export function handlePersonDeparture(params) {
  return request.post({
    url: '/swm/swmPerson/handleDeparture',
    data: params,
    headers: {
      'Content-Type': 'application/json',
    },
  });
}

/**
 * 根据身份证查询离职人员
 */
export function findDepartedByIdentityCard(identityCard: string) {
  return request.get({
    url: '/swm/swmPerson/findDepartedByIdentityCard',
    params: { identityCard },
  });
}

/**
 * 获取工种列表
 */
export function getWorkTypeList() {
  return request.get({
    url: '/swm/swmWorkType/activeList',
  });
}

/**
 * 获取离职人员详情
 */
export function getDepartureDetail(id: string) {
  return request.get({
    url: '/swm/swmPerson/getDepartureDetail',
    params: { id },
  });
}

/**
 * 绑定安全帽
 */
export function bindSafetyHelmet(params: { personId: string; safetyHelmetId: string }) {
  // 由于SwmHelmetDevice实体类缺少@Table注解导致后台异常，改为直接更新人员信息
  return request.post({
    url: '/swm/swmPerson/save',
    params: {
      id: params.personId,
      safetyHelmetId: params.safetyHelmetId,
    },
  });
}

/**
 * 清除人员关联的安全帽
 * 用于离职处理过程中，当选择"已归还安全帽"时调用
 */
export function clearSafetyHelmet(id: string) {
  return request.post({
    url: '/swm/swmPerson/clearSafetyHelmet',
    params: { id },
  });
}

/**
 * 获取所有人员数据
 */
export function getAllPersons() {
  return request.get<{
    total: number;
    data: PersonInfo[];
    success: boolean;
  }>({
    url: '/swm/swmVoiceTemplate/getAllPersons',
  });
}

/**
 * 创建安全帽订购记录
 */
export function createSafetyHelmetOrder(params: any) {
  return request.post({
    url: '/swm/safetyHelmetOrder/save',
    params,
  });
}

/**
 * 检查身份证号码是否已存在
 */
export function checkIdentityCard(identityCard: string, excludeId?: string) {
  return request.get<{
    success: boolean;
    exists: boolean;
    existingPerson?: string;
    hasInactivePerson?: boolean;
    inactivePerson?: string;
    message: string;
  }>({
    url: '/swm/swmPerson/checkIdentityCard',
    params: { identityCard, excludeId },
  });
}

/**
 * 获取全部在职的：

 */
export function getAllActivePersonsFromCache(params = {}) {
  return request.get({
    url: '/swm/swmPerson/getAllActivePersonsFromCache',
    params,
  });
}

/**
 * 获取有身份证的所有在职人员缓存信息
 */
export function getAllActivePersonsWithIdCardFromCache(params = {}) {
  return request.get({
    url: '/swm/swmPerson/getAllActivePersonsWithIdCardFromCache',
    params,
  });
}

/**
 * 根据部门条件查询在职人员
 */
export function getPersonsByDepartmentCondition(departmentCondition: string) {
  return request.get<PersonInfo[]>({
    url: '/swm/swmPerson/findPersonsByDepartmentCondition',
    params: { departmentCondition },
  });
}

/**
 * 搜索人员（支持姓名、身份证、电话搜索）
 */
export function searchPersons(params: {
  keyword?: string; // 搜索关键词
  searchType?: string; // 搜索类型：name(姓名)、idCard(身份证)、phone(电话)、all(全部)
  pageNo?: number;
  pageSize?: number;
}) {
  return request.get<{
    list: PersonInfo[];
    total: number;
    success: boolean;
  }>({
    url: '/swm/swmPerson/searchPersons',
    params,
  });
}

/**
 * 批量完成安全教育
 */
export function batchCompleteSafetyEducation(personIds: string) {
  return request.post<{
    result: string;
    message: string;
    successCount: number;
    totalCount: number;
    failedCount?: number;
    failedNames?: string;
  }>({
    url: '/swm/swmPerson/batchCompleteSafetyEducation',
    params: { personIds },
  });
}

/**
 * 根据身份证号查询该人员参与的所有安全教育记录
 */
export function getSafetyEducationByIdentityCard(identityCard: string) {
  return request.get<{
    success: boolean;
    data: any[];
    total: number;
    message: string;
  }>({
    url: '/swm/swmPerson/getSafetyEducationByIdentityCard',
    params: { identityCard },
  });
}
