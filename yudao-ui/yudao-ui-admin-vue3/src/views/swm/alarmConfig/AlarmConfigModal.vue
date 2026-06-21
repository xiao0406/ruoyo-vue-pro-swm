<!--
  @author zwf
  @date 2025-6-18
-->
<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    @ok="handleSubmit"
    :width="800"
  >
    <div style="padding: 20px 20px">
      <BasicForm @register="registerForm" />
    </div>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicForm, useForm } from '@/components/swm/Form';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { formSchema } from './data';
  import { getAlarmConfig, saveAlarmConfig } from '@/api/swm/alarmConfig';

  export default defineComponent({
    name: 'AlarmConfigModal',
    components: { BasicModal, BasicForm },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { createMessage } = useMessage();
      const isUpdate = ref(false);
      const rowId = ref('');

      const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
        labelWidth: 100,
        schemas: formSchema,
        showActionButtonGroup: false,
        baseColProps: { span: 24 },
      });

      const getTitle = computed(() => (!unref(isUpdate) ? '新增报警配置' : '编辑报警配置'));

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        resetFields();
        setModalProps({ confirmLoading: false });
        isUpdate.value = !!data?.isUpdate;

        if (unref(isUpdate)) {
          rowId.value = data.record.id;
          if (data.record) {
            // 处理数据，确保字段类型正确
            const record = {
              ...data.record,
              enableAlarm: String(data.record.enableAlarm),
              needConfirm: String(data.record.needConfirm),
              isSendZjt: data.record.isSendZjt > 0 ? String(data.record.isSendZjt) : '0', // 添加这一行
            };

            // 确保逻辑一致性
            if (record.enableAlarm === '0') {
              record.needConfirm = '0';
            }

            setFieldsValue(record);
          } else {
            const record = await getAlarmConfig(data.id);
            rowId.value = record.id;
            // 处理数据，确保字段类型正确
            record.enableAlarm = String(record.enableAlarm);
            record.needConfirm = String(record.needConfirm);

            // 确保逻辑一致性
            if (record.enableAlarm === '0') {
              record.needConfirm = '0';
            }

            setFieldsValue(record);
          }
        }
      });

      async function handleSubmit() {
        try {
          const values = await validate();
          setModalProps({ confirmLoading: true });

          // 提交前转换为字符串类型并确保逻辑一致性
          const params = {
            ...values,
            enableAlarm: String(values.enableAlarm),
            needConfirm: String(values.needConfirm),
          };

          // 确保逻辑一致性
          if (params.enableAlarm === '0') {
            params.needConfirm = '0';
          }

          if (unref(isUpdate)) {
            params.id = rowId.value;
          }

          await saveAlarmConfig(params);

          closeModal();
          createMessage.success(`${unref(isUpdate) ? '更新' : '新增'}成功`);

          // 触发全局事件以更新警告数据
          window.dispatchEvent(new CustomEvent('refresh-warnings'));

          emit('success');
        } catch (error: any) {
          createMessage.error(error?.message || '提交失败');
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      return { registerModal, registerForm, getTitle, handleSubmit };
    },
  });
</script>
