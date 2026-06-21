<template>
  <BasicDrawer
    v-bind="$attrs"
    :showFooter="true"
    @register="registerDrawer"
    @ok="handleSubmit"
    width="60%"
  >
    <template #title>
      <Icon :icon="getTitle.icon" class="pr-1 m-1" />
      <span> {{ getTitle.value }} </span>
    </template>
    <BasicForm @register="registerForm" />
  </BasicDrawer>
</template>

<script lang="ts">
  import { defineComponent } from 'vue';
  export default defineComponent({
    name: 'ViewsFmsFmsProdLineForm',
  });
</script>

<script lang="ts" setup>
  import { ref, computed, nextTick } from 'vue';
  import { useI18n } from '@/hooks/swm/useI18n';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { router } from '@/router';
  import { Icon } from '@/components/swm/Icon';
  import { BasicForm, FormSchema, useForm } from '@/components/swm/Form';
  import { BasicDrawer, useDrawerInner } from '@/components/swm/Drawer';
  import { addWorkShopProdLine } from '@/api/swm/prodLine';
  import { fetchOrgTreeData } from '@/api/swm/organizationTree';

  const emit = defineEmits(['success', 'register']);

  const { t } = useI18n('fms.fmsProdLine');
  const { showMessage } = useMessage();
  const record = ref<any>({});
  const getTitle = computed(() => ({
    icon: router.currentRoute.value.meta.icon || 'ant-design:book-outlined',
    value: record.value.id ? t('编辑车间档案') : t('新增车间档案'),
  }));

  // 获取单位列表数据
  const getCompanyOptions = async () => {
    try {
      const res = await fetchOrgTreeData('root');
      if (res && Array.isArray(res)) {
        // 将树形数据转换为Select组件需要的格式
        return res.map((item) => ({
          label: item.title,
          value: item.value,
        }));
      }
      return [];
    } catch (error) {
      console.error('获取单位列表失败', error);
      return [];
    }
  };

  const inputFormSchemas: FormSchema[] = [
    {
      field: 'region',
      label: '所属单位',
      component: 'Select', // 修改为Select
      componentProps: {
        api: getCompanyOptions,
        placeholder: '请选择单位',
        allowClear: true,
        showSearch: true,
        labelField: 'label',
        valueField: 'value',
        immediate: true, // 立即加载数据
      },
      colProps: { span: 8 },
    },
    {
      label: t('车间名称'),
      field: 'positionName',
      component: 'Input',
      componentProps: {
        maxlength: 32,
        placeholder: '请输入车间名称',
      },
      colProps: { span: 8 },
    },
  ];

  const [registerForm, { resetFields, setFieldsValue, validate, getFieldsValue }] = useForm({
    labelWidth: 120,
    schemas: inputFormSchemas,
    baseColProps: { lg: 12, md: 24 },
  });

  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    resetFields();
    console.log('接收到的数据:', data);
    setDrawerProps({ loading: true });

    record.value = data || {};

    if (record.value.id) {
      // 先清空表单
      resetFields();

      // 等待一下，让ApiSelect组件有机会加载数据
      await nextTick();

      // 设置表单值
      setFieldsValue({
        ...record.value,
        // 确保region字段使用的是正确的值
        region: record.value.region,
      });
    }

    setDrawerProps({ loading: false });
  });

  async function handleSubmit() {
    try {
      const data = await validate();
      setDrawerProps({ confirmLoading: true });

      const params: any = {
        ...data,
      };

      // 如果有id，说明是编辑
      if (record.value.id) {
        params.id = record.value.id;
      }

      const res = await addWorkShopProdLine(params);
      showMessage(record.value.id ? '编辑成功' : '新增成功');
      setTimeout(closeDrawer, 500);
      emit('success', data);
    } catch (error: any) {
      if (error && error.errorFields) {
        showMessage(t('您填写的信息有误，请根据提示修正。'));
      }
      console.log('error', error);
    } finally {
      setDrawerProps({ confirmLoading: false });
    }
  }
</script>
