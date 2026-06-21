<template>
  <div class="labor-content">
    <!-- 左侧区域 -->
    <div class="content-left">
      <!-- 统计卡片和环形图 -->
      <div class="data-row">
        <div class="data-col" style="flex: 1">
          <div class="card-group">
            <AttendanceCard title="今日工人考勤" :data="attendanceData.worker" />
            <AttendanceCard
              title="今日管理层考勤"
              :data="attendanceData.manager"
              style="margin-top: 16px"
            />
          </div>
        </div>
        <div class="data-col" style="flex: 1">
          <LiquidFillChart title="本月平均工效" :percentage="90" color="#4080ff" />
        </div>
        <div class="data-col" style="flex: 1">
          <CircleChart title="本月考勤达成率" :percentage="80" color="#4080ff" />
        </div>
      </div>

      <!-- 图表行 -->
      <div class="data-row">
        <div class="data-col" style="flex: 1">
          <LineBarChart title="本月出勤统计" :data="monthAttendanceData" type="attendance" />
        </div>
        <div class="data-col" style="flex: 1">
          <LineBarChart title="本月工效统计" :data="monthEfficiencyData" type="efficiency" />
        </div>
      </div>
    </div>

    <!-- 右侧排名区域 -->
    <div class="content-right">
      <div class="data-col" style="margin-bottom: 16px">
        <RankingTable title="今日出勤、工效排名" :data="todayRankingData" type="today" />
      </div>
      <div class="data-col" style="margin-bottom: 16px">
        <RankingTable title="工种分布" :data="workTypeData" type="workType" />
      </div>
      <div class="data-col">
        <RankingTable title="本月出勤、工效排名" :data="monthRankingData" type="month" />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive, onMounted } from 'vue';
  import AttendanceCard from '../components/AttendanceCard.vue';
  import CircleChart from '../components/CircleChart.vue';
  import LiquidFillChart from '../components/LiquidFillChart.vue';
  import LineBarChart from '../components/LineBarChart.vue';
  import RankingTable from '../components/RankingTable.vue';

  // 考勤数据
  const attendanceData = reactive({
    worker: {
      shouldAttend: 1000,
      actualAttend: 950,
      attendRate: '95%',
      inPosition: 945,
      onDuty: 940,
    },
    manager: {
      shouldAttend: 100,
      actualAttend: 98,
      attendRate: '98%',
      inPosition: 98,
      onDuty: 97,
    },
  });

  // 今日排名数据
  const todayRankingData = ref([
    { rank: 1, name: '打磨工班', attendees: 10, attendRate: '100%', efficiency: '95%' },
    { rank: 2, name: '打磨班组', attendees: 10, attendRate: '90%', efficiency: '96%' },
    { rank: 3, name: '涂漆班组', attendees: 9, attendRate: '94%', efficiency: '99%' },
  ]);

  // 工种分布数据
  const workTypeData = ref([
    { rank: 1, name: '油漆工', attendees: 15, attendRate: '100%', efficiency: '95%' },
    { rank: 2, name: '安装工', attendees: 12, attendRate: '90%', efficiency: '96%' },
    { rank: 3, name: '电气工', attendees: 10, attendRate: '94%', efficiency: '99%' },
  ]);

  // 本月排名数据
  const monthRankingData = ref([
    { rank: 1, name: '打磨班组', attendees: 10, attendRate: '100%', efficiency: '95%' },
    { rank: 2, name: '打磨班组', attendees: 10, attendRate: '90%', efficiency: '96%' },
    { rank: 3, name: '涂漆班组', attendees: 9, attendRate: '94%', efficiency: '99%' },
  ]);

  // 本月出勤统计数据（示例）
  const monthAttendanceData = ref({
    days: Array.from({ length: 31 }, (_, i) => i + 1),
    shouldAttend: Array.from({ length: 31 }, () => Math.floor(Math.random() * 30) + 100),
    actualAttend: Array.from({ length: 31 }, () => Math.floor(Math.random() * 30) + 90),
    attendRate: Array.from({ length: 31 }, () => (Math.random() * 0.2 + 0.8) * 100),
  });

  // 本月工效统计数据（示例）
  const monthEfficiencyData = ref({
    days: Array.from({ length: 31 }, (_, i) => i + 1),
    shouldEfficiency: Array.from({ length: 31 }, () => Math.floor(Math.random() * 20) + 90),
    actualEfficiency: Array.from({ length: 31 }, () => Math.floor(Math.random() * 20) + 80),
    efficiency: Array.from({ length: 31 }, () => (Math.random() * 0.3 + 0.7) * 100),
  });

  onMounted(() => {
    console.log('劳动力管理页面已加载');
  });
</script>

<style lang="less" scoped>
  .labor-content {
    display: flex;
    gap: 16px;
    margin-top: 16px;

    .content-left {
      flex: 3;
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .content-right {
      flex: 1;
      display: flex;
      flex-direction: column;
    }

    .data-row {
      display: flex;
      gap: 16px;
    }

    .data-col {
      background: #fff;
      border-radius: 4px;
      padding: 16px;
      box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);
      flex: 1;
    }

    .card-group {
      height: 100%;
      display: flex;
      flex-direction: column;
    }
  }
</style>
