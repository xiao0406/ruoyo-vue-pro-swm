/**
 * @author Shawn
 * @date 2025-05-14
 */
import { FormSchema } from '@/components/swm/Form';
import { BasicColumn } from '@/components/swm/Table';
import { formatToDate } from '@/utils/dateUtil';

// 定义离职表单
export const formSchema: FormSchema[] = [
  {
    field: 'id',
    label: 'ID',
    component: 'Input',
    show: false,
  },
  {
    field: 'name',
    label: '姓名',
    component: 'Input',
    componentProps: {
      disabled: true,
    },
    required: true,
  },
  {
    field: 'helmetReturned',
    label: '是否归还安全帽',
    component: 'RadioButtonGroup',
    defaultValue: '1',
    required: true,
    componentProps: {
      options: [
        { label: '是', value: '1' },
        { label: '否', value: '0' },
      ],
    },
  },
  {
    field: 'departureType',
    label: '离职类型',
    component: 'RadioButtonGroup',
    defaultValue: '1',
    required: true,
    componentProps: {
      options: [
        { label: '正常离职', value: '1' },
        { label: '异常离职', value: '0' },
      ],
    },
  },
  {
    field: 'departureReason',
    label: '离职原因',
    component: 'InputTextArea',
    required: true,
    componentProps: {
      rows: 4,
      placeholder: '请输入离职原因',
    },
    colProps: { span: 24 },
  },
];

/**
 * 离职记录列表列定义
 */
export const departureRecordColumns: BasicColumn[] = [
  {
    title: '姓名',
    dataIndex: 'name',
    width: 100,
    align: 'center',
  },
  {
    title: '身份证号码',
    dataIndex: 'identityCard',
    width: 180,
    align: 'center',
  },
  {
    title: '离职时间',
    dataIndex: 'departureDate',
    width: 150,
    align: 'center',
    customRender: ({ text }) => {
      return text ? formatToDate(text, 'YYYY-MM-DD') : '';
    },
  },
  {
    title: '离职类型',
    dataIndex: 'departureTypeText',
    width: 100,
    align: 'center',
  },
  {
    title: '是否归还安全帽',
    dataIndex: 'helmetReturnedText',
    width: 120,
    align: 'center',
  },
  {
    title: '离职原因',
    dataIndex: 'departureReason',
    width: 200,
    align: 'center',
  },
  {
    title: '所属单位',
    dataIndex: 'company',
    width: 180,
    align: 'center',
  },
];
