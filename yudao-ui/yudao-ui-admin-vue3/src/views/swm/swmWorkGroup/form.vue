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
  import { defineComponent, ref, computed, h, nextTick } from 'vue';
  import { useI18n } from '@/hooks/swm/useI18n';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { router } from '@/router';
  import { Icon } from '@/components/swm/Icon';
  import { BasicForm, FormSchema, useForm } from '@/components/swm/Form';
  import { BasicDrawer, useDrawerInner } from '@/components/swm/Drawer';
  import { addWorkGroup, getCode } from '@/api/swm/prodLine';
  import { fetchOrgTreeData } from '@/api/swm/organizationTree';
  import { Popup } from '@/components/swm/Popupinput';

  const emit = defineEmits(['success', 'register']);

  const { t } = useI18n('fms.fmsProdLine');
  const { showMessage } = useMessage();
  const record = ref<any>({});
  const getTitle = computed(() => ({
    icon: router.currentRoute.value.meta.icon || 'ant-design:book-outlined',
    value: record.value.id ? t('编辑班组档案') : t('新增班组档案'),
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
      field: 'makeUnit',
      label: '所属单位',
      component: 'Select', // 修改为Select
      componentProps: {
        api: getCompanyOptions,
        placeholder: '请选择单位',
        allowClear: true,
        showSearch: true,
        labelField: 'label',
        valueField: 'value',
        immediate: true, // 立即请求数据
        resultField: '', // 直接返回数组
      },
      colProps: { span: 8 },
    },
    {
      label: t('所属车间'),
      field: 'workShopId',
      component: 'Select',
      helpMessage: t('请先确保已选择制造单位'),
      render: ({ model, field }) => {
        return h(Popup, {
          valueText: model[field],
          code: 'SWM_CJTC',
          slectMode: '',
          enabled: !!model['makeUnit'],
          dataTextField: '',
          dataValueField: '',
          InternalSearch: {
            goal: 'regionName',
            convert: 'region',
          },
          requestOrnot: model['makeUnit'],
          params: {
            region: model['makeUnit'] || '“”',
          },
          onChange: (e) => {
            model[field] = e;
          },
        });
      },
    },
    {
      label: t('所属产线'),
      field: 'prodLineId',
      component: 'Select',
      helpMessage: t('请先确保已选择制造单位'),
      render: ({ model, field }) => {
        return h(Popup, {
          valueText: model[field],
          code: 'SWM_SSCX',
          slectMode: '',
          enabled: !!model['workShopId'],
          dataTextField: '',
          dataValueField: '',
          requestOrnot: model['workShopId'],
          params: {
            workShopId: model['workShopId'] || '“”',
          },
          onChange: (e) => {
            model[field] = e;
          },
        });
      },
    },
    {
      label: t('班组名称'),
      field: 'workGroupName',
      component: 'Input',
      componentProps: {
        maxlength: 64,
        onBlur: handleNameBlur,
      },
    },
    {
      label: t('班组编码'),
      field: 'workGroupCode',
      component: 'Input',
      componentProps: {
        maxlength: 32,
        placeholder: '请输入班组编码',
      },
    },
  ];

  const [registerForm, { resetFields, setFieldsValue, validate, getFieldsValue }] = useForm({
    labelWidth: 120,
    schemas: inputFormSchemas,
    baseColProps: { lg: 12, md: 24 },
    showActionButtonGroup: false, // 添加这行优化渲染
  });

  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    resetFields();
    setDrawerProps({ loading: true });

    record.value = data || {};

    if (record.value.id) {
      // 编辑模式：设置表单值
      setFieldsValue(record.value);

      // 如果所属单位字段有值，预加载数据
      if (record.value.makeUnit) {
        try {
          await getCompanyOptions();
        } catch (error) {
          console.error('预加载单位数据失败', error);
        }
      }
    }

    setDrawerProps({ loading: false });
  });

  // 班组名称失去焦点时调用接口获取编码
  async function handleNameBlur() {
    const formData = getFieldsValue();
    const workGroupName = formData.workGroupName;

    if (workGroupName && !record.value.id) {
      try {
        const res = await getCode({ name: workGroupName });
        if (res) {
          const currentValues = getFieldsValue();
          setFieldsValue({ ...currentValues, workGroupCode: res });
        }
      } catch (error) {
        console.error('获取班组编码失败', error);
      }
    }
  }

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

      const res = await addWorkGroup(params);
      showMessage(record.value.id ? '编辑成功' : '新增成功');
      setTimeout(closeDrawer);
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
