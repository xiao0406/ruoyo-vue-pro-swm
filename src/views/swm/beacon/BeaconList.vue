<template>
  <div class="beacon-list-container">
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleCreate">
          <Icon icon="ant-design:plus-outlined" /> 新增
        </a-button>
        <a-button type="primary" :disabled="checkedKeys.length === 0" @click="handleBatchManage"
          >批量管理</a-button
        >
        <a-button type="primary" @click="handleBeacon">信标标注</a-button>

        <a-button type="primary" preIcon="tdesign:folder-import" @click="openImportModal">
          导入
        </a-button>
        <a-button type="primary" preIcon="tdesign:folder" @click="importBeaconList">
          更新信标到算法
        </a-button>
      </template>

      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <TableAction
            :actions="[
              {
                icon: 'clarity:note-edit-line',
                tooltip: '编辑',
                onClick: handleEdit.bind(null, record),
              },
              {
                icon: 'ant-design:delete-outlined',
                color: 'error',
                tooltip: '删除',
                popConfirm: {
                  title: '是否确认删除',
                  confirm: handleDelete.bind(null, record),
                },
              },
            ]"
          />
        </template>
        <template v-else-if="column.dataIndex === 'beaconType'">
          <DictLabel dictType="beacon_type_enum" :dictValue="record.beaconType" />
        </template>
        <template v-else-if="column.dataIndex === 'beaconStatus'">
          <DictLabel dictType="beacon_status_enum" :dictValue="record.beaconStatus" />
        </template>
        <template v-else-if="column.dataIndex === 'deployStatus'">
          <!-- 直接使用字典组件 -->
          <DictLabel dictType="deploy_status_enum" :dictValue="record.deployStatus" />
        </template>
        <template v-else-if="column.dataIndex === 'area'">
          <!-- 显示区域名称 -->
          {{ areaMap.get(record.area) || record.area || '-' }}
        </template>
        <template v-else-if="column.dataIndex === 'beaconColor'">
          <!-- 显示信标颜色 -->
          <div
            class="beacon-color"
            :style="{ backgroundColor: record.beaconColor || '#ff0000' }"
            :title="record.beaconColor || '默认红色'"
          ></div>
        </template>
      </template>
    </BasicTable>
    <BeaconModal @register="registerModal" @success="handleSuccess" />
    <batchModal @register="managementModal" @success="batchSuccess" />
    <UploadModal
      title="信标管理"
      acceptString=".xls,.xlsx"
      prefix="/swm"
      url="/swmBeaconStation/importData"
      @register="importModal"
      @success="handleSuccess"
      templateShowApi="/swmBeaconStation/export"
    />
  </div>
</template>

