<!--
  @description 语音模板选择组件
  @date 2024-06-10
-->
<template>
  <div class="template-selector">
    <div class="section-label">请选择语音模板</div>
    <a-select
      :value="modelValue"
      :options="voiceTemplateOptions"
      style="width: 100%"
      @change="handleValueChange"
      :loading="loading"
    />
    <!-- 推送方式 -->
    <div class="push-method-section">
      <div class="section-label">推送方式</div>
      <a-checkbox-group
        v-model:value="pushMethods"
        :options="pushMethodOptions"
        @change="handlePushMethodChange"
      />
    </div>

    <!-- 推送频次 -->
    <div class="push-frequency-section">
      <div class="section-label">推送频次</div>
      <a-radio-group
        v-model:value="pushFrequency"
        :options="pushFrequencyOptions"
        @change="handlePushFrequencyChange"
      />
    </div>
    <div class="push-frequency-content" v-if="pushFrequency === '2'">
      频率(秒)：<a-input-number
        id="inputNumber1"
        v-model:value="pushInfo.frequency"
        :min="0"
        :step="1"
        :precision="0"
        :max="1000000"
      />
      次数(次)：<a-input-number
        id="inputNumber2"
        v-model:value="pushInfo.count"
        :min="1"
        :step="1"
        :precision="0"
        :max="10000"
      />
    </div>
    <!-- 语音文字 -->
    <div class="template-content-section">
      <div class="section-label">语音文字</div>
      <a-textarea
        :value="displayVoiceText"
        :rows="4"
        :disabled="modelValue === '应急召回'"
        :placeholder="templatePlaceholder"
        @update:value="handleVoiceTextChange"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref, defineProps, defineEmits, inject, onMounted } from 'vue';
  import { Select, Input, Checkbox, Radio, InputNumber } from 'ant-design-vue';
  import { getActiveVoiceTemplates, VoiceTemplateItem } from '@/api/swm/voiceTemplate';
  import { useMessage } from '@/hooks/swm/useMessage';
  import {
    CUSTOM_TEMPLATE_CODE,
    DEFAULT_CUSTOM_CONTENT,
    tempVoiceOptions,
  } from '../oneKeyRecall.data';

  const ASelect = Select;
  const AInputNumber = InputNumber;
  const ATextarea = Input.TextArea;
  const ACheckboxGroup = Checkbox.Group;
  const ARadioGroup = Radio.Group;

  // 引入消息提示
  const { createMessage } = useMessage();

  const props = defineProps({
    modelValue: {
      type: String,
      required: true,
    },
    templateContent: {
      type: String,
      default: '',
    },
    pushMethod: {
      type: Array as () => string[],
      default: () => ['1'],
    },
    pushFrequency: {
      type: String,
      default: '1',
    },
  });

  const emit = defineEmits([
    'update:modelValue',
    'update:templateContent',
    'update:voiceText',
    'update:pushMethod',
    'update:pushFrequency',
    'template-change',
  ]);

  // 语音模板相关
  const voiceTemplateOptions = ref<{ label: string; value: string }[]>([]);
  const templateContentProp = ref<string>(props.templateContent); // 实际发送的内容（content字段）
  const displayVoiceText = ref<string>(''); // 界面显示的语音文字（voiceText字段）
  const templatePlaceholder = ref<string>('请输入语音文字内容');
  const loading = ref<boolean>(false);
  const templateList = ref<VoiceTemplateItem[]>([]);
  const pushMethods = ref<string[]>(props.pushMethod);
  const pushFrequency = ref<string>(props.pushFrequency);

  // 推送方式选项
  const pushMethodOptions = [
    { label: '定位模块推送', value: '1' },
    { label: '短信推送', value: '2' },
  ];

  // 推送频次选项
  const pushFrequencyOptions = [
    { label: '仅一次', value: '1' },
    { label: '循环推送', value: '2' },
  ];
  const pushInfo = inject('pushInfo') as any;
  // 当前选中的模板
  const handleTempSelect = (val) => {
    if (!val) return '';
    const item = tempVoiceOptions.find((item) => item.label === val) || null;
    return item?.voiceText || '';
  };

  // 处理下拉选择值变更
  const handleValueChange = (value: string) => {
    const vText = handleTempSelect(value);
    displayVoiceText.value = vText;
    emit('update:voiceText', vText);
    emit('update:modelValue', value);
  };

  // 处理语音文字变更（界面显示的内容）
  const handleVoiceTextChange = (value: string) => {
    displayVoiceText.value = value;
    // 如果是自定义模板，同时更新实际发送的内容
    // if (props.modelValue === CUSTOM_TEMPLATE_CODE) {
    // }
    templateContentProp.value = value;
    emit('update:templateContent', '');
    emit('update:voiceText', value);
  };

  // 处理推送方式变更
  const handlePushMethodChange = (value: string[]) => {
    pushMethods.value = value;
    emit('update:pushMethod', value.join(','));
  };

  // 处理推送频次变更
  const handlePushFrequencyChange = (e: any) => {
    const val = e.target.value;
    pushFrequency.value = val;
    if (val === '1') {
      pushInfo.frequency = 0;
      pushInfo.count = 1;
    } else if (val === '2') {
      pushInfo.frequency = 1;
      pushInfo.count = 1;
    }
    emit('update:pushFrequency', e.target.value);
  };

  // 获取语音模板数据
  const fetchVoiceTemplates = async () => {
    loading.value = true;
    // templateList.value = tempVoiceOptions.map(({ label, value }) => ({ label, value }));
    voiceTemplateOptions.value = tempVoiceOptions.map(({ label, value }) => ({ label, value }));
    emit('update:modelValue', tempVoiceOptions[0].value);
    const voiceText = tempVoiceOptions[0].voiceText;
    displayVoiceText.value = voiceText;
    emit('update:voiceText', voiceText || '');
    emit('update:templateContent', '');
    emit('update:pushMethod', '1');
    loading.value = false;
    return;

  };

  // 页面加载时获取数据
  onMounted(fetchVoiceTemplates);

  defineExpose({
    fetchVoiceTemplates,
  });
</script>

<style lang="less" scoped>
  .template-selector {
    display: flex;
    flex-direction: column;
    gap: 24px;
    width: 100%;
  }

  .section-label {
    font-weight: 600;
    margin-bottom: 12px;
    color: #1f2937;
    font-size: 15px;
    display: flex;
    align-items: center;

    &::before {
      content: '';
      display: inline-block;
      width: 4px;
      height: 16px;
      background-color: #1890ff;
      margin-right: 8px;
      border-radius: 2px;
    }
  }
  .push-frequency-content {
    max-width: 500px;
    min-width: 360px;
    display: flex;
    align-items: center;
    gap: 12px;
  }
  .push-method-section,
  .push-frequency-section,
  .template-content-section {
    margin-top: 16px;
  }

  :deep(.ant-select),
  :deep(.ant-input),
  :deep(.ant-radio-group),
  :deep(.ant-checkbox-group) {
    border-radius: 6px;

    &:hover,
    &:focus {
      border-color: #1890ff;
    }
  }

  :deep(.ant-input) {
    resize: none;
    transition: all 0.3s;

    &:hover {
      box-shadow: 0 0 0 1px rgba(24, 144, 255, 0.1);
    }

    &:focus {
      box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
    }
  }
</style>
