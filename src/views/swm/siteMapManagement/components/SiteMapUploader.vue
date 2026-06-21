<!--
 * 场地底图上传组件
 * @author AI Assistant
 * @date 2024-06-17
-->
<template>
  <div class="site-map-uploader">
    <Upload.Dragger
      v-model:file-list="fileListRef"
      name="file"
      :multiple="false"
      :maxCount="1"
      :beforeUpload="beforeUpload"
      @change="handleChange"
      @preview="handlePreview"
      :disabled="loading"
      :showUploadList="false"
      :customRequest="customRequest"
      :accept="accept"
    >
      <template v-if="loading">
        <div class="upload-loading">
          <Spin>
            <template #indicator>
              <LoadingOutlined style="font-size: 24px" spin />
            </template>
          </Spin>
          <div class="upload-loading-text">上传中...</div>
        </div>
      </template>
      <template v-else>
        <div class="upload-area" :class="{ 'has-file': !!modelValue }">
          <div v-if="!modelValue" class="upload-placeholder">
            <CloudUploadOutlined class="upload-icon" />
            <div class="upload-text">点击或拖拽底图上传</div>
            <div class="upload-hint">支持 jpg、png、gif、pdf 格式，最大 {{ maxSize }}MB</div>
          </div>
          <div v-else class="preview-container">
            <!-- 图片预览 -->
            <div v-if="isImage(modelValue)" class="image-preview">
              <img :src="thumbnailUrl" class="preview-image" @click.stop="openPreview" />
              <div class="preview-mask">
                <div class="preview-actions">
                  <EyeOutlined class="preview-action-icon" @click.stop="openPreview" />
                  <DeleteOutlined class="preview-action-icon" @click.stop="handleRemove" />
                </div>
              </div>
            </div>
            <!-- PDF预览 -->
            <div v-else-if="isPDF(modelValue)" class="pdf-preview" @click="openPreview">
              <FilePdfOutlined class="pdf-icon" />
              <div class="pdf-name">{{ getFileName(modelValue) }}</div>
              <div class="preview-mask">
                <div class="preview-actions">
                  <EyeOutlined class="preview-action-icon" @click.stop="openPreview" />
                  <DeleteOutlined class="preview-action-icon" @click.stop="handleRemove" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </Upload.Dragger>
  </div>
</template>

