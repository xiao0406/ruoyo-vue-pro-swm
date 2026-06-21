<template>
  <div class="table-action" :style="{ justifyContent: props.align || 'center' }">
    <template v-for="(action, index) in visibleActions" :key="index">
      <a-divider v-if="action.divider && index > 0" type="vertical" />
      <a-popconfirm v-if="action.popConfirm" :title="action.popConfirm.title" @confirm="action.popConfirm.confirm" @cancel="action.popConfirm.cancel">
        <a-button :type="action.color === 'error' ? 'primary' : 'link'" :danger="action.color === 'error'" :disabled="action.disabled" size="small">{{ action.label || action.tooltip }}</a-button>
      </a-popconfirm>
      <a-button v-else :type="action.color === 'primary' ? 'primary' : 'link'" :danger="action.color === 'error'" :disabled="action.disabled" size="small" @click="action.onClick">{{ action.label || action.tooltip }}</a-button>
    </template>
  </div>
</template>
<script lang="ts" setup>
import { computed } from 'vue';
import { Button as AButton, Divider as ADivider, Popconfirm as APopconfirm } from 'ant-design-vue';
import type { ActionItem } from '../types/tableAction';
const props = defineProps<{ actions?: ActionItem[]; dropDownActions?: ActionItem[]; divider?: boolean; align?: string; }>();
const visibleActions = computed(() => (props.actions || []).filter(a => { if (a.ifShow === false) return false; if (typeof a.ifShow === 'function') return a.ifShow(a); return true; }));
</script>
<style scoped>.table-action { display: flex; align-items: center; gap: 4px; }</style>
