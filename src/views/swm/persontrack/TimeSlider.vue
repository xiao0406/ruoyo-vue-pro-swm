<template>
  <div class="time-slider-container">
    <!-- 拖动条 -->
    <div class="slider-track" ref="trackRef">
      <div class="slider-range" :style="rangeStyle"></div>
      <div
        class="slider-handle start-handle"
        :style="{ left: startPosition + '%' }"
        @mousedown="startDrag('start')"
        @touchstart.passive="startDrag('start')"
      ></div>
      <div
        class="slider-handle end-handle"
        :style="{ left: endPosition + '%' }"
        @mousedown="startDrag('end')"
        @touchstart.passive="startDrag('end')"
      ></div>
    </div>
    <div class="selected-range"
      >{{ formatTime(modelValue[0]) }} - {{ formatTime(modelValue[1]) }}</div
    >
  </div>
</template>

<script setup>
  import { ref, computed, onUnmounted } from 'vue';

  const props = defineProps({
    modelValue: {
      // v-model 绑定的值 [startTime, endTime]
      type: Array,
      required: true,
      validator: (value) => value.length === 2,
    },
    min: {
      // 最小时间（秒）
      type: Number,
      default: 0,
    },
    max: {
      // 最大时间（秒）
      type: Number,
      default: 86400, // 24小时
    },
    step: {
      // 步长（秒）
      type: Number,
      default: 60, // 1分钟
    },
    format: {
      // 时间格式化函数
      type: Function,
      default: (seconds) => {
        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        const secs = Math.floor(seconds % 60);
        return `${hours}:${minutes.toString().padStart(2, '0')}:${secs
          .toString()
          .padStart(2, '0')}`;
      },
    },
  });

  const emit = defineEmits(['update:modelValue']);

  const trackRef = ref(null);
  const activeHandle = ref(null); // 'start' 或 'end'
  const isDragging = ref(false);

  // 计算位置百分比
  const startPosition = computed(() => {
    return ((props.modelValue[0] - props.min) / (props.max - props.min)) * 100;
  });

  const endPosition = computed(() => {
    return ((props.modelValue[1] - props.min) / (props.max - props.min)) * 100;
  });

  // 计算范围样式
  const rangeStyle = computed(() => {
    return {
      left: startPosition.value + '%',
      width: endPosition.value - startPosition.value + '%',
    };
  });
  // 开始拖拽
  const startDrag = (handle) => {
    activeHandle.value = handle;
    isDragging.value = true;
    document.addEventListener('mousemove', handleDrag);
    document.addEventListener('mouseup', stopDrag);
    document.addEventListener('touchmove', handleDrag, { passive: false });
    document.addEventListener('touchend', stopDrag);
  };

  // 处理拖拽
  const handleDrag = (e) => {
    if (!isDragging.value) return;
    const trackRect = trackRef.value.getBoundingClientRect();
    const clientX = e.clientX ?? e.touches?.[0]?.clientX;
    if (!clientX) return;
    // 计算点击位置占总长度的百分比
    let percentage = (clientX - trackRect.left) / trackRect.width;
    percentage = Math.max(0, Math.min(1, percentage)); // 限制在0-1之间
    // 计算对应的时间值（按步长对齐）
    let timeValue = props.min + percentage * (props.max - props.min);
    timeValue = Math.round(timeValue / props.step) * props.step;
    // 更新对应的值
    const newValue = [...props.modelValue];
    if (activeHandle.value === 'start') {
      newValue[0] = Math.min(timeValue, props.modelValue[1]);
    } else {
      newValue[1] = Math.max(timeValue, props.modelValue[0]);
    }
    emit('update:modelValue', newValue);
    if (e.cancelable) e.preventDefault();
  };

  // 停止拖拽
  const stopDrag = () => {
    isDragging.value = false;
    activeHandle.value = null;
    removeEventListeners();
  };

  // 清理事件监听
  const removeEventListeners = () => {
    document.removeEventListener('mousemove', handleDrag);
    document.removeEventListener('mouseup', stopDrag);
    document.removeEventListener('touchmove', handleDrag);
    document.removeEventListener('touchend', stopDrag);
  };
  const formatTime = (seconds) => {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
  };
  // 组件卸载时清理
  onUnmounted(() => {
    removeEventListeners();
  });
</script>

<style lang="less" scoped>
  .time-slider-container {
    padding: 0 20px;
    width: 100%;
    height: 120px;
    display: flex;
    align-items: center;
    flex-direction: column;
    user-select: none;
    background: #f0f0f0;
    padding-top: 40px;
  }

  .slider-track {
    position: relative;
    width: 100%;
    height: 30px;
    background-color: #ccc;
    cursor: pointer;
    padding: 0 10px;
  }

  .slider-range {
    position: absolute;
    height: 100%;
    background-color: #1890ff;
  }

  .slider-handle {
    position: absolute;
    width: 16px;
    height: 16px;
    border-radius: 50%;
    top: 50%;
    transform: translate(-50%, -50%);
    cursor: grab;
    z-index: 1;
  }

  .slider-handle:active {
    cursor: grabbing;
    transform: translate(-50%, -50%) scale(1.2);
  }

  .start-handle {
    z-index: 2; /* 确保开始手柄在上层 */
  }
  .selected-range {
    display: flex;
    justify-content: center;
    margin-top: 10px;
  }
</style>
