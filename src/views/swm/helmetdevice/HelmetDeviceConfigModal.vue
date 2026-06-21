<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    @ok="handleSubmit"
    width="1000px"
    :show-ok-btn="false"
    :show-cancel-btn="false"
  >
    <template #title>
      <Icon icon="ant-design:setting-outlined" class="pr-1 m-1" />
      <span> {{ getTitle.value }} </span>
    </template>
    <div style="padding: 0px 20px">
      <!-- 批量模式设备信息显示 -->
      <div v-if="isBatchMode" class="batch-device-info">
        <div class="info-title">
          <Icon icon="ant-design:info-circle-outlined" />
          将为以下 {{ selectedDevices.length }} 个设备配置相同参数：
        </div>
        <div class="device-tags">
          <a-tag v-for="deviceId in selectedDevices" :key="deviceId" color="blue">
            {{ deviceId }}
          </a-tag>
        </div>
      </div>

      <BasicForm @register="registerForm" />

      <!-- 进度显示区域 -->
      <div v-if="isProcessing" class="progress-section">
        <!-- 批量模式进度显示 -->
        <div v-if="isBatchMode" class="batch-progress-section">
          <div class="batch-header">
            <span class="progress-title">批量配置进度</span>
            <span class="progress-count"
              >{{ currentDeviceIndex }}/{{ selectedDevices.length }}</span
            >
          </div>

          <div class="current-device" v-if="currentDevice">
            <Icon icon="ant-design:loading-outlined" />
            当前设备: {{ currentDevice }}
          </div>

          <!-- 设备级进度条 -->
          <div class="device-progress">
            <span class="progress-label">设备进度:</span>
            <a-progress
              :percent="deviceProgressPercent"
              :status="progressStatus"
              :show-info="true"
            />
          </div>

          <!-- 设备列表状态 -->
          <div class="device-list">
            <div class="device-list-title">设备列表:</div>
            <div class="device-items">
              <div
                v-for="deviceId in selectedDevices"
                :key="deviceId"
                :class="getDeviceStatusClass(deviceId)"
              >
                <Icon :icon="getDeviceStatusIcon(deviceId)" />
                <span class="device-name">{{ deviceId }}</span>
                <span class="device-status">{{ getDeviceStatusText(deviceId) }}</span>
              </div>
            </div>
          </div>

          <!-- 参数级进度（当前设备的参数进度） -->
          <div class="parameter-progress" v-if="currentDevice">
            <span class="progress-label">参数进度: {{ currentIndex }}/{{ totalCount }}</span>
            <a-progress :percent="progressPercent" size="small" :show-info="true" />

            <div class="current-task">
              <Icon icon="ant-design:loading-outlined" v-if="progressStatus === 'normal'" />
              <Icon icon="ant-design:check-circle-filled" v-if="progressStatus === 'success'" />
              <Icon icon="ant-design:close-circle-filled" v-if="progressStatus === 'exception'" />
              {{ currentTask }}
            </div>
          </div>
        </div>

        <!-- 单设备模式进度显示 -->
        <div v-else class="single-progress-section">
          <div class="progress-header">
            <span class="progress-title">参数下发进度</span>
            <span class="progress-count">{{ currentIndex }}/{{ totalCount }}</span>
          </div>

          <a-progress :percent="progressPercent" :status="progressStatus" :show-info="true" />

          <div class="current-task">
            <Icon icon="ant-design:loading-outlined" v-if="progressStatus === 'normal'" />
            <Icon icon="ant-design:check-circle-filled" v-if="progressStatus === 'success'" />
            <Icon icon="ant-design:close-circle-filled" v-if="progressStatus === 'exception'" />
            {{ currentTask }}
          </div>

          <div class="task-list">
            <div
              v-for="(task, index) in taskList"
              :key="task.field"
              :class="getTaskClass(task.status)"
            >
              <Icon :icon="getTaskIcon(task.status)" />
              <span class="task-name">{{ task.name }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 自定义footer -->
    <template #footer>
      <a-button key="reset" @click="handleReset">重置</a-button>
      <a-button key="cancel" @click="closeModal">取消</a-button>
      <a-button key="submit" type="primary" @click="handleSubmit">确定</a-button>
    </template>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicForm, useForm, FormSchema } from '@/components/swm/Form/index';
  import { Icon } from '@/components/swm/Icon';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { Modal, Tag } from 'ant-design-vue';
  import { getDeviceConfig, sendProtocolToDevice } from '@/api/swm/helmetDevice';
  import { saveHelmetDeviceConfig } from '@/api/swm/helmetDeviceConfig';
  import { ProtocolGenerator, ProtocolResultHandler } from '@/utils/protocolUtils';

  export default defineComponent({
    name: 'HelmetDeviceConfigModal',
    components: { BasicModal, BasicForm, Icon, 'a-tag': Tag },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { createMessage } = useMessage();
      const deviceId = ref('');

      // 进度控制相关数据
      const isProcessing = ref(false);
      const currentIndex = ref(0);
      const totalCount = ref(0);
      const currentTask = ref('');
      const progressPercent = ref(0);
      const progressStatus = ref('normal');
      const taskList = ref([]);

      // 批量模式相关数据
      const selectedDevices = ref([]);
      const isBatchMode = ref(false);
      const currentDeviceIndex = ref(0);
      const currentDevice = ref('');
      const deviceProgressPercent = ref(0);
      const deviceResults = ref([]);
      const deviceStatusMap = ref({});

      // 进度控制函数
      function initProgress(protocols) {
        isProcessing.value = true;
        totalCount.value = protocols.length;
        currentIndex.value = 0;
        progressPercent.value = 0;
        progressStatus.value = 'normal';
        currentTask.value = '准备开始...';

        // 初始化任务列表
        taskList.value = protocols.map((protocol) => ({
          name: protocol.name,
          field: protocol.field,
          status: 'pending', // pending, processing, success, failed
        }));
      }

      function updateProgress(index, taskName, status = 'processing') {
        currentIndex.value = index + 1;
        currentTask.value = taskName;
        progressPercent.value = Math.round(((index + 1) / totalCount.value) * 100);

        // 更新任务状态
        if (index < taskList.value.length) {
          taskList.value[index].status = status;
        }
      }

      function finishProgress(success = true) {
        progressPercent.value = 100;
        progressStatus.value = success ? 'success' : 'exception';
        currentTask.value = success ? '所有参数处理完成' : '处理过程中出现错误';

        // 延时隐藏进度区域
        setTimeout(() => {
          isProcessing.value = false;
        }, 2000);
      }

      function getTaskIcon(status) {
        switch (status) {
          case 'success':
            return 'ant-design:check-circle-filled';
          case 'failed':
            return 'ant-design:close-circle-filled';
          case 'processing':
            return 'ant-design:loading-outlined';
          default:
            return 'ant-design:clock-circle-outlined';
        }
      }

      function getTaskClass(status) {
        return `task-item task-${status}`;
      }

      // 批量模式相关函数
      function initBatchProgress(devices, protocols) {
        selectedDevices.value = devices;
        isBatchMode.value = devices.length > 1;
        currentDeviceIndex.value = 0;
        deviceProgressPercent.value = 0;
        deviceResults.value = [];

        // 初始化设备状态
        deviceStatusMap.value = {};
        devices.forEach((deviceId) => {
          deviceStatusMap.value[deviceId] = 'pending';
        });

        // 如果是批量模式，也初始化单设备进度
        if (isBatchMode.value) {
          isProcessing.value = true;
          totalCount.value = protocols.length;

          // 初始化任务列表 - 修复批量模式下taskList未初始化的问题
          taskList.value = protocols.map((protocol) => ({
            name: protocol.name,
            field: protocol.field,
            status: 'pending',
          }));
        }
      }

      function updateDeviceStatus(deviceId, status) {
        deviceStatusMap.value[deviceId] = status;

        // 更新设备级进度
        const completedDevices = Object.values(deviceStatusMap.value).filter(
          (status) => status === 'success' || status === 'failed' || status === 'error',
        ).length;
        deviceProgressPercent.value = Math.round(
          (completedDevices / selectedDevices.value.length) * 100,
        );
      }

      function getDeviceStatusIcon(deviceId) {
        const status = deviceStatusMap.value[deviceId];
        switch (status) {
          case 'success':
            return 'ant-design:check-circle-filled';
          case 'failed':
          case 'error':
            return 'ant-design:close-circle-filled';
          case 'processing':
            return 'ant-design:loading-outlined';
          default:
            return 'ant-design:clock-circle-outlined';
        }
      }

      function getDeviceStatusText(deviceId) {
        const status = deviceStatusMap.value[deviceId];
        switch (status) {
          case 'success':
            return '(已完成)';
          case 'failed':
            return '(配置失败)';
          case 'error':
            return '(连接错误)';
          case 'processing':
            return '(进行中)';
          default:
            return '(等待中)';
        }
      }

      function getDeviceStatusClass(deviceId) {
        const status = deviceStatusMap.value[deviceId];
        return `device-item device-${status}`;
      }

      const getTitle = computed(() => ({
        icon: 'ant-design:setting-outlined',
        value: isBatchMode.value
          ? `批量参数配置 (${selectedDevices.value.length}个设备)`
          : '设备参数配置',
      }));

      const formSchema = computed((): FormSchema[] => [
        // 设备编号字段 - 只在单设备模式显示
        ...(isBatchMode.value
          ? []
          : [
              {
                field: 'deviceId',
                label: '设备编号',
                component: 'Input',
                required: true,
                componentProps: {
                  disabled: true,
                  style: { backgroundColor: '#f5f5f5' },
                },
                colProps: { span: 12 },
              },
            ]),
        {
          field: 'serverIp',
          label: '服务器IP',
          component: 'Input',
          componentProps: {
            placeholder: '请输入服务器IP',
          },
          colProps: { span: 12 },
        },
        {
          field: 'bluetoothScanWindow',
          label: '蓝牙扫描窗口',
          component: 'InputNumber',
          componentProps: {
            min: 1,
            style: { width: '100%' },
            addonAfter: '秒',
          },
          colProps: { span: 12 },
        },
        {
          field: 'serverPort',
          label: '端口Port',
          component: 'Input',
          componentProps: {
            placeholder: '请输入端口号',
          },
          colProps: { span: 12 },
        },
        {
          field: 'groupDuration',
          label: '每组时长',
          component: 'InputNumber',
          componentProps: {
            min: 1,
            style: { width: '100%' },
            addonAfter: '秒',
          },
          colProps: { span: 12 },
        },
        {
          field: 'normalBeaconCs',
          label: '普通信标CS',
          component: 'InputNumber',
          componentProps: {
            min: 0,
            style: { width: '100%' },
          },
          colProps: { span: 12 },
        },
        {
          field: 'specialBeaconCs',
          label: '特殊信标CS',
          component: 'InputNumber',
          componentProps: {
            min: 0,
            style: { width: '100%' },
          },
          colProps: { span: 12 },
        },
        {
          field: 'locationMode',
          label: '定位模式',
          component: 'Select',
          componentProps: {
            allowClear: true,
            placeholder: '请选择定位模式',
            options: [
              { label: 'GPS/北斗模式', value: '1' },
              { label: 'GPS/北斗+BT模式', value: '2' },
              { label: 'BT+GPS/北斗模式', value: '3' },
              { label: 'BT模式', value: '4' },
            ],
          },
          colProps: { span: 12 },
        },
        {
          field: 'deepSleepDuration',
          label: '深度休眠时长',
          component: 'InputNumber',
          componentProps: {
            min: 1,
            style: { width: '100%' },
            addonAfter: '分钟',
          },
          colProps: { span: 12 },
        },
        {
          field: 'bluetoothScanDuration',
          label: '蓝牙扫描持续时间窗口',
          component: 'InputNumber',
          componentProps: {
            min: 1,
            style: { width: '100%' },
            addonAfter: '0.1秒',
          },
          colProps: { span: 12 },
        },
        {
          field: 'sendInterval',
          label: '发送间隔',
          component: 'InputNumber',
          componentProps: {
            min: 1,
            style: { width: '100%' },
            addonAfter: '秒',
          },
          colProps: { span: 12 },
        },
        {
          field: 'hazardRetriggerInterval',
          label: '风险重新触发间隔',
          component: 'InputNumber',
          componentProps: {
            min: 1,
            style: { width: '100%' },
            addonAfter: '秒',
          },
          colProps: { span: 12 },
        },
        {
          field: 'sleepWakeupTime',
          label: '休眠唤醒时间',
          component: 'InputNumber',
          componentProps: {
            min: 1,
            style: { width: '100%' },
            addonAfter: '秒',
          },
          colProps: { span: 12 },
        },
        {
          field: 'timeInterval',
          label: '脱帽报警时间间隔',
          component: 'InputNumber',
          componentProps: {
            min: 0,
            style: { width: '100%' },
            addonAfter: '秒',
          },
          colProps: { span: 12 },
        },
        {
          field: 'beaconFilterName',
          label: '接收信标（名称）',
          component: 'Input',
          componentProps: {
            placeholder: '请输入接收信标名称',
          },
          colProps: { span: 24 },
        },
      ]);

      const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
        labelWidth: 150,
        baseColProps: { span: 24 },
        schemas: formSchema,
        showActionButtonGroup: false,
        actionColOptions: {
          span: 24,
        },
      });

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data: any) => {
        resetFields();
        setModalProps({ confirmLoading: false });

        // 重置批量模式相关状态
        isProcessing.value = false;
        selectedDevices.value = [];
        isBatchMode.value = false;
        currentDeviceIndex.value = 0;
        deviceResults.value = [];
        deviceStatusMap.value = {};

        if (data?.deviceIds) {
          // 批量模式
          selectedDevices.value = data.deviceIds;
          isBatchMode.value = data.isBatch || data.deviceIds.length > 1;

          if (!isBatchMode.value && data.deviceIds.length === 1) {
            // 单设备模式，预填数据
            deviceId.value = data.deviceIds[0];
            try {
              const deviceConfig = await getDeviceConfig(data.deviceIds[0]);
              setFieldsValue({
                deviceId: data.deviceIds[0],
                timeInterval: deviceConfig?.hatOffAlarmInterval * 0.4,
                ...deviceConfig,
              });
            } catch (error) {
              console.error('获取设备配置失败:', error);
              setFieldsValue({
                deviceId: data.deviceIds[0],
              });
            }
          } else {
            // 批量模式，不预填数据，表单为空
            setFieldsValue({});
          }
        } else if (data?.deviceId) {
          // 兼容旧的单设备模式
          deviceId.value = data.deviceId;
          selectedDevices.value = [data.deviceId];
          isBatchMode.value = false;

          try {
            const deviceConfig = await getDeviceConfig(data.deviceId);
            setFieldsValue({
              deviceId: data.deviceId,
              timeInterval: deviceConfig?.hatOffAlarmInterval * 0.4,
              ...deviceConfig,
            });
          } catch (error) {
            console.error('获取设备配置失败:', error);
            setFieldsValue({
              deviceId: data.deviceId,
            });
          }
        }
      });

      async function handleSubmit() {
        try {
          const values = await validate();
          setModalProps({ confirmLoading: true });
          if (
            values.timeInterval !== null &&
            values.timeInterval !== undefined &&
            values.timeInterval !== '' &&
            !isNaN(values.timeInterval)
          ) {
            values.hatOffAlarmInterval = values.timeInterval / 0.4;
          }
          // 1. 生成协议列表
          const protocols = ProtocolGenerator.generateProtocolList(values);
          // return;
          if (protocols.length === 0) {
            // 没有需要下发的协议，直接保存到数据库
            if (isBatchMode.value) {
              await handleBatchSave(values);
            } else {
              const saveResult = await saveHelmetDeviceConfig(values);
              if (saveResult.result) {
                Modal.success({
                  title: '操作完成',
                  content: '参数保存成功！',
                });
                closeModal();
                emit('success');
              } else {
                Modal.error({
                  title: '保存失败',
                  content: saveResult.message || '保存失败',
                });
              }
            }
            return;
          }

          // 2. 根据模式选择处理方式
          if (isBatchMode.value) {
            await handleBatchConfig(values, protocols);
          } else {
            await handleSingleConfig(values, protocols);
          }

          closeModal();
          emit('success');
        } catch (error) {
          console.error('参数配置失败:', error);
          finishProgress(false);
          Modal.error({
            title: '操作失败',
            content: '参数配置失败',
          });
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      /**
       * 处理单设备配置
       */
      async function handleSingleConfig(values, protocols) {
        // 初始化进度显示
        initProgress(protocols);

        // 开始下发协议到设备
        const protocolResults = await handleProtocolSend(protocols, deviceId.value);

        // 检查是否设备离线（空数组表示设备离线）
        if (protocolResults.length === 0) {
          // 设备离线，已经在 handleProtocolSend 中弹窗提示
          finishProgress(false);
          return;
        }

        // 检查所有下发结果并决定是否保存
        const resultSummary = ProtocolResultHandler.processResults(protocolResults);

        if (resultSummary.isAllSuccess) {
          // 全部成功，保存到数据库
          currentTask.value = '所有参数下发成功，正在保存到数据库...';
          const saveResult = await saveHelmetDeviceConfig(values);

          if (saveResult.result) {
            finishProgress(true);
            showSuccessResult(resultSummary);
          } else {
            finishProgress(false);
            Modal.error({
              title: '保存失败',
              content: '参数下发成功但保存到数据库失败：' + (saveResult.message || '未知错误'),
            });
          }
        } else {
          // 有失败，不保存数据库
          finishProgress(false);
          showFailureResult(resultSummary);
        }
      }

      /**
       * 处理批量配置
       */
      async function handleBatchConfig(values, protocols) {
        // 初始化批量进度
        initBatchProgress(selectedDevices.value, protocols);

        for (let i = 0; i < selectedDevices.value.length; i++) {
          const deviceId = selectedDevices.value[i];
          currentDeviceIndex.value = i + 1;
          currentDevice.value = deviceId;

          // 更新设备状态为处理中
          updateDeviceStatus(deviceId, 'processing');

          try {
            // 重置参数级进度
            currentIndex.value = 0;
            progressPercent.value = 0;
            progressStatus.value = 'normal';

            // 为当前设备下发所有协议（批量模式传入true）
            const results = await handleProtocolSend(protocols, deviceId, true);

            // 检查是否是设备离线（空数组表示设备离线）
            const isDeviceOffline = results.length === 0;
            const summary = isDeviceOffline ? null : ProtocolResultHandler.processResults(results);

            if (isDeviceOffline) {
              // 设备离线，不保存
              updateDeviceStatus(deviceId, 'failed');
              deviceResults.value.push({
                deviceId,
                status: 'offline',
                error: '设备未开机',
              });
            } else if (summary && summary.isAllSuccess) {
              // 保存到数据库
              currentTask.value = '参数下发成功，正在保存到数据库...';
              const saveData = { ...values, deviceId };
              const saveResult = await saveHelmetDeviceConfig(saveData);

              if (saveResult.result) {
                updateDeviceStatus(deviceId, 'success');
                deviceResults.value.push({ deviceId, status: 'success', summary });
              } else {
                updateDeviceStatus(deviceId, 'failed');
                deviceResults.value.push({
                  deviceId,
                  status: 'failed',
                  error: '保存到数据库失败：' + (saveResult.message || '未知错误'),
                });
              }
            } else {
              updateDeviceStatus(deviceId, 'failed');
              deviceResults.value.push({ deviceId, status: 'failed', summary });
            }
          } catch (error) {
            updateDeviceStatus(deviceId, 'error');
            deviceResults.value.push({
              deviceId,
              status: 'error',
              error: error.message || '网络错误',
            });
          }

          // 延时200ms，避免设备处理不过来
          if (i < selectedDevices.value.length - 1) {
            await new Promise((resolve) => setTimeout(resolve, 200));
          }
        }

        // 显示批量结果
        showBatchResult();
      }

      /**
       * 批量保存（无协议下发）
       */
      async function handleBatchSave(values) {
        let successCount = 0;
        let failedDevices = [];

        for (const deviceId of selectedDevices.value) {
          try {
            const saveData = { ...values, deviceId };
            const saveResult = await saveHelmetDeviceConfig(saveData);
            if (saveResult.result) {
              successCount++;
            } else {
              failedDevices.push(deviceId);
            }
          } catch (error) {
            failedDevices.push(deviceId);
          }
        }

        if (failedDevices.length === 0) {
          Modal.success({
            title: '批量保存完成',
            content: `所有 ${successCount} 个设备参数保存成功！`,
          });
        } else {
          Modal.warning({
            title: '批量保存完成',
            content: `
              成功: ${successCount} 个设备
              失败: ${failedDevices.length} 个设备

              失败设备: ${failedDevices.join(', ')}
            `,
          });
        }
      }

      /**
       * 处理协议下发
       * @param {Array} protocols - 协议列表
       * @param {string} deviceId - 设备ID
       * @param {boolean} isBatchMode - 是否批量模式
       */
      async function handleProtocolSend(protocols, deviceId, isBatchMode = false) {
        const results = [];
        let isFirstCommand = true;

        for (let i = 0; i < protocols.length; i++) {
          const protocol = protocols[i];

          // 更新进度状态
          updateProgress(i, protocol.name, 'processing');

          try {
            const result = await sendProtocolToDevice(deviceId, protocol.message);

            if (result.success) {
              results.push({
                name: protocol.name,
                field: protocol.field,
                status: 'success',
                message: '下发成功',
              });
              // 更新任务状态为成功
              taskList.value[i].status = 'success';
            } else {
              results.push({
                name: protocol.name,
                field: protocol.field,
                status: 'failed',
                message: result.message || '下发失败',
              });
              // 更新任务状态为失败
              taskList.value[i].status = 'failed';

              if (isFirstCommand) {
                // 第一个失败，设备离线
                if (!isBatchMode) {
                  // 单设备模式才弹窗
                  finishProgress(false);
                  Modal.error({
                    title: '设备未开机',
                    content: '设备没有开机，无法下发参数。参数未保存，请确保设备在线后重试。',
                  });
                }
                // 返回空数组表示设备离线
                return [];
              }
            }
          } catch (error) {
            results.push({
              name: protocol.name,
              field: protocol.field,
              status: 'error',
              message: error.message || '网络错误',
            });
            // 更新任务状态为失败
            taskList.value[i].status = 'failed';

            if (isFirstCommand) {
              if (!isBatchMode) {
                // 单设备模式才弹窗
                finishProgress(false);
                Modal.error({
                  title: '设备未开机',
                  content: '设备没有开机，无法下发参数。参数未保存，请确保设备在线后重试。',
                });
              }
              // 返回空数组表示设备离线
              return [];
            }
          }

          isFirstCommand = false;

          // 延时200ms，避免设备处理不过来
          if (i < protocols.length - 1) {
            await new Promise((resolve) => setTimeout(resolve, 200));
          }
        }

        return results;
      }

      /**
       * 显示成功结果
       */
      function showSuccessResult(resultSummary) {
        Modal.success({
          title: '操作完成',
          content: `所有 ${resultSummary.successCount} 个参数下发成功并已保存到数据库！`,
        });
      }

      /**
       * 显示失败结果
       */
      function showFailureResult(resultSummary) {
        const isDeviceOffline = resultSummary.total === 1 && resultSummary.failCount === 1;

        if (isDeviceOffline) {
          // 设备离线的情况已在协议下发过程中处理，这里不需要重复显示
          return;
        }

        Modal.error({
          title: '参数下发失败',
          content: `
            部分参数下发失败，参数未保存到数据库

            成功: ${resultSummary.successCount} 个
            失败: ${resultSummary.failCount} 个

            失败详情: ${resultSummary.failedItems}

            请检查设备状态后重试。
          `,
        });
      }

      /**
       * 显示批量配置结果
       */
      function showBatchResult() {
        const successCount = deviceResults.value.filter((r) => r.status === 'success').length;
        const failedCount = deviceResults.value.length - successCount;

        // 延时隐藏进度区域
        setTimeout(() => {
          isProcessing.value = false;
        }, 2000);

        if (failedCount === 0) {
          Modal.success({
            title: '批量配置完成',
            content: `所有 ${successCount} 个设备配置成功！`,
          });
        } else {
          // 分类统计失败原因
          const offlineDevices = deviceResults.value.filter((r) => r.status === 'offline');
          const failedDevices = deviceResults.value.filter(
            (r) => r.status === 'failed' || r.status === 'error',
          );

          let failureDetails = [];
          if (offlineDevices.length > 0) {
            failureDetails.push(`设备未开机: ${offlineDevices.map((r) => r.deviceId).join('、')}`);
          }
          if (failedDevices.length > 0) {
            const otherFailures = failedDevices
              .map((r) => {
                const reason = r.error || (r.summary && r.summary.failedItems) || '未知错误';
                return `${r.deviceId}(${reason})`;
              })
              .join('、');
            failureDetails.push(`其他失败: ${otherFailures}`);
          }

          Modal.warning({
            title: '批量配置完成',
            content: `
              成功: ${successCount} 个设备
              失败: ${failedCount} 个设备

              ${failureDetails.join('\n')}
            `,
          });
        }
      }

      /**
       * 处理重置按钮点击
       */
      async function handleReset() {
        Modal.confirm({
          title: '重置确认',
          content:
            '页面上的参数将会被清空,页面上有填写的参数才会下发设备，原来已经下发的参数在设备保持不变。',
          onOk: () => {
            // 获取当前设备编号
            const currentDeviceId = deviceId.value;
            // 重置所有字段
            resetFields();
            // 如果是单设备模式，重新设置设备编号
            if (!isBatchMode.value && currentDeviceId) {
              setFieldsValue({
                deviceId: currentDeviceId,
              });
            }
          },
        });
      }

      return {
        registerModal,
        registerForm,
        getTitle,
        handleSubmit,
        handleReset,
        closeModal,
        handleProtocolSend,
        showSuccessResult,
        showFailureResult,
        showBatchResult,
        // 进度相关
        isProcessing,
        currentIndex,
        totalCount,
        currentTask,
        progressPercent,
        progressStatus,
        taskList,
        getTaskIcon,
        getTaskClass,
        // 批量模式相关
        selectedDevices,
        isBatchMode,
        currentDeviceIndex,
        currentDevice,
        deviceProgressPercent,
        deviceResults,
        deviceStatusMap,
        getDeviceStatusIcon,
        getDeviceStatusText,
        getDeviceStatusClass,
      };
    },
  });
