<template>
  <div>
    <BasicTable @register="registerTable">
      <template #tableTitle>
        <Icon :icon="getTitle.icon" class="m-1 pr-1" />
        <span> {{ getTitle.value }} </span>
      </template>

      <template #idleHours="{ record }">
        <a-button
          type="default"
          style="border: none; background: none; color: #0081cc"
          @click="openForms(record, 'slack')"
        >
          {{ record.idleHours }}
        </a-button>
      </template>
      <template #effectiveWorkHours="{ record }">
        <a-button
          type="default"
          style="border: none; background: none; color: #0081cc"
          @click="openForms(record, 'work')"
        >
          {{ record.effectiveWorkHours }}
        </a-button>
      </template>

      <template #toolbar>
        <a-button type="default" :loading="exportLoading" @click="handleExport">
          <Icon icon="ant-design:download-outlined" />
          导出Excel
        </a-button>
      </template>

      <template #firstColumn="{ record }">
        <a @click="handleForm({ id: record.id })">
          {{ record.employeeName }}
        </a>
      </template>
    </BasicTable>
    <InputForm @register="registerModal" />
    <Froms @register="registerFormsModal" />
  </div>
</template>
<script lang="ts">
  export default defineComponent({
    name: 'ViewsSwmSwmDifyList',
  });
