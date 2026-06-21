<!--
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author zwf
 * @date 2025-05-21
-->
<template>
  <div class="date-group">
    <div class="date-title">{{ date }}</div>
    <div class="file-list">
      <a-row :gutter="[16, 16]">
        <a-col :xs="12" :sm="8" :md="6" v-for="item in files" :key="item.id">
          <VideoCard :fileData="item" @click="handleFileClick(item)" />
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { defineProps, defineEmits } from 'vue';
  import { Row as ARow, Col as ACol } from 'ant-design-vue';
  import VideoCard from './VideoCard.vue';
  import { DictLabel } from '@/components/swm/Dict';

  interface FileItem {
    id: string;
    fileName: string;
    fileType: string;
    thumbnailUrl: string;
    fileUrl: string;
    uploadBy: string;
    uploadByName: string;
    uploadTime: string;
    remarks: string;
    tags: string;
    businessType: string;
    fileSize: number;
    [key: string]: any;
  }

  const props = defineProps({
    date: {
      type: String,
      required: true,
    },
    files: {
      type: Array as () => FileItem[],
      default: () => [],
    },
  });

  const emit = defineEmits(['file-click']);

  const handleFileClick = (file: FileItem) => {
    emit('file-click', file);
  };
</script>

<style lang="less" scoped>
  .date-group {
    margin-bottom: 32px;

    .date-title {
      font-size: 16px;
      font-weight: bold;
      padding-bottom: 10px;
      margin-bottom: 18px;
      border-bottom: 1px solid #e8eaec;
      color: #17233d;
      position: relative;

      &::after {
        content: '';
        position: absolute;
        left: 0;
        bottom: -1px;
        width: 64px;
        height: 2px;
        background-color: #2d8cf0;
      }
    }

    .file-list {
      margin-bottom: 12px;
    }
  }
</style>
