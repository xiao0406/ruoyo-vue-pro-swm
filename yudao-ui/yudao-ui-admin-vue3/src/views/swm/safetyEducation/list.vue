<!--
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author zwf
-->
<template>
  <div class="safety-education-container">
    <BasicTable @register="registerTable" @change="handleTableChange">
      <template #toolbar>
        <a-button type="primary" @click="handleCreate">
          <Icon icon="fluent:add-12-filled" /> {{ t('新增') }}
        </a-button>
      </template>
      <template #bodyCell="{ column, record, index }">
        <template v-if="column.key === 'index'">
          {{ calculateRowIndex(index) }}
        </template>
        <!-- <template v-if="column.key === 'action'">
          <div class="action-buttons">
            <a-button type="primary" size="small" @click="handleView(record)" :id="`view-btn-${record.id}`"
              title="双击可能更好打开">查看</a-button>
            <a-button type="primary" size="small" @click="handleComplete(record)"
              v-if="record.safetyStatus === StatusEnum.NOT_STARTED">完成教育</a-button>
          </div>
        </template> -->
        <template v-else-if="column.key === 'safetyStatus'">
          <Tag :color="getStatusColor(record.safetyStatus)">
            <DictLabel dictType="education_status_enum" :dictValue="record.safetyStatus" />
          </Tag>
        </template>
        <template v-else-if="column.key === 'a.participation_type'">
          <DictLabel
            dictType="education_participation_type_enum"
            :dictValue="record.participationType"
          />
        </template>
        <template v-else-if="column.key === 'a.safety_education_type'">
          <DictLabel dictType="education_type_enum" :dictValue="record.safetyEducationType" />
        </template>
        <template v-else-if="column.key === 'action'">
          <TableAction :actions="getActions(record)" />
        </template>
      </template>
    </BasicTable>
    <InputForm @register="registerCreateDrawer" @success="handleSuccess" />
    <InputForm @register="registerViewDrawer" @success="handleSuccess" />
    <CompleteForm @register="registerModal" @success="handleSuccess" />
  </div>
</template>
<script lang="ts">
  export default defineComponent({
    name: 'ViewsSwmSafetyEducationList',
    components: { DictLabel },
  });
