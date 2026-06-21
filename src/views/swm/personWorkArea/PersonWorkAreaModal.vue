<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    @ok="handleSubmit"
    :width="760"
    :confirmLoading="confirmLoading"
    destroyOnClose
  >
    <template #title>
      <Icon icon="ant-design:link-outlined" class="pr-1 m-1" />
      <span>绑定人员工作区域</span>
    </template>

    <BasicTable @register="registerAreaTable" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { nextTick, ref } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicTable, BasicColumn, useTable } from '@/components/swm/Table';
  import { Icon } from '@/components/swm/Icon';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { getAllAreaList } from '@/api/swm/area';
  import {
    deletePersonWorkArea,
    getPersonWorkAreaList,
    savePersonWorkArea,
  } from '@/api/swm/personWorkArea';

  const emit = defineEmits(['success', 'register']);
  const { createMessage } = useMessage();
  const confirmLoading = ref(false);
  const identityCards = ref<string[]>([]);
  const existingBindings = ref<Recordable[]>([]);

  const areaColumns: BasicColumn[] = [
    {
      title: '区域名称',
      dataIndex: 'areaName',
      width: 220,
    },
  ];

  const [registerAreaTable, { reload, getSelectRows, clearSelectedRowKeys, setSelectedRowKeys }] =
    useTable({
      api: getAreaTableData,
      rowKey: 'id',
      columns: areaColumns,
      pagination: false,
      showIndexColumn: false,
      showTableSetting: false,
      canResize: false,
      maxHeight: 420,
      rowSelection: {
        type: 'checkbox',
      },
    });

  const [registerModal, { closeModal }] = useModalInner(async (data) => {
    identityCards.value = data?.identityCards || [];
    existingBindings.value = [];
    clearSelectedRowKeys();
    if (isSingleBind()) {
      existingBindings.value = await loadExistingBindings(identityCards.value[0]);
    }
    await reload();
    if (isSingleBind()) {
      await nextTick();
      setSelectedRowKeys(getExistingAreaIds());
    }
  });

  function isSingleBind() {
    return identityCards.value.length === 1;
  }

  async function loadExistingBindings(identityCard: string) {
    const result = await getPersonWorkAreaList({
      identityCard,
      pageSize: 9999,
    });
    return normalizeList(result);
  }

  async function getAreaTableData() {
    const result = await getAllAreaList({ areaType: '0' });
    const list = normalizeList(result)
      .filter((item) => item.id)
      .map((item) => ({
        ...item,
        id: String(item.id),
      }));
    return {
      list,
      total: list.length,
    };
  }

  function normalizeList(result: any) {
    if (Array.isArray(result)) {
      return result;
    }
    if (Array.isArray(result?.data)) {
      return result.data;
    }
    if (Array.isArray(result?.list)) {
      return result.list;
    }
    if (Array.isArray(result?.items)) {
      return result.items;
    }
    if (Array.isArray(result?.records)) {
      return result.records;
    }
    if (Array.isArray(result?.rows)) {
      return result.rows;
    }
    if (Array.isArray(result?.page?.list)) {
      return result.page.list;
    }
    return [];
  }

  async function handleSubmit() {
    if (!identityCards.value.length) {
      createMessage.warning('请先在列表选择人员');
      return;
    }

    const selectedAreaIds = getSelectRows()
      .map((item) => item.id)
      .filter(Boolean);
    if (!selectedAreaIds.length && !isSingleBind()) {
      createMessage.warning('请选择区域');
      return;
    }

    try {
      confirmLoading.value = true;
      if (isSingleBind()) {
        await saveSingleBind(selectedAreaIds);
        createMessage.success('绑定成功');
      } else {
        const result: any = await savePersonWorkArea({
          isNewRecord: true,
          identityCards: identityCards.value.join(','),
          areaIds: selectedAreaIds.join(','),
        } as any);
        createMessage.success(result?.message || '绑定成功');
      }
      closeModal();
      emit('success');
    } finally {
      confirmLoading.value = false;
    }
  }

  async function saveSingleBind(selectedAreaIds: string[]) {
    const bindIds = getExistingBindIds();

    await Promise.all(bindIds.map((id) => deletePersonWorkArea(id)));
    if (selectedAreaIds.length) {
      await savePersonWorkArea({
        isNewRecord: true,
        identityCards: identityCards.value.join(','),
        areaIds: selectedAreaIds.join(','),
      } as any);
    }
  }

  function getExistingAreaIds() {
    return splitIds(
      existingBindings.value.flatMap((item) => [item.areaIds, item.areaId]).join(','),
    );
  }

  function getExistingBindIds() {
    return splitIds(existingBindings.value.map((item) => item.bindIds).join(','));
  }

  function splitIds(value?: string) {
    return String(value || '')
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean);
  }
</script>
