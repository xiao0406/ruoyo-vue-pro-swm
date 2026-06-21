<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    title="开始巡检任务"
    @ok="handleSubmit"
    width="500px"
  >
    <div class="confirm-content">
      <p class="confirm-message">确认开始当前巡检任务？</p>
      <p class="confirm-detail">开始后，巡检任务状态将变为"进行中"。</p>
    </div>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { startInspectionTask } from '@/api/swm/inspectionList';
  import { useMessage } from '@/hooks/swm/useMessage';

  const emit = defineEmits(['success', 'register']);
  const taskId = ref('');
  const { createMessage } = useMessage();

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false });
    if (data?.record) {
      taskId.value = data.record.id;
    }
  });

  async function handleSubmit() {
    try {
      setModalProps({ confirmLoading: true });

      // 提交数据
      await startInspectionTask({ id: taskId.value });
      createMessage.success('巡检任务已开始!');
      closeModal();
      emit('success');
    } catch (error) {
      console.error('开始巡检任务失败:', error);
      createMessage.error('开始巡检任务失败，请重试');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
<style scoped>
  .confirm-content {
    padding: 16px 20px;
  }

  .confirm-message {
    font-size: 16px;
    font-weight: 500;
    margin-bottom: 16px;
  }

  .confirm-detail {
    color: #666;
  }
</style>
