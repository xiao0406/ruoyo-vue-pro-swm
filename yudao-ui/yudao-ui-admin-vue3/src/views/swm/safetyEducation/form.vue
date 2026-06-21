<!--
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * 
 * @author Shawn
 * @date 2025-06-20
-->
<template>
  <BasicDrawer
    v-bind="$attrs"
    :showFooter="!isView"
    @register="registerDrawer"
    @ok="handleSubmit"
    @cancel="handleCancel"
    width="60%"
    :title="getDrawerTitle"
    :showCancelBtn="!isView"
    cancelText="取消"
    okText="保存"
    :canFullscreen="false"
  >
    <div style="padding: 0px 15px">
      <BasicForm @register="registerForm" :disabled="isView">
        <template #participantsTree>
          <TreeSelectTransfer
            :isView="isView"
            :treeData="organizationTreeData"
            v-model="record.participants"
            :participationType="currentParticipationType"
            @change="onParticipantsChange"
            @namesChange="onParticipantsNameChange"
            ref="treeSelectTransferRef"
          />
        </template>
        <template #participantsView>
          <div class="participants-tags-container">
            <Tag
              v-for="(name, index) in getParticipantNamesList(record.participantsName)"
              :key="index"
              color="blue"
              style="margin: 4px 4px 4px 0"
            >
              {{ name }}
            </Tag>
            <span
              v-if="
                !record.participantsName ||
                getParticipantNamesList(record.participantsName).length === 0
              "
              class="no-participants"
            >
              无参与对象
            </span>
          </div>
        </template>
        <template #fileUpload>
          <div class="file-upload-container">
            <div v-if="isUploading" class="upload-status-banner">
              <LoadingOutlined spin class="loading-icon" />
              <div class="upload-status-content">
                <div class="upload-status-text">文件上传中，请等待上传完成后再保存...</div>
                <div class="upload-status-progress">
                  {{ getUploadingStatus }}
                </div>
              </div>
            </div>
            <FileUploader
              v-model="fileList"
              :recordId="record.id"
              :businessType="'safety-education'"
              :loading="isLoading"
              :uploadUrl="'/js/swm/fileUpload/upload'"
              @change="onFileListChange"
              @uploading-change="onFileUploadingChange"
              ref="fileUploaderRef"
            />
          </div>
        </template>
      </BasicForm>
      <!-- 调试信息 -->
      <div v-if="isUploading" class="debug-info"> 上传中: {{ isUploading }} </div>
    </div>
  </BasicDrawer>
</template>

<script lang="ts">
  import { defineComponent } from 'vue';

  export default defineComponent({
    name: 'ViewsSwmSafetyEducationForm',
  });
</script>

<script lang="ts" setup>
  import { onMounted } from 'vue';
  import { BasicForm } from '@/components/swm/Form';
  import { BasicDrawer } from '@/components/swm/Drawer';
  import { TreeSelectTransfer } from './components';
  import { FileUploader } from '../components';
  import { useEducationForm } from './useEducationForm';
  import { treeSelectData } from './schema';
  import { LoadingOutlined } from '@ant-design/icons-vue';
  import { Tag } from 'ant-design-vue';

  const emit = defineEmits(['success', 'register']);

  // 使用提取的表单逻辑
  const {
    t,
    isView,
    record,
    fileList,
    isLoading,
    isUploading,
    fileUploaderRef,
    getDrawerTitle,
    onParticipantsChange,
    onFileListChange,
    onFileUploadingChange,
    registerForm,
    registerDrawer,
    handleSubmit,
    handleCancel,
    getUploadingStatus,
    organizationTreeData,
    initOrganizationTree,
    currentParticipationType,
    onParticipantsNameChange,
    treeSelectTransferRef,
    getParticipantNamesList,
  } = useEducationForm(emit);

  // 组件挂载时初始化组织树数据
  onMounted(() => {
    initOrganizationTree();
  });
</script>

<style lang="less" scoped>
  :deep(.ant-drawer-body) {
    padding: 16px;
    overflow: hidden;

    .ant-drawer-content {
      overflow: hidden;
    }

    .ant-form {
      height: 100%;
      overflow: auto;
    }

    .ant-table-wrapper {
      .ant-table-body {
        overflow-x: hidden !important;
      }
    }

    .ant-table-empty .ant-table-body {
      overflow-x: hidden !important;
      overflow-y: hidden !important;
    }
  }

  .participants-tags-container {
    min-height: 32px;
    padding: 4px 0;

    .ant-tag {
      margin: 2px 4px 2px 0;
      padding: 2px 8px;
      border-radius: 4px;
      font-size: 12px;
    }

    .no-participants {
      color: #999;
      font-style: italic;
    }
  }

  .file-upload-container {
    position: relative;
    width: 100%;
    margin-top: 50px;

    .upload-status-banner {
      position: absolute;
      top: -50px;
      left: 0;
      right: 0;
      z-index: 1000;
      background-color: #f9f0ff;
      border: 2px dashed #722ed1;
      border-radius: 4px;
      padding: 12px 16px;
      display: flex;
      align-items: center;
      margin-bottom: 16px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      animation: pulse 1.5s infinite ease-in-out;

      .loading-icon {
        color: #722ed1;
        font-size: 24px;
        margin-right: 16px;
      }

      .upload-status-content {
        display: flex;
        flex-direction: column;

        .upload-status-text {
          color: #722ed1;
          font-weight: 500;
          font-size: 16px;
          margin-bottom: 6px;
        }

        .upload-status-progress {
          color: #52c41a;
          font-size: 14px;
        }
      }
    }
  }

  @keyframes pulse {
    0% {
      box-shadow: 0 0 0 0 rgba(114, 46, 209, 0.4);
    }

    70% {
      box-shadow: 0 0 0 8px rgba(114, 46, 209, 0);
    }

    100% {
      box-shadow: 0 0 0 0 rgba(114, 46, 209, 0);
    }
  }

  .debug-info {
    position: fixed;
    bottom: 10px;
    right: 10px;
    background-color: rgba(0, 0, 0, 0.6);
    color: #fff;
    padding: 8px 12px;
    border-radius: 4px;
    font-size: 12px;
    z-index: 9999;
  }
</style>
