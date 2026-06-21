<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    @ok="handleSubmit"
    width="800px"
    :show-ok-btn="!isView"
  >
    <div style="padding: 0 15px">
      <BasicForm @register="registerForm" />
    </div>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, unref, watch, nextTick } from 'vue';
  import { BasicForm, useForm } from '@/components/swm/Form/index';
  import { formSchema } from './inspectionPlanData';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { getInspectionPlan, saveInspectionPlan } from '@/api/swm/inspectionPlan';
  import { useMessage } from '@/hooks/swm/useMessage';

  export default defineComponent({
    name: 'InspectionPlanModal',
    components: { BasicModal, BasicForm },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const isUpdate = ref(false);
      const isView = ref(false);
      const recordId = ref('');
      const isInitializing = ref(false); // 添加初始化标志，防止watch误触发
      const { createMessage } = useMessage();

      const [
        registerForm,
        {
          resetFields,
          setFieldsValue,
          validate,
          validateFields,
          updateSchema,
          getFieldsValue,
          clearValidate,
        },
      ] = useForm({
        labelWidth: 100,
        schemas: formSchema,
        showActionButtonGroup: false,
      });

      // 监听巡检类型变化
      watch(
        () => {
          const values = getFieldsValue();
          return values && values.inspectionType;
        },
        (val, oldVal) => {
          // 如果正在初始化，不处理
          if (unref(isInitializing)) {
            return;
          }

          // 只在值真正变化时处理
          if (!oldVal || oldVal === val) {
            return;
          }

          // 当切换到风险巡检时
          if (val === '3') {
            // 只在新增模式下清空，编辑模式下保留原有数据
            if (!unref(isUpdate)) {
              setFieldsValue({ hazardSourceIds: [] });
            }
          } else {
            // 切换到其他类型时，清空风险选择
            setFieldsValue({ hazardSourceIds: [] });
          }
        },
      );

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        resetFields();
        setModalProps({ confirmLoading: false });
        isUpdate.value = !!data?.isUpdate;
        isView.value = !!data?.isView;

        if (unref(isUpdate) || unref(isView)) {
          isInitializing.value = true; // 开始初始化
          recordId.value = data.record.id;
          const record = await getInspectionPlan({ id: recordId.value });

          // 处理风险数据 - 只使用API，确保能包含当前选项
          if (record.hazardSourceIds && record.hazardSourceNames && record.inspectionType === '3') {
            const currentOptions = record.hazardSourceIds.map((id, index) => ({
              label: record.hazardSourceNames[index] || id,
              value: id,
            }));

            updateSchema([
              {
                field: 'hazardSourceIds',
                componentProps: {
                  // 只使用API，但确保包含当前选项
                  api: async () => {
                    const { getNotPatrolledHazardSourceList } = await import(
                      '@/api/swm/hazardSource'
                    );
                    const remoteOptions = (await getNotPatrolledHazardSourceList()) || [];

                    // 合并当前选项和远程选项
                    const optionsMap = new Map();
                    // 先添加当前选项，确保能显示
                    currentOptions.forEach((opt) => {
                      optionsMap.set(opt.value, opt);
                    });
                    // 再添加远程选项
                    remoteOptions.forEach((opt) => {
                      if (!optionsMap.has(opt.value)) {
                        optionsMap.set(opt.value, opt);
                      }
                    });

                    return Array.from(optionsMap.values());
                  },
                  immediate: true, // 立即执行API
                },
              },
            ]);
          }

          // 不需要特殊处理负责人，让原有的API正常工作

          // 等待schema更新和API执行
          await nextTick();
          // 再等待一下，确保API已经开始执行
          await new Promise((resolve) => setTimeout(resolve, 100));

          // 设置表单值，确保风险ID数组正确设置
          const formValues = {
            ...record,
            id: recordId.value,
          };

          // 如果有风险数据，确保设置数组格式的值
          if (record.hazardSourceIds && record.inspectionType === '3') {
            formValues.hazardSourceIds = record.hazardSourceIds;
          }

          setFieldsValue(formValues);

          // 如果是风险巡检类型，清除风险字段的验证状态
          if (record.inspectionType === '3' && record.hazardSourceIds) {
            // 等待表单值设置完成后清除验证
            await nextTick();
            clearValidate('hazardSourceIds');
          }

          // 设置完值后，结束初始化状态
          setTimeout(() => {
            isInitializing.value = false;
          }, 200);
        } else {
          // 新增时，设置编号字段为空，显示占位符
          setFieldsValue({
            planCode: '',
          });
        }
        updateSchema(
          formSchema.map((schema) => ({
            field: schema.field,
            componentProps: {
              // 编号字段始终禁用，其他字段根据查看状态决定
              disabled: schema.field === 'planCode' ? true : isView.value,
            },
          })),
        );
      });

      const getTitle = computed(() =>
        unref(isView) ? '查看巡检计划' : unref(isUpdate) ? '编辑巡检计划' : '新增巡检计划',
      );

      async function handleSubmit() {
        try {
          setModalProps({ confirmLoading: true });

          // 先获取所有表单值
          const values = getFieldsValue();

          // 手动验证风险字段
          if (
            values.inspectionType === '3' &&
            (!values.hazardSourceIds || values.hazardSourceIds.length === 0)
          ) {
            createMessage.error('请选择关联的风险！');
            setModalProps({ confirmLoading: false });
            return;
          }

          // 验证其他必填字段（排除风险）
          try {
            const fieldsToValidate = [
              'planName',
              'frequencyDays',
              'inspectionType',
              'responsiblePersonId',
              'firstInspectionTime',
            ];

            // 如果有planStatus字段也加入验证
            if ('planStatus' in values) {
              fieldsToValidate.push('planStatus');
            }

            await validateFields(fieldsToValidate);
          } catch (validationError) {
            console.error('验证失败:', validationError);
            createMessage.error('请检查表单必填项');
            setModalProps({ confirmLoading: false });
            return;
          }

          // 所有验证通过，保存数据
          await saveInspectionPlan(values);
          closeModal();
          emit('success');
        } catch (error) {
          console.error('保存失败:', error);
          createMessage.error('保存失败，请检查表单数据');
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      return {
        registerModal,
        registerForm,
        getTitle,
        handleSubmit,
        isView,
      };
    },
  });
</script>
<style lang="css" scoped>
  :deep(.ant-form-item-label) {
    min-width: 120px;
  }
</style>
