<template>
  <div class="safety-container" :class="{ 'full-width': !isAlarmTab }">
    <!-- 报警报警标签页 -->
    <template v-if="isAlarmTab">
      <div class="vertical-layout">
        <!-- 导航菜单（水平排列） -->
        <div class="menu-panel">
          <div class="menu-item" :class="{ active: isAlarmTab }" @click="switchTab('alarm')">
            <i class="menu-icon">
              <Icon icon="ant-design:eye-outlined" />
            </i>
            <span>报警</span>
          </div>
          <div class="menu-item" :class="{ active: isDangerTab }" @click="switchTab('danger')">
            <i class="menu-icon">
              <Icon icon="ant-design:alert-outlined" />
            </i>
            <span>风险</span>
          </div>
          <div class="menu-item" :class="{ active: isSafetyTab }" @click="switchTab('safety')">
            <i class="menu-icon">
              <Icon icon="ant-design:heat-map-outlined" />
            </i>
            <span>安全隐患</span>
          </div>
        </div>

        <div class="alarm-content">
          <!-- 左侧区域：菜单和报警记录 -->
          <div class="left-section">
            <!-- 近七日报警统计 -->
            <div class="weekly-chart-panel">
              <WeeklyAlarmChart />
            </div>

            <!-- 今日报警记录 -->
            <div class="alarm-record-panel">
              <AlarmRecords />
            </div>
          </div>

          <div class="center-section">
            <!-- 顶部统计区域 -->
            <div class="stats-container">
              <div class="stat-card">
                <div class="stat-label">厂区人数：</div>
                <div class="stat-value">{{ warningData.personCount }}</div>
              </div>
              <div class="stat-card">
                <div class="stat-label">触发报警数：</div>
                <div class="stat-value">{{ warningData.warningCount }}</div>
              </div>
              <div class="stat-card">
                <div class="stat-label">触发预警数：</div>
                <div class="stat-value">{{ warningData.alarmCount }}</div>
              </div>
            </div>

            <!-- 地图区域 -->
            <div class="map-container">
              <SafetyMap />
            </div>
          </div>

          <!-- 右侧区域：现场人员状态 -->
          <div class="right-section">
            <PersonnelStatus />
          </div>
        </div>
      </div>
    </template>

    <!-- 风险标签页 -->
    <template v-else-if="isDangerTab">
      <div class="vertical-layout">
        <!-- 只保留导航菜单 -->
        <div class="menu-panel">
          <div class="menu-item" :class="{ active: isAlarmTab }" @click="switchTab('alarm')">
            <i class="menu-icon">
              <Icon icon="ant-design:eye-outlined" />
            </i>
            <span>报警</span>
          </div>
          <div class="menu-item" :class="{ active: isDangerTab }" @click="switchTab('danger')">
            <i class="menu-icon">
              <Icon icon="ant-design:alert-outlined" />
            </i>
            <span>风险</span>
          </div>
          <div class="menu-item" :class="{ active: isSafetyTab }" @click="switchTab('safety')">
            <i class="menu-icon">
              <Icon icon="ant-design:heat-map-outlined" />
            </i>
            <span>安全隐患</span>
          </div>
        </div>

        <!-- 风险内容 -->
        <div class="danger-content">
          <DangerSource />
        </div>
      </div>
    </template>

    <!-- 安全隐患标签页 -->
    <template v-else-if="isSafetyTab">
      <div class="vertical-layout">
        <!-- 只保留导航菜单 -->
        <div class="menu-panel">
          <div class="menu-item" :class="{ active: isAlarmTab }" @click="switchTab('alarm')">
            <i class="menu-icon">
              <Icon icon="ant-design:eye-outlined" />
            </i>
            <span>报警</span>
          </div>
          <div class="menu-item" :class="{ active: isDangerTab }" @click="switchTab('danger')">
            <i class="menu-icon">
              <Icon icon="ant-design:alert-outlined" />
            </i>
            <span>风险</span>
          </div>
          <div class="menu-item" :class="{ active: isSafetyTab }" @click="switchTab('safety')">
            <i class="menu-icon">
              <Icon icon="ant-design:heat-map-outlined" />
            </i>
            <span>安全隐患</span>
          </div>
        </div>

        <!-- 安全隐患内容 -->
        <div class="safety-content">
          <div class="coming-soon"> 安全隐患页面 - 开发中 </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script lang="ts" setup>
  import { ref, onMounted, computed } from 'vue';
  import { Icon } from '@/components/swm/Icon';
  import SafetyMap from './components/SafetyMap.vue';
  import PersonnelStatus from './components/PersonnelStatus.vue';
  import AlarmRecords from './components/AlarmRecords.vue';
  import WeeklyAlarmChart from './components/WeeklyAlarmChart.vue';
  import DangerSource from './DangerSource.vue';
  import { warningPersonCount } from '@/api/swm/dataScreen';

  // 定义标签页类型
  type TabType = 'alarm' | 'danger' | 'safety';
  const warningData = ref({
    alarmCount: 0,
    warningCount: 0,
    personCount: 0,
  });
  // 当前选中的标签
  const currentTab = ref < TabType > ('alarm');

  // 计算属性判断当前标签
  const isAlarmTab = computed(() => currentTab.value === 'alarm');
  const isDangerTab = computed(() => currentTab.value === 'danger');
  const isSafetyTab = computed(() => currentTab.value === 'safety');

  // 切换标签页
  const switchTab = (tab: TabType) => {
    currentTab.value = tab;
    console.log('切换到标签页:', tab);
  };

  onMounted(() => {
    warningPersonData();
  });
  // 厂区人数中间三项统计
  const warningPersonData = async () => {
    const res = await warningPersonCount(); // 接口调用
    warningData.value = res;
  };
