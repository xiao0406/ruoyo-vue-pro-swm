<template>
  <div>
    <BasicTable @register="registerTable">
      <template #tableTitle>
        <Icon :icon="getTitle.icon" class="m-1 pr-1" />
        <span> {{ getTitle.value }} </span>
      </template>
      <template #toolbar>
        <a-button type="primary" @click="handleForm({})">
          <Icon icon="fluent:add-12-filled" /> {{ t('新增') }}
        </a-button>
      </template>
      <template #firstColumn="{ record }">
        <a @click="handleForm(record)">
          {{ record.positionName }}
        </a>
      </template>
      <template #headColumn="{ record }">
        <span>
          {{ record.headName }}
          {{ record.headId ? `(${record.headId})` : '' }}
        </span>
      </template>
    </BasicTable>
    <InputForm @register="registerDrawer" @success="handleSuccess" />
  </div>
</template>

<script lang="ts">
  import { defineComponent } from 'vue';
  export default defineComponent({
    name: 'ViewsFmsFmsProdLineList',
  });
</script>

<script lang="ts" setup>
  import { defineComponent, h } from 'vue';
  import { useI18n } from '@/hooks/swm/useI18n';
  import { router } from '@/router';
  import { Icon } from '@/components/swm/Icon';
  import { BasicTable, BasicColumn, useTable } from '@/components/swm/Table';
  import { getWorkShopPageList } from '@/api/swm/prodLine';
  import { FormProps } from '@/components/swm/Form';
  import { Popup } from '@/components/swm/Popupinput';
  import { useDrawer } from '@/components/swm/Drawer';
  import InputForm from './form.vue';

  const { t } = useI18n('fms.fmsProdLine');

  const getTitle = {
    icon: router.currentRoute.value.meta.icon || 'ant-design:book-outlined',
    value: router.currentRoute.value.meta.title || t('生产线档案管理'),
  };

  const searchForm: FormProps = {
    baseColProps: { lg: 6, md: 8 },
    labelWidth: 90,
    schemas: [
      {
        label: t('制造单位'),
        field: 'makeUnitName',
        component: 'Input',
      },
    ],
  };

  const tableColumns: BasicColumn[] = [
    {
      title: t('制造单位'),
      dataIndex: 'regionName',
      key: 'a.make_unit',
      width: 230,
      align: 'left',
    },
    {
      title: t('车间名称'),
      dataIndex: 'positionName',
      key: 'a.prod_line_name',
      width: 230,
      align: 'left',
      slot: 'firstColumn',
    },
    {
      title: t('更新时间'),
      dataIndex: 'updateDate',
      key: 'a.update_date',
      width: 130,
      align: 'center',
    },
  ];

  const [registerDrawer, { openDrawer }] = useDrawer();
  const [registerTable, { reload }] = useTable({
    api: getWorkShopPageList,
    beforeFetch: (params) => {
      return params;
    },
    columns: tableColumns,
    formConfig: searchForm,
    showTableSetting: true,
    useSearchForm: true,
    canResize: true,
  });

  function handleForm(record: Recordable) {
    openDrawer(true, record);
  }

  function handleSuccess() {
    reload();
  }
</script>