<script lang="ts" setup name="ViewsSwmBeaconBeaconList">
  import { ref, onMounted, computed, createVNode } from 'vue';
  import { BasicTable, useTable, TableAction } from '@/components/swm/Table';
  import { getBeaconList, deleteBeacon, getBeaconLocationForMap } from '@/api/swm/beacon';
  import { refreshAllCache } from '@/api/swm/cache';
  import { useModal } from '@/components/swm/Modal';
  import BeaconModal from './BeaconModal.vue';
  import BatchModal from './batchModal.vue';
  import { Icon } from '@/components/swm/Icon';
  import { router } from '@/router';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { DictLabel, useDict } from '@/components/swm/Dict';
  import { useUserStore } from '@/store/modules/user';
  import { getAreaOptions } from '@/api/swm/area';
  import UploadModal from '@/components/swm/UploadModal/index.vue';
  import { Input, Modal } from 'ant-design-vue';
  import { dictDataListData } from '@/api/sys/dictData';
  import axios from 'axios';

  const checkedKeys = ref<Array<string>>([]);
  const { createMessage } = useMessage();

  const [registerModal, { openModal: AddModal }] = useModal();
  const [managementModal, { openModal: batchManagementModal }] = useModal();
  const [importModal, { openModal: openImportModal }] = useModal();

  const { initDict, getDictList } = useDict();
  const areaOptions = ref<Array<{ value: string; label: string; areaName: string }>>([]);
  const areaMap = ref<Map<string, string>>(new Map());

  // 初始化字典数据和区域选项
  onMounted(async () => {
    try {
      // 获取用户存储以清除字典缓存
      const userStore = useUserStore();
      // 清除部署状态字典缓存
      const dictMap = userStore.getPageCacheByKey('dictListMap', {});
      delete dictMap['deploy_status_enum'];

      // 重新加载所有字典
      await initDict(['beacon_type_enum', 'beacon_status_enum', 'deploy_status_enum']);

      // 检查部署状态字典是否正确加载
      const deployStatusDict = getDictList('deploy_status_enum');
      console.log('部署状态字典数据:', deployStatusDict);

      if (deployStatusDict.length === 0) {
        // 尝试直接从API加载，绕过缓存
        const { dictDataTreeData } = await import('@/api/sys/dictData');
        const dictData = await dictDataTreeData({ dictType: 'deploy_status_enum' });
        console.log('直接从API加载字典:', dictData);

        // 手动更新缓存
        if (dictData && dictData.length > 0) {
          dictMap['deploy_status_enum'] = dictData;
          console.log('字典数据已手动更新');
        } else {
          createMessage.error('部署状态字典加载失败');
        }
      }

      // 加载区域选项
      await loadAreaOptions();
    } catch (error: any) {
      console.error('字典加载异常:', error);
      createMessage.error('字典加载异常: ' + (error.message || String(error)));
    }
  });

  // 加载区域选项
  async function loadAreaOptions() {
    try {
      const result = await getAreaOptions();
      if (result.success && result.options) {
        areaOptions.value = result.options;
        // 构建区域ID到名称的映射
        areaMap.value.clear();
        result.options.forEach((option: any) => {
          areaMap.value.set(option.value, option.label);
        });
        console.log('区域选项加载成功:', areaOptions.value);
      } else {
        console.warn('区域选项加载失败:', result.message);
      }
    } catch (error: any) {
      console.error('区域选项加载异常:', error);
      createMessage.error('区域选项加载异常: ' + (error.message || String(error)));
    }
  }
  //标题和上面导航栏保持一致
  const pageTitle = computed(() => {
    // 假设你通过路由元信息定义了页面标题
    return router.currentRoute.value.meta.title || '监测设置管理';
  });
  const [registerTable, { reload }] = useTable({
    title: pageTitle.value,
    api: getBeaconList,
    rowKey: 'id',
    searchInfo: {
      orderBy: 'beacon_type asc',
    },
    columns: [
      {
        title: 'MAC地址',
        dataIndex: 'beaconId',
        width: 120,
      },
      {
        title: 'Major',
        dataIndex: 'major',
        width: 100,
      },
      {
        title: 'Minor',
        dataIndex: 'minor',
        width: 100,
      },
      {
        title: '设备名称',
        dataIndex: 'deviceName',
        width: 120,
      },
      {
        title: '信标类型',
        dataIndex: 'beaconType',
        width: 120,
      },
      {
        title: '所在位置',
        dataIndex: 'location',
        width: 180,
      },
      {
        title: '所属区域',
        dataIndex: 'area',
        width: 120,
      },
      {
        title: '信标颜色',
        dataIndex: 'beaconColor',
        width: 100,
      },
      {
        title: '图纸像素X坐标',
        dataIndex: 'pixelX',
        width: 120,
      },
      {
        title: '图纸像素Y坐标',
        dataIndex: 'pixelY',
        width: 120,
      },
      {
        title: '实际地址X坐标',
        dataIndex: 'realX',
        width: 120,
      },
      {
        title: '实际地址Y坐标',
        dataIndex: 'realY',
        width: 120,
      },
      {
        title: '信标状态',
        dataIndex: 'beaconStatus',
        width: 120,
      },
      {
        title: '部署状态',
        dataIndex: 'deployStatus',
        width: 120,
      },
      {
        title: '操作',
        dataIndex: 'action',
        key: 'action',
        width: 100,
        fixed: 'right',
      },
    ],
    showTableSetting: true,
    canResize: true,
    useSearchForm: true,
    formConfig: {
      baseColProps: { lg: 6, md: 8 },
      labelWidth: 100,
      schemas: [
        {
          field: 'beaconType',
          label: '信标类型',
          component: 'Select',
          componentProps: {
            dictType: 'beacon_type_enum',
            allowClear: true,
          },
          colProps: { span: 8 },
        },
        {
          field: 'beaconId',
          label: 'MAC地址',
          component: 'Input',
          colProps: { span: 8 },
        },
        {
          field: 'deviceName',
          label: '设备名称',
          component: 'Input',
          colProps: { span: 8 },
        },
        {
          field: 'beaconStatus',
          label: '信标状态',
          component: 'Select',
          componentProps: {
            dictType: 'beacon_status_enum',
            allowClear: true,
          },
          colProps: { span: 8 },
        },
        {
          field: 'deployStatus',
          label: '部署状态',
          component: 'Select',
          componentProps: {
            dictType: 'deploy_status_enum',
            allowClear: true,
          },
          colProps: { span: 8 },
        },
        {
          field: 'location',
          label: '所在位置',
          component: 'Input',
          colProps: { span: 8 },
        },
        {
          field: 'area',
          label: '所属区域',
          component: 'Select',
          componentProps: {
            options: areaOptions,
            allowClear: true,
            placeholder: '请选择所属区域',
          },
          colProps: { span: 8 },
        },
      ],
    },
    rowSelection: {
      onChange: (selectedRowKeys) => {
        checkedKeys.value = selectedRowKeys as string[];
      },
    },
  });

  // 新增
  function handleCreate() {
    AddModal(true, {
      isUpdate: false,
    });
  }

  // 删除
  async function handleDelete(record: Recordable) {
    await deleteBeacon(record.id);
    createMessage.success('删除成功');
    reload();

    // 刷新缓存
    // @author Shawn
    // @date 2025/01/09
    try {
      await refreshAllCache();
    } catch (error) {
      console.error('刷新缓存失败:', error);
    }
  }

  // 批量管理
  function handleBatchManage() {
    batchManagementModal(true, {
      selectedIds: checkedKeys.value,
    });
  }

  // 信标标注
  function handleBeacon() {
    router.push({ path: '/swm/beaconMap/beaconBase' });
  }

  function handleEdit(record: Recordable) {
    AddModal(true, {
      record,
      isUpdate: true,
    });
  }

  function handleSuccess() {
    reload();

    // 刷新缓存
    // @author Shawn
    // @date 2025/01/09
    try {
      refreshAllCache();
    } catch (error) {
      console.error('刷新缓存失败:', error);
    }
  }

  function batchSuccess() {
    reload();

    // 刷新缓存
    // @author Shawn
    // @date 2025/01/09
    try {
      refreshAllCache();
    } catch (error) {
      console.error('刷新缓存失败:', error);
    }
  }
  const userStore = useUserStore();
  const currentCorpCode = userStore.getPageCacheByKey('currentCorpCode', '0');

  const importBeaconList = () => {
    const password = ref('');

    Modal.confirm({
      title: '安全验证',
      content: createVNode(Input.Password, {
        placeholder: '请输入操作密码',
        onChange: (e: any) => {
          password.value = e.target.value;
        },
      }),
      onOk() {
        if (password.value != 'ZJZK8888') {
          createMessage.error('密码错误，无法执行操作');
          return Promise.reject(); // 返回 reject 可以阻止弹窗关闭，让用户重新输入
        }
        // let formatResult = getExportAlgorithmFormat();
        // 密码正确，执行原有逻辑
        getBeaconLocationForMap()
          .then((formatResult: any) => {
            dictDataListData({ dictType: 'corp_code_chat_base_url ' })
              .then((dictResult) => {
                console.log(dictResult);
                if (dictResult?.length > 0) {
                  let serverIp: any = null;
                  dictResult?.forEach((element) => {
                    if (element.dictValue == currentCorpCode) {
                      serverIp = element.dictLabelRaw;
                    }
                  });

                  if (serverIp) {
                    let serverName = '/algorithm/updateConfigBeacons';
                    const requestUrl = serverIp + serverName;

                    // 发送请求更换算法
                    axios
                      .post(requestUrl, {
                        beacons: formatResult,
                      })
                      .then((_response) => {
                        createMessage.success('更新信标到算法成功');
                      })
                      .catch((_error) => {
                        createMessage.error('更新信标到算法失败，算法服务异常');
                      });
                  } else {
                    createMessage.error('更新失败,当前租户没有匹配的算法服务');
                  }
                } else {
                  createMessage.error('更新失败,当前租户没有匹配的算法服务');
                }
              })
              .catch((_err) => {
                createMessage.error('获取字典数据失败');
              });
          })
          .catch((_err) => {
            createMessage.error('获取信标算法格式失败');
          });

        return Promise.resolve(); // 允许弹窗关闭
      },
      onCancel() {
        password.value = '';
      },
    });
  };

  function getExportAlgorithmFormat() {
    return {
      '80:ec:cc:d2:3c:28': { location: 'L4', x: 2134.0, y: 3127.0 },
      '80:ec:cc:d2:40:43': { location: 'L5', x: 2134.0, y: 3434.0 },
      '80:ec:cc:d2:3d:cb': { location: 'L6', x: 2134.0, y: 3741.0 },
      '80:ec:cc:d2:3d:2c': { location: 'D1', x: 2154.0, y: 4052.0 },
      '80:ec:cc:d2:3d:a0': { location: 'B1', x: 2134.0, y: 2810.0 },
      '80:ec:cc:d2:3d:76': { location: 'L3', x: 2134.0, y: 2513.0 },
      '80:ec:cc:d2:2f:e9': { location: 'L2', x: 2134.0, y: 2206.0 },
      '80:ec:cc:d2:3b:af': { location: 'L1', x: 2134.0, y: 1899.0 },
      '80:ec:cc:d2:3e:16': { location: 'B2', x: 2522.0, y: 2810.0 },
      '80:ec:cc:d2:3d:2f': { location: 'D2', x: 2522.0, y: 4052.0 },
      '80:ec:cc:d2:3b:b1': { location: 'A2', x: 2522.0, y: 1592.0 },
      '80:ec:cc:d2:3d:fc': { location: 'A1', x: 2154.0, y: 1592.0 },
      '80:ec:cc:d2:3e:0e': { location: 'D3', x: 2922.0, y: 4048.0 },
    };
  }
</script>

<style lang="less" scoped>
  .beacon-list-container {
    height: 100%;
    display: flex;
    flex-direction: column;

    .page-header {
      margin-bottom: 16px;
    }

    .page-title {
      font-size: 18px;
      font-weight: bold;
      color: #303133;
    }
  }

  .beacon-color {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    border: 2px solid #d9d9d9;
    display: inline-block;
    cursor: pointer;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
</style>
