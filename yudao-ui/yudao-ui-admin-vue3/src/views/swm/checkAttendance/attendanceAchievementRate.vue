<template>
  <h1 style="text-align: center">本月考勤达成率</h1>
  <div :loading="loading">
    <div ref="chartRef" :style="{ width, height }"></div>
  </div>
</template>
<script lang="ts" setup>
  import { Ref, ref, watch, onMounted } from 'vue';
  import { useECharts } from '@/hooks/swm/useECharts';

  const props = defineProps({
    loading: Boolean,
    width: {
      type: String as PropType<string>,
      default: '100%',
    },
    height: {
      type: String as PropType<string>,
      default: '250px',
    },
    recordData: {
      type: Object,
      default: () => ({}),
    },
  });

  const chartRef = ref<HTMLDivElement | null>(null);
  const { setOptions } = useECharts(chartRef as Ref<HTMLDivElement>);
  const attendanceRate = ref(0); // 默认值80%

  // 获取考勤达成率数据
  const fetchAttendanceRateData = async () => {
    // 如果没有recordId，不发送请求
    if (!props.recordData) {
      updateChart(); // 使用默认值更新图表
      return;
    }

    try {
      if (props.recordData && props.recordData.attendanceSummary) {
        // 将小数转换为百分比 - 保留两位小数
        attendanceRate.value = parseFloat(
          (props.recordData.attendanceSummary.attendanceAchievementRate * 100).toFixed(2),
        );

        updateChart();
      } else {
        console.log('没有找到考勤达成率数据');
        // 使用默认值
        updateChart();
      }
    } catch (error) {
      console.error('获取考勤达成率数据失败:', error);
      // 出错时使用默认值
      updateChart();
    }
  };

  // 更新图表
  const updateChart = () => {
    setOptions({
      tooltip: {
        trigger: 'item',
      },
      series: [
        {
          name: '本月考勤达成率',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['50%', '50%'],
          clockwise: false,
          label: {
            show: true,
            position: 'center',
            formatter: () => {
              return attendanceRate.value + '%';
            },
          },
          color: ['#e8e8e8', '#1684fc'],
          data: [
            { value: 100 - attendanceRate.value, name: '本月考勤未达成率' },
            { value: attendanceRate.value, name: '本月考勤达成率' },
          ],

          animationType: 'scale',
          animationEasing: 'exponentialInOut',
          animationDelay: function () {
            return Math.random() * 400;
          },
        },
      ],
    });
  };

  // 监听props变化
  watch(
    () => [props.recordData],
    ([newRecordId]) => {
      if (newRecordId) {
        fetchAttendanceRateData();
      }
    },
    { immediate: false }, // 设为false避免组件初始化时自动触发
  );

  // 初始加载
  onMounted(() => {
    // 只有当recordId存在时才获取数据
    if (props.recordData) {
      fetchAttendanceRateData();
    } else {
      console.log('组件挂载时recordId为空，使用默认值');
      updateChart(); // 使用默认值更新图表
    }
  });

  // 监听loading状态变化
  watch(
    () => props.loading,
    () => {
      if (props.loading) {
        return;
      }
      updateChart();
    },
  );
</script>
