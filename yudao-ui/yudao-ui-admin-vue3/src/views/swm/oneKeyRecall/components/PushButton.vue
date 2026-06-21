<!--
  @description 推送按钮组件
  @date 2024-06-10
-->
<template>
  <div class="push-button-section">
    <a-button type="primary" @click="handleClick" :loading="loading"> 推送 </a-button>
  </div>
</template>

<script lang="ts" setup>
  import { defineProps, defineEmits } from 'vue';
  import { Button } from 'ant-design-vue';

  const AButton = Button;

  const props = defineProps({
    loading: {
      type: Boolean,
      default: false,
    },
  });

  const emit = defineEmits(['click']);

  const handleClick = () => {
    emit('click');
  };
</script>

<style lang="less" scoped>
  .push-button-section {
    // margin-top: auto;
    position: fixed;
    width: 83%;
    bottom: 24px; // 距离底部的距离
    left: 24px;
    padding-top: 24px;
    text-align: center;
    :deep(.ant-btn) {
      min-width: 120px;
      height: 40px;
      border-radius: 20px;
      font-size: 15px;
      font-weight: 500;
      box-shadow: 0 2px 8px rgba(24, 144, 255, 0.25);

      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(24, 144, 255, 0.35);
      }

      &:active {
        transform: translateY(0);
      }
    }
  }
</style>
