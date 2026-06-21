<template>
  <div class="raw-message-log-container">
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleExport" :loading="exportLoading">
          导出当前页
        </a-button>
      </template>

      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'messageContent'">
          <a-tooltip :title="record.messageContent">
            <div class="message-content-cell">
              {{ record.messageContent }}
            </div>
          </a-tooltip>
        </template>
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref, reactive, onMounted } from 'vue';
  import { BasicTable, useTable } from '@/components/swm/Table';
  import { getRawMessageLogList, exportRawMessageLog } from '@/api/swm/rawMessageLog';
  import type { RawMessageLogQuery } from '@/api/swm/rawMessageLog';
  import { message } from 'ant-design-vue';
  import { downloadByData } from '@/utils/file/download';
  import moment from 'moment';

  export default defineComponent({
    name: 'ViewsSwmRawMessageLogIndex',
    components: {
      BasicTable,
    },
    setup() {
      // 加载状态
      const exportLoading = ref(false);

      // 分页信息
      const currentPage = ref(1);
      const currentPageSize = ref(1000);

      // 表格配置
      const [registerTable, { reload, getForm, setProps }] = useTable({
        title: '设备上传原始消息日志',
        api: getRawMessageLogList,
        beforeFetch: (params) => {
          // 记录分页参数
          if (params.pageNo) currentPage.value = params.pageNo;
          if (params.pageSize) currentPageSize.value = params.pageSize;

          // 处理时间范围参数
          if (params.timeRange && params.timeRange.length === 2) {
            params.startTime = params.timeRange[0];
            params.endTime = params.timeRange[1];
            delete params.timeRange;
          }

          // 处理多设备ID参数
          if (params.deviceId && Array.isArray(params.deviceId)) {
            // 过滤空值和重复值，然后转换为逗号分隔的字符串
            const validDeviceIds = [...new Set(params.deviceId.filter((id) => id && id.trim()))];
            params.deviceId = validDeviceIds.join(',');
          }

          return params;
        },
        afterFetch: (result) => {
          return result;
        },
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
            customRender: ({ index }) => {
              // 正确计算跨页序号：(当前页码-1) × 每页条数 + 当前页内索引 + 1
              return (currentPage.value - 1) * currentPageSize.value + index + 1;
            },
          },
          {
            title: 'time',
            dataIndex: 'timeText',
            width: 180,
          },
          {
            title: 'session_id',
            dataIndex: 'sessionId',
            width: 200,
          },
          {
            title: 'message_time',
            dataIndex: 'messageTime',
            width: 150,
          },
          {
            title: 'message_content',
            dataIndex: 'messageContent',
            width: 300,
            ellipsis: true,
          },
          {
            title: 'original_length',
            dataIndex: 'originalLength',
            width: 120,
          },
          {
            title: 'is_truncated',
            dataIndex: 'isTruncatedText',
            width: 120,
          },
          {
            title: 'device_id',
            dataIndex: 'deviceId',
            width: 150,
          },
        ],
        pagination: {
          pageSize: 1000,
          showSizeChanger: true,
          showQuickJumper: true,
          pageSizeOptions: ['100', '500', '1000', '5000', '10000'],
          showTotal: (total) => `共 ${total} 条数据`,
        },
        scroll: {
          x: 1200,
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

      // 导出
      const handleExport = async () => {
        exportLoading.value = true;
        try {
          const form = getForm();
          const formValues = await form.getFieldsValue();

          // 处理时间范围参数
          const exportParams: any = { ...formValues };
          if (formValues.timeRange && formValues.timeRange.length === 2) {
            exportParams.startTime = formValues.timeRange[0];
            exportParams.endTime = formValues.timeRange[1];
            delete exportParams.timeRange;
          }

          // 处理多设备ID参数
          if (formValues.deviceId && Array.isArray(formValues.deviceId)) {
            // 过滤空值和重复值，然后转换为逗号分隔的字符串
            const validDeviceIds = [
              ...new Set(formValues.deviceId.filter((id) => id && id.trim())),
            ];
            exportParams.deviceId = validDeviceIds.join(',');
          }

          // 添加当前页分页参数，确保导出当前页数据
          exportParams.pageNo = currentPage.value;
          exportParams.pageSize = currentPageSize.value;

          const data = await exportRawMessageLog(exportParams);
          downloadByData(data, `raw_message_log_${new Date().getTime()}.xlsx`);
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
      };
    },
  });
</script>

<style lang="less" scoped>
  .raw-message-log-container {
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

  :deep(.ant-table-tbody > tr > td) {
    white-space: nowrap;
  }
</style>
