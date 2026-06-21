<!--
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author Shawn
 * @date 2025-05-22
-->
<template>
  <div class="upload-container">
    <a-upload-dragger
      name="file"
      :multiple="true"
      v-model:file-list="fileListRef"
      action=""
      :before-upload="beforeUpload"
      @change="handleChange"
      :remove="handleRemove"
      @preview="handlePreview"
      accept=".jpg,.jpeg,.png,.gif,.doc,.docx,.xls,.xlsx,.pdf,.zip,.rar,.7z"
      :disabled="loading"
      list-type="picture-card"
    >
      <template v-if="loading">
        <a-spin tip="正在加载附件列表...">
          <LoadingOutlined style="font-size: 24px" spin />
        </a-spin>
      </template>
      <template v-else-if="fileListRef.length < maxFiles">
        <div class="upload-area">
          <CloudUploadOutlined class="upload-icon" />
          <div class="upload-text">{{ t('点击或拖拽文件上传') }}</div>
          <div class="upload-hint">最大支持{{ maxSize }}MB文件</div>
        </div>
      </template>
    </a-upload-dragger>
  </div>
</template>

<script lang="ts" setup>
  import { ref, defineProps, defineEmits, watch } from 'vue';
  import { Upload, Spin } from 'ant-design-vue';
  import { useI18n } from '@/hooks/swm/useI18n';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { CloudUploadOutlined, LoadingOutlined } from '@ant-design/icons-vue';
  import { createImgPreview } from '@/components/swm/Preview';

  const AUploadDragger = Upload.Dragger;

  const props = defineProps({
    modelValue: {
      type: Array,
      default: () => [],
    },
    recordId: {
      type: String,
      default: '',
    },
    loading: {
      type: Boolean,
      default: false,
    },
    maxFiles: {
      type: Number,
      default: 5,
    },
    maxSize: {
      type: Number,
      default: 20,
    },
    uploadUrl: {
      type: String,
      default: '/js/swm/safetyEducation/upload',
    },
  });

  const emit = defineEmits(['update:modelValue', 'change', 'upload-success', 'upload-error']);

  const { t } = useI18n('swm.safetyEducation');
  const { showMessage } = useMessage();

  // 文件列表
  const fileListRef = ref<any[]>([]);
  const uploadedFiles = ref<any[]>([]);

  // 监听外部值变化
  watch(
    () => props.modelValue,
    (newVal) => {
      if (newVal && Array.isArray(newVal)) {
        fileListRef.value = [...newVal];
        uploadedFiles.value = [...newVal];
      }
    },
    { immediate: true, deep: true },
  );

  // 处理文件列表变化
  const handleChange = (info: any) => {
    fileListRef.value = [...info.fileList];
    emit('update:modelValue', fileListRef.value);
    emit('change', fileListRef.value);
  };

  // 文件上传前处理
  const beforeUpload = (file: File) => {
    // 检查文件大小
    if (file.size > props.maxSize * 1024 * 1024) {
      showMessage({
        content: t(`文件大小不能超过${props.maxSize}MB`),
        type: 'error',
        duration: 3,
      });
      return false;
    }

    // 如果已经达到文件数量限制
    if (fileListRef.value.length >= props.maxFiles) {
      showMessage({
        content: t(`最多只能上传${props.maxFiles}个文件`),
        type: 'error',
        duration: 3,
      });
      return false;
    }

    // 自动上传文件
    uploadSingleFile(file);

    // 返回false阻止默认上传行为
    return false;
  };

  // 上传单个文件
  const uploadSingleFile = async (file: File) => {
    try {
      const formData = new FormData();
      formData.append('file', file);

      if (props.recordId) {
        formData.append('recordId', props.recordId);
      }

      // 使用fetch API上传
      const response = await fetch(props.uploadUrl, {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) {
        throw new Error(`上传失败，HTTP错误: ${response.status}`);
      }

      const result = await response.json();

      if (result && (result.result === 'success' || result.code === 0)) {
        // 找到对应的文件并更新状态
        const index = fileListRef.value.findIndex(
          (item) =>
            item.originFileObj &&
            item.originFileObj.name === file.name &&
            item.originFileObj.size === file.size,
        );

        if (index !== -1) {
          // 保存原始的thumbUrl，避免在后续更新中丢失
          const originalThumbUrl = fileListRef.value[index].thumbUrl;

          fileListRef.value[index].status = 'done';
          fileListRef.value[index].response = result;
          fileListRef.value[index].url = result.url || '';

          // 保留和优化thumbUrl处理
          if (file.type.startsWith('image/')) {
            // 如果服务器返回了thumbUrl就使用它
            if (result.thumbUrl) {
              fileListRef.value[index].thumbUrl = result.thumbUrl;
            }
            // 如果没有原始的thumbUrl，则创建一个
            else if (!originalThumbUrl) {
              try {
                const reader = new FileReader();
                reader.onload = (e) => {
                  if (fileListRef.value[index]) {
                    fileListRef.value[index].thumbUrl = e.target?.result as string;
                  }
                };
                reader.readAsDataURL(file);
              } catch (e) {
                console.error('创建预览图失败:', e);
              }
            }
            // 保留原有的thumbUrl
            else {
              fileListRef.value[index].thumbUrl = originalThumbUrl;
            }
          }

          // 添加到已上传文件列表
          const newUploadedFile = {
            fileId: result.fileId || result.id || '',
            id: result.fileId || result.id || '',
            fileName: file.name,
            url: result.url || '',
            thumbUrl: '',
          };

          // 如果是图片，处理缩略图URL
          if (file.type.startsWith('image/')) {
            if (result.thumbUrl) {
              newUploadedFile.thumbUrl = result.thumbUrl;
            } else if (fileListRef.value[index]?.thumbUrl) {
              newUploadedFile.thumbUrl = fileListRef.value[index].thumbUrl;
            } else if (result.url) {
              newUploadedFile.thumbUrl = result.url;
            }
          }

          uploadedFiles.value.push(newUploadedFile);

          // 更新父组件
          emit('update:modelValue', fileListRef.value);
          emit('change', fileListRef.value);
          emit('upload-success', {
            file: newUploadedFile,
            response: result,
          });
        }

        showMessage({
          content: t('上传成功'),
          type: 'success',
          duration: 2,
        });
      } else {
        throw new Error(result?.message || '上传失败');
      }
    } catch (error: any) {
      console.error('文件上传失败:', error);

      // 找到对应的文件并更新状态为错误
      const index = fileListRef.value.findIndex(
        (item) => item.originFileObj && item.originFileObj.name === file.name,
      );

      if (index !== -1) {
        fileListRef.value[index].status = 'error';
        fileListRef.value[index].error = error.message || '上传失败';
      }

      emit('upload-error', {
        file,
        error: error.message || '上传失败',
      });

      showMessage({
        content: error.message || t('上传失败，请重试'),
        type: 'error',
        duration: 3,
      });
    }
  };

  // 处理文件移除
  const handleRemove = (file: any) => {
    // 移除文件时更新uploadedFiles列表
    if (file && file.response) {
      const fileId = file.response.fileId || file.response.id;
      if (fileId) {
        uploadedFiles.value = uploadedFiles.value.filter((item) => item.fileId !== fileId);
      }
    }

    // 更新父组件
    emit('update:modelValue', fileListRef.value);
    emit('change', fileListRef.value);

    // 返回true表示允许移除
    return true;
  };

  // 处理文件预览
  const handlePreview = async (file: any) => {
    let previewUrl = '';
    let previewTitle = file.name || file.url?.substring(file.url.lastIndexOf('/') + 1);

    // 判断文件是否为图片类型
    const isImage =
      file.type?.startsWith('image/') ||
      file.originFileObj?.type?.startsWith('image/') ||
      file.name?.match(/\.(jpeg|jpg|gif|png)$/i) ||
      file.url?.match(/\.(jpeg|jpg|gif|png)$/i);

    if (file.url) {
      previewUrl = file.url;
    } else if (file.response && file.response.url) {
      previewUrl = file.response.url;
    } else if (file.thumbUrl && file.thumbUrl.startsWith('data:')) {
      // 使用thumbUrl作为预览，如果它是base64格式
      previewUrl = file.thumbUrl;
    } else if (file.originFileObj) {
      // 对于正在上传或上传失败的图片，尝试生成本地预览
      if (file.originFileObj.type.startsWith('image/')) {
        previewUrl = await getBase64(file.originFileObj);
      }
    }

    if (previewUrl) {
      if (isImage) {
        // 使用图片预览组件预览图片
        createImgPreview({
          imageList: [previewUrl],
          defaultWidth: 800,
        });
      } else {
        // 非图片文件仍然使用新标签页打开
        window.open(previewUrl, '_blank');
      }
    } else {
      showMessage({
        content: t('无法预览该文件'),
        type: 'warning',
      });
    }
  };

  // 转换文件为Base64
  function getBase64(file: File | Blob): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => resolve(reader.result as string);
      reader.onerror = (error) => reject(error);
    });
  }

  // 暴露方法给父组件
  defineExpose({
    getUploadedFiles: () => uploadedFiles.value,
    clearFiles: () => {
      fileListRef.value = [];
      uploadedFiles.value = [];
      emit('update:modelValue', []);
      emit('change', []);
    },
  });
