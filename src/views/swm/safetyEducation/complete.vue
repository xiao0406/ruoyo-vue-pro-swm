<!--
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author zwf
 * @date 2024-06-12
 * @modified_by Shawn
 * @modified_date 2024-06-22
-->
<template>
  <BasicModal
    v-bind="$attrs"
    :title="t('完成安全教育')"
    @register="registerModal"
    @ok="saveUploadFiles"
    :width="700"
    okText="确定"
    cancelText="取消"
    :maskClosable="false"
    :keyboard="false"
    :defaultFullscreen="false"
    :showOkBtn="true"
    :showCancelBtn="true"
    :canFullscreen="false"
  >
    <div style="padding: 0 20px">
      <div class="header-info">
        <InfoCircleOutlined />
        <span>请上传完成教育的证明材料，支持图片、文档和压缩包等格式，最多上传5个文件</span>
      </div>

      <BasicForm @register="registerForm">
        <template #fileUpload>
          <FileUploader
            v-model="fileList"
            :recordId="recordId"
            :loading="isLoading"
            :maxFiles="5"
            :maxSize="20"
            uploadUrl="/js/swm/fileUpload/upload"
            accept=".jpg,.jpeg,.png,.gif,.doc,.docx,.xls,.xlsx,.pdf,.zip,.rar,.7z"
            @uploading-change="onUploadingChange"
            @upload-success="onUploadSuccess"
            ref="uploaderRef"
          />
        </template>
      </BasicForm>

      <div class="file-stats" v-if="fileList.length > 0">
        <div class="stat-item">
          <FileOutlined />
          <span>已上传 {{ fileList.length }}/5 个文件</span>
        </div>
      </div>
    </div>
  </BasicModal>
</template>
<script lang="ts">
  export default defineComponent({
    name: 'ViewsSwmSafetyEducationComplete',
  });
