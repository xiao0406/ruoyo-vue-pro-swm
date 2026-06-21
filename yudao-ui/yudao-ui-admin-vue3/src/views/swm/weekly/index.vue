<template>
  <div>
    <BasicTable @register="registerTable">
      <template #tableTitle>
        <Icon :icon="getTitle.icon" class="m-1 pr-1" />
        <span> {{ getTitle.value }} </span>
      </template>
      <template #toolbar>
        <a-button type="primary" @click="handleExport" :loading="exportLoading">
          <Icon icon="ant-design:export-outlined" /> {{ t('导出') }}
        </a-button>
      </template>

      <!-- 员工姓名列 - 可点击查看详情 -->
      <template #employeeNameColumn="{ record }">
        <!-- <a @click="handleViewDetail(record)"> -->
        {{ record.employeeName }}
        <!-- </a> -->
      </template>

      <!-- 考勤状态列 -->
      <template #attendanceNormalColumn="{ record }">
        <DictLabel dictType="swm_attendance_status" :dictValue="record.attendanceNormal" />
      </template>

      <!-- 班次列 -->
      <template #classesColumn="{ record }">
        <DictLabel dictType="shift_type_enum" :dictValue="record.classes" />
      </template>

      <!-- 迟到标记列 -->
      <template #clockInDateColumn="{ record }">
        <span :class="{ 'text-danger': isLate(record) }">
          {{ formatDateTime(record.clockInDate) }}
        </span>
      </template>
    </BasicTable>

    <EditModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts">
  import { defineComponent } from 'vue';
  export default defineComponent({
    name: 'ViewsSwmDailyAttendanceIndex',
  });
</script>

