<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    title="排班记录"
    width="80%"
    :showOkBtn="false"
  >
    <BasicTable @register="registerTable" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicTable, useTable, BasicColumn } from '@/components/swm/Table';
  import { getScheduleLog } from '@/api/swm/staffSchedule';

  const [registerModal, { setModalProps }] = useModalInner(async (data) => {
    try {
      setTableData([]);
      setModalProps({ loading: true });
      const res = await getScheduleLog(data.id);
      setTableData(res || []);
    } finally {
      setModalProps({ loading: false });
    }
  });

  const tableColumns: BasicColumn[] = [
    {
      title: '操作描述',
      dataIndex: 'operateDesc',
    },
  ];

  const [registerTable, { setTableData }] = useTable({
    dataSource: [],
    columns: tableColumns,
    showTableSetting: false,
    canResize: true,
    resizeHeightOffset: 100,
    pagination: false,
  });
</script>
