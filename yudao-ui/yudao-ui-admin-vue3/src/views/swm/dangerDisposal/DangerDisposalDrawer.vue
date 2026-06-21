<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    @ok="handleSubmit"
    width="700px"
  >
    <div v-if="isLoading" class="loading-container">
      <a-spin tip="加载中..." />
    </div>
    <template v-else>
      <BasicForm @register="registerForm">
        <template #fileUpload>
          <div class="file-upload-container">
            <div class="upload-hint">
              <InfoCircleOutlined style="color: #1890ff; margin-right: 8px" />
              <span>请上传处置相关文件，支持图片、文档等多种格式</span>
            </div>
            <FileUploader
              v-model="fileList"
              :recordId="recordId || (isUpdate ? '' : hiddenDangerId)"
              :loading="false"
              :maxFiles="5"
              :maxSize="20"
              uploadUrl="/js/swm/fileUpload/upload"
              accept=".jpg,.jpeg,.png,.gif,.doc,.docx,.xls,.xlsx,.pdf,.zip,.rar,.7z"
              @uploading-change="onUploadingChange"
              @upload-success="onUploadSuccess"
              ref="uploaderRef"
            />
          </div>
        </template>
      </BasicForm>

      <div class="file-stats" v-if="fileList.length > 0">
        <div class="stat-item">
          <FileOutlined />
          <span>已上传 {{ fileList.length }}/5 个文件</span>
        </div>
      </div>
    </template>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, onMounted } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicForm, useForm } from '@/components/swm/Form';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { formSchema } from './dangerDisposalData';
  import { saveDangerDisposal, getDangerDisposal } from '@/api/swm/dangerDisposal';
  import { saveHiddenDanger, getHiddenDanger } from '@/api/swm/hiddenDanger';
  import { FileUploader } from '@/views/swm/components';
  import { FileOutlined, InfoCircleOutlined } from '@ant-design/icons-vue';
  import { getTypeOnePersonList } from '@/api/swm/person';

  const emit = defineEmits(['success', 'register']);
  const isUpdate = ref(false);
  const isView = ref(false);
  const recordId = ref('');
  const hiddenDangerId = ref('');
  const fileList = ref<any[]>([]);
  const uploaderRef = ref<any>(null);
  const isLoading = ref(false);
  const isUploading = ref(false);
  const { createMessage } = useMessage();
  const personOptions = ref<{ label: string; value: string }[]>([]); // 处置人员选项

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

        // 更新处置人员字段的选项
        updateSchema([
          {
            field: 'disposalUser',
            componentProps: {
              options: personOptions.value,
            },
          },
        ]);
      }
    } catch (error) {
      console.error('获取处置人员列表失败:', error);
      createMessage.error('获取处置人员列表失败');
    }
  }

  // 表单
  const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
    labelWidth: 100,
    schemas: formSchema,
    showActionButtonGroup: false,
  });

  // 弹窗
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    console.log('弹窗打开，参数：', data);

    try {
      resetFields();
      setModalProps({ confirmLoading: false, loading: true });
      isLoading.value = true;

      // 清空文件列表
      fileList.value = [];

      isUpdate.value = !!data?.isUpdate;
      isView.value = !!data?.isView;

      console.log('模式：', isView.value ? '查看' : isUpdate.value ? '编辑' : '新增');

      // 加载处置人员列表
      await loadPersonOptions();

      // 处理隐患ID
      if (data?.hiddenDangerId) {
        hiddenDangerId.value = data.hiddenDangerId;
        const now = new Date();
        const formattedDateTime =
          now.getFullYear() +
          '-' +
          String(now.getMonth() + 1).padStart(2, '0') +
          '-' +
          String(now.getDate()).padStart(2, '0') +
          ' ' +
          String(now.getHours()).padStart(2, '0') +
          ':' +
          String(now.getMinutes()).padStart(2, '0') +
          ':' +
          String(now.getSeconds()).padStart(2, '0');

        // 设置表单字段值
        const formData = {
          hiddenDangerId: data.hiddenDangerId,
          disposalTime: formattedDateTime, // 默认当前时间（精确到秒）
        };

        // 如果有隐患名称和隐患位置，也设置到表单中
        if (data.dangerName) {
          formData['dangerName'] = data.dangerName;
        }

        if (data.location) {
          formData['location'] = data.location;
        }

        setFieldsValue(formData);
      }

      // 处理record - 确保查看数据的加载顺序正确
      if (data?.record) {
        recordId.value = data.record.id;
        await getDetail(data.record.id);
      }

      // 处理表单字段的可编辑状态
      // 查看模式下禁用所有字段，其他模式下启用所有字段
      updateSchema(
        formSchema.map((item) => {
          return {
            field: item.field,
            componentProps: {
              disabled: isView.value,
            },
          };
        }),
      );

      console.log('表单字段已设置为', isView.value ? '禁用' : '启用');
    } catch (error) {
      console.error('初始化弹窗失败:', error);
    } finally {
      // 确保加载状态被重置
      isLoading.value = false;
      setModalProps({ loading: false });
    }
  });

  // 获取标题
  const getTitle = computed(() => {
    return isView.value ? '查看隐患处置' : isUpdate.value ? '编辑隐患处置' : '新增隐患处置';
  });

  // 处理上传状态变化
  const onUploadingChange = (uploading: boolean) => {
    isUploading.value = uploading;
    console.log('上传状态变化:', uploading);
  };

  // 处理上传成功
  const onUploadSuccess = (data: any) => {
    console.log('文件上传成功:', data);
  };

  // 加载已有附件
  const loadExistingFiles = async (responseData: any) => {
    try {
      if (responseData && responseData.attachment) {
        try {
          const attachmentData = JSON.parse(responseData.attachment);
          if (Array.isArray(attachmentData)) {
            // 处理服务器返回的文件列表
            const files = attachmentData.map((file) => ({
              uid: String(
                file.fileId || file.id || Date.now().toString() + Math.random().toString(),
              ),
              name: file.fileName || file.name || '未知文件',
              url: file.url || '',
              thumbUrl:
                file.thumbUrl ||
                (file.url &&
                (file.fileName?.match(/\.(jpeg|jpg|gif|png)$/i) ||
                  file.url?.match(/\.(jpeg|jpg|gif|png)$/i))
                  ? file.url
                  : ''),
              previewUrl: file.previewUrl || '',
              status: 'done',
              response: {
                fileId: file.fileId || file.id,
                id: file.fileId || file.id,
                url: file.url || '',
                thumbUrl: file.thumbUrl || '',
                previewUrl: file.previewUrl || '',
              },
            }));

            // 设置文件列表
            fileList.value = files;
          }
        } catch (error) {
          console.error('解析附件数据失败:', error);
        }
      }
    } catch (error) {
      console.error('加载已有附件失败:', error);
    }
  };

  // 获取详情
  async function getDetail(id: string) {
    try {
      setModalProps({ loading: true });
      isLoading.value = true;
      const res = await getDangerDisposal({ id });
      console.log('获取到的隐患处置详情:', res);

      if (res) {
        // 准备表单数据对象，确保每个字段都有值
        const formData = {
          id: res.id || '',
          hiddenDangerId: res.hiddenDangerId || '',
          dangerName: res.dangerName || '',
          location: res.location || '',
          disposalTime: res.disposalTime || '',
          disposalUser: res.disposalUser || '',
          disposalMethod: res.disposalMethod || '',
          disposalStatus: res.disposalStatus || '',
          disposalContent: res.disposalContent || '',
        };

        console.log('设置到表单的数据:', formData);

        // 使用setTimeout确保表单已经准备好接收数据
        setTimeout(() => {
          setFieldsValue(formData);
        }, 100);

        // 加载附件，直接使用获取到的响应数据
        await loadExistingFiles(res);
      }
    } catch (error) {
      console.error('获取隐患处置详情失败', error);
    } finally {
      setModalProps({ loading: false });
      isLoading.value = false;
    }
  }

  // 更新隐患状态为已处置
  async function updateHiddenDangerStatus(dangerId: string) {
    try {
      // 先获取隐患详情
      const res = await getHiddenDanger({ id: dangerId });
      if (res) {
        const hiddenDangerData = res.hiddenDanger || res;

        // 设置为已处置
        await saveHiddenDanger({
          ...hiddenDangerData,
          isHandled: '1', // 设置为已处置
        });

        console.log('已更新隐患状态为已处置');
      }
    } catch (error) {
      console.error('更新隐患状态失败', error);
    }
  }

  // 提交表单
  async function handleSubmit() {
    // 查看模式下直接关闭
    if (isView.value) {
      closeModal();
      return;
    }

    // 如果还有文件在上传中，提示用户等待
    if (isUploading.value) {
      createMessage.warning('文件正在上传中，请等待上传完成');
      return;
    }

    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });

      // 获取上传的文件列表
      let uploadedFiles: any[] = [];
      try {
        // 尝试获取上传文件，如果ref不可用则使用fileList
        if (uploaderRef.value) {
          console.log('从ref获取上传文件');
          uploadedFiles = uploaderRef.value.getUploadedFiles() || [];
        } else {
          console.log('从fileList构造上传文件');
          // 从fileList构造uploadedFiles
          uploadedFiles = fileList.value
            .filter((file) => file.status === 'done' && file.response)
            .map((file) => ({
              fileId: file.response?.fileId || file.uid,
              id: file.response?.id || file.uid,
              fileName: file.name,
              url: file.response?.url || file.url || '',
              previewUrl: file.response?.previewUrl || file.previewUrl || '',
            }));
        }
      } catch (error) {
        console.error('获取上传文件失败:', error);
        uploadedFiles = [];
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
          if (!file || !file.fileId) return false;
          if (uniqueFileIds.has(file.fileId)) {
            return false; // 跳过重复的文件
          }
          uniqueFileIds.add(file.fileId);
          return true;
        })
        .map((file) => ({
          fileId: file.fileId,
          id: file.id || file.fileId,
          fileName: file.fileName || file.name || '未命名文件',
          url: file.url || '',
          previewUrl: file.previewUrl || '',
        }));

      // 删除备注字段
      const { remarks, ...restValues } = values;

      const data = {
        ...restValues,
        fileIds: fileIds,
        attachment: JSON.stringify(cleanUploadedFiles),
      };

      if (isUpdate.value) {
        data.id = recordId.value;
      }

      // 输出提交数据
      console.log('提交的处置数据:', data);

      await saveDangerDisposal(data);

      // 更新隐患状态为已处置
      if (hiddenDangerId.value && !isUpdate.value) {
        await updateHiddenDangerStatus(hiddenDangerId.value);
      }

      createMessage.success(`${isUpdate.value ? '更新' : '新增'}隐患处置成功`);
      closeModal();
      emit('success');
    } catch (error) {
      console.error('提交隐患处置失败', error);
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>

<style lang="less" scoped>
  .loading-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 200px;
  }

  .file-upload-container {
    margin-bottom: 16px;

    .upload-hint {
      display: flex;
      align-items: center;
      background-color: #e6f7ff;
      padding: 10px;
      border-radius: 4px;
      margin-bottom: 16px;

      span {
        color: #444;
        font-size: 14px;
      }
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
</style>
