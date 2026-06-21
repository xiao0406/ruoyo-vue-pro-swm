<!--
  @author zwf
  @date 2025-05-16
-->
<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    @ok="handleSubmit"
    :width="800"
    :canFullscreen="true"
  >
    <div style="padding: 10px">
      <!-- 顶部信息 -->
      <div class="record-box mb-3">
        <div class="record-item">
          <div class="record-label">报警记录</div>
          <div class="record-content" :title="record.personName || '---'">{{
            record.personName || '---'
          }}</div>
        </div>
        <div class="record-item">
          <div class="record-label">报警时间</div>
          <div class="record-content" :title="record.personName || '---'">{{
            record.alarmTime || '---'
          }}</div>
        </div>
      </div>

      <!-- 触发原因 -->
      <div class="record-boxs mb-3">
        <div class="trigger-label">触发原因</div>
        <div class="trigger-content">{{ record.triggerReason || '---' }}</div>
      </div>

      <!-- 处置表单 -->
      <BasicForm @register="registerForm" />

      <!-- 附件上传 -->
      <div class="mt-4" style="padding: 0 20px">
        <div class="attachment-label">附件</div>
        <div class="upload-info">
          <InfoCircleOutlined />
          <span>支持图片、文档等格式，最多上传5个文件，单个文件不超过10MB</span>
        </div>
        <div class="upload-container">
          <FileUploader
            v-model="fileList"
            :recordId="record.id"
            :loading="isUploading"
            :maxFiles="5"
            :maxSize="10"
            uploadUrl="/js/swm/fileUpload/upload"
            accept=".jpg,.jpeg,.png,.pdf,.doc,.docx,.xls,.xlsx"
            @uploading-change="onUploadingChange"
            @upload-success="onUploadSuccess"
            @remove="onFileRemove"
            ref="uploaderRef"
            :disabled="isView"
          />
          <div class="file-stats" v-if="fileList.length > 0">
            <div class="stat-item">
              <FileOutlined />
              <span
                >{{ isView ? '共' : '已上传' }} {{ fileList.length }}/{{
                  isView ? fileList.length : 5
                }}
                个文件</span
              >
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部按钮 - 仅在非查看模式时显示 -->
    <template #footer>
      <div v-if="!isView" class="modal-footer">
        <a-button @click="closeModal">取消</a-button>
        <a-button type="primary" ghost @click="handleDraft" :loading="draftLoading">暂存</a-button>
        <a-button type="primary" @click="handleSubmit" :loading="saveLoading">提交</a-button>
      </div>
    </template>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, unref, onMounted } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicForm, useForm } from '@/components/swm/Form/index';
  import { processWarning } from '@/api/swm/warning';
  import { getTypeOnePersonList } from '@/api/swm/person';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { Alert, Button } from 'ant-design-vue';
  import { Icon } from '@/components/swm/Icon';
  import { FormSchema } from '@/components/swm/Form';
  import { FileUploader } from '@/views/swm/components';
  import { InfoCircleOutlined, FileOutlined } from '@ant-design/icons-vue';

  export default defineComponent({
    name: 'WarningProcessModal',
    components: {
      BasicModal,
      BasicForm,
      Alert,
      Icon,
      FileUploader,
      InfoCircleOutlined,
      FileOutlined,
      'a-button': Button,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const record = ref<Recordable>({});
      const { createMessage } = useMessage();
      const isView = ref(false);
      const fileList = ref<any[]>([]);
      const isUploading = ref(false);
      const uploaderRef = ref<any>(null);
      const uploadResponses = ref<any[]>([]);
      const confirmLoading = ref(false);
      const draftLoading = ref(false);
      const saveLoading = ref(false);
      const personOptions = ref<{ label: string; value: string }[]>([]); // 人员选项列表

      // 加载人员列表（person_type为1的）
      async function loadPersonOptions() {
        try {
          const res = await getTypeOnePersonList();
          if (res && res.list && res.list.length > 0) {
            personOptions.value = res.list.map((item) => ({
              label: item.name,
              value: item.name,
            }));
            console.log('已加载处置人员列表:', personOptions.value);
          }
        } catch (error) {
          console.error('获取处置人员列表失败:', error);
          createMessage.error('获取处置人员列表失败');
        }
      }

      // 页面加载时获取人员列表
      onMounted(() => {
        loadPersonOptions();
      });

      const formSchema: FormSchema[] = [
        {
          field: 'id',
          label: 'ID',
          component: 'Input',
          show: false,
        },
        {
          field: 'handler',
          label: '处置人',
          component: 'Select',
          componentProps: {
            placeholder: '请选择处置人',
            showSearch: true,
            optionFilterProp: 'label',
            options: personOptions, // 使用动态加载的人员列表
            filterOption: (input: string, option: any) => {
              return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0;
            },
          },
          required: true,
          dynamicDisabled: () => isView.value,
          colProps: { span: 12 },
          rules: [{ required: true, message: '请选择处置人' }],
        },
        {
          field: 'handleTime',
          label: '处置时间',
          component: 'DatePicker',
          componentProps: {
            showTime: true,
            format: 'YYYY-MM-DD HH:mm:ss',
            style: { width: '100%' },
          },
          dynamicDisabled: () => isView.value,
          colProps: { span: 12 },
          required: true,
          rules: [{ required: true, message: '请选择处置时间' }],
        },
        {
          field: 'handleProcess',
          label: '处置过程',
          component: 'InputTextArea',
          componentProps: {
            rows: 4,
            placeholder: '请详细描述处置过程',
          },
          dynamicDisabled: () => isView.value,
          required: true,
          rules: [{ required: true, message: '请输入处置过程' }],
        },
        {
          field: 'handleStatus',
          label: '处置状态',
          component: 'RadioGroup',
          defaultValue: '1',
          dynamicDisabled: () => isView.value,
          show: false,
          componentProps: {
            options: [
              { label: '已处置', value: '1' },
              { label: '草稿', value: '2' },
            ],
          },
        },
      ];

      const [registerForm, { resetFields, setFieldsValue, validate, setProps }] = useForm({
        labelWidth: 80,
        baseColProps: { span: 24 },
        schemas: formSchema,
        showActionButtonGroup: false,
        actionColOptions: {
          span: 24,
        },
      });

      const [registerModal, { setModalProps, closeModal }] = useModalInner((data) => {
        resetFields();
        setModalProps({ confirmLoading: false });
        isView.value = !!data?.isView;
        fileList.value = [];
        uploadResponses.value = [];

        // 确保人员列表已加载
        if (personOptions.value.length === 0) {
          loadPersonOptions();
        }

        if (data?.record) {
          record.value = data.record;
          // 预填充表单数据
          setFieldsValue({
            id: data.record.id,
            handleStatus: '1', // 默认设置为已处置
            handler: data.record.handler || '', // 处置人
            handleTime: data.record.handleTime ? new Date(data.record.handleTime) : new Date(), // 处置时间
            handleProcess: data.record.handleProcess || '', // 处置过程
          });

          // 设置表单标题和按钮
          setProps({
            showSubmitButton: !isView.value,
            submitButtonOptions: {
              text: '保存',
            },
          });

          // 如果有附件数据，解析并设置到fileList
          if (data.record.attachment) {
            try {
              let attachmentData;

              // 尝试解析JSON字符串
              if (typeof data.record.attachment === 'string') {
                try {
                  attachmentData = JSON.parse(data.record.attachment);
                } catch (e) {
                  console.warn('附件数据不是有效的JSON格式', e);
                  attachmentData = null;
                }
              } else if (Array.isArray(data.record.attachment)) {
                // 如果已经是数组，直接使用
                attachmentData = data.record.attachment;
              }

              if (Array.isArray(attachmentData) && attachmentData.length > 0) {
                console.log('处理附件数据:', attachmentData);

                // 格式化文件列表
                fileList.value = attachmentData.map((item) => ({
                  uid: item.fileId || item.id || Math.random().toString(36).substring(2),
                  name: item.fileName || '未命名文件',
                  status: 'done',
                  url: item.url || '',
                  response: item,
                }));

                // 保存到响应数据
                uploadResponses.value = attachmentData.map((item) => ({
                  fileId: item.fileId || item.id || '',
                  id: item.id || item.fileId || '',
                  fileName: item.fileName || '未命名文件',
                  url: item.url || '',
                  thumbUrl: item.thumbUrl || '',
                }));

                console.log('设置文件列表:', fileList.value);
              }
            } catch (e) {
              console.error('附件数据解析失败', e);
              // 如果不是有效的JSON，可能是旧版单个文件URL
              if (
                typeof data.record.attachment === 'string' &&
                data.record.attachment.match(/^https?:\/\//)
              ) {
                const fileUrl = data.record.attachment;
                fileList.value = [
                  {
                    uid: '1',
                    name: '附件文件',
                    status: 'done',
                    url: fileUrl,
                    response: {
                      fileId: '1',
                      id: '1',
                      fileName: '附件文件',
                      url: fileUrl,
                    },
                  },
                ];
                uploadResponses.value = [
                  {
                    fileId: '1',
                    id: '1',
                    fileName: '附件文件',
                    url: fileUrl,
                  },
                ];
              }
            }
          }
        }
      });

      const getTitle = computed(() => {
        return isView.value ? '处置记录' : '新增处置';
      });

      // 处理上传状态变化
      function onUploadingChange(uploading: boolean) {
        isUploading.value = uploading;
        console.log('文件上传状态变化:', uploading);
      }

      // 处理上传成功
      function onUploadSuccess(data: any) {
        console.log('文件上传成功:', data);

        // 将后端返回的完整响应添加到响应集合中
        if (data && data.response) {
          // 检查是否已存在相同ID的响应（避免重复）
          const exists = uploadResponses.value.some(
            (item) =>
              (item.fileId && item.fileId === data.response.fileId) ||
              (item.id && item.id === data.response.id),
          );

          if (!exists) {
            uploadResponses.value.push(data.response);
            console.log('添加新文件响应到集合，当前共有', uploadResponses.value.length, '个文件');
          }
        }
      }

      // 处理文件删除
      function onFileRemove(file: any) {
        console.log('删除文件:', file);
        if (file && (file.response || file.fileId || file.id)) {
          // 获取文件ID
          const fileId = file.response?.fileId || file.response?.id || file.fileId || file.id;

          // 从响应数组中移除
          if (fileId) {
            uploadResponses.value = uploadResponses.value.filter(
              (item) => item.fileId !== fileId && item.id !== fileId,
            );

            console.log('文件已从响应列表中移除，剩余文件数:', uploadResponses.value.length);
          }
        }
        return true; // 允许删除
      }

      function handleDownload() {
        if (record.value.attachment) {
          try {
            const attachmentData = JSON.parse(record.value.attachment);
            if (Array.isArray(attachmentData) && attachmentData.length > 0) {
              // 打开第一个附件，实际项目中可以提供列表选择
              window.open(attachmentData[0].url);
            } else {
              createMessage.warning('附件数据格式不正确');
            }
          } catch (e) {
            // 如果不是JSON格式，可能是单个URL
            window.open(record.value.attachment);
          }
        } else {
          createMessage.warning('没有可下载的附件');
        }
      }

      // 暂存函数
      async function handleDraft() {
        try {
          const values = await validate();
          draftLoading.value = true;
          confirmLoading.value = true;
          setModalProps({ confirmLoading: true });

          values.id = record.value.id;
          values.handleStatus = '2';

          if (uploadResponses.value.length > 0) {
            const formattedAttachments = uploadResponses.value.map((file) => ({
              fileId: file.fileId || file.id || '',
              id: file.id || file.fileId || '',
              fileName: file.fileName || file.name || '未命名文件',
              url: file.url || '',
              thumbUrl: file.thumbUrl || '',
            }));

            values.attachment = JSON.stringify(formattedAttachments);
          }

          await processWarning(values);

          createMessage.success('暂存成功！');
          closeModal();
          emit('success');
        } catch (error) {
          console.error('暂存失败', error);
          createMessage.error('暂存失败，请重试');
        } finally {
          confirmLoading.value = false;
          draftLoading.value = false;
          setModalProps({ confirmLoading: false });
        }
      }

      async function handleSubmit() {
        if (isView.value) {
          closeModal();
          return;
        }

        try {
          const values = await validate();
          saveLoading.value = true;
          confirmLoading.value = true;
          setModalProps({ confirmLoading: true });

          values.id = record.value.id;
          values.handleStatus = '1';

          if (uploadResponses.value.length > 0) {
            const formattedAttachments = uploadResponses.value.map((file) => ({
              fileId: file.fileId || file.id || '',
              id: file.id || file.fileId || '',
              fileName: file.fileName || file.name || '未命名文件',
              url: file.url || '',
              thumbUrl: file.thumbUrl || '',
            }));

            values.attachment = JSON.stringify(formattedAttachments);
            console.log('提交处置记录，附件数据:', values.attachment);
          }

          await processWarning(values);

          createMessage.success('处置成功！');
          closeModal();
          emit('success');
        } catch (error) {
          console.error('处置报警失败', error);
          createMessage.error('处置报警失败，请重试');
        } finally {
          confirmLoading.value = false;
          saveLoading.value = false;
          setModalProps({ confirmLoading: false });
        }
      }

      return {
        registerModal,
        registerForm,
        getTitle,
        handleSubmit,
        handleDraft,
        record,
        isView,
        fileList,
        handleDownload,
        isUploading,
        onUploadingChange,
        onUploadSuccess,
        onFileRemove,
        uploaderRef,
        confirmLoading,
        draftLoading,
        saveLoading,
        closeModal: () => closeModal(),
      };
    },
  });
</script>

<style lang="less" scoped>
  .record-box {
    display: flex;
    align-items: center;
    padding: 0px 20px;

    .record-item {
      display: flex;
      align-items: center;
      min-width: 50%;

      .record-label {
        font-weight: bold;
        width: 80px;
        height: 32px;
        line-height: 32px;
        text-align: right;
        padding: 0 8px;
      }

      .record-content {
        border: 1px solid #d9d9d9;
        border-radius: 4px;
        height: 32px;
        background-color: #fafafa;
        width: calc(100% - 80px);
        line-height: 32px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        padding: 0px 11px;
      }
    }
  }

  .record-boxs {
    display: flex;
    align-items: flex-start;
    padding: 0px 20px;

    .trigger-label {
      font-weight: bold;
      width: 80px;
      text-align: right;
      padding: 0 8px;
    }

    .trigger-content {
      width: calc(100% - 80px);
      padding: 8px;
      border: 1px solid #d9d9d9;
      border-radius: 4px;
      min-height: 60px;
      background-color: #fafafa;
    }
  }

  .attachment-label {
    font-weight: bold;
    margin-bottom: 8px;
  }

  .upload-info {
    display: flex;
    align-items: center;
    padding: 8px 12px;
    background-color: #e6f7ff;
    border-radius: 4px;
    margin-bottom: 12px;

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

  .upload-container {
    border-radius: 4px;
    padding: 16px;
    margin-bottom: 12px;

    &:hover {
      border-color: #1890ff;
    }
  }

  .file-stats {
    display: flex;
    align-items: center;
    margin-top: 12px;
    padding: 8px 12px;
    background-color: #f1f1f1;
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
        font-weight: 500;
      }
    }
  }

  .attachment-preview {
    padding: 8px;
  }

  .modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }
</style>
