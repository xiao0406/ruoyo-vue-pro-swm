<!--
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author 冼国文
-->
<template>
  <div>
    <BasicTable @register="registerTable" @selection-change="selectChange">
      <template #tableTitle>
        <Icon :icon="getTitle.icon" class="m-1 pr-1" />
        <span> {{ getTitle.value }} </span>
      </template>
      <template #toolbar>
        <a-button type="primary" @click="handleOrgForm({})" v-auth="'sys:monitorDeviceInfo:edit'">
          <Icon icon="fluent:add-12-filled" /> {{ t('新增分组') }}
        </a-button>
        <a-button type="primary" @click="handleForm({})" v-auth="'sys:monitorDeviceInfo:edit'">
          <Icon icon="fluent:add-12-filled" /> {{ t('新增监控设备') }}
        </a-button>
        <a-button
          type="primary"
          @click="handleAllot"
          v-auth="'sys:monitorDeviceInfo:edit'"
          :disabled="selectKeys.length === 0"
        >
          <Icon icon="allot|svg" /> {{ t('批量分配') }}
        </a-button>
        <a-button type="primary" @click="openUploadModal" v-auth="'sys:monitorDeviceInfo:edit'">
          <Icon icon="ant-design:upload-outlined" /> {{ t('导入') }}
        </a-button>
      </template>
      <template #subtype="{ record }">
        {{ ['主码', '辅码'][Number(record.subtype)] }}
      </template>
    </BasicTable>
    <InputForm @register="registerModal" @success="reload" />
    <FormOrg @register="registerOrgModal" @success="reload" />
    <FormAssignRoles @register="registerDrawer" @success="clearSelectedRowKeys" />
    <UploadModal
      title="导入监控设备"
      acceptString=".xls,.xlsx"
      :is-fms="false"
      url="/sys/monitorDeviceInfo/importData"
      @register="registerUploadModal"
      @success="handleSuccess"
    />
  </div>
</template>
<script lang="ts">
  export default defineComponent({
    name: 'ViewsSysMonitorDeviceInfoList',
  });
