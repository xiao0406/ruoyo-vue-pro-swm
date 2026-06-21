<template>
  <BasicModal
    v-bind="$attrs"
    :showFooter="true"
    @register="registerModal"
    :showOkBtn="false"
    width="80%"
  >
    <template #title>
      <Icon :icon="getTitle.icon" class="pr-1 m-1" />
      <span> {{ getTitle.value }} </span>
    </template>

    <v-md-editor v-model="record.text" mode="preview"></v-md-editor>
  </BasicModal>
</template>
<script lang="ts">
  export default defineComponent({
    name: 'ViewsSwmSwmDifyForm',
  });
</script>
<script lang="ts" setup>
  import { defineComponent, ref, computed } from 'vue';
  import { router } from '@/router';
  import { Icon } from '@/components/swm/Icon';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { swmDifyForm } from '@/api/swm/swmDify';

  const emit = defineEmits(['success', 'register']);

  const getTitle = computed(() => ({
    icon: router.currentRoute.value.meta.icon || 'ant-design:book-outlined',
    value: '日报',
  }));

  const record = ref<Recordable>({});

  const [registerModal, { setModalProps }] = useModalInner(async (data) => {
    setModalProps({ loading: true });
    const res = await swmDifyForm(data);
    record.value = res.swmDify || {};
    console.log(record.value, 'record.value');
    setModalProps({ loading: false });
  });
</script>