<script lang="ts" setup>
  import { ref, reactive } from 'vue';
  import { useI18n } from '@/hooks/swm/useI18n';
  import { router } from '@/router';
  import { Icon } from '@/components/swm/Icon';
  import { BasicTable, BasicColumn, useTable } from '@/components/swm/Table';
  import { useModal } from '@/components/swm/Modal';
  import { useDict } from '@/components/swm/Dict';
  import { DictLabel } from '@/components/swm/Dict';
  import { FormProps } from '@/components/swm/Form';
  import { message } from 'ant-design-vue';
  import { defHttp } from '@/utils/http/axios';
  import dayjs from 'dayjs';
  import { downloadByData } from '@/utils/file/download';
  import { exportWeeklyListData } from '@/api/swm/weekly';
  import EditModal from './components/EditModal.vue';
  import { dictDataListData } from '@/api/swm/dictData'; // 获取人员类型

  const { t } = useI18n('swm.dailyAttendance');
  const exportLoading = ref<boolean>(false);

  // 初始化字典
  const { initDict } = useDict();
  initDict(['swm_attendance_status', 'swm_current_position', 'shift_type_enum']);

  const getTitle = {
    icon: router.currentRoute.value.meta.icon || 'ant-design:book-outlined',
    value: router.currentRoute.value.meta.title || t('月考勤记录'),
  };

  // 搜索表单配置
  const searchForm: FormProps = {
    baseColProps: { lg: 6, md: 8 },
    labelWidth: 90,
    schemas: [
      {
        label: t('员工姓名'),
        field: 'employeeName',
        component: 'Input',
        componentProps: {
          placeholder: t('请输入员工姓名'),
        },
      },
      {
        label: t('开始时间'),
        field: 'startDate',
        component: 'DatePicker',
        componentProps: {
          format: 'YYYY-MM-DD',
          valueFormat: 'YYYY-MM-DD',
          placeholder: t('请选择开始日期'),
          style: { width: '100%' },
          allowClear: false,
          defaultValue: (() => {
            // 获取本周周一（如果今天是周日，dayjs的startOf('week')返回周日，所以需要调整）
            const today = dayjs();
            const dayOfWeek = today.day(); // 0是周日，1是周一，... 6是周六

            if (dayOfWeek === 0) {
              // 如果是周日，本周周一是6天前
              return today.subtract(6, 'day').format('YYYY-MM-DD');
            } else {
              // 其他情况，本周周一是 (dayOfWeek - 1) 天前
              return today.subtract(dayOfWeek - 1, 'day').format('YYYY-MM-DD');
            }
          })(),
        },
        defaultValue: (() => {
          const today = dayjs();
          const dayOfWeek = today.day();

          if (dayOfWeek === 0) {
            return today.subtract(6, 'day').format('YYYY-MM-DD');
          } else {
            return today.subtract(dayOfWeek - 1, 'day').format('YYYY-MM-DD');
          }
        })(),
      },
      {
        label: t('结束时间'),
        field: 'endDate',
        component: 'DatePicker',
        componentProps: {
          format: 'YYYY-MM-DD',
          valueFormat: 'YYYY-MM-DD',
          placeholder: t('请选择结束日期'),
          style: { width: '100%' },
          allowClear: false,
          defaultValue: dayjs().format('YYYY-MM-DD'),
          disabledDate: (currentDate: any) => {
            const form = getForm?.();
            if (form) {
              const values = form.getFieldsValue();
              if (values.startDate) {
                return (
                  currentDate &&
                  (currentDate < dayjs(values.startDate) || currentDate > dayjs().endOf('day'))
                );
              }
            }
            return currentDate && currentDate > dayjs().endOf('day');
          },
        },
        defaultValue: dayjs().format('YYYY-MM-DD'),
      },
      {
        label: t('人员类型'),
        field: 'personType',
        component: 'Select',
        // componentProps: {
        //   dictType: 'person_type_enum',
        //   placeholder: t('请输入员工姓名'),
        // },
        componentProps: () => {
          return {
            api: dictDataListData,
            params: { dictType: 'person_type_enum' },
            fieldNames: {
              label: 'dictLabel',
              value: 'dictValue',
            },
            immediate: true,
            allowClear: true,
            showSearch: true,
            filterOption: (input: string, option: any) => {
              return option.title.toLowerCase().indexOf(input.toLowerCase()) >= 0;
            },
          };
        },
      },
    ],
  };

  // 表格列配置
  const tableColumns: BasicColumn[] = [
    {
      title: t('姓名'),
      dataIndex: 'employeeName',
      key: 'employeeName',
      width: 120,
      align: 'left',
      fixed: 'left',
      slots: { customRender: 'employeeNameColumn' },
    },
    {
      title: t('手机号码'),
      dataIndex: 'phoneNumber',
      key: 'phoneNumber',
      width: 150,
      align: 'left',
    },
    {
      title: t('班组'),
      dataIndex: 'team',
      key: 'team',
      width: 80,
      align: 'left',
    },
    {
      title: t('工种'),
      dataIndex: 'jobType',
      key: 'jobType',
      width: 150,
      sorter: true,
      align: 'left',
    },
    {
      title: t('出勤天数(天)'),
      dataIndex: 'attendanceDay',
      key: 'attendanceDay',
      width: 150,
      sorter: true,
      align: 'right',
    },
    {
      title: t('周出勤率'),
      dataIndex: 'monthlyAttendanceRate',
      key: 'monthlyAttendanceRate',
      width: 120,
      align: 'right',
    },
    {
      title: t('有效考勤天数(天)'),
      dataIndex: 'validAttendanceDays',
      key: 'validAttendanceDays',
      width: 120,
      align: 'right',
    },
    {
      title: t('本周有效考勤时长(h)'),
      dataIndex: 'actualHours',
      key: 'actualHours',
      width: 120,
      sorter: true,
      align: 'right',
    },
    {
      title: t('本周怠工时长(h)'),
      dataIndex: 'idleHours',
      key: 'idleHours',
      width: 130,
      sorter: true,
      align: 'right',
    },
  ];

  const [registerModal, { openModal }] = useModal();

  const [registerTable, { reload, getForm }] = useTable({
    api: (params) => {
      return defHttp.get({
        url: '/swm/swmDailyAttendance/weeklyListData',
        params,
      });
    },
    beforeFetch: (params) => {
      return {
        ...params,
        pageNo: params.pageNo || 1,
        pageSize: params.pageSize || 10,
      };
    },
    columns: tableColumns,
    formConfig: searchForm,
    showTableSetting: true,
    useSearchForm: true,
    canResize: true,
    pagination: {
      pageSize: 10,
      pageSizeOptions: ['10', '20', '50', '100'],
      showSizeChanger: true,
      showQuickJumper: true,
    },
  });

  // 查看详情
  function handleViewDetail(record: Recordable) {
    openModal(true, {
      record,
    });
  }

  // 操作成功刷新
  function handleSuccess() {
    reload();
  }

  // 导出Excel
  // 导出Excel - 按照提供的示例修改
  // 导出Excel
  async function handleExport() {
    try {
      // 获取当前搜索条件
      const formInstance = getForm?.();
      if (!formInstance) {
        message.warning(t('无法获取搜索条件'));
        return;
      }

      const searchParams = await formInstance.getFieldsValue();

      if (!searchParams?.startDate && !searchParams?.endDate) {
        message.warning(t('请先选择考勤月份'));
        return;
      }

      exportLoading.value = true;

      const res = await defHttp.get({
        url: '/swm/swmDailyAttendance/exportWeeklyListData',
        params: {
          ...searchParams,
        },
      });

      if (res && (res.result === true || res.result === 'true') && res.data) {
        console.log(res, 'resres');
        const downloadUrl = res.data;

        // 创建隐藏的链接进行下载
        const link = document.createElement('a');
        link.href = downloadUrl;
        link.target = '_blank'; // 新窗口打开
        link.style.display = 'none';

        // 可以尝试从URL中提取文件名，如果没有就使用默认文件名
        const fileName =
          downloadUrl.split('/').pop() || `安全帽设备列表_${new Date().getTime()}.xlsx`;
        link.download = fileName;

        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        message.success(t('导出成功，文件正在下载...'));
      } else {
        message.error(res?.message || t('导出失败'));
      }
    } catch (error) {
      console.error(t('导出失败'), error);
      message.error(t('导出失败'));
    } finally {
      exportLoading.value = false;
    }
  }
  // 判断是否迟到
  const isLate = (record: any) => {
    if (!record.clockInDate || !record.workTimeRange) return false;

    const clockInTime = dayjs(record.clockInDate);
    const workTimeStart = record.workTimeRange?.split('-')[0];

    if (!workTimeStart) return false;

    const [hour, minute] = workTimeStart.split(':').map(Number);
    const scheduledStartTime = dayjs(record.attendanceDate).hour(hour).minute(minute).second(0);

    return clockInTime.isAfter(scheduledStartTime);
  };

  // 格式化日期时间
  const formatDateTime = (time: string) => {
    if (!time) return '-';
    return dayjs(time).format('YYYY-MM-DD HH:mm:ss');
  };
</script>

<style lang="less" scoped>
  .text-danger {
    color: #ff4d4f;
  }
</style>
