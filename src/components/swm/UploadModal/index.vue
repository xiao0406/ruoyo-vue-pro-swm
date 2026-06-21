<template>
  <div>
    <a-upload
      :file-list="fileList"
      :before-upload="beforeUpload"
      :custom-request="customRequest"
      :accept="accept"
      :multiple="multiple"
      @change="handleChange"
    >
      <a-button type="primary">
        <upload-outlined />
        {{ buttonText || '上传文件' }}
      </a-button>
    </a-upload>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { Upload as AUpload, Button as AButton } from 'ant-design-vue';
  import { UploadOutlined } from '@ant-design/icons-vue';

  const props = defineProps<{
    accept?: string;
    multiple?: boolean;
    buttonText?: string;
    action?: string;
  }>();

  const emit = defineEmits(['upload', 'change']);
  const fileList = ref<any[]>([]);

  function beforeUpload(file: File) {
    emit('upload', file);
    return false;
  }

  function customRequest() {
    // no-op, handled by beforeUpload
  }

  function handleChange(info: any) {
    fileList.value = info.fileList;
    emit('change', info);
  }
</script>
