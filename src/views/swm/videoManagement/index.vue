<!--
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author Claude
 * @date 2025-05-21
-->
<template>
  <div class="video-management">
    <div class="page-header">
      <h1>视频/图片库</h1>
    </div>

    <SearchBar @search="handleSearch" />

    <div class="content">
      <div v-if="loading" class="loading-container">
        <a-spin tip="加载中..."></a-spin>
      </div>

      <div v-else-if="isEmpty" class="empty-container">
        <a-empty description="暂无数据" />
      </div>

      <template v-else>
        <DateGroup
          v-for="group in groupedFiles"
          :key="group.date"
          :date="group.date"
          :files="group.files"
          @file-click="handleFileClick"
        />
      </template>
    </div>

    <!-- 预览模态框 -->
    <a-modal
      v-model:visible="previewVisible"
      :footer="null"
      :width="800"
      @cancel="handlePreviewCancel"
    >
      <div class="preview-container" v-if="currentFile">
        <template v-if="isVideo(currentFile.fileType)">
          <video :src="currentFile.fileUrl" controls class="preview-video" width="100%"></video>
        </template>
        <template v-else>
          <img :src="currentFile.fileUrl" class="preview-image" />
        </template>

        <div class="preview-info">
          <p>文件名：{{ currentFile.fileName }}</p>
          <p>上传人：{{ currentFile.uploadByName }}</p>
          <p>上传时间：{{ currentFile.uploadTime }}</p>
          <p>备注：{{ currentFile.remarks }}</p>
          <p>标签：{{ currentFile.tags }}</p>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" setup name="ViewsSwmVideoManagementIndex">
  import { ref, computed, onMounted } from 'vue';
  import { Spin, Empty, Modal } from 'ant-design-vue';
  import SearchBar from './components/SearchBar.vue';
  import DateGroup from './components/DateGroup.vue';
  import { listAllMediaFiles } from '@/api/swm/mediaFile';
  import { DictLabel, useDict } from '@/components/swm/Dict';

  // 文件接口定义
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
  }

  // 搜索参数接口
  interface SearchParams {
    startDate: string | null;
    endDate: string | null;
    keyword: string;
  }

  // 加载状态
  const loading = ref(true);

  // 当前预览文件
  const currentFile = ref<FileItem | null>(null);
  const previewVisible = ref(false);

  // 搜索参数
  const searchParams = ref<SearchParams>({
    startDate: null,
    endDate: null,
    keyword: '',
  });

  // 文件数据
  const fileList = ref<FileItem[]>([]);

  // 初始化字典数据
  const { initDict } = useDict();

  // 计算属性：按日期分组的文件
  const groupedFiles = computed(() => {
    const filteredFiles = filterFiles();

    // 按日期分组
    const groups: Record<string, FileItem[]> = {};
    filteredFiles.forEach((file) => {
      const date = file.uploadTime.split(' ')[0]; // 提取日期部分
      if (!groups[date]) {
        groups[date] = [];
      }
      groups[date].push(file);
    });

    // 转换为数组并排序
    return Object.keys(groups)
      .sort((a, b) => (b < a ? -1 : 1)) // 降序排列
      .map((date) => ({
        date,
        files: groups[date],
      }));
  });

  // 是否为空数据
  const isEmpty = computed(() => {
    return groupedFiles.value.length === 0;
  });

  // 过滤文件
  const filterFiles = (): FileItem[] => {
    let result = [...fileList.value];

    // 按上传时间过滤
    if (searchParams.value.startDate) {
      result = result.filter((file) => {
        const fileDate = file.uploadTime.split(' ')[0];
        return fileDate >= searchParams.value.startDate!;
      });
    }

    if (searchParams.value.endDate) {
      result = result.filter((file) => {
        const fileDate = file.uploadTime.split(' ')[0];
        return fileDate <= searchParams.value.endDate!;
      });
    }

    // 按关键词过滤（同时查找文件名和上传人）
    if (searchParams.value.keyword) {
      const keyword = searchParams.value.keyword.toLowerCase();
      result = result.filter((file) => {
        return (
          file.fileName.toLowerCase().includes(keyword) ||
          file.uploadByName.toLowerCase().includes(keyword) ||
          (file.tags && file.tags.toLowerCase().includes(keyword))
        );
      });
    }

    return result;
  };

  // 处理搜索
  const handleSearch = (params: SearchParams) => {
    console.log('搜索参数:', params);
    searchParams.value = { ...params };

    // 模拟搜索加载
    loading.value = true;
    setTimeout(() => {
      loading.value = false;
    }, 500);
  };

  // 判断是否为视频
  const isVideo = (fileType: string): boolean => {
    return Boolean(fileType && fileType.includes('video'));
  };

  // 处理文件点击
  const handleFileClick = (file: FileItem) => {
    currentFile.value = file;
    previewVisible.value = true;
  };

  // 关闭预览
  const handlePreviewCancel = () => {
    previewVisible.value = false;
  };

  // 加载媒体文件数据
  const loadMediaFiles = async () => {
    try {
      loading.value = true;
      const res = await listAllMediaFiles();
      fileList.value = res || [];
    } catch (error) {
      console.error('加载媒体文件失败:', error);
    } finally {
      loading.value = false;
    }
  };

  // 生命周期钩子
  onMounted(async () => {
    // 加载字典数据
    await initDict(['media_file_type_enum']);

    // 加载媒体文件
    loadMediaFiles();
  });
</script>

<style lang="less" scoped>
  .video-management {
    padding: 24px;

    .page-header {
      margin-bottom: 24px;

      h1 {
        font-size: 24px;
        font-weight: bold;
        color: #17233d;
        margin: 0;
        padding-bottom: 12px;
        border-bottom: 1px solid #e8eaec;
      }
    }

    .content {
      background: #fff;
      padding: 24px;
      border-radius: 4px;
      min-height: 500px;
      box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);
    }

    .loading-container,
    .empty-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 400px;
    }

    .preview-container {
      .preview-video,
      .preview-image {
        max-width: 100%;
        margin-bottom: 16px;
        border-radius: 4px;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
      }

      .preview-image {
        max-height: 500px;
        display: block;
        margin: 0 auto;
      }

      .preview-info {
        margin-top: 16px;
        padding: 16px;
        border-radius: 4px;
        background-color: #f8f8f9;
        border-top: 1px solid #e8eaec;

        p {
          margin-bottom: 8px;
          color: #515a6e;

          &:last-child {
            margin-bottom: 0;
          }
        }
      }
    }
  }
</style>
