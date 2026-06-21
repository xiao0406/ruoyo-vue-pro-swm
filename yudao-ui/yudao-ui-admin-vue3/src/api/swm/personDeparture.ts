/**
 * @author Shawn
 * @date 2025-05-14
 */
import request from '@/config/axios';
import { Page } from '@/api/model/baseModel';

/**
 * 离职人员信息接口
 */
export interface PersonDepartureInfo {
  id: string;
  name: string;
  personType: string;
  gender: string;
  company: string;
  department: string;
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
  departureDate?: string; // 离职日期
  createBy: string;
  createDate: string;
  updateBy: string;
  updateDate: string;
  remarks: string;
}

/**
 * 分页查询离职人员列表
 */
export function getPersonDepartureList(params) {
  return request.get<Page<PersonDepartureInfo>>({
    url: '/swm/swmPersonDeparture/listData',
    params,
  });
}

/**
 * 获取单个离职人员信息
 */
export function getPersonDeparture(id: string) {
  return request.get<PersonDepartureInfo>({
    url: '/swm/swmPersonDeparture/form',
    params: { id },
  });
}

/**
 * 根据身份证查询离职记录
 */
export function findDepartureByIdentityCard(identityCard: string) {
  return request.get({
    url: '/swm/swmPersonDeparture/findListByIdentityCard',
    params: { identityCard },
  });
}
