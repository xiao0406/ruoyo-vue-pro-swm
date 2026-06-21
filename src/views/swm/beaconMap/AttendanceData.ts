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
    field: 'middleShift',
    label: '中班',
    component: 'InputGroup',
    slot: 'middleShift',
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
    label: '',
    field: 'activityId',
    component: 'Select',
    slot: 'backActivityTable',
    colProps: { lg: 12, md: 24 },
    required: false,
  },
];
