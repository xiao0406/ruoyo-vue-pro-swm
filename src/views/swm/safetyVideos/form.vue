<template>
  <BasicModal
    v-bind="$attrs"
    :showFooter="true"
    @register="registerModal"
    @ok="handleSubmit"
    width="80%"
    destroyOnClose
  >
    <template #title>
      <Icon :icon="getTitle.icon" class="pr-1 m-1" />
      <span> {{ getTitle.value }} </span>
    </template>

    <div class="min-h-[400px]">
      <BasicForm @register="registerForm">
        <template #coverUrlSlot="{ model }">
          <ImagesUpload ref="imagesUploadRef" v-model="model.coverUrl" :maxCount="1" />
        </template>
        <template #fileUrlSlot="{ model }">
          <VideoUploader v-model="model.fileUrl" @success="handleVideoUploadSuccess" />
        </template>
      </BasicForm>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, nextTick } from 'vue';
  import { useI18n } from '@/hooks/swm/useI18n';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { router } from '@/router';
  import { Icon } from '@/components/swm/Icon';
  import { BasicForm, FormSchema, useForm } from '@/components/swm/Form';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import {
    swmSafetyFileManageSave,
    swmSafetyFileManageForm,
    swmSafetyFileManageJobTypeList,
  } from '@/api/swm/safetyVideos';
  import ImagesUpload from './components/imagesUpload.vue';
  import VideoUploader from './components/videoUploader.vue';
  import { log } from 'console';

  const emit = defineEmits(['success', 'register']);

  const { t } = useI18n();
  const { showMessage } = useMessage();

  const record = ref<Recordable>({});
  const imagesUploadRef = ref();

  const getTitle = computed(() => ({
    icon: router.currentRoute.value.meta.icon || 'ant-design:book-outlined',
    value: '安全教育视频',
  }));

  const inputFormSchemas: FormSchema[] = [
    {
      label: t('视频标题'),
      field: 'title',
      component: 'Input',
      required: true,
      componentProps: {
        maxlength: 120,
      },
    },
    {
      label: t('视频分类'),
      field: 'type',
      component: 'Select',
      required: true,
      componentProps: {
        dictType: 'swm_safety_type',
        allowClear: true,
      },
    },
    {
      field: 'selectAll',
      label: '是否全部',
      component: 'Switch',
      componentProps: {
        checkedValue: '1',
        unCheckedValue: '0',
        checkedChildren: '是',
        unCheckedChildren: '否',
        onChange: (value) => handleSelectAllChange(value),
      },
    },
    {
      label: t('目标工种'),
      field: 'jobType',
      component: 'Select',
      required: true,
      componentProps: {
        api: async (params) => {
          const res = await swmSafetyFileManageJobTypeList(params);
          return res.map((val) => ({ label: val, value: val }));
        },
        allowClear: true,
        mode: 'multiple',
        filterable: true,
      },
    },
    {
      label: t('推送日期'),
      field: 'pushDate',
      component: 'DatePicker',
      required: true,
      componentProps: {
        format: 'YYYY-MM-DD',
        valueFormat: 'YYYY-MM-DD',
      },
    },
    {
      label: t('教育视频'),
      field: 'fileUrl',
      component: 'Input',
      required: true,
      slot: 'fileUrlSlot',
    },
    {
      label: t('视频封面'),
      field: 'coverUrl',
      component: 'Upload',
      required: true,
      slot: 'coverUrlSlot',
    },
    {
      label: t('视频时长（秒）'),
      field: 'duration',
      component: 'InputNumber',
      show: false,
      componentProps: {
        disabled: true,
      },
    },
  ];

  const [registerForm, { resetFields, setFieldsValue, getFieldsValue, validate, updateSchema }] =
    useForm({
      labelWidth: 120,
      schemas: inputFormSchemas,
      baseColProps: { lg: 12, md: 24 },
    });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    resetFields();
    setModalProps({ loading: true });

    const res = await swmSafetyFileManageForm(data);

    record.value = res.swmSafetyFileManage || {};
    // 根据selectAll值控制目标工种的显示和必填
    if (record.value.selectAll === '1') {
      // 隐藏目标工种，设置为非必填
      updateSchema({
        field: 'jobType',
        required: false,
        ifShow: false,
      });

      // 清除目标工种的值
      setFieldsValue({ jobType: undefined });
    } else {
      // 显示目标工种，设置为必填
      updateSchema({
        field: 'jobType',
        required: true,
        ifShow: true,
      });
    }
    // 使用setFieldsValue设置表单值
    setFieldsValue(record.value);

    // 使用nextTick确保表单渲染完成
    await nextTick();
    setModalProps({ loading: false });
  });

  // 处理是否全部变更
  function handleSelectAllChange(value) {
    // 使用nextTick确保表单状态更新
    nextTick(() => {
      if (value === '1' || value === true) {
        // 选择"是"（全部）：隐藏目标工种，清除值，设置为非必填
        setFieldsValue({ ...getFieldsValue(), jobType: undefined });
        updateSchema({
          field: 'jobType',
          required: false,
          ifShow: false,
        });
      } else {
        // 选择"否"（非全部）：显示目标工种，设置为必填
        updateSchema({
          field: 'jobType',
          required: true,
          ifShow: true,
        });
      }
    });
  }

  // 处理视频上传成功
  function handleVideoUploadSuccess(data) {
    // 设置视频时长
    if (data.duration) {
      setFieldsValue({ ...getFieldsValue(), duration: data.duration });
    }

    // 如果没有封面，设置默认封面
    const formValues = getFieldsValue();
    const currentCover = formValues.coverUrl;

    if ((!currentCover || currentCover === '' || currentCover === '[]') && data.thumbnail) {
      // 将base64转换为File对象
      const byteString = atob(data.thumbnail.split(',')[1]);
      const mimeString = data.thumbnail.split(',')[0].split(':')[1].split(';')[0];
      const ab = new ArrayBuffer(byteString.length);
      const ia = new Uint8Array(ab);
      for (let i = 0; i < byteString.length; i++) {
        ia[i] = byteString.charCodeAt(i);
      }
      const blob = new Blob([ab], { type: mimeString });
      const file = new File([blob], 'video_thumbnail.jpg', { type: mimeString });

      // 直接调用ImagesUpload组件的beforeUpload方法
      if (imagesUploadRef.value && imagesUploadRef.value.beforeUpload) {
        imagesUploadRef.value.beforeUpload(file);
      }
    }
  }

  async function handleSubmit() {
    try {
      const formData = await validate();
      setModalProps({ loading: true });
      const params = {
        ...record.value,
        ...formData,
      };

      const res = await swmSafetyFileManageSave(params);
      showMessage(res.message);
      setTimeout(closeModal);
      emit('success');
    } catch (error: any) {
      if (error && error.errorFields) {
        showMessage(t('您填写的信息有误，请根据提示修正。'));
      }
      console.log('error', error);
    } finally {
      setModalProps({ loading: false });
    }
  }
</script>
