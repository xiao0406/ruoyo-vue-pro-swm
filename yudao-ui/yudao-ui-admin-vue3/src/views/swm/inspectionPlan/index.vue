<template>
  <div>
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleCreate"> 新增 </a-button>
      </template>
      <template #planName="{ record }">
        <a @click="handleView(record)">{{ record.planName }}</a>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" />
      </template>
    </BasicTable>
    <InspectionPlanModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, computed } from 'vue';
  import { BasicTable, useTable, TableAction } from '@/components/swm/Table';
  import { useModal } from '@/components/swm/Modal';
  import InspectionPlanModal from './InspectionPlanModal.vue';
  import { columns, searchFormSchema } from './inspectionPlanData';
  import {
    getInspectionPlanListByPage,
    deleteInspectionPlan,
    openInspectionPlan,
    pauseInspectionPlan,
  } from '@/api/swm/inspectionPlan';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { router } from '@/router';

  export default defineComponent({
    name: 'ViewsSwmInspectionPlanIndex',
    components: { BasicTable, InspectionPlanModal, TableAction },
    setup() {
      const { createMessage } = useMessage();
      const [registerModal, { openModal }] = useModal();
      //标题和上面导航栏保持一致
      const pageTitle = computed(() => {
        // 假设你通过路由元信息定义了页面标题
        return router.currentRoute.value.meta.title || '巡检计划';
      });
      const [registerTable, { reload }] = useTable({
        title: pageTitle.value,
        api: getInspectionPlanListByPage,
        columns,
        formConfig: {
          baseColProps: { lg: 6, md: 8 },
          labelWidth: 150,
          schemas: searchFormSchema,
        },
        useSearchForm: true,
        showTableSetting: true,
        fetchSetting: {
          pageField: 'pageNo',
          sizeField: 'pageSize',
          totalField: 'count',
          listField: 'list',
        },
        actionColumn: {
          width: 200, // 增加宽度以容纳更多按钮
          title: '操作',
          dataIndex: 'action',
          slots: { customRender: 'action' },
        },
        canResize: true,
      });

      function handleCreate() {
        openModal(true, {
          isUpdate: false,
        });
      }

      function handleView(record: Recordable) {
        openModal(true, {
          record,
          isUpdate: false,
          isView: true,
        });
      }

      function handleEdit(record: Recordable) {
        openModal(true, {
          record,
          isUpdate: true,
        });
      }

      async function handleDelete(record: Recordable) {
        await deleteInspectionPlan({ id: record.id });
        createMessage.success('删除成功');
        reload();
      }

      async function handleOpen(record: Recordable) {
        await openInspectionPlan({ id: record.id });
        createMessage.success('巡检计划已开启');
        reload();
      }

      async function handlePause(record: Recordable) {
        await pauseInspectionPlan({ id: record.id });
        createMessage.success('巡检计划已暂停');
        reload();
      }

      function handleSuccess() {
        reload();
      }

      // 动态生成操作按钮
      function getActions(record: Recordable) {
        // 定义操作按钮的类型
        type ActionType = {
          label: string;
          type: string;
          color?: string;
          onClick?: Function;
          popConfirm?: {
            title: string;
            confirm: Function;
          };
        };

        const actions: ActionType[] = [];

        // 如果巡检频次为0，只显示删除按钮
        if (record.frequencyDays === 0) {
          actions.push({
            icon: 'ant-design:delete-outlined',
            tooltip: '删除',
            color: 'error',
            type: 'link',
            popConfirm: {
              title: '确定删除吗?',
              confirm: handleDelete.bind(null, record),
            },
          });

          return actions;
        }

        // 添加开启/暂停按钮
        if (record.planStatus === 'pause') {
          actions.push({
            icon: 'ant-design:pause-circle-outlined',
            tooltip: '开启',
            type: 'link',
            color: 'success',
            onClick: handleOpen.bind(null, record),
          });
        } else {
          actions.push({
            icon: 'ant-design:play-circle-outlined',
            tooltip: '暂停',
            type: 'link',
            onClick: handlePause.bind(null, record),
          });
        }

        // 添加编辑和删除按钮
        actions.push({
          icon: 'ant-design:form-outlined',
          tooltip: '编辑',
          type: 'link',
          onClick: handleEdit.bind(null, record),
        });

        actions.push({
          icon: 'ant-design:delete-outlined',
          tooltip: '删除',
          color: 'error',
          type: 'link',
          popConfirm: {
            title: '确定删除吗?',
            confirm: handleDelete.bind(null, record),
          },
        });

        return actions;
      }

      return {
        registerTable,
        registerModal,
        handleCreate,
        handleEdit,
        handleDelete,
        handleSuccess,
        handleView,
        handleOpen,
        handlePause,
        getActions,
      };
    },
  });
</script>
<style lang="css" scoped>
  :deep(.ant-form-item-label) {
    min-width: 120px;
  }
</style>