</script>
<script lang="ts" setup>
  import { defineComponent, ref, onMounted, nextTick, computed } from 'vue';
  import { useI18n } from '@/hooks/swm/useI18n';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { router } from '@/router';
  import { Icon } from '@/components/swm/Icon';
  import { BasicTable, useTable, TableAction, BasicColumn } from '@/components/swm/Table';
  import {
    safetyEducationDelete,
    safetyEducationListData,
    safetyEducationForm,
  } from '@/api/swm/safetyEducation';
  import { useDrawer } from '@/components/swm/Drawer';
  import { useModal } from '@/components/swm/Modal';
  import { FormProps } from '@/components/swm/Form';
  import InputForm from './form.vue';
  import CompleteForm from './complete.vue';
  import { Tag } from 'ant-design-vue';
  import { StatusEnum, DICT } from '@/enums/swm/safetyEducationEnum';
  import { DictLabel, useDict } from '@/components/swm/Dict';

  const { t } = useI18n('swm.safetyEducation');
  const { showMessage, createConfirm } = useMessage();
  const getTitle = {
    icon: router.currentRoute.value.meta.icon || 'ant-design:safety-outlined',
    value: router.currentRoute.value.meta.title || t('安全教育管理'),
  };

  // 获取字典相关工具
  const { initDict, getDictLabel } = useDict();

  // 初始化字典数据
  onMounted(async () => {
    try {
      await initDict([DICT.EDUCATION_STATUS, DICT.EDUCATION_TYPE, DICT.PARTICIPATION_TYPE]);
    } catch (error) {
      console.error('字典加载异常:', error);
      showMessage({
        content: '字典加载异常',
        type: 'error',
      });
    }
  });
  function getActions(record: Recordable) {
    const actions = [
      {
        icon: 'ant-design:eye-outlined',
        tooltip: '查看',
        onClick: handleView.bind(null, record),
      },
    ];
    if (record.safetyStatus == StatusEnum.NOT_STARTED) {
      actions.push({
        icon: 'ant-design:check-circle-outlined',
        tooltip: '完成教育',
        type: 'link',
        color: 'success',
        onClick: handleComplete.bind(null, record),
      });
    }
    return actions;
  }
  const searchForm: FormProps = {
    baseColProps: { lg: 6, md: 8 },
    labelWidth: 100,
    showResetButton: true,
    showSubmitButton: true,
    schemas: [
      {
        label: t('主题'),
        field: 'theme',
        component: 'Input',
        componentProps: {
          placeholder: '请输入主题',
          allowClear: true,
        },
      },
      {
        label: t('安全教育类型'),
        field: 'safetyEducationType',
        component: 'Select',
        componentProps: {
          placeholder: '请选择',
          allowClear: true,
          dictType: DICT.EDUCATION_TYPE,
        },
        defaultValue: '', // 默认选择全部
      },
      {
        label: t('参与类型'),
        field: 'participationType',
        component: 'Select',
        componentProps: {
          placeholder: '请选择',
          allowClear: true,
          dictType: DICT.PARTICIPATION_TYPE,
        },
        defaultValue: '', // 默认选择全部
      },
      {
        label: t('状态'),
        field: 'status',
        component: 'Select',
        componentProps: {
          placeholder: '请选择',
          allowClear: true,
          dictType: DICT.EDUCATION_STATUS,
        },
        defaultValue: '', // 默认选择全部
      },
    ],
  };

  const tableColumns: BasicColumn[] = [
    {
      title: '序号',
      key: 'index',
      width: 70,
    },
    {
      title: t('主题'),
      dataIndex: 'theme',
      key: 'a.theme',
      sorter: true,
      width: 180,
      align: 'left',
    },
    {
      title: t('安全教育类型'),
      dataIndex: 'safetyEducationType',
      key: 'a.safety_education_type',
      sorter: true,
      width: 120,
      align: 'center',
    },
    {
      title: t('参与类型'),
      dataIndex: 'participationType',
      key: 'a.participation_type',
      sorter: true,
      width: 120,
      align: 'center',
    },
    {
      title: t('开始时间'),
      dataIndex: 'startTime',
      key: 'a.start_time',
      sorter: true,
      width: 120,
      align: 'center',
    },
    {
      title: t('参与对象'),
      dataIndex: 'participantsName',
      key: 'a.participants_name',
      sorter: true,
      width: 150,
      align: 'left',
    },
    {
      title: t('状态'),
      dataIndex: 'safetyStatus',
      key: 'safetyStatus',
      width: 80,
      align: 'center',
    },
    {
      title: t('操作'),
      key: 'action',
      width: 200,
      align: 'center',
    },
  ];

  // 确保表格中使用的columns是有效的
  const validTableColumns = tableColumns.map((column) => ({
    ...column,
    // 确保每列都有dataIndex，用于正确获取数据
    dataIndex: column.dataIndex || column.key,
  }));

  // 表格数据
  const tableData = ref<any[]>([]);
  const tableTotal = ref(0);
  const searchParams = ref<Record<string, any>>({});

  // 使用不同的抽屉实例
  const [registerCreateDrawer, { openDrawer: openCreateDrawer }] = useDrawer();
  const [registerViewDrawer, { openDrawer: openViewDrawer }] = useDrawer();
  const [registerModal, { openModal }] = useModal();

  // 根据操作类型记录当前打开的弹窗类型
  const currentOperation = ref('');
  //标题和上面导航栏保持一致
  const pageTitle = computed(() => {
    // 假设你通过路由元信息定义了页面标题
    return router.currentRoute.value.meta.title || '安全教育';
  });
  const [registerTable, { reload, setLoading, setPagination, getPaginationRef }] = useTable({
    title: pageTitle.value,
    // 不直接使用API，通过自定义函数获取数据
    // api: safetyEducationListData,
    beforeFetch: (params) => {
      // 合并已有的搜索参数
      const mergedParams = { ...params, ...searchParams.value };

      // 处理查询参数，确保参数名称与后端一致
      if (mergedParams) {
        // 将驼峰字段转换为下划线格式
        const transformedParams = {} as Record<string, any>;
        Object.keys(mergedParams).forEach((key) => {
          if (key === 'safetyEducationType') {
            transformedParams.safety_education_type = mergedParams[key];
          } else if (key === 'startTime') {
            transformedParams.start_time = mergedParams[key];
          } else {
            transformedParams[key] = mergedParams[key];
          }
        });

        // 不需要处理分页参数名称
        // API期望使用pageNo，不是pageNum

        return transformedParams;
      }
      return mergedParams;
    },
    // 使用响应式数据源
    dataSource: tableData,
    // 不再使用afterFetch
    columns: validTableColumns,
    formConfig: searchForm,
    showTableSetting: false, // 禁用列设置按钮
    useSearchForm: true,
    canResize: true,
    showIndexColumn: false, // 关闭默认序号列，使用自定义序号列
    immediate: false, // 不立即加载数据，由手动加载
    emptyDataIsShowTable: true, // 空数据时显示表格而不是加载状态
    rowKey: 'id', // 确保指定行key
    historyColumn: false, // 禁用列设置历史记录
    tableKey: '', // 禁用列设置功能，防止调用formRecord/getListFrom接口
    // 添加处理搜索信息的函数
    handleSearchInfoFn: (info) => {
      handleSearch(info);
      return info;
    },
    pagination: {
      pageSize: 20,
      defaultPageSize: 20,
      showSizeChanger: true,
      showQuickJumper: true,
      // 使用计算属性处理total
      get total() {
        return tableTotal.value;
      },
    },
  });

  // 手动获取表格数据
  async function fetchTableData(params: Record<string, any> = {}) {
    try {
      setLoading(true);
      // 合并搜索参数，确保不带默认的空值
      const queryParams: Record<string, any> = { ...params };

      // 移除空值参数
      Object.keys(queryParams).forEach((key) => {
        if (
          queryParams[key] === undefined ||
          queryParams[key] === null ||
          queryParams[key] === ''
        ) {
          delete queryParams[key];
        }
      });

      // 设置默认分页参数
      if (!queryParams.pageSize) {
        queryParams.pageSize = 20;
      }

      if (!queryParams.pageNo) {
        queryParams.pageNo = 1;
      }

      // 参数转换，确保与后端一致
      const apiParams: Record<string, any> = { ...queryParams };

      // 将驼峰字段转换为下划线格式
      if (apiParams.safetyEducationType) {
        apiParams.safety_education_type = apiParams.safetyEducationType;
        delete apiParams.safetyEducationType;
      }

      if (apiParams.participationType) {
        apiParams.participation_type = apiParams.participationType;
        delete apiParams.participationType;
      }

      if (apiParams.startTime) {
        apiParams.start_time = apiParams.startTime;
        delete apiParams.startTime;
      }

      // 不需要将pageNo转换为pageNum，使用正确的参数名
      // API期望使用pageNo，不是pageNum

      // 保存当前使用的查询参数
      searchParams.value = queryParams;

      const res = await safetyEducationListData(apiParams);

      if (res && typeof res === 'object' && Array.isArray(res.list)) {
        // 格式化列表数据
        const items = res.list.map((item, index) => {
          return {
            ...item,
            key: item.id || `key-${index}`, // 确保每行有唯一key
          };
        });

        tableData.value = items;
        // 使用实际的总数据量，而不是当前页面数据量
        tableTotal.value = res.count || 0;

        // 重新设置分页信息，使用当前查询参数设置分页参数，确保总数据量正确
        setPagination({
          total: res.count || 0,
          current: res.pageNo || queryParams.pageNo || 1,
          pageSize: res.pageSize || queryParams.pageSize || 20,
        });
      } else {
        tableData.value = [];
        tableTotal.value = 0;
      }
    } catch (error) {
      tableData.value = [];
      tableTotal.value = 0;
    } finally {
      setLoading(false);
    }
  }

  // 在组件挂载后加载数据
  onMounted(() => {
    fetchTableData();
  });

  // 处理搜索事件
  function handleSearch(values: Record<string, any>) {
    // 如果状态字段为空，则删除该字段，确保查询所有状态
    const searchValues: Record<string, any> = { ...values };
    if (
      searchValues.status === undefined ||
      searchValues.status === null ||
      searchValues.status === ''
    ) {
      delete searchValues.status;
    }

    // 移除未输入的查询条件
    Object.keys(searchValues).forEach((key) => {
      if (
        searchValues[key] === undefined ||
        searchValues[key] === null ||
        searchValues[key] === ''
      ) {
        delete searchValues[key];
      }
    });

    // 重置分页到第一页
    searchValues.pageNo = 1;

    // 获取分页大小
    const pagination = getPaginationRef();
    const pageSize = typeof pagination === 'object' && pagination ? pagination.pageSize || 20 : 20;
    searchValues.pageSize = pageSize;

    // 更新搜索参数
    searchParams.value = searchValues;

    // 执行查询
    fetchTableData(searchValues);
  }

  // 处理重置事件
  function handleReset() {
    // 重置为空对象，确保不带任何筛选条件
    // 获取分页大小
    const pagination = getPaginationRef();
    const pageSize = typeof pagination === 'object' && pagination ? pagination.pageSize || 20 : 20;

    const defaultParams = {
      pageNo: 1,
      pageSize: pageSize,
    };

    searchParams.value = defaultParams;
    fetchTableData(defaultParams);
  }

  function handleSuccess() {
    fetchTableData(searchParams.value);
  }

  // 新建教育
  function handleCreate() {
    // 使用时间戳确保每次都能正确触发
    const timestamp = Date.now();

    // 多次尝试打开抽屉，但使用不同的延迟策略
    // 第一次立即尝试
    openCreateDrawer(true, {
      isNewRecord: true,
      _timestamp: timestamp,
      _operation: 'create',
    });

    // 100ms后第二次尝试
    setTimeout(() => {
      openCreateDrawer(true, {
        isNewRecord: true,
        _timestamp: timestamp + 1,
        _operation: 'create',
      });
    }, 100);
  }

  // 查看教育详情
  async function handleView(record: Recordable) {
    try {
      setLoading(true);

      // 先通过API获取完整的教育详情
      const response = await safetyEducationForm({ id: record.id });
      console.log('API返回的原始数据:', response);

      // 检查响应中是否包含contentDescription字段
      const hasContentDescription =
        response &&
        (response.contentDescription !== undefined ||
          (response.safetyEducation && response.safetyEducation.contentDescription !== undefined));
      console.log('响应中是否包含contentDescription字段:', hasContentDescription);

      // 构造完整记录
      const completeRecord = {
        ...record,
        ...response,
        // 如果API返回的是safetyEducation对象中包含contentDescription，则提取出来
        contentDescription:
          response.contentDescription ||
          (response.safetyEducation ? response.safetyEducation.contentDescription : undefined),
      };

      console.log('合并后的完整教育详情:', completeRecord);

      // 使用强制重新渲染的方式确保每次都能正确打开
      const timestamp = Date.now();

      // 多次尝试打开抽屉，但使用不同的延迟策略
      // 第一次立即尝试
      openViewDrawer(true, {
        id: record.id,
        isView: true,
        record: completeRecord,
        _timestamp: timestamp,
        _operation: 'view',
      });

      // 100ms后第二次尝试
      setTimeout(() => {
        openViewDrawer(true, {
          id: record.id,
          isView: true,
          record: completeRecord,
          _timestamp: timestamp + 1,
          _operation: 'view',
        });
      }, 100);
    } catch (error) {
      console.error('获取安全教育详情失败', error);
      showMessage({
        content: '获取详情失败，请重试',
        type: 'error',
      });
    } finally {
      setLoading(false);
    }
  }

  // 完成教育
  async function handleComplete(record: Recordable) {
    try {
      setLoading(true);

      // 先通过API获取完整的教育详情
      const response = await safetyEducationForm({ id: record.id });
      console.log('完成模态框 - API返回的原始数据:', response);

      // 构造完整记录
      const completeRecord = {
        ...record,
        ...response,
        // 如果API返回的是safetyEducation对象中包含contentDescription，则提取出来
        contentDescription:
          response.contentDescription ||
          (response.safetyEducation ? response.safetyEducation.contentDescription : undefined),
      };

      console.log('完成模态框 - 合并后的完整教育详情:', completeRecord);

      // 直接打开模态框
      openModal(true, {
        ...completeRecord,
        id: record.id,
        _timestamp: Date.now(),
        _operation: 'complete',
      });
    } catch (error) {
      console.error('获取安全教育详情失败', error);
      showMessage({
        content: '获取详情失败，请重试',
        type: 'error',
      });
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete(record: Recordable) {
    createConfirm({
      iconType: 'warning',
      title: t('确认删除'),
      content: t('是否确认删除此记录？'),
      okText: t('确认'),
      cancelText: t('取消'),
      onOk: async () => {
        try {
          setLoading(true);
          const res = await safetyEducationDelete(record);
          let result = res;
          if (res && res.data) {
            result = res.data;
          }

          if (result) {
            showMessage(result.message || '删除成功');
            handleSuccess();
          }
        } catch (error) {
          showMessage({
            content: '删除失败，请重试',
            type: 'error',
          });
        } finally {
          setLoading(false);
        }
      },
    });
  }

  // 获取状态对应的颜色
  function getStatusColor(status: string) {
    switch (status) {
      case StatusEnum.COMPLETED:
      case '1':
        return 'success';
      case StatusEnum.NOT_STARTED:
      case '0':
        return 'warning';
      default:
        return 'default';
    }
  }

  // 获取状态对应的文本
  function getStatusText(status: string) {
    return getDictLabel(DICT.EDUCATION_STATUS, status, status);
  }

  function handleTableChange(pagination: any, filters: any, sorter: any) {
    // 处理表格变化后的逻辑
    const params = {
      ...searchParams.value,
      pageNo: pagination.current || 1,
      pageSize: pagination.pageSize || 20,
    };

    // 更新搜索参数
    searchParams.value = params;

    // 执行查询
    fetchTableData(params);
  }

  // 计算表格行序号，考虑分页因素
  function calculateRowIndex(index: number) {
    // 获取当前分页信息
    const paginationRef = getPaginationRef();
    if (paginationRef && typeof paginationRef !== 'boolean') {
      const current = paginationRef.current || 1;
      const pageSize = paginationRef.pageSize || 20;
      // 计算序号：(当前页码-1) * 每页条数 + 当前行索引 + 1
      return (current - 1) * pageSize + index + 1;
    }
    // 默认返回索引+1
    return index + 1;
  }
</script>

<style lang="less" scoped>
  .safety-education-container {
    height: 100%;
    display: flex;
    flex-direction: column;

    .page-header {
      display: flex;
      align-items: center;

      .page-title {
        font-size: 18px;
        font-weight: bold;
        margin-right: 12px;
      }
    }

    .action-buttons {
      display: flex;
      gap: 8px;
      justify-content: center;

      .ant-btn {
        padding: 0 8px;
        height: 24px;
        line-height: 22px;
      }
    }

    // :deep(.ant-table-wrapper) {
    //   height: calc(100% - 60px);

    //   .ant-spin-nested-loading {
    //     height: 100%;

    //     .ant-spin-container {
    //       height: 100%;
    //       display: flex;
    //       flex-direction: column;
    //     }
    //   }

    //   .ant-table {
    //     flex: 1;
    //   }

    //   .ant-pagination {
    //     margin: 16px 0;
    //   }
    // }

    :deep(.ant-table-empty .ant-table-body) {
      overflow-x: hidden !important;
      overflow-y: hidden !important;
    }
  }
</style>
