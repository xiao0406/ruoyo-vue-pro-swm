<template>
  <div class="attendence-list-container">
    <BasicTable @register="registerTable">
      <template #tableTitle>
        <Icon :icon="getTitle.icon" class="m-1 pr-1" />
        <span> {{ getTitle.value }} </span>

        <Radio.Group
          v-model:value="classes"
          size="small"
          style="margin-left: 20px"
          @change="handleChangeClasses"
        >
          <Radio.Button
            v-for="item in getDictList('shift_type_enum')"
            :key="item.value"
            :value="item.value"
          >
            {{ item.name }}
          </Radio.Button>
        </Radio.Group>

        <a-button-group size="small" style="margin-left: 20px">
          <a-button type="primary" @click="handleSelectAll">全选人员</a-button>
          <a-button @click="clearSelectedRowKeys">全不选</a-button>
        </a-button-group>
      </template>

      <template #toolbar>
        <a-button type="primary" @click="handleCreate"> 排班时间管理</a-button>
        <!-- <a-button type="primary" @click="handleBatchManage">人员排班</a-button> -->
        <a-button type="primary" @click="handleArrange">人员排班</a-button>
        <a-button type="primary" preIcon="tdesign:folder-import" @click="openImportModal">
          班次调整
        </a-button>
      </template>
    </BasicTable>

    <SchedulModal @register="registerModal" @success="handleSuccess" />
    <StaffModal @register="managementModal" @success="handleSuccess" />
    <EditScheduleModal @register="editScheduleModal" @success="handleSuccess" />
    <Schedules @register="schedulesModal" />
    <UploadModal
      title="班次调整"
      acceptString=".xls,.xlsx"
      prefix="/swm"
      url="/personSchedule/importData"
      @register="importModal"
      @success="handleSuccess"
      templateShowApi="/personSchedule/export"
    />
  </div>
</template>

