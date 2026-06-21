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
    :canFullscreen="true"
    :footer="isView ? null : undefined"
  >
    <div style="padding: 10px">
      <div class="record-box">
        <div v-for="(item, index) in descSchema" class="record-item" :key="item.field + index">
          <div class="record-label">{{ item.label }}</div>
          <div class="record-content" :title="record[item.field] || '---'">{{
            record[item.field] || '---'
          }}</div>
        </div>
      </div>
      <!-- <Description :data="record" :schema="descSchema" :column="2" size="middle" bordered /> -->
      <div v-if="record.triggerReason" class="record-boxs">
        <a-divider class="trigger-label">预警说明</a-divider>
        <div class="trigger-content">{{ record.triggerReason }}</div>
      </div>
      <div class="record-boxs mt-4">
        <a-divider class="trigger-label">附件</a-divider>
        <div class="trigger-content">
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
      <div v-if="record.handleProcess" class="record-boxs mt-4">
        <a-divider class="trigger-label">处理过程</a-divider>
        <div class="trigger-content">{{ record.handleProcess }}</div>
      </div>
    </div>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, computed } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { Description, DescItem } from '@/components/swm/Description';
  import { getWarning } from '@/api/swm/warning';
  import { Icon } from '@/components/swm/Icon';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { Divider } from 'ant-design-vue';
  import { FileOutlined, InfoCircleOutlined } from '@ant-design/icons-vue';

  export default defineComponent({
    name: 'WarningModal',
    components: { BasicModal, Description, Icon, Divider, FileOutlined, InfoCircleOutlined },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const isView = ref(true);
      const record = ref<Recordable>({});
      const { createMessage } = useMessage();
      const fileList = ref<any[]>([]);
      const loading = ref(false);

      const descSchema = computed((): DescItem[] => {
        return [
          {
            field: 'personName',
            label: '人员姓名',
          },
          {
            field: 'warningType',
            label: '报警性质',
          },
          {
            field: 'warningContent',
            label: '报警类型',
          },
          {
            field: 'warningTime',
            label: '报警时间',
          },
          {
            field: 'alarmRecord',
            label: '报警记录',
          },
          {
            field: 'alarmTime',
            label: '报警时间',
          },
          {
            field: 'handler',
            label: '处理人',
          },
          {
            field: 'handleTime',
            label: '处理时间',
          },
          {
            field: 'location',
            label: '位置',
          },
          {
            field: 'area',
            label: '区域',
          },
          // {
          //   field: 'remarks',
          //   label: '备注',
          // },
        ];
      });

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        isView.value = !!data?.isView;
        setModalProps({ confirmLoading: false });
        record.value = {};
        fileList.value = [];
        loading.value = true;

        try {
          if (data?.record) {
            if (data.record.id) {
              try {
                setModalProps({ confirmLoading: true });
                const warningData = await getWarning(data.record.id);
                record.value = warningData;

                // 处理附件数据
                if (record.value.attachment) {
                  handleAttachmentData(record.value.attachment);
                }
              } catch (error) {
                console.error('获取报警详情失败', error);
                createMessage.error('获取报警详情失败');
              } finally {
                setModalProps({ confirmLoading: false });
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
        }
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
            fileList.value = parsedData.map((item) => {
              const isImage =
                item.fileName?.match(/\.(jpeg|jpg|gif|png)$/i) ||
                item.url?.match(/\.(jpeg|jpg|gif|png)$/i);

              // 处理URL，确保图片使用预览URL格式
              let processedUrl = item.url || '';
              let thumbUrl = '';

              if (isImage && processedUrl) {
                processedUrl = generatePreviewUrl(processedUrl);
                thumbUrl = processedUrl;
                console.log('处理图片URL:', item.url, '->', processedUrl);
              }

              return {
                uid: item.fileId || item.id || Math.random().toString(36).substring(2),
                name: item.fileName || '未命名文件',
                status: 'done',
                url: processedUrl,
                thumbUrl: thumbUrl,
                response: item,
              };
            });

            console.log('已加载附件数量:', fileList.value.length);
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

        // 确保使用正确的预览URL格式
        let previewUrl = file.url;
        if (isImageFile(file)) {
          previewUrl = generatePreviewUrl(file.url);
          console.log('预览图片URL:', file.url, '->', previewUrl);
        }

        if (isImageFile(file)) {
          // 使用组件库的图片预览功能
          try {
            // 预览图片 - 如果你有引入预览组件的话
            window.open(previewUrl, '_blank');
          } catch (e) {
            window.open(previewUrl, '_blank');
          }
        } else {
          // 其他类型文件直接打开
          window.open(previewUrl, '_blank');
        }
      }

      const getTitle = computed(() => {
        return '报警详情';
      });

      return {
        registerModal,
        getTitle,
        isView,
        record,
        descSchema,
        fileList,
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
    align-items: center;
    padding: 0px 20px;
    flex-wrap: wrap;

    .record-item {
      display: flex;
      align-items: center;
      width: 50%;
      margin-bottom: 20px;

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

  .trigger-reason,
  .handle-process {
    padding: 12px;
    background-color: #f5f5f5;
    border-radius: 4px;
    line-height: 1.6;
    min-height: 60px;
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
