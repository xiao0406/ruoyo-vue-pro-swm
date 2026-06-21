<template>
  <div class="video-uploader">
    <Upload.Dragger
      v-model:file-list="fileListRef"
      name="file"
      :multiple="false"
      :maxCount="1"
      :beforeUpload="beforeUpload"
      @change="handleChange"
      @preview="handlePreview"
      :disabled="uploading"
      :showUploadList="false"
      :customRequest="customRequest"
      accept=".mp4"
    >
      <template v-if="uploading">
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
        <div class="upload-area" :class="{ 'has-file': internalValue.length > 0 }">
          <div v-if="internalValue.length === 0" class="upload-placeholder">
            <CloudUploadOutlined class="upload-icon" />
            <div class="upload-text">点击或拖拽视频上传</div>
            <div class="upload-hint">支持 mp4 格式，最大 {{ maxSize }}MB</div>
          </div>
          <div v-else class="preview-container">
            <!-- 视频预览 -->
            <div class="video-preview">
              <video :src="thumbnailUrl" class="preview-video" controls @click.stop />
              <div class="video-actions">
                <DeleteOutlined class="video-action-icon" @click.stop="handleRemove" />
              </div>
            </div>
          </div>
        </div>
      </template>
    </Upload.Dragger>
  </div>
</template>

