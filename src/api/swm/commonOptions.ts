/**
 * 通用选项数据API
 * @date 2023-06-20
 */
import request from '@/config/axios';

/**
 * 获取单位选项
 */
export function getCompanyOptions() {
  return request.get({ url: '/swm/common/options/companies' });
}

/**
 * 获取车间选项
 */
export function getDepartmentOptions() {
  return request.get({ url: '/swm/common/options/departments' });
}

/**
 * 获取产线选项
 */
export function getProdLineOptions() {
  return request.get({ url: '/swm/common/options/prodLines' });
}

/**
 * 获取班组选项
 */
export function getWorkGroupOptions() {
  return request.get({ url: '/swm/common/options/workGroups' });
}
