<!--
  @author zwf
  @date 2025-05-16
-->
<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :width="900"
    :canFullscreen="true"
    :maskClosable="false"
    @ok="handleSubmit"
    :okText="'保存'"
    :cancelText="'取消'"
    :confirmLoading="loading"
  >
    <!-- 自定义底部按钮区域 -->
    <template #title>
      <Icon :icon="getTitle.icon" class="pr-1 m-1" />
      <span> {{ getTitle.value }} </span>
    </template>
    <template #footer>
      <div class="modal-footer">
        <a-button @click="closeModal">取消</a-button>
        <a-button type="primary" ghost @click="handleDraft" :loading="draftLoading">暂存</a-button>
        <a-button type="primary" @click="handleSubmit" :loading="saveLoading">保存</a-button>
      </div>
    </template>
    <div style="padding: 14px">
      <div>
        <div class="form-container">
          <!-- 顶部信息 - 报警记录改为下拉搜索框，支持分页 -->
          <div class="section-row mb-3">
            <div class="record-item">
              <div class="record-label">报警记录</div>
              <a-select
                v-model:value="selectedRecordId"
                class="record-contents"
                placeholder="请选择报警记录"
                :filter-option="false"
                :loading="searchLoading"
                show-search
                @search="onSearch"
                @change="onSelectChange"
                @popup-scroll="onPopupScroll"
                :dropdown-style="{ maxHeight: '300px' }"
              >
                <a-select-option v-for="item in warningOptions" :key="item.id" :value="item.id">
                  {{
                    item.idCard
                      ? `【${item.idCard}】${item.personName}触发${item.warningContent}`
                      : `${item.personName}触发${item.warningContent}`
                  }}
                </a-select-option>
                <!-- 加载更多的提示 -->
                <a-select-option
                  v-if="hasMoreData && !searchLoading"
                  key="load-more"
                  value="load-more"
                  disabled
                  style="text-align: center; color: #1890ff"
                >
                  滚动加载更多...
                </a-select-option>
                <!-- 加载中的提示 -->
                <a-select-option
                  v-if="searchLoading && warningOptions.length > 0"
                  key="loading"
                  value="loading"
                  disabled
                  style="text-align: center"
                >
                  <ASpin size="small" /> 加载中...
                </a-select-option>
                <!-- 没有更多数据的提示 -->
                <a-select-option
                  v-if="!hasMoreData && warningOptions.length > 0 && !searchLoading"
                  key="no-more"
                  value="no-more"
                  disabled
                  style="text-align: center; color: #999"
                >
                  没有更多数据了
                </a-select-option>
              </a-select>
            </div>
            <div class="record-item">
              <div class="record-label">报警时间</div>
              <div class="record-content">{{ warningInfo.warningTime || '---' }}</div>
            </div>
          </div>

          <!-- 触发原因 - 可读区域 -->
          <div class="record-boxs mb-3">
            <div class="trigger-label">触发原因</div>
            <div class="trigger-content">{{ warningInfo.triggerReason || '---' }}</div>
          </div>

          <!-- 处置信息 - 输入区域 -->
          <div class="section-row mb-3">
            <div class="record-item">
              <div class="record-label required">处置人</div>
              <a-select
                v-model:value="form.handler"
                class="record-contents"
                placeholder="请选择处置人"
                show-search
                :filter-option="true"
                :options="personOptions"
              />
            </div>
            <div class="record-item">
              <div class="record-label">处置时间</div>
              <input
                type="datetime-local"
                class="ant-input record-contents"
                v-model="form.handleTimeString"
              />
            </div>
          </div>

          <!-- 处置过程 - 输入区域 -->
          <div class="record-boxs mb-3">
            <div class="trigger-label required">处置过程</div>
            <a-textarea
              v-model:value="form.handleProcess"
              style="margin-left: 7px"
              placeholder="请输入处置过程"
              :rows="4"
            />
          </div>

          <!-- 附件 -->
          <div class="mb-3" style="padding: 0 20px">
            <div class="record-label">附件</div>
            <div class="upload-info">
              <InfoCircleOutlined />
              <span>支持图片、文档等格式，最多上传5个文件，单个文件不超过10MB</span>
            </div>
            <div class="upload-container">
              <FileUploader
                v-model="fileList"
                :recordId="form.warningId"
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
              <div class="file-stats" v-if="fileList.length > 0">
                <div class="stat-item">
                  <FileOutlined />
                  <span>已上传 {{ fileList.length }}/5 个文件</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </BasicModal>
</template>

