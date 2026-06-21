<template>
  <h1>本月考勤统计</h1>

  <!-- 员工基本信息 -->
  <table class="kq-table">
    <thead>
      <tr>
        <th colspan="6" class="kq-border-header color-1">员工基本信息</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td class="kq-border-header-top color-1" width="16.66%">员工名称</td>
        <td class="kq-border-header-center" width="16.66%">{{
          employeeInfo.employeeName || '暂无'
        }}</td>
        <td class="kq-border-header-center color-1" width="16.66%">所属车间</td>
        <td class="kq-border-header-center" width="16.66%">{{
          employeeInfo.department || '暂无'
        }}</td>
        <td class="kq-border-header-center color-1" width="16.66%">所属产线</td>
        <td class="kq-border-header-center" width="16.66%">{{
          employeeInfo.prodLine || '暂无'
        }}</td>
      </tr>
      <tr>
        <td class="kq-border-header-top color-1">所属班组</td>
        <td class="kq-border-header-center">{{ employeeInfo.team || '暂无' }}</td>
        <td class="kq-border-header-center color-1">工种</td>
        <td class="kq-border-header-center">{{ employeeInfo.jobType || '暂无' }}</td>
        <td class="kq-border-header-center color-1">所属班次</td>
        <td class="kq-border-header-center">{{ employeeInfo.workShift || '暂无' }}</td>
      </tr>
    </tbody>
  </table>

  <!-- 今日考勤统计 -->
  <table class="kq-table">
    <thead>
      <tr>
        <th colspan="6" class="kq-border-header color-1">今日考勤统计</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td class="kq-border-header-top color-1" width="16.66%">考勤时间</td>
        <td class="kq-border-header-center" width="16.66%">{{
          attendanceDetail.workTime || '暂无'
        }}</td>
        <td class="kq-border-header-center color-1" width="16.66%">上班打卡时间</td>
        <td
          class="kq-border-header-center"
          width="16.66%"
          :class="{ 'color-2': attendanceDetail.clockInTime }"
          >{{ attendanceDetail.clockInTime || '暂无' }}</td
        >
        <td class="kq-border-header-center color-1" width="16.66%">下班打卡时间</td>
        <td class="kq-border-header-center" width="16.66%">{{
          attendanceDetail.checkOutTime || '暂无'
        }}</td>
      </tr>
      <tr>
        <td class="kq-border-header-top color-1">应考勤时长(h)</td>
        <td class="kq-border-header-center">{{ attendanceDetail.scheduledHoursToday || 0 }}</td>
        <td class="kq-border-header-center color-1">实际考勤时长(h)</td>
        <td class="kq-border-header-center">{{ attendanceDetail.actualHoursToday || 0 }}</td>
        <td class="kq-border-header-center color-1">怠工时长(h)</td>
        <td class="kq-border-header-center">{{ attendanceDetail.idleHoursToday || 0 }}</td>
      </tr>
      <tr>
        <td class="kq-border-header-top color-1">今日工效</td>
        <td class="kq-border-header-center">{{
          formatPercent(attendanceDetail.efficiencyToday)
        }}</td>
        <td class="kq-border-header-center color-1">考勤状态</td>
        <td class="kq-border-header-center">{{ attendanceDetail.attendanceStatus || '暂无' }}</td>
        <td class="kq-border-header-center color-1"></td>
        <td class="kq-border-header-center"></td>
      </tr>
    </tbody>
  </table>

  <!-- 本月考勤统计 -->
  <table class="kq-table">
    <thead>
      <tr>
        <th colspan="6" class="kq-border-header color-1">本月考勤统计</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td class="kq-border-header-top color-1" width="16.66%">应出勤天数(天)</td>
        <td class="kq-border-header-center" width="16.66%">{{
          attendanceData.scheduledDays || 0
        }}</td>
        <td class="kq-border-header-center color-1" width="16.66%">实际出勤天数(天)</td>
        <td class="kq-border-header-center" width="16.66%">{{ attendanceData.actualDays || 0 }}</td>
        <td class="kq-border-header-center color-1" width="16.66%">出勤率</td>
        <td class="kq-border-header-center" width="16.66%">{{
          formatPercent(attendanceData.attendanceRate)
        }}</td>
      </tr>
      <tr>
        <td class="kq-border-header-top color-1">应考勤时间(h)</td>
        <td class="kq-border-header-center">{{ attendanceData.scheduledHours || 0 }}</td>
        <td class="kq-border-header-center color-1">实际工作时间(h)</td>
        <td class="kq-border-header-center">{{ attendanceData.actualHours || 0 }}</td>
        <td class="kq-border-header-center color-1">考勤达成率</td>
        <td class="kq-border-header-center">{{
          formatPercent(attendanceData.attendanceAchievementRate)
        }}</td>
      </tr>
      <tr>
        <td class="kq-border-header-top color-1">怠工时长(h)</td>
        <td class="kq-border-header-end">{{ attendanceData.idleHours || 0 }}</td>
        <td class="kq-border-header-end color-1">工效</td>
        <td class="kq-border-header-end">{{ formatPercent(attendanceData.efficiency) }}</td>
        <td class="kq-border-header-end color-1"></td>
        <td class="kq-border-header-end"></td>
      </tr>
    </tbody>
  </table>