</script>
<script lang="ts" setup>
  import { defineComponent, ref } from 'vue';
  import { useI18n } from '@/hooks/swm/useI18n';
  import { router } from '@/router';
  import { Icon } from '@/components/swm/Icon';
  import { BasicTable, BasicColumn, useTable } from '@/components/swm/Table';
  import { getDailyAttendanceListData, exportDailyAttendance } from '@/api/swm/dailyAttendance';
  import { useModal } from '@/components/swm/Modal';
  import { FormProps } from '@/components/swm/Form';
  import InputForm from './form.vue';
  import dayjs from 'dayjs';
  import { message } from 'ant-design-vue';
  import { organizationTreeGetNodes } from '@/api/swm/organizationTree';
  import Froms from './forms.vue';

  const { t } = useI18n('swm.swmDify');

  const getTitle = {
    icon: router.currentRoute.value.meta.icon || 'ant-design:book-outlined',
    value: router.currentRoute.value.meta.title || t('安全帽：ai日报表管理'),
  };

  const exportLoading = ref(false);

  const searchForm: FormProps = {
    baseColProps: { lg: 6, md: 8 },
    labelWidth: 90,
    schemas: [
      {
        label: t('员工姓名'),
        field: 'employeeName',
        component: 'Input',
      },
      {
        label: t('考勤日期'),
        field: 'attendanceDate',
        component: 'DatePicker',
        defaultValue: dayjs().format('YYYY-MM-DD'),
        required: true,
        componentProps: {
          valueFormat: 'YYYY-MM-DD',
          allowClear: false,
        },
      },
      {
        field: 'company',
        label: '所属单位',
        component: 'Select',
        componentProps: ({ formModel }) => {
          return {
            api: organizationTreeGetNodes,
            params: { nodeType: 'root' },
            fieldNames: {
              label: 'title',
              value: 'value',
            },
            immediate: true,
            allowClear: true,
            showSearch: true,
            filterOption: (input: string, option: any) => {
              return option.title.toLowerCase().indexOf(input.toLowerCase()) >= 0;
            },
            onChange: () => {
              // 清空所属车间、所属产线、所属班组
              formModel.department = '';
              formModel.prodLine = '';
              formModel.team = '';
            },
          };
        },
      },
      {
        field: 'department',
        label: '所属车间',
        component: 'Select',
        componentProps: ({ formModel }) => {
          return {
            api: organizationTreeGetNodes,
            params: {
              nodeType: 'office',
              parentId: formModel.company || '',
            },
            disabled: !formModel.company,
            immediate: true,
            allowClear: true,
            showSearch: true,
            fieldNames: {
              label: 'title',
              value: 'value',
            },
            filterOption: (input: string, option: any) => {
              return option.title.toLowerCase().indexOf(input.toLowerCase()) >= 0;
            },
            onChange: () => {
              // 清空所属产线、所属班组
              formModel.prodLine = '';
              formModel.team = '';
            },
          };
        },
      },
      {
        field: 'prodLine',
        label: '所属产线',
        component: 'Select',
        componentProps: ({ formModel }) => {
          return {
            api: organizationTreeGetNodes,
            params: {
              nodeType: 'workshop',
              parentId: formModel.department || '',
            },
            disabled: !formModel.department,
            immediate: true,
            allowClear: true,
            showSearch: true,
            fieldNames: {
              label: 'title',
              value: 'value',
            },
            filterOption: (input: string, option: any) => {
              return option.title.toLowerCase().indexOf(input.toLowerCase()) >= 0;
            },
            onChange: () => {
              // 清空所属班组
              formModel.team = '';
            },
          };
        },
      },
      {
        field: 'team',
        label: '所属班组',
        component: 'Select',
        componentProps: ({ formModel }) => {
          return {
            api: organizationTreeGetNodes,
            params: {
              nodeType: 'prodLine',
              parentId: formModel.prodLine || '',
            },
            disabled: !formModel.prodLine,
            immediate: true,
            allowClear: true,
            showSearch: true,
            fieldNames: {
              label: 'title',
              value: 'value',
            },
            filterOption: (input: string, option: any) => {
              return option.title.toLowerCase().indexOf(input.toLowerCase()) >= 0;
            },
          };
        },
      },
      {
        label: t('开机状态'),
        field: 'powerOnStatus',
        component: 'Select',
        componentProps: {
          options: [
            { label: '开机', value: '0' },
            { label: '关机', value: '1' },
          ],
          allowClear: true,
        },
      },
      {
        label: t('班次'),
        field: 'classes',
        component: 'Select',
        componentProps: {
          dictType: 'shift_type_enum',
          allowClear: true,
        },
      },
    ],
    fieldMapToTime: [['date', ['startDate', 'endDate']]],
  };

  const tableColumns: BasicColumn[] = [
    {
      title: '姓名',
      dataIndex: 'employeeName',
      key: 'employeeName',
      width: 120,
      slot: 'firstColumn',
    },
    {
      title: '所属单位',
      dataIndex: 'company',
      width: 180,
    },
    {
      title: '所属车间',
      dataIndex: 'department',
      width: 150,
    },
    {
      title: '所属产线',
      dataIndex: 'prodLine',
      width: 120,
    },
    {
      title: '所属班组',
      dataIndex: 'team',
      width: 120,
    },
    {
      title: '考勤日期',
      dataIndex: 'attendanceDate',
      key: 'attendanceDate',
      width: 120,
      sorter: true,
      customRender: ({ record }) => {
        return record?.attendanceDate ? dayjs(record.attendanceDate).format('YYYY-MM-DD') : '';
      },
    },
    {
      title: '应考勤时间范围',
      dataIndex: 'workTimeRange',
      key: 'workTimeRange',
      width: 150,
      customRender: ({ record }) => {
        return record.workTimeRange || '未排班';
      },
    },
    {
      title: '班次',
      dataIndex: 'classes',
      key: 'classes',
      width: 80,
      dictType: 'shift_type_enum',
    },
    {
      title: '上班打卡时间',
      dataIndex: 'clockInDate',
      key: 'clockInDate',
      width: 180,
      sorter: true,
      customRender: ({ record }) => {
        return {
          children: record.clockInDate,
          props: {
            class: { 'text-[#ff4d4f]': isLate(record) },
          },
        };
      },
    },
    {
      title: '中午下班时间',
      dataIndex: 'noonEndDate',
      key: 'noonEndDate',
      width: 180,
      sorter: true,
    },
    {
      title: '下午上班时间',
      dataIndex: 'afterStartDate',
      key: 'afterStartDate',
      width: 180,
      sorter: true,
    },
    {
      title: '下班打卡时间',
      dataIndex: 'clockOutDate',
      key: 'clockOutDate',
      width: 180,
      sorter: true,
    },
    {
      title: '应考勤时长(h)',
      dataIndex: 'scheduledHours',
      key: 'scheduledHours',
      width: 120,
    },
    {
      title: '有效考勤时长(h)',
      dataIndex: 'actualHours',
      key: 'actualHours',
      width: 120,
    },
    {
      title: '怠工时长(h)',
      dataIndex: 'idleHours',
      key: 'idleHours',
      slots: { customRender: 'idleHours' },
      width: 120,
      sorter: true,
    },
    {
      title: '工作区考勤时长(h)',
      dataIndex: 'effectiveWorkHours',
      key: 'effectiveWorkHours',
      slots: { customRender: 'effectiveWorkHours' },
      width: 130,
      sorter: true,
    },
    {
      title: '考勤状态',
      dataIndex: 'attendanceNormal',
      key: 'attendanceNormal',
      width: 100,
      dictType: 'swm_attendance_status',
    },
    {
      title: '开机状态',
      dataIndex: 'powerOnStatus',
      key: 'powerOnStatus',
      width: 100,
      customRender: ({ record }) => {
        switch (record.powerOnStatus) {
          case '0':
            return '开机';
          case '1':
            return '关机';
          default:
            return '';
        }
      },
    },
  ];

  const actionColumn: BasicColumn = {
    width: 80,
    actions: (record: Recordable) => [
      {
        icon: 'ant-design:eye-outlined',
        title: t('查看'),
        onClick: handleForm.bind(this, { id: record.id }),
      },
    ],
  };

  const [registerModal, { openModal }] = useModal();
  const [registerFormsModal, { openModal: openFormModel }] = useModal();

  const [registerTable, { getForm }] = useTable({
    api: getDailyAttendanceListData,
    beforeFetch: (params) => {
      params.attendanceDate = params.attendanceDate
        ? dayjs(params.attendanceDate).format('YYYY-MM-DD')
        : '';
      return params;
    },
    columns: tableColumns,
    actionColumn: actionColumn,
    formConfig: searchForm,
    showTableSetting: true,
    useSearchForm: true,
    canResize: true,
  });

  // 判断是否迟到
  const isLate = (record: Recordable) => {
    // clockInDate 上班打卡时间 YYYY-MM-DD HH:mm:ss
    // workTimeRange 应考勤时间范围 HH:mm-HH:mm
    if (!record.clockInDate || !record.workTimeRange || !record.attendanceDate) return false;

    // 考勤日期 YYYY-MM-DD
    const attendanceDate = dayjs(record.attendanceDate).format('YYYY-MM-DD');
    // 应考勤时间范围 [HH:mm, HH:mm]
    const [workTimeStart, _workTimeEnd] = record.workTimeRange.split('-');
    // 应考勤开始时间 YYYY-MM-DD HH:mm:ss
    const scheduledStartTime = dayjs(`${attendanceDate} ${workTimeStart}:00`);

    // 上班打卡时间是否晚于应考勤开始时间
    return dayjs(record.clockInDate).isAfter(scheduledStartTime);
  };

  function handleForm(record: Recordable) {
    openModal(true, record);
  }

  function openForms(record: any, type) {
    openFormModel(true, { ...record, types: type });
  }
  // 导出处理
  const handleExport = async () => {
    try {
      const formData = await getForm().validate();

      exportLoading.value = true;

      const res = await exportDailyAttendance({
        ...formData,
        attendanceDate: dayjs(formData.attendanceDate).format('YYYY-MM-DD'),
      });

      if (res && (res.result === true || res.result === 'true') && res.data) {
        // 拼接完整的下载URL
        const baseURL = import.meta.env.VITE_GLOB_API_URL || '';
        const fullDownloadUrl = baseURL + '/swm/' + res.data;

        // 直接打开下载链接
        window.open(fullDownloadUrl, '_blank');
      } else {
        console.error('导出失败，响应数据:', res);
        message.error(res.message || '导出失败');
      }
    } finally {
      exportLoading.value = false;
    }
  };
</script>
