<template>
  <div>
    <Upload
      list-type="picture-card"
      :file-list="internalValue"
      v-loading="loading"
      :before-upload="beforeUpload"
      :customRequest="() => {}"
      :remove="handleRemove"
      multiple
      :maxCount="maxCount"
      action=""
      @preview="handlePreview"
      :disabled="disabled"
      :accept="accept"
    >
      <div v-if="!disabled && internalValue.length < maxCount">
        <Icon icon="ant-design:plus-outlined" :size="36" color="#0000001a" />
      </div>
    </Upload>

    <!-- 预览 -->
    <Modal :visible="previewVisible" :footer="null" @cancel="previewVisible = false">
      <img alt="example" style="width: 100%" :src="previewImage" />
    </Modal>
  </div>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { Upload, Modal, message } from 'ant-design-vue';
  import type { UploadProps, UploadFile } from 'ant-design-vue';
  import { Icon } from '@/components/swm/Icon';
  import { swmUploadFileApi } from '@/api/pdm/ssiipPdmUploadFiles';

  const props = defineProps({
    disabled: {
      type: Boolean,
      default: false,
    },
    maxCount: {
      type: Number,
      default: 1,
    },
    modelValue: {
      type: String,
      default: '',
    },
    accept: {
      type: String,
      default: '.jpg,.jpeg,.png,.gif',
    },
  });

  const emit = defineEmits(['change', 'update:modelValue', 'uploading', 'success']);

  const internalValue = ref<UploadFile<any>[]>([]);
  const loading = ref(false); // 控制上传按钮的 loading 状态

  const previewVisible = ref(false); // 控制图片预览的显示隐藏
  const previewImage = ref(''); // 预览图片的地址

  // 监听外部传入的 modelValue 变化
  watch(
    () => props.modelValue,
    (newVal) => {
      if (newVal) {
        try {
          // 尝试解析JSON字符串为数组
          const parsedValue = JSON.parse(newVal);
          if (Array.isArray(parsedValue)) {
            // 处理传入的数据，确保URL格式正确
            const processedData = parsedValue.map((item: UploadFile) => {
              return {
                ...item,
                url: processPreviewUrl(item.url || ''),
              };
            });
            internalValue.value = processedData;
          } else {
            internalValue.value = [];
          }
        } catch (e) {
          // 如果不是JSON格式，清空文件列表
          internalValue.value = [];
        }
      } else {
        // 如果没有值，清空文件列表
        internalValue.value = [];
      }
    },
    { immediate: true },
  );

  // 监听内部的 value 变化
  watch(
    () => internalValue.value,
    (newValue) => {
      emit('change', newValue);
      // 将数组转换为JSON字符串返回给父组件
      emit('update:modelValue', newValue.length ? JSON.stringify(newValue) : '');
    },
    { deep: true },
  );

  // 处理预览URL，将完整URL转换为预览接口格式
  function processPreviewUrl(url: string) {
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
        console.warn('无法解析URL:', url);
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

  const beforeUpload = async (file: File) => {
    // 检查文件类型
    const fileExt = file.name.slice(file.name.lastIndexOf('.') + 1).toLowerCase();
    const allowedTypes = ['jpg', 'jpeg', 'png', 'gif'];

    if (!allowedTypes.includes(fileExt)) {
      message.error('只能上传JPG/JPEG/PNG/GIF格式的图片!');
      return false;
    }

    try {
      loading.value = true;
      const res = await swmUploadFileApi({ file });
      // swmUploadFileApi返回的是完整的AxiosResponse对象，需要通过res.data访问响应数据
      const responseData = res?.data || {};
      if (responseData?.result === 'success') {
        // 处理返回的URL，确保它可以正确显示
        const processedUrl = processPreviewUrl(responseData?.url || responseData?.previewUrl || '');
        internalValue.value?.push({
          name: file?.name,
          url: processedUrl,
        } as UploadFile<any>);
      }
    } finally {
      loading.value = false;
    }
  };

  const handleRemove = (file: UploadFile) => {
    internalValue.value = internalValue.value.filter((item) => item.name !== file.name);
  };

  // 清空已选图片
  const setUploadValue = (data: UploadFile[]) => {
    // 处理传入的数据，确保URL格式正确
    const processedData = data.map((item: UploadFile) => {
      return {
        ...item,
        url: processPreviewUrl(item.url || ''),
      };
    });
    internalValue.value = processedData;
  };

  function getBase64(file: File) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => resolve(reader.result);
      reader.onerror = (error) => reject(error);
    });
  }

  // 图片预览
  const handlePreview = async (file: Exclude<UploadProps['fileList'], undefined>[number]) => {
    if (!file.url && !file.preview && file.originFileObj) {
      file.preview = (await getBase64(file.originFileObj)) as string;
    }

    previewImage.value = file.url || file.preview || '';
    previewVisible.value = true;
  };

  defineExpose({
    setUploadValue,
    beforeUpload,
  });
</script>

<style scoped lang="less">
  :deep(.ant-upload-picture-card-wrapper) {
    .ant-upload {
      width: 400px;
      height: 200px;
    }

    .ant-upload-list-picture-card-container {
      width: 400px;
      height: 200px;
    }

    .ant-upload-list-picture-card-item {
      width: 400px;
      height: 200px;
    }

    .ant-upload-list-item-image {
      width: 400px;
      height: 200px;
    }

    .ant-upload-list-picture .ant-upload-list-item-thumbnail,
    .ant-upload-list-picture-card .ant-upload-list-item-thumbnail {
      opacity: 1 !important;
    }
  }
</style>
