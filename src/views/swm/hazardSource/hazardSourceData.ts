import { FormSchema } from '@/components/swm/Form';
import {
  getBeaconSelectList,
  getAvailableDangerousSourceBeaconSelectList,
} from '@/api/swm/beacon';
import { getAllPersons } from '@/api/swm/person';
import { getActiveVoiceTemplates } from '@/api/swm/voiceTemplate';

// 创建一个用于存储当前编辑记录ID的变量
export let currentEditingHazardSourceId: string | null = null;

// 设置当前编辑的风险ID
export function setCurrentEditingHazardSourceId(id: string | null) {
  currentEditingHazardSourceId = id;
}

/**
 * 风险表单数据
 * @author Shawn
 * @date 2025-05-22
 */

// 新增表单
export const formSchema: FormSchema[] = [
  {
    field: 'hazardName',
    label: '危险区域名称',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入危险区域名称' }],
  },
  {
    field: 'hazardCategory',
    label: '危险区域类别',
    component: 'Select',
    required: true,
    componentProps: {
      dictType: 'hazard_category_enum',
      allowClear: true,
      placeholder: '请选择危险区域类别',
    },
    rules: [{ required: true, message: '请选择危险区域类别' }],
  },
  {
    field: 'location',
    label: '位置',
    component: 'InputTextArea',
    required: true,
    componentProps: {
      rows: 2,
    },
    rules: [{ required: true, message: '请输入位置信息' }],
  },
  {
    field: 'beaconIdentifier',
    label: '所属信标',
    component: 'Select',
    required: false,
    componentProps: {
      api: () => getAvailableDangerousSourceBeaconSelectList(currentEditingHazardSourceId),
      resultField: '',
      labelField: 'label',
      valueField: 'value',
      immediate: true,
      showSearch: true,
      allowClear: true,
      mode: 'multiple', // 启用多选模式
      placeholder: '请选择所属信标（可多选）',
      maxTagCount: 3, // 最多显示3个标签，其余折叠显示
      optionFilterProp: 'label', // 搜索时过滤option的属性
    },
  },
  {
    field: 'isPatrolIncluded',
    label: '是否加入巡检',
    component: 'Select',
    required: false,
    defaultValue: '0',
    componentProps: {
      dictType: 'is_patrol_included_enum',
      allowClear: true,
    },
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
    ifShow: ({ values }) => values.isPatrolIncluded === '1',
    rules: [{ required: true, message: '请输入巡检频次' }],
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
      immediate: true, // 立即加载选项，确保编辑时能正确回显
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
    },
    ifShow: ({ values }) => values.isPatrolIncluded === '1',
    rules: [{ required: true, message: '请选择巡检负责人' }],
  },
  {
    field: 'firstInspectionTime',
    label: '首次巡检时间',
    component: 'DatePicker',
    componentProps: {
      showTime: true,
      format: 'YYYY-MM-DD HH:mm:ss',
      style: { width: '100%' },
    },
    ifShow: ({ values }) => values.isPatrolIncluded === '1',
    required: true,
    rules: [{ required: true, message: '请选择首次巡检时间' }],
  },
  {
    field: 'patrolRecordSummary',
    label: '巡检记录摘要',
    component: 'InputTextArea',
    componentProps: {
      rows: 3,
    },
    required: false,
  },
  {
    field: 'registrationTime',
    label: '登记时间',
    component: 'DatePicker',
    componentProps: {
      showTime: true,
      format: 'YYYY-MM-DD HH:mm:ss',
      style: { width: '100%' },
    },
    required: true,
    rules: [{ required: true, message: '请选择登记时间' }],
  },
  {
    field: 'hazardStatus',
    label: '危险区域状态',
    component: 'Select',
    defaultValue: '0',
    required: true,
    componentProps: {
      dictType: 'hazard_status_enum',
      allowClear: true,
    },
  },
  {
    field: 'voiceTemplateId',
    label: '语音模板',
    component: 'Select',
    required: false,
    componentProps: {
      api: async () => {
        const res = await getActiveVoiceTemplates();
        return (
          res?.map((item) => ({
            label: item.templateName,
            value: item.id,
          })) || []
        );
      },
      immediate: true, // 立即加载选项
      showSearch: true,
      filterOption: (input, option) => {
        return option.label?.toLowerCase().indexOf(input.toLowerCase()) >= 0;
      },
      placeholder: '请选择语音模板',
      allowClear: true,
    },
  },
  // {
  //   field: 'beaconTag',
  //   label: '信标标记',
  //   component: 'Input',
  //   required: false,
  //   componentProps: {
  //     placeholder: '请输入信标标记',
  //     maxlength: 200,
  //   },
  // },
  {
    field: 'remarks',
    label: '备注信息',
    component: 'InputTextArea',
    componentProps: {
      rows: 3,
    },
    required: false,
  },
  // 在 formSchema 和 editFormSchema 中都添加人员白名单字段
  {
    field: 'personWhiteList',
    label: '人员白名单',
    component: 'Select',
    required: false,
    componentProps: {
      mode: 'multiple', // 多选模式
      placeholder: '请选择人员白名单（可多选）',
      maxTagCount: 3, // 最多显示3个标签，其余折叠显示
      allowClear: true,
      showSearch: true,
      filterOption: (input, option) => {
        return option.label?.toLowerCase().indexOf(input.toLowerCase()) >= 0;
      },
      popupClassName: 'person-white-list-dropdown',
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
];

// 编辑表单
export const editFormSchema: FormSchema[] = [
  {
    field: 'hazardName',
    label: '危险区域名称',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入危险区域名称' }],
  },
  {
    field: 'hazardCategory',
    label: '危险区域类别',
    component: 'Select',
    required: true,
    componentProps: {
      dictType: 'hazard_category_enum',
      allowClear: true,
      placeholder: '请选择危险区域类别',
    },
    rules: [{ required: true, message: '请选择危险区域类别' }],
  },
  {
    field: 'location',
    label: '位置',
    component: 'InputTextArea',
    required: true,
    componentProps: {
      rows: 2,
    },
    rules: [{ required: true, message: '请输入位置信息' }],
  },
  {
    field: 'beaconIdentifier',
    label: '所属信标',
    component: 'Select',
    required: false,
    componentProps: {
      api: () => getAvailableDangerousSourceBeaconSelectList(currentEditingHazardSourceId),
      resultField: '',
      labelField: 'label',
      valueField: 'value',
      immediate: true,
      showSearch: true,
      allowClear: true,
      mode: 'multiple', // 启用多选模式
      placeholder: '请选择所属信标（可多选）',
      maxTagCount: 3, // 最多显示3个标签，其余折叠显示
      optionFilterProp: 'label', // 搜索时过滤option的属性
    },
  },
  {
    field: 'isPatrolIncluded',
    label: '是否加入巡检',
    component: 'Select',
    required: false,
    componentProps: {
      dictType: 'is_patrol_included_enum',
      allowClear: true,
    },
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
    ifShow: ({ values }) => values.isPatrolIncluded === '1',
    rules: [{ required: true, message: '请输入巡检频次' }],
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
      immediate: true, // 立即加载选项，确保编辑时能正确回显
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
    },
    ifShow: ({ values }) => values.isPatrolIncluded === '1',
    rules: [{ required: true, message: '请选择巡检负责人' }],
  },
  {
    field: 'firstInspectionTime',
    label: '首次巡检时间',
    component: 'DatePicker',
    componentProps: {
      showTime: true,
      format: 'YYYY-MM-DD HH:mm:ss',
      style: { width: '100%' },
    },
    ifShow: ({ values }) => values.isPatrolIncluded === '1',
    required: true,
    rules: [{ required: true, message: '请选择首次巡检时间' }],
  },
  {
    field: 'patrolRecordSummary',
    label: '巡检记录摘要',
    component: 'InputTextArea',
    componentProps: {
      rows: 3,
    },
    required: false,
  },
  {
    field: 'registrationTime',
    label: '登记时间',
    component: 'DatePicker',
    componentProps: {
      showTime: true,
      format: 'YYYY-MM-DD HH:mm:ss',
      style: { width: '100%' },
    },
    required: true,
    rules: [{ required: true, message: '请选择登记时间' }],
  },
  {
    field: 'hazardStatus',
    label: '危险区域状态',
    component: 'Select',
    required: true,
    componentProps: {
      dictType: 'hazard_status_enum',
      allowClear: true,
    },
  },
  {
    field: 'voiceTemplateId',
    label: '语音模板',
    component: 'Select',
    required: false,
    componentProps: {
      api: async () => {
        const res = await getActiveVoiceTemplates();
        return (
          res?.map((item) => ({
            label: item.templateName,
            value: item.id,
          })) || []
        );
      },
      immediate: true, // 立即加载选项
      showSearch: true,
      filterOption: (input, option) => {
        return option.label?.toLowerCase().indexOf(input.toLowerCase()) >= 0;
      },
      placeholder: '请选择语音模板',
      allowClear: true,
    },
  },
  // {
  //   field: 'beaconTag',
  //   label: '信标标记',
  //   component: 'Input',
  //   required: false,
  //   componentProps: {
  //     placeholder: '请输入信标标记',
  //     maxlength: 200,
  //   },
  // },
  {
    field: 'remarks',
    label: '备注信息',
    component: 'InputTextArea',
    componentProps: {
      rows: 3,
    },
    required: false,
  },
];
