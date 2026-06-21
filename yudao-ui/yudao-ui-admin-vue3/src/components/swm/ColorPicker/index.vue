<template>
  <div class="color-picker-wrapper">
    <a-input
      :value="modelValue"
      @update:value="$emit('update:modelValue', $event)"
      :placeholder="placeholder"
    >
      <template #prefix>
        <div
          :style="{
            width: '20px',
            height: '20px',
            borderRadius: '2px',
            backgroundColor: modelValue,
            border: '1px solid #d9d9d9',
          }"
        />
      </template>
    </a-input>
  </div>
</template>

<script lang="ts" setup>
  import { Input as AInput } from 'ant-design-vue';

  defineProps<{
    modelValue?: string;
    placeholder?: string;
  }>();

  defineEmits(['update:modelValue']);
</script>
