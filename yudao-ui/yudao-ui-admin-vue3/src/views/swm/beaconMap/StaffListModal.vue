<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="'编辑准入名单'" :width="1000">
    <div class="staff-list-container">
      <BasicTable @register="registerTable">
        <template #toolbar>
          <a-button type="primary" @click="batchManagementModal(true)">
            <Icon icon="ant-design:plus-outlined" /> 新增
          </a-button>
          <a-button type="primary" @click="handleBatchReminder">
            <Icon icon="ant-design:bell-outlined" /> 删除
          </a-button>
        </template>
        <template #action="{ record }">
          <TableAction
            :actions="[
              {
                icon: 'clarity:note-edit-line',
                onClick: handleEdit.bind(null, record),
                tooltip: '编辑',
              },
            ]"
          />
        </template>
        <template #batteryLevel="{ record }">
          <span v-if="record.batteryLevel !== undefined && record.batteryLevel !== null">
            {{ record.batteryLevel }}%
          </span>
          <span v-else>-</span>
        </template>
        <template #helmetType="{ record }">
          <a-tag>
            <DictLabel dictType="helmet_type_enum" :dictValue="record.helmetType" />
          </a-tag>
        </template>
        <template #motionStatus="{ record }">
          <a-tag>
            <DictLabel dictType="motion_status_enum" :dictValue="record.motionStatus" />
          </a-tag>
        </template>
      </BasicTable>
    </div>

    <StaffModal @register="managementModal" :formState="props.formState" @success="reload" />
  </BasicModal>
</template>

<script setup lang="ts">
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicTable, useTable, TableAction } from '@/components/swm/Table';
  import { ref, reactive, watch, computed } from 'vue';
  import { Table, Input, Button, message } from 'ant-design-vue';
  import { useModal } from '@/components/swm/Modal';
  import StaffModal from './staffModal.vue';
  import { Icon } from '@/components/swm/Icon';
  import { getAreaAccessList } from '@/api/swm/beacon';
  const [managementModal, { openModal: batchManagementModal }] = useModal();
  let props = defineProps({
    formState: Object,
    areaTypeList: Array,
  });
  const [registerTable, { reload, getSelectRowKeys, getDataSource, setFormSchemas }] = useTable({
    title: '准入名单',
    api: getAreaAccessList,
    rowKey: 'id',
    columns: [
      {
        title: '序号',
        dataIndex: 'deviceId',
        width: 120,
      },
      {
        title: '姓名',
        dataIndex: 'helmetType',
        width: 120,
        slots: { customRender: 'helmetType' },
      },
      {
        title: '人员类型',
        dataIndex: 'batteryLevel',
        width: 120,
        slots: { customRender: 'batteryLevel' },
      },
      {
        title: '区域公司',
        dataIndex: 'ip',
        width: 130,
      },
      {
        title: '关联安全帽',
        dataIndex: 'macAddress',
        width: 150,
      },
      {
        title: '坐标',
        dataIndex: 'assignedPerson',
        width: 120,
      },
    ],
    bordered: true,
    showIndexColumn: false,
    actionColumn: {
      width: 180,
      title: '操作',
      dataIndex: 'action',
      slots: { customRender: 'action' },
    },
    rowSelection: {
      onChange: (selectedRowKeys) => {
        checkedKeys.value = selectedRowKeys;
      },
    },
    showTableSetting: true,
    useSearchForm: true,
    canResize: true,
    formConfig: {
      baseColProps: { lg: 6, md: 24 },
      labelWidth: 100,
      schemas: [
        {
          field: 'areaName',
          label: '区域名称',
          component: 'Input',
          colProps: { span: 12 },
          defaultValue: props?.formState?.areaName || '',
          componentProps: {
            disabled: true, // 根据条件禁用
          },
        },
        {
          field: 'areaType',
          label: '区域类型',
          component: 'Select',
          colProps: { span: 12 },
          defaultValue: props?.formState?.areaType || '',
          componentProps: {
            disabled: true, // 根据条件禁用
            options: props.areaTypeList,
          },
        },
        {
          field: 'personName',
          label: '人员名称',
          component: 'Input',
          colProps: { span: 12 },
        },
        {
          field: 'areaId',
          label: '区域id',
          component: 'Input',
          colProps: { span: 12 },
          defaultValue: props?.formState?.id || '',
          componentProps: {
            showIndexColumn: false,
            disabled: true, // 根据条件禁用
          },
        },
      ],
    },
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    // console.log('aaa', data);
    setFormSchemas([
      {
        field: 'areaName',
        componentProps: {
          disabled: true,
          defaultValue: props?.formState?.areaName || '',
        },
      },
      {
        field: 'areaType',
        componentProps: {
          disabled: true,
          defaultValue: props?.formState?.areaType || '',
          options: props?.areaTypeList,
        },
      },
      {
        field: 'areaId',
        componentProps: {
          disabled: true,
          defaultValue: props?.formState?.id || '',
        },
      },
    ]);

    reload();
  });

  const emit = defineEmits(['update:visible', 'submit']);

  const visible = ref(false);
  const confirmLoading = ref(false);
  const searchValue = ref('');
  const staffList = ref([]);
  const selectedRowKeys = ref<string[]>([]);
  const staffModalVisible = ref(false);
  const currentStaff = ref(null);

  const pagination = reactive({
    current: 1,
    pageSize: 10,
    total: 0,
  });

  const columns = [
    {
      title: '姓名',
      dataIndex: 'name',
      key: 'name',
      width: 120,
    },
    {
      title: '工号',
      dataIndex: 'employeeNumber',
      key: 'employeeNumber',
      width: 120,
    },
    {
      title: '部门',
      dataIndex: 'department',
      key: 'department',
      width: 150,
    },
    {
      title: '职位',
      dataIndex: 'position',
      key: 'position',
      width: 150,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      customRender: ({ text }) => (text === 1 ? '在职' : '离职'),
    },
    {
      title: '操作',
      key: 'action',
      width: 100,
    },
  ];

  const modalTitle = computed(() => {
    return '区域准入人员管理';
  });

  const handleSearch = () => {
    pagination.current = 1;
    fetchStaffList();
  };

  const resetSearch = () => {
    searchValue.value = '';
    pagination.current = 1;
    fetchStaffList();
  };

  const handleEdit = (record: Staff) => {
    currentStaff.value = { ...record };
    staffModalVisible.value = true;
  };
</script>

<style scoped>
  .staff-list-container {
    padding: 10px;
  }

  .search-area {
    margin-bottom: 16px;
  }
</style>
