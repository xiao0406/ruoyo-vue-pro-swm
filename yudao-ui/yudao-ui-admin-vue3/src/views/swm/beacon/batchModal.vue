<template>
  <BasicModal
    v-bind="$attrs"
    @register="managementModal"
    :title="'批量更新信标'"
    @ok="handleSubmit"
    :width="600"
  >
    <BasicForm @register="managementForm" />
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, onMounted } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicForm, useForm } from '@/components/swm/Form/index';
  import { FormSchema } from '@/components/swm/Form';
  import { saveBeacon, getBeacon } from '@/api/swm/beacon';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { useDict } from '@/components/swm/Dict';

  // 批量更新表单定义
  const batchUpdateSchema: FormSchema[] = [
    {
      field: 'beaconType',
      label: '信标类型',
      component: 'Select',
      required: true,
      componentProps: {
        dictType: 'beacon_type_enum',
        allowClear: true,
      },
    },
    {
      field: 'beaconColor',
      label: '信标颜色',
      component: 'Input',
      required: false,
      componentProps: {
        type: 'color',
        style: { width: '60px' },
      },
    },
    {
      field: 'controlType',
      label: '围栏类型',
      component: 'Select',
      required: true,
      defaultValue: '一车间',
      componentProps: {
        options: [
          { label: '一车间', value: '一车间' },
          { label: '茶水间', value: '茶水间' },
          { label: '成品堆场', value: '成品堆场' },
          { label: '半成品堆场', value: '半成品堆场' },
          { label: '包装车间', value: '包装车间' },
        ],
      },
    },
  ];

  export default defineComponent({
    name: 'BatchModal',
    components: { BasicModal, BasicForm },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const selectedIds = ref<string[]>([]);
      const { createMessage } = useMessage();
      const processingCount = ref(0);
      const totalCount = ref(0);
      const { initDict } = useDict();

      // 初始化字典数据
      onMounted(() => {
        initDict(['beacon_type_enum']);
      });

      const [managementForm, { resetFields, setFieldsValue, validate }] = useForm({
        labelWidth: 100,
        baseColProps: { span: 24 },
        schemas: batchUpdateSchema,
        showActionButtonGroup: false,
        actionColOptions: {
          span: 24,
        },
      });

      const [managementModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        resetFields();
        setModalProps({ confirmLoading: false });

        if (data && data.selectedIds) {
          selectedIds.value = data.selectedIds as string[];
        }

        // 设置默认值
        setFieldsValue({
          beaconType: '1', // 常规信标
          beaconColor: '#ff0000', // 默认红色
          controlType: '一车间',
        });
      });

      async function handleSubmit() {
        try {
          if (selectedIds.value.length === 0) {
            createMessage.warning('请选择需要批量更新的信标');
            return;
          }

          const values = await validate();
          setModalProps({ confirmLoading: true });

          // 设置进度计数
          totalCount.value = selectedIds.value.length;
          processingCount.value = 0;

          // 批量更新
          const updatedValues = {
            beaconType: values.beaconType,
            beaconColor: values.beaconColor,
            controlType: values.controlType,
          };

          const updatePromises = selectedIds.value.map(async (id) => {
            try {
              // 获取原始信标数据
              const beaconData = await getBeacon(id);

              // 只更新信标类型、围栏类型和颜色，保留其他字段不变
              const updateData = {
                ...beaconData,
                beaconType: updatedValues.beaconType,
                beaconColor: updatedValues.beaconColor,
                controlType: updatedValues.controlType,
              };

              // 保存更新后的数据
              await saveBeacon(updateData);
              processingCount.value += 1;
              return true;
            } catch (error) {
              console.error(`更新信标 ID ${id} 失败:`, error);
              return false;
            }
          });

          // 等待所有更新完成
          await Promise.all(updatePromises);

          createMessage.success(`批量更新成功，已更新 ${processingCount.value} 个信标`);
          closeModal();
          emit('success');
        } catch (error) {
          createMessage.error('批量更新过程中发生错误');
          console.error('批量更新错误:', error);
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      return { managementModal, managementForm, handleSubmit };
    },
  });
</script>
