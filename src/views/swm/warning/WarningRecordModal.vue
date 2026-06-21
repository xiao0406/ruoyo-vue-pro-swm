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
    :canFullscreen="false"
    :footer="null"
  >
    <div style="padding: 10px">
      <!-- 顶部信息 -->
      <div class="record-box mb-3">
        <div class="record-item">
          <div class="record-label">报警记录</div>
          <div class="record-content">{{ record.personName || '---' }}</div>
        </div>
        <div class="record-item">
          <div class="record-label">报警时间</div>
          <div class="record-content">{{ record.alarmTime || '---' }}</div>
        </div>
      </div>

      <!-- 触发原因 -->
      <div class="mb-3">
        <div class="trigger-label">触发原因</div>
        <div class="trigger-content">{{ record.triggerReason || '---' }}</div>
      </div>

      <!-- 处置信息 -->
      <div class="process-box mb-3">
        <div class="process-row">
          <div class="process-item">
            <div class="process-label">处置人</div>
            <div class="process-content">{{ record.handler || '---' }}</div>
          </div>
          <div class="process-item">
            <div class="process-label">处置时间</div>
            <div class="process-content">{{ record.handleTime || '---' }}</div>
          </div>
        </div>
      </div>

      <!-- 处置过程 -->
      <div class="mb-3">
        <div class="process-label">处置过程</div>
        <div class="process-detail">{{ record.handleProcess || '---' }}</div>
      </div>

      <!-- 附件 -->
      <div class="mt-4">
        <div class="attachment-label">附件</div>
        <div class="upload-container read-only">
          <!-- 只读模式下的文件预览 -->
          <div v-if="fileList.length > 0" class="file-preview-list">
            <div
              v-for="file in fileList"
              :key="file.uid"
              class="file-item"
              @click="handleFilePreview(file)"
            >
              <!-- 图片文件 -->
              <div v-if="isImageFile(file)" class="file-thumbnail">
                <img :src="file.url || file.thumbUrl" alt="缩略图" />
              </div>
              <!-- 非图片文件 -->
              <div v-else class="file-icon">
                <FileOutlined />
                <div class="file-type">{{ getFileType(file) }}</div>
              </div>
              <div class="file-name" :title="file.name">{{ file.name }}</div>
            </div>
          </div>

          <div class="file-stats" v-if="fileList.length > 0">
            <div class="stat-item">
              <FileOutlined />
              <span>共 {{ fileList.length }} 个文件</span>
            </div>
          </div>

          <!-- 无附件时的显示 -->
          <div v-if="fileList.length === 0" class="empty-attachment">
            <InfoCircleOutlined />
            <span>无附件</span>
          </div>
        </div>
      </div>
    </div>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, computed } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { getWarning } from '@/api/swm/warning';
  import { Icon } from '@/components/swm/Icon';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { FileUploader } from '@/views/swm/components';
  import { InfoCircleOutlined, FileOutlined } from '@ant-design/icons-vue';

  export default defineComponent({
    name: 'WarningRecordModal',
    components: { BasicModal, Icon, FileUploader, InfoCircleOutlined, FileOutlined },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const record = ref<Recordable>({});
      const { createMessage } = useMessage();
      const fileList = ref<any[]>([]);
      const loading = ref(false);
      const uploaderRef = ref<any>(null);

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: false });
        record.value = {};
        fileList.value = [];
        loading.value = true;

        try {
          if (data?.record) {
            if (data.record.id) {
              try {
                const warningData = await getWarning(data.record.id);
                record.value = warningData;

                // 处理附件数据
                if (record.value.attachment) {
                  try {
                    let attachmentData;

                    // 尝试解析JSON字符串
                    if (typeof record.value.attachment === 'string') {
                      attachmentData = JSON.parse(record.value.attachment);
                    } else if (Array.isArray(record.value.attachment)) {
                      // 如果已经是数组，直接使用
                      attachmentData = record.value.attachment;
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

                      console.log('设置文件列表:', fileList.value);
                    }
                  } catch (e) {
                    console.error('附件数据解析失败', e);
                    // 如果不是有效的JSON，可能是旧版单个文件URL
                    if (
                      typeof record.value.attachment === 'string' &&
                      record.value.attachment.match(/^https?:\/\//)
                    ) {
                      const fileUrl = record.value.attachment;
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
                    }
                  }
                }
              } catch (error) {
                console.error('获取报警详情失败', error);
                createMessage.error('获取报警详情失败');
              }
            } else {
              record.value = data.record;

              // 处理附件数据
              if (data.record.attachment) {
                handleAttachmentData(data.record.attachment);
              }
            }
          }
        } catch (error) {
          console.error('处理数据错误', error);
        } finally {
          loading.value = false;
          setModalProps({ confirmLoading: false });
        }
      });

      // 处理附件数据
      function handleAttachmentData(attachmentData) {
        try {
          let parsedData;

          if (typeof attachmentData === 'string') {
            try {
              parsedData = JSON.parse(attachmentData);
            } catch (e) {
              // 如果不是JSON，可能是单个URL
              if (attachmentData.match(/^https?:\/\//)) {
                parsedData = [
                  {
                    fileId: '1',
                    id: '1',
                    fileName: '附件文件',
                    url: attachmentData,
                  },
                ];
              } else {
                console.error('无法解析附件数据', e);
                return;
              }
            }
          } else if (Array.isArray(attachmentData)) {
            parsedData = attachmentData;
          }

          if (Array.isArray(parsedData) && parsedData.length > 0) {
            fileList.value = parsedData.map((item) => ({
              uid: item.fileId || item.id || Math.random().toString(36).substring(2),
              name: item.fileName || '未命名文件',
              status: 'done',
              url: item.url || '',
              response: item,
            }));
          }
        } catch (e) {
          console.error('处理附件数据错误', e);
        }
      }

      // 判断是否为图片文件
      function isImageFile(file) {
        if (!file) return false;
        const url = file.url || file.thumbUrl || '';
        return /\.(jpg|jpeg|png|gif|bmp|webp)$/i.test(url);
      }

      // 获取文件类型
      function getFileType(file) {
        if (!file) return 'FILE';
        const url = file.url || '';
        const match = url.match(/\.([^.]+)$/);
        return match ? match[1].toUpperCase() : 'FILE';
      }

      // 处理文件预览
      function handleFilePreview(file) {
        if (!file || !file.url) {
          createMessage.warning('无法预览该文件');
          return;
        }

        if (isImageFile(file)) {
          // 使用组件库的图片预览功能
          try {
            // 预览图片 - 如果你有引入预览组件的话
            window.open(file.url, '_blank');
          } catch (e) {
            window.open(file.url, '_blank');
          }
        } else {
          // 其他类型文件直接打开
          window.open(file.url, '_blank');
        }
      }

      const getTitle = computed(() => {
        return '处置记录';
      });

      return {
        registerModal,
        getTitle,
        record,
        fileList,
        loading,
        uploaderRef,
        isImageFile,
        getFileType,
        handleFilePreview,
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
    margin-bottom: 8px;
  }

  .upload-container {
    border: 1px dashed #d9d9d9;
    border-radius: 4px;
    padding: 16px;
    background-color: #fafafa;
    margin-bottom: 12px;

    &.read-only {
      border-style: solid;
      border-color: #e8e8e8;
    }
  }

  // 文件预览列表样式
  .file-preview-list {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    margin-bottom: 16px;

    .file-item {
      width: 120px;
      height: 140px;
      border: 1px solid #e8e8e8;
      border-radius: 8px;
      overflow: hidden;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
        border-color: #1890ff;
      }

      .file-thumbnail {
        width: 100%;
        height: 100px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #f5f5f5;
        overflow: hidden;

        img {
          max-width: 100%;
          max-height: 100%;
          object-fit: contain;
        }
      }

      .file-icon {
        width: 100%;
        height: 100px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        background-color: #f5f5f5;
        font-size: 36px;
        color: #1890ff;

        .file-type {
          font-size: 12px;
          margin-top: 8px;
          color: #666;
        }
      }

      .file-name {
        padding: 8px;
        font-size: 12px;
        text-align: center;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
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

  .empty-attachment {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
    color: #999;

    .anticon {
      margin-right: 8px;
      font-size: 16px;
    }
  }
</style>
