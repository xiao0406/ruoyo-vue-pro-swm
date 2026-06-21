<template>
  <div class="description-container">
    <a-descriptions :column="column" :bordered="bordered" :title="title">
      <a-descriptions-item
        v-for="item in schema"
        :key="item.field"
        :label="item.label"
        :span="item.span"
      >
        <slot :name="item.field" :data="data">
          {{ data?.[item.field] ?? item.defaultValue ?? '--' }}
        </slot>
      </a-descriptions-item>
    </a-descriptions>
  </div>
</template>

<script lang="ts" setup>
  import { Descriptions as ADescriptions, DescriptionsItem as ADescriptionsItem } from 'ant-design-vue';

  defineProps<{
    schema: Array<{
      field: string;
      label: string;
      span?: number;
      defaultValue?: string;
    }>;
    data?: Record<string, any>;
    column?: number;
    bordered?: boolean;
    title?: string;
  }>();
</script>
