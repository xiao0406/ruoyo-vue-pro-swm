<template>
  <a-modal
    :visible="internalVisible"
    title="批量下发 TCP 指令"
    :maskClosable="false"
    :destroy-on-close="false"
    :width="960"
    :footer="null"
    wrap-class-name="dispatch-modal"
    @update:visible="handleVisibleChange"
  >
    <a-tabs v-model:activeKey="modalActiveTab">
      <a-tab-pane key="form" tab="编辑指令">
        <a-form layout="vertical" class="dispatch-form">
          <a-form-item
            label="设备号"
            help="支持逗号、空格、换行分隔多个设备号，示例：helmet-00018,helmet-00019"
          >
            <a-textarea
              v-model:value="sendForm.deviceIdsInput"
              :rows="4"
              placeholder="helmet-00018,helmet-00019"
              :disabled="sending"
              allow-clear
            />
          </a-form-item>
          <a-form-item label="下发内容">
            <a-textarea
              v-model:value="sendForm.message"
              :rows="4"
              placeholder="$1A,PS,0,TTSD,请立即撤离,#\n"
              :disabled="sending"
              allow-clear
            />
          </a-form-item>
          <a-form-item label="备注">
            <a-textarea
              v-model:value="sendForm.remark"
              :rows="2"
              placeholder="备注信息（选填）"
              :disabled="sending"
              allow-clear
            />
          </a-form-item>
          <div class="form-actions">
            <a-space>
              <a-button type="primary" @click="handleSend" :loading="sending">
                下发
              </a-button>
              <a-button @click="handleReset" :disabled="sending">
                清空
              </a-button>
            </a-space>
          </div>
        </a-form>

        <div class="history-section">
          <div class="section-header">
            下发历史（仅保存在浏览器，最多 50 条）
          </div>
          <a-empty v-if="!historyRows.length" description="暂无历史记录" />
          <a-table
            v-else
            size="small"
            :data-source="historyRows"
            :columns="historyColumns"
            :pagination="false"
            :scroll="{ y: 240 }"
            row-key="id"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'devices'">
                <a-tooltip :title="record.deviceIds.join(', ')">
                  <span>{{ record.deviceCount }} 台</span>
                </a-tooltip>
              </template>
              <template v-else-if="column.key === 'message'">
                <a-tooltip :title="record.messageDisplay">
                  <span class="history-message">{{ record.messagePreview }}</span>
                </a-tooltip>
              </template>
              <template v-else-if="column.key === 'actions'">
                <a-space>
                  <a-button type="link" size="small" @click="handleHistoryFill(record)">
                    回填
                  </a-button>
                  <a-button type="link" size="small" danger @click="handleHistoryDelete(record.id)">
                    删除
                  </a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </div>
      </a-tab-pane>

      <a-tab-pane key="result" tab="下发结果">
        <div class="result-section">
          <a-alert
            type="info"
            show-icon
            message="成功只根据接口返回判断，失败无需重试的设备可以关闭窗口。"
            class="result-alert"
          />
          <a-empty v-if="!sendProgressList.length" description="暂无下发记录" />
          <a-table
            v-else
            size="small"
            :data-source="sendProgressList"
            :columns="progressColumns"
            :pagination="false"
            row-key="deviceId"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag v-if="record.status === 'success'" color="success">成功</a-tag>
                <a-tag v-else-if="record.status === 'failed'" color="error">失败</a-tag>
                <a-tag v-else>待发送</a-tag>
              </template>
              <template v-else-if="column.key === 'message'">
                <a-tooltip :title="record.message">
                  <span class="progress-message">{{ record.message }}</span>
                </a-tooltip>
              </template>
            </template>
          </a-table>
          <div class="result-actions">
            <a-space>
              <a-button type="primary" @click="openFormTab" :disabled="sending">
                返回编辑
              </a-button>
              <a-button @click="closeModal" :disabled="sending">
                关闭
              </a-button>
            </a-space>
          </div>
        </div>
      </a-tab-pane>
    </a-tabs>
  </a-modal>
