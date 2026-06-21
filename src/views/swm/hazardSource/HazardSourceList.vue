<template>
  <div>
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleCreate">
          <Icon icon="ant-design:plus-outlined" /> 新增
        </a-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column && column.key === 'action'">
          <TableAction
            :actions="[
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
                  confirm: handleDelete.bind(null, record),
                },
              },
            ]"
          />
        </template>
        <template v-else-if="column && column.key === 'inspectionRecords'">
          <a-button
            v-if="record.isPatrolIncluded === '1'"
            type="link"
            @click="handleViewInspectionRecords(record)"
          >
            查看记录
          </a-button>
          <span v-else>-</span>
        </template>
        <template v-else-if="column && column.dataIndex === 'isPatrolIncluded'">
          <DictLabel dictType="is_patrol_included_enum" :dictValue="record.isPatrolIncluded" />
        </template>
        <template v-else-if="column && column.dataIndex === 'hazardStatus'">
          <DictLabel dictType="hazard_status_enum" :dictValue="record.hazardStatus" />
        </template>
        <template v-else-if="column && column.dataIndex === 'hazardCategory'">
          <DictLabel dictType="hazard_category_enum" :dictValue="record.hazardCategory" />
        </template>
        <template v-else-if="column && column.dataIndex === 'beaconIdentifier'">
          <span v-if="record.beaconIdentifierText">{{ record.beaconIdentifierText }}</span>
          <span v-else-if="record.beaconIdentifier">{{ record.beaconIdentifier }}</span>
          <span v-else>-</span>
        </template>
      </template>
    </BasicTable>
    <HazardSourceModal @register="registerModal" @success="handleSuccess" />
    <InspectionRecordModal @register="registerInspectionModal" />
  </div>
</template>

<script lang="ts">
  /**
   * 风险列表页面
   * @author Shawn
   * @date 2025-05-22
   */
  import { defineComponent, onMounted, computed, h } from 'vue';
  import { BasicTable, useTable, TableAction } from '@/components/swm/Table';
  import { getHazardSourceList, deleteHazardSource } from '@/api/swm/hazardSource';
  import { useModal } from '@/components/swm/Modal';
  import HazardSourceModal from './HazardSourceModal.vue';
  import { Icon } from '@/components/swm/Icon';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { DictLabel, useDict } from '@/components/swm/Dict';
  import { formatToDateTime } from '@/utils/dateUtil';
  import { isNil } from 'lodash-es';
  import { router } from '@/router';
  import InspectionRecordModal from './InspectionRecordModal.vue';

  export default defineComponent({
    name: 'ViewsSwmHazardSourceHazardSourceList',
    components: {
      BasicTable,
      TableAction,
      Icon,
      HazardSourceModal,
      DictLabel,
      InspectionRecordModal,
    },
    setup() {
      const { createMessage } = useMessage();
      const [registerModal, { openModal }] = useModal();
      const [registerInspectionModal, { openModal: openInspectionModal }] = useModal();
      const { initDict } = useDict();

      // 初始化字典数据
      onMounted(() => {
        initDict(['is_patrol_included_enum', 'hazard_status_enum', 'hazard_category_enum']);
      });
      //标题和上面导航栏保持一致
      const pageTitle = computed(() => {
        // 假设你通过路由元信息定义了页面标题
        return router.currentRoute.value.meta.title || '危险区域管理';
      });
      const [registerTable, { reload }] = useTable({
        title: pageTitle.value,
        api: getHazardSourceList,
        rowKey: 'id',
        columns: [
          {
            title: '危险区域名称',
            dataIndex: 'hazardName',
            width: 150,
          },
          {
            title: '危险区域类别',
            dataIndex: 'hazardCategory',
            width: 100,
          },
          {
            title: '位置',
            dataIndex: 'location',
            width: 180,
          },
          {
            title: '所属信标',
            dataIndex: 'beaconIdentifier',
            width: 180,
            ellipsis: true, // 文本过长时显示省略号
          },
          {
            title: '是否加入巡检',
            dataIndex: 'isPatrolIncluded',
            width: 100,
          },
          {
            title: '巡检记录',
            key: 'inspectionRecords',
            width: 100,
          },
          {
            title: '登记时间',
            dataIndex: 'registrationTime',
            width: 150,
            customRender: ({ text }) => {
              return text ? formatToDateTime(text) : '';
            },
          },
          {
            title: '危险区域状态',
            dataIndex: 'hazardStatusText',
            width: 100,
            customRender: ({ record }) => {
              const isDraft = record.isDraft === '1';
              if (isDraft) {
                return h('span', { style: { color: '#faad14' } }, '草稿');
              }
              return record.hazardStatusText;
            },
          },
          {
            title: '语音模板',
            dataIndex: 'voiceTemplateText',
            width: 120,
          },
          // {
          //   title: '信标标记',
          //   dataIndex: 'beaconTag',
          //   width: 120,
          // },
          {
            title: '操作',
            dataIndex: 'action',
            key: 'action',
            width: 100,
            fixed: 'right',
          },
        ],
        showTableSetting: true,
        canResize: true,
        useSearchForm: true,
        formConfig: {
          baseColProps: { lg: 6, md: 8 },
          labelWidth: 100,
          schemas: [
            {
              field: 'hazardName',
              label: '危险区域名称',
              component: 'Input',
              colProps: { span: 8 },
            },
            {
              field: 'hazardCategory',
              label: '危险区域类别',
              component: 'Select',
              componentProps: {
                dictType: 'hazard_category_enum',
                allowClear: true,
                placeholder: '请选择危险区域类别',
              },
              colProps: { span: 8 },
            },
            {
              field: 'beaconIdentifier',
              label: '所属信标',
              component: 'Input',
              componentProps: {
                placeholder: '可输入设备名称或MAC地址，多个用逗号分隔',
              },
              colProps: { span: 8 },
            },
            {
              field: 'isPatrolIncluded',
              label: '是否加入巡检',
              component: 'Select',
              componentProps: {
                dictType: 'is_patrol_included_enum',
                allowClear: true,
              },
              colProps: { span: 8 },
            },
            {
              field: 'hazardStatus',
              label: '危险区域状态',
              component: 'Select',
              componentProps: {
                dictType: 'hazard_status_enum',
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
            // {
            //   field: 'beaconTag',
            //   label: '信标标记',
            //   component: 'Input',
            //   colProps: { span: 8 },
            // },
          ],
        },
      });

      // 新增
      function handleCreate() {
        openModal(true, {
          isUpdate: false,
        });
      }

      // 编辑
      function handleEdit(record: Recordable) {
        openModal(true, {
          record,
          isUpdate: true,
        });
      }

      // 删除
      async function handleDelete(record: Recordable) {
        await deleteHazardSource({ id: record.id });
        createMessage.success('删除成功');
        reload();
      }

      // 成功回调
      function handleSuccess() {
        reload();
      }

      // 查看巡检记录
      function handleViewInspectionRecords(record: Recordable) {
        openInspectionModal(true, {
          hazardSourceId: record.id,
        });
      }

      return {
        registerTable,
        registerModal,
        registerInspectionModal,
        handleCreate,
        handleEdit,
        handleDelete,
        handleSuccess,
        handleViewInspectionRecords,
      };
    },
  });
</script>
