<template>
  <a-button v-bind="$attrs" @click="$emit('click', $event)">
    <slot />
  </a-button>
</template>

<script lang="ts" setup>
  import { Button as AButton } from 'ant-design-vue';
  defineEmits(['click']);
</script>
