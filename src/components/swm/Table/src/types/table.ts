export interface BasicColumn {
  title: string; dataIndex: string; key?: string; width?: number;
  align?: 'left' | 'right' | 'center'; fixed?: boolean | 'left' | 'right';
  defaultHidden?: boolean; dictType?: string; defaultValue?: string;
  customRender?: (params: { text: any; record: any; index: number }) => any;
  flag?: 'INDEX' | 'DEFAULT' | 'CHECKBOX' | 'RADIO' | 'ACTION';
  slot?: string; actions?: (record: any) => ActionItem[];
  sorter?: boolean | Function; filters?: any[]; [key: string]: any;
}
export interface ActionItem {
  icon?: string; label?: string; tooltip?: string;
  color?: 'success' | 'error' | 'info' | 'warning' | 'primary';
  onClick?: (...args: any[]) => void; disabled?: boolean; divider?: boolean;
  auth?: string; ifShow?: boolean | ((action: any) => boolean);
  popConfirm?: { title: string; confirm: (...args: any[]) => void; cancel?: (...args: any[]) => void; okText?: string; };
}
export interface FetchSetting { pageField: string; sizeField: string; listField: string; totalField: string; }
export interface PaginationProps { showSizeChanger?: boolean; showQuickJumper?: boolean; pageSize?: number; pageSizeOptions?: string[]; total?: number; current?: number; }
export interface BasicTableProps { title?: string; api?: (...args: any[]) => Promise<any>; rowKey?: string | ((record: any) => string); columns: BasicColumn[]; showTableSetting?: boolean; canResize?: boolean; useSearchForm?: boolean; formConfig?: any; pagination?: PaginationProps | false; fetchSetting?: Partial<FetchSetting>; beforeFetch?: (params: any) => any; afterFetch?: (data: any) => any; immediate?: boolean; searchInfo?: Recordable; dataSource?: any[]; loading?: boolean; bordered?: boolean; showIndexColumn?: boolean; rowSelection?: any; scroll?: any; [key: string]: any; }
export interface FetchParams { page?: number; pageSize?: number; [key: string]: any; }
export interface TableActionType { reload: (opt?: FetchParams) => Promise<void>; setProps: (props: Partial<BasicTableProps>) => void; getDataSource: () => any[]; getRawDataSource: () => any; getColumns: () => BasicColumn[]; setColumns: (columns: BasicColumn[]) => void; updateColumn: (key: string, column: Partial<BasicColumn>) => void; getSelectRowKeys: () => string[]; getSelectRows: () => any[]; setSelectedRowKeys: (keys: string[]) => void; clearSelectedRowKeys: () => void; setPagination: (info: Partial<PaginationProps>) => void; getPaginationRef: () => PaginationProps; setTableData: (values: any[]) => void; deleteTableDataRecord: (record: any) => void; insertTableDataRecord: (record: any, index?: number) => void; updateTableDataRecord: (rowKey: string | number, record: any) => void; findTableDataRecord: (rowKey: string | number) => any; setLoading: (loading: boolean) => void; redoHeight: () => void; expandAll: () => void; collapseAll: () => void; setShowPagination: (show: boolean) => void; getShowPagination: () => boolean; getForm: () => any; }
type Fn = (...args: any[]) => any; type Recordable = Record<string, any>;
