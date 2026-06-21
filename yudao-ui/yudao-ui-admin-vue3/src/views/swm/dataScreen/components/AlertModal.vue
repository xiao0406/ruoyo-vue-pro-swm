<template>
  <a-modal
    v-model:visible="visible"
    :footer="null"
    :closable="false"
    :maskClosable="false"
    :width="400"
    class="alert-modal"
    wrapClassName="center-alert-modal"
  >
    <div class="alert-content">
      <div class="alert-title">{{ title }}</div>
      <div class="alert-message" v-html="message"></div>
      <div class="alert-footer">
        <a-button type="primary" @click="handleConfirm">确定</a-button>
      </div>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
  import { ref, defineExpose } from 'vue';
  import { Modal as AModal, Button as AButton } from 'ant-design-vue';

  const visible = ref(false);
  const title = ref('');
  const message = ref('');

  // 打开弹窗
  const showAlert = (alertTitle: string, alertMessage: string) => {
    title.value = alertTitle;
    message.value = alertMessage;
    visible.value = true;
  };

  // 关闭弹窗
  const handleConfirm = () => {
    visible.value = false;
  };

  // 对外暴露方法
  defineExpose({
    showAlert,
  });
</script>

<style lang="less" scoped>
  .alert-modal {
    :deep(.ant-modal-content) {
      border-radius: 8px;
      overflow: hidden;
    }

    :deep(.ant-modal-body) {
      padding: 0;
    }

    .alert-content {
      padding: 24px;
      text-align: center;

      .alert-title {
        font-size: 24px;
        font-weight: bold;
        margin-bottom: 16px;
        color: #f5222d;
      }

      .alert-message {
        font-size: 16px;
        margin-bottom: 24px;
        text-align: left;
        color: #f5222d;
        line-height: 1.8;
      }

      .alert-footer {
        display: flex;
        justify-content: center;

        .ant-btn {
          min-width: 90px;
        }
      }
    }
  }
</style>

<style lang="less">
  .center-alert-modal {
    display: flex;
    align-items: center;
    justify-content: center;

    .ant-modal {
      top: 0 !important;
      padding-bottom: 0 !important;
      margin: 0 auto !important;
      max-height: 100vh;
      display: flex;
      align-items: center;

      &-content {
        top: 50%;
        transform: translateY(-50%);
      }
    }
  }
</style>
