/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author Shawn
 * @date 2025-05-20
 */
import { ref, computed, watch } from 'vue';
import { useI18n } from '@/hooks/swm/useI18n';
import { useMessage } from '@/hooks/swm/useMessage';
import { FormSchema, useForm } from '@/components/swm/Form';
import { useDrawerInner } from '@/components/swm/Drawer';
import {
  safetyEducationSave,
  safetyEducationForm,
  safetyEducationFileList,
} from '@/api/swm/safetyEducation';
import { StatusEnum } from '@/enums/swm/safetyEducationEnum';
import { getFormSchemas } from './schema';
import { fetchOrgTreeData, OrgTreeNode } from '@/api/swm/organizationTree';

export function useEducationForm(emit: Function) {
  const { t } = useI18n('swm.safetyEducation');
  const { showMessage } = useMessage();

  // 处理缩略图URL，将完整URL转换为预览接口格式
  function processThumbnailUrl(url: string) {
    if (!url) return '';

    // 如果已经是预览接口格式，直接返回
    if (url.startsWith('/js/swm/fileUpload/preview')) {
      return url;
    }

    // 如果是以 /js/swm/ 开头的相对路径，直接返回
    if (url.startsWith('/js/swm/')) {
      return url;
    }

    // 如果是完整的URL（包含域名和端口），提取文件路径部分
    if (url.includes('://')) {
      try {
        const urlObj = new URL(url);
        let pathname = urlObj.pathname;

        // 移除开头的斜杠和可能的项目名称
        if (pathname.startsWith('/swm/')) {
          pathname = pathname.substring(5); // 移除 '/swm/'
        } else if (pathname.startsWith('/')) {
          pathname = pathname.substring(1); // 移除开头的 '/'
        }

        // 转换为预览接口格式
        return `/js/swm/fileUpload/preview?objectName=${pathname}`;
      } catch (e) {
        console.warn('无法解析缩略图URL:', url);
        return url;
      }
    }

    // 如果是相对路径，转换为预览接口格式
    if (url.startsWith('fileUpload/')) {
      return '/js/swm/' + url;
    } else if (url.startsWith('common/') || url.includes('/')) {
      // 对于其他相对路径，转换为预览接口格式
      return `/js/swm/fileUpload/preview?objectName=${url}`;
    }

    return url;
  }
  const record = ref<any>({});
  const isView = ref(false);
  const fileList = ref<any[]>([]);
  const isLoading = ref(false);
  const fileUploaderRef = ref<any>(null);
  // 添加参与对象组件引用
  const treeSelectTransferRef = ref<any>(null);
  // 添加参与对象名称存储
  const participantsNames = ref<string[]>([]);
  // 添加文件上传中状态
  const isUploading = ref(false);

  // 记录上一次操作的信息，用于避免重复处理相同记录
  const lastOperation = ref({
    id: '',
    timestamp: 0,
  });

  // 获取抽屉标题
  const getDrawerTitle = computed(() => {
    if (isView.value) {
      return t('查看安全教育');
    }
    return record.value.isNewRecord ? t('新增安全教育') : t('编辑安全教育');
  });

  // 上传状态文本
  const getUploadingStatus = computed(() => {
    if (!isUploading.value) return '';

    const uploadingFiles = fileList.value.filter((file) => file.status === 'uploading');
    const doneFiles = fileList.value.filter((file) => file.status === 'done');
    const errorFiles = fileList.value.filter((file) => file.status === 'error');
    const totalFiles = fileList.value.length;

    // 计算完成百分比
    const donePercent =
      totalFiles > 0 ? Math.floor(((doneFiles.length + errorFiles.length) / totalFiles) * 100) : 0;

    // 状态描述
    if (uploadingFiles.length > 1) {
      return `正在上传 ${uploadingFiles.length} 个文件，已完成 ${doneFiles.length}/${totalFiles} (${donePercent}%)`;
    } else if (uploadingFiles.length === 1) {
      const currentFile = uploadingFiles[0];
      const fileName = currentFile.name || '文件';
      return `正在上传 "${fileName}"，总进度: ${doneFiles.length}/${totalFiles} (${donePercent}%)`;
    }

    return `已完成 ${doneFiles.length}/${totalFiles} (${donePercent}%)`;
  });

  // 处理参与对象变化
  function onParticipantsChange(newParticipants: any[]) {
    // 先保存当前表单的所有值
    const currentFormValues = getFieldsValue();

    // 更新表单数据中的参与对象
    setFieldsValue({
      ...currentFormValues,
      participants: newParticipants,
    });

    // 手动触发参与对象字段的校验
    setTimeout(() => {
      validateFields(['participants']).catch(() => {
        // 忽略校验错误，只是为了触发校验显示
      });
    }, 100);
  }

  // 处理参与对象名称变化
  function onParticipantsNameChange(names: string[]) {
    participantsNames.value = names;
  }

  // 处理文件列表变化
  function onFileListChange(newFileList: any[]) {
    fileList.value = newFileList;
  }

  // 处理文件上传状态变化
  function onFileUploadingChange(uploading: boolean) {
    console.log('文件上传状态变化:', uploading, '当前fileList:', fileList.value);

    // 设置上传中状态
    isUploading.value = uploading;

    // 更新抽屉按钮状态和标题
    if (uploading) {
      // 计算当前正在上传的文件数量
      const uploadingFiles = fileList.value.filter((file) => file.status === 'uploading');
      const totalFiles = fileList.value.length;
      const uploadingCount = uploadingFiles.length;

      console.log('上传中文件列表:', uploadingFiles);

      // 构建更详细的状态标题
      const statusText =
        uploadingCount > 1 ? `(${uploadingCount}个文件上传中...)` : `(文件上传中...)`;

      // 设置按钮和标题状态
      setDrawerProps({
        confirmLoading: true,
        title: record.value.isNewRecord
          ? t(`新增安全教育 ${statusText}`)
          : t(`编辑安全教育 ${statusText}`),
      });

      // 输出日志
      console.log(`当前有${uploadingCount}/${totalFiles}个文件正在上传`);
    } else {
      // 重置状态
      setDrawerProps({
        confirmLoading: false,
        title: record.value.isNewRecord ? t('新增安全教育') : t('编辑安全教育'),
      });

      // 提示上传完成
      if (fileList.value.length > 0) {
        const successFiles = fileList.value.filter((file) => file.status === 'done');
        const errorFiles = fileList.value.filter((file) => file.status === 'error');

        if (errorFiles.length === 0 && successFiles.length > 0) {
          console.log(`${successFiles.length}个文件上传完成`);
        } else if (errorFiles.length > 0) {
          console.log(`${successFiles.length}个文件上传成功，${errorFiles.length}个文件上传失败`);
        }
      }
    }
  }

  // 组织树数据
  const organizationTreeData = ref<OrgTreeNode[]>([]);

  // 添加当前选择的参与类型
  const currentParticipationType = ref<string>('1'); // 默认为班组级别

  // 处理参与类型变更
  function onParticipationTypeChange(value: string) {
    console.log('----------------------------------------');
    console.log('参与类型变更事件触发!');
    console.log('选择的参与类型值:', value);

    // 保存当前参与类型
    currentParticipationType.value = value;

    // 构造一个映射关系，方便显示
    const typeMapping = {
      '1': '班组',
      '2': '产线',
      '3': '车间',
    };

    // 获取选中类型的文本描述
    const typeText = typeMapping[value] || value;
    console.log('参与类型文本:', typeText);

    // 记录选择时间
    const selectTime = new Date().toLocaleTimeString();
    console.log('选择时间:', selectTime);

    // 更新提示文本
    let helpText = '请在左侧选择节点，点击箭头查询并添加该节点下的所有人员到右侧';
    if (value === '1') {
      helpText = '已选择班组级别，请在左侧选择具体班组，点击箭头查询并添加该班组下的所有人员到右侧';
    } else if (value === '2') {
      helpText = '已选择产线级别，请在左侧选择具体产线，点击箭头查询并添加该产线下的所有人员到右侧';
    } else if (value === '3') {
      helpText = '已选择车间级别，请在左侧选择具体车间，点击箭头查询并添加该车间下的所有人员到右侧';
    }

    // 尝试更新表单字段配置（实际效果需要测试）
    const currentValues = getFieldsValue();
    setFieldsValue({
      ...currentValues,
      _participationTypeHelpText: helpText,
    });

    // 更新树组件 - 需要清空已选项，因为层级变化可能导致不一致
    setFieldsValue({
      ...currentValues,
      participants: [],
    });

    console.log('参与类型变更处理完成');
    console.log('----------------------------------------');
  }

  // 表单初始化
  const [registerForm, { resetFields, setFieldsValue, validate, getFieldsValue, validateFields }] =
    useForm({
      labelWidth: 100,
      schemas: getFormSchemas(t, isView, onParticipationTypeChange, treeSelectTransferRef),
      baseColProps: { span: 24 },
      showActionButtonGroup: false,
    });

  // 加载已有附件
  const loadExistingFiles = async (id: string) => {
    try {
      isLoading.value = true;
      const res = await safetyEducationFileList({ id });

      if (res && Array.isArray(res)) {
        // 处理服务器返回的文件列表
        const files = res.map((file) => {
          // 判断是否为图片并获取缩略图
          let thumbUrl = file.thumbUrl || '';
          // 如果是图片且没有thumbUrl，则使用url
          if (
            !thumbUrl &&
            file.url &&
            (file.fileName?.match(/\.(jpeg|jpg|gif|png)$/i) ||
              file.url?.match(/\.(jpeg|jpg|gif|png)$/i))
          ) {
            thumbUrl = file.url;
          }

          // 处理缩略图URL，转换为预览接口格式
          if (thumbUrl) {
            thumbUrl = processThumbnailUrl(thumbUrl);
          }

          return {
            fileId: file.fileId || file.id,
            id: file.fileId || file.id,
            name: file.fileName || file.name || '未知文件',
            url: file.url || '',
            previewUrl: file.previewUrl || '',
            thumbUrl: thumbUrl,
            status: 'done',
            uid: String(file.fileId || file.id || Date.now().toString() + Math.random().toString()),
          };
        });

        // 设置文件列表
        fileList.value = files.map((f) => ({
          uid: f.uid,
          name: f.name,
          status: 'done',
          url: f.url,
          thumbUrl: f.thumbUrl, // 已经在上面处理过了
          response: {
            fileId: f.fileId,
            id: f.id,
            url: f.url,
            thumbUrl: f.thumbUrl, // 已经在上面处理过了
            previewUrl: f.previewUrl || '',
          },
        }));
      }
    } catch (error) {
      console.error('加载已有附件失败:', error);
      throw error;
    } finally {
      isLoading.value = false;
    }
  };

  // 抽屉初始化
  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner((data) => {
    console.log('抽屉接收数据:', data);

    // 检查是否是相同记录的重复操作(在短时间内)
    const currentTimestamp = data?._timestamp || 0;
    const currentId = data?.id || (data?.record ? data.record.id : '');
    const currentOperation = data?._operation || '';
    const timeDiff = currentTimestamp - lastOperation.value.timestamp;

    // 增强重复检测逻辑，同时考虑时间戳和操作类型
    if (
      (currentId && currentId === lastOperation.value.id && timeDiff > 0 && timeDiff < 1000) ||
      (currentOperation === 'create' &&
        lastOperation.value.timestamp > 0 &&
        timeDiff > 0 &&
        timeDiff < 1000)
    ) {
      console.log('检测到短时间内重复操作同一记录或创建操作，跳过重新加载数据');
      // 只确保抽屉是打开的，不重新加载数据
      setDrawerProps({
        visible: true,
        loading: false,
      });
      return;
    }

    // 记录本次操作
    lastOperation.value = {
      id: currentId,
      timestamp: currentTimestamp,
    };

    // 重置状态，防止上次操作影响本次
    resetFields();

    // 确保抽屉处于打开状态
    setDrawerProps({
      visible: true,
      loading: false,
      confirmLoading: false,
    });

    // 设置查看模式
    isView.value = !!data?.isView;

    // 处理数据 - 检查是否有时间戳，确保是新操作
    const timestamp = data?._timestamp || 0;
    console.log('当前操作时间戳:', timestamp);

    // 清空之前的数据
    record.value = {};
    fileList.value = [];

    // 处理不同类型的操作
    if (data?.isNewRecord) {
      // 新增记录
      console.log('处理新增操作');
      record.value = {
        isNewRecord: true,
        safetyStatus: StatusEnum.NOT_STARTED,
        participants: [],
      };

      // 清空参与对象相关的状态
      participantsNames.value = [];

      // 清空TreeSelectTransfer组件中的已选人员
      setTimeout(() => {
        if (
          treeSelectTransferRef.value &&
          typeof treeSelectTransferRef.value.clearSelectedPersons === 'function'
        ) {
          treeSelectTransferRef.value.clearSelectedPersons();
          console.log('已清空TreeSelectTransfer组件中的已选人员');
        }
      }, 200);
    } else if (data?.record) {
      // 已有数据，直接使用
      console.log('使用传入的完整数据:', data.record);
      record.value = { ...data.record, isNewRecord: false };

      // 处理参与对象为数组 - 支持JSON格式
      if (typeof record.value.participants === 'string') {
        try {
          // 尝试解析JSON
          if (
            record.value.participants.startsWith('[') &&
            record.value.participants.endsWith(']')
          ) {
            record.value.participants = JSON.parse(record.value.participants);
          } else {
            // 兼容旧格式：逗号分隔的字符串
            record.value.participants = record.value.participants
              .split(',')
              .map((item) => item.trim());
          }
        } catch (e) {
          console.error('解析参与对象JSON失败:', e);
          // 解析失败时，回退到逗号分隔处理
          record.value.participants = record.value.participants
            .split(',')
            .map((item) => item.trim());
        }
      } else if (!Array.isArray(record.value.participants)) {
        record.value.participants = [];
      }

      // 加载附件（如果有ID）
      if (record.value.id) {
        loadExistingFiles(record.value.id).catch((err) => {
          console.error('加载附件失败:', err);
        });
      }
    } else if (data?.id) {
      // 只有ID，需要加载详情
      console.log('根据ID加载详情:', data.id);
      setDrawerProps({ loading: true });

      // 先设置一个基本结构
      record.value = {
        id: data.id,
        isNewRecord: false,
        safetyStatus: StatusEnum.NOT_STARTED,
        participants: [],
      };

      // 设置表单初始值，避免出现未定义错误
      setFieldsValue(record.value);

      // 异步加载详情数据
      safetyEducationForm({ id: data.id })
        .then((res) => {
          if (res && res.safetyEducation) {
            const detailData = res.safetyEducation;
            record.value = { ...detailData, isNewRecord: false };

            // 处理参与对象为数组 - 支持JSON格式
            if (typeof record.value.participants === 'string') {
              try {
                // 尝试解析JSON
                if (
                  record.value.participants.startsWith('[') &&
                  record.value.participants.endsWith(']')
                ) {
                  record.value.participants = JSON.parse(record.value.participants);
                } else {
                  // 兼容旧格式：逗号分隔的字符串
                  record.value.participants = record.value.participants
                    .split(',')
                    .map((item) => item.trim());
                }
              } catch (e) {
                console.error('解析参与对象JSON失败:', e);
                // 解析失败时，回退到逗号分隔处理
                record.value.participants = record.value.participants
                  .split(',')
                  .map((item) => item.trim());
              }
            } else if (!Array.isArray(record.value.participants)) {
              record.value.participants = [];
            }

            // 更新表单数据
            setFieldsValue(record.value);

            // 加载附件
            return loadExistingFiles(data.id);
          }
        })
        .catch((err) => {
          console.error('加载表单数据失败:', err);
          showMessage(t('加载数据失败，请重试'));
        })
        .finally(() => {
          setDrawerProps({ loading: false });
        });

      // 不再等待异步请求完成
      return;
    }

    // 设置表单值
    setFieldsValue(record.value);

    // 新增时额外确保participants字段被正确重置
    if (data?.isNewRecord) {
      setTimeout(() => {
        setFieldsValue({
          participants: [],
        });
      }, 100);
    }
  });

  // 处理表单提交
  async function handleSubmit() {
    try {
      // 检查是否有文件正在上传
      if (isUploading.value || fileUploaderRef.value?.isUploading?.()) {
        showMessage({
          content: t('文件正在上传中，请等待上传完成后再保存'),
          type: 'warning',
          duration: 3,
        });
        return;
      }

      // 1. 验证表单数据
      const formData = await validate();

      // 检查是否选择了参与对象
      if (treeSelectTransferRef.value) {
        const selectedNames = treeSelectTransferRef.value.getSelectedNames?.() || [];

        // 如果没有选择参与对象，提示用户
        if (selectedNames.length === 0) {
          showMessage({
            content: '请先在右侧框中选择参与对象，确保有已选人员',
            type: 'warning',
            duration: 3,
          });
          return;
        }
      }

      // 2. 设置提交中状态
      setDrawerProps({ confirmLoading: true });

      // 3. 合并数据并准备提交
      const formValues = { ...record.value, ...formData };

      // 4. 处理参与对象字段 - 现在participants存储的是人员ID数组
      let participantsValue = '';
      let participantsNameValue = '';

      // 优先从组件引用中获取实际选中的人员数据
      let actualParticipants: string[] = [];
      let actualNames: string[] = [];

      if (treeSelectTransferRef.value) {
        try {
          // 获取选中的人员名称
          if (typeof treeSelectTransferRef.value.getSelectedNames === 'function') {
            actualNames = treeSelectTransferRef.value.getSelectedNames() || [];
          }

          // 获取选中的人员ID
          if (typeof treeSelectTransferRef.value.getSelectedValues === 'function') {
            actualParticipants = treeSelectTransferRef.value.getSelectedValues() || [];
          }

          console.log('从组件获取的人员名称:', actualNames);
          console.log('从组件获取的人员ID:', actualParticipants);
        } catch (error) {
          console.error('从组件获取选中数据失败:', error);
        }
      }

      // 如果从组件获取到了数据，优先使用
      if (actualParticipants.length > 0) {
        participantsValue = JSON.stringify(actualParticipants);
        participantsNameValue = JSON.stringify(
          actualNames.length > 0 ? actualNames : actualParticipants,
        );
      } else {
        // 回退到表单数据处理或设置为空
        participantsValue = JSON.stringify([]);
        participantsNameValue = JSON.stringify([]);

        console.warn('没有从组件获取到参与对象数据，将保存空数组');
      }

      console.log('最终的participantsValue:', participantsValue);
      console.log('最终的participantsNameValue:', participantsNameValue);

      // 获取已上传文件信息
      const uploadedFiles = fileUploaderRef.value?.getUploadedFiles() || [];

      // 处理附件信息
      let attachmentUrl = '';
      if (uploadedFiles && uploadedFiles.length > 0) {
        // 处理上传文件的base64数据，避免数据过长
        const processedFiles = uploadedFiles.map((file) => {
          // 创建一个新对象，避免修改原始对象
          const newFile = { ...file };
          // 如果thumbUrl是base64数据且过长，则截断
          if (
            newFile.thumbUrl &&
            newFile.thumbUrl.startsWith('data:') &&
            newFile.thumbUrl.length > 200
          ) {
            newFile.thumbUrl = newFile.thumbUrl.substring(0, 200) + '...';
          }
          return newFile;
        });
        attachmentUrl = JSON.stringify(processedFiles);
      }

      // 5. 准备提交数据
      const submitData = {
        theme: formValues.theme,
        safetyEducationType: formValues.safetyEducationType,
        startTime: formValues.startTime,
        participationType: formValues.participationType,
        participants: participantsValue, // 现在存储的是人员ID的JSON数组
        participantsName: participantsNameValue, // 人员姓名的JSON数组
        contentDescription: formValues.contentDescription || '',
        safetyStatus: formValues.isNewRecord
          ? StatusEnum.NOT_STARTED
          : formValues.safetyStatus || formValues.status || StatusEnum.NOT_STARTED,
        attachmentUrl: attachmentUrl,
        fileIds: uploadedFiles.map((item) => item.fileId).join(','),
      };

      // 添加日志输出（调试用）
      console.log('安全教育提交数据:', submitData);
      console.log('safetyStatus字段值:', submitData.safetyStatus);
      console.log('formValues.isNewRecord:', formValues.isNewRecord);
      console.log('formValues.safetyStatus:', formValues.safetyStatus);
      console.log('formValues.status:', formValues.status);

      const params = {
        id: formValues.id,
        isNewRecord: formValues.isNewRecord,
      };

      // 6. 提交数据
      const res = await safetyEducationSave(params, submitData);
      const result = res?.data || res;

      // 7. 处理响应结果
      if (result && (result.result === 'true' || result.code === 0)) {
        closeDrawer();
        showMessage(result.message || '保存成功');
        emit('success', true);
      } else {
        showMessage({
          content: result?.message || '保存失败，请重试',
          type: 'error',
        });
      }
    } catch (error: any) {
      console.error('提交表单时出错:', error);
      showMessage({
        content: error?.message || '提交失败，请检查表单填写是否完整',
        type: 'error',
      });
    } finally {
      setDrawerProps({ confirmLoading: false });
    }
  }

  // 处理取消操作
  function handleCancel() {
    console.log('取消操作');
    closeDrawer();
  }

  // 初始化组织树数据
  async function initOrganizationTree() {
    try {
      // 加载厂间数据 - 厂间是最高级父节点
      const result = await fetchOrgTreeData('root');
      if (result && Array.isArray(result)) {
        organizationTreeData.value = result.map((item) => ({
          ...item,
          isLeaf: false, // 标记为非叶子节点，允许展开加载子节点
        }));
      }
    } catch (error) {
      console.error('加载组织树根节点失败:', error);
    }
  }

  // 解析参与对象名称列表
  function getParticipantNamesList(participantsName: string): string[] {
    if (!participantsName) return [];

    // 首先尝试解析JSON格式的数组字符串，如 ["小勇测试安全教育"]
    if (participantsName.startsWith('[') && participantsName.endsWith(']')) {
      try {
        const parsed = JSON.parse(participantsName);
        if (Array.isArray(parsed)) {
          return parsed.map((name) => String(name).trim()).filter((name) => name.length > 0);
        }
      } catch (e) {
        console.warn('解析JSON格式的参与对象失败:', e);
      }
    }

    // 处理普通的分隔符格式：逗号、分号、换行符等
    return participantsName
      .split(/[,，;；\n\r]/)
      .map((name) => name.trim())
      .filter((name) => name.length > 0);
  }

  return {
    t,
    isView,
    record,
    fileList,
    isLoading,
    isUploading,
    fileUploaderRef,
    treeSelectTransferRef,
    participantsNames,
    getDrawerTitle,
    getUploadingStatus,
    onParticipantsChange,
    onParticipantsNameChange,
    onFileListChange,
    onFileUploadingChange,
    onParticipationTypeChange,
    currentParticipationType, // 添加当前参与类型到返回值
    registerForm,
    registerDrawer,
    handleSubmit,
    handleCancel,
    organizationTreeData,
    initOrganizationTree,
    getParticipantNamesList, // 添加解析参与对象名称列表的函数
  };
}