<script lang="ts" setup name="ViewsSwmAttendanceAttendanceList">
  import { ref, onMounted } from 'vue';
  import { BasicTable, useTable, BasicColumn } from '@/components/swm/Table';
  import { useModal } from '@/components/swm/Modal';
  import SchedulModal from './schedulModal.vue';
  import StaffModal from './staffModal.vue';
  import EditScheduleModal from './EditScheduleModal.vue';
  import {
    getStaffScheduleList,
    getPersonIdList,
    batchUpdateClasses,
  } from '@/api/swm/staffSchedule';
  import { fetchOrgTreeData } from '@/api/swm/organizationTree';
  import { useDict } from '@/components/swm/Dict';
  import { router } from '@/router';
  import { Icon } from '@/components/swm/Icon';
  import { FormProps } from '@/components/swm/Form';
  import { Radio, Modal } from 'ant-design-vue';
  import { useMessage } from '@/hooks/swm/useMessage';
  import Schedules from './schedules.vue';
  import UploadModal from '@/components/swm/UploadModal/index.vue';

  // 初始化字典数据
  const { initDict, getDictList } = useDict();

  const getTitle = {
    icon: router.currentRoute.value.meta.icon || 'ant-design:book-outlined',
    value: router.currentRoute.value.meta.title || '排班管理',
  };

  const { showMessage } = useMessage();

  const classes = ref<'1' | '3'>('1'); // 班次类型，1：早班，3：晚班

  // 四级联动选项数据
  const companyOptions = ref<{ label: string; value: string }[]>([]);
  const departmentOptions = ref<{ label: string; value: string }[]>([]);
  const prodLineOptions = ref<{ label: string; value: string }[]>([]);
  const teamOptions = ref<{ label: string; value: string }[]>([]);

  // 存储完整的组织数据，用于查找ID
  const orgDataMap = ref<Map<string, any>>(new Map());

  // 初始化
  onMounted(async () => {
    await initDict(['shift_type_enum']);
    classes.value = getDictList('shift_type_enum')[0].value;

    // 加载下拉选项数据
    try {
      // 只加载单位数据，其他选项通过联动加载
      const result = await fetchOrgTreeData('root');
      if (result && Array.isArray(result)) {
        companyOptions.value = result.map((item) => ({
          label: item.title,
          value: item.title,
        }));
        // 将所有加载的数据存储到orgDataMap中
        result.forEach((item) => {
          orgDataMap.value.set(`${item.title}-office`, item);
        });
      }
    } catch (error) {
      console.error('加载选项数据失败:', error);
    }
  });

  const [registerModal, { openModal: AddModal }] = useModal();
  const [managementModal, { openModal: batchManagementModal }] = useModal();
  const [editScheduleModal, { openModal: EditModal }] = useModal();
  const [schedulesModal, { openModal: SchedulesModal }] = useModal();
  const [importModal, { openModal: openImportModal }] = useModal();

  const searchForm: FormProps = {
    baseColProps: { lg: 6, md: 8 },
    labelWidth: 100,
    schemas: [
      // {
      //   field: 'month',
      //   label: '月份',
      //   component: 'DatePicker',
      //   componentProps: {
      //     picker: 'month',
      //     valueFormat: 'YYYY-MM',
      //     format: 'YYYY-MM',
      //   },
      // },
      {
        field: 'personName',
        label: '人员',
        component: 'Input',
      },
      {
        field: 'organization',
        label: '所属单位',
        component: 'Select',
        componentProps: () => ({
          placeholder: '请选择单位',
          allowClear: true,
          showSearch: true,
          fieldNames: { label: 'label', value: 'value' },
          options: companyOptions.value,
          onChange: (value: string) => {
            // 延迟执行，避免干扰表单值设置
            setTimeout(() => {
              handleCompanyChange(value);
            }, 100);
          },
        }),
      },
      {
        field: 'workshop',
        label: '所属车间',
        component: 'Select',
        componentProps: () => ({
          placeholder: '请选择车间',
          allowClear: true,
          showSearch: true,
          fieldNames: { label: 'label', value: 'value' },
          options: departmentOptions.value,
          onChange: (value: string) => {
            // 延迟执行，避免干扰表单值设置
            setTimeout(() => {
              handleDepartmentChange(value);
            }, 100);
          },
        }),
      },
      {
        field: 'process',
        label: '所属产线',
        component: 'Select',
        componentProps: () => ({
          placeholder: '请选择产线',
          allowClear: true,
          showSearch: true,
          fieldNames: { label: 'label', value: 'value' },
          options: prodLineOptions.value,
          onChange: (value: string) => {
            // 延迟执行，避免干扰表单值设置
            setTimeout(() => {
              handleProdLineChange(value);
            }, 100);
          },
        }),
      },
      {
        field: 'workGroupName',
        label: '所属班组',
        component: 'Select',
        componentProps: () => ({
          placeholder: '请选择班组',
          allowClear: true,
          showSearch: true,
          fieldNames: { label: 'label', value: 'value' },
          options: teamOptions.value,
        }),
      },
    ],
  };

  const tableColumns: BasicColumn[] = [
    // {
    //   title: '月份',
    //   dataIndex: 'month',
    //   width: 120,
    // },
    {
      title: '所属单位',
      dataIndex: 'organization',
      width: 120,
    },
    {
      title: '所属车间',
      dataIndex: 'workshop',
      width: 120,
    },
    {
      title: '所属产线',
      dataIndex: 'process',
      width: 120,
    },
    {
      title: '班组',
      dataIndex: 'workGroupName',
      width: 120,
    },
    {
      title: '人员',
      dataIndex: 'personName',
      width: 120,
    },
    {
      title: '班次',
      dataIndex: 'classes',
      width: 80,
      dictType: 'shift_type_enum',
    },
    {
      title: '创建时间',
      dataIndex: 'createDate',
      width: 180,
    },
  ];

  const actionColumn: BasicColumn = {
    width: 80,
    actions: (record: Recordable) => [
      {
        icon: 'clarity:note-edit-line',
        title: '编辑',
        onClick: handleEdit.bind(this, record),
      },
      {
        icon: 'material-symbols-light:event-note-outline',
        title: '排班记录',
        onClick: SchedulesModal.bind(this, true, record),
      },
    ],
  };

  const [
    registerTable,
    { reload, getForm, getSelectRowKeys, clearSelectedRowKeys, setSelectedRowKeys },
  ] = useTable({
    api: getStaffScheduleList,
    beforeFetch: (params) => {
      params.classes = classes.value;
    },
    columns: tableColumns,
    actionColumn: actionColumn,
    formConfig: searchForm,
    showTableSetting: true,
    useSearchForm: true,
    canResize: true,
    clickToRowSelect: false,
    rowSelection: {
      type: 'checkbox',
    },
  });

  // 处理班次变化
  const handleChangeClasses = async () => {
    await reload();
    clearSelectedRowKeys();
  };

  // 处理所属单位变化
  async function handleCompanyChange(value: string) {
    // 清空下级选项数据
    departmentOptions.value = [];
    prodLineOptions.value = [];
    teamOptions.value = [];

    // 使用 setTimeout 延迟清空表单字段，避免干扰当前字段值设置
    setTimeout(() => {
      const formInstance = getForm();
      const currentValues = formInstance.getFieldsValue();
      formInstance.setFieldsValue({
        ...currentValues,
        workshop: undefined,
        process: undefined,
        workGroupName: undefined,
      });
    }, 150);

    if (!value) {
      return;
    }

    try {
      // 查找单位ID
      const companyData = Array.from(orgDataMap.value.values()).find(
        (item) => item.title === value && item.nodeType === 'office',
      );
      if (!companyData) {
        console.warn('未找到对应的单位数据');
        return;
      }

      // 根据单位ID加载车间数据
      const result = await fetchOrgTreeData('office', companyData.value || companyData.id);
      if (result && Array.isArray(result)) {
        // 存储车间数据到映射中
        result.forEach((item) => {
          orgDataMap.value.set(`${item.title}-workshop`, item);
        });

        departmentOptions.value = result.map((item) => ({
          label: item.title,
          value: item.title,
        }));
      }
    } catch (error) {
      console.error('加载车间数据失败:', error);
    }
  }

  // 处理所属车间变化
  async function handleDepartmentChange(value: string) {
    // 清空下级选项数据
    prodLineOptions.value = [];
    teamOptions.value = [];

    // 使用 setTimeout 延迟清空表单字段，避免干扰当前字段值设置
    setTimeout(() => {
      const formInstance = getForm();
      const currentValues = formInstance.getFieldsValue();
      formInstance.setFieldsValue({
        ...currentValues,
        process: undefined,
        workGroupName: undefined,
      });
    }, 150);

    if (!value) {
      return;
    }

    try {
      // 查找车间ID
      const departmentData = orgDataMap.value.get(`${value}-workshop`);
      if (!departmentData) {
        console.warn('未找到对应的车间数据');
        return;
      }

      // 根据车间ID加载产线数据
      const result = await fetchOrgTreeData('workshop', departmentData.value || departmentData.id);
      if (result && Array.isArray(result)) {
        // 存储产线数据到映射中
        result.forEach((item) => {
          orgDataMap.value.set(`${item.title}-prodLine`, item);
        });

        prodLineOptions.value = result.map((item) => ({
          label: item.title,
          value: item.title,
        }));
      }
    } catch (error) {
      console.error('加载产线数据失败:', error);
    }
  }

  // 处理所属产线变化
  async function handleProdLineChange(value: string) {
    // 清空下级选项数据
    teamOptions.value = [];

    // 使用 setTimeout 延迟清空表单字段，避免干扰当前字段值设置
    setTimeout(() => {
      const formInstance = getForm();
      const currentValues = formInstance.getFieldsValue();
      formInstance.setFieldsValue({
        ...currentValues,
        workGroupName: undefined,
      });
    }, 150);

    if (!value) {
      return;
    }

    try {
      // 查找产线ID
      const prodLineData = orgDataMap.value.get(`${value}-prodLine`);
      if (!prodLineData) {
        console.warn('未找到对应的产线数据');
        return;
      }

      // 根据产线ID加载班组数据
      const result = await fetchOrgTreeData('prodLine', prodLineData.value || prodLineData.id);
      if (result && Array.isArray(result)) {
        // 存储班组数据到映射中
        result.forEach((item) => {
          orgDataMap.value.set(`${item.title}-workGroup`, item);
        });

        teamOptions.value = result.map((item) => ({
          label: item.title,
          value: item.title,
        }));
      }
    } catch (error) {
      console.error('加载班组数据失败:', error);
    }
  }

  // 排班时间管理
  let loadingScheduleTime = false;
  async function handleCreate() {
    if (loadingScheduleTime) return;

    loadingScheduleTime = true;
    AddModal(true, {
      reload: true,
    });
    loadingScheduleTime = false;
  }

  // 人员排班
  function handleBatchManage() {
    batchManagementModal(true);
  }

  // 全选
  async function handleSelectAll() {
    await reload();
    const formData = getForm().getFieldsValue();

    const res = await getPersonIdList({
      ...formData,
      classes: classes.value,
    });

    setSelectedRowKeys(res || []);
  }

  // 人员排班
  function handleArrange() {
    const selectRowKeys = getSelectRowKeys();

    if (selectRowKeys.length === 0) {
      showMessage('请选择要排班的人员');
      return;
    }

    const classesName = classes.value == '1' ? '晚班' : '早班';

    Modal.confirm({
      title: `是否确认将选择的人员调整为“${classesName}”？`,
      content: `已选择 ${selectRowKeys.length} 人，调整成功后次日生效。`,
      okText: '确认',
      okType: 'primary',
      onOk: async () => {
        try {
          await batchUpdateClasses({
            ids: selectRowKeys,
            classes: classes.value == '1' ? '3' : '1',
          });
          showMessage('操作成功');
          handleSuccess();
        } catch (error) {
          console.error('操作失败:', error);
          showMessage('操作失败，请稍后重试');
        }
      },
    });
  }

  function handleEdit(record: Recordable) {
    EditModal(true, {
      record,
      isUpdate: true,
    });
  }

  function handleSuccess() {
    reload();
  }