</script>
<script lang="ts" setup>
  import { defineComponent, ref } from 'vue';
  import { useI18n } from '@/hooks/swm/useI18n';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { router } from '@/router';
  import { Icon } from '@/components/swm/Icon';
  import { FormProps } from '@/components/swm/Form';
  import { BasicTable, BasicColumn, useTable } from '@/components/swm/Table';
  import { useModal } from '@/components/swm/Modal';
  import { useDrawer } from '@/components/swm/Drawer';
  import { monitorDeviceInfoListData, monitorDeviceInfoDelete } from '@/api/sys/monitorDeviceInfo';
  import InputForm from './form.vue';
  import FormAssignRoles from './formAssignRoles.vue';
  import FormOrg from './formOrg.vue';
  import UploadModal from '@/components/swm/UploadModal/index.vue';

  const { t } = useI18n();
  const { showMessage } = useMessage();
  const getTitle = {
    icon: router.currentRoute.value.meta.icon || 'ant-design:book-outlined',
    value: router.currentRoute.value.meta.title || t('监控管理'),
  };

  const searchForm: FormProps = {
    baseColProps: { lg: 6, md: 8 },
    labelWidth: 110,
    schemas: [
      {
        label: t('监控设备名称'),
        field: 'name',
        component: 'Input',
        componentProps: {
          maxlength: 200,
        },
      },
      {
        label: t('监控设备类型'),
        field: 'deviceType',
        component: 'Select',
        componentProps: {
          dictType: 'monitor_device_type',
        },
      },
    ],
  };

  const tableColumns: BasicColumn[] = [
    {
      title: t('机构'),
      dataIndex: 'parentName',
      key: 'a.parent_name',
      sorter: false,
      width: 180,
      align: 'left',
    },
    {
      title: t('监控类型'),
      dataIndex: 'deviceTypeName',
      key: 'a.device_type_name',
      sorter: false,
      width: 80,
      align: 'left',
    },
    {
      title: t('监控设备编号'),
      dataIndex: 'code',
      key: 'a.code',
      sorter: false,
      width: 80,
      align: 'left',
    },
    {
      title: t('监控设备名称'),
      dataIndex: 'name',
      key: 'a.name',
      sorter: false,
      width: 150,
      align: 'left',
    },
    {
      title: t('监控点唯一标识'),
      dataIndex: 'cameraIndexCode',
      sorter: false,
      width: 150,
      align: 'left',
    },
    {
      title: 'IP',
      dataIndex: 'ip',
      key: 'a.ip',
      sorter: false,
      width: 100,
      align: 'left',
    },
    {
      title: t('端口'),
      dataIndex: 'rtspPort',
      key: 'a.rtsp_port',
      sorter: false,
      width: 60,
      align: 'left',
    },
    {
      title: t('设备管理用户'),
      dataIndex: 'adminUser',
      key: 'a.admin_user',
      sorter: false,
      width: 100,
      align: 'center',
    },
    {
      title: t('设备管理用户密码'),
      dataIndex: 'adminPassword',
      key: 'a.admin_password',
      sorter: false,
      width: 120,
      align: 'center',
    },
    {
      title: t('设备资源路径'),
      dataIndex: 'rtspUri',
      key: 'a.rtsp_uri',
      sorter: false,
      width: 120,
      align: 'center',
    },
    {
      title: t('直播通道'),
      dataIndex: 'channel',
      key: 'a.channel',
      sorter: false,
      width: 80,
      align: 'left',
    },
    {
      title: t('码流'),
      dataIndex: 'subtype',
      key: 'a.subtype',
      sorter: false,
      width: 80,
      align: 'left',
      slot: 'subtype',
    },
    {
      title: t('加载路径'),
      dataIndex: 'loadSource',
      key: 'a.load_source',
      sorter: false,
      width: 120,
      align: 'left',
    },
    {
      title: t('直播流获取路径'),
      dataIndex: 'streamUrl',
      key: 'a.stream_url',
      sorter: false,
      width: 120,
      align: 'left',
    },
  ];

  const actionColumn: BasicColumn = {
    width: 120,
    actions: (record: Recordable) => [
      {
        icon: 'clarity:note-edit-line',
        title: t('编辑'),
        onClick: handleForm.bind(this, record),
        auth: 'sys:monitorDeviceInfo:edit',
      },
      {
        icon: 'ant-design:delete-outlined',
        color: 'error',
        title: t('删除'),
        popConfirm: {
          title: t(`是否确认删除 ${record.name} 监控？`),
          confirm: handleDelete.bind(this, { id: record.id }),
        },
        auth: 'sys:monitorDeviceInfo:edit',
      },
      {
        icon: 'allot|svg',
        title: t('分配角色'),
        onClick: handleFormRole.bind(this, { id: record.id }),
        auth: 'sys:monitorDeviceInfo:edit',
      },
    ],
  };

  const deviceTypeObj = {
    ['MDP_01']: '海康',
    ['MDP_02']: '大华',
  };
  const [registerTable, { reload, clearSelectedRowKeys }] = useTable({
    api: monitorDeviceInfoListData,
    afterFetch: (data) => {
      data.map((item: any) => {
        item.deviceTypeName = deviceTypeObj[item.deviceType];
      });
      return data;
    },
    columns: tableColumns,
    actionColumn: actionColumn,
    formConfig: searchForm,
    showTableSetting: true,
    useSearchForm: true,
    canResize: true,
    clickToRowSelect: false,
    rowSelection: {
      type: 'checkbox',
    },
    pagination: {
      defaultPageSize: 100,
    },
  });

  const [registerModal, { openModal }] = useModal();
  function handleForm(record: Recordable) {
    if (record.recType === '1') {
      handleOrgForm(record);
    } else {
      openModal(true, record);
    }
  }

  const [registerOrgModal, { openModal: openOrgModal }] = useModal();
  function handleOrgForm(record: Recordable) {
    openOrgModal(true, record);
  }

  async function handleDelete(record: Recordable) {
    const res = await monitorDeviceInfoDelete(record);
    showMessage(res.message);
    reload();
  }

  const [registerDrawer, { openDrawer }] = useDrawer();
  function handleFormRole(record: Recordable) {
    openDrawer(true, record);
  }

  // 批量分配
  const selectKeys = ref<string[]>([]);
  function selectChange({ keys }: { keys: string[] }) {
    selectKeys.value = keys;
  }
  function handleAllot() {
    openDrawer(true, {
      id: selectKeys.value.join(','),
    });
  }

  // 导入
  const [registerUploadModal, { openModal: openUploadModal }] = useModal();
  function handleSuccess(isRefresh: boolean) {
    isRefresh && reload();
  }
</script>
