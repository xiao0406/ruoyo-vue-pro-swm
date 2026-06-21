<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="isUpdate ? '编辑排班' : '添加排班'"
    @ok="handleSubmit"
    :width="800"
    :maskClosable="false"
    :keyboard="false"
  >
    <div style="padding: 0 20px"> </div>
    <div v-if="loading" class="loading-container">
      <a-spin tip="加载中..."></a-spin>
    </div>
    <BasicForm v-else @register="registerForm" v-model:model="modelRef">
      <!-- 表单内容 -->
    </BasicForm>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicForm, useForm } from '@/components/swm/Form/index';
  import { Spin } from 'ant-design-vue';
  import { getStaffSchedule, saveStaffSchedule } from '@/api/swm/staffSchedule';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { classesEnum } from '@/enums/swm/attendanceEnum';
  import dayjs from 'dayjs';

  // 编辑表单
  const formSchema = [
    {
      field: 'id',
      label: 'ID',
      component: 'Input',
      show: false,
    },
    {
      field: 'personName',
      label: '人员姓名',
      component: 'Input',
      dynamicDisabled: true,
      required: true,
      componentProps: {
        placeholder: '请输入人员姓名',
      },
      rules: [
        {
          required: true,
          message: '请输入人员姓名',
        },
      ],
    },
    {
      field: 'workGroupName',
      label: '班组',
      component: 'Input',
      dynamicDisabled: true,
      componentProps: {
        placeholder: '班组信息',
      },
    },
    // {
    //   field: 'idCard',
    //   label: '身份证号',
    //   component: 'Input',
    //   required: true,
    //   componentProps: {
    //     placeholder: '请输入身份证号码',
    //   },
    //   rules: [
    //     {
    //       required: true,
    //       message: '请输入身份证号码',
    //     },
    //     {
    //       pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/,
    //       message: '请输入正确的身份证号码',
    //     },
    //   ],
    // },
    {
      field: 'month',
      label: '月份',
      component: 'MonthPicker',
      required: true,
      dynamicDisabled: true,
      componentProps: {
        format: 'YYYY-MM',
        placeholder: '请选择月份',
        valueFormat: 'YYYY-MM',
        style: { width: '100%' },
        allowClear: false,
      },
      rules: [
        {
          required: true,
          message: '请选择月份',
        },
      ],
    },
    {
      field: 'classes',
      label: '班次',
      component: 'Select',
      required: true,
      // dynamicDisabled: true,
      componentProps: {
        options: [
          { label: '早班', value: '1' },
          { label: '晚班', value: '3' },
        ],
        placeholder: '请选择班次',
      },
      rules: [
        {
          required: true,
          message: '请选择班次',
        },
      ],
    },
    {
      field: 'remarks',
      label: '备注',
      component: 'InputTextArea',
      componentProps: {
        placeholder: '请输入备注信息',
        rows: 2,
      },
    },
  ];

  export default defineComponent({
    name: 'EditScheduleModal',
    components: { BasicModal, BasicForm, ASpin: Spin },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { createMessage } = useMessage();
      const loading = ref(false);
      const isUpdate = ref(false);
      const rowId = ref('');
      // 添加表单数据模型引用
      const modelRef = ref({
        id: '',
        personName: '',
        workGroupName: '',
        idCard: '',
        month: null,
        classes: '1',
        remarks: '',
      });

      // 注册表单
      const [registerForm, { resetFields, setFieldsValue, validate, setProps }] = useForm({
        labelWidth: 100,
        schemas: formSchema,
        showActionButtonGroup: false,
        autoFocusFirstItem: false,
        submitOnReset: false,
        submitOnChange: false,
        resetFunc: () => {},
        transformDateFunc: (date) => {
          return date ? dayjs(date).format('YYYY-MM') : '';
        },
      });

      // 注册模态框
      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        resetFields();
        setModalProps({ confirmLoading: false });

        // 判断是否是编辑模式
        isUpdate.value = !!data?.record?.id;
        if (isUpdate.value) {
          rowId.value = data.record.id;
          loading.value = true;

          try {
            // 始终从API加载最新的排班详情
            console.log('从API加载排班详情, ID:', rowId.value);
            const scheduleData = await getStaffSchedule(rowId.value);

            console.log('后端返回的数据:', scheduleData);

            // 处理日期格式
            if (scheduleData.month) {
              scheduleData.month = dayjs(scheduleData.month);
            }

            console.log('准备设置到表单的数据:', JSON.stringify(scheduleData));

            // 手动构造表单数据对象，确保所有字段都被正确设置
            const formData = {
              id: scheduleData.id,
              personName: scheduleData.personName,
              workGroupName: scheduleData.workGroupName,
              idCard: scheduleData.idCard,
              month: scheduleData.month,
              classes: scheduleData.classes,
              remarks: scheduleData.remarks,
            };

            console.log('处理后的表单数据:', formData);

            // 直接更新modelRef引用，这会自动更新表单
            modelRef.value = {
              id: scheduleData.id,
              personName: scheduleData.personName,
              workGroupName: scheduleData.workGroupName,
              idCard: scheduleData.idCard,
              month: scheduleData.month,
              classes: scheduleData.classes,
              remarks: scheduleData.remarks || '',
            };

            console.log('直接更新modelRef:', modelRef.value);

            // 使用延迟确保表单已渲染后再次设置值
            setTimeout(() => {
              console.log('延迟设置表单值');
              setFieldsValue(modelRef.value);
            }, 200);
          } catch (error) {
            console.error('加载排班详情失败', error);
            createMessage.error('加载排班详情失败');
          } finally {
            loading.value = false;
          }
        }
      });

      // 提交表单
      async function handleSubmit() {
        try {
          const values = await validate();
          setModalProps({ confirmLoading: true });

          // 处理日期格式
          if (values.month) {
            if (typeof values.month === 'object') {
              values.month = dayjs(values.month).format('YYYY-MM');
            }
          }

          // 添加ID（编辑模式）
          if (isUpdate.value) {
            values.id = rowId.value;
          }

          // 保存数据
          await saveStaffSchedule(values);
          createMessage.success(`${isUpdate.value ? '编辑' : '添加'}排班成功！`);

          // 关闭模态框并刷新列表
          closeModal();
          emit('success');
        } catch (error) {
          console.error('保存排班失败', error);
          createMessage.error(`${isUpdate.value ? '编辑' : '添加'}排班失败！`);
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      return {
        registerModal,
        registerForm,
        handleSubmit,
        loading,
        isUpdate,
        modelRef, // 添加modelRef到返回值
      };
    },
  });
</script>

<style scoped>
  .loading-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 200px;
  }
</style>
