import { ref, reactive, unref, onMounted } from 'vue';
import type { BasicTableProps, TableActionType, FetchParams, PaginationProps, BasicColumn } from '../types/table';
import { isFunction } from '@/utils/is';

export function useTable(tableProps?: Partial<BasicTableProps>): [(instance: any) => void, TableActionType] {
  const tableRef = ref<any>(null);
  const state = reactive({ dataSource: [] as any[], loading: false,
    pagination: { showSizeChanger: true, showQuickJumper: true, pageSize: 10, pageSizeOptions: ['10','20','50','100'], total: 0, current: 1 } as PaginationProps,
    selectedRowKeys: [] as string[], selectedRows: [] as any[], rawDataSource: null as any, showPagination: true });
  const mergedProps = ref<Partial<BasicTableProps>>({ ...tableProps });

  function register(instance: any) {
    tableRef.value = instance;
    onMounted(() => { if (unref(mergedProps).immediate !== false) fetch(); });
  }

  async function fetch(opt?: FetchParams) {
    const { api, beforeFetch, afterFetch, fetchSetting, searchInfo } = unref(mergedProps);
    if (!api || !isFunction(api)) return;
    const pf = fetchSetting?.pageField || 'pageNo', sf = fetchSetting?.sizeField || 'pageSize';
    state.loading = true;
    try {
      let params: any = { [pf]: opt?.page ?? state.pagination.current, [sf]: opt?.pageSize ?? state.pagination.pageSize, ...searchInfo, ...opt };
      if (beforeFetch) params = beforeFetch(params) || params;
      const res = await api(params);
      let list: any[] = [], total = 0;
      if (Array.isArray(res)) { list = res; total = res.length; }
      else if (res) {
        const lf = fetchSetting?.listField || 'list', tf = fetchSetting?.totalField || 'total';
        list = res[lf] || res.items || res.data?.list || res.data?.items || [];
        total = res[tf] || res.count || res.data?.total || list.length;
      }
      if (afterFetch) list = afterFetch(list) || list;
      state.dataSource = list; state.rawDataSource = res; state.pagination.total = total;
    } catch (e) { console.error('Table fetch error:', e); state.dataSource = []; }
    finally { state.loading = false; }
  }

  const action: TableActionType = {
    async reload(opt?: FetchParams) { if (opt?.page) state.pagination.current = opt.page; await fetch(opt); },
    setProps(props) { mergedProps.value = { ...unref(mergedProps), ...props }; },
    getDataSource() { return unref(state.dataSource); }, getRawDataSource() { return state.rawDataSource; },
    getColumns() { return (unref(mergedProps).columns || []).filter(c => !c.defaultHidden); },
    setColumns() {}, updateColumn() {},
    getSelectRowKeys() { return unref(state.selectedRowKeys); }, getSelectRows() { return unref(state.selectedRows); },
    setSelectedRowKeys(keys) { state.selectedRowKeys = keys; }, clearSelectedRowKeys() { state.selectedRowKeys = []; state.selectedRows = []; },
    setPagination(info) { Object.assign(state.pagination, info); }, getPaginationRef() { return state.pagination; },
    setTableData(values) { state.dataSource = values; },
    deleteTableDataRecord(record) { const idx = state.dataSource.findIndex(i => i === record || i.id === record.id); if (idx !== -1) state.dataSource.splice(idx, 1); },
    insertTableDataRecord(record, index) { if (index !== undefined) state.dataSource.splice(index, 0, record); else state.dataSource.push(record); },
    updateTableDataRecord(rowKey, record) { const idx = state.dataSource.findIndex(i => String(i.id) === String(rowKey)); if (idx !== -1) state.dataSource[idx] = { ...state.dataSource[idx], ...record }; },
    findTableDataRecord(rowKey) { return state.dataSource.find(i => String(i.id) === String(rowKey)); },
    setLoading(loading) { state.loading = loading; }, redoHeight() {}, expandAll() {}, collapseAll() {},
    setShowPagination(show) { state.showPagination = show; }, getShowPagination() { return state.showPagination; }, getForm() { return {}; },
  };
  return [register, action];
}
