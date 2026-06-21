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
    <div style="padding: 20px">
      <BasicForm @register="registerForm" />
    </div>
  </BasicModal>
</template>

<script lang="ts">
  import { router } from '@/router';
  import { Icon } from '@/components/swm/Icon';
  import { defineComponent, ref, computed, unref, onMounted } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicForm, useForm } from '@/components/swm/Form/index';
  import { getHelmetDevice, saveHelmetDevice } from '@/api/swm/helmetDevice';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { FormSchema } from '@/components/swm/Form';
  import { useDict } from '@/components/swm/Dict';

  export default defineComponent({
    name: 'HelmetDeviceModal',
    components: { BasicModal, BasicForm, Icon },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { createMessage } = useMessage();
      const isUpdate = ref(false);
      const helmetId = ref('');
      const { initDict } = useDict();
      const getTitle = computed(() => ({
        icon: router.currentRoute.value.meta.icon || 'ant-design:book-outlined',
        value: unref(isUpdate) ? '编辑设备' : '新增设备',
      }));
      // 初始化字典数据
      onMounted(() => {
        initDict(['motion_status_enum', 'helmet_type_enum']);
      });

      const formSchema: FormSchema[] = [
        {
          field: 'id',
          label: 'ID',
          component: 'Input',
          show: false,
        },
        {
          field: 'deviceId',
          label: '设备编号',
          component: 'Input',
          required: true,
          rules: [{ required: true, message: '请输入设备编号' }],
        },
        {
          field: 'helmetType',
          label: '设备类型',
          component: 'Select',
          componentProps: {
            dictType: 'helmet_type_enum',
            allowClear: true,
          },
          required: true,
        },

        {
          field: 'macAddress',
          label: 'MAC地址',
          component: 'Input',
          componentProps: {
            disabled: true,
            placeholder: '系统更新',
          },
        },
      ];

      const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
        labelWidth: 100,
        baseColProps: { span: 12 },
        schemas: formSchema,
        showActionButtonGroup: false,
        actionColOptions: {
          span: 24,
        },
      });

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data: any) => {
        resetFields();
        setModalProps({ confirmLoading: false });
        isUpdate.value = !!data?.isUpdate;

        if (unref(isUpdate)) {
          helmetId.value = data.record.id;
          const helmetData = await getHelmetDevice(data.record.id);
          setFieldsValue({
            ...helmetData,
          });
        }
      });

      async function handleSubmit() {
        try {
          const values = await validate();
          setModalProps({ confirmLoading: true });
          if (values.assignedProcess) {
            delete values.assignedProcess;
          }
          await saveHelmetDevice(values);
          closeModal();
          emit('success');
          createMessage.success(`${unref(isUpdate) ? '修改' : '新增'}成功！`);
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
