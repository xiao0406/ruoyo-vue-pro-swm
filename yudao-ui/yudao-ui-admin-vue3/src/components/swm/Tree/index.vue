<template>
  <a-tree v-bind="$attrs">
    <template v-for="(_, name) in $slots" #[name]="slotData" :key="name">
      <slot v-bind="slotData || {}" :name="name" />
    </template>
  </a-tree>
</template>

<script lang="ts" setup>
  import { Tree as ATree } from 'ant-design-vue';
</script>
