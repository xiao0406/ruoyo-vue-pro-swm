<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    title="排班时间管理"
    @ok="handleSubmit"
    :width="960"
  >
    <div v-if="loading" class="loading-container">
      <a-spin tip="加载中..."></a-spin>
    </div>

    <BasicForm v-else @register="registerForm" class="schedule-form">
      <!-- 早班 -->
      <template #earlyShift>
        <div class="shift-container">
          <!-- 早班：上午 -->
          <div class="shift-row">
            <div class="time-section">
              <span class="sub-label">上午</span>
              <a-time-picker
                v-model:value="shifts.early.startTimeRef"
                format="HH:mm"
                placeholder="上班"
                class="time-picker"
              />
              <span class="separator">至</span>
              <a-time-picker
                v-model:value="shifts.early.noonEndTimeRef"
                format="HH:mm"
                placeholder="下班"
                class="time-picker"
              />
            </div>

            <!-- 休息配置 (仅在第一行显示) -->
            <div class="config-section">
              <div class="config-item">
                <span class="block-label">休息时长(h):</span>
                <a-input-number
                  v-model:value="shifts.early.restTime"
                  :min="0"
                  :max="24"
                  :step="0.5"
                  :precision="1"
                  class="rest-input"
                />
              </div>
              <div class="config-item flex-fill">
                <span class="block-label">休息日:</span>
                <a-select
                  v-model:value="shifts.early.restDaysArray"
                  mode="multiple"
                  placeholder="请选择休息日"
                  :options="REST_DAY_OPTIONS"
                  class="days-select"
                />
              </div>
            </div>
          </div>

          <!-- 早班：下午 -->
          <div class="shift-row">
            <div class="time-section">
              <span class="sub-label">下午</span>
              <a-time-picker
                v-model:value="shifts.early.afterStartTimeRef"
                format="HH:mm"
                placeholder="上班"
                class="time-picker"
              />
              <span class="separator">至</span>
              <a-time-picker
                v-model:value="shifts.early.endTimeRef"
                format="HH:mm"
                placeholder="下班"
                class="time-picker"
              />
            </div>
            <!-- 下午右侧留白，保持与上午严格对齐 -->
            <div class="config-section"></div>
          </div>
        </div>
      </template>

      <!-- 晚班 -->
      <template #nightShift>
        <div class="shift-container">
          <div class="shift-row">
            <div class="time-section">
              <span class="sub-label">晚班</span>
              <a-time-picker
                v-model:value="shifts.night.startTimeRef"
                format="HH:mm"
                placeholder="上班"
                class="time-picker"
              />
              <span class="separator">至</span>
              <a-time-picker
                v-model:value="shifts.night.endTimeRef"
                format="HH:mm"
                placeholder="下班"
                class="time-picker"
              />
            </div>

            <!-- 休息配置 -->
            <div class="config-section">
              <div class="config-item">
                <span class="block-label">休息时长(h):</span>
                <a-input-number
                  v-model:value="shifts.night.restTime"
                  :min="0"
                  :max="24"
                  :step="0.5"
                  :precision="1"
                  class="rest-input"
                />
              </div>
              <div class="config-item flex-fill">
                <span class="block-label">休息日:</span>
                <a-select
                  v-model:value="shifts.night.restDaysArray"
                  mode="multiple"
                  placeholder="请选择休息日"
                  :options="REST_DAY_OPTIONS"
                  class="days-select"
                />
              </div>
            </div>
          </div>
        </div>
      </template>
    </BasicForm>
  </BasicModal>
</template>

