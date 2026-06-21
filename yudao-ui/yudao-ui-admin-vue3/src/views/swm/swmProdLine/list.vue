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
          {{ record.prodLineName }}
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
  import { getPageList } from '@/api/swm/prodLine';
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
        label: t('产线名称'),
        field: 'prodLineName',
        component: 'Input',
      },
      {
        label: t('产线编码'),
        field: 'prodLineCode',
        component: 'Input',
      },
      {
        label: t('制造单位'),
        field: 'makeUnitName',
        component: 'Input',
      },
      {
        label: t('所属车间'),
        field: 'workShopId',
        component: 'Select',
        render: ({ model, field }) => {
          return h(Popup, {
            valueText: model[field],
            code: 'SWM_CJTC',
            slectMode: '',
            enabled: true,
            dataTextField: '',
            dataValueField: '',
            params: {},
            onChange: (e) => {
              model[field] = e;
            },
          });
        },
      },
    ],
  };

  const tableColumns: BasicColumn[] = [
    {
      title: t('产线名称'),
      dataIndex: 'prodLineName',
      key: 'a.prod_line_name',
      width: 230,
      align: 'left',
      slot: 'firstColumn',
    },
    {
      title: t('制造单位'),
      dataIndex: 'makeUnitName',
      key: 'a.make_unit',
      width: 230,
      align: 'left',
    },
    {
      title: t('产线编码'),
      dataIndex: 'prodLineCode',
      key: 'a.prod_line_code',
      width: 130,
      align: 'left',
    },
    {
      title: t('所属车间'),
      dataIndex: 'workName',
      key: 'a.work_name',
      width: 130,
      align: 'left',
    },
    {
      title: t('更新人'),
      dataIndex: 'updateName',
      key: 'a.update_name',
      width: 130,
      align: 'center',
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
    api: getPageList,
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
