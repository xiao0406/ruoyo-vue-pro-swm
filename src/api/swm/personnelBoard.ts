/**
 * @author
 * @date 2025-05-20
 */
import request from '@/config/axios';
import { Page } from '@/api/model/baseModel';

/**
 * 人员看板信息接口
 */
export interface PersonnelBoard {
  id: string; // 主键ID
  name?: string; // 姓名
  organization?: string; // 所属单位
  workshop?: string; // 所属车间
  process?: string; // 所属工序
  team?: string; // 所属班组

  workStatus?: string; // 工作状态
  workStatusText?: string; // 工作状态文本

  deviceId?: string; // 安全帽编号
  helmetStatus?: string; // 安全帽状态
  helmetStatusText?: string; // 安全帽状态文本

  personnelStatus?: string; // 人员状态
  personnelStatusText?: string; // 人员状态文本

  attendanceCount?: number; // 本月出勤次数
  workingHours?: number; // 本月工作时长
  idleHours?: number; // 本月怠工时长

  leisureCount?: number; // 进入休闲次数
  leisureDurationMin?: number; // 休闲区总逗留时长(分钟)

  idCard?: string; // 身份证号

  remarks?: string; // 备注
  createDate?: string; // 创建时间
  updateDate?: string; // 更新时间
  createBy?: string; // 创建人
  updateBy?: string; // 更新人
  status?: string; // 状态
  isNewRecord?: boolean; // 是否新记录
}

/**
 * 获取人员看板列表数据（分页）
 */
export function getPersonnelBoardList(params?: any) {
  return request.get<Page<PersonnelBoard>>({
    url: '/swm/personnelBoard/listData',
    params,
  });
}

/**
 * 导出人员看板数据
 */
export function exportPersonnelBoardData(params?: any) {
  return request.get(
    {
      url: '/swm/personnelBoard/export',
      params,
      responseType: 'blob',
    },
    { isReturnNativeResponse: true },
  );
}
/**
 * 个人考勤记录明细
 */
export function attendanceDetails(params: { employeeId: string; month?: string }) {
  return request.get({
    url: '/swm/swmAttendanceSummary/attendanceDetails',
    params,
  });
}

/**
 * 获取人员安全帽领用记录
 */
export function getPersonHelmetOrders(params: { personId: string }) {
  return request.get({
    url: '/swm/safetyHelmetOrder/findByPersonId',
    params,
  });
}