</template>
<script lang="ts" setup>
  import { ref, watch, onMounted, defineProps } from 'vue';

  // 定义接口类型
  interface EmployeeInfo {
    employeeName?: string;
    department?: string;
    prodLine?: string;
    team?: string;
    jobType?: string;
    workShift?: string;
    [key: string]: any;
  }

  interface AttendanceDetail {
    workTime: string;
    checkOutTime: string;
    clockInTime: string;
    scheduledHoursToday: number;
    actualHoursToday: number;
    idleHoursToday: number;
    efficiencyToday: number;
    achievementRateToday: number;
    [key: string]: any;
  }

  interface AttendanceData {
    id?: string;
    scheduledDays?: number;
    actualDays?: number;
    attendanceRate?: number;
    scheduledHours?: number;
    actualHours?: number;
    attendanceAchievementRate?: number;
    idleHours?: number;
    efficiency?: number;
    todayDetail?: AttendanceDetail;
    [key: string]: any;
  }

  // 定义props接收记录数据
  const props = defineProps({
    recordData: {
      type: Object,
      default: () => ({}),
    },
  });

  // 定义数据
  const attendanceData = ref<AttendanceData>({});
  const employeeInfo = ref<EmployeeInfo>({});
  const attendanceDetail = ref<AttendanceDetail>({
    workTime: '7:30-17:30',
    checkOutTime: '暂无',
    clockInTime: '',
    scheduledHoursToday: 0,
    actualHoursToday: 0,
    idleHoursToday: 0,
    efficiencyToday: 0,
    achievementRateToday: 0,
  });

  // 获取考勤数据
  const fetchAttendanceData = async () => {
    try {
      // 如果recordData中已有需要的数据，直接使用
      if (props.recordData && props.recordData.person) {
        // 填充员工基本信息
        employeeInfo.value = {
          employeeName: props.recordData.person.name || '',
          department: props.recordData.person.department || '',
          prodLine: props.recordData.person.prodLine || '',
          team: props.recordData.person.team || '',
          jobType: props.recordData.person.jobType || '',
          workShift: props.recordData.workShift || '',
        };
      }
      // 填充日考勤信息
      if (props.recordData && props.recordData.dailyAttendance) {
        const daily = props.recordData.dailyAttendance;
        attendanceDetail.value = {
          workTime: daily.workTimeRange || '',
          clockInTime: daily.clockInTime || '',
          checkOutTime: daily.clockOutTime || '',
          scheduledHoursToday: daily.scheduledHours || 0,
          actualHoursToday: daily.actualHours || 0,
          idleHoursToday: daily.idleHours || 0,
          efficiencyToday: daily.dailyEfficiency || 0,
          achievementRateToday: daily.dailyAchievementRate || 0,
        };
        // 月考勤数据
        const attendanceSummary = props.recordData.attendanceSummary;
        attendanceData.value = {
          scheduledDays: attendanceSummary.scheduledDays || 0,
          actualDays: attendanceSummary.actualDays || 0,
          attendanceRate: attendanceSummary.attendanceRate || 0,
          scheduledHours: attendanceSummary.scheduledHours || '',
          actualHours: attendanceSummary.actualHours || 0,
          idleHours: attendanceSummary.idleHours || 0,
          efficiency: attendanceSummary.efficiency || 0,
          attendanceAchievementRate: attendanceSummary.attendanceAchievementRate || 0,
        };
      }
    } catch (error) {
      console.error('获取考勤详细数据失败:', error);
    }
  };

  // 格式化百分比
  const formatPercent = (value) => {
    if (value === undefined || value === null) return '0%';
    return (value * 100).toFixed(2) + '%';
  };

  // 监听props变化
  watch(
    () => props.recordData,
    (newVal) => {
      if (newVal && Object.keys(newVal).length > 0) {
        fetchAttendanceData();
      }
    },
    { immediate: true, deep: true },
  );

  // 初始加载
  onMounted(() => {
    if (props.recordData && Object.keys(props.recordData).length > 0) {
      fetchAttendanceData();
    }
  });
</script>
<style lang="less" scoped>
  .kq-table {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: 15px;

    th,
    td {
      text-align: center;
      border: 1px solid #999;
    }
  }

  .color-1 {
    background-color: #efefef;
  }

  .color-2 {
    background-color: red;
  }
</style>
