/**
 * 班次类型枚举
 */
export enum classesEnum {
  early = '早班',
  nightShift = '晚班',
}

export const classesTextMap = {
  [classesEnum.early]: '早班',
  [classesEnum.nightShift]: '晚班',
};

/**
 * 排班状态枚举
 */
export enum scheduleStatusEnum {
  active = '1',
  inactive = '0',
}

export const scheduleStatusTextMap = {
  [scheduleStatusEnum.active]: '生效',
  [scheduleStatusEnum.inactive]: '未生效',
};
