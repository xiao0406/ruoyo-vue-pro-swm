<template>
  <div class="hidden-danger-container">
    <!-- 表格 -->
    <BasicTable @register="registerTable">
      <!-- 操作按钮 -->
      <template #toolbar>
        <a-button type="primary" @click="handleCreate">新建隐患</a-button>
      </template>

      <!-- 表格列自定义 -->
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'isBeaconDeployed'">
          <DictLabel dictType="is_beacon_deployed_enum" :dictValue="record.isBeaconDeployed" />
        </template>
        <template v-else-if="column.dataIndex === 'isHandled'">
          <DictLabel dictType="is_handled_enum" :dictValue="record.isHandled" />
        </template>
        <template v-else-if="column.dataIndex === 'action'">
          <div class="action-buttons">
            <a-button size="small" type="primary" @click="handleView(record)">查看</a-button>
            <a-button size="small" type="warning" class="ml-1" @click="handleEdit(record)"
              >编辑</a-button
            >
            <a-button
              v-if="record.isHandled !== '1'"
              size="small"
              type="success"
              class="ml-1"
              @click="handleDisposal(record)"
              >处置</a-button
            >
            <a-popconfirm
              title="确定要删除这条隐患信息吗？"
              @confirm="handleDelete(record)"
              okText="确定"
              cancelText="取消"
            >
              <a-button size="small" type="danger" class="ml-1">删除</a-button>
            </a-popconfirm>
          </div>
        </template>
      </template>
    </BasicTable>

    <!-- 新建/编辑弹窗 -->
    <HiddenDangerDrawer @register="registerModal" @success="handleSuccess" />

    <!-- 隐患处置弹窗 -->
    <DangerDisposalDrawer @register="registerDisposalModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" setup>
  import { ref, computed, onMounted } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { BasicTable, useTable } from '@/components/swm/Table';
  import { useModal } from '@/components/swm/Modal';
  import { FormProps } from '@/components/swm/Form';
  import { getHiddenDangerList, deleteHiddenDanger } from '@/api/swm/hiddenDanger';
  import { searchFormSchema } from './hiddenDangerData';
  import HiddenDangerDrawer from './HiddenDangerDrawer.vue';
  import DangerDisposalDrawer from '../dangerDisposal/DangerDisposalDrawer.vue';
  import { DictLabel, useDict } from '@/components/swm/Dict';
  import { router } from '@/router';

  // 初始化字典
  const { initDict } = useDict();
  onMounted(() => {
    initDict(['is_beacon_deployed_enum', 'is_handled_enum']);
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
      title: '关联巡检计划',
      dataIndex: 'inspectionPlanName',
      width: 150,
    },
    {
      title: '是否布设信标',
      dataIndex: 'isBeaconDeployed',
      width: 120,
    },
    {
      title: '是否已处置',
      dataIndex: 'isHandled',
      width: 120,
    },
    {
      title: '创建时间',
      dataIndex: 'createDate',
      width: 180,
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

  const { createMessage, createConfirm } = useMessage();
  const [registerModal, { openModal }] = useModal();
  const [registerDisposalModal, { openModal: openDisposalModal }] = useModal();
  //标题和上面导航栏保持一致
  const pageTitle = computed(() => {
    // 假设你通过路由元信息定义了页面标题
    return router.currentRoute.value.meta.title || '隐患列表';
  });
  // 注册表格
  const [registerTable, { reload, setLoading, getPaginationRef }] = useTable({
    title: pageTitle.value,
    api: getHiddenDangerList,
    columns,
    rowKey: 'id',
    formConfig: searchForm,
    useSearchForm: true,
    canResize: true,
    showTableSetting: true,
    showIndexColumn: false,
    pagination: {
      pageSize: 10,
      defaultPageSize: 10,
    },
  });

  // 新建隐患
  function handleCreate() {
    openModal(true, {
      isUpdate: false,
    });
  }

  // 查看隐患
  function handleView(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: false,
      isView: true,
    });
  }

  // 编辑隐患
  function handleEdit(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  // 删除隐患
  async function handleDelete(record: Recordable) {
    try {
      setLoading(true);
      await deleteHiddenDanger({ id: record.id });
      createMessage.success('删除成功');
      reload();
    } catch (error) {
      console.error('删除隐患信息失败', error);
      createMessage.error('删除失败');
    } finally {
      setLoading(false);
    }
  }

  // 处理隐患处置
  function handleDisposal(record: Recordable) {
    openDisposalModal(true, {
      hiddenDangerId: record.id,
      dangerName: record.dangerName,
      location: record.location,
      isUpdate: false,
    });
  }

  // 处理成功
  function handleSuccess() {
    reload();
  }
</script>

<style lang="less" scoped>
  .hidden-danger-container {
    height: 100%;
    display: flex;
    flex-direction: column;

    .page-header {
      display: flex;
      align-items: center;
      margin-bottom: 16px;

      .page-title {
        font-size: 18px;
        font-weight: bold;
      }
    }

    .action-buttons {
      display: flex;
      gap: 4px;
      justify-content: center;
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
