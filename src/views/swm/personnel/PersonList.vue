<template>
  <div class="personnel-list-container">
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button v-if="checkedKeys.length > 0" type="primary" @click="handleBatchSafetyEducation">
          <Icon icon="ant-design:safety-outlined" /> 批量完成安全教育
        </a-button>
        <a-button type="primary" @click="handleCreate">
          <Icon icon="ant-design:plus-outlined" /> 新增
        </a-button>
        <a-button type="primary" @click="handleImport">
          <Icon icon="ant-design:import-outlined" /> 批量导入
        </a-button>
        <a-button type="danger" :disabled="checkedKeys.length === 0" @click="handleBatchDelete">
          <Icon icon="ant-design:delete-outlined" /> 批量删除
        </a-button>
        <a-button type="default" :loading="exportLoading" @click="handleExport">
          <Icon icon="ant-design:download-outlined" />
          导出
        </a-button>
        <a-button type="primary" :loading="exportLoading" @click="handleTeamImport">
          切换班组
        </a-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'safetyEducation'">
          <DictLabel dictType="safety_education_enum" :dictValue="record.safetyEducation" />
        </template>
        <!-- <template v-else-if="column.key === 'personType'">
          <DictLabel dictType="person_type_enum" :dictValue="record.personType" />
        </template> -->
        <template v-else-if="column.key === 'personnelStatus'">
          <DictLabel dictType="person_status_enum" :dictValue="record.personnelStatus" />
        </template>
        <!-- <template v-else-if="column.key === 'isExternalPersonnel'">
          <DictLabel dictType="external_personnel_enum" :dictValue="record.isExternalPersonnel" />
        </template> -->
        <template v-else-if="column.key === 'helmetReturned'">
          <template v-if="record.helmetReturned">
            <DictLabel dictType="helmet_returned_enum" :dictValue="record.helmetReturned" />
          </template>
          <template v-else>
            <!-- 未知状态显示为空 -->
          </template>
        </template>
        <template v-else-if="column.key === 'departureType'">
          <template v-if="record.departureType">
            <DictLabel dictType="departure_type_enum" :dictValue="record.departureType" />
          </template>
          <template v-else>
            <!-- 未知状态显示为空 -->
          </template>
        </template>
        <template v-else-if="column.key === 'action'">
          <TableAction
            :actions="[
              {
                icon: 'clarity:note-edit-line',
                tooltip: '编辑',
                ifShow: isActive(record), // 只有在职人员才能编辑
                onClick: handleEdit.bind(null, record),
              },
              {
                icon: 'ant-design:safety-outlined',
                tooltip: '绑定安全帽',
                ifShow:
                  record.safetyEducation === SafetyEducationEnum.COMPLETED &&
                  record.personnelStatus === PersonStatusEnum.ACTIVE,
                onClick: handleBindSafety.bind(null, record),
              },
              {
                icon: 'ant-design:user-switch-outlined',
                tooltip: '处理离职',
                ifShow: isActive(record),
                onClick: handleDeparture.bind(null, record),
              },
              /* 删除按钮已隐藏
            {
              icon: 'ant-design:delete-outlined',
              color: 'error',
              tooltip: '删除',
              popConfirm: {
                title: '是否确认删除',
                confirm: handleDelete.bind(null, record),
              },
            },
            */
            ]"
          />
        </template>
      </template>
    </BasicTable>
    <PersonModal
      @register="registerModal"
      @success="handleSuccess"
      @view-departed-records="handleViewDepartedRecords"
    />
    <ImportModal @register="registerImportModal" @success="handleSuccess" />
    <UploadModal
      title="安全帽管理"
      acceptString=".xls,.xlsx"
      prefix="/swm"
      url="/swmPerson/importData"
      @register="importModal"
      @success="handleSuccess"
      templateShowApi="/swmPerson/teamTempExport"
    />
    <DepartureModal @register="registerDepartureModal" @success="handleSuccess" />
    <DepartureRecordList @register="registerDepartureRecordList" />
    <SafetyHelmetModal @register="registerSafetyHelmetModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" setup name="ViewsSwmPersonnelPersonList">
  import { ref, onMounted, nextTick, computed } from 'vue';
  import { BasicTable, useTable, TableAction } from '@/components/swm/Table';
  import {
    getPersonList,
    deletePerson,
    batchDeletePerson,
    batchCompleteSafetyEducation,
    exportPersonnelList,
  } from '@/api/swm/person';
  import { fetchOrgTreeData } from '@/api/swm/organizationTree';
  import PersonModal from './PersonModal.vue';
  import ImportModal from './ImportModal.vue';
  import UploadModal from '@/components/swm/UploadModal/index.vue';
  import DepartureModal from './DepartureModal.vue';
  import DepartureRecordList from './DepartureRecordList.vue';
  import SafetyHelmetModal from './SafetyHelmetModal.vue';
  import { useModal } from '@/components/swm/Modal';
  import { Icon } from '@/components/swm/Icon';
  import { Tag, message } from 'ant-design-vue';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { DictLabel, useDict } from '@/components/swm/Dict';
  import { SafetyEducationEnum, PersonStatusEnum } from '@/enums/swm/personEnum';
  import { dictDataListData } from '@/api/swm/dictData'; // 获取人员类型
  import { router } from '@/router';
  import { reactive } from 'vue';
  import { dictDataTreeData } from '@/api/sys/dictData';
  // 手动获取人员类型（不再取大系统管理
  const personTypeData = reactive({
    data: [] as any,
    externalPersonnelEnumData: [] as any,
  });
  dictDataListData({ dictType: 'person_type_enum' }).then((result) => {
    personTypeData.data = result;
  });

  dictDataTreeData({ dictType: 'external_personnel_enum' }).then((result) => {
    personTypeData.externalPersonnelEnumData = result;
  });
  const checkedKeys = ref<Array<string>>([]);
  const { createMessage, createConfirm } = useMessage();
  const [registerModal, { openModal }] = useModal();
  const [registerImportModal, { openModal: openImportModal }] = useModal();
  const [importModal, { openModal: openTeamImportModal }] = useModal();
  const [registerDepartureModal, { openModal: openDepartureModal }] = useModal();
  const [registerDepartureRecordList, { openModal: openDepartureRecordList }] = useModal();
  const [registerSafetyHelmetModal, { openModal: openSafetyHelmetModal }] = useModal();

  const { initDict } = useDict();

  // 四级联动选项数据
  const companyOptions = ref<{ label: string; value: string }[]>([]);
  const departmentOptions = ref<{ label: string; value: string }[]>([]);
  const prodLineOptions = ref<{ label: string; value: string }[]>([]);
  const teamOptions = ref<{ label: string; value: string }[]>([]);

  // 存储完整的组织数据，用于查找ID
  const orgDataMap = ref<Map<string, any>>(new Map());

  const exportLoading = ref(false);

  // 初始化字典数据和下拉选项
  initDict([
    'person_status_enum',
    'safety_education_enum',
    'helmet_returned_enum',
    // 'person_type_enum',
    'departure_type_enum',
    // 'external_personnel_enum',
  ]);

  // 加载下拉选项数据
  // 只加载单位数据，其他选项通过联动加载
  fetchOrgTreeData('root')
    .then((result) => {
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
    })
    .catch((err) => {
      console.error('加载选项数据失败:', err);
    });

  // 处理所属单位变化
  async function handleCompanyChange(value: string) {
    // console.log('所属单位变化:', value);

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
        department: undefined,
        prodLine: undefined,
        team: undefined,
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
    // console.log('所属车间变化:', value);

    // 清空下级选项数据
    prodLineOptions.value = [];
    teamOptions.value = [];

    // 使用 setTimeout 延迟清空表单字段，避免干扰当前字段值设置
    setTimeout(() => {
      const formInstance = getForm();
      const currentValues = formInstance.getFieldsValue();
      formInstance.setFieldsValue({
        ...currentValues,
        prodLine: undefined,
        team: undefined,
      });
    }, 150);

    if (!value) {
      return;
    }

    try {
      // 查找车间ID
      const departmentData = orgDataMap.value.get(`${value}-workshop`);
      if (!departmentData) {
        // console.warn('未找到对应的车间数据');
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
    // console.log('所属产线变化:', value);

    // 清空下级选项数据
    teamOptions.value = [];

    // 使用 setTimeout 延迟清空表单字段，避免干扰当前字段值设置
    setTimeout(() => {
      const formInstance = getForm();
      const currentValues = formInstance.getFieldsValue();
      formInstance.setFieldsValue({
        ...currentValues,
        team: undefined,
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

  //标题和上面导航栏保持一致
  const pageTitle = computed(() => {
    // 假设你通过路由元信息定义了页面标题
    return router.currentRoute.value.meta.title || '人员登记';
  });
  const [registerTable, { reload, getForm }] = useTable({
    title: pageTitle.value,
    scroll: {
      x: 1920,
    },
    api: (params) => {
      // console.log('API请求参数:', params);
      return getPersonList(params).then((res) => {
        // console.log('API响应数据:', res);
        // console.log('数据条数:', res.list?.length);
        return res;
      });
    },
    rowKey: 'id',
    columns: [
      {
        title: '人员编码',
        dataIndex: 'personNumber',
        width: 140,
      },
      {
        title: '姓名',
        dataIndex: 'name',
        width: 140,
      },
      {
        title: '性别',
        dataIndex: 'gender',
        width: 80,
      },
      {
        title: '手机号码',
        dataIndex: 'phoneNumber',
        width: 120,
      },
      {
        title: '是否厂内员工',
        dataIndex: 'isExternalPersonnel',
        key: 'isExternalPersonnel',
        width: 120,
        customRender: ({ record }) => {
          let value = null;
          personTypeData.externalPersonnelEnumData?.forEach((item) => {
            if (item.value == record.isExternalPersonnel) {
              value = item.name;
            }
          });
          return value || '未知';
        },
      },

      {
        title: '人员类型',
        dataIndex: 'personType',
        key: 'personType',
        width: 120,
        customRender: ({ record }) => {
          // 获取数组数据
          const options = personTypeData.data?.length > 0 ? personTypeData.data : [];
          // 查找匹配的字典项
          const match = options.find((item: any) => item.dictValue == record.personType);
          return match ? match.dictLabel : record.personType;
        },
      },

      {
        title: '入场安全教育',
        dataIndex: 'safetyEducation',
        key: 'safetyEducation',
        width: 150,
      },
      {
        title: '所属单位',
        dataIndex: 'company',
        width: 180,
      },
      {
        title: '所属车间',
        dataIndex: 'department',
        width: 150,
      },
      {
        title: '所属产线',
        dataIndex: 'prodLine',
        width: 120,
      },
      {
        title: '所属班组',
        dataIndex: 'team',
        width: 120,
      },
      {
        title: '所属工种',
        dataIndex: 'jobType',
        width: 120,
      },
      {
        title: '身份证号码',
        dataIndex: 'identityCard',
        width: 180,
      },
      {
        title: '安全帽编码',
        dataIndex: 'safetyHelmetId',
        width: 150,
      },
      {
        title: '安全帽状态',
        dataIndex: 'powerOnStatus',
        key: 'powerOnStatus',
        width: 100,
        customRender: ({ record }) => {
          switch (record.powerOnStatus) {
            case '0':
              return '开机';
            case '1':
              return '关机';
            default:
              return '';
          }
        },
      },
      {
        title: '人员状态',
        dataIndex: 'personnelStatus',
        key: 'personnelStatus',
        width: 120,
      },
      {
        title: '离职类型',
        dataIndex: 'departureType',
        key: 'departureType',
        width: 120,
      },
      {
        title: '离职原因',
        dataIndex: 'departureReason',
        width: 200,
      },
      {
        title: '是否归还安全帽',
        dataIndex: 'helmetReturned',
        key: 'helmetReturned',
        width: 120,
      },
      {
        title: '操作',
        dataIndex: 'action',
        key: 'action',
        width: 200,
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
          field: 'personNumber',
          label: '人员编码',
          component: 'Input',
          colProps: { span: 8 },
        },
        {
          field: 'name',
          label: '姓名',
          component: 'Input',
          colProps: { span: 8 },
        },
        {
          field: 'identityCard',
          label: '身份证号码',
          component: 'Input',
          colProps: { span: 8 },
        },
        {
          field: 'phoneNumber',
          label: '手机号码',
          component: 'Input',
          colProps: { span: 8 },
        },
        {
          field: 'company',
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
          colProps: { span: 8 },
        },
        {
          field: 'department',
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
          colProps: { span: 8 },
        },
        {
          field: 'prodLine',
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
          colProps: { span: 8 },
        },
        {
          field: 'team',
          label: '所属班组',
          component: 'Select',
          componentProps: () => ({
            placeholder: '请选择班组',
            allowClear: true,
            showSearch: true,
            fieldNames: { label: 'label', value: 'value' },
            options: teamOptions.value,
          }),
          colProps: { span: 8 },
        },
        {
          field: 'personType',
          label: '人员类型',
          component: 'Select',
          componentProps: {
            options: personTypeData.data?.length > 0 ? personTypeData.data : [],
            fieldNames: {
              label: 'dictLabel',
              value: 'dictValue',
            },
            allowClear: true,
            showSearch: true,
            filterOption: (input: string, option: any) => {
              const label = option?.dictLabel || '';
              return label.toLowerCase().indexOf(input.toLowerCase()) >= 0;
            },
          },
          colProps: { span: 8 },
        },

        {
          field: 'personnelStatus',
          label: '人员状态',
          component: 'Select',
          componentProps: {
            placeholder: '请选择人员状态',
            allowClear: true,
            dictType: 'person_status_enum',
          },
          colProps: { span: 8 },
        },
        {
          field: 'safetyEducation',
          label: '安全教育',
          component: 'Select',
          componentProps: {
            placeholder: '请选择是否培训安全教育',
            allowClear: true,
            dictType: 'safety_education_enum',
            options: [
              { label: '未开始', value: '0' },
              { label: '已培训', value: '1' },
            ],
          },
          colProps: { span: 8 },
        },
        {
          field: 'departureType',
          label: '离职类型',
          component: 'Select',
          componentProps: {
            placeholder: '请选择离职类型',
            allowClear: true,
            dictType: 'departure_type_enum',
          },
          colProps: { span: 8 },
        },
        {
          label: '安全帽状态',
          field: 'powerOnStatus',
          component: 'Select',
          componentProps: {
            options: [
              { label: '开机', value: '0' },
              { label: '关机', value: '1' },
            ],
            allowClear: true,
          },
        },
        {
          field: 'safetyHelmetId',
          label: '安全帽编码',
          component: 'Input',
          colProps: { span: 8 },
        },
      ],
    },
    rowSelection: {
      onChange: (selectedRowKeys) => {
        checkedKeys.value = selectedRowKeys as string[];
      },
    },
    canResize: true,
  });

  // 判断是否在职
  function isActive(record) {
    return record.personnelStatus === PersonStatusEnum.ACTIVE;
  }

  function handleCreate() {
    openModal(true, {
      isUpdate: false,
    });
  }

  function handleTeamImport() {
    openTeamImportModal(true);
  }
  function handleImport() {
    openImportModal(true);
  }

  function handleEdit(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  function handleBindSafety(record: Recordable) {
    // 避免使用记录中可能为undefined的safetyHelmetId
    // 不传递原始record，而是创建一个包含必要属性的新对象
    const safetyData = {
      id: record?.id || '',
      name: record?.name || '',
      personType: record?.personType || '',
      safetyHelmetId: record?.safetyHelmetId || '',
    };

    // 确保ID有值
    if (!safetyData.id) {
      createMessage.error('无效的人员记录');
      return;
    }

    // 使用必要的属性打开模态窗
    nextTick(() => {
      openSafetyHelmetModal(true, {
        record: safetyData,
      });
    });
  }

  function handleDeparture(record: Recordable) {
    openDepartureModal(true, {
      record,
    });
  }

  function handleViewDepartedRecords(records: Recordable[]) {
    openDepartureRecordList(true, records);
  }

  async function handleDelete(record: Recordable) {
    await deletePerson(record.id);
    createMessage.success('删除成功');
    reload();
  }

  function handleBatchDelete() {
    if (checkedKeys.value.length === 0) {
      createMessage.warning('请至少选择一条记录');
      return;
    }

    createConfirm({
      iconType: 'warning',
      title: '确认删除',
      content: '是否确认删除所选记录？',
      onOk: async () => {
        await batchDeletePerson(checkedKeys.value.join(','));
        checkedKeys.value = [];
        createMessage.success('批量删除成功');
        reload();
      },
    });
  }

  function handleSafetyEducation() {
    // 跳转到安全教育页面
    window.open('#/swm/safetyEducation/list', '_blank');
  }

  function handleBatchSafetyEducation() {
    if (checkedKeys.value.length === 0) {
      createMessage.warning('请至少选择一条记录');
      return;
    }

    createConfirm({
      iconType: 'info',
      title: '批量完成安全教育',
      content: `是否确认将所选的 ${checkedKeys.value.length} 名人员的安全教育状态设置为已完成？`,
      onOk: async () => {
        try {
          const res = await batchCompleteSafetyEducation(checkedKeys.value.join(','));
          if (res.result === 'success') {
            createMessage.success(res.message || '批量完成安全教育成功');
            checkedKeys.value = [];
            reload();
          } else {
            createMessage.error(res.message || '批量完成安全教育失败');
          }
        } catch (error) {
          console.error('批量完成安全教育出错', error);
          createMessage.error('批量完成安全教育失败，请稍后重试');
        }
      },
    });
  }

  function handleSuccess() {
    reload();
  }

  // 导出
  const handleExport = async () => {
    try {
      const formData = await getForm().validate();

      exportLoading.value = true;

      const res = await exportPersonnelList(formData);

      if (res && (res.result === true || res.result === 'true') && res.data) {
        // 直接打开下载链接
        window.open(res.data, '_blank');
      } else {
        console.error('导出失败，响应数据:', res);
        message.error(res.message || '导出失败');
      }
    } finally {
      exportLoading.value = false;
    }
  };
</script>

<style lang="less" scoped>
  .personnel-list-container {
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