</template>

<script lang="ts">
  import { computed, defineComponent, onMounted, reactive, ref, watch } from 'vue';
  import { message } from 'ant-design-vue';
  import { sendTcpDeviceCommand } from '@/api/swm/tcpDeviceCommandLog';
  import { formatToDateTime, dateUtil } from '@/utils/dateUtil';
  import {
    Modal,
    Tabs,
    Form,
    Input,
    Space,
    Table,
    Tag,
    Alert,
    Empty,
    Tooltip,
  } from 'ant-design-vue';

  interface SendHistoryItem {
    id: string;
    key: string;
    deviceIds: string[];
    deviceInput: string;
    message: string;
    remark: string;
    updatedAt: string;
  }

  interface HistoryRow extends SendHistoryItem {
    deviceCount: number;
    messagePreview: string;
    messageDisplay: string;
    deviceInput: string;
  }

  type SendStatus = 'pending' | 'success' | 'failed';

  interface SendProgressItem {
    deviceId: string;
    status: SendStatus;
    message: string;
  }

  interface NormalizedSendResult {
    success: boolean;
    message: string;
  }

  const HISTORY_STORAGE_KEY = 'tcpSendHistory';
  const HISTORY_LIMIT = 50;

  export default defineComponent({
    name: 'TcpDispatchModal',
    components: {
      'a-modal': Modal,
      'a-tabs': Tabs,
      'a-tab-pane': Tabs.TabPane,
      'a-form': Form,
      'a-form-item': Form.Item,
      'a-textarea': Input.TextArea,
      'a-space': Space,
      'a-table': Table,
      'a-tag': Tag,
      'a-alert': Alert,
      'a-empty': Empty,
      'a-tooltip': Tooltip,
    },
    props: {
      visible: {
        type: Boolean,
        default: false,
      },
    },
    emits: ['update:visible'],
    setup(props, { emit }) {
      const internalVisible = ref(props.visible);
      const modalActiveTab = ref<'form' | 'result'>('form');
      const sending = ref(false);

      watch(
        () => props.visible,
        (value) => {
          internalVisible.value = value;
        },
        { immediate: true },
      );

      watch(internalVisible, (visible) => {
        if (!visible) {
          sending.value = false;
          modalActiveTab.value = 'form';
          sendProgressList.value = [];
        }
      });

      const handleVisibleChange = (value: boolean) => {
        internalVisible.value = value;
        emit('update:visible', value);
      };

      const sendForm = reactive({
        deviceIdsInput: '',
        message: '',
        remark: '',
      });
      const sendProgressList = ref<SendProgressItem[]>([]);
      const historyList = ref<SendHistoryItem[]>([]);

      const historyColumns = [
        { title: '设备数量', dataIndex: 'deviceCount', key: 'devices', width: 120 },
        { title: '下发内容', dataIndex: 'messagePreview', key: 'message', width: 240 },
        { title: '备注', dataIndex: 'remark', key: 'remark', width: 200 },
        { title: '更新时间', dataIndex: 'updatedAt', key: 'updatedAt', width: 180 },
        { title: '操作', dataIndex: 'actions', key: 'actions', width: 160 },
      ];

      const progressColumns = [
        { title: '设备ID', dataIndex: 'deviceId', key: 'deviceId', width: 160 },
        { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
        { title: '接口返回', dataIndex: 'message', key: 'message' },
      ];

      const historyRows = computed<HistoryRow[]>(() => {
        return historyList.value.map((item) => {
          const deviceInput = item.deviceInput ?? item.deviceIds.join(',');
          const messageDisplay = item.message.replace(/\n/g, '\\n');
          const preview =
            messageDisplay.length > 30 ? `${messageDisplay.slice(0, 30)}...` : messageDisplay;
          return {
            ...item,
            deviceCount: item.deviceIds.length,
            messagePreview: preview,
            messageDisplay,
            deviceInput,
          };
        });
      });

      const parseDeviceIds = (input: string) => {
        if (!input) {
          return [];
        }
        const tokens = input
          .split(/[\s,，\n\r\t]+/)
          .map((id) => id.trim())
          .filter(Boolean);
        const unique: string[] = [];
        tokens.forEach((id) => {
          if (!unique.includes(id)) {
            unique.push(id);
          }
        });
        return unique;
      };

      const buildHistoryKey = (deviceIds: string[], messageText: string, remarkText: string) => {
        return `${deviceIds.join('|')}|${messageText}|${remarkText}`;
      };

      const saveHistory = () => {
        if (typeof window === 'undefined') {
          return;
        }
        window.localStorage.setItem(HISTORY_STORAGE_KEY, JSON.stringify(historyList.value));
      };

      const loadHistory = () => {
        if (typeof window === 'undefined') {
          return;
        }
        try {
          const cached = window.localStorage.getItem(HISTORY_STORAGE_KEY);
          if (!cached) {
            historyList.value = [];
            return;
          }
          const parsed = JSON.parse(cached);
          if (Array.isArray(parsed)) {
            historyList.value = parsed.slice(0, HISTORY_LIMIT).map((item: any) => {
              const deviceIds = Array.isArray(item.deviceIds) ? item.deviceIds : [];
              return {
                ...item,
                deviceIds,
                deviceInput: item.deviceInput ?? deviceIds.join(','),
              } as SendHistoryItem;
            });
          } else {
            historyList.value = [];
          }
        } catch (err) {
          console.error('加载下发历史失败:', err);
          historyList.value = [];
        }
      };

      const upsertHistory = (
        deviceIds: string[],
        rawMessage: string,
        remarkText: string,
        deviceInputRaw: string,
      ) => {
        const key = buildHistoryKey(deviceIds, rawMessage, remarkText);
        const now = formatToDateTime(dateUtil());
        const existingIndex = historyList.value.findIndex((item) => item.key === key);
        if (existingIndex >= 0) {
          const existing = historyList.value.splice(existingIndex, 1)[0];
          historyList.value.unshift({
            ...existing,
            deviceInput: deviceInputRaw,
            updatedAt: now || '',
          });
        } else {
          historyList.value.unshift({
            id: `${Date.now()}`,
            key,
            deviceIds,
            deviceInput: deviceInputRaw,
            message: rawMessage,
            remark: remarkText,
            updatedAt: now || '',
          });
          if (historyList.value.length > HISTORY_LIMIT) {
            historyList.value.length = HISTORY_LIMIT;
          }
        }
        saveHistory();
      };

      const normalizeSendResponse = (raw: unknown): NormalizedSendResult => {
        if (raw === undefined || raw === null) {
          return { success: false, message: '接口无返回' };
        }
        let payload: any = raw;
        if (typeof payload === 'string') {
          const trimmed = payload.trim();
          if (!trimmed) {
            return { success: false, message: '接口返回空字符串' };
          }
          try {
            payload = JSON.parse(trimmed);
          } catch (error) {
            return { success: false, message: trimmed };
          }
        }
        if (typeof payload !== 'object') {
          return { success: false, message: String(payload) };
        }
        const successFlag =
          payload.success === true ||
          payload.result === 'true' ||
          payload.result === true ||
          payload.code === 0 ||
          payload.code === '0';
        const msg = payload.msg || payload.message || (successFlag ? '下发成功' : '下发失败');
        return { success: successFlag, message: msg };
      };

      const handleSend = async () => {
        if (sending.value) {
          return;
        }
        const rawDeviceInput = (sendForm.deviceIdsInput || '').trim();
        const deviceIds = parseDeviceIds(rawDeviceInput);
        if (!deviceIds.length) {
          message.warning('请至少输入一个设备号');
          return;
        }
        const rawMessage = sendForm.message || '';
        const convertedMessage = rawMessage.replace(/\\n/g, '\n');
        if (!convertedMessage.trim()) {
          message.warning('请输入下发内容');
          return;
        }
        const remarkText = sendForm.remark.trim();

        sending.value = true;
        sendProgressList.value = deviceIds.map<SendProgressItem>((id) => ({
          deviceId: id,
          status: 'pending',
          message: '待发送',
        }));

        let successCount = 0;
        let failCount = 0;

        try {
          for (const item of sendProgressList.value) {
            try {
              const response = await sendTcpDeviceCommand(item.deviceId, convertedMessage);
              const normalized = normalizeSendResponse(response);
              if (normalized.success) {
                item.status = 'success';
                item.message = normalized.message;
                successCount += 1;
              } else {
                item.status = 'failed';
                item.message = normalized.message || '发送失败';
                failCount += 1;
              }
            } catch (error: any) {
              const errorMsg = error?.message || '发送异常';
              item.status = 'failed';
              item.message = errorMsg;
              failCount += 1;
            }
          }

          if (successCount) {
            upsertHistory(deviceIds, rawMessage, remarkText, rawDeviceInput);
          }

          if (successCount && !failCount) {
            message.success(`成功下发 ${successCount} 条指令`);
          } else if (successCount && failCount) {
            message.warning(`成功 ${successCount} 条，失败 ${failCount} 条`);
          } else {
            message.error('全部设备下发失败');
          }

          modalActiveTab.value = 'result';
        } finally {
          sending.value = false;
        }
      };

      const handleReset = () => {
        if (sending.value) {
          return;
        }
        sendForm.deviceIdsInput = '';
        sendForm.message = '';
        sendForm.remark = '';
      };

      const handleHistoryFill = (item: HistoryRow) => {
        if (sending.value) {
          return;
        }
        sendForm.deviceIdsInput = item.deviceInput;
        sendForm.message = item.message;
        sendForm.remark = item.remark;
        modalActiveTab.value = 'form';
      };

      const handleHistoryDelete = (id: string) => {
        const index = historyList.value.findIndex((item) => item.id === id);
        if (index < 0) {
          return;
        }
        historyList.value.splice(index, 1);
        saveHistory();
      };

      const openFormTab = () => {
        modalActiveTab.value = 'form';
      };

      const closeModal = () => {
        handleVisibleChange(false);
      };

      onMounted(() => {
        loadHistory();
      });

      return {
        internalVisible,
        modalActiveTab,
        sending,
        sendForm,
        sendProgressList,
        historyRows,
        historyColumns,
        progressColumns,
        handleSend,
        handleReset,
        handleHistoryFill,
        handleHistoryDelete,
        openFormTab,
        closeModal,
        handleVisibleChange,
      };
    },
  });
</script>

<style lang="less" scoped>
  .dispatch-form {
    margin-bottom: 16px;
    padding: 16px;
    background-color: #f7f8fa;
    border-radius: 4px;
  }

  .form-actions {
    margin-top: 8px;
    text-align: right;
  }

  .history-section {
    margin-top: 16px;
    padding: 16px;
    background-color: #f7f8fa;
    border-radius: 4px;
  }

  .section-header {
    font-weight: 600;
    margin-bottom: 8px;
  }

  .history-message,
  .progress-message {
    display: inline-block;
    max-width: 320px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    vertical-align: middle;
  }

  .result-section {
    padding: 16px;
    background-color: #f7f8fa;
    border-radius: 4px;
  }

  .result-alert {
    margin-bottom: 12px;
  }

  .result-actions {
    margin-top: 16px;
    text-align: right;
  }

  :deep(.dispatch-modal .ant-modal-body) {
    max-height: 70vh;
    overflow-y: auto;
    padding: 24px;
  }

  :deep(.dispatch-modal .ant-tabs-content) {
    padding-top: 8px;
  }
</style>
