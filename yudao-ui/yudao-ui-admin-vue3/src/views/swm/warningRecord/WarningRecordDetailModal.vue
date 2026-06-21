<!--
  @author zwf
  @date 2025-05-16
-->
<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    :width="800"
    :height="600"
    :canFullscreen="true"
    :okText="isView ? undefined : '保存'"
    :cancelText="isView ? '关闭' : '取消'"
    :footer="isView ? null : undefined"
    @ok="handleSubmit"
    class="detail-modal"
  >
    <!-- 自定义底部按钮 - 添加暂存按钮 -->
    <template v-if="!isView" #footer>
      <div class="modal-footer">
        <a-button @click="closeModal">{{ '取消' }}</a-button>
        <a-button type="primary" ghost @click="handleDraft" :loading="draftLoading || false">{{
          '暂存'
        }}</a-button>
        <a-button type="primary" @click="handleSubmit" :loading="saveLoading || false">{{
          '保存'
        }}</a-button>
      </div>
    </template>
    <div style="padding: 14px">
      <!-- 顶部信息 -->
      <div class="record-box mb-3">
        <div class="record-item">
          <div class="record-label">报警记录</div>
          <div class="record-content">{{ record.warningRecord || '---' }}</div>
        </div>
        <div class="record-item">
          <div class="record-label">报警时间</div>
          <div class="record-content">{{ record.alarmTime || '---' }}</div>
        </div>
      </div>

      <!-- 触发原因 -->
      <div class="mb-3">
        <div class="trigger-label">触发原因</div>
        <div class="trigger-content">{{ record.triggerReason || record.remarks || '---' }}</div>
      </div>

      <!-- 处置信息 -->
      <div class="process-box mb-3">
        <div class="process-row">
          <div class="process-item">
            <div class="process-label">处置人</div>
            <!-- 编辑模式使用下拉选择器 -->
            <template v-if="!isView">
              <a-select
                v-model:value="form.handler"
                placeholder="请选择处置人"
                :options="personOptions"
                show-search
                :filter-option="filterPersonOption"
                style="width: 100%"
              />
            </template>
            <!-- 查看模式使用只读区域 -->
            <template v-else>
              <div class="process-content">{{ record.handler || '---' }}</div>
            </template>
          </div>
          <div class="process-item">
            <div class="process-label">处置时间</div>
            <!-- 编辑模式使用时间选择器 -->
            <template v-if="!isView">
              <input
                type="datetime-local"
                v-model="form.handleTimeString"
                class="ant-input"
                style="width: 100%"
              />
            </template>
            <!-- 查看模式使用只读区域 -->
            <template v-else>
              <div class="process-content">{{ record.handleTime || '---' }}</div>
            </template>
          </div>
        </div>
      </div>

      <!-- 处置过程 -->
      <div class="mb-3">
        <div class="process-label">处置过程</div>
        <!-- 编辑模式使用多行文本框 -->
        <template v-if="!isView">
          <a-textarea v-model:value="form.handleProcess" placeholder="请输入处置过程" :rows="4" />
        </template>
        <!-- 查看模式使用只读区域 -->
        <template v-else>
          <div class="process-detail">{{ record.handleProcess || '---' }}</div>
        </template>
      </div>

      <!-- 附件 -->
      <div class="mt-4">
        <div class="attachment-label">附件</div>
        <!-- 编辑模式显示文件上传组件 -->
        <template v-if="!isView">
          <div class="upload-info">
            <InfoCircleOutlined />
            <span>支持图片、文档等格式，最多上传5个文件，单个文件不超过10MB</span>
          </div>
          <div class="upload-container">
            <FileUploader
              v-model="fileList"
              :recordId="form.id"
              :loading="isUploading"
              :maxFiles="5"
              :maxSize="10"
              uploadUrl="/js/swm/fileUpload/upload"
              accept=".jpg,.jpeg,.png,.pdf,.doc,.docx,.xls,.xlsx"
              @uploading-change="onUploadingChange"
              @upload-success="onUploadSuccess"
              @remove="onFileRemove"
              ref="uploaderRef"
            />
          </div>
        </template>
        <!-- 查看模式显示文件预览区 -->
        <template v-else>
          <div class="attachment-container">
            <!-- 文件预览区域 -->
            <div class="attachment-preview" v-if="attachmentList.length > 0">
              <div class="file-item" v-for="file in attachmentList" :key="file.fileId || file.id">
                <!-- 图片文件显示缩略图 -->
                <div
                  class="file-preview"
                  v-if="isImageFile(file.url || file.fileName)"
                  @click="previewFile(file)"
                >
                  <img :src="generatePreviewUrl(file.url || file.thumbUrl)" alt="文件预览" />
                </div>
                <!-- 非图片文件显示图标 -->
                <div class="file-preview file-icon" v-else @click="previewFile(file)">
                  <FileOutlined />
                  <div class="file-type">{{ getFileType(file.url || file.fileName) }}</div>
                </div>
                <div class="file-name" :title="file.fileName">{{ file.fileName }}</div>
              </div>
            </div>
            <div class="attachment-empty" v-else>
              <div class="empty-text">该记录没有附件</div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, watch, reactive } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { getWarningRecord, saveWarningRecord } from '@/api/swm/warningRecord';
  import { Icon } from '@/components/swm/Icon';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { FileOutlined, InfoCircleOutlined } from '@ant-design/icons-vue';
  import { createImgPreview } from '@/components/swm/Preview';
  import { FileUploader } from '@/views/swm/components';
  import dayjs from 'dayjs';
  import { getTypeOnePersonList } from '@/api/swm/person';
  import { Select } from 'ant-design-vue';

  export default defineComponent({
    name: 'WarningRecordDetailModal',
    components: {
      BasicModal,
      Icon,
      FileOutlined,
      InfoCircleOutlined,
      FileUploader,
      'a-select': Select,
      'a-select-option': Select.Option,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const record = ref<Recordable>({});
      const attachmentList = ref<any[]>([]);
      const { createMessage } = useMessage();
      const isView = ref(true); // 默认为查看模式
      const isUploading = ref(false);
      const uploaderRef = ref<any>(null);
      const fileList = ref<any[]>([]);
      const uploadResponses = ref<any[]>([]);
      const confirmLoading = ref(false);
      const saveLoading = ref(false); // 保存按钮状态
      const draftLoading = ref(false); // 暂存按钮状态
      const personOptions = ref<{ label: string; value: string }[]>([]); // 处置人员选项

      // 表单数据
      const form = reactive({
        id: '',
        warningId: '',
        handler: '',
        handleTimeString: dayjs().format('YYYY-MM-DDTHH:mm'),
        handleProcess: '',
        attachment: '',
      });

      // 下拉搜索过滤
      function filterPersonOption(input: string, option: any) {
        return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0;
      }

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

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: false });
        record.value = {};
        isView.value = data?.isView !== false; // 如果没有传入isView或为true，则为查看模式
        fileList.value = [];
        uploadResponses.value = [];

        // 加载人员列表
        loadPersonOptions();

        // 重置表单数据
        form.id = '';
        form.warningId = '';
        form.handler = '';
        form.handleTimeString = dayjs().format('YYYY-MM-DDTHH:mm');
        form.handleProcess = '';
        form.attachment = '';

        if (data?.record) {
          if (data.record.id) {
            try {
              setModalProps({ confirmLoading: true });
              const recordData = await getWarningRecord(data.record.id);
              record.value = recordData;

              // 如果是编辑模式，填充表单数据
              if (!isView.value) {
                form.id = recordData.id || '';
                form.warningId = recordData.warningId || '';
                form.handler = recordData.handler || '';
                form.handleTimeString = recordData.handleTime
                  ? dayjs(recordData.handleTime).format('YYYY-MM-DDTHH:mm')
                  : dayjs().format('YYYY-MM-DDTHH:mm');
                form.handleProcess = recordData.handleProcess || '';
                form.attachment = recordData.attachment || '';

                // 填充文件列表
                if (recordData.attachment) {
                  try {
                    let attachmentData;

                    // 尝试解析JSON字符串
                    if (typeof recordData.attachment === 'string') {
                      attachmentData = JSON.parse(recordData.attachment);
                    } else if (Array.isArray(recordData.attachment)) {
                      // 如果已经是数组，直接使用
                      attachmentData = recordData.attachment;
                    }

                    if (Array.isArray(attachmentData) && attachmentData.length > 0) {
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
                    }
                  } catch (e) {
                    console.error('附件数据解析失败', e);
                  }
                }
              }
            } catch (error) {
              console.error('获取处置记录详情失败', error);
              createMessage.error('获取处置记录详情失败');
            } finally {
              setModalProps({ confirmLoading: false });
            }
          } else {
            record.value = data.record;
            if (!isView.value) {
              form.warningId = data.record.id || data.record.warningId;
            }
          }
        }
      });

      const getTitle = computed(() => {
        return isView.value ? '处置记录详情' : '编辑处置记录';
      });

      // 生成预览URL的本地工具函数
      function generatePreviewUrl(filePath: string): string {
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
        } else if (filePath.startsWith('http://') || filePath.startsWith('https://')) {
          // 如果是完整的HTTP URL，提取路径部分
          try {
            const url = new URL(filePath);
            let pathname = url.pathname;

            // 移除开头的斜杠和可能的项目名称
            if (pathname.startsWith('/swm/')) {
              pathname = pathname.substring(5); // 移除 '/swm/'
            } else if (pathname.startsWith('/')) {
              pathname = pathname.substring(1); // 移除开头的 '/'
            }

            objectName = pathname;
            console.log('从完整URL中提取objectName:', filePath, '->', objectName);
          } catch (e) {
            console.warn('无法解析URL:', filePath);
            objectName = filePath;
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
      }

      // 处理附件数据
      watch(
        () => record.value.attachment,
        (val) => {
          if (val && isView.value) {
            try {
              // 尝试解析JSON数据
              const parsed = JSON.parse(val);
              if (Array.isArray(parsed)) {
                attachmentList.value = parsed;
              } else {
                // 如果是单个对象，转换为数组
                attachmentList.value = [parsed];
              }
            } catch (e) {
              // 如果不是JSON格式，可能是旧版本的单个文件URL
              attachmentList.value = [
                {
                  url: val,
                  fileName: '附件文件',
                },
              ];
            }
          } else {
            attachmentList.value = [];
          }
        },
        { immediate: true },
      );

      // 判断是否为图片文件
      function isImageFile(url) {
        if (!url) return false;
        return /\.(jpg|jpeg|png|gif|bmp|webp)$/i.test(url);
      }

      // 获取文件类型
      function getFileType(url) {
        if (!url) return 'FILE';
        const match = url.match(/\.([^.]+)$/);
        return match ? match[1].toUpperCase() : 'FILE';
      }

      // 预览文件
      function previewFile(file) {
        if (!file || !file.url) {
          createMessage.warning('无法预览该文件');
          return;
        }

        // 确保使用正确的预览URL格式
        let previewUrl = file.url;
        if (isImageFile(file.url)) {
          previewUrl = generatePreviewUrl(file.url);
          console.log('预览图片URL:', file.url, '->', previewUrl);
        }

        if (isImageFile(file.url)) {
          // 预览图片
          createImgPreview({
            imageList: [previewUrl],
            defaultWidth: 800,
          });
        } else {
          // 打开其他文件
          window.open(previewUrl, '_blank');
        }
      }

      // 处理上传状态变化
      function onUploadingChange(uploading: boolean) {
        isUploading.value = uploading;
        console.log('文件上传状态变化:', uploading);
      }

      // 处理上传成功
      function onUploadSuccess(data: any) {
        console.log('文件上传成功，完整响应数据:', data);

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
          }

          // 更新attachment字段，包含所有上传文件的完整响应数据
          form.attachment = JSON.stringify(uploadResponses.value);
        }
      }

      // 处理文件删除
      function onFileRemove(file: any) {
        console.log('删除文件:', file);
        if (file && (file.response || file.fileId || file.id)) {
          // 获取文件ID
          const fileId: string = (
            file.response?.fileId ||
            file.response?.id ||
            file.fileId ||
            file.id ||
            ''
          ).toString();

          // 从响应数组中移除
          if (fileId) {
            uploadResponses.value = uploadResponses.value.filter((item) => {
              const itemFileId = item.fileId || '';
              const itemId = item.id || '';
              return itemFileId !== fileId && itemId !== fileId;
            });

            // 更新attachment字段
            form.attachment =
              uploadResponses.value.length > 0 ? JSON.stringify(uploadResponses.value) : '';
          }
        }
        return true; // 允许删除
      }

      // 提交表单
      async function handleSubmit() {
        if (isView.value) return; // 查看模式不提交

        try {
          setModalProps({ confirmLoading: true });
          confirmLoading.value = true;
          saveLoading.value = true; // 设置保存按钮加载状态

          // 验证表单
          if (!form.handler) {
            createMessage.error('处置人不能为空');
            saveLoading.value = false;
            return;
          }

          if (!form.handleProcess) {
            createMessage.error('处置过程不能为空');
            saveLoading.value = false;
            return;
          }

          // 处理附件
          let finalAttachment = '';
          if (uploadResponses.value.length > 0) {
            finalAttachment = JSON.stringify(uploadResponses.value);
          } else if (form.attachment) {
            try {
              // 尝试解析当前attachment
              const parsed = JSON.parse(form.attachment);
              if (Array.isArray(parsed) && parsed.length > 0) {
                finalAttachment = form.attachment;
              }
            } catch (e) {
              console.warn('附件数据解析失败，将使用空数据');
            }
          }

          // 准备提交数据
          const formData = {
            id: form.id,
            warningId: form.warningId || '',
            handler: form.handler,
            handleTime: form.handleTimeString ? form.handleTimeString.replace('T', ' ') : '',
            handleProcess: form.handleProcess,
            attachment: finalAttachment,
            handleStatus: '1', // 已处置
            recordName: record.value.recordName,
            triggerReason: record.value.triggerReason,
            alarmTime: record.value.alarmTime,
          };

          // 调用API保存
          await saveWarningRecord(formData);
          createMessage.success('保存成功');
          closeModal();
          emit('success');
        } catch (error) {
          console.error('保存失败', error);
          createMessage.error('保存失败');
        } finally {
          setModalProps({ confirmLoading: false });
          confirmLoading.value = false;
          saveLoading.value = false;
        }
      }

      // 暂存表单
      async function handleDraft() {
        if (isView.value) return; // 查看模式不提交

        try {
          setModalProps({ confirmLoading: true });
          confirmLoading.value = true;
          draftLoading.value = true; // 设置暂存按钮加载状态

          // 验证处置人必填
          if (!form.handler) {
            createMessage.error('处置人不能为空');
            draftLoading.value = false;
            return;
          }

          // 处理附件
          let finalAttachment = '';
          if (uploadResponses.value.length > 0) {
            finalAttachment = JSON.stringify(uploadResponses.value);
          } else if (form.attachment) {
            try {
              // 尝试解析当前attachment
              const parsed = JSON.parse(form.attachment);
              if (Array.isArray(parsed) && parsed.length > 0) {
                finalAttachment = form.attachment;
              }
            } catch (e) {
              console.warn('附件数据解析失败，将使用空数据');
            }
          }

          // 准备提交数据
          const formData = {
            id: form.id,
            warningId: form.warningId || '',
            handler: form.handler,
            handleTime: form.handleTimeString ? form.handleTimeString.replace('T', ' ') : '',
            handleProcess: form.handleProcess || '',
            attachment: finalAttachment,
            handleStatus: '2', // 草稿状态
            recordName: record.value.recordName,
            triggerReason: record.value.triggerReason,
            alarmTime: record.value.alarmTime,
          };

          // 调用API保存
          await saveWarningRecord(formData);
          createMessage.success('暂存成功');
          closeModal();
          emit('success');
        } catch (error) {
          console.error('暂存失败', error);
          createMessage.error('暂存失败');
        } finally {
          setModalProps({ confirmLoading: false });
          confirmLoading.value = false;
          draftLoading.value = false;
        }
      }

      return {
        registerModal,
        closeModal,
        getTitle,
        record,
        attachmentList,
        isView,
        form,
        isImageFile,
        getFileType,
        previewFile,
        fileList,
        isUploading,
        onUploadingChange,
        onUploadSuccess,
        onFileRemove,
        handleSubmit,
        handleDraft,
        uploaderRef,
        confirmLoading,
        saveLoading,
        draftLoading,
        personOptions,
        filterPersonOption,
        generatePreviewUrl,
      };
    },
  });
