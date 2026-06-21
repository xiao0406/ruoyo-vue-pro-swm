<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    @ok="handleSubmit"
    width="800px"
  >
    <BasicForm @register="registerForm" />

    <!-- 查看模式下显示处置记录列表 -->
    <DisposalRecordList v-if="isView && recordId" :hiddenDangerId="recordId" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicForm, useForm } from '@/components/swm/Form';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { formSchema } from './hiddenDangerData';
  import { getHiddenDanger, saveHiddenDanger } from '@/api/swm/hiddenDanger';
  import DisposalRecordList from '../dangerDisposal/DisposalRecordList.vue';

  const emit = defineEmits(['success', 'register']);
  const isUpdate = ref(false);
  const isView = ref(false);
  const recordId = ref('');
  const { createMessage } = useMessage();

  // 表单
  const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
    labelWidth: 100,
    schemas: formSchema,
    showActionButtonGroup: false,
  });

  // 弹窗
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    resetFields();
    setModalProps({ confirmLoading: false });

    isUpdate.value = !!data?.isUpdate;
    isView.value = !!data?.isView;

    if (data?.record) {
      recordId.value = data.record.id;
      setFieldsValue({
        ...data.record,
      });
    } else if (data?.id) {
      recordId.value = data.id;
      await getDetail();
    }

    // 查看模式下禁用所有字段
    if (isView.value) {
      updateSchema(
        formSchema.map((item) => {
          return {
            field: item.field,
            componentProps: {
              disabled: true,
            },
          };
        }),
      );
    } else {
      // 编辑模式下启用所有字段
      updateSchema(
        formSchema.map((item) => {
          return {
            field: item.field,
            componentProps: {
              disabled: false,
            },
          };
        }),
      );
    }
  });

  // 获取标题
  const getTitle = computed(() => {
    return isView.value ? '查看隐患信息' : isUpdate.value ? '编辑隐患信息' : '新建隐患信息';
  });

  // 获取详情
  async function getDetail() {
    try {
      setModalProps({ loading: true });
      const res = await getHiddenDanger({ id: recordId.value });
      if (res && res.hiddenDanger) {
        setFieldsValue({
          ...res.hiddenDanger,
        });
      } else if (res) {
        // 直接处理res对象
        setFieldsValue({
          ...res,
        });
      }
    } catch (error) {
      console.error('获取隐患信息失败', error);
    } finally {
      setModalProps({ loading: false });
    }
  }

  // 提交表单
  async function handleSubmit() {
    if (isView.value) {
      closeModal();
      return;
    }

    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });

      const data = {
        ...values,
      };

      if (isUpdate.value) {
        data.id = recordId.value;
      }

      await saveHiddenDanger(data);
      createMessage.success(`${isUpdate.value ? '更新' : '创建'}成功`);
      closeModal();
      emit('success');
    } catch (error) {
      console.error('提交表单失败', error);
      createMessage.error(`${isUpdate.value ? '更新' : '创建'}失败`);
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
