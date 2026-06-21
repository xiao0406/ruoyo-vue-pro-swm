<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    @ok="handleSubmit"
    :width="800"
  >
    <template #title>
      <Icon :icon="getTitle.icon" class="pr-1 m-1" />
      <span> {{ getTitle.value }} </span>
    </template>
    <div style="padding: 0 20px">
      <BasicForm @register="registerForm" />
    </div>
  </BasicModal>
</template>

<script lang="ts">
  import { router } from '@/router';
  import { Icon } from '@/components/swm/Icon';
  import { defineComponent, ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicForm, useForm } from '@/components/swm/Form/index';
  import { alarmLightFormSchema } from './alarmLight.data';
  import { saveAlarmLight } from '@/api/swm/alarmLight';
  import { useMessage } from '@/hooks/swm/useMessage';

  export default defineComponent({
    name: 'AlarmLightModal',
    components: { BasicModal, BasicForm, Icon },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const isUpdate = ref(true);
      const rowId = ref('');
      const { createMessage } = useMessage();

      const [registerForm, { setFieldsValue, resetFields, validate }] = useForm({
        labelWidth: 100,
        schemas: alarmLightFormSchema,
        showActionButtonGroup: false,
        actionColOptions: {
          span: 23,
        },
      });
      const getTitle = computed(() => ({
        icon: router.currentRoute.value.meta.icon || 'ant-design:book-outlined',
        value: !unref(isUpdate) ? '新增报警灯' : '编辑报警灯',
      }));
      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        resetFields();
        setModalProps({ confirmLoading: false });
        isUpdate.value = !!data?.isUpdate;

        if (unref(isUpdate)) {
          rowId.value = data.record.id;
          setFieldsValue({
            ...data.record,
          });
        }
      });

      async function handleSubmit() {
        try {
          const values = await validate();
          setModalProps({ confirmLoading: true });

          if (unref(isUpdate)) {
            values.id = rowId.value;
          }

          await saveAlarmLight(values);
          createMessage.success('保存成功');
          closeModal();
          emit('success');
        } catch (error) {
          createMessage.error('保存失败');
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
