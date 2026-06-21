<template>
  <div class="person-work-area-list">
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" :disabled="checkedRows.length === 0" @click="handleBatchBind">
          <Icon icon="ant-design:link-outlined" /> 绑定
        </a-button>
      </template>

      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <TableAction
            :actions="[
              {
                icon: 'ant-design:link-outlined',
                tooltip: '绑定',
                onClick: handleBind.bind(null, record),
              },
              {
                icon: 'ant-design:disconnect-outlined',
                color: 'error',
                tooltip: '解绑',
                popConfirm: {
                  title: '是否确认解绑该区域？',
                  confirm: handleUnbind.bind(null, record),
                },
              },
            ]"
          />
        </template>
      </template>
    </BasicTable>

    <PersonWorkAreaModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" setup name="ViewsSwmPersonWorkAreaList">
  import { onMounted, ref } from 'vue';
  import { BasicTable, TableAction, useTable } from '@/components/swm/Table';
  import { Icon } from '@/components/swm/Icon';
  import { useModal } from '@/components/swm/Modal';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { deletePersonWorkArea, getPersonWorkAreaList } from '@/api/swm/personWorkArea';
  import { fetchOrgTreeData } from '@/api/swm/organizationTree';
  import PersonWorkAreaModal from './PersonWorkAreaModal.vue';

  interface SelectOption {
    label: string;
    value: string;
  }

  const { createMessage } = useMessage();
  const [registerModal, { openModal }] = useModal();
  const companyOptions = ref<SelectOption[]>([]);
  const departmentOptions = ref<SelectOption[]>([]);
  const prodLineOptions = ref<SelectOption[]>([]);
  const teamOptions = ref<SelectOption[]>([]);
  const checkedRows = ref<Recordable[]>([]);
  const orgDataMap = ref<Map<string, any>>(new Map());

  const [registerTable, { reload, getForm }] = useTable({
    title: '人员工作区域绑定',
    api: getPersonWorkAreaList,
    rowKey: 'id',
    columns: [
      {
        title: '姓名',
        dataIndex: 'personName',
        width: 120,
      },
      {
        title: '所属单位',
        dataIndex: 'company',
        width: 150,
      },
      {
        title: '所属车间',
        dataIndex: 'department',
        width: 150,
      },
      {
        title: '所属产线',
        dataIndex: 'prodLine',
        width: 150,
      },
      {
        title: '所属班组',
        dataIndex: 'team',
        width: 140,
      },
      {
        title: '所属工种',
        dataIndex: 'jobType',
        width: 140,
      },
      {
        title: '身份证号码',
        dataIndex: 'identityCard',
        width: 190,
      },
      {
        title: '区域名称',
        dataIndex: 'areaName',
        width: 160,
      },
      {
        title: '备注',
        dataIndex: 'remarks',
        width: 180,
      },
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
    clickToRowSelect: false,
    formConfig: {
      baseColProps: { lg: 6, md: 8 },
      labelWidth: 90,
      schemas: [
        {
          field: 'personName',
          label: '姓名',
          component: 'Input',
        },
        {
          field: 'identityCard',
          label: '身份证号',
          component: 'Input',
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
            filterOption,
            onChange: (value: string) => {
              setTimeout(() => {
                handleCompanyChange(value);
              }, 100);
            },
          }),
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
            filterOption,
            onChange: (value: string) => {
              setTimeout(() => {
                handleDepartmentChange(value);
              }, 100);
            },
          }),
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
            filterOption,
            onChange: (value: string) => {
              setTimeout(() => {
                handleProdLineChange(value);
              }, 100);
            },
          }),
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
            filterOption,
          }),
        },
        {
          field: 'areaName',
          label: '区域名称',
          component: 'Input',
        },
      ],
    },
    rowSelection: {
      type: 'checkbox',
      onChange: (_selectedRowKeys, selectedRows) => {
        checkedRows.value = selectedRows as Recordable[];
      },
    },
  });

  onMounted(() => {
    loadCompanyOptions();
  });

  async function loadCompanyOptions() {
    try {
      const result = await fetchOrgTreeData('root');
      if (result && Array.isArray(result)) {
        companyOptions.value = result.map((item) => ({
          label: item.title,
          value: item.title,
        }));
        result.forEach((item) => {
          orgDataMap.value.set(`${item.title}-office`, item);
        });
      }
    } catch (error) {
      console.error('加载单位数据失败:', error);
    }
  }

  async function handleCompanyChange(value: string) {
    departmentOptions.value = [];
    prodLineOptions.value = [];
    teamOptions.value = [];
    resetSearchFields({ department: undefined, prodLine: undefined, team: undefined });

    if (!value) {
      return;
    }

    try {
      const companyData = Array.from(orgDataMap.value.values()).find(
        (item) => item.title === value && item.nodeType === 'office',
      );
      if (!companyData) {
        return;
      }

      const result = await fetchOrgTreeData('office', companyData.value || companyData.id);
      if (result && Array.isArray(result)) {
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

  async function handleDepartmentChange(value: string) {
    prodLineOptions.value = [];
    teamOptions.value = [];
    resetSearchFields({ prodLine: undefined, team: undefined });

    if (!value) {
      return;
    }

    try {
      const departmentData = orgDataMap.value.get(`${value}-workshop`);
      if (!departmentData) {
        return;
      }

      const result = await fetchOrgTreeData('workshop', departmentData.value || departmentData.id);
      if (result && Array.isArray(result)) {
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

  async function handleProdLineChange(value: string) {
    teamOptions.value = [];
    resetSearchFields({ team: undefined });

    if (!value) {
      return;
    }

    try {
      const prodLineData = orgDataMap.value.get(`${value}-prodLine`);
      if (!prodLineData) {
        return;
      }

      const result = await fetchOrgTreeData('prodLine', prodLineData.value || prodLineData.id);
      if (result && Array.isArray(result)) {
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

  function resetSearchFields(values: Recordable) {
    setTimeout(() => {
      const formInstance = getForm();
      const currentValues = formInstance.getFieldsValue();
      formInstance.setFieldsValue({
        ...currentValues,
        ...values,
      });
    }, 150);
  }

  function filterOption(input: string, option: any) {
    return String(option?.label || '')
      .toLowerCase()
      .includes(input.toLowerCase());
  }

  function getIdentityCards(records: Recordable[]) {
    return Array.from(new Set(records.map((item) => item.identityCard).filter(Boolean)));
  }

  function handleBatchBind() {
    const identityCards = getIdentityCards(checkedRows.value);
    if (!identityCards.length) {
      createMessage.warning('请先选择人员');
      return;
    }
    openModal(true, { identityCards });
  }

  function handleBind(record: Recordable) {
    openModal(true, { identityCards: getIdentityCards([record]) });
  }

  async function handleUnbind(record: Recordable) {
    const bindIds = splitIds(record.bindIds);
    if (!bindIds.length) {
      createMessage.warning('当前人员没有可解绑的区域');
      return;
    }
    await Promise.all(bindIds.map((id) => deletePersonWorkArea(id)));
    createMessage.success('解绑成功');
    checkedRows.value = [];
    reload();
  }

  function handleSuccess() {
    checkedRows.value = [];
    reload();
  }

  function splitIds(value?: string) {
    return String(value || '')
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean);
  }
</script>

<style lang="less" scoped>
  .person-work-area-list {
    height: 100%;
  }
</style>
