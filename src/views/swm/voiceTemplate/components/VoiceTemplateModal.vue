<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    @ok="handleSubmit"
    width="800px"
  >
    <template #title>
      <Icon :icon="getTitle.icon" class="pr-1 m-1" />
      <span> {{ getTitle.value }} </span>
    </template>
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, ref, computed, unref, onMounted } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicForm, useForm } from '@/components/swm/Form';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { addVoiceTemplate, updateVoiceTemplate } from '@/api/swm/voiceTemplate';
  import type { VoiceTemplateItem } from '@/api/swm/voiceTemplate';
  import { useDict } from '@/components/swm/Dict';
  import { router } from '@/router';
  import { Icon } from '@/components/swm/Icon';

  export default defineComponent({
    name: 'VoiceTemplateModal',
    components: { BasicModal, BasicForm, Icon },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { createMessage } = useMessage();
      const isUpdate = ref(false);
      const modelRef = ref<Recordable>({});
      const { initDict } = useDict();
      const getTitle = computed(() => ({
        icon: router.currentRoute.value.meta.icon || 'ant-design:book-outlined',
        value: unref(isUpdate) ? '编辑语音模板' : '新增语音模板',
      }));
      // 初始化字典数据
      onMounted(() => {
        initDict(['voice_template_type']);
      });

      const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
        labelWidth: 100,
        schemas: [
          {
            field: 'templateName',
            label: '模板名称',
            component: 'Input',
            required: true,
            componentProps: {
              placeholder: '请输入模板名称',
            },
          },
          {
            field: 'templateCode',
            label: '模板类型',
            component: 'Select',
            componentProps: {
              placeholder: '请选择模板类型',
              dictType: 'voice_template_type',
              allowClear: true,
            },
            required: true,
          },
          {
            field: 'pushMethod',
            label: '推送方式',
            component: 'CheckboxGroup',
            defaultValue: ['1'],
            componentProps: {
              options: [
                { label: '定位模块推送', value: '1' },
                { label: '中建通推送', value: '2' },
              ],
            },
            required: true,
          },
          {
            field: 'pushFrequency',
            label: '推送频次',
            component: 'RadioGroup',
            defaultValue: '1',
            componentProps: {
              options: [
                { label: '仅一次', value: '1' },
                // { label: '按固定时间间隔推送', value: '2' },
              ],
            },
            required: true,
          },
          {
            field: 'voiceText',
            label: '语音文字',
            component: 'InputTextArea',
            required: false,
            componentProps: {
              rows: 4,
              placeholder: '请输入语音文字内容（选填）',
            },
          },
          {
            field: 'status',
            label: '应用状态',
            component: 'Select',
            componentProps: {
              placeholder: '请选择应用状态',
              options: [
                { value: '0', label: '启用' },
                { value: '1', label: '禁用' },
              ],
            },
            defaultValue: '0',
            required: true,
            show: false,
          },
          {
            field: 'language',
            label: '语言类型',
            component: 'Input',
            defaultValue: 'zh-CN',
            show: false,
          },
          {
            field: 'remarks',
            label: '备注',
            component: 'InputTextArea',
            componentProps: {
              rows: 2,
              placeholder: '请输入备注信息（选填）',
            },
          },
        ],
        showActionButtonGroup: false,
        actionColOptions: {
          span: 24,
        },
      });

      const [registerModal, { closeModal, setModalProps }] = useModalInner(async (data) => {
        resetFields();
        setModalProps({ confirmLoading: false });
        isUpdate.value = !!data?.isUpdate;

        if (unref(isUpdate)) {
          modelRef.value = data.record;
          // 确保pushMethod正确转换为数组
          let pushMethodVal = data.record.pushMethod || '1';
          let pushMethodArray: string[] = [];

          if (typeof pushMethodVal === 'string') {
            pushMethodArray = pushMethodVal.split(',');
          } else if (Array.isArray(pushMethodVal)) {
            pushMethodArray = pushMethodVal as string[];
          } else {
            pushMethodArray = ['1']; // 默认值
          }

          setFieldsValue({
            ...data.record,
            // 正确设置推送方式数组
            pushMethod: pushMethodArray,
            pushFrequency: data.record.pushFrequency || '1',
          });
        }
      });

      async function handleSubmit() {
        try {
          const values = await validate();
          setModalProps({ confirmLoading: true });

          // 处理推送方式，将数组转为字符串
          // 确保pushMethod是数组，如果是字符串则先转换为数组
          let pushMethodArray: string[] = [];

          if (typeof values.pushMethod === 'string') {
            pushMethodArray = values.pushMethod ? values.pushMethod.split(',') : ['1'];
          } else if (Array.isArray(values.pushMethod)) {
            pushMethodArray = values.pushMethod as string[];
          } else {
            // 如果值既不是字符串也不是数组，则使用默认值
            pushMethodArray = ['1'];
          }

          const pushMethod = pushMethodArray.join(',');

          // 构建保存数据
          const saveData: Recordable = {
            ...values,
            pushMethod,
            status: values.status || '0', // 确保新增时默认为启用状态
          };

          // 编辑时需要保留ID
          if (unref(isUpdate)) {
            saveData.id = modelRef.value.id;
            await updateVoiceTemplate(saveData);
          } else {
            // 新增
            await addVoiceTemplate(saveData);
          }

          createMessage.success(`${unref(isUpdate) ? '编辑' : '新增'}成功`);
          closeModal();
          emit('success');
        } catch (error: any) {
          createMessage.error(`操作失败: ${error.message || '未知错误'}`);
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      return {
        registerModal,
        registerForm,
        getTitle,
        handleSubmit,
      };
    },
  });
</script>
<style lang="css" scoped>
  :deep(.ant-form-item-label) {
    min-width: 50px;
    text-align: right;
  }
</style>
