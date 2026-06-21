<template>
  <div class="header-nav">
    <div class="nav-tabs">
      <div
        v-for="tab in tabs"
        :key="tab"
        :class="['nav-tab', { active: activeTab === tab }]"
        @click="handleTabClick(tab)"
      >
        <span v-if="tab === '安全管理'" class="tab-icon">
          <Icon icon="ant-design:safety-outlined" />
        </span>
        <span v-else-if="tab === '劳动力管理'" class="tab-icon">
          <Icon icon="ant-design:team-outlined" />
        </span>
        {{ tab }}
      </div>
    </div>
    <div class="nav-actions">
      <a-button type="primary" @click="showFallAlert"> 演示触发跌落报警 </a-button>
      <a-button type="primary" @click="showSOSAlert" style="margin-left: 12px">
        演示触发应急呼叫
      </a-button>
    </div>

    <!-- 跌落报警弹窗 -->
    <a-modal
      v-model:visible="fallAlertVisible"
      title="跌落报警"
      :footer="null"
      width="400px"
      :maskClosable="false"
      centered
      wrapClassName="global-modal"
    >
      <div class="alert-content">
        <div class="alert-title"
          >工人: 张三 在 一车间-打磨工序区域触发报警。信标位置: XBW-01,XBW-03</div
        >
        <div class="alert-actions">
          <a-button type="primary" @click="fallAlertVisible = false">确定</a-button>
        </div>
      </div>
    </a-modal>

    <!-- 一键SOS弹窗 -->
    <a-modal
      v-model:visible="sosAlertVisible"
      title="应急呼叫"
      :footer="null"
      width="400px"
      :maskClosable="false"
      centered
      wrapClassName="global-modal"
    >
      <div class="alert-content">
        <div class="alert-title"
          >工人: 张三 在 一车间-打磨工序区域触发报警。信标位置: XBW-01,XBW-03</div
        >
        <div class="alert-actions">
          <a-button type="primary" @click="sosAlertVisible = false">确定</a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
  import { defineProps, defineEmits, ref } from 'vue';
  import { Icon } from '@/components/swm/Icon';
  import { Button as AButton, Modal as AModal } from 'ant-design-vue';

  const props = defineProps({
    activeTab: {
      type: String,
      default: '安全管理',
    },
  });

  const emit = defineEmits(['change']);

  const tabs = ref(['安全管理', '劳动力管理']);

  // 弹窗可见性控制
  const fallAlertVisible = ref(false);
  const sosAlertVisible = ref(false);

  const handleTabClick = (tab: string) => {
    emit('change', tab);
  };

  // 显示跌落报警弹窗
  const showFallAlert = () => {
    fallAlertVisible.value = true;
  };

  // 显示一键SOS弹窗
  const showSOSAlert = () => {
    sosAlertVisible.value = true;
  };

  const handleAction = (action: string) => {
    console.log('执行操作:', action);
  };
</script>

<style lang="less" scoped>
  .header-nav {
    padding: 0 16px;
    height: 56px;
    background: #fff;
    display: flex;
    align-items: center;
    justify-content: space-between;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
    border-radius: 4px;

    .nav-tabs {
      display: flex;
      height: 100%;
    }

    .nav-tab {
      padding: 0 20px;
      height: 100%;
      display: flex;
      align-items: center;
      cursor: pointer;
      font-size: 15px;
      color: #515a6e;
      border-bottom: 2px solid transparent;
      transition: all 0.3s;

      &:hover {
        color: #2d8cf0;
      }

      &.active {
        color: #2d8cf0;
        border-bottom-color: #2d8cf0;
      }

      .tab-icon {
        margin-right: 6px;
        display: flex;
        align-items: center;
      }
    }
  }

  .alert-content {
    text-align: center;

    .alert-title {
      font-size: 16px;
      color: #f5222d;
      margin-bottom: 24px;
      font-weight: bold;
    }

    .alert-actions {
      margin-top: 16px;
    }
  }
</style>

<style lang="less">
  /* 全局样式，不使用scoped */
  .global-modal {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;

    .ant-modal {
      top: 0 !important;
      padding-bottom: 0;
      margin: 0 auto;
    }
  }
</style>