<script lang="ts">
  import { useI18n } from '@/hooks/swm/useI18n';
  import { defineComponent, ref, reactive, watch, onMounted, onUnmounted, computed } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { saveWarningRecord, getUnhandledWarnings } from '@/api/swm/warningRecord';
  import { getTypeOnePersonList } from '@/api/swm/person';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { InfoCircleOutlined, FileOutlined, LoadingOutlined } from '@ant-design/icons-vue';
  import { useUserStore } from '@/store/modules/user';
  import { FileUploader } from '@/views/swm/components';
  import { router } from '@/router';
  import { Icon } from '@/components/swm/Icon';
  import { Spin, Select, Input, Button } from 'ant-design-vue';
  const { t } = useI18n('msg.msgInner');

  import dayjs from 'dayjs';

  export default defineComponent({
    name: 'WarningProcessModal',
    components: {
      BasicModal,
      FileUploader,
      InfoCircleOutlined,
      FileOutlined,
      LoadingOutlined,
      ASpin: Spin,
      ASelect: Select,
      ASelectOption: Select.Option,
      AInput: Input,
      AButton: Button,
      ATextarea: Input.TextArea,
      Icon,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const getTitle = computed(() => ({
        icon: router.currentRoute.value.meta.icon || 'ant-design:book-outlined',
        value: t('新增处置'),
      }));
      const { createMessage } = useMessage();
      const loading = ref(false);
      const saveLoading = ref(false); // 保存按钮加载状态
      const draftLoading = ref(false); // 暂存按钮加载状态
      const searchLoading = ref(false); // 搜索加载状态
      const warningInfo = ref<Recordable>({});
      const userStore = useUserStore();
      const userInfo = userStore.getUserInfo || {};
      const warningOptions = ref<any[]>([]); // 报警记录下拉选项
      const selectedRecordId = ref(''); // 当前选中的记录ID
      const formRef = ref();
      const personOptions = ref<{ label: string; value: string }[]>([]); // 人员选项列表

      // 分页相关状态
      const currentPage = ref(1); // 当前页码
      const pageSize = ref(50); // 每页数量
      const hasMoreData = ref(true); // 是否还有更多数据
      const totalCount = ref(0); // 总数据量
      const currentKeyword = ref(''); // 当前搜索关键词
      const searchTimer = ref<NodeJS.Timeout | null>(null); // 搜索防抖定时器

      // 表单数据
      const form = reactive({
        warningId: '',
        handler: '',
        handleTimeString: dayjs().format('YYYY-MM-DDTHH:mm'),
        handleProcess: '',
        attachment: '',
      });

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

      // 组件销毁时清理定时器
      onUnmounted(() => {
        if (searchTimer.value) {
          clearTimeout(searchTimer.value);
          searchTimer.value = null;
        }
      });

      // 文件上传
      // 文件上传相关
      const fileList = ref<any[]>([]);
      const isUploading = ref(false);
      const uploaderRef = ref<any>(null);

      // 处理搜索 - 添加防抖优化
      function onSearch(value: string) {
        // 清除之前的定时器
        if (searchTimer.value) {
          clearTimeout(searchTimer.value);
        }

        // 设置新的防抖定时器，500ms后执行搜索
        searchTimer.value = setTimeout(async () => {
          const trimmedValue = value.trim();
          currentKeyword.value = trimmedValue;

          // 重置分页状态
          currentPage.value = 1;
          warningOptions.value = [];
          hasMoreData.value = true;

          try {
            if (currentKeyword.value === '') {
              // 如果搜索关键词为空，加载第一页数据
              await loadWarningOptions();
            } else {
              // 搜索特定关键词
              await loadWarningOptions(currentKeyword.value);
            }
          } catch (error) {
            console.error('搜索失败:', error);
            createMessage.error('搜索失败');
          }
        }, 500); // 500毫秒防抖延迟
      }

      // 处理下拉框滚动事件，实现分页加载
      async function onPopupScroll(e: Event) {
        const { target } = e;
        if (!target) return;

        const scrollTarget = target as HTMLElement;
        const { scrollTop, scrollHeight, clientHeight } = scrollTarget;

        // 当滚动到底部附近时加载更多数据
        if (
          scrollTop + clientHeight >= scrollHeight - 50 &&
          hasMoreData.value &&
          !searchLoading.value
        ) {
          currentPage.value += 1;
          await loadWarningOptions(currentKeyword.value, true);
        }
      }

      // 选择变更处理
      function onSelectChange(value: string) {
        console.log('选择变更:', value);
        const selectedRecord = warningOptions.value.find((item) => item.id === value);
        if (selectedRecord) {
          warningInfo.value = selectedRecord;
          form.warningId = selectedRecord.id;
          console.log('选择的记录:', selectedRecord);
        }
      }

      // 加载报警选项
      async function loadWarningOptions(keyword?: string, append = false) {
        console.log(
          '开始加载未处置预警列表，关键词:',
          keyword,
          '是否追加:',
          append,
          '页码:',
          currentPage.value,
        );

        searchLoading.value = true;
        // 调用API获取数据
        try {
          const res = await getUnhandledWarnings({
            keyword: keyword || '',
            pageNum: currentPage.value,
            pageSize: pageSize.value,
          });
          console.log('获取报警列表结果:', res);

          if (res && res.success) {
            totalCount.value = res.total || 0;
            const newList = res.list || [];

            if (append) {
              // 追加数据到现有列表
              warningOptions.value = [...warningOptions.value, ...newList];
            } else {
              // 替换现有列表
              warningOptions.value = newList;
            }

            // 检查是否还有更多数据
            hasMoreData.value = currentPage.value * pageSize.value < totalCount.value;

            console.log(
              '设置预警选项，当前数量:',
              warningOptions.value.length,
              '总数:',
              totalCount.value,
              '还有更多:',
              hasMoreData.value,
            );

            // 确保有默认选中（仅在第一次加载时）
            if (!append && !selectedRecordId.value && warningOptions.value.length > 0) {
              selectedRecordId.value = warningOptions.value[0].id;
              warningInfo.value = warningOptions.value[0];
              form.warningId = warningOptions.value[0].id;
              console.log('默认选中第一条记录:', warningOptions.value[0]);
            }
          } else {
            console.warn('报警列表为空或请求失败');
            if (!append) {
              warningOptions.value = [];
              totalCount.value = 0;
              hasMoreData.value = false;
            }
          }
        } catch (error) {
          console.error('加载未处置报警失败', error);
          if (!append) {
            warningOptions.value = [];
            totalCount.value = 0;
            hasMoreData.value = false;
          }
        } finally {
          searchLoading.value = false;
        }
      }

      // 模态框
      const [registerModal, { setModalProps, closeModal }] = useModalInner((data) => {
        // 先设置弹窗状态，让弹窗先显示出来
        setModalProps({ confirmLoading: false });
        resetForm();

        // 确保人员列表已加载
        if (personOptions.value.length === 0) {
          loadPersonOptions();
        }

        console.log('处置表单接收到的数据:', data);

        // 异步加载数据，不阻塞弹窗显示
        setTimeout(async () => {
          try {
            loading.value = true;

            // 加载报警选项
            await loadWarningOptions();

            // 如果传入了记录，选中该记录
            if (data?.record?.id) {
              const exists = warningOptions.value.some((item) => item.id === data.record.id);
              if (exists) {
                selectedRecordId.value = data.record.id;
                warningInfo.value = data.record;
                form.warningId = data.record.id;
                console.log('选中传入的记录:', data.record);
              }
            }
          } catch (error) {
            console.error('加载数据失败', error);
            createMessage.error('加载数据失败');
          } finally {
            loading.value = false;
          }
        }, 0);
      });

      // 重置表单
      function resetForm() {
        warningInfo.value = {};
        form.warningId = '';
        selectedRecordId.value = '';
        form.handler = '';
        form.handleTimeString = dayjs().format('YYYY-MM-DDTHH:mm');
        form.handleProcess = '';
        form.attachment = '';
        fileList.value = [];
        uploadResponses.value = []; // 重置文件响应数据

        // 重置分页状态
        currentPage.value = 1;
        currentKeyword.value = '';
        warningOptions.value = [];
        hasMoreData.value = true;
        totalCount.value = 0;

        // 清理搜索定时器
        if (searchTimer.value) {
          clearTimeout(searchTimer.value);
          searchTimer.value = null;
        }
      }

      // 处理上传状态变化
      function onUploadingChange(uploading: boolean) {
        isUploading.value = uploading;
        console.log('文件上传状态变化:', uploading);
      }

      // 存储所有上传文件的响应数据
      const uploadResponses = ref<any[]>([]);

      // 处理上传成功
      function onUploadSuccess(data: any) {
        console.log('文件上传成功，完整响应数据:', data);
        console.log('后端返回的响应数据结构:', JSON.stringify(data.response, null, 2));

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

          // 更新attachment字段，包含所有上传文件的完整响应数据
          form.attachment = JSON.stringify(uploadResponses.value);
          console.log(
            '设置attachment为所有文件的后端响应数据，文件数量:',
            uploadResponses.value.length,
          );
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

            // 更新attachment字段
            form.attachment =
              uploadResponses.value.length > 0 ? JSON.stringify(uploadResponses.value) : '';

            console.log('文件已从响应列表中移除，剩余文件数:', uploadResponses.value.length);
          }
        }
        return true; // 允许删除
      }

      // 提交表单
      async function handleSubmit() {
        // 立即显示加载状态
        saveLoading.value = true; // 使用保存专用加载状态
        setModalProps({ confirmLoading: true });

        try {
          // 已设置加载状态

          if (!form.warningId) {
            createMessage.error('报警信息不能为空');
            return;
          }

          // 手动验证表单
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
          if (fileList.value.length > 0) {
            // 获取所有已上传完成的文件
            const uploadedFiles = uploaderRef.value?.getUploadedFiles() || [];
            console.log('已上传文件:', uploadedFiles);

            // 如果有上传的文件，更新attachment字段
            if (uploadedFiles.length > 0) {
              // 合并所有文件URL，逗号分隔
              const fileUrls = uploadedFiles
                .filter((file) => file.url)
                .map((file) => file.url)
                .join(',');

              form.attachment = fileUrls;
              console.log('设置附件URL:', form.attachment);
            } else {
              console.log('附件未上传完成或上传失败');
            }
          } else {
            console.log('没有上传附件');
          }

          // 从上传组件获取所有上传文件
          const uploadedFiles = uploaderRef.value?.getUploadedFiles() || [];
          console.log('上传组件中的文件数量:', uploadedFiles.length);

          // 确保所有上传成功的文件都被包含
          if (uploadedFiles.length > 0 && uploadResponses.value.length < uploadedFiles.length) {
            console.warn('文件响应数据不完整，正在修复...');
            // 重新构建响应数据
            const allResponses = uploadedFiles.map((file) => {
              // 优先使用已存在的响应数据
              const existingResponse = uploadResponses.value.find(
                (resp) =>
                  (resp.fileId && resp.fileId === file.fileId) || (resp.id && resp.id === file.id),
              );

              if (existingResponse) {
                return existingResponse;
              }

              // 如果没有对应的响应，构建一个基础响应
              return {
                fileId: file.fileId || file.id,
                id: file.fileId || file.id,
                fileName: file.fileName,
                url: file.url,
                thumbUrl: file.thumbUrl,
              };
            });

            // 更新响应数组
            uploadResponses.value = allResponses;
          }

          // 最终检查附件数据
          let finalAttachment = '';
          if (uploadResponses.value.length > 0) {
            finalAttachment = JSON.stringify(uploadResponses.value);
            console.log('使用', uploadResponses.value.length, '个文件的响应数据');
          } else if (form.attachment) {
            try {
              // 尝试解析当前attachment
              const parsed = JSON.parse(form.attachment);
              if (Array.isArray(parsed) && parsed.length > 0) {
                finalAttachment = form.attachment;
                console.log('使用表单中已有的附件数据，包含', parsed.length, '个文件');
              }
            } catch (e) {
              console.warn('附件数据解析失败，将使用空数据');
            }
          }

          // 准备提交数据
          const formData = {
            warningId: form.warningId,
            handler: form.handler,
            handleTime: form.handleTimeString ? form.handleTimeString.replace('T', ' ') : '',
            handleProcess: form.handleProcess,
            attachment: finalAttachment, // 使用完整的后端响应JSON数据
            handleStatus: '1', // 已处置
            // 从报警信息中获取其他必要字段
            recordName: warningInfo.value.recordName,
            triggerReason: warningInfo.value.triggerReason,
            alarmTime: warningInfo.value.warningTime,
            personName: warningInfo.value.personName,
          };

          // 在控制台详细显示提交的数据
          console.log('准备提交的处置表单数据:', formData);
          console.log('附件数据(attachment):', formData.attachment);

          // 调用API保存处置记录
          await saveWarningRecord(formData);
          createMessage.success('处置成功');
          closeModal();
          emit('success');
        } catch (error) {
          console.error('保存失败', error);
          createMessage.error('处置失败');
        } finally {
          loading.value = false;
          saveLoading.value = false; // 清除加载状态
        }
      }

      // 暂存表单 - 添加暂存功能
      async function handleDraft() {
        // 立即显示加载状态
        draftLoading.value = true; // 使用暂存专用加载状态
        setModalProps({ confirmLoading: true });

        try {
          // 已设置加载状态

          if (!form.warningId) {
            createMessage.error('报警信息不能为空');
            return;
          }

          // 手动验证表单，只验证处置人
          if (!form.handler) {
            createMessage.error('处置人不能为空');
            draftLoading.value = false;
            return;
          }

          // 处理附件
          if (fileList.value.length > 0) {
            // 获取所有已上传完成的文件
            const uploadedFiles = uploaderRef.value?.getUploadedFiles() || [];

            // 如果有上传的文件，更新attachment字段
            if (uploadedFiles.length > 0) {
              // 合并所有文件URL，逗号分隔
              const fileUrls = uploadedFiles
                .filter((file) => file.url)
                .map((file) => file.url)
                .join(',');

              form.attachment = fileUrls;
            }
          }

          // 从上传组件获取所有上传文件
          const uploadedFiles = uploaderRef.value?.getUploadedFiles() || [];

          // 确保所有上传成功的文件都被包含
          if (uploadedFiles.length > 0 && uploadResponses.value.length < uploadedFiles.length) {
            // 重新构建响应数据
            const allResponses = uploadedFiles.map((file) => {
              // 优先使用已存在的响应数据
              const existingResponse = uploadResponses.value.find(
                (resp) =>
                  (resp.fileId && resp.fileId === file.fileId) || (resp.id && resp.id === file.id),
              );

              if (existingResponse) {
                return existingResponse;
              }

              // 如果没有对应的响应，构建一个基础响应
              return {
                fileId: file.fileId || file.id,
                id: file.fileId || file.id,
                fileName: file.fileName,
                url: file.url,
                thumbUrl: file.thumbUrl,
              };
            });

            // 更新响应数组
            uploadResponses.value = allResponses;
          }

          // 最终检查附件数据
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
            warningId: form.warningId,
            handler: form.handler,
            handleTime: form.handleTimeString ? form.handleTimeString.replace('T', ' ') : '',
            handleProcess: form.handleProcess || '', // 允许为空
            attachment: finalAttachment, // 使用完整的后端响应JSON数据
            handleStatus: '2', // 暂存状态为草稿
            // 从报警信息中获取其他必要字段
            recordName: warningInfo.value.recordName,
            triggerReason: warningInfo.value.triggerReason,
            alarmTime: warningInfo.value.warningTime,
            personName: warningInfo.value.personName,
          };

          // 调用API保存处置记录
          await saveWarningRecord(formData);
          createMessage.success('暂存成功');
          closeModal();
          emit('success');
        } catch (error) {
          console.error('暂存失败', error);
          createMessage.error('暂存失败');
        } finally {
          loading.value = false;
          draftLoading.value = false; // 清除加载状态
        }
      }

      return {
        registerModal,
        closeModal,
        warningInfo,
        form,
        fileList,
        loading,
        saveLoading, // 导出保存加载状态
        draftLoading, // 导出暂存加载状态
        searchLoading, // 导出搜索加载状态
        handleSubmit,
        handleDraft,
        warningOptions,
        selectedRecordId,
        onSelectChange,
        onSearch,
        onPopupScroll, // 导出滚动处理函数
        formRef,
        isUploading,
        onUploadingChange,
        onUploadSuccess,
        onFileRemove,
        uploaderRef,
        uploadResponses,
        personOptions, // 导出人员选项列表
        // 分页相关状态
        currentPage,
        pageSize,
        hasMoreData,
        totalCount,
        currentKeyword,
        searchTimer,
        getTitle,
      };
    },
  });
