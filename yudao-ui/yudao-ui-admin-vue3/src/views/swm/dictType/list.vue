<!--
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author ThinkGem
-->
<template>
  <div>
    <BasicTable @register="registerTable">
      <template #tableTitle>
        <Icon :icon="getTitle.icon" class="m-1 pr-1" />
        <span> {{ getTitle.value }} </span>
      </template>
      <template #toolbar>
        <!-- <a-button type="primary" @click="handleRefresh" v-auth="'sys:dictType:edit'">
          <Icon icon="ant-design:redo-outlined" /> {{ t('刷新字典缓存') }}
        </a-button> -->
        <a-button type="primary" @click="handleForm({})">
          <Icon icon="fluent:add-12-filled" /> {{ t('新增') }}
        </a-button>
      </template>
      <template #firstColumn="{ record }">
        <a @click="handleForm({ id: record.id })" :title="record.dictName">
          {{ record.dictName }}
        </a>
      </template>
      <template #dictTypeColumn="{ record }">
        <a @click="handleDictData(record)" :title="record.dictType">
          {{ record.dictType }}
        </a>
      </template>
    </BasicTable>
    <InputForm @register="registerDrawer" @success="handleSuccess" />
  </div>
</template>
<script lang="ts">
  export default defineComponent({
    name: 'ViewsSysDictTypeList',
  });
</script>
<script lang="ts" setup>
  import { defineComponent } from 'vue';
  import { useI18n } from '@/hooks/swm/useI18n';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { router } from '@/router';
  import { Icon } from '@/components/swm/Icon';
  import { BasicTable, BasicColumn, useTable } from '@/components/swm/Table';
  import { dictTypeDelete, dictTypeListData } from '@/api/swm/dictType';
  import { useDrawer } from '@/components/swm/Drawer';
  import { FormProps } from '@/components/swm/Form';
  import InputForm from './form.vue';

  const { t } = useI18n('sys.dictType');
  const { showMessage } = useMessage();
  const getTitle = {
    icon: router.currentRoute.value.meta.icon || 'ant-design:book-outlined',
    value: router.currentRoute.value.meta.title || t('字典管理'),
  };

  const searchForm: FormProps = {
    baseColProps: { lg: 4, md: 8 },
    labelWidth: 90,
    schemas: [
      {
        label: t('字典名称'),
        field: 'dictName',
        component: 'Input',
      },
      {
        label: t('字典类型'),
        field: 'dictType_like',
        component: 'Input',
      },
      {
        label: t('系统字典'),
        field: 'isSys',
        component: 'Select',
        componentProps: {
          dictType: 'sys_yes_no',
        },
      },
      {
        label: t('状态'),
        field: 'status',
        component: 'Select',
        componentProps: {
          dictType: 'sys_search_status',
        },
      },
    ],
  };

  const tableColumns: BasicColumn[] = [
    {
      title: t('字典名称'),
      dataIndex: 'dictName',
      key: 'a.dict_name',
      sorter: false,
      width: 260,
      align: 'left',
      slot: 'firstColumn',
    },
    {
      title: t('字典类型'),
      dataIndex: 'dictType',
      key: 'a.dict_type',
      sorter: false,
      width: 260,
      align: 'left',
      slot: 'dictTypeColumn',
    },
    {
      title: t('系统字典'),
      dataIndex: 'isSys',
      key: 'a.is_sys',
      sorter: false,
      width: 80,
      dictType: 'sys_yes_no',
    },
    {
      title: t('更新时间'),
      dataIndex: 'updateDate',
      key: 'a.update_date',
      sorter: false,
      width: 130,
    },
    {
      title: t('状态'),
      dataIndex: 'status',
      key: 'a.status',
      sorter: false,
      width: 80,
      dictType: 'sys_status',
    },
    {
      title: t('备注信息'),
      dataIndex: 'remarks',
      key: 'a.remarks',
      sorter: false,
      width: 130,
    },
  ];

  const actionColumn: BasicColumn = {
    width: 130,
    actions: (record: Recordable) => [
      {
        icon: 'clarity:note-edit-line',
        title: t('编辑字典'),
        onClick: handleForm.bind(this, { id: record.id }),
        // auth: 'sys:dictType:edit',
      },
      {
        icon: 'ant-design:delete-outlined',
        color: 'error',
        title: t('删除字典'),
        popConfirm: {
          title: t('是否确认删除字典'),
          confirm: handleDelete.bind(this, { id: record.id }),
        },
        // auth: 'sys:dictType:edit',
      },
      {
        icon: 'ant-design:unordered-list-outlined',
        title: t('字典数据'),
        onClick: handleDictData.bind(this, record),
        // auth: 'sys:dictData:edit',
      },
    ],
  };

  const [registerDrawer, { openDrawer }] = useDrawer();
  const [registerTable, { reload }] = useTable({
    api: dictTypeListData,
    beforeFetch: (params) => {
      return params;
    },
    columns: tableColumns,
    actionColumn: actionColumn,
    formConfig: searchForm,
    showTableSetting: true,
    useSearchForm: true,
    canResize: true,
  });

  // async function handleRefresh() {
  //   const res = await refreshDictCache();
  //   showMessage(res.message);
  // }

  function handleForm(record: Recordable) {
    console.log('handleForm---', record);
    openDrawer(true, record);
  }

  async function handleDelete(record: Recordable) {
    const res = await dictTypeDelete(record);
    showMessage(res.message);
    handleSuccess(record);
  }

  function handleDictData(record: Recordable) {
    router.push({
      path: '/swm/dictData/list',
      query: {
        dictType: record.dictType,
      },
    });
  }

  function handleSuccess(_record: Recordable) {
    reload();
  }
</script>
