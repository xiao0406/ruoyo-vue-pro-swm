<!--
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author 冼国文
-->
<template>
  <BasicModal
    v-bind="$attrs"
    :showFooter="true"
    :okAuth="'app:appUpgrade:edit'"
    @register="registerModal"
    @ok="handleSubmit"
    width="40%"
    :minHeight="340"
  >
    <template #title>
      <Icon :icon="getTitle.icon" class="pr-1 m-1" />
      <span> {{ getTitle.value }} </span>
    </template>
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed } from 'vue';
  import { useI18n } from '@/hooks/swm/useI18n';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { router } from '@/router';
  import { Icon } from '@/components/swm/Icon';
  import { BasicForm, FormSchema, useForm } from '@/components/swm/Form';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import {
    MonitorDeviceInfo,
    companyTreeData,
    monitorDeviceInfoForm,
    monitorDeviceInfoSave,
  } from '@/api/sys/monitorDeviceInfo';
  import { officeTreeData } from '@/api/sys/office';

  const emit = defineEmits(['success', 'register']);

  const { t } = useI18n();
  const { showMessage } = useMessage();
  const record = ref<MonitorDeviceInfo>({} as MonitorDeviceInfo);
  const getTitle = computed(() => ({
    icon: router.currentRoute.value.meta.icon || 'ant-design:book-outlined',
    value: !record.value.id ? t('新增设备监控信息') : t('编辑设备监控信息'),
  }));

  const inputFormSchemas: FormSchema[] = [
    {
      label: t('分组名称'),
      field: 'name',
      component: 'Input',
    },
    {
      label: t('机构'),
      field: 'parentId',
      component: 'TreeSelect',
      componentProps: {
        api: officeTreeData,
        params: { isLoadAll: true },
        canSelectParent: true,
      },
    },
    {
      label: t('排序'),
      field: 'treeSort',
      component: 'InputNumber',
    },
  ];

  const [registerForm, { resetFields, updateSchema, setFieldsValue, validate }] = useForm({
    labelWidth: 130,
    schemas: inputFormSchemas,
    baseColProps: { lg: 24, md: 24 },
  });

  let treeSort = 0;
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    resetFields();
    setModalProps({ loading: true });
    const res = await monitorDeviceInfoForm(data);
    record.value = (res.monitorDeviceInfo || {}) as MonitorDeviceInfo;
    record.value.__t = new Date().getTime();
    record.value.treeSort = record.value.treeSort || treeSort;
    console.log(record.value);
    setFieldsValue(record.value);
    setModalProps({ loading: false });
  });

  async function handleSubmit() {
    try {
      const data = await validate();
      setModalProps({ confirmLoading: true });
      const params: any = {
        isNewRecord: !record.value.id,
        id: record.value.id,
        recType: 1,
      };
      const res = await monitorDeviceInfoSave({ ...data, ...params });
      showMessage(res.message);
      setTimeout(closeModal);
      emit('success', data);
    } catch (error: any) {
      if (error && error.errorFields) {
        showMessage(t('您填写的信息有误，请根据提示修正。'));
      }
      console.log('error', error);
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
