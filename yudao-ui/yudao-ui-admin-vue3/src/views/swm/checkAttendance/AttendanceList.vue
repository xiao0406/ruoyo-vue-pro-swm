<template>
  <div class="personnel-list-container">
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleExport">
          <Icon icon="ant-design:download-outlined" />导出
        </a-button>
      </template>
      <template #action="{ record }">
        <TableAction
          :actions="[
            {
              icon: 'ant-design:file-text-outlined',
              tooltip: '查看',
              onClick: handleView.bind(null, record),
            },
          ]"
        />
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'workShift'">
          <DictLabel dictType="shift_type_enum" :dictValue="record.workShift" />
        </template>
      </template>
    </BasicTable>
    <ViewModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref, onMounted, computed } from 'vue';
  import { useGlobSetting } from '@/hooks/setting';
  import { downloadByUrl } from '@/utils/file/download';
  import { BasicTable, useTable, TableAction } from '@/components/swm/Table';
  import { useModal } from '@/components/swm/Modal';
  import ViewModal from './viewModal.vue';
  import { monthEnum } from '@/enums/swm/beaconEnum';
  import { Icon } from '@/components/swm/Icon';
  import { defHttp } from '@/utils/http/axios';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { DictLabel, useDict } from '@/components/swm/Dict';
  import dayjs from 'dayjs';
  import { router } from '@/router';

  // 定义考勤记录接口
  interface AttendanceRecord {
    id: string;
    employeeName: string;
    department: string;
    workProcess: string;
    team: string;
    jobType: string;
    workShift: string;
    month: string;
    scheduledDays: number;
    actualDays: number;
    attendanceRate: number;
    scheduledHours: number;
    actualHours: number;
    attendanceAchievementRate: number;
    idleHours: number;
    efficiency: number;
    [key: string]: any;
  }

  // 获取考勤记录列表
  const getAttendanceList = (params?: any) =>
    defHttp.get({
      url: '/swm/swmAttendanceSummary/listData',
      params,
    });

  // 导出考勤记录
  const exportAttendanceList = (params?: any) =>
    defHttp.get(
      {
        url: '/swm/swmAttendanceSummary/exportData',
        params,
        responseType: 'blob',
      },
      { isReturnNativeResponse: true },
    );

  export default defineComponent({
    name: 'AttendanceList',
    components: { BasicTable, ViewModal, TableAction, Icon, DictLabel },
    setup() {
      const { createMessage } = useMessage();
      const checkedKeys = ref<Array<string>>([]);
      const [registerModal, { openModal }] = useModal();

      // 初始化字典数据
      const { initDict } = useDict();
      onMounted(async () => {
        await initDict(['shift_type_enum']);
      });
      //标题和上面导航栏保持一致
      const pageTitle = computed(() => {
        // 假设你通过路由元信息定义了页面标题
        return router.currentRoute.value.meta.title || '出勤考勤记录';
      });
      const [registerTable, { reload }] = useTable({
        title: pageTitle.value,
        api: getAttendanceList,
        rowKey: 'id',
        fetchSetting: {
          pageField: 'pageNo',
          sizeField: 'pageSize',
          listField: 'list',
          totalField: 'count',
        },
        beforeFetch: (params) => {
          // 确保月份格式正确
          if (params.month && typeof params.month === 'string') {
            // 如果是日期时间格式，只保留年月部分
            if (params.month.length > 7) {
              params.month = params.month.substring(0, 7);
            }
          }
          console.log('发送到后端的参数:', params);
          return params;
        },
        pagination: {
          showSizeChanger: true,
          showQuickJumper: true,
          pageSize: 10,
          pageSizeOptions: ['10', '20', '50', '100'],
        },
        columns: [
          {
            title: '员工',
            dataIndex: 'employeeName',
            width: 120,
          },
          {
            title: '所属车间',
            dataIndex: 'department',
            width: 120,
          },
          {
            title: '所属工序',
            dataIndex: 'workProcess',
            width: 80,
          },
          {
            title: '所属班组',
            dataIndex: 'team',
            width: 180,
          },
          {
            title: '工种',
            dataIndex: 'jobType',
            width: 150,
          },
          {
            title: '所属班次',
            dataIndex: 'workShift',
            width: 120,
          },
          {
            title: '应出勤天数（天）',
            dataIndex: 'scheduledDays',
            width: 120,
          },
          {
            title: '实际出勤天数（天）',
            dataIndex: 'actualDays',
            width: 120,
          },
          {
            title: '出勤率',
            dataIndex: 'attendanceRate',
            width: 150,
            customRender: ({ text }) => {
              return text ? (text * 100).toFixed(2) + '%' : '';
            },
          },
          {
            title: '应考勤时间（h）',
            dataIndex: 'scheduledHours',
            width: 150,
          },
          {
            title: '实际工作时间（h）',
            dataIndex: 'actualHours',
            width: 180,
          },
          {
            title: '考勤达成率',
            dataIndex: 'attendanceAchievementRate',
            width: 120,
            customRender: ({ text }) => {
              return text ? (text * 100).toFixed(2) + '%' : '';
            },
          },
          {
            title: '怠工时长（h）',
            dataIndex: 'idleHours',
            width: 120,
          },
          {
            title: '工效',
            dataIndex: 'efficiency',
            width: 120,
            customRender: ({ text }) => {
              return text ? (text * 100).toFixed(2) + '%' : '';
            },
          },
        ],
        actionColumn: {
          width: 200,
          title: '操作',
          dataIndex: 'action',
          slots: { customRender: 'action' },
        },
        showTableSetting: true,
        useSearchForm: true,
        formConfig: {
          baseColProps: { lg: 6, md: 8 },
          labelWidth: 100,
          schemas: [
            {
              labelWidth: 36,
              field: 'month',
              label: '月份',
              component: 'MonthPicker',
              defaultValue: dayjs().format('YYYY-MM'),
              componentProps: {
                valueFormat: 'YYYY-MM',
                format: 'YYYY-MM',
                placeholder: '请选择月份',
                style: { width: '100%' },
                onChange: (value) => {
                  console.log('选择的月份值:', value);
                },
              },
              colProps: { span: 6 },
            },
            {
              field: 'employeeName',
              label: '员工名称',
              component: 'Input',
              colProps: { span: 6 },
            },
            {
              field: 'team',
              label: '班组',
              component: 'Input',
              colProps: { span: 6 },
            },
          ],
        },
        rowSelection: {
          onChange: (selectedRowKeys) => {
            checkedKeys.value = selectedRowKeys as string[];
          },
        },
        canResize: true,
      });

      function handleSuccess() {
        reload();
      }

      // 查看
      function handleView(record: Recordable) {
        openModal(true, {
          record,
          isUpdate: true,
        });
      }

      // 导出
      async function handleExport() {
        try {
          const res = await exportAttendanceList();
          const blob = new Blob([res.data], { type: 'application/vnd.ms-excel' });
          const url = URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          const today = new Date();
          const year = today.getFullYear();
          const month = String(today.getMonth() + 1).padStart(2, '0');
          const day = String(today.getDate()).padStart(2, '0');
          const fileName = `出勤考勤记录_${year}-${month}-${day}.xlsx`;
          link.setAttribute('download', fileName);
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          URL.revokeObjectURL(url);
          createMessage.success('导出成功');
        } catch (error) {
          createMessage.error('导出失败');
        }
      }

      return {
        registerTable,
        registerModal,
        handleSuccess,
        checkedKeys,
        handleView,
        handleExport,
      };
    },
  });
</script>

<style lang="less" scoped>
  .personnel-list-container {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .page-header {
    margin-bottom: 16px;
  }

  .page-title {
    font-size: 18px;
    font-weight: bold;
    color: #303133;
  }
</style>
