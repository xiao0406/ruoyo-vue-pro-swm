import { FormSchema } from '@/components/swm/Form';
import { classesEnum } from '@/enums/swm/attendanceEnum';
/**
 * 表单数据
 */
// 排班时间管理
export const formSchema: FormSchema[] = [
  {
    field: 'earlyShift',
    label: '早班',
    component: 'InputGroup',
    slot: 'earlyShift',
    required: false,
  },
  {
    field: 'nightShift',
    label: '晚班',
    component: 'InputGroup',
    slot: 'nightShift',
    required: false,
  },
];
// 人员排班
export const formScheduling: FormSchema[] = [
  {
    field: 'month',
    label: '月份',
    component: 'MonthPicker',
    required: true,
    componentProps: {
      format: 'YYYY-MM',
      placeholder: '请选择月份',
      valueFormat: 'YYYY-MM',
      style: { width: '100%' },
    },
    rules: [
      {
        required: true,
        message: '请选择月份',
      },
    ],
  },
  {
    field: 'classes',
    label: '班次',
    component: 'Select',
    required: false,
    defaultValue: classesEnum.early,
    componentProps: {
      options: [
        { label: classesEnum.early, value: classesEnum.early },
        { label: classesEnum.nightShift, value: classesEnum.nightShift },
      ],
    },
  },
  {
    label: '',
    field: 'activityId',
    component: 'Select',
    slot: 'backActivityTable',
    colProps: { lg: 12, md: 24 },
    required: false,
  },
];
