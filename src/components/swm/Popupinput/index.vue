<template>
  <div class="popup-input">
    <a-input
      :value="modelValue"
      :placeholder="placeholder"
      :disabled="disabled"
      @update:value="$emit('update:modelValue', $event)"
    >
      <template #suffix>
        <a-button size="small" type="link" @click="$emit('open')">
          <template #icon><ellipsis-outlined /></template>
        </a-button>
      </template>
    </a-input>
  </div>
</template>

<script lang="ts" setup>
  import { Input as AInput, Button as AButton } from 'ant-design-vue';
  import { EllipsisOutlined } from '@ant-design/icons-vue';

  defineProps<{
    modelValue?: string;
    placeholder?: string;
    disabled?: boolean;
  }>();

  defineEmits(['update:modelValue', 'open']);
</script>
