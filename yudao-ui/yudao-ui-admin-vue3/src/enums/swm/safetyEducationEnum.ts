/**
 * @author Shawn
 * @date 2025-05-22
 * @modified_by Shawn
 * @modified_date 2024-06-23
 */
import { useDict } from '@/components/swm/Dict';

/**
 * 安全教育状态枚举
 */
export enum StatusEnum {
  NOT_STARTED = '0',
  COMPLETED = '1',
}

// 字典类型常量
export const DICT = {
  EDUCATION_STATUS: 'education_status_enum',
  EDUCATION_TYPE: 'education_type_enum',
  PARTICIPATION_TYPE: 'education_participation_type_enum',
};

// 初始化字典数据 - 让每个使用的组件自行初始化
// const { initDict } = useDict();
// initDict([
//   DICT.EDUCATION_STATUS,
//   DICT.EDUCATION_TYPE,
//   DICT.PARTICIPATION_TYPE,
// ]);
