<template>
  <div class="color-config-container">
    <div class="title-info"> 颜色配置 </div>
    <!-- <PageWrapper title="系统颜色配置" contentFullHeight> -->
    <!-- <Card :bordered="false"> -->
    <a-tabs v-model:activeKey="activeKey" style="background-color: #fff">
      <a-tab-pane key="helmets" tab="安全帽颜色配置">
        <HelmetsColorConfig />
      </a-tab-pane>
      <a-tab-pane key="beacon" tab="信标颜色配置">
        <BeaconColorConfig />
      </a-tab-pane>
    </a-tabs>
    <!-- </Card> -->
    <!-- </PageWrapper> -->
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { PageWrapper } from '@/components/swm/Page';
  import { Card } from 'ant-design-vue';
  import { Tabs } from 'ant-design-vue';
  import HelmetsColorConfig from './components/HelmetsColorConfig.vue';
  import BeaconColorConfig from './components/BeaconColorConfig.vue';

  export default defineComponent({
    name: 'ViewsSwmColorConfigIndex',
    components: {
      PageWrapper,
      Card,
      HelmetsColorConfig,
      BeaconColorConfig,
      'a-tabs': Tabs,
      'a-tab-pane': Tabs.TabPane,
    },
    setup() {
      const activeKey = ref('helmets');

      return {
        activeKey,
      };
    },
  });
</script>

<style lang="less" scoped>
  .color-config-container {
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .title-info {
      font-size: 16px;
      font-weight: normal;
      background-color: #fff;
      margin-bottom: 10px;
      padding: 10px;
    }
  }
</style>
