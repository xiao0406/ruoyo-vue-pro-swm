<template>
  <div class="danger-disposal-container">
    <!-- 表格 -->
    <BasicTable @register="registerTable">
      <!-- 操作按钮 -->
      <template #toolbar>
        <!-- <a-button type="primary" @click="handleCreate">新增处置记录</a-button> -->
      </template>

      <!-- 表格列自定义 -->
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'disposalMethod'">
          <DictLabel dictType="disposal_method" :dictValue="record.disposalMethod" />
        </template>
        <template v-else-if="column.dataIndex === 'disposalStatus'">
          <DictLabel dictType="disposal_status" :dictValue="record.disposalStatus" />
        </template>
        <template v-else-if="column.dataIndex === 'action'">
          <div class="action-buttons">
            <a-button size="small" type="primary" @click="handleView(record)">查看</a-button>
          </div>
        </template>
      </template>
    </BasicTable>

    <!-- 处置弹窗 -->
    <DangerDisposalDrawer @register="registerDrawer" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" setup>
  /**
   * 隐患处置记录列表
   * @author Shawn
   * @date 2025-05-22
   */
  import { ref, onMounted, computed } from 'vue';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { BasicTable, useTable } from '@/components/swm/Table';
  import { useModal } from '@/components/swm/Modal';
  import { FormProps } from '@/components/swm/Form';
  import { getDangerDisposalList, deleteDangerDisposal } from '@/api/swm/dangerDisposal';
  import { searchFormSchema } from './dangerDisposalData';
  import DangerDisposalDrawer from './DangerDisposalDrawer.vue';
  import { DictLabel, useDict } from '@/components/swm/Dict';
  import { router } from '@/router';

  const { createMessage } = useMessage();
  const [registerDrawer, { openModal }] = useModal();
  const { initDict } = useDict();

  // 初始化字典数据
  onMounted(async () => {
    try {
      await initDict(['disposal_method', 'disposal_status']);
    } catch (error) {
      console.error('字典加载异常:', error);
      createMessage.error('字典加载异常');
    }
  });

  // 定义表格列
  const columns = [
    {
      title: '序号',
      dataIndex: 'index',
      width: 80,
      customRender: ({ index }) => {
        const paginationRef = getPaginationRef();
        if (paginationRef && typeof paginationRef !== 'boolean') {
          const current = paginationRef.current || 1;
          const pageSize = paginationRef.pageSize || 10;
          return (current - 1) * pageSize + index + 1;
        }
        return index + 1;
      },
    },
    {
      title: '隐患名称',
      dataIndex: 'dangerName',
      width: 180,
    },
    {
      title: '隐患位置',
      dataIndex: 'location',
      width: 180,
    },
    {
      title: '处置时间',
      dataIndex: 'disposalTime',
      width: 160,
    },
    {
      title: '处置人员',
      dataIndex: 'disposalUser',
      width: 120,
    },
    {
      title: '处置方法',
      dataIndex: 'disposalMethod',
      width: 120,
    },
    {
      title: '处置状态',
      dataIndex: 'disposalStatus',
      width: 100,
    },
    {
      title: '处置内容',
      dataIndex: 'disposalContent',
      ellipsis: true,
    },
    {
      title: '创建时间',
      dataIndex: 'createDate',
      width: 160,
    },
    {
      title: '操作',
      dataIndex: 'action',
      width: 200,
      fixed: 'right' as 'right',
    },
  ];

  // 搜索表单配置
  const searchForm: FormProps = {
    baseColProps: { lg: 6, md: 8 },
    labelWidth: 80,
    schemas: searchFormSchema,
    autoSubmitOnEnter: true,
  };
  //标题和上面导航栏保持一致
  const pageTitle = computed(() => {
    // 假设你通过路由元信息定义了页面标题
    return router.currentRoute.value.meta.title || '隐患处置列表';
  });
  // 注册表格
  const [registerTable, { reload, setLoading, getPaginationRef }] = useTable({
    title: pageTitle.value,
    api: getDangerDisposalList,
    columns,
    rowKey: 'id',
    formConfig: searchForm,
    useSearchForm: true,
    showTableSetting: true,
    canResize: true,
    showIndexColumn: false,
    pagination: {
      pageSize: 10,
      defaultPageSize: 10,
    },
  });

  // 新增处置记录
  function handleCreate() {
    openModal(true, {
      isUpdate: false,
    });
  }

  // 查看处置记录
  function handleView(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: false,
      isView: true,
    });
  }

  // 编辑处置记录
  function handleEdit(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  // 删除处置记录
  async function handleDelete(record: Recordable) {
    try {
      setLoading(true);
      await deleteDangerDisposal({ id: record.id });
      createMessage.success('删除成功');
      reload();
    } catch (error) {
      console.error('删除处置记录失败', error);
      createMessage.error('删除失败');
    } finally {
      setLoading(false);
    }
  }

  // 处理成功
  function handleSuccess() {
    reload();
  }
</script>

<style lang="less" scoped>
  .danger-disposal-container {
    height: 100%;
    display: flex;
    flex-direction: column;

    .action-buttons {
      display: flex;
      gap: 4px;
      justify-content: center;
    }

    .ml-1 {
      margin-left: 4px;
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
  }
</style>
