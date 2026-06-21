<!--
  @author zwf
  @date 2025-6-18
-->
<template>
  <div class="alarm-config-container">
    <BasicTable @register="registerTable">
      <template #tableTitle>
        <div class="page-header">
          <div class="page-title">报警配置</div>
        </div>
      </template>

      <template #toolbar>
        <a-button type="primary" @click="handleCreate">新增</a-button>
      </template>

      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'enableAlarm'">
          <Tag color="success" v-if="record.enableAlarm == 1 || record.enableAlarm === '1'">是</Tag>
          <Tag color="default" v-else>否</Tag>
        </template>
        <template v-else-if="column.key === 'needConfirm'">
          <Tag color="success" v-if="record.needConfirm == 1 || record.needConfirm === '1'">是</Tag>
          <Tag color="default" v-else>否</Tag>
        </template>
        <template v-else-if="column.key === 'isSendZjt'">
          <Tag color="success" v-if="record.isSendZjt == 1 || record.isSendZjt === '1'">是</Tag>
          <Tag color="default" v-else>否</Tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <TableAction :actions="[
              {
                icon: 'clarity:note-edit-line',
                tooltip: '编辑',
                onClick: handleEdit.bind(null, record),
              },
              {
                icon: 'streamline:interface-calendar-upload-calendar-date-day-month-push-up-arrow-upload',
                tooltip: '中间通推送',
                onClick: openModalPushF.bind(null, record),
              },
              {
                icon: 'ant-design:delete-outlined',
                color: 'error',
                tooltip: '删除',
                popConfirm: {
                  title: '是否确认删除?',
                  confirm: handleDelete.bind(null, record),
                },
              },
            ]" />
        </template>
      </template>
    </BasicTable>
    <AlarmConfigModal @register="registerModal" @success="handleSuccess" />
    <AlarmConfigFormModal @register="registerPushModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicTable, useTable, TableAction } from '@/components/swm/Table';
  import { useModal } from '@/components/swm/Modal';
  import AlarmConfigModal from './AlarmConfigModal.vue';
  import { Tag } from 'ant-design-vue';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { getAlarmConfigList, deleteAlarmConfig } from '@/api/swm/alarmConfig';
  import AlarmConfigFormModal from './form.vue';

  export default defineComponent({
    name: 'ViewsSwmAlarmConfigAlarmConfigList',
    components: { BasicTable, AlarmConfigModal, TableAction, Tag, AlarmConfigFormModal },
    setup() {
      const [registerPushModal, { openModal: openPushModal }] = useModal();
      const { createMessage } = useMessage();
      const [registerModal, { openModal }] = useModal();

      const [registerTable, { reload }] = useTable({
        title: '报警配置列表',
        api: getAlarmConfigList,
        rowKey: 'id',
        columns: [
          {
            title: '报警名称',
            dataIndex: 'alarmName',
            align: 'center',
            width: 150,
          },
          {
            title: 'key',
            dataIndex: 'alarmKey',
            align: 'center',
            width: 80,
          },
          {
            title: '是否报警',
            dataIndex: 'enableAlarm',
            key: 'enableAlarm',
            align: 'center',
            width: 100,
          },
          {
            title: '是否弹框确认',
            dataIndex: 'needConfirm',
            key: 'needConfirm',
            align: 'center',
            width: 120,
          },
          {
            title: '是否中建通推送消息',
            dataIndex: 'isSendZjt',
            key: 'isSendZjt',
            align: 'center',
            width: 120,
          },
          {
            title: '操作',
            dataIndex: 'action',
            key: 'action',
            align: 'center',
            width: 180,
            fixed: 'right',
          },
        ],
        showTableSetting: true,
        canResize: true,
      });

      function handleCreate() {
        openModal(true, {
          isUpdate: false,
        });
      }

      function handleEdit(record: Recordable) {
        openModal(true, {
          record,
          isUpdate: true,
        });
      }

      function handleDelete(record: Recordable) {
        deleteAlarmConfig(record.id)
          .then(() => {
            createMessage.success('删除成功');
            reload();
          })
          .catch((err) => {
            createMessage.error('删除失败: ' + err.message);
          });
      }

      function handleSuccess() {
        reload();
      }
      function openModalPushF(record: Recordable) {
        // console.log('record', record);
        openPushModal(true, record);
      }

      return {
        registerPushModal,
        registerTable,
        registerModal,
        handleCreate,
        handleEdit,
        handleDelete,
        openModalPushF,
        handleSuccess,
      };
    },
  });
</script>

<style scoped>
  .alarm-config-container {
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    background-color: #fff;
  }

  .page-header {
    display: flex;
    align-items: center;
    margin-right: 16px;
  }

  .page-title {
    font-size: 16px;
    font-weight: 500;
    margin-right: 12px;
  }
</style>