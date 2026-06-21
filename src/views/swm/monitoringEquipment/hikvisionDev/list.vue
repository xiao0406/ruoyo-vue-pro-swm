<template>
  <div>
    <BasicTable @register="registerTable">
      <template #tableTitle>
        <Icon icon="ant-design:book-outlined" class="pr-1 m-1" />
        <span> 开康设备 </span>
      </template>
    </BasicTable>
  </div>
</template>
<script lang="ts" setup>
  import { watch } from 'vue';
  import { Icon } from '@/components/swm/Icon';
  import { BasicTable, BasicColumn, useTable } from '@/components/swm/Table';
  import { getCameras } from '@/api/sys/monitorDeviceInfo';

  const props = defineProps({
    treeCode: String,
  });

  const tableColumns: BasicColumn[] = [
    {
      title: '监控设备名称',
      dataIndex: 'cameraName',
      width: 100,
    },
    {
      title: '监控点类型说明',
      dataIndex: 'cameraTypeName',
      width: 100,
    },
    {
      title: '监控点唯一标识',
      dataIndex: 'cameraIndexCode',
      width: 100,
    },
  ];

  const [registerTable, { reload, clearSelectedRowKeys, getSelectRows, redoHeight }] = useTable({
    rowKey: 'cameraIndexCode',
    api: async (params) => {
      const { data } = await getCameras(params);
      return {
        count: data.total,
        list: data.list,
      };
    },
    beforeFetch: (params) => {
      params.code = props.treeCode;
      return params;
    },
    immediate: false, // 是否立即请求数据
    columns: tableColumns,
    showTableSetting: true,
    useSearchForm: false,
    canResize: true,
    resizeHeightOffset: 65,
    rowSelection: {
      type: 'radio',
    },
  });

  watch(
    () => props.treeCode,
    async () => {
      await reload();
      redoHeight();
    },
  );

  defineExpose({
    clearSelectedRowKeys,
    getSelectRows,
  });
</script>
