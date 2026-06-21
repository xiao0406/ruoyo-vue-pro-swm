<!--
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author 冼国文
-->
<template>
  <BasicDrawer
    v-bind="$attrs"
    :showFooter="true"
    :okAuth="'sys:empUser:edit'"
    @register="registerDrawer"
    @ok="handleSubmit"
    width="60%"
  >
    <template #title>
      <Icon :icon="getTitle.icon" class="pr-1 m-1" />
      <span> {{ getTitle.value }} </span>
    </template>
    <BasicForm @register="registerForm">
      <template #userRoleString>
        <BasicTable @register="registerUserRoleTable" />
      </template>
    </BasicForm>
  </BasicDrawer>
</template>
<script lang="ts" setup>
  import { ref, computed } from 'vue';
  import { useI18n } from '@/hooks/swm/useI18n';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { router } from '@/router';
  import { Icon } from '@/components/swm/Icon';
  import { BasicForm, FormSchema, useForm } from '@/components/swm/Form';
  import { BasicDrawer, useDrawerInner } from '@/components/swm/Drawer';
  import {
    MonitorDeviceInfo,
    monitorDeviceInfoForm,
    roleTreeData,
    roleAccrEdit,
  } from '@/api/sys/monitorDeviceInfo';
  import { BasicTable, useTable } from '@/components/swm/Table';

  const emit = defineEmits(['success', 'register']);

  const { t } = useI18n('sys.empUser');
  const { showMessage } = useMessage();
  const record = ref<MonitorDeviceInfo>({} as MonitorDeviceInfo);
  const getTitle = computed(() => ({
    icon: router.currentRoute.value.meta.icon || 'ant-design:book-outlined',
    value: record.value.id?.includes(',') ? t('批量分配') : t('分配角色'),
  }));

  const inputFormSchemas: FormSchema[] = [
    {
      label: t('机构'),
      field: 'officeName',
      component: 'Input',
      componentProps: {
        disabled: true,
        ifShow: (record: MonitorDeviceInfo) => !record.id.includes(','),
      },
    },
    {
      label: t('监控设备名称'),
      field: 'name',
      component: 'Input',
      componentProps: {
        disabled: true,
        ifShow: (record: MonitorDeviceInfo) => {
          console.log(record.id);
          return !record.id.includes(',');
        },
      },
    },
    {
      label: t('角色信息'),
      field: 'roleInfo',
      component: 'FormGroup',
      colProps: { lg: 24, md: 24 },
    },
    {
      label: t('分配角色'),
      field: 'userRoleString',
      component: 'Input',
      colProps: { lg: 24, md: 24 },
      slot: 'userRoleString',
    },
  ];

  const [registerForm, { resetFields, setFieldsValue }] = useForm({
    schemas: inputFormSchemas,
    baseColProps: { lg: 12, md: 24 },
    labelWidth: 120,
  });

  const [registerUserRoleTable, { getSelectRowKeys, setSelectedRowKeys }] = useTable({
    api: roleTreeData,
    columns: [
      {
        title: t('角色名称'),
        dataIndex: 'name',
        width: 260,
        align: 'center',
      },
      {
        title: t('角色编码'),
        dataIndex: 'id',
        width: 260,
        align: 'center',
      },
    ],
    rowSelection: { type: 'checkbox' },
    pagination: false,
    bordered: true,
    size: 'small',
    inset: true,
  });

  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    resetFields();
    setDrawerProps({ loading: true });
    if (data.id.includes(',')) {
      record.value.id = data.id;
      setSelectedRowKeys([]);
      setDrawerProps({ loading: false });
      return;
    }
    const res = await monitorDeviceInfoForm(data);
    record.value = (res.monitorDeviceInfo || {}) as MonitorDeviceInfo;
    record.value.__t = new Date().getTime();
    setFieldsValue(record.value);
    setSelectedRowKeys(record.value.roleMonitorList_?.split(',') || []);
    setDrawerProps({ loading: false });
  });

  async function handleSubmit() {
    try {
      setDrawerProps({ confirmLoading: true });
      const data = {
        id_in: record.value.id,
        roleMonitorList: getSelectRowKeys(),
      };
      const res = await roleAccrEdit(data);
      showMessage(res.message);
      emit('success', data);
      setTimeout(closeDrawer);
    } catch (error: any) {
      if (error && error.errorFields) {
        showMessage(t('您填写的信息有误，请根据提示修正。'));
      }
    } finally {
      setDrawerProps({ confirmLoading: false });
    }
  }
</script>