<script setup lang="ts">
  import { ref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicForm, useForm } from '@/components/swm/Form/index';
  import { formSchema } from './AttendanceData';
  import {
    getScheduleTimeList,
    saveAllScheduleTime,
    ScheduleTimeInfo,
  } from '@/api/swm/scheduleTime';
  import { useMessage } from '@/hooks/swm/useMessage';
  import {
    TimePicker as ATimePicker,
    Spin as ASpin,
    Select as ASelect,
    InputNumber as AInputNumber,
  } from 'ant-design-vue';
  import dayjs, { Dayjs } from 'dayjs';

  const emit = defineEmits(['success', 'register']);
  const { createMessage } = useMessage();
  const loading = ref(false);

  const REST_DAY_OPTIONS = [
    { label: '周一', value: '1' },
    { label: '周二', value: '2' },
    { label: '周三', value: '3' },
    { label: '周四', value: '4' },
    { label: '周五', value: '5' },
    { label: '周六', value: '6' },
    { label: '周日', value: '7' },
  ];

  interface ShiftData {
    id: string;
    startTimeRef: Dayjs | null;
    noonEndTimeRef?: Dayjs | null;
    afterStartTimeRef?: Dayjs | null;
    endTimeRef: Dayjs | null;
    restTime: number;
    restDaysArray: string[];
  }

  const createCache = (timeout = 5 * 60 * 1000) => {
    let data: any = null;
    let time = 0;
    return {
      get: () => (Date.now() - time < timeout ? data : null),
      set: (newData: any) => {
        data = newData;
        time = Date.now();
      },
      clear: () => {
        data = null;
        time = 0;
      },
    };
  };
  const scheduleCache = createCache();

  const toDayjs = (timeStr?: string): Dayjs | null =>
    timeStr ? dayjs(`2000-01-01 ${timeStr}`) : null;
  const toTimeStr = (time?: Dayjs | null): string => (time ? time.format('HH:mm') : '');

  const shifts = reactive<{ early: ShiftData; night: ShiftData }>({
    early: {
      id: '',
      startTimeRef: null,
      noonEndTimeRef: null,
      afterStartTimeRef: null,
      endTimeRef: null,
      restTime: 0,
      restDaysArray: [],
    },
    night: { id: '', startTimeRef: null, endTimeRef: null, restTime: 0, restDaysArray: [] },
  });

  const resetShifts = () => {
    Object.values(shifts).forEach((shift) => {
      shift.id = '';
      shift.startTimeRef = null;
      shift.endTimeRef = null;
      shift.restTime = 0;
      shift.restDaysArray = [];
      if ('noonEndTimeRef' in shift) shift.noonEndTimeRef = null;
      if ('afterStartTimeRef' in shift) shift.afterStartTimeRef = null;
    });
  };

  const processData = (list: any[]) => {
    const typeMap: Record<string, ShiftData> = { '1': shifts.early, '3': shifts.night };
    list.forEach((item) => {
      const target = typeMap[item.shiftType];
      if (!target) return;

      target.id = item.id;
      target.startTimeRef = toDayjs(item.startTime);
      target.endTimeRef = toDayjs(item.endTime);
      target.restTime = item.restTime || 0;
      target.restDaysArray = item.restDays ? item.restDays.split(',') : [];

      if (target === shifts.early) {
        target.noonEndTimeRef = toDayjs(item.noonEndTime);
        target.afterStartTimeRef = toDayjs(item.afterStartTime);
      }
    });
  };

  const loadData = async (forceReload = false) => {
    try {
      loading.value = true;
      const cached = scheduleCache.get();
      if (!forceReload && cached) {
        processData(cached.list);
        return;
      }
      const res = await getScheduleTimeList({});
      scheduleCache.set(res);
      processData(res?.list || []);
    } catch (error) {
      createMessage.error('加载排班时间数据失败');
    } finally {
      loading.value = false;
    }
  };

  const [registerForm, { resetFields }] = useForm({
    labelWidth: 60,
    labelCol: { span: 3 },
    wrapperCol: { span: 21 },
    baseColProps: { span: 24 },
    schemas: formSchema,
    showActionButtonGroup: false,
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner((data) => {
    resetFields();
    resetShifts();
    setModalProps({ confirmLoading: false });
    setTimeout(() => loadData(data?.reload), 100);
  });

  const handleSubmit = async () => {
    try {
      setModalProps({ confirmLoading: true });
      const payload: any[] = [];

      const buildPayload = (type: string, data: ShiftData, remarks: string) => {
        if (!data.startTimeRef || !data.endTimeRef) return;

        const shiftData: any = {
          id: data.id,
          shiftType: type,
          startTime: toTimeStr(data.startTimeRef),
          endTime: toTimeStr(data.endTimeRef),
          restTime: data.restTime,
          restDays: data.restDaysArray.join(','),
          remarks,
        };

        if (data.noonEndTimeRef) shiftData.noonEndTime = toTimeStr(data.noonEndTimeRef);
        if (data.afterStartTimeRef) shiftData.afterStartTime = toTimeStr(data.afterStartTimeRef);

        payload.push(shiftData);
      };

      buildPayload('1', shifts.early, '早班时间');
      buildPayload('3', shifts.night, '晚班时间');

      if (payload.length > 0) {
        await saveAllScheduleTime(payload);
      }

      scheduleCache.clear();
      createMessage.success('保存成功！');
      closeModal();
      emit('success');
    } catch (error) {
      console.error('保存排班时间失败', error);
      createMessage.error('保存失败！');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  };
</script>

<style lang="less" scoped>
  .loading-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 200px;
  }

  /* --- 卡片式分组容器 --- */
  .shift-container {
    background-color: #fafbfc; /* 极浅的蓝灰色背景，区分早晚班 */
    border: 1px solid #f0f0f0;
    border-radius: 6px;
    padding: 16px;
    width: 100%;
  }

  /* --- 每一行的布局 --- */
  .shift-row {
    display: flex;
    align-items: center;
    width: 100%;
    gap: 32px; /* 左右两大块之间的间距 */
  }

  /* 早班的第二行（下午）增加顶部间距 */
  .shift-row + .shift-row {
    margin-top: 16px;
  }

  /* --- 左侧时间区域 (固定宽度，保证上下行绝对对齐) --- */
  .time-section {
    display: flex;
    align-items: center;
    width: 320px; /* 固定宽度：前缀标签 + 两个时间框 + 分隔符 */
    flex-shrink: 0;
  }

  /* 行内前缀标签 (上午/下午/晚班) */
  .sub-label {
    width: 40px;
    font-size: 14px;
    font-weight: 500;
    color: #333;
    flex-shrink: 0;
  }

  /* --- 右侧配置区域 (占据剩余空间) --- */
  .config-section {
    display: flex;
    align-items: center;
    gap: 24px; /* 休息时长和休息日之间的间距 */
    flex: 1;
  }

  .config-item {
    display: flex;
    align-items: center;
    gap: 8px; /* 标签和输入框的间距 */
  }

  .block-label {
    color: #606266;
    white-space: nowrap;
    font-size: 14px;
  }

  /* 让休息日下拉框撑满剩余空间 */
  .flex-fill {
    flex: 1;
    min-width: 200px;
  }

  /* --- 控件具体尺寸 --- */
  .time-picker {
    width: 110px !important;
  }

  .rest-input {
    width: 80px !important;
  }

  .days-select {
    width: 100% !important;
  }

  .separator {
    color: #999;
    font-size: 13px;
    margin: 0 10px;
    text-align: center;
  }

  /* --- 覆盖 Vben 表单默认样式，防止错位 --- */
  .schedule-form {
    :deep(.ant-form-item) {
      margin-bottom: 20px;
    }
    :deep(.ant-form-item-label) {
      width: 60px !important;
      flex: 0 0 60px !important;
    }
    :deep(.ant-form-item-control-input-content) {
      display: block !important; /* 解除默认的 flex，由我们的 shift-container 接管排版 */
    }
  }
</style>
