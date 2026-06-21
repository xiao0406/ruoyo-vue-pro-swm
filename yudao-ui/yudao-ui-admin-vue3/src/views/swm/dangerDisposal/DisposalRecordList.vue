<template>
  <div class="disposal-record-list">
    <a-card title="处置记录" :bordered="false">
      <template #extra>
        <a-button type="primary" @click="handleAddDisposal">添加处置</a-button>
      </template>
      <a-table
        :columns="columns"
        :data-source="disposalList"
        :loading="loading"
        :pagination="false"
        size="small"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'action'">
            <div class="action-buttons">
              <a-button size="small" type="primary" @click="handleViewDisposal(record)"
                >查看</a-button
              >
              <a-button size="small" type="warning" class="ml-1" @click="handleEditDisposal(record)"
                >编辑</a-button
              >
              <a-popconfirm
                title="确定要删除这条处置记录吗？"
                @confirm="handleDeleteDisposal(record)"
                okText="确定"
                cancelText="取消"
              >
                <a-button size="small" type="danger" class="ml-1">删除</a-button>
              </a-popconfirm>
            </div>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 处置弹窗 -->
    <DangerDisposalDrawer @register="registerDisposalModal" @success="loadDisposalList" />
  </div>
</template>

<script lang="ts" setup>
  import { ref, onMounted, defineProps, defineEmits, watch } from 'vue';
  import { useModal } from '@/components/swm/Modal';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { listByHiddenDangerId, deleteDangerDisposal } from '@/api/swm/dangerDisposal';
  import DangerDisposalDrawer from './DangerDisposalDrawer.vue';

  const props = defineProps({
    hiddenDangerId: {
      type: String,
      required: true,
    },
  });

  const emit = defineEmits(['reload']);
  const loading = ref(false);
  const disposalList = ref<any[]>([]);
  const { createMessage } = useMessage();

  // 处置弹窗
  const [registerDisposalModal, { openModal }] = useModal();

  // 表格列定义
  const columns = [
    {
      title: '处置时间',
      dataIndex: 'disposalTime',
      width: 160,
    },
    {
      title: '处置人员',
      dataIndex: 'disposalUser',
      width: 100,
    },
    {
      title: '处置方法',
      dataIndex: 'disposalMethod',
      width: 120,
      customRender: ({ text }) => {
        const methodMap = {
          '1': '现场整改',
          '2': '限期整改',
          '3': '挂牌督办',
          '4': '其他',
        };
        return methodMap[text] || text;
      },
    },
    {
      title: '处置状态',
      dataIndex: 'disposalStatus',
      width: 100,
      customRender: ({ text }) => {
        const statusMap = {
          '1': '处置中',
          '2': '已完成',
          '3': '已验收',
        };
        return statusMap[text] || text;
      },
    },
    {
      title: '处置内容',
      dataIndex: 'disposalContent',
      ellipsis: true,
    },
    {
      title: '操作',
      dataIndex: 'action',
      width: 200,
      fixed: 'right',
    },
  ];

  // 监听隐患ID变化
  watch(
    () => props.hiddenDangerId,
    (newVal) => {
      if (newVal) {
        loadDisposalList();
      }
    },
    { immediate: true },
  );

  // 加载处置记录
  async function loadDisposalList() {
    try {
      loading.value = true;
      const res = await listByHiddenDangerId({ hiddenDangerId: props.hiddenDangerId });
      disposalList.value = res || [];
    } catch (error) {
      console.error('加载处置记录失败', error);
    } finally {
      loading.value = false;
    }
  }

  // 添加处置记录
  function handleAddDisposal() {
    openModal(true, {
      hiddenDangerId: props.hiddenDangerId,
      isUpdate: false,
    });
  }

  // 查看处置记录
  function handleViewDisposal(record) {
    openModal(true, {
      record,
      isUpdate: false,
      isView: true,
    });
  }

  // 编辑处置记录
  function handleEditDisposal(record) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  // 删除处置记录
  async function handleDeleteDisposal(record) {
    try {
      loading.value = true;
      await deleteDangerDisposal({ id: record.id });
      createMessage.success('删除处置记录成功');
      await loadDisposalList();
    } catch (error) {
      console.error('删除处置记录失败', error);
      createMessage.error('删除处置记录失败');
    } finally {
      loading.value = false;
    }
  }
</script>

<style lang="less" scoped>
  .disposal-record-list {
    margin-top: 16px;

    .action-buttons {
      display: flex;
      gap: 4px;
    }

    .ml-1 {
      margin-left: 4px;
    }
  }
</style>
