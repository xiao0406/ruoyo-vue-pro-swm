<template>
  <div class="voice-templete-container">
    <VoiceTemplateTable ref="tableRef" @edit="handleEdit" @delete="handleDelete" @add="handleAdd" />
    <VoiceTemplateModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref } from 'vue';
  // import { PageWrapper } from '@/components/swm/Page';
  import { Card } from 'ant-design-vue';
  import { useModal } from '@/components/swm/Modal';
  import VoiceTemplateTable from './components/VoiceTemplateTable.vue';
  import VoiceTemplateModal from './components/VoiceTemplateModal.vue';

  export default defineComponent({
    name: 'ViewsSwmVoiceTemplateIndex',
    components: {
      // PageWrapper,
      Card,
      VoiceTemplateTable,
      VoiceTemplateModal,
    },
    setup() {
      const tableRef = ref<InstanceType<typeof VoiceTemplateTable>>();
      const [registerModal, { openModal }] = useModal();

      function handleAdd() {
        openModal(true, {
          isUpdate: false,
        });
      }

      function handleEdit(record: Recordable) {
        openModal(true, {
          record,
          isUpdate: true,
        });
      }

      function handleDelete() {
        // 删除已在表格组件中处理
      }

      function handleSuccess() {
        // 成功后刷新表格
        tableRef.value?.reload();
      }

      return {
        tableRef,
        registerModal,
        handleAdd,
        handleEdit,
        handleDelete,
        handleSuccess,
      };
    },
  });
</script>

<style lang="less" scoped>
  .voice-templete-container {
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    //     .header {
    //   display: flex;
    //   align-items: center;
    //   margin-bottom: 16px;
    //   height: 100%;

    //   .title {
    //     font-size: 16px;
    //     font-weight: bold;
    //   }
    // }
  }
</style>
