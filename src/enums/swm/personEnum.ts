/**
 * @author Shawn
 * @date 2025-05-21
 */
import { useDict } from '@/components/swm/Dict';

// 字典类型常量
export const DICT = {
  PERSON_STATUS: 'person_status_enum',
  SAFETY_EDUCATION: 'safety_education_enum',
  HELMET_RETURNED: 'helmet_returned_enum',
  DEPARTURE_TYPE: 'departure_type_enum',
  EXTERNAL_PERSONNEL: 'external_personnel_enum',
};

// 暴露获取字典的方法
const { initDict, getDictLabel } = useDict();

// 立即初始化字典数据
initDict([
  DICT.PERSON_STATUS,
  DICT.SAFETY_EDUCATION,
  DICT.HELMET_RETURNED,
  DICT.DEPARTURE_TYPE,
  DICT.EXTERNAL_PERSONNEL,
]);

/**
 * 性别枚举
 */
export enum GenderEnum {
  MALE = '男',
  FEMALE = '女',
}

/**
 * 安全教育状态枚举
 */
export enum SafetyEducationEnum {
  NOT_STARTED = '0',
  COMPLETED = '1',
}

/**
 * 人员状态枚举
 */
export enum PersonStatusEnum {
  ACTIVE = '1',
  INACTIVE = '0',
}

/**
 * 工种枚举
 */
export const jobTypeOptions = [
  { value: '电焊工', label: '电焊工' },
  { value: '普工', label: '普工' },
  { value: '钳工', label: '钳工' },
  { value: '操作工', label: '操作工' },
  { value: '安装工', label: '安装工' },
  { value: '工程师', label: '工程师' },
];

/**
 * 所属单位选项
 */
export const companyOptions = [
  { value: '中建钢构有限公司', label: '中建钢构有限公司' },
  { value: '天津厂', label: '天津厂' },
  { value: '北京厂', label: '北京厂' },
  { value: '上海厂', label: '上海厂' },
];

/**
 * 车间选项
 */
export const departmentOptions = [
  { value: '工程部', label: '工程部' },
  { value: '施工部', label: '施工部' },
  { value: '技术部', label: '技术部' },
  { value: '一车间', label: '一车间' },
  { value: '二车间', label: '二车间' },
  { value: '三车间', label: '三车间' },
];

/**
 * 工序选项
 */
export const processOptions = [
  { value: '加工车间', label: '加工车间' },
  { value: '安装车间', label: '安装车间' },
  { value: '设计车间', label: '设计车间' },
  { value: '打磨工序', label: '打磨工序' },
  { value: '焊接工序', label: '焊接工序' },
  { value: '喷涂工序', label: '喷涂工序' },
];

/**
 * 班组选项
 */
export const teamOptions = [
  { value: '一班', label: '一班' },
  { value: '二班', label: '二班' },
  { value: '三班', label: '三班' },
];

/**
 * 获取头盔归还状态文本
 * @param value 状态值
 * @param defaultValue 默认值
 * @returns 状态文本
 */
export function getHelmetReturnedText(value?: string, defaultValue = '未知') {
  return getDictLabel(DICT.HELMET_RETURNED, value, defaultValue);
}

/**
 * 获取离场类型文本
 * @param value 状态值
 * @param defaultValue 默认值
 * @returns 状态文本
 */
export function getDepartureTypeText(value?: string, defaultValue = '未知') {
  return getDictLabel(DICT.DEPARTURE_TYPE, value, defaultValue);
}
