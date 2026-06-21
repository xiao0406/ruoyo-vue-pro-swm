<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    title="语音充电提醒"
    @ok="handleSubmit"
    width="500px"
  >
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicForm, useForm } from '@/components/swm/Form/index';
  import { sendVoiceToDevices } from '@/api/swm/helmetDevice';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { FormSchema } from '@/components/swm/Form';

  export default defineComponent({
    name: 'BatchChargeModal',
    components: { BasicModal, BasicForm },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { createMessage } = useMessage();
      const selectedIds = ref<string[]>([]);

      const formSchema: FormSchema[] = [
        {
          field: 'templateId',
          label: '语音模板ID',
          component: 'Input',
          defaultValue: '1936227999566286848',
          componentProps: {
            disabled: true,
            placeholder: '语音模板ID（固定值）',
          },
          helpMessage: '用于语音提醒的模板ID，已预设为充电提醒模板',
          required: true,
        },
        {
          field: 'deviceCount',
          label: '设备数量',
          component: 'Input',
          componentProps: {
            disabled: true,
            placeholder: '将显示选中的设备数量',
          },
          helpMessage: '本次将向选中的设备发送语音提醒',
        },
      ];

      const [registerForm, { resetFields, validate, setFieldsValue }] = useForm({
        labelWidth: 120,
        schemas: formSchema,
        showActionButtonGroup: false,
      });

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data: any) => {
        resetFields();
        setModalProps({ confirmLoading: false });
        if (data && data.ids) {
          selectedIds.value = data.ids;
          // 设置设备数量显示
          setFieldsValue({
            deviceCount: `${data.ids.length}个设备`,
          });
        }
      });

      async function handleSubmit() {
        try {
          const values = await validate();
          setModalProps({ confirmLoading: true });

          // 调用语音模板发送接口
          await sendVoiceToDevices(selectedIds.value, values.templateId);

          closeModal();
          emit('success');
          createMessage.success('语音提醒已发送至设备！');
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      return {
        registerModal,
        registerForm,
        handleSubmit,
      };
    },
  });
</script>
