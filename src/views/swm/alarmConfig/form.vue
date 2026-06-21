<!-- @format -->

<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    title="中建通推送配置"
    width="50%"
    @ok="handleSubmit"
    destroyOnClose
  >
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, h, ref } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { useI18n } from '@/hooks/swm/useI18n';
  import { BasicForm, FormSchema, useForm } from '@/components/swm/Form';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { Popup } from '@/components/swm/Popupinput';
  import { setAlarmConfig, getConfigDetail } from '@/api/swm/alarmConfig';

  export default defineComponent({
    name: 'AlarmConfigFormModal',
    components: { BasicModal, BasicForm },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { createMessage } = useMessage();
      // const emit = defineEmits(['success', 'register']);

      const { t } = useI18n('fms.workTasksFileRead');
      const { showMessage } = useMessage();
      const rowIds = ref({
        id: '',
        mainId: '',
      });
      const record = ref(null);

      const formSchemas: FormSchema[] = [
        {
          label: t('推送角色'),
          field: 'roles',
          component: 'Select',
          // required: true,
          render: ({ model, field }) => {
            return h(Popup, {
              valueText: model[field],
              code: 'ROLE_ALL', // 配置模块
              slectMode: 'multiple', // 单选多选multiple
              enabled: true, // 是否可编辑
              dataTextField: '', // 需要更改显示值的字段
              dataValueField: 'roleCode', // 需要更改绑定值的字段
              params: {}, //添加默认参数
              onChange: (e) => {
                console.log(e);
                model[field] = e;
              },
            });
          },
        },
        {
          label: t('推送人员'),
          field: 'users',
          component: 'Select',
          // required: true,
          render: ({ model, field }) => {
            return h(Popup, {
              valueText: model[field],
              code: 'RYDA', // 配置模块
              slectMode: 'multiple', // 单选多选multiple
              enabled: true, // 是否可编辑
              dataTextField: '', // 需要更改显示值的字段
              dataValueField: 'empCode', // 需要更改绑定值的字段
              params: {}, //添加默认参数
              onChange: (e) => {
                console.log(e);
                model[field] = e;
              },
            });
          },
        },
      ];

      const [
        registerForm,
        { resetFields, validate, setFieldsValue, getFieldsValue, validateFields, updateSchema },
      ] = useForm({
        labelWidth: 130,
        schemas: formSchemas,
        baseColProps: { md: 24 },
      });

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        resetFields();
        setModalProps({ confirmLoading: false });
        rowIds.value.mainId = data.id;
        const record = await getConfigDetail(data.id);
        rowIds.value.id = record.id;
        // 确保回显的数据格式正确
        const formattedRecord = {
          ...record,
          roles: record.roles ? record.roles.split(',') : [],
          users: record.users ? record.users.split(',') : [],
        };

        setFieldsValue(formattedRecord);
      });

      async function handleSubmit() {
        setModalProps({ loading: true });

        try {
          const values = await validate();
          // if (record.value.id) {
          //   const data = await getFieldsValue();
          const params: any = {
            mainId: rowIds.value.mainId,
          };

          // 如果有 id，则添加到参数中
          if (rowIds.value.id) {
            params.id = rowIds.value.id;
          }

          // 处理 roles 字段，即使为空数组也要传递
          if (values.roles !== undefined && values.roles !== null) {
            params.roles = Array.isArray(values.roles) ? values.roles.join(',') : values.roles;
          } else {
            params.roles = ''; // 空值也传递空字符串
          }

          // 处理 users 字段，即使为空数组也要传递
          if (values.users !== undefined && values.users !== null) {
            params.users = Array.isArray(values.users) ? values.users.join(',') : values.users;
          } else {
            params.users = ''; // 空值也传递空字符串
          }

          await setAlarmConfig(params);

          closeModal();
          createMessage.success(`配置成功`);

          // 触发全局事件以更新警告数据
          window.dispatchEvent(new CustomEvent('refresh-warnings'));

          emit('success');
          // }
        } catch (error: any) {
          if (error && error.errorFields) {
            showMessage(t('您填写的信息有误，请根据提示修正。'));
          }
          console.log('error', error);
        } finally {
          setModalProps({ loading: false });
        }
      }
      return { registerModal, registerForm, handleSubmit };
    },
  });
</script>
