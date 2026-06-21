import { FormSchema } from '@/components/swm/Form';

/**
 * 表单数据
 */
// 新增
export const formSchema: FormSchema[] = [
  {
    field: 'beaconType',
    label: '信标类型',
    component: 'Select',
    required: false,
    componentProps: {
      dictType: 'beacon_type_enum',
      allowClear: true,
    },
  },
  {
    field: 'beaconId',
    label: 'MAC地址',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入' }],
    ifShow: ({ values }) => values.beaconType !== '2' && values.beaconType !== '电子围栏',
  },
  {
    field: 'major',
    label: 'Major',
    component: 'Input',
    required: false,
  },
  {
    field: 'minor',
    label: 'Minor',
    component: 'Input',
    required: false,
  },
  {
    field: 'deviceName',
    label: '设备名称',
    component: 'Input',
    required: false,
  },
  {
    field: 'beaconColor',
    label: '信标颜色',
    component: 'Input',
    required: false,
    defaultValue: '#ff0000',
    componentProps: {
      type: 'color',
      style: { width: '60px' },
    },
  },
  {
    field: 'location',
    label: '所在位置',
    component: 'Input',
    required: false,
  },
  {
    field: 'area',
    label: '所属区域',
    component: 'Input',
    required: false,
  },
  {
    field: 'pixelX',
    label: '图纸像素X坐标',
    component: 'InputNumber',
    required: false,
  },
  {
    field: 'pixelY',
    label: '图纸像素Y坐标',
    component: 'InputNumber',
    required: false,
  },
  // {
  //   field: 'realX',
  //   label: '实际地址X坐标',
  //   component: 'InputNumber',
  //   required: false,
  // },
  // {
  //   field: 'realY',
  //   label: '实际地址Y坐标',
  //   component: 'InputNumber',
  //   required: false,
  // },
  {
    field: 'streamUrl',
    label: '推流地址',
    component: 'Input',
    required: false,
    ifShow: ({ values }) => values.beaconType === '4', //判断beaconType为'2'(信标类型)时显示
  },
];
// 编辑
export const editFormSchema: FormSchema[] = [
  {
    field: 'beaconType',
    label: '信标类型',
    component: 'Select',
    required: false,
    componentProps: {
      dictType: 'beacon_type_enum',
      allowClear: true,
    },
  },
  {
    field: 'beaconId',
    label: 'MAC地址',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入' }],
    ifShow: ({ values }) => values.beaconType !== '2' && values.beaconType !== '电子围栏',
  },
  {
    field: 'major',
    label: 'Major',
    component: 'Input',
    required: false,
  },
  {
    field: 'minor',
    label: 'Minor',
    component: 'Input',
    required: false,
  },
  {
    field: 'deviceName',
    label: '设备名称',
    component: 'Input',
    required: false,
  },
  {
    field: 'beaconColor',
    label: '信标颜色',
    component: 'Input',
    required: false,
    defaultValue: '#ff0000',
    componentProps: {
      type: 'color',
      style: { width: '60px' },
    },
  },
  {
    field: 'location',
    label: '所在位置',
    component: 'Input',
    required: false,
  },
  {
    field: 'area',
    label: '所属区域',
    component: 'Input',
    required: false,
  },
  {
    field: 'pixelX',
    label: '图纸像素X坐标',
    component: 'InputNumber',
    required: false,
  },
  {
    field: 'pixelY',
    label: '图纸像素Y坐标',
    component: 'InputNumber',
    required: false,
  },
  {
    field: 'realX',
    label: '实际地址X坐标',
    component: 'InputNumber',
    required: false,
  },
  {
    field: 'realY',
    label: '实际地址Y坐标',
    component: 'InputNumber',
    required: false,
  },
  {
    field: 'beaconStatus',
    label: '信标状态',
    component: 'Select',
    required: false,
    componentProps: {
      dictType: 'beacon_status_enum',
      allowClear: true,
    },
  },
  {
    field: 'deployStatus',
    label: '部署状态',
    component: 'Select',
    required: false,
    componentProps: {
      dictType: 'deploy_status_enum',
      allowClear: true,
    },
  },
  {
    field: 'streamUrl',
    label: '推流地址',
    component: 'InputNumber',
    required: false,
    ifShow: ({ values }) => values.beaconType === '4', //判断beaconType为'2'(信标类型)时显示
  },
];
// 批量
export const batchSchema: FormSchema[] = [
  {
    field: 'action',
    label: '批量操作',
    component: 'Select',
    defaultValue: 'update',
    required: true,
    componentProps: {
      options: [
        { label: '批量更新', value: 'update' },
        { label: '批量删除', value: 'delete' },
      ],
    },
  },
  {
    field: 'beaconType',
    label: '信标类型',
    component: 'Select',
    required: false,
    componentProps: {
      dictType: 'beacon_type_enum',
      allowClear: true,
    },
    ifShow: ({ values }) => values.action === 'update',
  },
  {
    field: 'beaconColor',
    label: '信标颜色',
    component: 'Input',
    required: false,
    componentProps: {
      type: 'color',
      style: { width: '60px' },
    },
    ifShow: ({ values }) => values.action === 'update',
  },
  {
    field: 'beaconStatus',
    label: '信标状态',
    component: 'Select',
    required: false,
    componentProps: {
      dictType: 'beacon_status_enum',
      allowClear: true,
    },
    ifShow: ({ values }) => values.action === 'update',
  },
  {
    field: 'deployStatus',
    label: '部署状态',
    component: 'Select',
    required: false,
    componentProps: {
      dictType: 'deploy_status_enum',
      allowClear: true,
    },
    ifShow: ({ values }) => values.action === 'update',
  },
];
