<template>
  <div>
    <div class="staff-list-container">
      <BasicTable @register="registerTable">
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'index'">
            {{ calculateRowIndex(index) }}
          </template>
          <template v-else-if="column.key == 'areaType'">
            <span>{{ getAreatypeName(column) }}</span>
          </template>
        </template>
        <template #toolbar>
          <a-button type="primary">
            <!-- <Icon icon="ant-design:plus-outlined" /> -->
            新增区域
          </a-button>
        </template>
        <template #action="{ record }">
          <TableAction
            :actions="[
              {
                label: '准入名单',
                type: 'link',
                onClick: userTable.bind(null, record),
              },
              {
                label: '查看区域',
                type: 'link',
                onClick: toReginType.bind(null, record),
              },
            ]"
          />
        </template>
      </BasicTable>
    </div>

    <!-- <StaffListModal @register="register" @success="batchSuccess" :formState="formState.obj"
      :areaTypeList="areaTypeList" /> -->
  </div>
</template>

<script setup lang="ts">
  import { BasicTable, useTable, TableAction } from '@/components/swm/Table';
  //import StaffListModal from './StaffListModal.vue'
  import { ref, reactive, watch, computed } from 'vue';
  import { Table, Input, Button, message } from 'ant-design-vue';
  import { useModal } from '@/components/swm/Modal';
  import { Icon } from '@/components/swm/Icon';
  import { useDict } from '@/components/swm/Dict';
  import {
    getAllAreaList,
    saveAreaWithBeacons,
    getAreaBeaconCoordinates,
    deleteAreaWithBeacons,
    checkBeaconConflicts,
  } from '@/api/swm/area';

  import { getAreaAccessList } from '@/api/swm/beacon';
  const [register, { openModal: batchManagementModal }] = useModal();
  let areaTypeList = ref([]);
  const getAreatypeName = (value) => {
    return value;
  };
  let formState = reactive({
    obj: {},
  });
  const userTable = (item) => {
    console.log(item);

    formState.obj = JSON.parse(JSON.stringify(item));

    batchManagementModal(true, {});
  };
  const toReginType = (item) => {
    console.log(item);
  };
  const [registerTable, { reload, getPaginationRef }] = useTable({
    title: '区域报警记录',
    // api: getAllAreaList,
    rowKey: 'id',
    columns: [
      {
        title: '序号',
        dataIndex: 'index',
        key: 'index',
        width: 60,
      },
      {
        title: '报警单号',
        dataIndex: 'areaName',
        width: 120,
      },
      {
        title: '区域名称',
        dataIndex: 'areaType',
        width: 120,
        slots: { customRender: 'areaType' },
      },
      {
        title: '区域类型',
        dataIndex: 'areaColor',
        width: 120,
        slots: { customRender: 'areaColor' },
      },
      {
        title: '人员姓名',
        dataIndex: 'voiceEnabled',
        width: 130,
      },
      {
        title: '报警类型',
        dataIndex: 'voiceEnabled',
        width: 130,
      },
      {
        title: '报警时间',
        dataIndex: 'voiceEnabled',
        width: 130,
      },
      {
        title: '触发坐标',
        dataIndex: 'voiceEnabled',
        width: 130,
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
          label: '报警单号',
          component: 'Input',
          colProps: { span: 12 },
          defaultValue: null,
        },
        {
          field: 'areaType',
          label: '区域类型',
          component: 'Select',
          colProps: { span: 12 },
          defaultValue: null,
          componentProps: {
            options: areaTypeList,
          },
        },
      ],
    },
  });

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

  // 计算行序号，考虑分页
  function calculateRowIndex(index: number) {
    const paginationInfo = getPaginationRef();
    if (paginationInfo && typeof paginationInfo !== 'boolean') {
      const current = paginationInfo.current || 1;
      const pageSize = paginationInfo.pageSize || 10;
      return (current - 1) * pageSize + index + 1;
    }
    return index + 1;
  }
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

  // 使用字典管理信标类型
  const { initDict, getDictList } = useDict();
  let voiceTemplateList = reactive({
    data: [],
  });
  // 初始化字典数据
  async function initDictData() {
    await initDict(['area_type']);
    // 获取区域类型字典数据
    const areaTypeDict = getDictList('area_type');
    if (areaTypeDict && areaTypeDict.length > 0) {
      areaTypeList.value = areaTypeDict.map((item) => ({
        label: item.name,
        value: item.value,
      }));
    }
  }
  initDictData();
</script>

<style scoped>
  .staff-list-container {
    padding: 10px;
  }

  .search-area {
    margin-bottom: 16px;
  }
</style>
