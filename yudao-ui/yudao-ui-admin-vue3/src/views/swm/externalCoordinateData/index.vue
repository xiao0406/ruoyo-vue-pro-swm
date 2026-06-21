<template>
  <div class="external-coordinate-data-container">
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleExport" :loading="exportLoading">
          导出当前页
        </a-button>
      </template>

      <template #bodyCell="{ column, record }">
        <template v-if="['nearestBeacon', 'usedBeacons'].includes(column.dataIndex as string)">
          <a-tooltip :title="record[column.dataIndex]">
            <div class="ellipsis-cell">
              {{ record[column.dataIndex] }}
            </div>
          </a-tooltip>
        </template>
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { BasicTable, useTable } from '@/components/swm/Table';
  import {
    getExternalCoordinateDataList,
    exportExternalCoordinateData,
    type ExternalCoordinateDataQuery,
  } from '@/api/swm/externalCoordinateData';
  import { message } from 'ant-design-vue';
  import { downloadByData } from '@/utils/file/download';

  export default defineComponent({
    name: 'ViewsSwmExternalCoordinateDataIndex',
    components: { BasicTable },
    setup() {
      const exportLoading = ref(false);
      const currentPage = ref(1);
      const currentPageSize = ref(1000);

      const [registerTable, { getForm }] = useTable({
        title: '外部坐标数据',
        api: getExternalCoordinateDataList,
        striped: true,
        beforeFetch: (params: ExternalCoordinateDataQuery) => {
          if (params.pageNo) currentPage.value = params.pageNo;
          if (params.pageSize) currentPageSize.value = params.pageSize;

          if (params.timeRange && params.timeRange.length === 2) {
            params.startTime = params.timeRange[0];
            params.endTime = params.timeRange[1];
            delete params.timeRange;
          }

          const elderIds = params.elderId;
          if (elderIds && Array.isArray(elderIds)) {
            const valid = [...new Set(elderIds.filter((id) => id && id.trim()))];
            if (valid.length > 0) {
              params.elderId = valid.join(',');
            } else {
              delete params.elderId;
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
        rowKey: (record, index) =>
          `${record.timeText || record.timeStr || ''}_${record.elderId || index}`,
        useSearchForm: true,
        formConfig: {
          baseColProps: { lg: 6, md: 8 },
          labelWidth: 110,
          schemas: [
            {
              field: 'elderId',
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
          { title: 'elder_id', dataIndex: 'elderId', width: 160 },
          { title: 'id_card', dataIndex: 'idCard', width: 180 },
          { title: 'address', dataIndex: 'address', width: 220, ellipsis: true },
          { title: 'map_id', dataIndex: 'mapId', width: 100 },
          { title: 'x', dataIndex: 'x', width: 120 },
          { title: 'y', dataIndex: 'y', width: 120 },
          { title: 'org_cd', dataIndex: 'orgCd', width: 150 },
          { title: 'time_str', dataIndex: 'timeStr', width: 180, ellipsis: true },
          { title: 'type', dataIndex: 'type', width: 120 },
          { title: 'app_id', dataIndex: 'appId', width: 160 },
          { title: 'warning_id', dataIndex: 'warningId', width: 120 },
          { title: 'original_x', dataIndex: 'originalX', width: 140 },
          { title: 'original_y', dataIndex: 'originalY', width: 140 },
          { title: 'scale_x', dataIndex: 'scaleX', width: 140 },
          { title: 'scale_y', dataIndex: 'scaleY', width: 140 },
          { title: 'nearest_beacon', dataIndex: 'nearestBeacon', width: 220, ellipsis: true },
          { title: 'used_beacons', dataIndex: 'usedBeacons', width: 260, ellipsis: true },
        ],
        pagination: {
          pageSize: 1000,
          showSizeChanger: true,
          showQuickJumper: true,
          pageSizeOptions: ['100', '500', '1000', '5000', '10000'],
          showTotal: (total) => `共 ${total} 条数据`,
        },
        scroll: {
          x: 2200,
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

          if (formValues.elderId && Array.isArray(formValues.elderId)) {
            const valid = [...new Set(formValues.elderId.filter((id: string) => id && id.trim()))];
            if (valid.length > 0) {
              exportParams.elderId = valid.join(',');
            } else {
              delete exportParams.elderId;
            }
          }

          if (typeof exportParams.idCard === 'string') {
            exportParams.idCard = exportParams.idCard.trim();
            if (!exportParams.idCard) {
              delete exportParams.idCard;
            }
          } else if (exportParams.idCard && Array.isArray(exportParams.idCard)) {
            const valid = [
              ...new Set(exportParams.idCard.filter((card: string) => card && card.trim())),
            ];
            if (valid.length > 0) {
              exportParams.idCard = valid.join(',');
            } else {
              delete exportParams.idCard;
            }
          }

          exportParams.pageNo = currentPage.value;
          exportParams.pageSize = currentPageSize.value;

          const data = await exportExternalCoordinateData(exportParams);
          downloadByData(data, `external_coordinate_data_${new Date().getTime()}.xlsx`);
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
  .external-coordinate-data-container {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .ellipsis-cell {
    max-width: 240px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  :deep(.ant-table-tbody > tr > td) {
    white-space: nowrap;
  }
</style>
