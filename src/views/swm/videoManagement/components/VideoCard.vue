<!--
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author zwf
 * @date 2025-05-21
-->
<template>
  <div class="video-card">
    <div class="video-thumbnail">
      <img :src="fileData.thumbnailUrl" alt="视频缩略图" />
      <div class="file-type">
        <DictLabel dictType="media_file_type_enum" :dictValue="fileData.fileType" />
      </div>
    </div>
    <div class="video-info">
      <div class="file-name">{{ fileData.fileName }}</div>
      <div class="upload-info">上传人：{{ fileData.uploadByName }}</div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { defineProps, computed, onMounted } from 'vue';
  import { DictLabel, useDict } from '@/components/swm/Dict';

  const props = defineProps({
    fileData: {
      type: Object,
      required: true,
      default: () => ({
        id: '',
        fileName: '',
        fileType: 'video',
        thumbnailUrl: '',
        fileUrl: '',
        uploadBy: '',
        uploadByName: '',
        uploadTime: '',
      }),
    },
  });

  // 初始化字典数据
  const { initDict } = useDict();
  onMounted(async () => {
    await initDict(['media_file_type_enum']);
  });
</script>

<style lang="less" scoped>
  .video-card {
    width: 100%;
    border: 1px solid #eaeaea;
    border-radius: 6px;
    overflow: hidden;
    transition: all 0.3s;
    cursor: pointer;
    background-color: #fff;
    box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
    }

    .video-thumbnail {
      position: relative;
      width: 100%;
      height: 160px;
      overflow: hidden;
      background-color: #f0f2f5;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.5s;
      }

      &:hover img {
        transform: scale(1.05);
      }

      .file-type {
        position: absolute;
        bottom: 0;
        right: 0;
        background-color: rgba(0, 0, 0, 0.6);
        color: white;
        padding: 3px 10px;
        font-size: 12px;
        border-top-left-radius: 4px;
      }
    }

    .video-info {
      padding: 12px;

      .file-name {
        font-weight: 500;
        margin-bottom: 6px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        color: #17233d;
      }

      .upload-info {
        font-size: 12px;
        color: #808695;
        display: flex;
        align-items: center;
      }
    }
  }
</style>