<script lang="ts" setup>
  import { ref, defineProps, defineEmits, watch, computed } from 'vue';
  import { Upload, Spin, message } from 'ant-design-vue';
  import {
    CloudUploadOutlined,
    LoadingOutlined,
    EyeOutlined,
    DeleteOutlined,
    FilePdfOutlined,
  } from '@ant-design/icons-vue';
  import { defHttp } from '@/utils/http/axios';
  import { createImgPreview } from '@/components/swm/Preview';

  const props = defineProps({
    modelValue: {
      type: String,
      default: '',
    },
    recordId: {
      type: String,
      default: '',
    },
    loading: {
      type: Boolean,
      default: false,
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
      default: '.jpg,.jpeg,.png,.gif,.pdf',
    },
    baseUrl: {
      type: String,
      default: '',
    },
  });

  const emit = defineEmits(['update:modelValue', 'uploading', 'success', 'remove', 'register']);

  // 文件列表
  const fileListRef = ref<any[]>([]);
  const uploading = ref(false);

  // 计算缩略图URL（用于界面显示）
  const thumbnailUrl = computed(() => {
    if (!props.modelValue) return '';

    const actualUrl = getActualUrl(props.modelValue);
    return processPreviewUrl(actualUrl);
  });

  // 计算预览URL（用于点击预览）
  const previewUrl = computed(() => {
    if (!props.modelValue) return '';

    const actualUrl = getActualUrl(props.modelValue);
    return processPreviewUrl(actualUrl);
  });

  // 监听外部值变化
  watch(
    () => props.modelValue,
    (newVal) => {
      if (newVal) {
        // 如果有值，检查文件列表是否需要更新
        const fileName = getFileName(newVal);
        if (fileListRef.value.length === 0 || fileListRef.value[0].url !== newVal) {
          fileListRef.value = [
            {
              uid: '-1',
              name: fileName,
              status: 'done',
              url: newVal,
            },
          ];
        }
      } else {
        // 如果没有值，清空文件列表
        fileListRef.value = [];
      }
    },
    { immediate: true },
  );

  // 获取文件名
  function getFileName(value) {
    if (!value) return '未知文件';

    try {
      // 尝试解析JSON
      const fileData = JSON.parse(value);
      if (fileData && fileData.fileName) {
        return fileData.fileName;
      } else if (fileData && fileData.url) {
        return fileData.url.split('/').pop() || '未知文件';
      }
    } catch (e) {
      // 如果不是JSON，则按原来的方式处理
      return value.split('/').pop() || '未知文件';
    }

    return '未知文件';
  }

  // 获取实际URL
  function getActualUrl(value) {
    if (!value) return '';

    try {
      // 尝试解析JSON
      const fileData = JSON.parse(value);
      if (fileData && fileData.url) {
        return fileData.url;
      }
    } catch (e) {
      // 如果不是JSON，则直接返回
      return value;
    }

    return '';
  }

  // 处理预览URL，将完整URL转换为预览接口格式
  function processPreviewUrl(url: string) {
    if (!url) return '';

    // 如果已经是预览接口格式，直接返回
    if (url.startsWith('/js/swm/fileUpload/preview')) {
      return url;
    }

    // 如果是以 /js/swm/ 开头的相对路径，直接返回
    if (url.startsWith('/js/swm/')) {
      return url;
    }

    // 如果是完整的URL（包含域名和端口），提取文件路径部分
    if (url.includes('://')) {
      try {
        const urlObj = new URL(url);
        let pathname = urlObj.pathname;

        // 移除开头的斜杠和可能的项目名称
        if (pathname.startsWith('/swm/')) {
          pathname = pathname.substring(5); // 移除 '/swm/'
        } else if (pathname.startsWith('/')) {
          pathname = pathname.substring(1); // 移除开头的 '/'
        }

        // 转换为预览接口格式
        return `/js/swm/fileUpload/preview?objectName=${pathname}`;
      } catch (e) {
        console.warn('无法解析URL:', url);
        return url;
      }
    }

    // 如果是相对路径，转换为预览接口格式
    if (url.startsWith('fileUpload/')) {
      return '/js/swm/' + url;
    } else if (url.startsWith('common/') || url.includes('/')) {
      // 对于其他相对路径，转换为预览接口格式
      return `/js/swm/fileUpload/preview?objectName=${url}`;
    }

    return url;
  }

  // 判断是否为图片
  function isImage(value) {
    if (!value) return false;

    const url = getActualUrl(value);
    return /\.(jpg|jpeg|png|gif)$/i.test(url);
  }

  // 判断是否为PDF
  function isPDF(value) {
    if (!value) return false;

    const url = getActualUrl(value);
    return /\.pdf$/i.test(url);
  }

  // 上传前检查
  function beforeUpload(file) {
    // 检查文件类型
    const fileExt = file.name.slice(file.name.lastIndexOf('.') + 1).toLowerCase();
    const allowedTypes = ['jpg', 'jpeg', 'png', 'gif', 'pdf'];

    if (!allowedTypes.includes(fileExt)) {
      message.error('只能上传JPG/PNG/GIF/PDF格式的文件!');
      return false;
    }

    // 检查文件大小
    const isLt20M = file.size / 1024 / 1024 < props.maxSize;
    if (!isLt20M) {
      message.error(`文件必须小于${props.maxSize}MB!`);
      return false;
    }

    return true;
  }

  // 自定义上传请求
  function customRequest(options) {
    const { file, onProgress, onSuccess, onError } = options;

    // 标记上传中状态
    uploading.value = true;
    emit('uploading', true);

    // 创建FormData
    const formData = new FormData();
    formData.append('file', file);

    if (props.recordId) {
      formData.append('recordId', props.recordId);
    }

    // 进度模拟
    let percent = 0;
    const progressInterval = setInterval(() => {
      percent += 10;
      if (percent > 90) {
        clearInterval(progressInterval);
        return;
      }
      onProgress({ percent });
    }, 200);

    // 发送上传请求
    defHttp
      .uploadFile(
        {
          url: props.uploadUrl,
        },
        {
          file: file,
        },
      )
      .then((res) => {
        clearInterval(progressInterval);

        // 处理返回结果，确保类型安全
        const result = res && typeof res === 'object' ? res.data || res : {};

        if (result && result.result === 'success' && result.url) {
          // 上传成功
          onSuccess(result);

          // 更新组件值 - 存储整个响应对象，而不仅仅是URL
          emit('update:modelValue', JSON.stringify(result));
          emit('success', {
            file: file,
            name: file.name,
            response: result,
          });

          message.success('上传成功');
        } else {
          onError(new Error('上传失败'));
          message.error('上传失败: ' + (result.message || '未知错误'));
        }
      })
      .catch((err) => {
        clearInterval(progressInterval);
        onError(err);
        message.error('上传失败: ' + err.message);
      })
      .finally(() => {
        uploading.value = false;
        emit('uploading', false);
      });
  }

  // 处理文件变化
  function handleChange(info) {
    const { file, fileList } = info;

    // 当文件状态为done时，表示上传完成
    if (file.status === 'done' && file.response) {
      const res = file.response;

      if (res.result === 'success' && res.url) {
        // 存储完整的响应对象
        emit('update:modelValue', JSON.stringify(res));
      }
    }

    // 更新文件列表
    fileListRef.value = fileList;
  }

  // 打开预览
  function openPreview() {
    if (!props.modelValue) return;

    const actualUrl = getActualUrl(props.modelValue);
    if (!actualUrl) return;

    // 处理预览URL
    const previewUrl = processPreviewUrl(actualUrl);

    if (isImage(props.modelValue)) {
      // 图片预览
      createImgPreview({
        imageList: [previewUrl],
        defaultWidth: 800,
      });
    } else if (isPDF(props.modelValue)) {
      // PDF预览 - 在新窗口打开
      window.open(previewUrl, '_blank');
    }
  }

  // 删除文件
  function handleRemove() {
    fileListRef.value = [];
    emit('update:modelValue', '');
    emit('remove');
  }

  // 预览文件
  function handlePreview() {
    openPreview();
  }
