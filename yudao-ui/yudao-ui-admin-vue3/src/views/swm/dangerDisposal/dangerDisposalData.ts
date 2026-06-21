/**
 * 隐患处置数据模型
 * @author Shawn
 * @date 2025-05-22
 */
import { FormSchema } from '@/components/swm/Form';
import { BasicColumn } from '@/components/swm/Table';

/**
 * 表格列定义
 */
export const columns: BasicColumn[] = [
  {
    title: '隐患名称',
    dataIndex: 'dangerName',
    width: 180,
    align: 'left',
  },
  {
    title: '隐患位置',
    dataIndex: 'location',
    width: 180,
  },
  {
    title: '处置时间',
    dataIndex: 'disposalTime',
    width: 160,
  },
  {
    title: '处置人员',
    dataIndex: 'disposalUser',
    width: 120,
  },
  {
    title: '创建时间',
    dataIndex: 'createDate',
    width: 160,
  },
];

/**
 * 搜索表单
 */
export const searchFormSchema: FormSchema[] = [
  {
    field: 'dangerName',
    label: '隐患名称',
    component: 'Input',
    colProps: { span: 6 },
    componentProps: {
      placeholder: '请输入隐患名称',
    },
  },
];

/**
 * 编辑表单
 */
export const formSchema: FormSchema[] = [
  {
    field: 'id',
    label: 'ID',
    component: 'Input',
    show: false,
  },
  {
    field: 'hiddenDangerId',
    label: '隐患ID',
    component: 'Input',
    show: false,
  },
  {
    field: 'dangerName',
    label: '隐患名称',
    component: 'Input',
    show: false,
  },
  {
    field: 'location',
    label: '隐患位置',
    component: 'Input',
    show: false,
  },
  {
    field: 'disposalTime',
    label: '处置时间',
    component: 'DatePicker',
    required: true,
    colProps: { span: 12 },
    componentProps: {
      showTime: true,
      format: 'YYYY-MM-DD HH:mm:ss',
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
      placeholder: '请选择处置时间',
    },
  },
  {
    field: 'disposalUser',
    label: '处置人员',
    component: 'Select',
    required: true,
    colProps: { span: 12 },
    componentProps: {
      placeholder: '请选择处置人员',
      showSearch: true,
      filterOption: (input: string, option: any) => {
        return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0;
      },
      options: [], // 选项将在组件中动态加载
    },
  },
  {
    field: 'disposalMethod',
    label: '处置方法',
    component: 'Select',
    required: true,
    colProps: { span: 12 },
    componentProps: {
      dictType: 'disposal_method',
      placeholder: '请选择处置方法',
    },
  },
  {
    field: 'disposalStatus',
    label: '处置状态',
    component: 'Select',
    required: true,
    colProps: { span: 12 },
    componentProps: {
      dictType: 'disposal_status',
      placeholder: '请选择处置状态',
    },
  },
  {
    field: 'disposalContent',
    label: '处置内容',
    component: 'InputTextArea',
    required: true,
    colProps: { span: 24 },
    componentProps: {
      rows: 4,
      placeholder: '请输入处置内容',
    },
  },
  {
    field: 'attachment',
    label: '附件',
    component: 'Input',
    slot: 'fileUpload',
    colProps: { span: 24 },
  },
];
