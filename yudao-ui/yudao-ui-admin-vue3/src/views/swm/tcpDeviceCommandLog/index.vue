<template>
  <div class="tcp-device-command-log-container">
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" class="toolbar-button" @click="openDispatchModal">
          下发指令
        </a-button>
        <a-button class="toolbar-button" @click="handleExport" :loading="exportLoading">
          导出当前页
        </a-button>
      </template>

      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'sendMessage'">
          <a-tooltip :title="record.sendMessage">
            <div class="message-content-cell">
              {{ record.sendMessage }}
            </div>
          </a-tooltip>
        </template>
        <template v-if="column.dataIndex === 'errorMessage'">
          <a-tooltip :title="record.errorMessage">
            <div class="message-content-cell">
              {{ record.errorMessage }}
            </div>
          </a-tooltip>
        </template>
      </template>
    </BasicTable>

    <DispatchModal v-model:visible="dispatchModalVisible" />
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { BasicTable, useTable } from '@/components/swm/Table';
  import {
    getTcpDeviceCommandLogList,
    exportTcpDeviceCommandLog,
  } from '@/api/swm/tcpDeviceCommandLog';
  import { message } from 'ant-design-vue';
  import { downloadByData } from '@/utils/file/download';
  import DispatchModal from './components/DispatchModal.vue';
  import moment from 'moment';

  export default defineComponent({
    name: 'ViewsSwmTcpDeviceCommandLogIndex',
    components: {
      BasicTable,
      DispatchModal,
    },
    setup() {
      const exportLoading = ref(false);
      const currentPage = ref(1);
      const currentPageSize = ref(1000);
      const dispatchModalVisible = ref(false);

      const openDispatchModal = () => {
        dispatchModalVisible.value = true;
      };

      const [registerTable, { getForm }] = useTable({
        title: '下发指令到设备日志',
        api: getTcpDeviceCommandLogList,
        beforeFetch: (params) => {
          if (params.pageNo) currentPage.value = params.pageNo;
          if (params.pageSize) currentPageSize.value = params.pageSize;

          if (params.timeRange && params.timeRange.length === 2) {
            params.startTime = params.timeRange[0];
            params.endTime = params.timeRange[1];
            delete params.timeRange;
          }

          if (params.deviceId && Array.isArray(params.deviceId)) {
            const validDeviceIds = [...new Set(params.deviceId.filter((id) => id && id.trim()))];
            params.deviceId = validDeviceIds.join(',');
          }

          if (params.identityCard && Array.isArray(params.identityCard)) {
            const validIdentityCards = [
              ...new Set(params.identityCard.filter((card) => card && card.trim())),
            ];
            params.identityCard = validIdentityCards.join(',');
          }

          return params;
        },
        afterFetch: (result) => result,
        rowKey: (record, index) => `${record.time}_${record.deviceId}_${index}`,
        useSearchForm: true,
        formConfig: {
          baseColProps: { lg: 6, md: 8 },
          labelWidth: 100,
          schemas: [
            {
              field: 'deviceId',
              label: '设备ID',
              component: 'Select',
              componentProps: {
                mode: 'tags',
                placeholder: '请输入设备ID，支持多个（回车或逗号分隔）',
                allowClear: true,
                maxTagCount: 10,
                maxTagTextLength: 20,
                tokenSeparators: [',', '，', ' ', '\n'],
                filterOption: false,
                notFoundContent: null,
                style: { width: '100%' },
              },
              colProps: { span: 6 },
            },
            {
              field: 'identityCard',
              label: '身份证号',
              component: 'Select',
              componentProps: {
                mode: 'tags',
                placeholder: '请输入身份证号，支持多个（回车或逗号分隔）',
                allowClear: true,
                maxTagCount: 10,
                maxTagTextLength: 18,
                tokenSeparators: [',', '，', ' ', '\n'],
                filterOption: false,
                notFoundContent: null,
                style: { width: '100%' },
              },
              colProps: { span: 6 },
            },
            {
              field: 'timeRange',
              label: '时间范围',
              component: 'RangePicker',
              componentProps: {
                showTime: true,
                format: 'YYYY-MM-DD HH:mm:ss',
                valueFormat: 'YYYY-MM-DD HH:mm:ss',
                placeholder: ['开始时间', '结束时间'],
              },
              defaultValue: [
                moment().subtract(1, 'days').format('YYYY-MM-DD HH:mm:ss'),
                moment().format('YYYY-MM-DD HH:mm:ss'),
              ],
              colProps: { span: 8 },
            },
            {
              field: 'sortOrder',
              label: '时间排序',
              component: 'Select',
              componentProps: {
                placeholder: '选择排序方式',
                options: [
                  { label: '降序（最新在前）', value: 'DESC' },
                  { label: '升序（最早在前）', value: 'ASC' },
                ],
              },
              defaultValue: 'DESC',
              colProps: { span: 4 },
            },
          ],
        },
        columns: [
          {
            title: '序号',
            dataIndex: 'index',
            width: 80,
            fixed: 'left',
            customRender: ({ index }) =>
              (currentPage.value - 1) * currentPageSize.value + index + 1,
          },
          { title: 'time', dataIndex: 'timeText', width: 180 },
          { title: 'send_message', dataIndex: 'sendMessage', width: 300, ellipsis: true },
          { title: 'send_status', dataIndex: 'sendStatus', width: 120 },
          { title: 'error_message', dataIndex: 'errorMessage', width: 250, ellipsis: true },
          { title: 'identity_card', dataIndex: 'identityCard', width: 180 },
          { title: 'person_name', dataIndex: 'personName', width: 120 },
          { title: 'device_id', dataIndex: 'deviceId', width: 150 },
        ],
        pagination: {
          pageSize: 1000,
          showSizeChanger: true,
          showQuickJumper: true,
          pageSizeOptions: ['100', '500', '1000', '5000', '10000'],
          showTotal: (total) => `共 ${total} 条数据`,
        },
        scroll: {
          x: 1400,
          y: 'calc(100vh - 300px)',
        },
        showTableSetting: true,
        bordered: true,
        canResize: false,
        immediate: true,
        showIndexColumn: false,
        fetchSetting: {
          pageField: 'pageNo',
          sizeField: 'pageSize',
          listField: 'list',
          totalField: 'count',
        },
      });

      const handleExport = async () => {
        exportLoading.value = true;
        try {
          const form = getForm();
          const formValues = await form.getFieldsValue();

          const exportParams: Record<string, any> = { ...formValues };
          if (formValues.timeRange && formValues.timeRange.length === 2) {
            exportParams.startTime = formValues.timeRange[0];
            exportParams.endTime = formValues.timeRange[1];
            delete exportParams.timeRange;
          }

          if (formValues.deviceId && Array.isArray(formValues.deviceId)) {
            const validDeviceIds = [
              ...new Set(formValues.deviceId.filter((id) => id && id.trim())),
            ];
            exportParams.deviceId = validDeviceIds.join(',');
          }

          if (formValues.identityCard && Array.isArray(formValues.identityCard)) {
            const validIdentityCards = [
              ...new Set(formValues.identityCard.filter((card) => card && card.trim())),
            ];
            exportParams.identityCard = validIdentityCards.join(',');
          }

          exportParams.pageNo = currentPage.value;
          exportParams.pageSize = currentPageSize.value;

          const data = await exportTcpDeviceCommandLog(exportParams);
          downloadByData(data, `tcp_device_command_log_${new Date().getTime()}.xlsx`);
          message.success('导出成功');
        } catch (error) {
          console.error('导出失败:', error);
          message.error('导出失败');
        } finally {
          exportLoading.value = false;
        }
      };

      return {
        registerTable,
        exportLoading,
        handleExport,
        dispatchModalVisible,
        openDispatchModal,
      };
    },
  });
</script>

<style lang="less" scoped>
  .tcp-device-command-log-container {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .message-content-cell {
    max-width: 280px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .toolbar-button {
    margin-right: 12px;
  }

  .toolbar-button:last-child {
    margin-right: 0;
  }

  :deep(.ant-table-tbody > tr > td) {
    white-space: nowrap;
  }
</style>