</script>
<script lang="ts" setup>
  import { defineComponent, ref, computed } from 'vue';
  import { useI18n } from '@/hooks/swm/useI18n';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { BasicForm, FormSchema, useForm } from '@/components/swm/Form';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { FileOutlined, InfoCircleOutlined } from '@ant-design/icons-vue';
  import { safetyEducationComplete } from '@/api/swm/safetyEducation';
  import { safetyEducationFileList } from '@/api/swm/safetyEducation';
  import { StatusEnum } from '@/enums/swm/safetyEducationEnum';
  import { FileUploader } from '@/views/swm/components';

  const emit = defineEmits(['success', 'register']);
  const { t } = useI18n('swm.safetyEducation');
  const { showMessage } = useMessage();

  // 处理缩略图URL，将完整URL转换为预览接口格式
  function processThumbnailUrl(url: string) {
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
        console.warn('无法解析缩略图URL:', url);
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
  const recordId = ref('');
  const fileList = ref<any[]>([]);
  const uploaderRef = ref<any>(null);
  const isLoading = ref(false);
  const isUploading = ref(false);

  const inputFormSchemas: FormSchema[] = [
    {
      label: t('上传完成证明'),
      field: 'files',
      component: 'Input',
      slot: 'fileUpload',
      required: true,
      labelWidth: 120,
    },
  ];

  const [registerForm, { resetFields, validate }] = useForm({
    labelWidth: 120,
    schemas: inputFormSchemas,
    showActionButtonGroup: false,
    labelCol: { span: 4 },
    wrapperCol: { span: 20 },
  });

  const [registerModal, { closeModal, setModalProps }] = useModalInner(async (data) => {
    try {
      // 重置状态
      resetFields();
      // 设置加载状态
      isLoading.value = true;
      setModalProps({ confirmLoading: false });

      // 清空文件列表
      fileList.value = [];

      // 清空之前的ID
      recordId.value = '';

      // 设置记录ID
      if (data && data.id) {
        recordId.value = data.id;

        // 加载已有附件
        await loadExistingFiles(data.id);

        if (process.env.NODE_ENV !== 'production') {
          console.log('完成教育模态框记录ID:', recordId.value);
          console.log('已加载附件数量:', fileList.value.length);
        }
      } else {
        console.warn('完成教育模态框打开但未获取到记录ID');
      }
    } catch (error) {
      console.error('加载附件列表失败:', error);
      showMessage(
        {
          content: t('加载附件列表失败，请重试'),
        },
        'error',
      );
    } finally {
      isLoading.value = false;
    }
  });

  // 加载已有附件
  const loadExistingFiles = async (id: string) => {
    try {
      const res = await safetyEducationFileList({ id });

      if (res && Array.isArray(res)) {
        // 处理服务器返回的文件列表
        const files = res.map((file) => {
          // 处理缩略图URL
          let thumbUrl = file.thumbUrl || '';
          if (
            !thumbUrl &&
            file.url &&
            (file.fileName?.match(/\.(jpeg|jpg|gif|png)$/i) ||
              file.url?.match(/\.(jpeg|jpg|gif|png)$/i))
          ) {
            thumbUrl = file.url;
          }

          // 转换为预览接口格式
          if (thumbUrl) {
            thumbUrl = processThumbnailUrl(thumbUrl);
          }

          return {
            uid: String(file.fileId || file.id || Date.now().toString() + Math.random().toString()),
            name: file.fileName || file.name || '未知文件',
            url: file.url || '',
            previewUrl: file.previewUrl || '',
            thumbUrl: thumbUrl,
            status: 'done',
            response: {
              fileId: file.fileId || file.id,
              id: file.fileId || file.id,
              url: file.url || '',
              thumbUrl: thumbUrl,
              previewUrl: file.previewUrl || '',
            },
          };
        });

        // 设置文件列表
        fileList.value = files;
      }
    } catch (error) {
      console.error('加载已有附件失败:', error);
      throw error;
    }
  };

  // 处理上传状态变化
  const onUploadingChange = (uploading: boolean) => {
    isUploading.value = uploading;
    console.log('上传状态变化:', uploading);
  };

  // 处理上传成功
  const onUploadSuccess = (data: any) => {
    console.log('文件上传成功:', data);
  };

  // 保存上传文件的主函数
  async function saveUploadFiles() {
    try {
      // 如果还有文件在上传中，提示用户等待
      if (isUploading.value) {
        showMessage(
          {
            content: t('文件正在上传中，请等待上传完成'),
            duration: 3,
          },
          'warning',
        );
        return;
      }

      // 设置提交中状态
      setModalProps({ confirmLoading: true });

      // 获取上传的文件列表
      const uploadedFiles = uploaderRef.value?.getUploadedFiles() || [];

      // 验证上传文件
      if (fileList.value.length === 0 || uploadedFiles.length === 0) {
        showMessage(
          {
            content: t('没有上传成功的文件，无法保存!'),
            duration: 3,
          },
          'error',
        );
        setModalProps({ confirmLoading: false });
        return;
      }

      // 准备参数 - 确保fileIds中不包含重复的ID
      const fileIds = [
        ...new Set(
          fileList.value
            .filter((file) => file.status === 'done' && file.response && file.response.fileId)
            .map((file) => file.response.fileId),
        ),
      ].join(',');

      // 过滤上传文件列表，确保只包含必要的信息且不重复
      const uniqueFileIds = new Set();
      const cleanUploadedFiles = uploadedFiles
        .filter((file) => {
          if (uniqueFileIds.has(file.fileId)) {
            return false; // 跳过重复的文件
          }
          uniqueFileIds.add(file.fileId);
          return true;
        })
        .map((file) => ({
          fileId: file.fileId,
          id: file.id,
          fileName: file.fileName,
          url: file.url,
          previewUrl: file.previewUrl || '',
        }));

      const params = {
        id: recordId.value,
        fileIds: fileIds,
        attachmentUrl: JSON.stringify(cleanUploadedFiles),
        status: StatusEnum.COMPLETED,
      };

      if (process.env.NODE_ENV !== 'production') {
        console.log('提交完成安全教育请求，参数:', params);
      }

      // 提交数据
      const res = await safetyEducationComplete(params);
      const result = res?.data || res;

      // 处理响应结果
      if (result && (result.result === 'success' || result.code === 0)) {
        closeModal();
        showMessage(
          {
            content: result.message || '完成教育提交成功',
            duration: 2,
          },
          'success',
        );
        emit('success');
      } else {
        showMessage(
          {
            content: result?.message || '操作失败，请重试',
            duration: 3,
          },
          'error',
        );
      }
    } catch (error: any) {
      console.error('完成教育失败:', error);
      showMessage(
        {
          content: error?.message || '操作失败，请重试',
        },
        'error',
      );
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>

<style lang="less" scoped>
  .header-info {
    display: flex;
    align-items: center;
    padding: 10px 12px;
    background-color: #e6f7ff;
    border-radius: 4px;
    margin-bottom: 16px;

    .anticon {
      color: #1890ff;
      font-size: 16px;
      margin-right: 8px;
    }

    span {
      color: #333;
      font-size: 14px;
    }
  }

  :deep(.ant-modal-body) {
    padding: 20px;
    overflow: hidden;

    .ant-form {
      overflow: visible;
    }
  }

  .file-stats {
    display: flex;
    align-items: center;
    margin-top: 12px;
    padding: 8px 12px;
    background-color: #f5f5f5;
    border-radius: 4px;

    .stat-item {
      display: flex;
      align-items: center;

      .anticon {
        color: #1890ff;
        margin-right: 8px;
        font-size: 16px;
      }

      span {
        color: #555;
        font-size: 14px;
      }
    }
  }

  :deep(.ant-form-item-label) {
    font-weight: 500;
  }
</style>