</script>

<style lang="less" scoped>
  .upload-container {
    margin-bottom: 16px;
    width: 100%;
  }

  :deep(.ant-upload-drag) {
    background-color: #fafafa;
    border: 2px dashed #d9d9d9;
    border-radius: 8px;
    transition: all 0.3s;

    &:hover {
      border-color: #1890ff;
      background-color: #f0f7ff;
    }

    &:not(.ant-upload-disabled):hover {
      border-color: #1890ff;
    }
  }

  .upload-area {
    padding: 20px 0;

    .upload-icon {
      font-size: 36px;
      color: #1890ff;
      margin-bottom: 8px;
    }

    .upload-text {
      font-size: 16px;
      color: #333;
      margin-bottom: 8px;
      font-weight: 500;
    }

    .upload-hint {
      font-size: 12px;
      color: #888;
    }
  }

  :deep(.ant-upload-select-picture-card) {
    border-radius: 8px;
    overflow: hidden;
    border: 2px dashed #d9d9d9;
    background-color: #fafafa;

    &:hover {
      border-color: #1890ff;
      background-color: #f0f7ff;
    }
  }

  :deep(.ant-upload-list) {
    max-height: 300px;
    overflow-y: auto;
    overflow-x: hidden;
    border-radius: 8px;
    padding: 4px;

    &.ant-upload-list-picture-card {
      display: flex;
      flex-wrap: wrap;

      .ant-upload-list-item-container {
        margin-right: 10px;
        margin-bottom: 10px;
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        transition: all 0.3s;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
        }
      }

      .ant-upload-list-item {
        border-radius: 6px;
        border: 1px solid #f0f0f0;
      }
    }
  }
</style>
