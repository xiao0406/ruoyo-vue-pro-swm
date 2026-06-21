<template>
  <div>
    <BasicTable @register="registerTable">
      <template #tableTitle>
        <Icon :icon="getTitle.icon" class="m-1 pr-1" />
        <span> {{ getTitle.value }} </span>
      </template>
    </BasicTable>
  </div>
</template>
<script lang="ts">
  export default defineComponent({
    name: 'ViewsSwmSwmSafetyPersonTrainingList',
  });
</script>
<script lang="ts" setup>
  import { defineComponent, onMounted, ref } from 'vue';
  import { useI18n } from '@/hooks/swm/useI18n';
  import { router } from '@/router';
  import { Icon } from '@/components/swm/Icon';
  import { BasicTable, BasicColumn, useTable } from '@/components/swm/Table';
  import { swmSafetyPersonTrainingListData } from '@/api/swm/swmSafetyPersonTraining';
  import { FormProps } from '@/components/swm/Form';
  import { fetchOrgTreeData } from '@/api/swm/organizationTree';
  const { t } = useI18n('swm.swmSafetyPersonTraining');

  const getTitle = {
    icon: router.currentRoute.value.meta.icon || 'ant-design:book-outlined',
    value: router.currentRoute.value.meta.title || t('视频培训记录'),
  };

  // 四级联动选项数据
  const companyOptions = ref<{ label: string; value: string }[]>([]);
  const departmentOptions = ref<{ label: string; value: string }[]>([]);
  const prodLineOptions = ref<{ label: string; value: string }[]>([]);
  const teamOptions = ref<{ label: string; value: string }[]>([]);

  // 存储完整的组织数据，用于查找ID
  const orgDataMap = ref<Map<string, any>>(new Map());

  const searchForm: FormProps = {
    baseColProps: { lg: 6, md: 8 },
    labelWidth: 90,
    schemas: [
      {
        label: t('视频标题'),
        field: 'title',
        component: 'Input',
      },
      {
        label: t('推送日期'),
        field: 'pushDate',
        component: 'DatePicker',
      },
      {
        label: t('人员姓名'),
        field: 'personName',
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
          onChange: (value: string) => {
            // 延迟执行，避免干扰表单值设置
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
          onChange: (value: string) => {
            // 延迟执行，避免干扰表单值设置
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
          onChange: (value: string) => {
            // 延迟执行，避免干扰表单值设置
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
        }),
      },
      {
        label: t('完成状态'),
        field: 'completeStatus',
        component: 'Select',
        componentProps: {
          placeholder: '请选择',
          allowClear: true,
          dictType: 'swm_complete_status',
        },
        defaultValue: '', // 默认选择全部
      },
    ],
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

  const tableColumns: BasicColumn[] = [
    {
      title: t('视频标题'),
      dataIndex: 'title',
      key: 'a.title',
      width: 230,
      align: 'center',
    },
    {
      title: t('推送日期'),
      dataIndex: 'pushDate',
      key: 'a.push_date',
      width: 130,
      align: 'center',
    },
    {
      title: t('视频分类'),
      dataIndex: 'type',
      key: 'a.type',
      width: 130,
      align: 'center',
      dictType: 'swm_safety_type',
    },
    {
      title: t('目标工种'),
      dataIndex: 'jobType',
      key: 'a.job_type',
      width: 230,
      align: 'center',
    },
    {
      title: '所属单位',
      dataIndex: 'company',
      width: 120,
    },
    {
      title: '所属车间',
      dataIndex: 'department',
      width: 120,
    },
    {
      title: '所属产线',
      dataIndex: 'prodLine',
      width: 120,
    },
    {
      title: '班组',
      dataIndex: 'team',
      width: 120,
    },
    {
      title: t('人员姓名'),
      dataIndex: 'personName',
      key: 'a.person_name',
      width: 130,
      align: 'center',
    },
    {
      title: t('完成状态'),
      dataIndex: 'completeStatus',
      key: 'a.complete_status',
      width: 130,
      align: 'center',
      dictType: 'swm_complete_status',
    },
    {
      title: t('完成时间'),
      dataIndex: 'completeDate',
      key: 'a.complete_date',
      width: 130,
      align: 'center',
    },
  ];

  const [registerTable, { getForm }] = useTable({
    api: swmSafetyPersonTrainingListData,
    columns: tableColumns,
    formConfig: searchForm,
    showTableSetting: true,
    useSearchForm: true,
    canResize: true,
  });

  // 初始化
  onMounted(async () => {
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
</script>
