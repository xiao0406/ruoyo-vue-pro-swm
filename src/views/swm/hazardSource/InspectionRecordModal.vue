<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="title"
    :width="900"
    :canFullscreen="true"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <template #title>
      <Icon :icon="getTitle.icon" class="pr-1 m-1" />
      <span> {{ getTitle.value }} </span>
    </template>
    <div style="padding: 20px">
      <div class="inspection-records-container">
        <div class="header header-grid">
          <div class="header-item"
            ><strong>危险区域名称：</strong><span>{{ hazardSource?.hazardName }}</span></div
          >
          <div class="header-item"
            ><strong>危险区域位置：</strong><span>{{ hazardSource?.location }}</span></div
          >
          <div class="header-item">
            <strong>危险区域类别：</strong>
            <span>
              <template v-if="hazardSource?.hazardCategoryText">
                {{ hazardSource.hazardCategoryText }}
              </template>
              <template v-else-if="hazardSource?.hazardCategory">
                <DictLabel
                  dictType="hazard_category_enum"
                  :dictValue="hazardSource.hazardCategory"
                />
              </template>
              <template v-else>--</template>
            </span>
          </div>
          <div class="header-item">
            <strong>所属信标：</strong>
            <span>
              <template v-if="hazardSource?.beaconIdentifierText">{{
                hazardSource.beaconIdentifierText
              }}</template>
              <template v-else-if="hazardSource?.beaconIdentifier">{{
                hazardSource.beaconIdentifier
              }}</template>
              <template v-else>-</template>
            </span>
          </div>
        </div>
        <Table
          :dataSource="records"
          :columns="columns"
          :pagination="{ pageSize: 10 }"
          :loading="loading"
          :rowKey="(record) => record.id"
          :bordered="true"
        >
          <!-- 自定义状态列 -->
          <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'inspectionListStatus'">
              <Tag :color="getStatusColor(record.inspectionListStatus)">
                {{ record.inspectionListStatusText }}
              </Tag>
            </template>
            <template v-else-if="column.dataIndex === 'startTime'">
              {{ formatTime(record.startTime) }}
            </template>
            <template v-else-if="column.dataIndex === 'endTime'">
              {{ record.endTime ? formatTime(record.endTime) : '-' }}
            </template>
            <template v-else-if="column.dataIndex === 'attachmentPath'">
              <a @click.prevent="viewAttachments(record)" v-if="hasAttachments(record)">
                <FileOutlined />查看附件
              </a>
              <span v-else>无附件</span>
            </template>
          </template>
        </Table>
      </div>

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
                    <span>{{ getFileType(item.fileName) }}</span>
                  </template>
                </a-list-item-meta>
                <template #actions>
                  <a-button type="link" @click="openFile(item.url)">预览</a-button>
                </template>
              </a-list-item>
            </template>
          </a-list>
        </div>
      </a-modal>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, onMounted } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { Table, Tag, Modal, List, Button } from 'ant-design-vue';
  import { FileOutlined } from '@ant-design/icons-vue';
  import { router } from '@/router';
  import { Icon } from '@/components/swm/Icon';
  import { getHazardSourceInspectionRecords } from '@/api/swm/hazardSource';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { DictLabel } from '@/components/swm/Dict';
  const getTitle = computed(() => ({
    icon: router.currentRoute.value.meta.icon || 'ant-design:book-outlined',
    value: hazardSource.value ? `${hazardSource.value.hazardName} - 巡检记录` : '巡检记录',
  }));
  // 接口定义
  interface Attachment {
    fileId?: string;
    id?: string;
    fileName?: string;
    url?: string;
    previewUrl?: string;
    thumbUrl?: string;
  }

  interface InspectionRecord {
    id: string;
    planId: string;
    planName: string;
    planCode: string;
    inspectionType: string;
    inspectionTypeText: string;
    inspector: string;
    startTime: string;
    endTime: string;
    inspectionListStatus: string;
    inspectionListStatusText: string;
    attachmentPath: string;
    remarks: string;
  }

  // 表格列定义
  const columns = [
    {
      title: '巡检计划',
      dataIndex: 'planName',
      width: 180,
    },
    {
      title: '巡检类型',
      dataIndex: 'inspectionTypeText',
      width: 120,
    },
    {
      title: '巡检人',
      dataIndex: 'inspector',
      width: 100,
    },
    {
      title: '开始时间',
      dataIndex: 'startTime',
      width: 160,
    },
    {
      title: '结束时间',
      dataIndex: 'endTime',
      width: 160,
    },
    {
      title: '状态',
      dataIndex: 'inspectionListStatus',
      width: 100,
    },
    {
      title: '附件',
      dataIndex: 'attachmentPath',
      width: 100,
    },
    {
      title: '备注',
      dataIndex: 'remarks',
      width: 200,
    },
  ];

  const { createMessage } = useMessage();
  const loading = ref(false);
  const hazardSourceId = ref('');
  const hazardSource = ref<any>(null);
  const records = ref<InspectionRecord[]>([]);
  const title = computed(() =>
    hazardSource.value ? `${hazardSource.value.hazardName} - 巡检记录` : '巡检记录',
  );

  // 附件预览相关
  const attachmentModalVisible = ref(false);
  const attachmentModalTitle = ref('附件预览');
  const attachmentList = ref<Attachment[]>([]);

  // 格式化时间
  function formatTime(time: string) {
    if (!time) return '-';
    try {
      const date = new Date(time);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(
        date.getDate(),
      ).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(
        date.getMinutes(),
      ).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`;
    } catch (e) {
      return time;
    }
  }

  // 获取状态颜色
  function getStatusColor(status: string) {
    switch (status) {
      case '0':
        return 'blue'; // 待处理
      case '1':
        return 'orange'; // 进行中
      case '2':
        return 'green'; // 已完成
      case '3':
        return 'red'; // 已取消
      default:
        return 'default';
    }
  }

  // 判断是否有附件
  function hasAttachments(record: InspectionRecord) {
    return record.attachmentPath && record.attachmentPath !== '[]' && record.attachmentPath !== '';
  }

  // 获取文件类型
  function getFileType(fileName: string) {
    if (!fileName) return '';
    const ext = fileName.split('.').pop()?.toLowerCase();
    switch (ext) {
      case 'jpg':
      case 'jpeg':
      case 'png':
      case 'gif':
        return '图片';
      case 'pdf':
        return 'PDF文档';
      case 'doc':
      case 'docx':
        return 'Word文档';
      case 'xls':
      case 'xlsx':
        return 'Excel表格';
      case 'ppt':
      case 'pptx':
        return 'PPT演示文稿';
      case 'zip':
      case 'rar':
      case '7z':
        return '压缩文件';
      default:
        return ext ? `${ext}文件` : '未知文件';
    }
  }

  // 查看附件
  async function viewAttachments(record: InspectionRecord) {
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
        attachmentModalTitle.value = `${record.planName || '巡检任务'}的附件`;
        attachmentModalVisible.value = true;
      } else {
        createMessage.warning('没有找到附件');
      }
    } catch (error) {
      console.error('查看附件失败:', error);
      createMessage.error('查看附件失败');
    }
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

  // 关闭附件预览模态框
  function handleAttachmentModalClose() {
    attachmentModalVisible.value = false;
    attachmentList.value = [];
  }

  // 加载巡检记录
  async function loadInspectionRecords() {
    if (!hazardSourceId.value) return;

    loading.value = true;
    try {
      const res = await getHazardSourceInspectionRecords(hazardSourceId.value);
      if (res && res.success) {
        records.value = res.records || [];
        hazardSource.value = res.hazardSource || null;
      } else {
        createMessage.error(res?.message || '获取巡检记录失败');
      }
    } catch (error) {
      console.error('获取巡检记录失败:', error);
      createMessage.error('获取巡检记录失败');
    } finally {
      loading.value = false;
    }
  }

  // 模态框注册和初始化
  const [registerModal] = useModalInner((data) => {
    hazardSourceId.value = data?.hazardSourceId || '';
    loadInspectionRecords();
  });

  // 确定按钮处理函数
  function handleOk() {
    // 这里不需要特殊处理，直接关闭模态框
  }

  // 取消按钮处理函数
  function handleCancel() {
    // 这里不需要特殊处理，直接关闭模态框
  }
</script>

<style lang="less" scoped>
  .inspection-records-container {
    .header {
      margin-bottom: 16px;
      display: flex;
      flex-wrap: wrap;
      gap: 12px 32px;

      &.header-grid {
        .header-item {
          min-width: 220px;
          margin-bottom: 4px;
          display: flex;
          align-items: center;

          strong {
            min-width: 90px;
            display: inline-block;
            color: #333;
          }

          span {
            color: #555;
            word-break: break-all;
          }
        }
      }
    }
  }
</style>