</script>

<style lang="less" scoped>
  .batch-device-info {
    margin-bottom: 20px;
    padding: 12px 16px;
    background: #f0f9ff;
    border: 1px solid #bae7ff;
    border-radius: 6px;

    .info-title {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;
      color: #1890ff;
      font-weight: 500;
      font-size: 14px;
    }

    .device-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;

      .ant-tag {
        margin: 0;
        font-family: monospace;
        font-size: 12px;
      }
    }
  }

  .progress-section {
    margin-top: 20px;
    padding: 16px;
    background: #fafafa;
    border-radius: 6px;
    border: 1px solid #e8e8e8;
  }

  .progress-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  .progress-title {
    font-weight: 500;
    color: #262626;
  }

  .progress-count {
    color: #8c8c8c;
    font-size: 14px;
  }

  .current-task {
    margin: 12px 0;
    display: flex;
    align-items: center;
    gap: 8px;
    color: #595959;
    font-size: 14px;
  }

  .task-list {
    max-height: 200px;
    overflow-y: auto;
    margin-top: 12px;
  }

  .task-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 4px 0;
    font-size: 13px;
    transition: all 0.2s;
  }

  .task-name {
    flex: 1;
  }

  .task-pending {
    color: #8c8c8c;
  }

  .task-processing {
    color: #1890ff;
    font-weight: 500;
  }

  .task-success {
    color: #52c41a;
  }

  .task-failed {
    color: #ff4d4f;
  }

  // 批量模式样式
  .batch-progress-section {
    .batch-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      font-weight: 500;
    }

    .current-device {
      margin: 8px 0;
      display: flex;
      align-items: center;
      gap: 8px;
      color: #1890ff;
      font-weight: 500;
    }

    .device-progress,
    .parameter-progress {
      margin: 12px 0;
    }

    .progress-label {
      display: block;
      margin-bottom: 4px;
      font-size: 13px;
      color: #8c8c8c;
    }

    .device-list {
      margin-top: 16px;

      .device-list-title {
        font-size: 13px;
        color: #8c8c8c;
        margin-bottom: 8px;
      }

      .device-items {
        max-height: 150px;
        overflow-y: auto;
      }
    }

    .device-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 4px 0;
      font-size: 13px;
      transition: all 0.2s;

      .device-name {
        flex: 1;
        font-family: monospace;
      }

      .device-status {
        font-size: 12px;
      }

      &.device-pending {
        color: #8c8c8c;
      }

      &.device-processing {
        color: #1890ff;
        font-weight: 500;
      }

      &.device-success {
        color: #52c41a;
      }

      &.device-failed,
      &.device-error {
        color: #ff4d4f;
      }
    }
  }

  .single-progress-section {
    // 保持原有的单设备样式
  }

  // 进度条样式优化
  :deep(.ant-progress-text) {
    color: #595959 !important;
  }

  :deep(.ant-progress-bg) {
    transition: all 0.3s ease;
  }
</style>

<style lang="less" scoped>
  :deep(.ant-form-item-label) {
    width: 150px !important;
  }
</style>
