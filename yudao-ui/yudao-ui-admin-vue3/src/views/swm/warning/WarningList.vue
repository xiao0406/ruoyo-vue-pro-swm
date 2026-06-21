<!--
  @author zwf
  @date 2025-05-16
-->
<template>
  <div class="warning-list-container">
    <BasicTable @register="registerTable">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'warningType'">
          <Tag :color="getWarningTypeColor(record.warningType)">
            <DictLabel dictType="warning_type_enum" :dictValue="record.warningType" />
          </Tag>
        </template>
        <template v-else-if="column.key === 'warningContent'">
          <DictLabel
            dictType="warning_content_enum"
            :dictValue="record.warningContent"
            defaultValue="--"
          />
        </template>
        <template v-else-if="column.key === 'handleStatus'">
          <Tag :color="getHandleStatusColor(record.handleStatus)">
            {{ record.handleStatusText || getHandleStatusText(record.handleStatus) }}
          </Tag>
        </template>
        <template v-else-if="column.key === 'hazardCategory'">
          <DictLabel
            dictType="hazard_category_enum"
            :dictValue="record.hazardCategory"
            defaultValue="--"
          />
        </template>
        <template v-else-if="column.key === 'coordinates'">
          <Tooltip v-if="record.x && record.y" :title="`X: ${record.x}, Y: ${record.y}`">
            <span style="font-family: monospace; color: #1890ff; cursor: pointer">
              ({{ formatCoordinate(record.x) }}, {{ formatCoordinate(record.y) }})
            </span>
          </Tooltip>
          <span v-else>--</span>
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
  import { getWarningList, getWarning } from '@/api/swm/warning';
  import { useModal } from '@/components/swm/Modal';
  import WarningModal from './WarningModal.vue';
  import WarningProcessModal from './WarningProcessModal.vue';
  import WarningRecordModal from './WarningRecordModal.vue';
  import { Icon } from '@/components/swm/Icon';
  import { Tag, Tooltip } from 'ant-design-vue';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { DictLabel, useDict } from '@/components/swm/Dict';
  import { router } from '@/router';

  export default defineComponent({
    name: 'ViewsSwmWarningWarningList',
    components: {
      BasicTable,
      WarningModal,
      WarningProcessModal,
      WarningRecordModal,
      TableAction,
      Icon,
      Tag,
      Tooltip,
      DictLabel,
    },
    setup() {
      const { createMessage } = useMessage();
      const { initDict, getDictList } = useDict();

      // 预警类型选项的响应式数据
      const warningContentOptions = ref<Array<{ label: string; value: string; key: string }>>([]);

      // 初始化字典数据并设置选项
      const initDictData = async () => {
        await initDict([
          'hazard_category_enum',
          'warning_content_enum',
          'warning_type_enum',
          'handle_status_enum',
        ]);

        // 字典初始化完成后，获取过滤后的报警类型选项
        const dictList = getDictList('warning_content_enum');
        warningContentOptions.value = dictList
          .filter(
            (item) =>
              item.name !== '考勤打卡' &&
              item.value !== '考勤打卡' && // 过滤掉考勤打卡
              item.name !== '进入大门' &&
              item.value !== '进入大门', // 过滤掉进入大门
          )
          .map((item) => ({
            label: item.name,
            value: item.value,
            key: item.id,
          }));
      };

      // 调用初始化函数
      initDictData();

      const [registerModal, { openModal }] = useModal();
      const [registerProcessModal, { openModal: openProcessModal }] = useModal();
      const [registerRecordModal, { openModal: openRecordModal }] = useModal();
      //标题和上面导航栏保持一致

      const pageTitle = computed(() => {
        // 假设你通过路由元信息定义了页面标题
        return router.currentRoute.value.meta.title || '报警记录';
      });
      const [registerTable, { reload }] = useTable({
        title: pageTitle.value,
        api: getWarningList,
        rowKey: 'id',
        columns: [
          {
            title: '报警单号',
            dataIndex: 'id',
            width: 180,
            fixed: 'left',
          },
          {
            title: '人员姓名',
            dataIndex: 'personName',
            width: 120,
          },
          {
            title: '报警性质',
            dataIndex: 'warningType',
            key: 'warningType',
            width: 120,
          },
          {
            title: '报警类型',
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
            title: '危险区域类别',
            dataIndex: 'hazardCategory',
            key: 'hazardCategory',
            width: 120,
            customRender: ({ text }) => {
              return text || '--';
            },
          },

          {
            title: '位置',
            dataIndex: 'location',
            width: 120,
            customRender: ({ text }) => text || '--',
          },
          {
            title: '区域',
            dataIndex: 'area',
            width: 120,
            customRender: ({ text }) => text || '--',
          },
          {
            title: '处置状态',
            dataIndex: 'handleStatus',
            key: 'handleStatus',
            width: 100,
          },
          {
            title: '处置时长(分钟)',
            dataIndex: 'disposalDuration',
            width: 100,
            customRender: ({ text }) => {
              // 确保显示为"0"而不是空值，对于已处置的记录，显示处置时长，对于未处置的记录，显示"--"
              if (text !== undefined && text !== null) {
                return text;
              }
              return '--';
            },
          },
          {
            title: '报警说明',
            dataIndex: 'triggerReason',
            width: 200,
            customRender: ({ text }) => text || '--',
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
              field: 'id',
              label: '报警单号',
              component: 'Input',
              colProps: { span: 8 },
            },
            {
              field: 'personName',
              label: '人员姓名',
              component: 'Input',
              colProps: { span: 8 },
            },
            {
              field: 'warningType',
              label: '报警性质',
              component: 'Select',
              componentProps: {
                dictType: 'warning_type_enum',
                allowClear: true,
              },
              colProps: { span: 8 },
            },
            {
              field: 'warningContent',
              label: '报警类型',
              component: 'Select',
              componentProps: {
                options: warningContentOptions,
                allowClear: true,
              },
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
            {
              field: 'location',
              label: '位置',
              component: 'Input',
              colProps: { span: 8 },
            },
            {
              field: 'area',
              label: '区域',
              component: 'Input',
              colProps: { span: 8 },
            },
          ],
        },
        canResize: true,
      });

      function getWarningTypeColor(type) {
        if (type === '1') {
          return 'blue';
        } else if (type === '2') {
          return 'gold';
        }
        return 'default';
      }

      function getHandleStatusColor(status) {
        if (status === '1') {
          return 'success';
        } else if (status === '2') {
          return 'cyan';
        } else if (status === '0') {
          return 'warning';
        }
        return 'default';
      }

      function getHandleStatusText(status) {
        if (status === '1') {
          return '已处置';
        } else if (status === '2') {
          return '草稿';
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
            createMessage.error('获取报警数据失败');
            console.error('获取报警数据失败', error);
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

        // 如果是风险提示，只显示查看按钮，不显示处置相关按钮
        if (record.warningContent === '风险提示') {
          return actions;
        }

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

      function handleExport() {
        createMessage.success('导出成功');
      }

      function handleSuccess() {
        reload();
      }

      function formatCoordinate(value) {
        if (!value) return '--';
        return Number(value).toFixed(2);
      }

      return {
        registerTable,
        registerModal,
        registerProcessModal,
        registerRecordModal,
        handleView,
        handleProcess,
        handleViewProcess,
        handleSuccess,
        handleExport,
        getWarningTypeColor,
        getHandleStatusColor,
        getHandleStatusText,
        getActions,
        formatCoordinate,
        warningContentOptions,
      };
    },
  });
</script>

<style lang="less" scoped>
  .warning-list-container {
    height: 100%;
    display: flex;
    flex-direction: column;
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
