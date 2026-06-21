<!--
  @author zwf
  @date 2025-05-16
-->
<template>
  <div class="warning-record-list-container">
    <BasicTable @register="registerTable">
      <template #toolbar>
        <!-- <a-button type="primary" @click="handleAdd" class="ml-2">
          <PlusOutlined />新增
        </a-button> -->
      </template>

      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'warningContent'">
          <DictLabel
            dictType="warning_content_enum"
            :dictValue="record.warningContent"
            defaultValue="--"
          />
        </template>
        <template v-else-if="column.key === 'handleStatus'">
          <Tag :color="getHandleStatusColor(record.handleStatus)">
            {{ record.handleStatusText || getHandleStatusText(record.handleStatus) }}
          </Tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <TableAction :actions="getActions(record)" />
        </template>
      </template>
    </BasicTable>
    <WarningRecordDetailModal @register="registerDetailModal" @success="handleSuccess" />
    <WarningProcessModal @register="registerProcessModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref, computed } from 'vue';
  import { BasicTable, useTable, TableAction } from '@/components/swm/Table';
  import {
    getWarningRecordList,
    exportWarningRecordList,
    getUnhandledWarnings,
  } from '@/api/swm/warningRecord';
  import { useModal } from '@/components/swm/Modal';
  import WarningRecordDetailModal from './WarningRecordDetailModal.vue';
  import WarningProcessModal from './WarningProcessModal.vue';
  import { Icon } from '@/components/swm/Icon';
  import { Tag } from 'ant-design-vue';
  import { PlusOutlined } from '@ant-design/icons-vue';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { router } from '@/router';
  import { DictLabel, useDict } from '@/components/swm/Dict';
  import dayjs from 'dayjs';

  export default defineComponent({
    name: 'ViewsSwmWarningRecordWarningRecordList',
    components: {
      BasicTable,
      WarningRecordDetailModal,
      WarningProcessModal,
      TableAction,
      Icon,
      Tag,
      PlusOutlined,
      DictLabel,
    },
    setup() {
      const { createMessage } = useMessage();
      const { initDict } = useDict();

      // 初始化字典数据
      initDict(['warning_content_enum']);

      const [registerDetailModal, { openModal: openDetailModal }] = useModal();
      const [registerProcessModal, { openModal: openProcessModal }] = useModal();
      const loading = ref(false);
      //标题和上面导航栏保持一致
      const pageTitle = computed(() => {
        // 假设你通过路由元信息定义了页面标题
        return router.currentRoute.value.meta.title || '报警处置记录';
      });
      const [registerTable, { reload }] = useTable({
        title: pageTitle.value,
        api: getWarningRecordList,
        beforeFetch: (params) => {
          if (params.timeRange && params?.timeRange?.length === 2) {
            params.timeRange[0] = dayjs(params.timeRange[0])
              .startOf('day')
              .format('YYYY-MM-DD HH:mm:ss');
            params.timeRange[1] = dayjs(params.timeRange[1])
              .endOf('day')
              .format('YYYY-MM-DD HH:mm:ss');
          }
          return params;
        },
        rowKey: 'id',
        fetchSetting: {
          pageField: 'pageNum',
        },
        pagination: {
          showSizeChanger: true,
          showQuickJumper: true,
          pageSize: 10,
          pageSizeOptions: ['10', '20', '50', '100'],
        },
        columns: [
          {
            title: '报警单号',
            dataIndex: 'warningId',
            width: 180,
          },
          {
            title: '处置记录名称',
            dataIndex: 'recordName',
            width: 180,
          },
          {
            title: '报警记录',
            dataIndex: 'warningRecord',
            width: 120,
          },
          {
            title: '报警类型',
            dataIndex: 'warningContent',
            key: 'warningContent',
            width: 120,
          },
          {
            title: '报警时间',
            dataIndex: 'alarmTime',
            width: 180,
          },
          // {
          //   title: '位置',
          //   dataIndex: 'location',
          //   width: 120,
          //   customRender: ({ text }) => text || '--',
          // },
          // {
          //   title: '区域',
          //   dataIndex: 'area',
          //   width: 120,
          //   customRender: ({ text }) => text || '--',
          // },
          {
            title: '处置人',
            dataIndex: 'handler',
            width: 120,
          },
          {
            title: '处置方案',
            dataIndex: 'handleProcess',
            width: 180,
          },
          {
            title: '处置时间',
            dataIndex: 'handleTime',
            width: 180,
          },
          {
            title: '状态',
            dataIndex: 'handleStatus',
            key: 'handleStatus',
            width: 100,
          },
          {
            title: '操作',
            dataIndex: 'action',
            key: 'action',
            width: 120,
            fixed: 'right',
          },
        ],
        showTableSetting: true,
        useSearchForm: true,
        formConfig: {
          baseColProps: { lg: 6, md: 8 },
          labelWidth: 100,
          schemas: [
            {
              field: 'warningId',
              label: '报警单号',
              component: 'Input',
              colProps: { span: 8 },
            },
            {
              field: 'recordName',
              label: '记录名称',
              component: 'Input',
              colProps: { span: 8 },
            },
            {
              field: 'warningRecord',
              label: '报警记录',
              component: 'Input',
              colProps: { span: 8 },
            },
            {
              field: 'warningContent',
              label: '报警类型',
              component: 'Select',
              componentProps: {
                dictType: 'warning_content_enum',
                allowClear: true,
              },
              colProps: { span: 8 },
            },
            {
              field: 'handler',
              label: '处置人',
              component: 'Input',
              colProps: { span: 8 },
            },

            {
              field: 'handleStatus',
              label: '处置状态',
              component: 'Select',
              componentProps: {
                options: [
                  { label: '全部', value: '' },
                  { label: '已完成', value: '1' },
                  { label: '草稿', value: '2' },
                  { label: '未处置', value: '0' },
                ],
              },
              colProps: { span: 8 },
              defaultValue: '',
            },
            {
              field: 'timeRange',
              label: '处置时间',
              component: 'RangePicker',
              componentProps: {
                // showTime: true,
                style: { width: '100%' },
              },
              colProps: { span: 8 },
            },
          ],
        },
        canResize: true,
      });

      function getHandleStatusColor(status) {
        if (status === '1') {
          return 'success';
        } else if (status === '2') {
          return 'cyan';
        } else if (status === '0') {
          return 'warning';
        }
        return 'default';
      }

      function getHandleStatusText(status) {
        if (status === '1') {
          return '已完成';
        } else if (status === '2') {
          return '草稿';
        } else {
          return '草稿';
        }
      }

      function handleViewDetail(record: Recordable) {
        openDetailModal(true, {
          record,
          isView: true,
        });
      }

      function handleEdit(record: Recordable) {
        // 处理编辑逻辑
        openDetailModal(true, {
          record,
          isView: false,
        });
      }

      function handleAdd() {
        // 直接打开处置模态框，不再重复请求数据
        // 模态框内部会自动获取未处置报警数据
        openProcessModal(true, {});
      }

      function getActions(record: Recordable) {
        // 根据状态返回不同的操作按钮
        const actions = [
          {
            icon: 'ant-design:eye-outlined',
            tooltip: '查看',
            onClick: handleViewDetail.bind(null, record),
          },
        ];

        // 如果状态是草稿（未处置或状态为2），添加编辑按钮
        if (record.handleStatus === '0' || record.handleStatus === '2') {
          actions.unshift({
            icon: 'clarity:note-edit-line',
            tooltip: '编辑',
            onClick: handleEdit.bind(null, record),
          });
        }

        return actions;
      }

      function handleExport() {
        const params = {};
        exportWarningRecordList(params)
          .then((res) => {
            const blob = new Blob([res.data], { type: 'application/vnd.ms-excel' });
            const url = URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            const today = new Date();
            const year = today.getFullYear();
            const month = String(today.getMonth() + 1).padStart(2, '0');
            const day = String(today.getDate()).padStart(2, '0');
            const fileName = `报警处置记录_${year}-${month}-${day}.xlsx`;
            link.setAttribute('download', fileName);
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            URL.revokeObjectURL(url);
            createMessage.success('导出成功');
          })
          .catch(() => {
            createMessage.error('导出失败');
          });
      }

      function handleSuccess() {
        reload();
      }

      return {
        registerTable,
        registerDetailModal,
        registerProcessModal,
        loading,
        handleViewDetail,
        handleEdit,
        handleAdd,
        handleSuccess,
        handleExport,
        getHandleStatusColor,
        getHandleStatusText,
        getActions,
      };
    },
  });
</script>

<style lang="less" scoped>
  .warning-record-list-container {
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
