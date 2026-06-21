import { ref, reactive, computed, watch, onMounted, toRaw, unref, nextTick } from 'vue';
import type { BasicTableProps, TableActionType, FetchParams, PaginationProps, BasicColumn } from '../types/table';
import { isFunction } from '@/utils/is';

interface TableState {
  dataSource: any[];
  loading: boolean;
  pagination: PaginationProps;
  selectedRowKeys: string[];
  selectedRows: any[];
  rawDataSource: any;
  showPagination: boolean;
}

export function useTable(tableProps?: Partial<BasicTableProps>): [(instance: any) => void, TableActionType] {
  const tableRef = ref<any>(null);
  const state = reactive<TableState>({
    dataSource: [],
    loading: false,
    pagination: {
      showSizeChanger: true,
      showQuickJumper: true,
      pageSize: 10,
      pageSizeOptions: ['10', '20', '50', '100'],
      total: 0,
      current: 1,
    },
    selectedRowKeys: [],
    selectedRows: [],
    rawDataSource: null,
    showPagination: true,
  });

  const mergedProps = ref<Partial<BasicTableProps>>({ ...tableProps });

  function register(instance: any) {
    tableRef.value = instance;
    onMounted(() => {
      if (unref(mergedProps).immediate !== false) {
        fetch();
      }
    });
  }

  async function fetch(opt?: FetchParams) {
    const { api, beforeFetch, afterFetch, fetchSetting, searchInfo } = unref(mergedProps);
    if (!api || !isFunction(api)) return;

    const pageField = fetchSetting?.pageField || 'pageNo';
    const sizeField = fetchSetting?.sizeField || 'pageSize';

    state.loading = true;
    try {
      let params: any = {
        [pageField]: opt?.page ?? state.pagination.current,
        [sizeField]: opt?.pageSize ?? state.pagination.pageSize,
        ...searchInfo,
        ...opt,
      };

      if (beforeFetch && isFunction(beforeFetch)) {
        params = beforeFetch(params) || params;
      }

      const res = await api(params);

      let list: any[] = [];
      let total = 0;

      if (Array.isArray(res)) {
        list = res;
        total = res.length;
      } else if (res) {
        // Handle different response formats
        const listField = fetchSetting?.listField || 'list';
        const totalField = fetchSetting?.totalField || 'total';
        list = res[listField] || res.items || res.data?.list || res.data?.items || [];
        total = res[totalField] || res.count || res.data?.total || list.length;
      }

      if (afterFetch && isFunction(afterFetch)) {
        list = afterFetch(list) || list;
      }

      state.dataSource = list;
      state.rawDataSource = res;
      state.pagination.total = total;
    } catch (error) {
      console.error('Table fetch error:', error);
      state.dataSource = [];
    } finally {
      state.loading = false;
    }
  }

  function handleTableChange(pagination: any, _filters: any, _sorter: any) {
    state.pagination.current = pagination.current;
    state.pagination.pageSize = pagination.pageSize;
    fetch();
  }

  function handleSelectChange(keys: string[], rows: any[]) {
    state.selectedRowKeys = keys;
    state.selectedRows = rows;
  }

  const action: TableActionType = {
    async reload(opt?: FetchParams) {
      if (opt?.page) {
        state.pagination.current = opt.page;
      }
      await fetch(opt);
    },
    setProps(props: Partial<BasicTableProps>) {
      mergedProps.value = { ...unref(mergedProps), ...props };
    },
    getDataSource() {
      return unref(state.dataSource);
    },
    getRawDataSource() {
      return state.rawDataSource;
    },
    getColumns(): BasicColumn[] {
      return (unref(mergedProps).columns || []).filter((c) => !c.defaultHidden);
    },
    setColumns(_columns: BasicColumn[]) {
      // simplified
    },
    updateColumn(_key: string, _column: Partial<BasicColumn>) {
      // simplified
    },
    getSelectRowKeys() {
      return unref(state.selectedRowKeys);
    },
    getSelectRows() {
      return unref(state.selectedRows);
    },
    setSelectedRowKeys(keys: string[]) {
      state.selectedRowKeys = keys;
    },
    clearSelectedRowKeys() {
      state.selectedRowKeys = [];
      state.selectedRows = [];
    },
    setPagination(info: Partial<PaginationProps>) {
      Object.assign(state.pagination, info);
    },
    getPaginationRef() {
      return state.pagination;
    },
    setTableData(values: any[]) {
      state.dataSource = values;
    },
    deleteTableDataRecord(record: any) {
      const idx = state.dataSource.findIndex((item) => item === record || item.id === record.id);
      if (idx !== -1) state.dataSource.splice(idx, 1);
    },
    insertTableDataRecord(record: any, index?: number) {
      if (index !== undefined) {
        state.dataSource.splice(index, 0, record);
      } else {
        state.dataSource.push(record);
      }
    },
    updateTableDataRecord(rowKey: string | number, record: any) {
      const idx = state.dataSource.findIndex((item) => String(item.id) === String(rowKey));
      if (idx !== -1) {
        state.dataSource[idx] = { ...state.dataSource[idx], ...record };
      }
    },
    findTableDataRecord(rowKey: string | number) {
      return state.dataSource.find((item) => String(item.id) === String(rowKey));
    },
    setLoading(loading: boolean) {
      state.loading = loading;
    },
    redoHeight() {
      // no-op
    },
    expandAll() {
      // no-op
    },
    collapseAll() {
      // no-op
    },
    setShowPagination(show: boolean) {
      state.showPagination = show;
    },
    getShowPagination() {
      return state.showPagination;
    },
    getForm() {
      return {};
    },
  };

  return [register, action];
}
