<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    title="完成巡检任务"
    @ok="handleSubmit"
    width="600px"
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
        />
      </template>
    </BasicForm>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicForm, FormSchema, useForm } from '@/components/swm/Form/index';
  import { completeInspectionTask } from '@/api/swm/inspectionList';
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
  const recordId = ref('');
  const taskId = ref('');
  const fileList = ref<any[]>([]);
  const uploaderRef = ref<any>(null);
  const isLoading = ref(false);
  const isUploading = ref(false);
  const { createMessage } = useMessage();

  // 表单Schema
  const formSchema: FormSchema[] = [
    {
      field: 'id',
      label: 'ID',
      component: 'Input',
      show: false,
    },
    {
      field: 'remarks',
      label: '备注',
      component: 'InputTextArea',
      componentProps: {
        rows: 4,
        placeholder: '请输入备注',
        maxlength: 500,
      },
    },
    {
      field: 'files',
      label: '附件上传',
      component: 'Input',
      slot: 'fileUpload',
    },
    {
      field: 'fileIds',
      label: '附件ID',
      component: 'Input',
      show: false,
    },
    {
      field: 'attachmentPath',
      label: '附件路径',
      component: 'Input',
      show: false,
    },
  ];

  const [registerForm, { resetFields, setFieldsValue, validate, setProps }] = useForm({
    labelWidth: 100,
    schemas: formSchema,
    showActionButtonGroup: false,
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    resetFields();
    setModalProps({ confirmLoading: false });
    fileList.value = [];
    isLoading.value = false;

    if (data?.record) {
      recordId.value = data.record.id;
      taskId.value = data.record.id;
    }
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
    try {
      // 如果还有文件在上传中，提示用户等待
      if (isUploading.value) {
        createMessage.warning('文件正在上传中，请等待上传完成');
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

      // 准备提交的数据
      const submitData = {
        id: unref(taskId),
        remarks: values.remarks,
        fileIds: fileIds,
        attachmentPath: JSON.stringify(cleanUploadedFiles),
      };

      // 提交数据
      await completeInspectionTask(submitData);
      createMessage.success('巡检任务完成成功!');
      closeModal();
      emit('success');
    } catch (error) {
      console.error('完成巡检任务失败:', error);
      createMessage.error('完成巡检任务失败，请重试');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
