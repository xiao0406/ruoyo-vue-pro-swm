/**
 * 隐患信息表单定义
 * @author Shawn
 * @date 2023-11-16
 */
import { FormSchema } from '@/components/swm/Form';
import { getInspectionPlanSelectList } from '@/api/swm/inspectionPlan';

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
      allowClear: true,
    },
  },
  {
    field: 'location',
    label: '隐患位置',
    component: 'Input',
    colProps: { span: 6 },
    componentProps: {
      placeholder: '请输入隐患位置',
      allowClear: true,
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
    field: 'dangerName',
    label: '隐患名称',
    component: 'Input',
    required: true,
    colProps: { span: 24 },
    rules: [{ required: true, message: '请输入隐患名称' }],
  },
  {
    field: 'location',
    label: '隐患位置',
    component: 'InputTextArea',
    colProps: { span: 24 },
    componentProps: {
      rows: 2,
      placeholder: '请输入隐患位置',
    },
  },
  {
    field: 'inspectionPlanId',
    label: '关联巡检计划',
    component: 'Select',
    colProps: { span: 12 },
    componentProps: {
      api: getInspectionPlanSelectList,
      resultField: '',
      labelField: 'label',
      valueField: 'value',
      immediate: true,
      showSearch: true,
      filterOption: (input: string, option: any) => {
        return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0;
      },
      placeholder: '请选择关联巡检计划',
      popupClassName: 'inspection-plan-dropdown',
      dropdownMatchSelectWidth: false,
      dropdownStyle: {
        maxHeight: '300px',
        overflow: 'auto',
        position: 'fixed',
        zIndex: 1050,
      },
      getPopupContainer: () => document.body,
    },
  },
  {
    field: 'isBeaconDeployed',
    label: '是否布设信标',
    component: 'Select',
    defaultValue: '0',
    colProps: { span: 12 },
    componentProps: {
      dictType: 'is_beacon_deployed_enum',
      placeholder: '请选择',
    },
  },
  {
    field: 'isHandled',
    label: '是否已处置',
    component: 'Select',
    defaultValue: '0',
    colProps: { span: 12 },
    componentProps: {
      dictType: 'is_handled_enum',
      placeholder: '请选择',
    },
  },
  {
    field: 'remarks',
    label: '备注',
    component: 'InputTextArea',
    colProps: { span: 24 },
    componentProps: {
      rows: 4,
      placeholder: '请输入备注信息',
    },
  },
];
