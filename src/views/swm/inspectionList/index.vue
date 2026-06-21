<template>
  <div>
    <BasicTable @register="registerTable">
      <template #action="{ record }">
        <TableAction :actions="createActions(record)" />
      </template>
    </BasicTable>
    <InspectionListModal @register="registerModal" @success="handleSuccess" />
    <StartInspectionModal @register="registerStartModal" @success="handleSuccess" />
    <CompleteInspectionModal @register="registerCompleteModal" @success="handleSuccess" />

    <!-- 附件预览模态框 -->
    <a-modal
      v-model:visible="attachmentModalVisible"
      :title="attachmentModalTitle"
      :width="700"
      :footer="null"
      @cancel="handleAttachmentModalClose"
    >
      <div class="attachment-list">
        <a-list :dataSource="attachmentList" :bordered="true" size="small">
          <template #renderItem="{ item }">
            <a-list-item>
              <a-list-item-meta>
                <template #title>
                  <a :href="item.url" target="_blank">{{ item.fileName || '未知文件' }}</a>
                </template>
                <template #description>
                  <span>{{ item.fileType }}</span>
                </template>
              </a-list-item-meta>
              <template #actions>
                <a-button type="link" @click="openFile(item.previewUrl || item.url)">预览</a-button>
              </template>
            </a-list-item>
          </template>
        </a-list>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" setup name="ViewsSwmInspectionListIndex">
  import { ref, computed } from 'vue';
  import { BasicTable, useTable, TableAction } from '@/components/swm/Table';
  import { useModal } from '@/components/swm/Modal';
  import InspectionListModal from './InspectionListModal.vue';
  import StartInspectionModal from './StartInspectionModal.vue';
  import CompleteInspectionModal from './CompleteInspectionModal.vue';
  import { columns, searchFormSchema } from './inspectionListData';
  import {
    getInspectionListByPage,
    deleteInspectionList,
    getInspectionList,
  } from '@/api/swm/inspectionList';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { createImgPreview } from '@/components/swm/Preview';
  import { Modal, List, Button } from 'ant-design-vue';
  import { router } from '@/router';

  // 附件类型定义
  interface Attachment {
    fileId?: string;
    id?: string;
    fileName?: string;
    name?: string;
    url?: string;
    previewUrl?: string;
    thumbUrl?: string;
    fileType?: string;
  }

  const AModal = Modal;
  const AList = List;
  const AButton = Button;
  //标题和上面导航栏保持一致
  const pageTitle = computed(() => {
    // 假设你通过路由元信息定义了页面标题
    return router.currentRoute.value.meta.title || '巡检任务';
  });
  const { createMessage } = useMessage();
  const [registerModal, { openModal }] = useModal();
  const [registerStartModal, { openModal: openStartModal }] = useModal();
  const [registerCompleteModal, { openModal: openCompleteModal }] = useModal();
  const [registerTable, { reload }] = useTable({
    title: pageTitle.value,
    api: getInspectionListByPage,
    columns,
    formConfig: {
      baseColProps: { lg: 8, md: 8 },
      labelWidth: 120,
      schemas: searchFormSchema,
    },
    useSearchForm: true,
    showTableSetting: true,
    fetchSetting: {
      pageField: 'pageNo',
      sizeField: 'pageSize',
      totalField: 'count',
      listField: 'list',
    },
    actionColumn: {
      width: 180,
      title: '操作',
      dataIndex: 'action',
      slots: { customRender: 'action' },
    },
    canResize: true,
  });

  // 附件预览相关
  const attachmentModalVisible = ref(false);
  const attachmentModalTitle = ref('附件预览');
  const attachmentList = ref<Attachment[]>([]);

  // 创建操作按钮
  function createActions(record: Recordable) {
    const actions = [];

    // 巡检列表状态：0-待处理，1-进行中，2-已完成，3-已取消
    if (record.inspectionListStatus === '0') {
      // 待处理状态显示开始按钮
      actions.push({
        icon: 'ant-design:play-circle-outlined',
        tooltip: '开始',
        type: 'link',
        onClick: handleStart.bind(null, record),
      });
    } else if (record.inspectionListStatus === '1') {
      // 进行中状态显示完成按钮
      actions.push({
        icon: 'ant-design:check-circle-outlined',
        tooltip: '完成',
        type: 'link',
        onClick: handleComplete.bind(null, record),
      });
    }

    // 所有状态都显示查看按钮
    actions.push({
      icon: 'ant-design:eye-outlined',
      tooltip: '查看',
      type: 'link',
      onClick: handleView.bind(null, record),
    });

    return actions;
  }

  // 开始巡检
  function handleStart(record: Recordable) {
    openStartModal(true, {
      record,
    });
  }

  // 完成巡检
  function handleComplete(record: Recordable) {
    openCompleteModal(true, {
      record,
    });
  }

  // 查看巡检任务
  function handleView(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: false,
      isView: true, // 标记为查看模式
    });
  }

  function handleSuccess() {
    reload();
  }

  // 判断是否有附件
  function hasAttachments(record: Recordable) {
    return record.attachmentPath && record.attachmentPath !== '[]' && record.attachmentPath !== '';
  }

  // 关闭附件预览模态框
  function handleAttachmentModalClose() {
    attachmentModalVisible.value = false;
    attachmentList.value = [];
  }

  // 打开文件
  function openFile(url: string) {
    if (!url) {
      createMessage.warning('文件URL不存在');
      return;
    }

    // 处理预览URL，如果是相对路径则补全前缀
    if (url.startsWith('fileUpload/')) {
      // 完整的预览URL
      window.open('/js/swm/' + url, '_blank');
    } else {
      // 直接的文件URL
      window.open(url, '_blank');
    }
  }

  // 查看附件
  async function handleViewAttachments(record: Recordable) {
    try {
      let attachments: Attachment[] = [];

      // 直接从当前记录中提取附件信息
      if (record.attachmentPath) {
        try {
          const parsed = JSON.parse(record.attachmentPath);
          if (Array.isArray(parsed)) {
            attachments = parsed as Attachment[];
          }
        } catch (e) {
          console.warn('解析附件数据失败:', e);
        }
      }

      // 如果解析出了附件
      if (attachments.length > 0) {
        attachmentList.value = attachments;
        attachmentModalVisible.value = true;
        attachmentModalTitle.value = `${record.planName || '巡检任务'}附件列表`;
      } else {
        createMessage.warning('没有找到附件');
      }
    } catch (error) {
      console.error('查看附件失败:', error);
      createMessage.error('查看附件失败');
    }
  }

  // 获取文件类型
  function getFileType(fileName: string): string {
    if (!fileName) return '未知类型';

    const extension = fileName.split('.').pop()?.toLowerCase() || '';

    switch (extension) {
      case 'jpg':
      case 'jpeg':
      case 'png':
      case 'gif':
        return '图片';
      case 'doc':
      case 'docx':
        return 'Word文档';
      case 'xls':
      case 'xlsx':
        return 'Excel表格';
      case 'pdf':
        return 'PDF文档';
      case 'zip':
      case 'rar':
      case '7z':
        return '压缩文件';
      default:
        return '其他文件';
    }
  }
</script>

<style lang="less" scoped>
  .attachment-list {
    max-height: 400px;
    overflow-y: auto;
    margin-bottom: 16px;
  }
</style>
