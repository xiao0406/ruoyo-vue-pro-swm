<!--
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author 冼国文
-->
<template>
  <PageWrapper :sidebarWidth="230">
    <template #sidebar>
      <BasicTree
        ref="treeRef"
        :title="t('分组')"
        :search="true"
        :toolbar="true"
        :showIcon="true"
        :actionList="actionList"
        :api="companyTreeData"
        :defaultExpandLevel="2"
        @select="handleSelect"
      />
    </template>
    <ListView :treeCode="treeCode" @reload="reload" />
  </PageWrapper>
</template>
<script lang="ts">
  export default defineComponent({
    name: 'ViewsMonitorDeviceInfoIndex',
  });
</script>
<script lang="ts" setup>
  import { defineComponent, ref } from 'vue';
  import { useI18n } from '@/hooks/swm/useI18n';
  import { PageWrapper } from '@/components/swm/Page';
  import { BasicTree } from '@/components/swm/Tree';
  import { companyTreeData } from '@/api/sys/monitorDeviceInfo';
  import ListView from './list.vue';

  const { t } = useI18n();
  const treeCode = ref<string>('');
  const treeRef = ref();

  function handleSelect(keys: string[]) {
    treeCode.value = keys[0];
  }

  function reload() {
    treeRef.value.reload();
  }

  const actionList = [
    {
      render: () => '测试',
      show: true,
    },
  ];
</script>
