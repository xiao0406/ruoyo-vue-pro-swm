<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    @ok="handleSubmit"
    :width="1000"
  >
    <template #title>
      <Icon :icon="getTitle.icon" class="pr-1 m-1" />
      <span> {{ getTitle.value }} </span>
    </template>
    <div style="padding: 0 20px; min-height: 300px">
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
  import { formSchema, editFormSchema } from './beaconData';
  import { getBeacon, saveBeacon } from '@/api/swm/beacon';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { useDict } from '@/components/swm/Dict';
  import { getAreaOptions } from '@/api/swm/area';
  import type { FormSchema } from '@/components/swm/Form';

  export default defineComponent({
    name: 'BeaconModal',
    components: { BasicModal, BasicForm, Icon },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const getTitle = computed(() => ({
        icon: router.currentRoute.value.meta.icon || 'ant-design:book-outlined',
        value: unref(isUpdate) ? '编辑信标' : '新增信标',
      }));
      const isUpdate = ref(false);
      const rowId = ref('');
      const { createMessage } = useMessage();
      const { initDict } = useDict();
      const areaOptions = ref<Array<{ value: string; label: string; areaName: string }>>([]);

      // 初始化字典数据和区域选项
      onMounted(async () => {
        await initDict(['beacon_type_enum', 'beacon_status_enum', 'deploy_status_enum']);
        await loadAreaOptions();
      });

      // 加载区域选项
      async function loadAreaOptions() {
        try {
          const result = await getAreaOptions();
          if (result.success && result.options) {
            areaOptions.value = result.options;
            // 更新表单中的区域字段配置
            updateSchema({
              field: 'area',
              component: 'Select',
              componentProps: {
                options: areaOptions.value,
                allowClear: true,
                placeholder: '请选择所属区域',
              },
            });
          }
        } catch (error: any) {
          console.error('区域选项加载异常:', error);
        }
      }

      // 动态创建表单配置
      const getFormSchemas = computed((): FormSchema[] => {
        const baseSchemas = unref(isUpdate) ? editFormSchema : formSchema;
        return baseSchemas.map((schema) => {
          if (schema.field === 'area') {
            return {
              ...schema,
              component: 'Select',
              componentProps: {
                options: areaOptions.value,
                allowClear: true,
                placeholder: '请选择所属区域',
              },
            };
          }
          return schema;
        });
      });

      const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
        labelWidth: 100,
        baseColProps: { span: 12 },
        schemas: getFormSchemas,
        showActionButtonGroup: false,
        actionColOptions: {
          span: 24,
        },
      });

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        resetFields();
        setModalProps({ confirmLoading: false });
        isUpdate.value = !!data?.isUpdate;

        if (unref(isUpdate)) {
          rowId.value = data.record.id;
          if (rowId.value) {
            const beaconData = await getBeacon(rowId.value);
            setFieldsValue({
              ...beaconData,
            });
          }
        } else {
          // 新建时的默认值设置
          setFieldsValue({
            beaconType: '1', // 常规信标
            beaconColor: '#ff0000', // 默认红色
          });
        }
      });

      async function handleSubmit() {
        try {
          const values = await validate();
          setModalProps({ confirmLoading: true });

          // 如果是编辑，需要传入ID
          if (unref(isUpdate)) {
            values.id = rowId.value;
          }

          // 保存更新
          await saveBeacon(values);

          createMessage.success(`${unref(isUpdate) ? '更新' : '新增'}成功！`);
          closeModal();
          emit('success');
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      return { registerModal, registerForm, getTitle, handleSubmit, getTitle };
    },
  });
</script>
