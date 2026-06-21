<template>
  <a-modal
    :visible="visible"
    @update:visible="(val) => $emit('update:visible', val)"
    :title="null"
    :footer="null"
    width="500px"
    centered
    :mask-closable="false"
    :closable="false"
    class="recall-modal"
  >
    <div class="recall-modal-content">
      <div class="modal-header">
        <div class="modal-title">确认向 {{ personName }} 推送信息吗?</div>
      </div>
      <div class="form-content">
        <div class="form-item">
          <div class="label-item">
            <div class="dot-marker"></div>
            <span class="label-text">请选择语音模板</span>
          </div>
          <div class="template-area">
            <a-select
              v-model:value="selectedTemplate"
              class="simple-select"
              :bordered="true"
              :dropdown-match-select-width="false"
              placement="bottomLeft"
            >
              <a-select-option value="气体泄漏撤离">气体泄漏撤离</a-select-option>
              <a-select-option value="火灾撤离">火灾撤离</a-select-option>
              <a-select-option value="紧急撤离">紧急撤离</a-select-option>
            </a-select>
          </div>
        </div>
        <div class="form-item">
          <div class="label-item">
            <div class="dot-marker"></div>
            <span class="label-text">音频内容</span>
          </div>
          <a-textarea
            v-model:value="templateContent"
            :rows="4"
            placeholder="请输入"
            class="custom-textarea"
          ></a-textarea>
        </div>
        <div class="form-actions">
          <a-button class="cancel-btn" @click="handleCancel">取 消</a-button>
          <a-button type="primary" class="confirm-btn" @click="handleConfirm">推 送</a-button>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
  import { ref, defineProps, defineEmits } from 'vue';
  import { Modal as AModal, Button as AButton, Input, Select, message } from 'ant-design-vue';

  const ATextarea = Input.TextArea;
  const ASelect = Select;
  const ASelectOption = Select.Option;

  const props = defineProps({
    visible: {
      type: Boolean,
      required: true,
    },
    personName: {
      type: String,
      default: '张三',
    },
  });

  const emit = defineEmits(['update:visible', 'confirm', 'cancel']);

  const selectedTemplate = ref('紧急撤离');
  const templateContent = ref('');

  const handleCancel = () => {
    templateContent.value = '';
    emit('cancel');
    emit('update:visible', false);
  };

  const handleConfirm = () => {
    emit('confirm', {
      template: selectedTemplate.value,
      content: templateContent.value,
    });
    message.success(`已成功向 ${props.personName} 推送信息`);
    templateContent.value = '';
    emit('update:visible', false);
  };
</script>

<style lang="less" scoped>
  .recall-modal {
    :deep(.ant-modal-content) {
      background-color: #fff;
      border-radius: 0;
      box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
      overflow: hidden;
      padding: 0;
    }

    :deep(.ant-modal-body) {
      padding: 0;
    }
  }

  .recall-modal-content {
    .modal-header {
      background-color: #003366;
      padding: 16px 24px;
      text-align: center;

      .modal-title {
        color: white;
        font-size: 18px;
        font-weight: normal;
        text-align: center;
      }
    }

    .form-content {
      padding: 20px;

      .form-item {
        margin-bottom: 20px;
        background-color: #f9f9f9;
        border-radius: 0;
        padding: 16px;

        .label-item {
          position: relative;
          padding-left: 16px;
          margin-bottom: 12px;

          .dot-marker {
            position: absolute;
            left: 0;
            top: 50%;
            transform: translateY(-50%);
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background-color: #003366;
          }

          .label-text {
            font-size: 14px;
          }
        }

        .template-area {
          padding: 6px 0;

          .simple-select {
            width: 100%;

            :deep(.ant-select-selector) {
              padding-left: 12px;
              background-color: #fff;
              border: 1px solid #003366 !important;
              border-radius: 4px;
              height: 38px !important;
              box-shadow: none;
              transition: all 0.3s;

              &:hover {
                background-color: #f0f7ff;
                border-color: #0055b3 !important;
              }
            }

            :deep(.ant-select-selection-item) {
              font-size: 14px;
              color: #333;
              line-height: 38px;
              font-weight: 500;
            }

            :deep(.ant-select-arrow) {
              color: #003366;
              font-size: 16px;
              margin-right: 6px;
            }

            :deep(.ant-select-dropdown) {
              border: 1px solid #003366;
              border-radius: 4px;

              .ant-select-item {
                padding: 8px 12px;

                &.ant-select-item-option-selected {
                  background-color: rgba(0, 51, 102, 0.1);
                  font-weight: bold;
                }

                &:hover {
                  background-color: rgba(0, 51, 102, 0.05);
                }
              }
            }
          }
        }

        .custom-textarea {
          border: 1px solid #d9d9d9;
          border-radius: 0;

          :deep(.ant-input) {
            background-color: white;
          }
        }
      }

      .form-actions {
        display: flex;
        justify-content: center;
        margin-top: 30px;
        gap: 16px;

        .cancel-btn {
          min-width: 100px;
          height: 36px;
          background-color: #f0f2f5;
          border: none;
          border-radius: 2px;
          color: #333;
        }

        .confirm-btn {
          min-width: 100px;
          height: 36px;
          background-color: #003366;
          border: none;
          border-radius: 2px;
        }
      }
    }
  }
</style>
