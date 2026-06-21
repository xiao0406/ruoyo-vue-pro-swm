<template>
  <div class="alarm-records">
    <div class="records-header">
      <div class="header-title">今日报警记录</div>
    </div>

    <!-- 报警统计卡片 -->
    <div class="alarm-stats">
      <div class="stat-card">
        <div class="stat-title">主动报警</div>
        <div class="stat-value">{{ warningCount.主动报警 ? warningCount.主动报警 : 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">跌落报警</div>
        <div class="stat-value">{{ warningCount.跌落报警 ? warningCount.跌落报警 : 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">静止报警</div>
        <div class="stat-value">{{ warningCount.静默报警 ? warningCount.静默报警 : 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">安全预警</div>
        <div class="stat-value">{{ warningCount.安全预警 ? warningCount.安全预警 : 0 }}</div>
      </div>
    </div>

    <!-- 报警记录列表 -->
    <div class="alarm-list">
      <div class="alarm-card" v-for="(record, index) in alarmData" :key="index">
        <div class="alarm-info">
          <div class="info-row">
            <span class="label">姓名：</span>
            <span class="value">{{ record.personName }}</span>
          </div>
          <div class="info-row">
            <span class="label">班组：</span>
            <!-- todo: 获取班组名称字段未找到 -->
            <!-- <span class="value">{{ record.team }}</span> -->
          </div>
          <div class="info-row">
            <span class="label">位置：</span>
            <span class="value">{{ record.triggerReason }}</span>
          </div>
          <div class="info-row">
            <span class="label">报警：</span>
            <span class="value">{{ record.warningContent }}</span>
          </div>
        </div>
        <!-- <div class="alarm-time">{{ record.time }}</div> -->
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { warningStatisticsForToday } from '@/api/swm/dataScreen';

  import { ref, onMounted } from 'vue';
  //定义变量
  const alarmData = ref([
    {
      personName: '',
      triggerReason: '',
      warningContent: '',
    },
  ]);
  const warningCount = ref({
    静默报警: 0,
    跌落报警: 0,
    主动报警: 0,
    安全预警: 0,
  });
  // 今日预警报警记录
  const warningForToday = async () => {
    const res = await warningStatisticsForToday(); // 接口调用
    alarmData.value = res.record;
    warningCount.value = res.warning;
  };
  onMounted(() => {
    warningForToday();
  });
</script>

<style lang="less" scoped>
  .alarm-records {
    height: 100%;
    padding: 16px;
    display: flex;
    flex-direction: column;

    .records-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 16px;

      .header-title {
        font-size: 16px;
        font-weight: 500;
        color: #17233d;
      }
    }

    .alarm-stats {
      display: flex;
      justify-content: space-between;
      gap: 10px;
      margin-bottom: 16px;

      .stat-card {
        flex: 1;
        height: 80px;
        background: #fff;
        border: 1px solid #e9e9e9;
        border-radius: 4px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;

        .stat-title {
          font-size: 14px;
          color: #515a6e;
          margin-bottom: 8px;
          white-space: nowrap;
        }

        .stat-value {
          font-size: 20px;
          font-weight: bold;
          color: #17233d;
        }
      }
    }

    .alarm-list {
      flex: 1;
      overflow-y: auto;

      .alarm-card {
        background: #fff;
        border: 1px solid #e9e9e9;
        border-radius: 4px;
        padding: 12px 16px;
        margin-bottom: 12px;
        display: flex;
        justify-content: space-between;
        align-items: center;

        .alarm-info {
          flex: 1;

          .info-row {
            margin-bottom: 8px;

            &:last-child {
              margin-bottom: 0;
            }

            .label {
              color: #515a6e;
              font-size: 13px;
            }

            .value {
              color: #17233d;
              font-size: 13px;
            }
          }
        }

        .alarm-time {
          font-size: 14px;
          color: #17233d;
          font-weight: bold;
        }
      }
    }
  }
</style>