</script>

<style lang="less" scoped>
  .page-header {
    margin-bottom: 16px;
  }

  .page-title {
    font-size: 18px;
    font-weight: bold;
    color: #303133;
  }

  .attendence-list-container {
    height: 100%;
    display: flex;
    flex-direction: column;

    .search-container {
      display: flex;
      align-items: center;
      margin-bottom: 16px;
      padding: 16px;
      background-color: #fff;
      border-radius: 4px;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
    }
  }

  .search-item {
    display: flex;
    align-items: center;
    margin-right: 16px;
  }

  .search-label {
    margin-right: 8px;
    white-space: nowrap;
  }

  .action-column {
    display: flex;
    justify-content: center;

    .ant-btn-link {
      padding: 0 4px;

      &:hover {
        color: #1890ff;
        background-color: #f0f5ff;
      }
    }
  }

  // 添加编辑按钮悬停效果
  .edit-button {
    position: relative;

    &:hover::after {
      content: '编辑';
      position: absolute;
      top: -25px;
      left: 50%;
      transform: translateX(-50%);
      background-color: rgba(0, 0, 0, 0.75);
      color: white;
      padding: 2px 8px;
      border-radius: 4px;
      font-size: 12px;
      white-space: nowrap;
      z-index: 1000;
    }
  }
</style>