</script>

<style lang="less" scoped>
  .record-box {
    display: flex;
    gap: 16px;

    .record-item {
      flex: 1;

      .record-label {
        font-weight: bold;
        margin-bottom: 8px;
      }

      .record-content {
        padding: 8px;
        border: 1px solid #d9d9d9;
        border-radius: 4px;
        min-height: 36px;
        background-color: #fafafa;
      }
    }
  }

  .trigger-label {
    font-weight: bold;
    margin-bottom: 8px;
  }

  .trigger-content {
    padding: 8px;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
    min-height: 60px;
    background-color: #fafafa;
  }

  .process-box {
    .process-row {
      display: flex;
      gap: 16px;
      margin-bottom: 16px;

      .process-item {
        flex: 1;
      }
    }
  }

  .process-label {
    font-weight: bold;
    margin-bottom: 8px;
  }

  .process-content {
    padding: 8px;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
    min-height: 36px;
    background-color: #fafafa;
  }

  .process-detail {
    padding: 8px;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
    min-height: 80px;
    background-color: #fafafa;
  }

  .attachment-label {
    font-weight: bold;
    margin-bottom: 12px;
  }

  .attachment-container {
    border: 1px solid #f0f0f0;
    border-radius: 4px;
    padding: 16px;
    background-color: #fafafa;
  }

  .upload-info {
    display: flex;
    align-items: center;
    padding: 8px 12px;
    background-color: #e6f7ff;
    border-radius: 4px;
    margin-bottom: 12px;
  }

  .attachment-preview {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;

    .file-item {
      width: 120px;
      text-align: center;
      border: 1px solid #e8e8e8;
      border-radius: 4px;
      overflow: hidden;
      background-color: white;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        border-color: #1890ff;
      }

      .file-preview {
        width: 100%;
        height: 100px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        overflow: hidden;
        background-color: #f5f5f5;

        img {
          max-width: 100%;
          max-height: 100%;
          object-fit: contain;
        }

        &.file-icon {
          font-size: 36px;
          color: #1890ff;
          flex-direction: column;

          .file-type {
            font-size: 12px;
            margin-top: 4px;
            color: #666;
          }
        }
      }

      .file-name {
        padding: 8px 4px;
        font-size: 12px;
        color: #333;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        background-color: white;
      }
    }
  }

  .attachment-empty {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 120px;
    color: #999;

    .empty-text {
      font-size: 14px;
    }
  }

  // 详情模态框样式
  :deep(.detail-modal) {
    .ant-modal {
      top: 50px;
      padding-bottom: 50px;
    }

    .ant-modal-body {
      max-height: calc(100vh - 200px);
      overflow-y: auto;
      padding: 24px;
    }

    .attachment-preview {
      max-height: 300px;
      overflow-y: auto;
      padding-right: 5px;

      &::-webkit-scrollbar {
        width: 6px;
      }

      &::-webkit-scrollbar-thumb {
        background-color: rgba(0, 0, 0, 0.2);
        border-radius: 3px;
      }
    }
  }

  // 添加底部按钮样式
  .modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }
</style>
