<template>
  <span>{{ displayValue }}</span>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';

  const props = defineProps<{
    dictType?: string;
    dictValue?: string;
    defaultValue?: string;
  }>();

  const displayValue = computed(() => {
    return props.dictValue || props.defaultValue || '';
  });
</script>
