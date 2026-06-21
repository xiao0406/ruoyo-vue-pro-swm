<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    @ok="handleSubmit"
    :width="800"
  >
    <div style="padding: 20px 20px">
      <BasicForm @register="registerForm">
        <template #color>
          <div class="color-picker-wrapper">
            <div class="color-selector-container">
              <ColorPicker
                :hex="currentColor"
                @colorHex="handleColorChange"
                class="color-picker-component"
              />
            </div>
          </div>
        </template>
      </BasicForm>
    </div>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicForm, useForm } from '@/components/swm/Form/index';
  import { ColorPicker } from '@/components/swm/ColorPicker';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { updateAreaBeaconColors } from '@/api/swm/area';

  export default defineComponent({
    name: 'BeaconColorModal',
    components: { BasicModal, BasicForm, ColorPicker },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const isUpdate = ref(true); // 固定为编辑模式，因为只允许编辑区域
      const rowId = ref('');
      const { createMessage } = useMessage();
      const currentColor = ref('#FF0000');
      const formData = ref({
        name: '',
        color: '#FF0000',
        remarks: '',
      });

      const [registerForm, { setFieldsValue, validate, getFieldsValue }] = useForm({
        labelWidth: 100,
        schemas: [
          {
            field: 'name',
            label: '区域名称',
            component: 'Input',
            componentProps: {
              disabled: true, // 区域名称不允许编辑
              placeholder: '区域名称（不可编辑）',
            },
            required: false,
          },
          {
            field: 'color',
            label: '区域颜色',
            component: 'Input',
            slot: 'color',
            required: true,
          },
          {
            field: 'remarks',
            label: '备注',
            component: 'InputTextArea',
            componentProps: {
              placeholder: '请输入备注信息',
              rows: 4,
            },
          },
        ],
        showActionButtonGroup: false,
        actionColOptions: {
          span: 24,
        },
        model: formData.value,
      });

      // 处理颜色变化
      function handleColorChange(e: any) {
        if (e && e.hex) {
          const color = '#' + e.hex;
          currentColor.value = color;

          // 保存当前表单的所有值
          const currentValues = getFieldsValue();

          // 只更新颜色字段，保留其他字段值
          formData.value = {
            ...formData.value,
            color: color,
          };

          // 同步更新表单值，但保留其他字段不变
          setFieldsValue({
            ...currentValues,
            color,
          });
        }
      }

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: false });

        if (data?.record) {
          // 编辑模式：设置当前记录数据
          rowId.value = data.record.id;
          const record = data.record;

          // 设置表单数据
          formData.value = {
            name: record.name || '',
            color: record.color || '#ff0000',
            remarks: record.remarks || '',
          };

          // 设置当前颜色
          currentColor.value = record.color || '#ff0000';

          // 同步到表单
          setFieldsValue({
            name: record.name || '',
            color: record.color || '#ff0000',
            remarks: record.remarks || '',
          });
        }
      });

      const getTitle = computed(() => '编辑区域颜色');

      async function handleSubmit() {
        try {
          const values = await validate();
          setModalProps({ confirmLoading: true });

          // 调用API更新区域信标颜色
          const updateData = {
            areaId: rowId.value,
            beaconColor: values.color,
            remarks: values.remarks,
          };

          const response = await updateAreaBeaconColors(updateData);

          if (response && response.success) {
            createMessage.success(response.message || '更新区域颜色成功！');
            closeModal();
            emit('success');
          } else {
            createMessage.error(response?.message || '更新区域颜色失败');
          }
        } catch (error: any) {
          console.error('更新区域颜色失败:', error);
          createMessage.error('更新区域颜色失败，请重试');
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      return {
        registerModal,
        registerForm,
        getTitle,
        handleSubmit,
        currentColor,
        handleColorChange,
        formData,
      };
    },
  });
</script>

<style lang="less" scoped>
  .color-picker-wrapper {
    position: relative;
    margin-bottom: 15px;

    .color-selector-container {
      position: relative;
      display: flex;

      :deep(.color_selector) {
        z-index: 2;
        cursor: pointer;
      }

      :deep(.vc-sketch) {
        position: fixed !important;
        top: 50% !important;
        left: 50% !important;
        transform: translate(-50%, -50%) !important;
        z-index: 10000 !important;
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3) !important;
        border-radius: 8px !important;
      }
    }
  }

  :deep(.ant-modal-body) {
    padding: 20px 24px;
  }

  :deep(.ant-form-item) {
    margin-bottom: 18px;
  }

  /* 确保颜色选择器弹出层始终在最上层 */
  :deep(.vc-sketch-container),
  :deep(.vc-container),
  :deep(.vc-sketch),
  :deep(.vc-popover) {
    z-index: 10000 !important;
  }

  /* 添加覆盖整个屏幕的遮罩层 */
  :deep(.vc-popover-content-wrapper) {
    position: fixed !important;
    top: 0 !important;
    left: 0 !important;
    width: 100vw !important;
    height: 100vh !important;
    display: flex !important;
    justify-content: center !important;
    align-items: center !important;
    z-index: 9999 !important;
    background-color: rgba(0, 0, 0, 0.5) !important;
  }
</style>
