<template>
  <BasicModal
    v-bind="$attrs"
    :showFooter="true"
    @register="registerModal"
    @ok="handleSubmit"
    width="85%"
  >
    <PageWrapper :sidebarWidth="230">
      <template #sidebar>
        <BasicTree
          ref="treeRef"
          title="组织"
          :toolbar="true"
          :showIcon="true"
          :treeData="treeData"
          :loadData="onLoadData"
          :loadedKeys="loadedKeys"
          :expandOnSearch="false"
          @select="handleSelect"
          @reload="onReload"
        />
      </template>
      <ListView ref="listViewRef" :treeCode="treeCode" />
    </PageWrapper>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, unref } from 'vue';
  import { PageWrapper } from '@/components/swm/Page';
  import { BasicTree, TreeActionType, TreeItem } from '@/components/swm/Tree';
  import { getSubRegions } from '@/api/sys/monitorDeviceInfo';
  import ListView from './list.vue';
  import { isArray } from '@/utils/is';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { useMessage } from '@/hooks/swm/useMessage';

  const emit = defineEmits(['success']);

  const { showMessage } = useMessage();

  const listViewRef = ref<InstanceType<typeof ListView>>();

  const treeCode = ref<string>('');

  function handleSelect(keys: string[]) {
    treeCode.value = keys[0];
  }

  const treeRef = ref<Nullable<TreeActionType>>(null);
  const treeData = ref<TreeItem['treeData']>([]);
  const loadedKeys = ref<string[]>([]); // 已经加载的节点，需要配合 loadData 使用

  const onReload = async () => {
    treeCode.value = '';
    loadedKeys.value = [];
    treeRef.value?.setExpandedKeys([]);
    treeRef.value?.setSearchValue('');

    listViewRef.value?.clearSelectedRowKeys();

    treeData.value = await getTreeNodes({ code: 'root000000' });
  };

  // 获取树节点书记
  const getTreeNodes = async (params: { code: string }) => {
    const res = await getSubRegions(params);
    return res.data.list.map((item) => {
      return {
        id: item.indexCode,
        isParent: item.leaf ? false : true,
        disabled: item.leaf ? false : true, // 是否禁用
        name: item.name,
        pId: item.parentIndexCode,
      };
    });
  };

  const onLoadData: TreeItem['loadData'] = (treeNode) => {
    return new Promise(async (resolve: (value?: unknown) => void) => {
      if (isArray(treeNode.children) && treeNode.children.length > 0) {
        resolve();
        return;
      }
      const treeAction: TreeActionType | null = unref(treeRef);
      if (treeAction) {
        const children = await getTreeNodes({ code: treeNode.eventKey });
        treeAction.updateNodeByKey(treeNode.eventKey, { children });
        loadedKeys.value.push(treeNode.eventKey);
      }
      resolve();
      return;
    });
  };

  const [registerModal, { closeModal }] = useModalInner(async () => {
    onReload();
  });

  async function handleSubmit() {
    const selectRows = listViewRef.value?.getSelectRows();
    if (!selectRows || selectRows.length < 1) {
      showMessage('请选择监控设备');
      return;
    }

    emit('success', selectRows[0]);
    closeModal();
  }
</script>
<style scoped lang="less">
  ::v-deep(.ant-tree .ant-tree-treenode-disabled .ant-tree-node-content-wrapper) {
    color: #000000d9;
  }
</style>