</script>

<style lang="less" scoped>
  .safety-container {
    display: flex;
    height: calc(100vh - 72px); // 只减去header高度
    gap: 16px;
    padding: 16px;

    // 全宽模式（风险和安全隐患页面使用）
    &.full-width {
      display: block;
      padding: 16px;
    }

    // 垂直布局容器（用于风险和安全隐患页面）
    .vertical-layout {
      width: 100%;
      height: calc(100vh - 88px);
      /* 更精确地计算高度 */
      display: flex;
      flex-direction: column;
      gap: 16px;

      .menu-panel {
        width: 100%;
        background: #fff;
        border-radius: 4px;
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
        display: flex;
        flex-direction: row;
        height: 60px;
        flex-shrink: 0;

        .menu-item {
          flex: 1;
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          border-right: 1px solid #f0f2f5;

          &:last-child {
            border-right: none;
          }

          &:hover,
          &.active {
            background: #f0f6ff;
            color: #2d8cf0;
          }

          .menu-icon {
            font-size: 18px;
            margin-right: 6px;
          }
        }
      }

      .alarm-content {
        flex: 1;
        display: flex;
        gap: 16px;
        min-height: 0;
        /* 确保内部元素可以正确扩展 */
      }

      .danger-content {
        flex: 1;
        background-color: transparent;
        overflow: visible;
        display: flex;
        min-height: 0;
        /* 确保内部元素可以正确扩展 */
      }

      .safety-content {
        flex: 1;
        background: #fff;
        border-radius: 4px;
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
        display: flex;
        align-items: center;
        justify-content: center;

        .coming-soon {
          font-size: 20px;
          color: #999;
        }
      }
    }

    // 左侧区域
    .left-section {
      width: 280px;
      display: flex;
      flex-direction: column;
      gap: 16px;

      // 菜单面板
      .menu-panel {
        background: #fff;
        border-radius: 4px;
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
        display: flex;
        flex-direction: row;
        height: 60px;

        .menu-item {
          flex: 1;
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          border-right: 1px solid #f0f2f5;

          &:last-child {
            border-right: none;
          }

          &:hover,
          &.active {
            background: #f0f6ff;
            color: #2d8cf0;
          }

          .menu-icon {
            font-size: 18px;
            margin-right: 6px;
          }
        }
      }

      // 近七日报警统计面板
      .weekly-chart-panel {
        height: 280px;
        background: #fff;
        border-radius: 4px;
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
        overflow: hidden;
        padding: 10px 0;
      }

      // 报警记录面板
      .alarm-record-panel {
        flex: 1;
        background: #fff;
        border-radius: 4px;
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
        overflow: hidden;
        display: flex;
        flex-direction: column;
      }
    }

    // 中间区域
    .center-section {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 16px;

      // 统计卡片容器
      .stats-container {
        display: flex;
        gap: 16px;

        .stat-card {
          flex: 1;
          height: 60px;
          background: #fff;
          border-radius: 4px;
          padding: 0 16px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);

          .stat-label {
            font-size: 16px;
            color: #515a6e;
          }

          .stat-value {
            font-size: 20px;
            font-weight: bold;
            color: #17233d;
          }
        }
      }

      // 地图容器
      .map-container {
        flex: 1;
        background: transparent;
        border-radius: 4px;
        box-shadow: none;
        overflow: auto;
        display: flex;
        justify-content: center;
        align-items: center;
        min-height: 0;
        /* 确保flex布局正常工作 */
      }
    }

    // 右侧区域
    .right-section {
      width: 320px;
      background: #fff;
      border-radius: 4px;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
      overflow: hidden;
    }
  }

  // 风险和安全隐患内容区域的共同样式
  .content-wrapper {
    flex: 1;
    display: flex;
    overflow: hidden;
  }
</style>
