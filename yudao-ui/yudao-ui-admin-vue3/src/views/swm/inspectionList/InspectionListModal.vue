<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    @ok="handleSubmit"
    width="800px"
    :okButtonProps="{ disabled: isView }"
  >
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
          :readonly="isView"
        />
      </template>
    </BasicForm>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicForm, useForm } from '@/components/swm/Form/index';
  import { formSchema } from './inspectionListData';
  import { getInspectionList, saveInspectionList } from '@/api/swm/inspectionList';
  import { FileUploader } from '@/views/swm/components';
  import { useMessage } from '@/hooks/swm/useMessage';

  // 附件类型定义
  interface Attachment {
    fileId?: string;
    id?: string;
    fileName?: string;
    name?: string;
    url?: string;
    previewUrl?: string;
    thumbUrl?: string;
  }

  const emit = defineEmits(['success', 'register']);
  const isUpdate = ref(false);
  const isView = ref(false); // 添加查看模式标记
  const recordId = ref('');
  const fileList = ref<any[]>([]);
  const uploaderRef = ref<any>(null);
  const isLoading = ref(false);
  const isUploading = ref(false);
  const { showMessage } = useMessage();

  const [registerForm, { resetFields, setFieldsValue, validate, setProps, updateSchema }] = useForm(
    {
      labelWidth: 80,
      schemas: formSchema,
      showActionButtonGroup: false,
    },
  );

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    isView.value = !!data?.isView; // 设置查看模式

    // 如果是查看模式，设置表单为只读
    if (isView.value) {
      setProps({
        disabled: true,
      });
      // 更新表单字段显示状态
      updateSchema([
        {
          field: 'inspectorId',
          show: false,
        },
        {
          field: 'inspector',
          show: true,
        },
      ]);
    } else {
      setProps({
        disabled: false,
      });
      // 更新表单字段显示状态
      updateSchema([
        {
          field: 'inspectorId',
          show: true,
        },
        {
          field: 'inspector',
          show: false,
        },
      ]);
    }

    // 清空文件列表
    fileList.value = [];
    isLoading.value = true;

    try {
      if (data?.record) {
        recordId.value = data.record.id;
        const record = await getInspectionList({ id: recordId.value });

        // 设置表单值
        setFieldsValue({
          ...record.swmInspectionList,
        });

        // 使用详情接口返回的数据加载附件
        if (record.swmInspectionList && record.swmInspectionList.attachmentPath) {
          await parseAttachmentData(record.swmInspectionList.attachmentPath);
        }
      } else {
        // 新建记录
        recordId.value = '';
      }
    } catch (error) {
      console.error('加载数据失败:', error);
      showMessage({
        content: '加载数据失败，请重试',
        type: 'error',
      });
    } finally {
      isLoading.value = false;
    }
  });

  // 生成预览URL的本地工具函数
  const generatePreviewUrl = (filePath: string): string => {
    if (!filePath) return '';

    console.log('generatePreviewUrl 输入:', filePath);

    // 如果已经是正确的完整预览URL（包含/js/swm/），直接返回
    if (filePath.includes('/js/swm/fileUpload/preview?objectName=')) {
      console.log('已经是正确的预览URL，直接返回');
      return filePath;
    }

    // 如果是以 fileUpload/preview?objectName= 开头，添加 /js/swm/ 前缀
    if (filePath.startsWith('fileUpload/preview?objectName=')) {
      const result = `/js/swm/${filePath}`;
      console.log('添加前缀后的URL:', result);
      return result;
    }

    // 提取文件路径部分（objectName）
    let objectName = filePath;

    // 如果包含完整路径，提取objectName参数
    if (filePath.includes('objectName=')) {
      const match = filePath.match(/objectName=([^&]+)/);
      if (match) {
        objectName = match[1];
        console.log('从URL参数中提取objectName:', objectName);
      }
    } else if (filePath.startsWith('common/') || filePath.includes('/')) {
      // 如果是直接的文件路径（如：common/1945780113663344640/4f2651e0-5104-4a45-b781-316905a0a4f0.png）
      objectName = filePath;
      console.log('使用直接文件路径作为objectName:', objectName);
    }

    // 生成正确的预览URL，确保包含 /js/swm/ 前缀
    const result = `/js/swm/fileUpload/preview?objectName=${objectName}`;
    console.log('生成的预览URL:', result);
    return result;
  };

  // 解析附件数据
  const parseAttachmentData = async (attachmentData: string) => {
    try {
      // 尝试解析附件JSON数据
      const attachments = JSON.parse(attachmentData);

      if (Array.isArray(attachments) && attachments.length > 0) {
        // 处理附件数据
        const files = attachments.map((file) => {
          const isImage =
            file.fileName?.match(/\.(jpeg|jpg|gif|png)$/i) ||
            file.url?.match(/\.(jpeg|jpg|gif|png)$/i);

          // 处理thumbUrl，确保图片使用预览URL格式
          let thumbUrl = '';
          if (isImage) {
            if (file.thumbUrl && file.thumbUrl.includes('/js/swm/fileUpload/preview?objectName=')) {
              // 如果thumbUrl已经是正确格式，直接使用
              thumbUrl = file.thumbUrl;
              console.log('使用原有正确格式的thumbUrl:', thumbUrl);
            } else if (file.previewUrl) {
              // 对previewUrl进行处理，确保格式正确
              thumbUrl = generatePreviewUrl(file.previewUrl);
              console.log('处理previewUrl作为thumbUrl:', file.previewUrl, '->', thumbUrl);
            } else if (file.thumbUrl) {
              // 对原有thumbUrl进行处理
              thumbUrl = generatePreviewUrl(file.thumbUrl);
              console.log('处理原有thumbUrl:', file.thumbUrl, '->', thumbUrl);
            } else if (file.url) {
              // 从URL中提取文件路径，生成预览URL
              thumbUrl = generatePreviewUrl(file.url);
              console.log('从URL生成thumbUrl:', file.url, '->', thumbUrl);
            }
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
        console.log('已加载附件数量:', fileList.value.length);
      }
    } catch (e) {
      console.error('解析附件数据失败:', e);
    }
  };

  const getTitle = computed(() => {
    if (isView.value) {
      return '查看巡检任务';
    }
    return unref(isUpdate) ? '编辑巡检任务' : '新增巡检任务';
  });

  // 处理上传状态变化
  const onUploadingChange = (uploading: boolean) => {
    isUploading.value = uploading;
  };

  // 处理上传成功
  const onUploadSuccess = (data: any) => {
    console.log('文件上传成功:', data);
  };

  async function handleSubmit() {
    // 如果是查看模式，直接关闭
    if (isView.value) {
      closeModal();
      return;
    }

    try {
      // 如果还有文件在上传中，提示用户等待
      if (isUploading.value) {
        showMessage({
          content: '文件正在上传中，请等待上传完成',
          type: 'warning',
          duration: 3,
        });
        return;
      }

      const values = await validate();
      setModalProps({ confirmLoading: true });

      // 获取上传的文件列表
      const uploadedFiles = uploaderRef.value?.getUploadedFiles() || [];

      // 准备附件ID参数
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
            return false;
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

      // 更新表单数据
      values.fileIds = fileIds;
      values.attachmentPath = JSON.stringify(cleanUploadedFiles);

      await saveInspectionList(values);
      closeModal();
      emit('success');
    } catch (error) {
      console.error('保存失败:', error);
      showMessage({
        content: '保存失败，请重试',
        type: 'error',
      });
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
