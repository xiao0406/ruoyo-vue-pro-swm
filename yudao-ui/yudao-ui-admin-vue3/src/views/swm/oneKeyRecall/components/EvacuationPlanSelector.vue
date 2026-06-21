<!--
  @description 撤离方案选择组件
  @date 2024-06-10
-->
<template>
  <div class="evacuation-plan-selector">
    <div class="section-label">撤离方案</div>
    <div class="evacuation-radio-group">
      <a-radio-group :value="modelValue" @change="handleValueChange">
        <a-radio v-for="item in evacuationPlanList" :key="item.value" :value="item.value">
          {{ item.name }}
        </a-radio>
      </a-radio-group>
    </div>
    <div class="evacuation-description">
      <span v-if="showWarning" class="evacuation-notice"> 请在右侧选择撤离对象 </span>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { defineProps, defineEmits, computed } from 'vue';
  import { Radio } from 'ant-design-vue';
  import { EvacuationPlanEnum, getEvacuationPlanList } from '../oneKeyRecall.data';

  const ARadio = Radio;
  const ARadioGroup = Radio.Group;

  const props = defineProps({
    modelValue: {
      type: String,
      required: true,
    },
    selectedTargets: {
      type: Array as () => string[],
      default: () => [],
    },
  });

  const emit = defineEmits(['update:modelValue', 'plan-change']);

  // 撤离方案相关
  const evacuationPlanList = getEvacuationPlanList();

  // 是否显示警告信息（在非全体撤离且未选择对象时显示）
  const showWarning = computed(() => {
    return props.selectedTargets.length === 0 && props.modelValue !== EvacuationPlanEnum.ALL;
  });

  // 处理值变更
  const handleValueChange = (e) => {
    emit('update:modelValue', e.target.value);
    handleEvacuationPlanChange();
  };

  // 处理撤离方案变更
  const handleEvacuationPlanChange = () => {
    emit('plan-change');
  };
</script>

<style lang="less" scoped>
  .evacuation-plan-selector {
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

  .evacuation-radio-group {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 4px;

    :deep(.ant-radio-wrapper) {
      margin-right: 0;
      margin-bottom: 8px;
      transition: all 0.3s;
      padding: 8px 12px;
      border-radius: 6px;

      &:hover {
        background-color: #f5f7fa;
      }

      &.ant-radio-wrapper-checked {
        background-color: #e6f7ff;
      }
    }
  }

  .evacuation-description {
    margin-top: 12px;
    min-height: 20px;
  }

  .evacuation-notice {
    color: #ff4d4f;
    font-size: 13px;
    display: flex;
    align-items: center;
    gap: 4px;

    &::before {
      content: '⚠';
      font-size: 14px;
    }
  }
</style>
