<template>
  <BasicModal
    @register="registerVideoModal"
    :width="900"
    :height="650"
    :maskClosable="true"
    :fullScreen="false"
    :footer="null"
    :showCancelBtn="false"
    :showOkBtn="false"
    :centered="true"
    :useWrapper="true"
    @cancel="handleModalClose"
    @visible-change="handleVisibleChange"
  >
    <template #title>
      <span>{{ currentTitle }}</span>
    </template>
    <template #default>
      <div
        class="video-player-container"
        style="
          width: 100%;
          height: 100%;
          display: flex;
          justify-content: center;
          align-items: center;
          padding: 20px;
          box-sizing: border-box;
        "
      >
        <video
          ref="videoRef"
          v-if="currentVideoUrl"
          :src="currentVideoUrl"
          controls
          autoplay="false"
          style="width: 100%; height: 100%; max-height: 550px; object-fit: contain"
          @error="handleVideoError"
        >
          您的浏览器不支持视频播放
        </video>
        <div v-else style="padding: 50px 0; color: #999">{{ t('视频地址无效') }}</div>
      </div>
    </template>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { useI18n } from '@/hooks/swm/useI18n';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { BasicModal, useModal } from '@/components/swm/Modal';

  const { t } = useI18n();
  const { showMessage } = useMessage();

  // 组件内部响应式状态
  const currentTitle = ref('');
  const currentVideoUrl = ref('');
  const videoRef = ref<HTMLVideoElement | null>(null);

  const [registerVideoModal, { openModal, closeModal }] = useModal();

  // 停止视频播放
  const stopVideo = () => {
    if (videoRef.value) {
      videoRef.value.pause();
      videoRef.value.currentTime = 0;
    }
  };

  // 模态框取消事件处理
  const handleModalClose = () => {
    stopVideo();
  };

  // 模态框可见性变化事件处理
  const handleVisibleChange = (visible: boolean) => {
    if (!visible) {
      stopVideo();
    }
  };

  // 视频播放错误处理
  const handleVideoError = (event: Event) => {
    console.error('Video play error:', event);
    console.error('Video URL:', currentVideoUrl.value);
    showMessage(t('视频播放失败，请检查视频地址或网络连接'));
  };

  // 获取实际UR
  const getActualUrl = (value: string): string => {
    if (!value) return '';
    try {
      const fileData = JSON.parse(value);
      if (Array.isArray(fileData) && fileData.length > 0 && fileData[0].url) {
        // 只处理数组格式，取第一个元素的URL
        return fileData[0].url;
      }
    } catch (e) {
      console.warn('无法解析URL:', e);
      return '';
    }
    return '';
  };

  // 处理预览URL，将URL转换为预览接口格式
  const processPreviewUrl = (url: string): string => {
    if (!url) return '';

    // 如果已经是预览接口格式，直接返回
    if (url.startsWith('/js/swm/fileUpload/preview')) {
      return url;
    }

    // 如果是相对路径（如 common/xxx.mp4），转换为预览接口格式
    if (!url.includes('://') && !url.startsWith('/')) {
      return `/js/swm/fileUpload/preview?objectName=${url}`;
    }

    return url;
  };

  // 播放视频
  const playVideo = (title: string, fileUrl: string) => {
    if (fileUrl) {
      const actualUrl = getActualUrl(fileUrl);
      const previewUrl = processPreviewUrl(actualUrl);

      // 更新组件内部状态
      currentTitle.value = title;
      currentVideoUrl.value = previewUrl;

      // 打开模态框
      openModal(true);
    }
  };

  // 对外暴露方法
  defineExpose({
    playVideo,
    closeModal,
  });
</script>
