<!--
 * 场地底图管理抽屉组件
 * 功能：新增和编辑场地底图，支持上传图片并自动计算图片尺寸填充到底图尺寸字段
 * @author Shawn
 * @date 2025-06-03
-->
<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    showFooter
    :title="getTitle"
    width="50%"
    @ok="handleSubmit"
  >
    <BasicForm @register="registerForm">
      <template #url="{ model, field }">
        <div class="upload-container">
          <SiteMapUploader
            v-model="model[field]"
            :loading="isUploading"
            :recordId="recordId"
            baseUrl=""
            @uploading="onUploading"
            @success="onUploadSuccess"
            @remove="onUploadRemove"
          />
        </div>
      </template>
    </BasicForm>
  </BasicDrawer>
</template>

<script lang="ts">
  import { defineComponent, ref, computed, unref, nextTick } from 'vue';
  import { BasicForm, useForm, FormSchema } from '@/components/swm/Form/index';
  import { BasicDrawer, useDrawerInner } from '@/components/swm/Drawer';
  import { useI18n } from '@/hooks/swm/useI18n';
  import { useMessage } from '@/hooks/swm/useMessage';
  import SiteMapUploader from './SiteMapUploader.vue';
  import { createSiteMap, updateSiteMap, getCompanyOptions } from '@/api/swm/siteMap';

  export default defineComponent({
    name: 'SiteMapDrawer',
    components: { BasicDrawer, BasicForm, SiteMapUploader },
    emits: ['success', 'register', 'update:modelValue'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const { createMessage } = useMessage();
      const isUpdate = ref(false);
      const recordId = ref('');
      const isUploading = ref(false);

      // 获取公司列表数据
      const companyOptions = ref([]);

      // 获取公司列表
      async function fetchCompanyList() {
        try {
          const res = await getCompanyOptions();
          companyOptions.value = res || [];
        } catch (error) {
          console.error('获取公司列表失败:', error);
          companyOptions.value = [];
        }
      }

      // 页面加载时获取公司列表
      fetchCompanyList();

      // 记录上一次操作的信息，用于避免重复处理相同记录
      const lastOperation = ref({
        id: '',
        timestamp: 0,
      });

      // 上传状态变更
      function onUploading(status: boolean) {
        isUploading.value = status;
      }

      // 上传成功
      function onUploadSuccess(info: any) {
        // 如果需要保存fileId，可以在这里处理
        if (info && info.response && info.response.fileId) {
          // 可以将fileId保存到表单中的其他字段
          // setFieldsValue({ fileId: info.response.fileId });
        }

        // 如果是图片文件，计算图片宽度并填充到底图尺寸字段
        if (info && info.response && info.response.url) {
          const fileUrl = info.response.url;
          const fileName = info.name || '';
          // 检查是否为图片文件
          if (
            /\.(jpg|jpeg|png|gif)$/i.test(fileName.toLowerCase()) ||
            /\.(jpg|jpeg|png|gif)$/i.test(fileUrl.toLowerCase())
          ) {
            calculateImageSize(fileUrl);
          }
        }
      }

      // 计算图片尺寸
      function calculateImageSize(imageUrl: string) {
        const img = new Image();

        // 添加跨域支持
        img.crossOrigin = 'anonymous';

        img.onload = function () {
          const width = img.naturalWidth;
          const height = img.naturalHeight;
          // 填充底图尺寸字段，格式为：宽×高像素
          const sizeText = `${width}×${height}像素`;

          // 获取当前表单的所有值，然后更新相关字段，保持其他字段不变
          const currentValues = getFieldsValue();
          setFieldsValue({
            ...currentValues,
            size: sizeText,
            drawingPixelX: width,
            drawingPixelY: height,
          });
          console.log(`图片尺寸已自动填充: ${sizeText}, X: ${width}, Y: ${height}`);
        };

        img.onerror = function () {
          console.warn('无法加载图片获取尺寸信息，可能是跨域问题或图片不存在');
          // 如果加载失败，尝试不使用跨域模式重新加载
          const imgFallback = new Image();
          imgFallback.onload = function () {
            const width = imgFallback.naturalWidth;
            const height = imgFallback.naturalHeight;
            const sizeText = `${width}×${height}像素`;

            // 获取当前表单的所有值，然后更新相关字段，保持其他字段不变
            const currentValues = getFieldsValue();
            setFieldsValue({
              ...currentValues,
              size: sizeText,
              drawingPixelX: width,
              drawingPixelY: height,
            });
            console.log(`图片尺寸已自动填充(fallback): ${sizeText}, X: ${width}, Y: ${height}`);
          };
          imgFallback.onerror = function () {
            console.warn('无法获取图片尺寸，请手动填写');
          };
          imgFallback.src = imageUrl;
        };

        img.src = imageUrl;
      }

      // 删除上传
      function onUploadRemove() {}

      // 从尺寸文本中提取宽高像素值
      function parseMapSize(sizeText: string) {
        if (!sizeText) return { width: 0, height: 0 };

        // 匹配形如"1920×1080像素"或"1920x1080像素"的格式
        const regex = /(\d+)[\u00D7x×](\d+)/;
        const matches = sizeText.match(regex);

        if (matches && matches.length >= 3) {
          const width = parseInt(matches[1], 10);
          const height = parseInt(matches[2], 10);
          return { width, height };
        }

        return { width: 0, height: 0 };
      }

      const formSchema: FormSchema[] = [
        {
          field: 'name',
          label: '底图名称',
          component: 'Input',
          required: true,
          componentProps: {
            placeholder: '请输入底图名称',
          },
          rules: [
            {
              required: true,
              message: '请输入底图名称',
            },
          ],
        },
        {
          field: 'project',
          label: '所属项目',
          component: 'Select',
          required: true,
          componentProps: {
            placeholder: '请选择所属项目',
            options: companyOptions,
            fieldNames: {
              label: 'label',
              value: 'value',
            },
            getPopupContainer: () => document.body,
            dropdownStyle: { zIndex: 9999 },
          },
          rules: [
            {
              required: true,
              message: '请选择所属项目',
            },
          ],
        },
        {
          field: 'is3d',
          label: '是否是3D',
          component: 'Switch',
          componentProps: {
            checkedValue: '1',
            unCheckedValue: '0',
            checkedChildren: '是',
            unCheckedChildren: '否',
          },
        },
        {
          field: 'size',
          label: '底图尺寸',
          component: 'Input',
          componentProps: {
            placeholder: '请输入底图尺寸',
            disabled: true,
          },
        },
        {
          field: 'drawingPixelX',
          label: '图纸X像素',
          component: 'InputNumber',
          componentProps: {
            placeholder: '图纸宽度像素值',
            style: { width: '100%' },
          },
        },
        {
          field: 'drawingPixelY',
          label: '图纸Y像素',
          component: 'InputNumber',
          componentProps: {
            placeholder: '图纸高度像素值',
            style: { width: '100%' },
          },
        },
        {
          field: 'siteCoordinateXM',
          label: '场地X坐标(米)',
          component: 'InputNumber',
          componentProps: {
            placeholder: '对应实际场地X坐标(米)',
            style: { width: '100%' },
            precision: 2,
          },
        },
        {
          field: 'siteCoordinateYM',
          label: '场地Y坐标(米)',
          component: 'InputNumber',
          componentProps: {
            placeholder: '对应实际场地Y坐标(米)',
            style: { width: '100%' },
            precision: 2,
          },
        },
        {
          field: 'scale',
          label: '比例尺',
          component: 'Input',
          componentProps: {
            placeholder: '请输入比例尺',
          },
        },
        {
          field: 'url',
          label: '上传底图',
          component: 'Input',
          required: true,
          slot: 'url',
          rules: [
            {
              required: true,
              message: '请上传底图文件',
            },
          ],
        },
        {
          field: 'remarks',
          label: '备注',
          component: 'InputTextArea',
          componentProps: {
            placeholder: '请输入备注信息',
            rows: 4,
          },
        },
        // 隐藏状态字段，但保留以便提交时使用
        {
          field: 'status',
          label: '状态',
          component: 'Input',
          show: false,
          defaultValue: '禁用',
        },
      ];

      const [registerForm, { resetFields, setFieldsValue, validate, getFieldsValue }] = useForm({
        labelWidth: 100,
        schemas: formSchema,
        showActionButtonGroup: false,
      });

      const getTitle = computed(() => {
        return unref(isUpdate) ? '编辑场地底图' : '新增场地底图';
      });

      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        try {
          // 检查是否是相同记录的重复操作(在短时间内)
          const currentTimestamp = data?._timestamp || Date.now();
          const currentId = data?.record ? data.record.id : '';
          const timeDiff = currentTimestamp - lastOperation.value.timestamp;

          // 防止重复操作
          if (
            currentId &&
            currentId === lastOperation.value.id &&
            timeDiff > 0 &&
            timeDiff < 1000
          ) {
            // 只确保抽屉是打开的，不重新加载数据
            setDrawerProps({
              visible: true,
              confirmLoading: false,
            });
            return;
          }

          // 记录本次操作
          lastOperation.value = {
            id: currentId,
            timestamp: currentTimestamp,
          };

          // 重置表单
          resetFields();

          // 设置抽屉状态
          setDrawerProps({
            visible: true,
            confirmLoading: false,
          });

          // 标记是否为更新模式
          isUpdate.value = !!data?.isUpdate;

          // 等待DOM渲染完成
          await nextTick();

          // 如果是编辑模式，设置表单值
          if (unref(isUpdate) && data.record) {
            recordId.value = data.record.id;
            // 保存现有表单值
            const formValues = {
              name: data.record.name,
              project: data.record.projectId || data.record.project, // 使用projectId用于选择框匹配
              size: data.record.size,
              scale: data.record.scale,
              is3d: data.record.is3d + '',
              url: data.record.filePath, // 原始JSON数据
              remarks: data.record.remarks,
              status: data.record.status,
              drawingPixelX: data.record.drawingPixelX,
              drawingPixelY: data.record.drawingPixelY,
              siteCoordinateXM: data.record.siteCoordinateXM,
              siteCoordinateYM: data.record.siteCoordinateYM,
            };

            // 如果没有X/Y像素值但有尺寸字符串，自动解析尺寸
            if ((!formValues.drawingPixelX || !formValues.drawingPixelY) && formValues.size) {
              const { width, height } = parseMapSize(formValues.size);
              if (width > 0 && height > 0) {
                formValues.drawingPixelX = width;
                formValues.drawingPixelY = height;
              }
            }
            setFieldsValue(formValues);
          } else {
            // 新增模式
            recordId.value = '';
            setFieldsValue({ status: '禁用' });
          }
        } catch (error) {
          console.error('初始化抽屉时发生错误:', error);
        }
      });

      async function handleSubmit() {
        try {
          // 如果正在上传，禁止提交
          if (isUploading.value) {
            createMessage.warning('文件正在上传中，请稍后提交');
            return;
          }

          const values = await validate();
          setDrawerProps({ confirmLoading: true });

          if (unref(isUpdate)) {
            await updateSiteMap({
              ...values,
              id: recordId.value,
            });
            createMessage.success('更新成功');
          } else {
            // 新增时设置默认状态为"禁用"
            await createSiteMap({
              ...values,
              status: '禁用',
            });
            createMessage.success('保存成功');
          }

          closeDrawer();
          emit('success');
        } finally {
          setDrawerProps({ confirmLoading: false });
        }
      }

      return {
        t,
        registerDrawer,
        registerForm,
        getTitle,
        handleSubmit,
        recordId,
        isUploading,
        onUploading,
        onUploadSuccess,
        onUploadRemove,
        calculateImageSize,
        companyOptions,
      };
    },
  });
</script>

<style lang="less" scoped>
  .upload-container {
    width: 100%;
  }
</style>
