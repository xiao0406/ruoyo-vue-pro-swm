<template>
  <span :class="['anticon', `anticon-${icon}`]" :style="style">
    <slot></slot>
  </span>
</template>

<script lang="ts" setup>
  const props = defineProps<{
    icon: string;
    color?: string;
    size?: number | string;
    spin?: boolean;
  }>();

  const style = {
    color: props.color,
    fontSize: typeof props.size === 'number' ? `${props.size}px` : props.size,
  };
</script>
