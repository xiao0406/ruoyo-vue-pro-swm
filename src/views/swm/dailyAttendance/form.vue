<template>
  <BasicModal
    v-bind="$attrs"
    :showFooter="true"
    :okAuth="'sys:supplierManagement:edit'"
    @register="registerModal"
    :showOkBtn="false"
    width="60%"
  >
    <template #title>
      <Icon icon="ant-design:book-outlined" class="pr-1 m-1" />
      <span> 考勤记录 </span>
    </template>

    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts">
  export default defineComponent({
    name: 'ViewsSysSupplierManagementForm',
  });
</script>
<script lang="ts" setup>
  import { defineComponent } from 'vue';
  import { Icon } from '@/components/swm/Icon';
  import { BasicForm, FormSchema, useForm } from '@/components/swm/Form';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { getDailyAttendanceDetail } from '@/api/swm/dailyAttendance';

  const inputFormSchemas: FormSchema[] = [
    {
      field: 'id',
      component: 'Input',
      show: false,
    },
    {
      field: 'employeeName',
      label: '员工姓名',
      component: 'Input',
      componentProps: {
        disabled: true,
      },
    },
    {
      field: 'employeeId',
      label: '员工ID',
      component: 'Input',
      componentProps: {
        disabled: true,
      },
    },
    {
      field: 'attendanceNormal',
      label: '考勤状态',
      component: 'Select',
      componentProps: {
        disabled: true,
        dictType: 'swm_attendance_status',
      },
    },
    {
      field: 'classes',
      label: '班次',
      component: 'Select',
      required: true,
      componentProps: {
        disabled: true,
        dictType: 'shift_type_enum',
      },
    },
    {
      field: 'attendanceDate',
      label: '考勤日期',
      component: 'DatePicker',
      required: true,
      componentProps: {
        disabled: true,
        format: 'YYYY-MM-DD',
        valueFormat: 'YYYY-MM-DD',
      },
    },
    {
      field: 'workTimeRange',
      label: '应考勤时间范围',
      component: 'Input',
      required: true,
      componentProps: {
        disabled: true,
        placeholder: '格式如：08:00-17:00',
      },
    },
    {
      field: 'clockInDate',
      label: '上班打卡时间',
      component: 'DatePicker',
      componentProps: {
        disabled: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
      },
    },
    {
      field: 'clockOutDate',
      label: '下班打卡时间',
      component: 'DatePicker',
      componentProps: {
        disabled: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
      },
    },
    {
      field: 'scheduledHours',
      label: '应考勤时长(h)',
      component: 'InputNumber',
      required: true,
      componentProps: {
        disabled: true,
        min: 0,
        step: 0.5,
        precision: 2,
      },
    },
    {
      field: 'actualHours',
      label: '实际考勤时长(h)',
      component: 'InputNumber',
      componentProps: {
        disabled: true,
        min: 0,
        step: 0.5,
        precision: 2,
      },
    },
    {
      field: 'idleHours',
      label: '怠工时长(h)',
      component: 'InputNumber',
      componentProps: {
        disabled: true,
        min: 0,
        step: 0.1,
        precision: 2,
      },
    },
    {
      field: 'effectiveWorkHours',
      label: '实际工作时长(h)',
      component: 'InputNumber',
      componentProps: {
        disabled: true,
        min: 0,
        step: 0.1,
        precision: 2,
      },
    },
    {
      field: 'dailyEfficiency',
      label: '今日工效',
      component: 'InputNumber',
      componentProps: {
        disabled: true,
        min: 0,
        max: 1,
        step: 0.01,
        precision: 4,
      },
    },
    {
      field: 'dailyAchievementRate',
      label: '今日达成率',
      component: 'InputNumber',
      componentProps: {
        disabled: true,
        min: 0,
        max: 1,
        step: 0.01,
        precision: 4,
      },
    },
    {
      field: 'remarks',
      label: '备注',
      component: 'InputTextArea',
      componentProps: {
        disabled: true,
      },
      colProps: { lg: 24, md: 24 },
    },
  ];

  const [registerForm, { resetFields, setFieldsValue }] = useForm({
    labelWidth: 120,
    schemas: inputFormSchemas,
    baseColProps: { lg: 12, md: 24 },
  });

  const [registerModal, { setModalProps }] = useModalInner(async (data) => {
    resetFields();
    setModalProps({ loading: true });
    const res = await getDailyAttendanceDetail({ id: data.id });

    setFieldsValue(res);
    setModalProps({ loading: false });
  });
</script>
<style scoped>
  :deep(.ant-input[disabled]),
  :deep(.ant-select-disabled.ant-select:not(.ant-select-customize-input) .ant-select-selector),
  :deep(.ant-select-disabled.ant-select-multiple .ant-select-selection-item),
  :deep(.ant-input-number-disabled),
  :deep(.ant-picker-input > input[disabled]) {
    color: #000000d9 !important;
  }
</style>
