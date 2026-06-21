<template>
  <div class="data-screen-container">
    <!-- 顶部导航 -->
    <HeaderNav :activeTab="activeTab" @change="handleTabChange" />

    <!-- 安全管理视图 -->
    <SafetyView v-if="activeTab === '安全管理'" />

    <!-- 劳动力管理视图 -->
    <template v-else>
      <!-- 筛选栏 -->
      <FilterBar @filter-change="handleFilterChange" />

      <!-- 数据内容区 -->
      <div class="data-content">
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
              <LiquidFillChart
                title="本月平均工效"
                :percentage="monthlyEfficiencyData.avgEfficiency * 100"
                color="#4080ff"
              />
            </div>
            <div class="data-col" style="flex: 1">
              <CircleChart
                title="本月考勤达成率"
                :percentage="monthlyEfficiencyData.achievementRate * 100"
                color="#4080ff"
              />
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
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive, onMounted } from 'vue';
  import HeaderNav from './components/HeaderNav.vue';
  import FilterBar from './components/FilterBar.vue';
  import AttendanceCard from './components/AttendanceCard.vue';
  import CircleChart from './components/CircleChart.vue';
  import LineBarChart from './components/LineBarChart.vue';
  import RankingTable from './components/RankingTable.vue';
  import LiquidFillChart from './components/LiquidFillChart.vue';
  import SafetyView from './safety/index.vue';
  import { laborForce } from '@/api/swm/dataScreen';

  // 当前激活的标签页
  const activeTab = ref('安全管理');

  // 考勤数据
  const attendanceData = ref({
    worker: {
      totalCount: 0,
      onSiteCount: 0,
      attendanceRate: '0%',
      presentCount: 0,
      workingCount: 0,
    },
    manager: {
      totalCount: 0,
      onSiteCount: 0,
      attendanceRate: '0%',
      presentCount: 0,
      workingCount: 0,
    },
  });
  //本月平均工效和本月考勤达成率
  const monthlyEfficiencyData = ref({
    avgEfficiency: 0,
    achievementRate: 0,
  });
  //本月平均工效
  const formatMonthlyEfficiencyData = (data) => {
    return {
      days: data.dates.map((date) => date.split('-')[2]), // 取出日期中的日部分
      shouldEfficiency: data.scheduledHours, // 工效时长
      actualEfficiency: data.actualHours, // 实际工效时长
      efficiency: data.rate, // 工效率
    };
  };
  //本月考勤达成率
  const formatMonthlyAttendanceData = (data) => {
    return {
      days: data.dates.map((date) => date.split('-')[2]), // 取出日期中的日部分
      shouldAttend: data.scheduled, // 应考勤时长
      actualAttend: data.actual, // 实际考勤时长
      attendRate: data.rate, // 考勤率
    };
  };
  // 工种分布数据
  const formatWorkTypeData = (workTypeList) => {
    return workTypeList.map((item, index) => ({
      rank: index + 1,
      name: item.name,
      attendees: item.presentCount,
      attendRate: `${(item.attendanceRate * 100).toFixed(0)}%`,
      efficiency: `${(item.avgEfficiency * 100).toFixed(0)}%`,
    }));
  };

  const workTypeData = ref([
    { rank: 1, name: '油漆工', attendees: 35, attendRate: '100%', efficiency: '95%' },
  ]);
  // 月度班组排名数据
  const formatMonthlyTeamRanking = (rankingList) => {
    return rankingList.map((item, index) => ({
      rank: index + 1,
      name: item.name,
      attendees: item.presentCount,
      attendRate: `${(item.attendanceRate * 100).toFixed(0)}%`,
      efficiency: `${(item.avgEfficiency * 100).toFixed(0)}%`,
    }));
  };
  const monthRankingData = ref([
    { rank: 1, name: '', attendees: 0, attendRate: '100%', efficiency: '95%' },
  ]);
  // 今日出勤、工效排名
  const formatTodayTeamRanking = (rankingList) => {
    return rankingList.map((item, index) => ({
      rank: index + 1,
      name: item.name,
      attendees: item.presentCount,
      attendRate: `${(item.attendanceRate * 100).toFixed(0)}%`,
      efficiency: `${(item.avgEfficiency * 100).toFixed(0)}%`,
    }));
  };
  // 今日排名数据
  const todayRankingData = ref([
    { rank: 1, name: '打磨工班', attendees: 10, attendRate: '100%', efficiency: '95%' },
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

  // 处理标签页切换
  const handleTabChange = (tab: string) => {
    activeTab.value = tab;
    // 可以在这里重新加载数据
  };

  // 处理筛选条件变化
  const handleFilterChange = (filters: any) => {
    console.log('筛选条件变化:', filters);
    // 这里可以根据筛选条件重新加载数据
  };

  onMounted(async () => {
    try {
      const res = await laborForce(); //
      if (res) {
        // 本月出勤数据(图表)
        monthAttendanceData.value = formatMonthlyAttendanceData(res.monthlyAttendanceChart);
        // 本月工效数据(图表)
        monthEfficiencyData.value = formatMonthlyEfficiencyData(res.monthlyEfficiencyChart);
        //本月工效统计及本月考勤达成率
        monthlyEfficiencyData.value = res.monthlyEfficiency;
        //今日工人及管理层考勤
        attendanceData.value = res.todayAttendance;
        // 更新本月团队排名数据
        monthRankingData.value = formatMonthlyTeamRanking(res.monthlyTeamRanking);
        // 更新今日团队排名数据
        todayRankingData.value = formatTodayTeamRanking(res.todayTeamRanking);
        //工种分布
        workTypeData.value = formatWorkTypeData(res.todayJobDistribution);
      }
    } catch (error) {
      console.error('获取出勤数据失败:', error);
    }
  });
</script>

<style lang="less" scoped>
  .data-screen-container {
    min-height: 100vh;
    background-color: #f0f2f5;
    padding: 16px;

    .data-content {
      margin-top: 16px;
      display: flex;
      gap: 16px;
    }

    .content-left {
      flex: 3;
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .content-right {
      flex: 1.2;
      display: flex;
      flex-direction: column;
      min-width: 380px;
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
