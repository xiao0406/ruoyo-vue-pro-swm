<template>
  <div class="basic-table">
    <div class="basic-table-header" v-if="props.title || $slots.tableTitle || $slots.toolbar">
      <div class="basic-table-title">
        <slot name="tableTitle">{{ props.title }}</slot>
      </div>
      <div class="basic-table-toolbar">
        <slot name="toolbar" />
      </div>
    </div>
    <a-table
      v-bind="getBindValues"
      :columns="mergedColumns"
      :data-source="dataSource"
      :loading="loading"
      :pagination="showPagination ? paginationConfig : false"
      :row-selection="rowSelectionConfig"
      :row-key="rowKey || 'id'"
      :scroll="props.scroll"
      :bordered="props.bordered"
      @change="handleTableChange"
    >
      <template v-for="(_, name) in $slots" #[name]="slotData" :key="name">
        <slot v-bind="slotData || {}" :name="name" />
      </template>
    </a-table>
  </div>
</template>

<script lang="ts" setup>
  import { ref, computed, onMounted, watch, unref } from 'vue';
  import { Table as ATable } from 'ant-design-vue';
  import type { BasicTableProps, BasicColumn } from './types/table';
  import type { TablePaginationConfig } from 'ant-design-vue/es/table';

  const props = withDefaults(
    defineProps<{
      title?: string;
      columns?: BasicColumn[];
      dataSource?: any[];
      loading?: boolean;
      pagination?: any;
      rowSelection?: any;
      rowKey?: string | ((record: any) => string);
      scroll?: any;
      bordered?: boolean;
      showIndexColumn?: boolean;
      inset?: boolean;
      canResize?: boolean;
      useSearchForm?: boolean;
      fetchSetting?: any;
      api?: (...args: any[]) => Promise<any>;
      beforeFetch?: (params: any) => any;
      afterFetch?: (data: any) => any;
      immediate?: boolean;
      searchInfo?: any;
      showTableSetting?: boolean;
      formConfig?: any;
      clickToRowSelect?: boolean;
      sortFn?: Function;
      filterFn?: Function;
      showSelectionBar?: boolean;
    }>(),
    {
      title: '',
      loading: false,
      rowKey: 'id',
      bordered: false,
      showIndexColumn: true,
      canResize: false,
      useSearchForm: false,
      immediate: true,
      showTableSetting: false,
      inset: false,
      clickToRowSelect: true,
      showSelectionBar: false,
    },
  );

  const emit = defineEmits(['register', 'fetchSuccess', 'selectionChange']);

  const mergedColumns = computed(() => {
    return (props.columns || []).filter((col) => !col.defaultHidden);
  });

  const showPagination = computed(() => {
    if (props.pagination === false) return false;
    return true;
  });

  const paginationConfig = computed<TablePaginationConfig>(() => {
    if (props.pagination === false) return {};
    return {
      showSizeChanger: true,
      showQuickJumper: true,
      pageSize: 10,
      pageSizeOptions: ['10', '20', '50', '100'],
      ...props.pagination,
    };
  });

  const rowSelectionConfig = computed(() => {
    if (!props.rowSelection) return undefined;
    return {
      ...props.rowSelection,
      onChange: (keys: string[], rows: any[]) => {
        emit('selectionChange', keys, rows);
      },
    };
  });

  const getBindValues = computed(() => {
    return {
      ...Object.fromEntries(Object.entries(props).filter(([_, v]) => v !== undefined)),
    };
  });

  function handleTableChange(pag: TablePaginationConfig, filters: any, sorter: any) {
    if (props.pagination && typeof props.pagination === 'object') {
      Object.assign(props.pagination, {
        current: pag.current,
        pageSize: pag.pageSize,
      });
    }
  }

  onMounted(() => {
    emit('register', {
      reload: async () => {},
      setProps: () => {},
      getDataSource: () => props.dataSource || [],
      getRawDataSource: () => null,
      getColumns: () => mergedColumns.value,
      setColumns: () => {},
      updateColumn: () => {},
      getSelectRowKeys: () => [],
      getSelectRows: () => [],
      setSelectedRowKeys: () => {},
      clearSelectedRowKeys: () => {},
      setPagination: () => {},
      getPaginationRef: () => paginationConfig.value,
      setTableData: () => {},
      deleteTableDataRecord: () => {},
      insertTableDataRecord: () => {},
      updateTableDataRecord: () => {},
      findTableDataRecord: () => null,
      setLoading: () => {},
      redoHeight: () => {},
      expandAll: () => {},
      collapseAll: () => {},
      setShowPagination: () => {},
      getShowPagination: () => true,
      getForm: () => ({}),
    });
  });
</script>

<style scoped>
  .basic-table-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }
  .basic-table-title {
    font-size: 16px;
    font-weight: 600;
  }
  .basic-table-toolbar {
    display: flex;
    gap: 8px;
  }
</style>
