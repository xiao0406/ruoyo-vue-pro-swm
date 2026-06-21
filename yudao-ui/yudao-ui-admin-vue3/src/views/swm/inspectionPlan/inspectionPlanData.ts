import { FormSchema } from '@/components/swm/Form';
import { BasicColumn } from '@/components/swm/Table';
import { formatToDate } from '@/utils/dateUtil';
import { getAllPersons } from '@/api/swm/person';
import { getNotPatrolledHazardSourceList } from '@/api/swm/hazardSource';

/**
 * 表格列定义
 */
export const columns: BasicColumn[] = [
  {
    title: '巡检计划编号',
    dataIndex: 'planCode',
    width: 180,
  },
  {
    title: '计划名称',
    dataIndex: 'planName',
    width: 200,
    slots: { customRender: 'planName' },
  },
  {
    title: '巡检频次（天数）',
    dataIndex: 'frequencyDays',
    width: 130,
  },
  {
    title: '巡检类型',
    dataIndex: 'inspectionType',
    width: 150,
    dictType: 'inspection_type',
  },
  {
    title: '巡检负责人',
    dataIndex: 'responsiblePerson',
    width: 120,
  },
  {
    title: '关联风险',
    dataIndex: 'hazardSourceName',
    width: 200,
    customRender: ({ text }) => {
      if (!text) return '-';
      // 如果是多个风险（逗号分隔），显示前两个，超过则显示省略号
      const names = text.split(',');
      if (names.length <= 2) {
        return names.join(', ');
      } else {
        return `${names.slice(0, 2).join(', ')}...（共${names.length}个）`;
      }
    },
  },
  {
    title: '首次巡检时间',
    dataIndex: 'firstInspectionTime',
    width: 180,
    customRender: ({ text }) => {
      return text ? formatToDate(text, 'YYYY-MM-DD HH:mm:ss') : '';
    },
  },
  {
    title: '计划状态',
    dataIndex: 'planStatus',
    width: 100,
    customRender: ({ text }) => {
      if (text === 'open') {
        return '开启';
      } else if (text === 'pause') {
        return '暂停';
      }
      return text || '开启';
    },
  },
  {
    title: '创建时间',
    dataIndex: 'createDate',
    width: 180,
    customRender: ({ text }) => {
      return text ? formatToDate(text, 'YYYY-MM-DD HH:mm:ss') : '';
    },
  },
];

/**
 * 搜索表单
 */
export const searchFormSchema: FormSchema[] = [
  {
    field: 'planCode',
    label: '巡检计划编号',
    component: 'Input',
    componentProps: {
      placeholder: '请输入巡检计划编号',
      // allowClear: true,
    },
    colProps: { span: 8 },
  },
  {
    field: 'frequencyDays',
    label: '巡检频次(天数)',
    component: 'InputNumber',
    componentProps: {
      placeholder: '请输入巡检频次',
      min: 0,
      precision: 0,
      // allowClear: true,
      // style: { width: '100%' },
    },
    colProps: { span: 8 },
  },
  {
    field: 'inspectionType',
    label: '巡检类型',
    component: 'Select',
    componentProps: {
      dictType: 'inspection_type',
      placeholder: '请选择巡检类型',
      // allowClear: true,
    },
    colProps: { span: 8 },
  },
  {
    field: 'planStatus',
    label: '计划状态',
    component: 'Select',
    componentProps: {
      options: [
        { label: '开启', value: 'open' },
        { label: '暂停', value: 'pause' },
      ],
      placeholder: '请选择计划状态',
      // allowClear: true,
    },
    colProps: { span: 8 },
  },
];

/**
 * 表单数据
 */
export const formSchema: FormSchema[] = [
  {
    field: 'id',
    label: 'ID',
    component: 'Input',
    show: false,
  },
  {
    field: 'planCode',
    label: '巡检计划编号',
    component: 'Input',
    componentProps: {
      disabled: true,
      placeholder: '系统自动生成',
    },
    // 始终显示，不再使用 ifShow 条件
  },
  {
    field: 'planName',
    label: '计划名称',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入计划名称' }],
  },
  {
    field: 'frequencyDays',
    label: '巡检频次（天数）',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 0,
      precision: 0,
    },
    rules: [{ required: true, message: '请输入巡检频次' }],
  },
  {
    field: 'inspectionType',
    label: '巡检类型',
    component: 'Select',
    required: true,
    componentProps: {
      dictType: 'inspection_type',
      allowClear: true,
    },
    rules: [{ required: true, message: '请选择巡检类型' }],
  },
  {
    field: 'hazardSourceIds',
    label: '关联风险',
    component: 'Select',
    componentProps: {
      api: getNotPatrolledHazardSourceList,
      mode: 'multiple',
      showSearch: true,
      optionFilterProp: 'label',
      placeholder: '请选择未加入巡检的风险',
      fieldNames: {
        label: 'label',
        value: 'value',
      },
      optionLabelProp: 'label',
      dropdownMatchSelectWidth: false,
      dropdownStyle: { maxHeight: '400px', overflow: 'auto' },
      immediate: true, // 确保API立即执行
    },
    ifShow: ({ values }) => values.inspectionType === '3', // 只在选择风险巡检时显示
    rules: [{ required: true, message: '请选择关联的风险', trigger: 'change' }],
  },
  {
    field: 'responsiblePersonId',
    label: '巡检负责人',
    component: 'Select',
    required: true,
    componentProps: {
      api: async () => {
        const res = await getAllPersons();
        return (
          res.data?.map((item) => ({
            label: item.name,
            value: item.id,
          })) || []
        );
      },
      showSearch: true,
      filterOption: (input, option) => {
        return option.label?.toLowerCase().indexOf(input.toLowerCase()) >= 0;
      },
      placeholder: '请选择巡检负责人',
      popupClassName: 'responsible-person-dropdown',
      dropdownMatchSelectWidth: false,
      dropdownStyle: {
        maxHeight: '300px',
        overflow: 'auto',
        position: 'fixed',
        zIndex: 1050,
      },
      getPopupContainer: () => document.body,
      // 确保编辑时能正确显示和搜索
      optionFilterProp: 'label',
      labelInValue: false,
      immediate: true, // 确保API立即执行，解决回显问题
    },
    rules: [{ required: true, message: '请选择巡检负责人' }],
  },
  {
    field: 'firstInspectionTime',
    label: '首次巡检时间',
    component: 'DatePicker',
    required: true,
    componentProps: {
      showTime: true,
      format: 'YYYY-MM-DD HH:mm:ss',
      style: { width: '100%' },
    },
    rules: [{ required: true, message: '请选择首次巡检时间' }],
  },
  {
    field: 'planStatus',
    label: '计划状态',
    component: 'Select',
    defaultValue: 'open',
    componentProps: {
      options: [
        { label: '开启', value: 'open' },
        { label: '暂停', value: 'pause' },
      ],
    },
  },
  {
    field: 'remarks',
    label: '备注信息',
    component: 'InputTextArea',
    componentProps: {
      rows: 4,
    },
  },
];