</script>

<style lang="less" scoped>
  .section-row {
    display: flex;
    padding: 0px 20px;

    .record-item {
      width: 50%;
      display: flex;
      align-items: center;
      justify-content: space-between;

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

      .record-contents {
        border-radius: 4px;
        height: 32px;
        background-color: #fafafa;
        width: calc(100% - 80px);
        line-height: 32px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
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
    border: 1px dashed #d9d9d9;
    border-radius: 4px;
    padding: 16px;
    background-color: #fafafa;
    margin-bottom: 12px;

    &:hover {
      border-color: #1890ff;
    }

    :deep(.ant-upload-list) {
      margin-top: 16px;
    }

    :deep(.ant-upload-list-picture-card .ant-upload-list-item) {
      padding: 4px;
      border: 1px solid #f0f0f0;
      border-radius: 4px;
      margin-right: 12px;
      margin-bottom: 12px;
    }

    :deep(.ant-upload-list-picture-card-container) {
      width: 100px;
      height: 100px;
      margin-right: 12px;
    }
  }

  .modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 16px;
  }

  .file-stats {
    margin-top: 12px;
    display: flex;

    .stat-item {
      display: flex;
      align-items: center;
      margin-right: 16px;

      .anticon {
        margin-right: 4px;
        color: #666;
      }
    }
  }
</style>
