<template>
  <div class="alarm-light-container">
    <BasicTable @register="registerTable" @edit="handleEdit" @delete="handleDelete">
      <template #toolbar>
        <a-button type="primary" @click="handleCreate">新增报警灯</a-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'enableAlarm'">
          <a-tag :color="record.enableAlarm === '1' ? 'green' : 'red'">
            {{ record.enableAlarm === '1' ? '是' : '否' }}
          </a-tag>
        </template>
        <template v-if="column.key === 'alarmTypeNames'">
          <div v-if="record.alarmTypeNames" class="alarm-types-container">
            <a-tooltip placement="topLeft" :title="getTooltipContent(record.alarmTypeNames)">
              <!-- 显示的标签（限制数量） -->
              <span class="alarm-tags-wrapper">
                <a-tag
                  v-for="(typeName, index) in getDisplayAlarmTypes(record.alarmTypeNames)"
                  :key="index"
                  color="blue"
                  class="alarm-type-tag"
                >
                  {{ typeName }}
                </a-tag>

                <!-- 如果有更多，显示省略提示 -->
                <span v-if="hasMoreAlarmTypes(record.alarmTypeNames)" class="more-types-hint">
                  等{{ getAlarmTypeList(record.alarmTypeNames).length }}个
                </span>
              </span>
            </a-tooltip>
          </div>
          <span v-else class="no-config-text">暂无配置</span>
        </template>
        <template v-if="column.key === 'action'">
          <TableAction
            :actions="[
              {
                icon: 'clarity:settings-line',
                tooltip: '配置',
                onClick: handleConfig.bind(null, record),
              },
              {
                icon: 'clarity:note-edit-line',
                tooltip: '编辑',
                onClick: handleEdit.bind(null, record),
              },
              {
                icon: 'ant-design:delete-outlined',
                color: 'error',
                tooltip: '删除',
                popConfirm: {
                  title: '是否确认删除',
                  placement: 'left',
                  confirm: handleDelete.bind(null, record),
                },
              },
            ]"
          />
        </template>
      </template>
    </BasicTable>
    <AlarmLightModal @register="registerModal" @success="handleSuccess" />
    <AlarmLightConfigModal @register="registerConfigModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref, onMounted } from 'vue';
  import { BasicTable, useTable, TableAction } from '@/components/swm/Table';
  import { useModal } from '@/components/swm/Modal';
  import AlarmLightModal from './AlarmLightModal.vue';
  import AlarmLightConfigModal from './AlarmLightConfigModal.vue';
  import { getAlarmLightList, deleteAlarmLight, getAlarmConfigList } from '@/api/swm/alarmLight';
  import { useMessage } from '@/hooks/swm/useMessage';

  export default defineComponent({
    name: 'ViewsSwmAlarmLightIndex',
    components: { BasicTable, TableAction, AlarmLightModal, AlarmLightConfigModal },
    setup() {
      const [registerModal, { openModal }] = useModal();
      const [registerConfigModal, { openModal: openConfigModal }] = useModal();
      const { createMessage } = useMessage();
      const alarmTypeOptions = ref([]);

      const columns = [
        {
          title: '报警灯名称',
          dataIndex: 'lightName',
          key: 'lightName',
          width: 150,
        },
        {
          title: 'SN码',
          dataIndex: 'snCode',
          key: 'snCode',
          width: 120,
        },
        {
          title: '是否报警',
          dataIndex: 'enableAlarm',
          key: 'enableAlarm',
          width: 100,
        },
        {
          title: '报警类型',
          dataIndex: 'alarmTypeNames',
          key: 'alarmTypeNames',
          width: 200,
        },
        {
          title: '创建时间',
          dataIndex: 'createDate',
          key: 'createDate',
          width: 160,
        },
        {
          title: '备注',
          dataIndex: 'remarks',
          key: 'remarks',
          ellipsis: true,
        },
      ];

      const [registerTable, { reload }] = useTable({
        title: '报警灯管理',
        api: getAlarmLightList,
        rowKey: 'id',
        columns,
        scroll: {
          x: 1200,
        },
        canResize: true,
        showTableSetting: true,
        useSearchForm: true,
        formConfig: {
          baseColProps: { lg: 6, md: 8 },
          labelWidth: 100,
          schemas: [
            {
              field: 'lightName',
              label: '报警灯名称',
              component: 'Input',
              colProps: { span: 8 },
            },
            {
              field: 'snCode',
              label: 'SN码',
              component: 'Input',
              colProps: { span: 8 },
            },
            {
              field: 'enableAlarm',
              label: '是否报警',
              component: 'Select',
              componentProps: {
                options: [
                  { label: '是', value: '1' },
                  { label: '否', value: '0' },
                ],
              },
              colProps: { span: 8 },
            },
            {
              field: 'alarmTypeName',
              label: '报警类型',
              component: 'Select',
              componentProps: {
                options: alarmTypeOptions,
                placeholder: '请选择报警类型',
                allowClear: true,
              },
              colProps: { span: 8 },
            },
          ],
        },
        useSearchForm: true,
        showTableSetting: true,
        bordered: true,
        actionColumn: {
          width: 160,
          title: '操作',
          dataIndex: 'action',
        },
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

      function handleConfig(record: Recordable) {
        openConfigModal(true, {
          record,
        });
      }

      async function handleDelete(record: Recordable) {
        try {
          await deleteAlarmLight(record.id);
          createMessage.success('删除成功');
          reload();
        } catch (error) {
          createMessage.error('删除失败');
        }
      }

      function handleSuccess() {
        reload();
      }

      function getAlarmTypeList(alarmTypeNames: string) {
        if (!alarmTypeNames) return [];
        return alarmTypeNames.split(',').filter((name) => name.trim());
      }

      function getDisplayAlarmTypes(alarmTypeNames: string, maxDisplay = 3) {
        const allTypes = getAlarmTypeList(alarmTypeNames);
        return allTypes.slice(0, maxDisplay);
      }

      function hasMoreAlarmTypes(alarmTypeNames: string, maxDisplay = 3) {
        const allTypes = getAlarmTypeList(alarmTypeNames);
        return allTypes.length > maxDisplay;
      }

      function getTooltipContent(alarmTypeNames: string) {
        const allTypes = getAlarmTypeList(alarmTypeNames);
        return allTypes.join('、');
      }

      async function loadAlarmTypeOptions() {
        try {
          const result = await getAlarmConfigList({ pageSize: 1000, status: '0' });
          if (result && Array.isArray(result.list)) {
            alarmTypeOptions.value = result.list.map((item) => ({
              label: item.alarmName,
              value: item.alarmName,
            }));
          }
        } catch (error) {
          console.error('加载报警类型选项失败:', error);
        }
      }

      onMounted(() => {
        loadAlarmTypeOptions();
      });

      return {
        registerTable,
        registerModal,
        registerConfigModal,
        handleCreate,
        handleEdit,
        handleConfig,
        handleDelete,
        handleSuccess,
        getAlarmTypeList,
        getDisplayAlarmTypes,
        hasMoreAlarmTypes,
        getTooltipContent,
      };
    },
  });
</script>

<style lang="less" scoped>
  .alarm-light-container {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .alarm-types-container {
    .alarm-tags-wrapper {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 6px;
    }

    .alarm-type-tag {
      margin: 0;
    }

    .more-types-hint {
      color: #666;
      font-size: 12px;
      white-space: nowrap;
    }
  }

  .no-config-text {
    color: #999;
    font-style: italic;
  }
</style>
