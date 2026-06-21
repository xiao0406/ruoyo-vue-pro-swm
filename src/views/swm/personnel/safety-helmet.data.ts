/**
 * @author Shawn
 * @date 2025-05-22
 */
import { FormSchema } from '@/components/swm/Form';
import { findAvailableHelmets } from '@/api/swm/helmetDevice';

/**
 * 安全帽设备类型
 */
export interface HelmetDevice {
  id?: string;
  helmetId?: string;
  helmetType?: string;
  batteryLevel?: number;
  assignedPerson?: string;
  assignedWorkshop?: string;
  assignedProcess?: string;
  assignedTeam?: string;
  createBy?: string;
  createTime?: string;
  updateBy?: string;
  updateTime?: string;
  remarks?: string;
}

/**
 * 安全帽选项类型
 */
export interface HelmetOption {
  label: string;
  value: string;
}

/**
 * 绑定安全帽表单数据
 */
export const formSchema: FormSchema[] = [
  {
    field: 'personId',
    label: 'ID',
    component: 'Input',
    show: false,
  },
  {
    field: 'name',
    label: '工人姓名',
    component: 'Input',
    componentProps: {
      disabled: true,
    },
  },
  {
    field: 'personType',
    label: '人员类型',
    component: 'Input',
    componentProps: {
      disabled: true,
    },
  },
  {
    field: 'safetyHelmetId',
    label: '关联安全帽',
    component: 'Select',
    required: true,
    componentProps: {
      options: async () => {
        try {
          const result = await findAvailableHelmets();
          if (result && Array.isArray(result)) {
            return result.map((item) => ({
              label: item.helmetId,
              value: item.helmetId,
            }));
          }
          return [];
        } catch (error) {
          console.error('获取安全帽选项失败', error);
          return [];
        }
      },
      placeholder: '请选择安全帽编号',
      allowClear: true,
      showSearch: true,
      filterOption: (input: string, option: any) => {
        return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0;
      },
      dropdownStyle: { maxHeight: '200px', overflow: 'auto' },
      optionLabelProp: 'label',
    },
    rules: [{ required: true, message: '请选择安全帽编号' }],
  },
];
