<template>
  <div class="area-fence-data-container">
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleExport" :loading="exportLoading">
          导出当前页
        </a-button>
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { BasicTable, useTable } from '@/components/swm/Table';
  import {
    getAreaFenceDataList,
    exportAreaFenceData,
    type AreaFenceDataQuery,
  } from '@/api/swm/areaFenceData';
  import { message } from 'ant-design-vue';
  import { downloadByData } from '@/utils/file/download';
  import moment from 'moment';

  export default defineComponent({
    name: 'ViewsSwmAreaFenceDataIndex',
    components: { BasicTable },
    setup() {
      const exportLoading = ref(false);
      const currentPage = ref(1);
      const currentPageSize = ref(1000);

      const [registerTable, { getForm }] = useTable({
        title: '区域围栏数据',
        api: getAreaFenceDataList,
        striped: true,
        beforeFetch: (params: AreaFenceDataQuery) => {
          if (params.pageNo) currentPage.value = params.pageNo;
          if (params.pageSize) currentPageSize.value = params.pageSize;

          if (params.timeRange && params.timeRange.length === 2) {
            params.startTime = params.timeRange[0];
            params.endTime = params.timeRange[1];
            delete params.timeRange;
          }

          const deviceIds = params.deviceId;
          if (deviceIds && Array.isArray(deviceIds)) {
            const valid = [...new Set(deviceIds.filter((id) => id && id.trim()))];
            if (valid.length > 0) {
              params.deviceId = valid.join(',');
            } else {
              delete params.deviceId;
            }
          }

          const idCards = params.idCard;
          if (idCards && Array.isArray(idCards)) {
            const valid = [...new Set(idCards.filter((card) => card && card.trim()))];
            if (valid.length > 0) {
              params.idCard = valid.join(',');
            } else {
              delete params.idCard;
            }
          } else if (typeof params.idCard === 'string') {
            params.idCard = params.idCard.trim();
            if (!params.idCard) {
              delete params.idCard;
            }
          }

          return params;
        },
        afterFetch: (result) => result,
        rowKey: (record, index) => `${record.timeText || ''}_${record.deviceId || index}`,
        useSearchForm: true,
        formConfig: {
          baseColProps: { lg: 6, md: 8 },
          labelWidth: 110,
          schemas: [
            {
              field: 'deviceId',
              label: '设备号',
              component: 'Select',
              componentProps: {
                mode: 'tags',
                placeholder: '请输入设备号，支持多个',
                allowClear: true,
                tokenSeparators: [',', '，', ' ', '\\n'],
                filterOption: false,
                notFoundContent: null,
                style: { width: '100%' },
              },
              colProps: { span: 6 },
            },
            {
              field: 'idCard',
              label: '身份证',
              component: 'Select',
              componentProps: {
                mode: 'tags',
                placeholder: '请输入身份证号，支持多个',
                allowClear: true,
                tokenSeparators: [',', '，', ' ', '\\n'],
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
              defaultValue: 'DESC',
              componentProps: {
                options: [
                  { label: '降序（最新在前）', value: 'DESC' },
                  { label: '升序（最早在前）', value: 'ASC' },
                ],
              },
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
          { title: 'device_id', dataIndex: 'deviceId', width: 160 },
          { title: 'id_card', dataIndex: 'idCard', width: 180 },
          { title: 'x', dataIndex: 'x', width: 120 },
          { title: 'y', dataIndex: 'y', width: 120 },
          { title: 'area_name', dataIndex: 'areaName', width: 200, ellipsis: true },
          { title: 'area_id', dataIndex: 'areaId', width: 160, ellipsis: true },
          { title: 'area_type', dataIndex: 'areaType', width: 120 },
          { title: 'remarks', dataIndex: 'remarks', width: 220, ellipsis: true },
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
          y: 'calc(100vh - 318px)', // 设置固定高度，让分页固定在底部
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
          const exportParams: any = { ...formValues };

          if (formValues.timeRange && formValues.timeRange.length === 2) {
            exportParams.startTime = formValues.timeRange[0];
            exportParams.endTime = formValues.timeRange[1];
            delete exportParams.timeRange;
          }

          if (formValues.deviceId && Array.isArray(formValues.deviceId)) {
            const valid = [...new Set(formValues.deviceId.filter((id: string) => id && id.trim()))];
            if (valid.length > 0) {
              exportParams.deviceId = valid.join(',');
            } else {
              delete exportParams.deviceId;
            }
          }

          if (formValues.idCard && Array.isArray(formValues.idCard)) {
            const valid = [
              ...new Set(formValues.idCard.filter((card: string) => card && card.trim())),
            ];
            if (valid.length > 0) {
              exportParams.idCard = valid.join(',');
            } else {
              delete exportParams.idCard;
            }
          } else if (typeof exportParams.idCard === 'string') {
            exportParams.idCard = exportParams.idCard.trim();
            if (!exportParams.idCard) {
              delete exportParams.idCard;
            }
          }

          exportParams.pageNo = currentPage.value;
          exportParams.pageSize = currentPageSize.value;

          const data = await exportAreaFenceData(exportParams);
          downloadByData(data, `area_fence_data_${new Date().getTime()}.xlsx`);
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
  .area-fence-data-container {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  :deep(.ant-table-tbody > tr > td) {
    white-space: nowrap;
  }
</style>
