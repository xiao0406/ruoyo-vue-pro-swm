<template>
  <a-drawer
    :open="open"
    :title="title"
    :width="width"
    :placement="placement"
    @close="$emit('close')"
  >
    <slot />
    <template #footer v-if="$slots.footer">
      <slot name="footer" />
    </template>
  </a-drawer>
</template>

<script lang="ts" setup>
  import { Drawer as ADrawer } from 'ant-design-vue';

  defineProps<{
    open?: boolean;
    title?: string;
    width?: number | string;
    placement?: 'top' | 'right' | 'bottom' | 'left';
  }>();

  defineEmits(['close']);
</script>
