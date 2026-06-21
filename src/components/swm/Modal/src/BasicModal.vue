<template>
  <a-modal
    v-model:open="visible"
    :title="title"
    :width="width"
    :confirm-loading="confirmLoading"
    :destroy-on-close="destroyOnClose"
    :centered="centered"
    :mask-closable="maskClosable"
    :footer="footer"
    :z-index="zIndex"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <a-spin :spinning="spinning">
      <slot />
    </a-spin>
  </a-modal>
</template>

<script lang="ts" setup>
  import { ref, watch, computed } from 'vue';
  import { Modal as AModal, Spin as ASpin } from 'ant-design-vue';

  const props = withDefaults(
    defineProps<{
      title?: string;
      width?: number | string;
      canFullscreen?: boolean;
      footer?: any;
      destroyOnClose?: boolean;
      centered?: boolean;
      maskClosable?: boolean;
      zIndex?: number;
      confirmLoading?: boolean;
      loading?: boolean;
      draggable?: boolean;
      showOkBtn?: boolean;
      showCancelBtn?: boolean;
      defaultFullscreen?: boolean;
      height?: number;
      minHeight?: number;
      bodyStyle?: any;
      visible?: boolean;
    }>(),
    {
      title: '',
      width: 520,
      destroyOnClose: true,
      centered: false,
      maskClosable: true,
      canFullscreen: false,
      confirmLoading: false,
      loading: false,
      showOkBtn: true,
      showCancelBtn: true,
      defaultFullscreen: false,
    },
  );

  const emit = defineEmits(['register', 'ok', 'cancel', 'visible-change', 'update:visible']);

  const visible = ref(false);
  const spinning = computed(() => props.loading);
  const confirmLoadingState = ref(false);

  // Sync with parent's visible state
  watch(
    () => props.visible,
    (val) => {
      if (val !== undefined) visible.value = val;
    },
    { immediate: true },
  );

  watch(visible, (val) => {
    emit('visible-change', val);
    emit('update:visible', val);
  });

  function handleOk() {
    emit('ok');
  }

  function handleCancel() {
    visible.value = false;
    emit('cancel');
  }

  // Expose methods for useModalInner
  defineExpose({
    open: () => {
      visible.value = true;
    },
    close: () => {
      visible.value = false;
    },
    setVisible: (val: boolean) => {
      visible.value = val;
    },
    setConfirmLoading: (val: boolean) => {
      confirmLoadingState.value = val;
    },
  });

  emit('register', {
    visibleRef: visible,
    confirmLoadingRef: confirmLoadingState,
    open: () => {
      visible.value = true;
    },
    close: () => {
      visible.value = false;
    },
  });
</script>
