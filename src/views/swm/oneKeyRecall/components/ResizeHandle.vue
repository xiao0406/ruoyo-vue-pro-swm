<!--
  @description 可调整的分隔条组件
  @date 2024-06-10
-->
<template>
  <div class="resize-handle" @mousedown="startResize" @dblclick="resetWidth"></div>
</template>

<script lang="ts" setup>
  import { defineEmits } from 'vue';

  const emit = defineEmits(['resize-start', 'reset-width']);

  // 开始调整宽度
  const startResize = (e: MouseEvent) => {
    emit('resize-start', e);
  };

  // 重置宽度
  const resetWidth = () => {
    emit('reset-width');
  };
</script>

<style lang="less" scoped>
  .resize-handle {
    width: 4px;
    height: 100%;
    background-color: #f0f2f5;
    cursor: col-resize;
    transition: all 0.3s;
    z-index: 10;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 2px;
      height: 24px;
      background-color: #d9d9d9;
      border-radius: 1px;
      transition: all 0.3s;
    }

    &:hover {
      background-color: #e6f7ff;

      &::after {
        background-color: #1890ff;
        height: 32px;
      }
    }

    &:active {
      background-color: #bae7ff;

      &::after {
        background-color: #096dd9;
      }
    }
  }
</style>
