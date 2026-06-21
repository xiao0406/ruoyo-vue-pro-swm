<template>
  <div ref="chartRef" style="width: 100%; height: 300px"></div>
</template>
<script lang="ts" setup>
  import { onMounted, ref, Ref, watch, defineProps } from 'vue';
  import { useECharts } from '@/hooks/swm/useECharts';

  // 定义props接收记录数据
  const props = defineProps({
    recordData: {
      type: Object,
      default: () => ({}),
    },
  });

  const chartRef = ref<HTMLDivElement | null>(null);
  const { setOptions } = useECharts(chartRef as Ref<HTMLDivElement>);

  // 定义图表数据结构
  interface ChartData {
    xAxis: string[];
    scheduledHours: number[];
    actualHours: number[];
  }

  // 初始化图表数据
  const chartData = ref<ChartData>({
    xAxis: [],
    scheduledHours: [],
    actualHours: [],
  });

  // 获取图表数据
  const fetchChartData = async () => {
    // 如果没有recordId，不发送请求
    if (!props.recordData) {
      initEmptyChart();
      return;
    }
    try {
      // 处理响应数据
      if (props.recordData && props.recordData.attendanceChartData) {
        chartData.value = props.recordData.attendanceChartData;
        updateChart();
      } else {
        console.log('获取考勤时间图表数据失败或数据为空');
        initEmptyChart();
      }
    } catch (error) {
      console.error('获取考勤时间图表数据失败:', error);
      initEmptyChart();
    }
  };

  // 初始化空图表
  const initEmptyChart = () => {
    // 创建默认的31天数据
    const days = Array.from({ length: 31 }, (_, i) => (i + 1).toString());
    const emptyData = Array(31).fill(0);

    chartData.value = {
      xAxis: days,
      scheduledHours: emptyData,
      actualHours: emptyData,
    };

    updateChart();
  };

  // 更新图表
  const updateChart = () => {
    setOptions({
      legend: {
        data: ['应考勤时长', '实际考勤时长'],
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        data: chartData.value.xAxis || [],
      },
      yAxis: {
        type: 'value',
        name: '时长（小时）',
        axisLabel: {
          formatter: '{value}h',
        },
      },
      series: [
        {
          name: '应考勤时长',
          data: chartData.value.scheduledHours || [],
          type: 'line',
          smooth: true,
          itemStyle: {
            color: 'red',
          },
        },
        {
          name: '实际考勤时长',
          data: chartData.value.actualHours || [],
          type: 'line',
          smooth: true,
          itemStyle: {
            color: '#4fe325',
          },
        },
      ],
      tooltip: {
        trigger: 'axis',
        formatter: function (params) {
          let result = params[0].name + '日<br>';

          params.forEach((param) => {
            result += param.marker + ' ' + param.seriesName + ': ' + param.value + 'h<br>';
          });

          return result;
        },
      },
    });
  };

  // 监听props变化
  watch(
    () => [props.recordData],
    () => {
      fetchChartData();
    },
    { immediate: false },
  );

  // 初始加载
  onMounted(() => {
    if (props.recordData) {
      fetchChartData();
    } else {
      initEmptyChart();
    }
  });
</script>
