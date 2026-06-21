<template>
  <div class="helmet-list-container">
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleCreate">
          <Icon icon="ant-design:plus-outlined" /> 新增
        </a-button>
        <a-button type="primary" @click="handleBatchReminder">
          <Icon icon="ant-design:bell-outlined" /> 提醒充电
        </a-button>
        <a-button type="primary" @click="handleDeviceConfig">
          <Icon icon="ant-design:setting-outlined" /> 参数设置
        </a-button>
        <!-- 添加导出按钮 -->
        <a-button type="primary" @click="handleExport" :loading="exportLoading">
          <Icon icon="ant-design:export-outlined" /> 导出
        </a-button>
        <a-button type="primary" preIcon="tdesign:folder-import" @click="openImportModal">
          导入
        </a-button>
      </template>
      <template #action="{ record }">
        <TableAction
          :actions="[
            {
              icon: 'clarity:note-edit-line',
              onClick: handleEdit.bind(null, record),
              tooltip: '编辑',
            },
            {
              icon: 'ant-design:setting-outlined',
              onClick: handleRowConfig.bind(null, record),
              tooltip: '参数配置',
            },
            {
              icon: 'ant-design:disconnect-outlined',
              tooltip: '解绑安全帽',
              ifShow: !!record.assignedPerson,
              popConfirm: {
                title: '确认解绑该安全帽？',
                confirm: handleUnbindHelmet.bind(null, record),
              },
            },
            {
              icon: 'ant-design:file-search-outlined',
              onClick: handleShowUsageRecord.bind(null, record),
              tooltip: '使用记录',
            },
            {
              icon: 'ant-design:delete-outlined',
              color: 'error',
              tooltip: !!record.assignedPerson ? '该安全帽已绑定人员，无法删除' : '删除',
              ifShow: !record.assignedPerson, // 只有未绑定人员时才显示删除按钮
              popConfirm: {
                title: '是否确认删除',
                confirm: handleDelete.bind(null, record),
              },
            },
          ]"
        />
      </template>
      <template #batteryLevel="{ record }">
        <span v-if="record.batteryLevel !== undefined && record.batteryLevel !== null">
          {{ record.batteryLevel }}%
        </span>
        <span v-else>-</span>
      </template>
      <template #helmetType="{ record }">
        <a-tag>
          <DictLabel dictType="helmet_type_enum" :dictValue="record.helmetType" />
        </a-tag>
      </template>
      <template #motionStatus="{ record }">
        <a-tag>
          <DictLabel dictType="motion_status_enum" :dictValue="record.motionStatus" />
        </a-tag>
      </template>
    </BasicTable>
    <HelmetDeviceModal @register="registerModal" @success="handleSuccess" />
    <BatchChargeModal @register="registerChargeModal" @success="handleReminderSuccess" />
    <HelmetUsageRecordModal @register="registerUsageRecordModal" />
    <HelmetDeviceConfigModal @register="registerConfigModal" @success="handleConfigSuccess" />
    <UploadModal
      title="安全帽管理"
      acceptString=".xls,.xlsx"
      prefix="/swm"
      url="/swmHelmetDevice/importData"
      @register="importModal"
      @success="handleSuccess"
      templateShowApi="/swmHelmetDevice/export"
    />
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref, onMounted, computed } from 'vue';
  import { BasicTable, useTable, TableAction } from '@/components/swm/Table';
  import { useModal } from '@/components/swm/Modal';
  import { getHelmetDeviceList, deleteHelmetDevice, unassignPerson } from '@/api/swm/helmetDevice';
  import { clearSafetyHelmet } from '@/api/swm/person';
  import HelmetDeviceModal from './HelmetDeviceModal.vue';
  import BatchChargeModal from './BatchChargeModal.vue';
  import HelmetUsageRecordModal from './HelmetUsageRecordModal.vue';
  import HelmetDeviceConfigModal from './HelmetDeviceConfigModal.vue';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { Icon } from '@/components/swm/Icon';
  import { DictLabel, useDict } from '@/components/swm/Dict';
  import { router } from '@/router';
  import { defHttp } from '@/utils/http/axios';
  import UploadModal from '@/components/swm/UploadModal/index.vue';

  interface HelmetDeviceRecord {
    id: string;
    deviceId: string;
    helmetType: string;
    helmetTypeText: string;
    batteryLevel: number;
    ip: string;
    macAddress: string;
    assignedPerson: string;
    personName: string;
    personPhone: string;
    assignedWorkshop: string;
    assignedProcess: string;
    assignedTeam: string;
    motionStatus: string;
    motionStatusText: string;
    bindTime?: string;
    unbindTime?: string;
    bindDurationDays?: number;
    usageStatus?: string;
    usageStatusText?: string;
  }

  export default defineComponent({
    name: 'ViewsSwmHelmetdeviceList',
    components: {
      BasicTable,
      TableAction,
      HelmetDeviceModal,
      BatchChargeModal,
      HelmetUsageRecordModal,
      HelmetDeviceConfigModal,
      Icon,
      DictLabel,
      UploadModal,
    },
    setup() {
      const { createMessage } = useMessage();
      const [registerModal, { openModal }] = useModal();
      const [registerChargeModal, { openModal: openChargeModal }] = useModal();
      const [registerUsageRecordModal, { openModal: openUsageRecordModal }] = useModal();
      const [registerConfigModal, { openModal: openConfigModal }] = useModal();
      const [importModal, { openModal: openImportModal }] = useModal();

      const checkedKeys = ref<string[]>([]);
      const exportLoading = ref(false);
      const { initDict } = useDict();

      // 初始化字典数据
      onMounted(() => {
        initDict(['motion_status_enum', 'helmet_type_enum', 'helmet_battery_enum']);
      });
      //标题和上面导航栏保持一致
      const pageTitle = computed(() => {
        // 假设你通过路由元信息定义了页面标题
        return router.currentRoute.value.meta.title || '定位设置管理';
      });
      const [registerTable, { reload, getSelectRowKeys, getSelectRows, getDataSource, getForm }] =
        useTable({
          title: pageTitle.value,
          api: getHelmetDeviceList,
          rowKey: 'id',
          pagination: {
            defaultPageSize: 20,
            pageSizeOptions: ['20', '30', '50', '100', '500'], // 可选的每页条数
          },
          columns: [
            {
              title: '设备编号',
              dataIndex: 'deviceId',
              width: 120,
            },
            {
              title: '设备类型',
              dataIndex: 'helmetType',
              width: 120,
              slots: { customRender: 'helmetType' },
            },
            {
              title: '安全帽状态',
              dataIndex: 'powerOnStatus',
              key: 'powerOnStatus',
              width: 100,
              customRender: ({ record }) => {
                switch (record.powerOnStatus) {
                  case '0':
                    return '开机';
                  case '1':
                    return '关机';
                  default:
                    return '';
                }
              },
            },
            {
              title: '电量',
              dataIndex: 'batteryLevel',
              width: 120,
              slots: { customRender: 'batteryLevel' },
            },
            {
              title: 'IP',
              dataIndex: 'ip',
              width: 130,
            },
            {
              title: 'MAC地址',
              dataIndex: 'macAddress',
              width: 150,
            },
            // {
            //   title: '绑定人员',
            //   dataIndex: 'assignedPerson',
            //   width: 120,
            // },
            {
              title: '人员姓名',
              dataIndex: 'personName',
              width: 100,
            },
            {
              title: '手机号码',
              dataIndex: 'personPhone',
              width: 120,
            },
            {
              title: '所属车间',
              dataIndex: 'assignedWorkshop',
              width: 180,
            },
            {
              title: '所属班组',
              dataIndex: 'assignedTeam',
              width: 120,
            },
            {
              title: '运动状态',
              dataIndex: 'motionStatus',
              width: 100,
              slots: { customRender: 'motionStatus' },
            },
          ],
          bordered: true,
          showIndexColumn: false,
          actionColumn: {
            width: 220,
            title: '操作',
            dataIndex: 'action',
            slots: { customRender: 'action' },
          },
          rowSelection: {
            onChange: (selectedRowKeys: string[]) => {
              checkedKeys.value = selectedRowKeys;
            },
          },
          showTableSetting: true,
          useSearchForm: true,
          canResize: true,
          formConfig: {
            baseColProps: { lg: 6, md: 8 },
            labelWidth: 100,
            schemas: [
              {
                field: 'deviceId',
                label: '设备编号',
                component: 'Input',
                colProps: { span: 8 },
              },
              {
                field: 'helmetType',
                label: '设备类型',
                component: 'Select',
                componentProps: {
                  dictType: 'helmet_type_enum',
                  allowClear: true,
                },
                colProps: { span: 8 },
              },
              {
                label: '安全帽状态',
                field: 'powerOnStatus',
                component: 'Select',
                componentProps: {
                  options: [
                    { label: '开机', value: '0' },
                    { label: '关机', value: '1' },
                  ],
                  allowClear: true,
                },
              },
              {
                field: 'batteryLevel',
                label: '电量',
                component: 'Select',
                componentProps: {
                  dictType: 'helmet_battery_enum',
                  allowClear: true,
                },
                colProps: { span: 8 },
              },
              {
                field: 'personName',
                label: '人员姓名',
                component: 'Input',
                colProps: { span: 8 },
              },
            ],
          },
        });

      function getBatteryStatus(
        batteryLevel: number | null | undefined,
      ): 'success' | 'normal' | 'exception' | 'active' {
        if (batteryLevel === null || batteryLevel === undefined) {
          return 'normal';
        }
        if (batteryLevel < 20) {
          return 'exception';
        } else if (batteryLevel < 50) {
          return 'normal';
        }
        return 'success';
      }

      function handleCreate() {
        openModal(true, {
          isUpdate: false,
        });
      }

      function handleEdit(record: HelmetDeviceRecord) {
        openModal(true, {
          record,
          isUpdate: true,
        });
      }

      async function handleDelete(record: HelmetDeviceRecord) {
        // 检查是否有绑定人员，如果有则不允许删除
        if (record.assignedPerson) {
          createMessage.warning('该安全帽已绑定人员，请先解绑后再删除');
          return;
        }

        try {
          await deleteHelmetDevice(record.id);
          createMessage.success('删除成功');
          reload();
        } catch (error) {
          console.error('删除安全帽失败', error);
          createMessage.error('删除安全帽失败，请重试');
        }
      }

      function handleSuccess() {
        reload();
      }

      function handleBatchReminder() {
        const selectedKeys = getSelectRowKeys() as string[];
        if (selectedKeys.length === 0) {
          createMessage.warning('请选择要提醒充电的设备');
          return;
        }

        // 获取选中的设备记录，提取deviceId
        const dataSource = getDataSource();
        const selectedDeviceIds = dataSource
          .filter((item: HelmetDeviceRecord) => selectedKeys.includes(item.id))
          .map((item: HelmetDeviceRecord) => item.deviceId);

        openChargeModal(true, {
          ids: selectedDeviceIds,
        });
      }

      function handleReminderSuccess() {
        reload();
      }

      function handleDeviceConfig() {
        const selectedKeys = getSelectRowKeys() as string[];
        if (selectedKeys.length === 0) {
          createMessage.warning('请选择要配置参数的设备');
          return;
        }

        // 获取选中设备的deviceId列表
        const selectedRows = getSelectRows() as HelmetDeviceRecord[];
        const selectedDevices = selectedRows.map((row) => row.deviceId);

        openConfigModal(true, {
          deviceIds: selectedDevices,
          isBatch: selectedDevices.length > 1,
        });
      }

      function handleConfigSuccess() {
        reload();
      }

      function handleRowConfig(record: HelmetDeviceRecord) {
        openConfigModal(true, {
          deviceId: record.deviceId,
        });
      }

      function handleShowUsageRecord(record: HelmetDeviceRecord) {
        openUsageRecordModal(true, {
          record,
        });
      }

      /**
       * 导出数据
       */
      async function handleExport() {
        try {
          exportLoading.value = true;

          // 获取当前查询表单的参数
          const form = await getForm();
          const searchParams = form?.getFieldsValue() || {};

          // 构建导出参数（和列表查询接口参数保持一致）
          const exportParams = {
            ...searchParams,
            // 可以添加其他需要的参数
          };

          // 调用导出接口 - 返回的是包含下载链接的JSON
          const response = await defHttp.get(
            {
              url: '/swm/swmHelmetDevice/export',
              params: exportParams,
            },
            { isReturnNativeResponse: true },
          );

          // 处理响应数据
          if (response && response.data) {
            const result = response.data;

            if (result.result === 'true' && result.data) {
              // 从响应中获取下载地址
              const downloadUrl = result.data;

              // 创建隐藏的链接进行下载
              const link = document.createElement('a');
              link.href = downloadUrl;
              link.target = '_blank'; // 新窗口打开
              link.style.display = 'none';

              // 可以尝试从URL中提取文件名，如果没有就使用默认文件名
              const fileName =
                downloadUrl.split('/').pop() || `安全帽设备列表_${new Date().getTime()}.xlsx`;
              link.download = fileName;

              document.body.appendChild(link);
              link.click();
              document.body.removeChild(link);

              createMessage.success('导出成功，文件开始下载');
            } else {
              createMessage.error(result.message || '导出失败');
            }
          } else {
            createMessage.error('导出响应格式错误');
          }
        } catch (error) {
          console.error('导出失败', error);
          createMessage.error('导出失败，请重试');
        } finally {
          exportLoading.value = false;
        }
      }

      /**
       * 解绑安全帽
       * @param record 安全帽设备记录
       */
      async function handleUnbindHelmet(record: HelmetDeviceRecord) {
        try {
          if (!record.assignedPerson) {
            createMessage.warning('该安全帽未绑定人员');
            return;
          }

          // 1. 先通过安全帽设备接口解绑人员
          await unassignPerson(record.deviceId);

          // 2. 如果有绑定的人员身份证信息，清除人员表中的安全帽关联
          if (record.assignedPerson) {
            try {
              // 根据身份证号查询人员信息并清除安全帽关联
              const personData = await defHttp.get({
                url: '/swm/swmPerson/getActivePersonFromCache',
                params: { identityCard: record.assignedPerson },
              });

              if (personData && personData.success && personData.data) {
                // 清除人员的安全帽关联
                await defHttp.post(
                  {
                    url: '/swm/swmPerson/clearSafetyHelmet',
                    params: { id: personData.data.id },
                  },
                  { isTransformResponse: false },
                );
              }
            } catch (error) {
              console.error('清除人员安全帽关联失败', error);
              // 不阻止主流程，因为安全帽侧的解绑已经成功
            }
          }

          createMessage.success('安全帽解绑成功');
          reload(); // 刷新列表
        } catch (error) {
          console.error('解绑安全帽失败', error);
          createMessage.error('解绑安全帽失败，请重试');
        }
      }

      return {
        registerTable,
        registerModal,
        registerChargeModal,
        registerUsageRecordModal,
        registerConfigModal,
        handleCreate,
        handleEdit,
        handleDelete,
        handleSuccess,
        handleBatchReminder,
        handleReminderSuccess,
        handleDeviceConfig,
        handleConfigSuccess,
        handleRowConfig,
        handleShowUsageRecord,
        handleUnbindHelmet,
        handleExport, // 导出方法
        getBatteryStatus,
        exportLoading,
        importModal,
        openImportModal,
      };
    },
  });
</script>

<style lang="less" scoped>
  .helmet-list-container {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .page-header {
    margin-bottom: 16px;
  }

  .page-title {
    font-size: 18px;
    font-weight: bold;
    color: #303133;
  }
</style>
