import { FormSchema } from '@/components/swm/Form/index';

export interface AlarmConfigModel {
  id: string;
  alarmName: string;
  alarmKey: string;
  enableAlarm: string | number;
  needConfirm: string | number;
}

// 是否选项 - 使用字符串数字，解决选择问题
export const YesNoOptions = [
  { label: '是', value: '1' },
  { label: '否', value: '0' },
];

// 表单配置
export const formSchema: FormSchema[] = [
  {
    field: 'alarmName',
    label: '报警名称',
    component: 'Input',
    required: true,
  },
  {
    field: 'alarmKey',
    label: 'key',
    component: 'Input',
    required: true,
  },
  {
    field: 'enableAlarm',
    label: '是否报警',
    component: 'RadioButtonGroup',
    defaultValue: '1',
    componentProps: {
      options: YesNoOptions,
      onChange: (e: any, formModel: any) => {
        // 当"是否报警"为否时，"是否弹框确认"自动设为否
        if (e === '0') {
          formModel.needConfirm = '0';
        }
      },
    },
    required: true,
    rules: [{ required: true, message: '请选择是否报警' }],
  },
  {
    field: 'isSendZjt',
    label: '是否推送中建通',
    component: 'RadioButtonGroup',
    labelWidth: 140,
    defaultValue: '0',
    componentProps: {
      options: YesNoOptions,
    },
    required: true,
    rules: [{ required: true, message: '请选择是否推送中建通' }],
  },
  {
    field: 'needConfirm',
    label: '是否弹框确认',
    component: 'RadioButtonGroup',
    defaultValue: '1',
    componentProps: {
      options: YesNoOptions,
    },
    required: true,
    dynamicDisabled: ({ values }) => values.enableAlarm === '0',
    ifShow: ({ values }) => values.enableAlarm === '1',
  },
];
