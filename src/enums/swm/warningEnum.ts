/**
 * @author Generated
 * @date 2023-05-20
 */

/**
 * 预警类型枚举
 */
export enum WarningTypeEnum {
  ACTIVE = '1',
  PASSIVE = '2',
}

/**
 * 预警类型文本
 */
export const WarningTypeTextMap = {
  [WarningTypeEnum.ACTIVE]: '主动报警',
  [WarningTypeEnum.PASSIVE]: '被动报警',
};

/**
 * 预警内容枚举
 */
export enum WarningContentEnum {
  FALL_ALARM = '跌落报警',
  HELMET_OFF_ALARM = '脱帽报警',
  HELMET_ALARM = '安全帽报警',
  SILENT_ALARM = '静默报警',
  HAZARD_ALARM = '危险区域闯入提示',
  SIP_SOS_ALARM = 'SIP 应急呼叫',
  ELECTRIC_ALARM = '近电报警',
  ONE_KEY_SOS = '应急呼叫',
}

/**
 * 预警内容文本
 */
export const WarningContentTextMap = {
  [WarningContentEnum.FALL_ALARM]: '跌落报警',
  [WarningContentEnum.HELMET_OFF_ALARM]: '脱帽报警',
  [WarningContentEnum.HELMET_ALARM]: '安全帽报警',
  [WarningContentEnum.SILENT_ALARM]: '静默报警',
  [WarningContentEnum.HAZARD_ALARM]: '危险区域闯入提示',
  [WarningContentEnum.SIP_SOS_ALARM]: 'SIP 应急呼叫',
  [WarningContentEnum.ELECTRIC_ALARM]: '近电报警',
  [WarningContentEnum.ONE_KEY_SOS]: '应急呼叫',
};

/**
 * 预警状态枚举
 */
export enum WarningStatusEnum {
  UNPROCESSED = '0',
  PROCESSED = '1',
}

/**
 * 预警状态文本
 */
export const WarningStatusTextMap = {
  [WarningStatusEnum.UNPROCESSED]: '未处理',
  [WarningStatusEnum.PROCESSED]: '已处理',
};
