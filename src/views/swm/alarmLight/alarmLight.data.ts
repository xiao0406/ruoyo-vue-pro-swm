import { BasicColumn } from '@/components/swm/Table';
import { FormSchema } from '@/components/swm/Table';

export const columns: BasicColumn[] = [
  {
    title: '报警灯名称',
    dataIndex: 'lightName',
    width: 150,
  },
  {
    title: 'SN码',
    dataIndex: 'snCode',
    width: 120,
  },
  {
    title: '是否报警',
    dataIndex: 'enableAlarm',
    width: 100,
  },
  {
    title: '配置数量',
    dataIndex: 'configCount',
    width: 100,
  },
  {
    title: '创建时间',
    dataIndex: 'createDate',
    width: 160,
  },
  {
    title: '备注',
    dataIndex: 'remarks',
    ellipsis: true,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'lightName',
    label: '报警灯名称',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'snCode',
    label: 'SN码',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'enableAlarm',
    label: '是否报警',
    component: 'Select',
    componentProps: {
      options: [
        { label: '是', value: '1' },
        { label: '否', value: '0' },
      ],
    },
    colProps: { span: 8 },
  },
];

export const alarmLightFormSchema: FormSchema[] = [
  {
    field: 'lightName',
    label: '报警灯名称',
    component: 'Input',
    required: true,
    componentProps: {
      placeholder: '请输入报警灯名称',
      maxlength: 100,
    },
  },
  {
    field: 'snCode',
    label: 'SN码',
    component: 'Input',
    required: true,
    componentProps: {
      placeholder: '请输入SN码',
      maxlength: 100,
    },
  },
  {
    field: 'enableAlarm',
    label: '是否报警',
    component: 'RadioButtonGroup',
    defaultValue: '1',
    componentProps: {
      options: [
        { label: '是', value: '1' },
        { label: '否', value: '0' },
      ],
    },
  },
  {
    field: 'remarks',
    label: '备注',
    component: 'InputTextArea',
    componentProps: {
      placeholder: '请输入备注信息',
      rows: 4,
      maxlength: 500,
    },
  },
];