<script lang="ts" setup>
  import { ref, watch, computed } from 'vue';
  import { Upload, Spin, message } from 'ant-design-vue';
  import type { UploadFile } from 'ant-design-vue';
  import { CloudUploadOutlined, LoadingOutlined, DeleteOutlined } from '@ant-design/icons-vue';
  import { defHttp } from '@/utils/http/axios';

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
      default: 300,
    },
    uploadUrl: {
      type: String,
      default: '/js/swm/fileUpload/upload',
    },
    baseUrl: {
      type: String,
      default: '',
    },
  });

  const emit = defineEmits(['update:modelValue', 'uploading', 'success', 'remove', 'register']);

  // 文件列表
  const fileListRef = ref<any[]>([]);
  const internalValue = ref<UploadFile<any>[]>([]);
  const uploading = ref(false);

  // 计算缩略图URL（用于界面显示）
  const thumbnailUrl = computed(() => {
    if (internalValue.value.length === 0) return '';
    return internalValue.value[0].url || '';
  });

  // 监听外部值变化
  watch(
    () => props.modelValue,
    (newVal) => {
      if (newVal) {
        try {
          // 尝试解析JSON字符串为数组
          const parsedValue = JSON.parse(newVal);
          if (Array.isArray(parsedValue)) {
            // 直接使用数据，已确认URL格式正确
            internalValue.value = parsedValue;

            // 更新fileListRef以保持同步
            fileListRef.value = parsedValue.map((item) => ({
              uid: '-1',
              name: item.name || '未知文件',
              status: 'done',
              url: item.url,
            }));
          }
        } catch (e) {
          // 如果不是JSON格式或解析失败，清空文件列表
          internalValue.value = [];
          fileListRef.value = [];
        }
      } else {
        // 如果没有值，清空文件列表
        internalValue.value = [];
        fileListRef.value = [];
      }
    },
    { immediate: true },
  );

  // 监听内部值变化
  watch(
    () => internalValue.value,
    (newValue) => {
      // 将数组转换为JSON字符串返回给父组件
      emit('update:modelValue', newValue.length ? JSON.stringify(newValue) : '');
    },
    { deep: true },
  );

  // 上传前检查
  function beforeUpload(file) {
    // 检查文件类型
    const fileExt = file.name.slice(file.name.lastIndexOf('.') + 1).toLowerCase();
    const allowedTypes = ['mp4'];

    if (!allowedTypes.includes(fileExt)) {
      message.error('只能上传MP4格式的文件!');
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

        if (result && result.result === 'success') {
          // 上传成功
          onSuccess(result);

          // 使用previewUrl字段，它已经是预览接口格式，只需要加上系统前缀
          let videoUrl = result.previewUrl || '';
          if (videoUrl && !videoUrl.startsWith('/js/swm/')) {
            // 加上系统前缀，转换为完整的预览URL
            videoUrl = `/js/swm/${videoUrl}`;
          }

          // 更新内部值 - 只保留最新的一个视频
          internalValue.value = [
            {
              name: file.name,
              url: videoUrl,
            } as UploadFile<any>,
          ];

          // 更新fileListRef以保持同步
          fileListRef.value = [
            {
              uid: '-1',
              name: file.name,
              status: 'done',
              url: videoUrl,
            },
          ];

          // 获取视频时长和生成封面
          getVideoInfo(videoUrl).then((videoInfo) => {
            emit('success', {
              file: file,
              name: file.name,
              response: result,
              duration: videoInfo.duration,
              thumbnail: videoInfo.thumbnail,
            });
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
    const { fileList } = info;
    // 更新文件列表
    fileListRef.value = fileList;
  }

  // 打开预览
  function handlePreview() {
    if (internalValue.value.length === 0) return;

    const videoUrl = internalValue.value[0].url || '';
    if (!videoUrl) return;

    // 直接使用URL，已确认格式正确
    window.open(videoUrl, '_blank');
  }

  // 获取视频信息（时长和第一帧封面）
  function getVideoInfo(videoUrl) {
    console.log('Getting video info for:', videoUrl);
    return new Promise((resolve) => {
      const video = document.createElement('video');
      video.crossOrigin = 'anonymous';

      video.onloadedmetadata = () => {
        console.log('Video metadata loaded, duration:', video.duration);

        // 设置视频播放位置到第一帧
        video.currentTime = 0.1; // 确保有画面
      };

      video.onseeked = () => {
        console.log(
          'Video seeked, videoWidth:',
          video.videoWidth,
          'videoHeight:',
          video.videoHeight,
        );

        // 创建canvas元素，使用固定的16:9宽高比，避免黑边
        const targetWidth = 800;
        const targetHeight = 450;
        const canvas = document.createElement('canvas');
        canvas.width = targetWidth;
        canvas.height = targetHeight;

        // 获取视频的宽高比
        const videoAspectRatio = video.videoWidth / video.videoHeight;
        const targetAspectRatio = targetWidth / targetHeight;

        let drawWidth, drawHeight, offsetX, offsetY;

        // 根据宽高比调整绘制区域，确保填充整个canvas
        if (videoAspectRatio > targetAspectRatio) {
          // 视频更宽，按高度缩放，裁剪宽度
          drawHeight = targetHeight;
          drawWidth = video.videoWidth * (targetHeight / video.videoHeight);
          offsetX = (targetWidth - drawWidth) / 2;
          offsetY = 0;
        } else {
          // 视频更高，按宽度缩放，裁剪高度
          drawWidth = targetWidth;
          drawHeight = video.videoHeight * (targetWidth / video.videoWidth);
          offsetX = 0;
          offsetY = (targetHeight - drawHeight) / 2;
        }

        // 绘制第一帧，居中裁剪
        const ctx = canvas.getContext('2d');
        ctx.fillStyle = '#000';
        ctx.fillRect(0, 0, canvas.width, canvas.height);
        ctx.drawImage(
          video,
          0,
          0,
          video.videoWidth,
          video.videoHeight,
          offsetX,
          offsetY,
          drawWidth,
          drawHeight,
        );

        // 转换为base64格式
        const thumbnail = canvas.toDataURL('image/jpeg');
        console.log('Generated thumbnail:', thumbnail.substring(0, 50) + '...');

        resolve({
          duration: Math.round(video.duration),
          thumbnail,
        });
      };

      video.onerror = (error) => {
        console.error('Video error:', error);
        resolve({
          duration: 0,
          thumbnail: '',
        });
      };

      // 设置视频源
      video.src = videoUrl;
    });
  }

  // 删除文件
  function handleRemove() {
    internalValue.value = [];
    fileListRef.value = [];
    emit('remove');
  }
</script>

<style lang="less" scoped>
  .video-uploader {
    width: 400px;

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

      .video-preview {
        width: 100%;
        height: 100%;
        position: relative;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #000;

        .preview-video {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .video-actions {
          position: absolute;
          top: 10px;
          right: 10px;
          display: flex;
          gap: 8px;
        }

        .video-action-icon {
          font-size: 20px;
          color: #fff;
          cursor: pointer;
          padding: 8px;
          background: rgba(0, 0, 0, 0.5);
          border-radius: 50%;
          z-index: 10;

          &:hover {
            transform: scale(1.1);
            background: rgba(0, 0, 0, 0.7);
          }
        }
      }
    }
  }
</style>
