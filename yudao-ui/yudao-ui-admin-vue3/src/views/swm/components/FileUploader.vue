<!--
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author Shawn
 * @date 2025-05-22
-->
<template>
  <div class="upload-container">
    <!-- 调试信息 -->
    <div v-if="uploading" class="upload-debug"> 上传中: {{ uploading }} </div>
    <a-upload-dragger
      name="file"
      :multiple="true"
      v-model:file-list="fileListRef"
      action=""
      :before-upload="beforeUpload"
      @change="handleChange"
      @remove="handleRemove"
      @preview="handlePreview"
      :accept="accept"
      :disabled="loading || readonly"
      list-type="picture-card"
      :customRequest="customRequest"
    >
      <template v-if="loading">
        <a-spin tip="正在加载附件列表...">
          <LoadingOutlined style="font-size: 24px" spin />
        </a-spin>
      </template>
      <template v-else-if="fileListRef.length < maxFiles && !readonly">
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
  import type { UploadProps } from 'ant-design-vue';
  import { useI18n } from '@/hooks/swm/useI18n';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { CloudUploadOutlined, LoadingOutlined } from '@ant-design/icons-vue';
  import { createImgPreview } from '@/components/swm/Preview';

  const AUploadDragger = Upload.Dragger;
  const ASpin = Spin;

  const props = defineProps({
    modelValue: {
      type: Array,
      default: () => [],
    },
    recordId: {
      type: String,
      default: '',
    },
    businessType: {
      type: String,
      default: '',
    },
    loading: {
      type: Boolean,
      default: false,
    },
    readonly: {
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
      default: '/js/swm/fileUpload/upload',
    },
    accept: {
      type: String,
      default: '.jpg,.jpeg,.png,.gif,.doc,.docx,.xls,.xlsx,.pdf,.zip,.rar,.7z',
    },
    listType: {
      type: String,
      default: 'picture-card', // 可选：text, picture, picture-card
    },
  });

  const emit = defineEmits([
    'update:modelValue',
    'change',
    'upload-success',
    'upload-error',
    'uploading-change',
  ]);

  const { t } = useI18n('swm.common');
  const { showMessage } = useMessage();

  // 文件列表
  const fileListRef = ref<any[]>([]);
  const uploadedFiles = ref<any[]>([]);
  // 添加上传中状态
  const uploading = ref<boolean>(false);

  // 监听外部值变化
  watch(
    () => props.modelValue,
    (newVal) => {
      if (newVal && Array.isArray(newVal)) {
        // 深拷贝新值，避免引用问题，同时过滤掉被拒绝的文件
        fileListRef.value = [...newVal].filter((file: any) => file.status !== 'rejected');

        // 更新uploadedFiles，确保唯一性
        const uniqueFiles = new Map();

        // 处理新文件列表，只处理非拒绝状态的文件
        fileListRef.value.forEach((file: any) => {
          if (file.response && file.response.fileId) {
            const fileId = file.response.fileId;
            // 使用Map以fileId为键确保唯一性
            uniqueFiles.set(fileId, {
              fileId: fileId,
              id: fileId,
              fileName: file.name || '未知文件',
              url: file.response.url || file.url || '',
              thumbUrl: file.response.thumbUrl || file.thumbUrl || '',
              previewUrl: file.response.previewUrl || file.previewUrl || '',
            });
          }
        });

        // 转换回数组
        uploadedFiles.value = Array.from(uniqueFiles.values());
      }
    },
    { immediate: true, deep: true },
  );

  // 处理文件列表变化
  const handleChange = (info: any) => {
    console.log('文件列表变化:', info);

    // 获取上传列表，移除被标记为rejected的文件
    const filteredFileList = info.fileList.filter((file: any) => file.status !== 'rejected');

    // 确保文件对象保存previewUrl信息
    filteredFileList.forEach((file) => {
      if (file.response && file.response.previewUrl && !file.previewUrl) {
        file.previewUrl = file.response.previewUrl;
      }
    });

    // 更新文件列表
    fileListRef.value = filteredFileList;

    // 发送更新事件
    emit('update:modelValue', fileListRef.value);
    emit('change', fileListRef.value);

    // 检查是否有文件正在上传中
    const hasUploading = fileListRef.value.some((file) => file.status === 'uploading');

    // 只有状态变化时才触发
    if (uploading.value !== hasUploading) {
      uploading.value = hasUploading;
      emit('uploading-change', uploading.value);
      console.log('上传状态发生变化:', uploading.value);
    }
  };

  // 上传中的文件追踪器，防止重复上传相同文件
  const uploadingTracker = new Set();

  // 自定义请求处理，用于取代默认上传行为
  const customRequest = (options: any) => {
    const { file, onProgress, onSuccess, onError } = options;

    // 创建文件唯一标识
    const fileKey = `${file.name}_${file.size}`;

    // 检查文件是否已经在上传中
    if (uploadingTracker.has(fileKey)) {
      console.warn('⚠️ 文件正在上传中，忽略重复上传请求:', fileKey);
      return;
    }

    // 添加到上传中追踪器
    uploadingTracker.add(fileKey);

    console.log('📤 开始自定义上传文件:', file.name, '文件大小:', file.size);

    // 创建FormData
    const formData = new FormData();
    formData.append('file', file);

    if (props.recordId) {
      formData.append('recordId', props.recordId);
    }

    if (props.businessType) {
      formData.append('businessType', props.businessType);
    }

    // 模拟进度更新
    let percent = 0;
    const progressInterval = setInterval(() => {
      percent += 10;
      if (percent > 90) {
        clearInterval(progressInterval);
        return;
      }
      onProgress({ percent });
    }, 300);

    // 发送请求
    fetch(props.uploadUrl, {
      method: 'POST',
      body: formData,
    })
      .then(async (response) => {
        clearInterval(progressInterval);

        if (!response.ok) {
          throw new Error(`上传失败，HTTP错误: ${response.status}`);
        }

        const result = await response.json();

        if (result && (result.result === 'success' || result.code === 0)) {
          onSuccess(result);

          // 添加到已上传文件列表
          const newUploadedFile = {
            fileId: result.fileId || result.id || '',
            id: result.fileId || result.id || '',
            fileName: file.name,
            url: result.url || '',
            thumbUrl: '',
            previewUrl: result.previewUrl || '',
          };

          // 如果是图片，处理缩略图URL
          if (file.type?.startsWith('image/')) {
            if (result.thumbUrl) {
              newUploadedFile.thumbUrl = result.thumbUrl;
            } else if (result.url) {
              newUploadedFile.thumbUrl = result.url;
            } else if (result.previewUrl) {
              newUploadedFile.thumbUrl = result.previewUrl;
            }
          }

          // 检查是否已存在相同的文件ID，避免重复添加
          const existingFileIndex = uploadedFiles.value.findIndex(
            (item) => item.fileId === newUploadedFile.fileId,
          );

          if (existingFileIndex === -1) {
            // 如果不存在，则添加
            uploadedFiles.value.push(newUploadedFile);
          } else {
            // 如果已存在，则更新
            uploadedFiles.value[existingFileIndex] = newUploadedFile;
          }

          // 触发上传成功事件
          emit('upload-success', {
            file: newUploadedFile,
            response: result,
          });

          showMessage({
            content: t('上传成功'),
            type: 'success',
            duration: 2,
          });
        } else {
          throw new Error(result?.message || '上传失败');
        }
      })
      .catch((error) => {
        clearInterval(progressInterval);
        console.error('文件上传失败:', error);
        onError(error);

        showMessage({
          content: error.message || t('上传失败，请重试'),
          type: 'error',
          duration: 3,
        });
      })
      .finally(() => {
        // 从上传追踪器中移除
        uploadingTracker.delete(fileKey);

        // 检查是否还有文件在上传中
        const hasUploading = fileListRef.value.some((file) => file.status === 'uploading');
        uploading.value = hasUploading;
        emit('uploading-change', hasUploading);
      });
  };

  // 处理文件被拒绝
  const handleFileRejection = (
    file: File,
    reason: string,
    type: 'warning' | 'error' = 'warning',
  ) => {
    showMessage({
      content: reason,
      type,
      duration: 3,
    });

    // 给文件添加rejected标记，以便在handleChange中过滤掉
    (file as any).status = 'rejected';

    // 下一个事件循环中移除该文件
    setTimeout(() => {
      fileListRef.value = fileListRef.value.filter(
        (item) =>
          !(item.name === file.name && item.size === file.size && item.status === 'rejected'),
      );
      emit('update:modelValue', fileListRef.value);
      emit('change', fileListRef.value);
    }, 0);

    return false;
  };

  // 文件上传前处理
  const beforeUpload = (file: File, fileList?: File[]) => {
    console.log('beforeUpload 触发，文件:', file.name, '当前文件列表:', fileListRef.value.length);

    // 检查文件大小
    if (file.size > props.maxSize * 1024 * 1024) {
      return handleFileRejection(file, t(`文件大小不能超过${props.maxSize}MB`), 'error');
    }

    // 如果已经达到文件数量限制
    if (fileListRef.value.length >= props.maxFiles) {
      return handleFileRejection(file, t(`最多只能上传${props.maxFiles}个文件`), 'error');
    }

    // 检查是否已经上传过相同文件
    const isDuplicate = fileListRef.value.some(
      (existingFile) =>
        existingFile.name === file.name &&
        existingFile.size === file.size &&
        (existingFile.status === 'done' || existingFile.status === 'uploading'),
    );

    if (isDuplicate) {
      return handleFileRejection(file, t(`文件 "${file.name}" 已在上传列表中`));
    }

    return true; // 使用customRequest处理上传
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

    // 将文件的previewUrl信息保存到文件对象中，确保移除时不丢失此信息
    fileListRef.value.forEach((item) => {
      if (item.response && item.response.previewUrl && !item.previewUrl) {
        item.previewUrl = item.response.previewUrl;
      }
    });

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

    // 优先使用previewUrl
    if (file.previewUrl) {
      previewUrl = processPreviewUrl(file.previewUrl);
    } else if (file.response && file.response.previewUrl) {
      previewUrl = processPreviewUrl(file.response.previewUrl);
    } else if (file.url) {
      previewUrl = file.url;
    } else if (file.response && file.response.url) {
      previewUrl = file.response.url;
    } else if (file.originFileObj && isImage) {
      // 对于本地预览，使用base64但不保存到uploadedFiles
      previewUrl = await getBase64(file.originFileObj);
    }

    // 处理预览URL
    function processPreviewUrl(url: string) {
      if (url && url.startsWith('fileUpload/')) {
        return '/js/swm/' + url;
      }
      return url;
    }

    if (previewUrl) {
      if (isImage) {
        // 使用图片预览组件预览图片
        createImgPreview({
          imageList: [previewUrl],
          defaultWidth: 800,
        });
      } else {
        // 非图片文件使用新标签页打开
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
    getUploadedFiles: () => {
      // 确保每个文件对象都包含previewUrl字段
      return uploadedFiles.value.map((file) => {
        // 查找fileListRef中对应的文件获取可能的previewUrl
        const fileListItem = fileListRef.value.find(
          (item) =>
            (item.response && item.response.fileId === file.fileId) || item.uid === file.fileId,
        );

        // 如果在fileListRef中找到匹配的文件，优先使用其previewUrl
        const previewUrl =
          fileListItem?.previewUrl || fileListItem?.response?.previewUrl || file.previewUrl || '';

        return {
          ...file,
          previewUrl: previewUrl,
        };
      });
    },
    clearFiles: () => {
      fileListRef.value = [];
      uploadedFiles.value = [];
      emit('update:modelValue', []);
      emit('change', []);
    },
    isUploading: () => uploading.value,
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

        &.ant-upload-list-item-uploading {
          border-color: #1890ff;
          background-color: #e6f7ff;

          .ant-upload-list-item-info {
            background-color: transparent;
          }

          .ant-upload-list-item-thumbnail {
            opacity: 0.6;
          }

          .ant-upload-list-item-name {
            color: #1890ff;
            font-weight: 500;
          }

          // 添加闪烁动画
          animation: uploading-pulse 2s infinite ease-in-out;
        }
      }
    }
  }

  @keyframes uploading-pulse {
    0% {
      box-shadow: 0 0 0 0 rgba(24, 144, 255, 0.4);
    }
    50% {
      box-shadow: 0 0 0 4px rgba(24, 144, 255, 0.2);
    }
    100% {
      box-shadow: 0 0 0 0 rgba(24, 144, 255, 0);
    }
  }

  .upload-debug {
    position: fixed;
    top: 10px;
    right: 10px;
    background-color: rgba(0, 0, 0, 0.6);
    color: #fff;
    padding: 8px 12px;
    border-radius: 4px;
    font-size: 12px;
    z-index: 9999;
  }
</style>
