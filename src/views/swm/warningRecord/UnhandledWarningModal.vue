<!--
  @author zwf
  @date 2025-05-16
-->
<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="'新增处置'"
    :width="800"
    :canFullscreen="false"
    :maskClosable="false"
    :footer="null"
    @afterOpen="onAfterOpen"
  >
    <div style="padding: 14px">
      <div class="unhandled-warning-container">
        <!-- 添加数据加载状态和调试信息 -->
        <div v-if="loading" class="loading-container">
          <a-spin tip="加载中..."></a-spin>
        </div>

        <div v-else class="warning-list">
          <!-- 添加调试信息显示数据长度 -->
          <div v-if="warningList.length > 0" class="debug-bar">
            <span>获取到 {{ warningList.length }} 条未处置报警数据</span>
          </div>

          <a-table
            :dataSource="warningList"
            :columns="columns"
            :pagination="{ pageSize: 5 }"
            :rowKey="(record) => record.id"
            :locale="{ emptyText: '暂无未处置预警数据' }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'action'">
                <a-button type="primary" @click="handleSelect(record)"> 选择处置 </a-button>
              </template>
            </template>
          </a-table>

          <!-- 添加调试信息显示 -->
          <div class="debug-info" v-if="warningList.length === 0">
            <p>未获取到数据，请检查API是否正确配置</p>
            <p>API路径: /swm/handleRecord/unhandledWarnings</p>
            <p>
              <a-button type="link" @click="reloadData">重试</a-button>
            </p>
          </div>

          <!-- 添加原始数据显示，方便调试 -->
          <div class="raw-data" v-if="showRawData && warningList.length > 0">
            <a-collapse>
              <a-collapse-panel key="1" header="原始数据 (调试用)">
                <pre>{{ JSON.stringify(warningList, null, 2) }}</pre>
              </a-collapse-panel>
            </a-collapse>
          </div>
        </div>

        <div class="modal-footer mt-4">
          <a-button @click="toggleRawData" v-if="warningList.length > 0">
            {{ showRawData ? '隐藏原始数据' : '显示原始数据' }}
          </a-button>
          <a-button @click="closeModal">取消</a-button>
        </div>
      </div>
    </div>
  </BasicModal>

  <WarningProcessModal @register="registerProcessModal" @success="handleSuccess" />
</template>

