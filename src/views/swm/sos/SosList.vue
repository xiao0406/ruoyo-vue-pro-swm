<!--
  @author zwf
  @date 2025-06-22
-->
<template>
  <div class="sos-list-container">
    <BasicTable @register="registerTable">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'warningContent'">
          <Tag color="error">{{ record.warningContent }}</Tag>
        </template>
        <template v-else-if="column.key === 'handleStatus'">
          <Tag :color="getHandleStatusColor(record.handleStatus)">
            {{ record.handleStatusText || getHandleStatusText(record.handleStatus) }}
          </Tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <TableAction :actions="getActions(record)" />
        </template>
      </template>
    </BasicTable>
    <WarningModal @register="registerModal" @success="handleSuccess" />
    <WarningProcessModal @register="registerProcessModal" @success="handleSuccess" />
    <WarningRecordModal @register="registerRecordModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref, computed } from 'vue';
  import { BasicTable, useTable, TableAction } from '@/components/swm/Table';
  import { getSOSList, getWarning, sendSOSAlarm } from '@/api/swm/sos';
  import { useModal } from '@/components/swm/Modal';
  import WarningModal from '../warning/WarningModal.vue';
  import WarningProcessModal from '../warning/WarningProcessModal.vue';
  import WarningRecordModal from '../warning/WarningRecordModal.vue';
  import { ThunderboltOutlined } from '@ant-design/icons-vue';
  import { Tag } from 'ant-design-vue';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { useDict } from '@/components/swm/Dict';
  import { router } from '@/router';

  export default defineComponent({
    name: 'ViewsSwmSosSosList',
    components: {
      BasicTable,
      WarningModal,
      WarningProcessModal,
      WarningRecordModal,
      TableAction,
      Tag,
      ThunderboltOutlined,
    },
    setup() {
      const { createMessage } = useMessage();
      const { initDict } = useDict();
      const [registerModal, { openModal }] = useModal();
      const [registerProcessModal, { openModal: openProcessModal }] = useModal();
      const [registerRecordModal, { openModal: openRecordModal }] = useModal();
      const sendingSOSLoading = ref(false);

      // 初始化字典数据
      initDict(['handle_status_enum']);
      //标题和上面导航栏保持一致
      const pageTitle = computed(() => {
        // 假设你通过路由元信息定义了页面标题
        return router.currentRoute.value.meta.title || '应急呼叫';
      });
      const [registerTable, { reload }] = useTable({
        title: pageTitle.value,
        api: getSOSList,
        rowKey: 'id',
        columns: [
          {
            title: '人员姓名',
            dataIndex: 'personName',
            width: 120,
          },
          {
            title: '报警内容',
            dataIndex: 'warningContent',
            key: 'warningContent',
            width: 120,
          },
          {
            title: '报警时间',
            dataIndex: 'warningTime',
            width: 180,
          },
          {
            title: '触发地点',
            dataIndex: 'triggerReason',
            width: 200,
          },
          {
            title: '处置状态',
            dataIndex: 'handleStatus',
            key: 'handleStatus',
            width: 100,
          },
          {
            title: '操作',
            dataIndex: 'action',
            key: 'action',
            width: 180,
            fixed: 'right',
          },
        ],
        showTableSetting: true,
        useSearchForm: true,
        formConfig: {
          baseColProps: { lg: 6, md: 8 },
          labelWidth: 100,
          schemas: [
            {
              field: 'personName',
              label: '人员姓名',
              component: 'Input',
              colProps: { span: 8 },
            },
            {
              field: 'handleStatus',
              label: '处置状态',
              component: 'Select',
              componentProps: {
                dictType: 'handle_status_enum',
                allowClear: true,
              },
              colProps: { span: 8 },
            },
          ],
        },
        canResize: true,
      });

      function getHandleStatusColor(status) {
        if (status === '1') {
          return 'success';
        } else if (status === '0') {
          return 'warning';
        }
        return 'default';
      }

      function getHandleStatusText(status) {
        if (status === '1') {
          return '已处置';
        } else {
          return '未处置';
        }
      }

      function handleView(record: Recordable) {
        openModal(true, {
          record,
          isView: true,
        });
      }

      function handleProcess(record: Recordable) {
        // 先调用form接口获取最新数据
        getWarning(record.id)
          .then((res) => {
            // 获取最新数据后打开处置对话框
            openProcessModal(true, {
              record: res || record, // 如果获取成功就用最新数据，否则用原始记录
              isUpdate: false,
            });
          })
          .catch((error) => {
            createMessage.error('获取应急呼叫数据失败');
            console.error('获取应急呼叫数据失败', error);
          });
      }

      function handleViewProcess(record: Recordable) {
        openRecordModal(true, {
          record,
        });
      }

      function getActions(record: Recordable) {
        const actions = [
          {
            icon: 'ant-design:eye-outlined',
            tooltip: '查看',
            onClick: handleView.bind(null, record),
          },
        ];

        // 根据处置状态决定显示哪个按钮
        if (record.handleStatus === '1') {
          // 已处置状态 - 显示处置记录
          actions.push({
            icon: 'ant-design:file-text-outlined',
            tooltip: '处置记录',
            onClick: handleViewProcess.bind(null, record),
          });
        } else {
          // 未处置状态 - 显示新增处置
          actions.push({
            icon: 'ant-design:safety-outlined',
            tooltip: '新增处置',
            onClick: handleProcess.bind(null, record),
          });
        }

        return actions;
      }

      // 发送SOS报警
      async function handleSendSOS() {
        try {
          sendingSOSLoading.value = true;
          await sendSOSAlarm();
          createMessage.success('应急呼叫信号已发送，工作人员将尽快处理');
          reload(); // 刷新列表
        } catch (error) {
          console.error('发送应急呼叫失败', error);
          createMessage.error('发送应急呼叫失败，请稍后再试');
        } finally {
          sendingSOSLoading.value = false;
        }
      }

      function handleSuccess() {
        reload();
      }

      return {
        registerTable,
        registerModal,
        registerProcessModal,
        registerRecordModal,
        sendingSOSLoading,
        handleView,
        handleProcess,
        handleViewProcess,
        handleSuccess,
        handleSendSOS,
        getHandleStatusColor,
        getHandleStatusText,
        getActions,
      };
    },
  });
</script>

<style lang="less" scoped>
  .sos-list-container {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .sos-action-panel {
    margin-bottom: 24px;
    padding: 24px;
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    text-align: center;
  }

  .sos-description {
    margin-top: 12px;
    color: #666;
    font-size: 14px;
  }

  .page-header {
    margin-bottom: 16px;
  }

  .page-title {
    font-size: 18px;
    font-weight: bold;
    color: #303133;
  }
</style>