</script>

<style lang="less" scoped>
  .site-map-uploader {
    width: 100%;

    :deep(.ant-upload-drag) {
      background: #fafafa;
      border: 1px dashed #d9d9d9;
      border-radius: 4px;
      cursor: pointer;
      transition: border-color 0.3s;
      height: 200px; // 固定高度

      &:hover {
        border-color: #1890ff;
      }

      .ant-upload {
        padding: 0;
        height: 100%;
        width: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      // 确保文件输入控件正常工作
      .ant-upload-btn {
        width: 100%;
        height: 100%;
      }
    }

    .upload-area {
      padding: 20px;
      height: 100%;
      width: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;

      &.has-file {
        padding: 0;
      }
    }

    .upload-placeholder {
      text-align: center;
      cursor: pointer;
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
    }

    .upload-icon {
      font-size: 48px;
      color: #1890ff;
      margin-bottom: 8px;
    }

    .upload-text {
      font-size: 16px;
      color: #000;
      margin-bottom: 4px;
    }

    .upload-hint {
      font-size: 12px;
      color: #999;
    }

    .upload-loading {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;

      &-text {
        margin-top: 8px;
        color: #1890ff;
      }
    }

    .preview-container {
      width: 100%;
      height: 100%;
      position: relative;
      overflow: hidden;

      .image-preview {
        width: 100%;
        height: 100%;
        position: relative;
        display: flex;
        align-items: center;
        justify-content: center;

        .preview-image {
          max-width: 100%;
          max-height: 100%;
          object-fit: contain;
        }
      }

      .pdf-preview {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        background-color: #f5f5f5;

        .pdf-icon {
          font-size: 48px;
          color: #ff4d4f;
          margin-bottom: 8px;
        }

        .pdf-name {
          font-size: 14px;
          color: #333;
          max-width: 90%;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .preview-mask {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.3);
        opacity: 0;
        transition: opacity 0.3s;
        display: flex;
        align-items: center;
        justify-content: center;

        &:hover {
          opacity: 1;
        }

        .preview-actions {
          display: flex;
          gap: 16px;

          .preview-action-icon {
            font-size: 20px;
            color: #fff;
            cursor: pointer;
            padding: 8px;
            background: rgba(0, 0, 0, 0.5);
            border-radius: 50%;

            &:hover {
              transform: scale(1.1);
            }
          }
        }
      }
    }
  }
</style>