<script lang="ts">
  import { defineComponent, ref, onMounted } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { getUnhandledWarnings, UnhandledWarning } from '@/api/swm/warningRecord';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { useModal } from '@/components/swm/Modal';
  import { TableColumnType } from 'ant-design-vue';
  import WarningProcessModal from './WarningProcessModal.vue';

  export default defineComponent({
    name: 'UnhandledWarningModal',
    components: { BasicModal, WarningProcessModal },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { createMessage } = useMessage();
      const loading = ref(false);
      const warningList = ref<UnhandledWarning[]>([]);
      const showRawData = ref(false);

      // 列表列定义
      const columns: TableColumnType[] = [
        {
          title: '人员',
          dataIndex: 'personName',
          width: 100,
          key: 'personName',
        },
        {
          title: '报警类型',
          dataIndex: 'warningContent',
          width: 120,
          key: 'warningContent',
        },
        {
          title: '报警时间',
          dataIndex: 'warningTime',
          width: 150,
          key: 'warningTime',
        },
        {
          title: '报警记录',
          dataIndex: 'alarmRecord',
          width: 120,
          key: 'alarmRecord',
        },
        {
          title: '操作',
          key: 'action',
          width: 100,
          align: 'center',
        },
      ];

      // 切换显示原始数据
      function toggleRawData() {
        showRawData.value = !showRawData.value;
      }

      // 处置弹窗
      const [registerProcessModal, { openModal: openProcessModal }] = useModal();

      // 模态框
      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        console.log('模态框注册参数:', data);
        setModalProps({ confirmLoading: false });

        // 如果传入了immediate参数，则立即获取数据
        if (data?.immediate) {
          await fetchWarningList();
        }
      });

      // 模态框打开后回调
      function onAfterOpen() {
        console.log('模态框打开后回调触发');
        fetchWarningList();
      }

      // 重新加载数据
      function reloadData() {
        console.log('手动触发重新加载数据');
        fetchWarningList();
      }

      // 获取未处置报警列表
      async function fetchWarningList() {
        try {
          loading.value = true;
          console.log('正在请求未处置报警数据...');

          // 调用API获取数据
          const res = await getUnhandledWarnings();
          console.log('获取到未处置报警数据:', res);
          if (res && res.success && res.list) {
            warningList.value = res.list;
            console.log('赋值后的warningList:', warningList.value);

            if (res.list.length === 0) {
              createMessage.info('当前没有未处置的报警');
            } else {
              createMessage.success(`获取到${res.list.length}条未处置报警数据`);
            }
          } else {
            warningList.value = [];
            createMessage.info('当前没有未处置的报警');

            // 如果API返回异常，使用模拟数据用于演示
            if (process.env.NODE_ENV === 'development') {
              warningList.value = [
                {
                  id: '23',
                  personName: '杨十九',
                  warningType: '1',
                  warningContent: '高空预警',
                  warningTime: '2023-05-05 08:35:00',
                  alarmRecord: '防护设备异常',
                  alarmTime: '2023-05-05 08:35:30',
                  triggerReason:
                    '杨十九于2023-05-05 08:35（主动预警）（高空预警）功能，坐标位于北区J栋6楼，信标J605处',
                  handleStatus: '0',
                  warningTypeText: '主动预警',
                  handleStatusText: '未处置',
                },
                {
                  id: '9',
                  personName: '杨十九',
                  warningType: '1',
                  warningContent: '高空预警',
                  warningTime: '2023-05-05 08:35:00',
                  alarmRecord: '防护设备异常',
                  alarmTime: '2023-05-05 08:35:30',
                  triggerReason:
                    '杨十九于2023-05-05 08:35（主动预警）（高空预警）功能，坐标位于北区J栋6楼，信标J605处',
                  handleStatus: '0',
                  warningTypeText: '主动预警',
                  handleStatusText: '未处置',
                },
              ] as UnhandledWarning[];
              console.log('已加载模拟数据用于演示, 数据:', warningList.value);
              createMessage.warning('已加载模拟数据用于演示');
            }
          }
        } catch (error) {
          console.error('获取未处置报警列表失败', error);
          createMessage.error('获取未处置报警列表失败');
          warningList.value = [];

          // 出错时加载模拟数据用于演示
          if (process.env.NODE_ENV === 'development') {
            warningList.value = [
              {
                id: '23',
                personName: '杨十九',
                warningType: '1',
                warningContent: '高空预警',
                warningTime: '2023-05-05 08:35:00',
                alarmRecord: '防护设备异常',
                alarmTime: '2023-05-05 08:35:30',
                triggerReason:
                  '杨十九于2023-05-05 08:35（主动预警）（高空预警）功能，坐标位于北区J栋6楼，信标J605处',
                handleStatus: '0',
                warningTypeText: '主动预警',
                handleStatusText: '未处置',
              },
              {
                id: '9',
                personName: '杨十九',
                warningType: '1',
                warningContent: '高空预警',
                warningTime: '2023-05-05 08:35:00',
                alarmRecord: '防护设备异常',
                alarmTime: '2023-05-05 08:35:30',
                triggerReason:
                  '杨十九于2023-05-05 08:35（主动预警）（高空预警）功能，坐标位于北区J栋6楼，信标J605处',
                handleStatus: '0',
                warningTypeText: '主动预警',
                handleStatusText: '未处置',
              },
            ] as UnhandledWarning[];
            console.log('已加载模拟数据用于演示, 数据:', warningList.value);
            createMessage.warning('API请求失败，已加载模拟数据用于演示');
          }
        } finally {
          loading.value = false;
        }
      }

      // 选择处置
      function handleSelect(record: UnhandledWarning) {
        console.log('选择处置报警:', record);
        openProcessModal(true, {
          record,
        });
        closeModal();
      }

      // 处置成功回调
      function handleSuccess() {
        emit('success');
      }

      // 组件挂载时自动获取数据（防止其他方式都失效）
      onMounted(() => {
        console.log('UnhandledWarningModal组件已挂载');
      });

      return {
        registerModal,
        closeModal,
        registerProcessModal,
        loading,
        warningList,
        columns,
        handleSelect,
        handleSuccess,
        onAfterOpen,
        reloadData,
        showRawData,
        toggleRawData,
      };
    },
  });
</script>

<style lang="less" scoped>
  .unhandled-warning-container {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .warning-list {
    flex: 1;
  }

  .modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 16px;
  }

  .loading-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 200px;
  }

  .debug-info {
    margin-top: 16px;
    padding: 16px;
    background-color: #f5f5f5;
    border-radius: 4px;
    color: #666;
  }

  .raw-data {
    margin-top: 16px;
    background-color: #f5f5f5;
    border-radius: 4px;

    pre {
      max-height: 300px;
      overflow: auto;
      padding: 8px;
      font-size: 12px;
    }
  }

  .debug-bar {
    margin-bottom: 8px;
    padding: 8px;
    background-color: #e6f7ff;
    border-radius: 4px;
    color: #1890ff;
    font-size: 14px;
  }
</style>
