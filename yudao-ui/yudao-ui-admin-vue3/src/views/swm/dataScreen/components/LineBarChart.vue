<template>
  <div class="line-bar-chart">
    <div class="chart-header">{{ title }}</div>
    <div class="chart-body">
      <div class="chart-container" ref="chartRef"></div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { defineProps, ref, onMounted, onUnmounted, watch, nextTick } from 'vue';
  import * as echarts from 'echarts/core';
  import { BarChart, LineChart } from 'echarts/charts';
  import {
    TitleComponent,
    TooltipComponent,
    GridComponent,
    LegendComponent,
  } from 'echarts/components';
  import { CanvasRenderer } from 'echarts/renderers';

  // 注册必要的组件
  echarts.use([
    TitleComponent,
    TooltipComponent,
    GridComponent,
    LegendComponent,
    BarChart,
    LineChart,
    CanvasRenderer,
  ]);

  const props = defineProps({
    title: {
      type: String,
      required: true,
    },
    type: {
      type: String,
      required: true,
      validator: (value: string) => ['attendance', 'efficiency'].includes(value),
    },
    data: {
      type: Object,
      required: true,
    },
  });

  const chartRef = ref<HTMLElement | null>(null);
  let chart: echarts.ECharts | null = null;

  const initChart = async () => {
    if (!chartRef.value) return;

    // 等待下一个渲染周期，确保DOM已更新
    await nextTick();

    // 如果已存在chart实例，先销毁
    if (chart) {
      chart.dispose();
    }

    // 创建新实例
    chart = echarts.init(chartRef.value);
    updateChart();
  };

  const updateChart = () => {
    if (!chart) return;

    let option;

    if (props.type === 'attendance') {
      // 出勤统计图表配置
      option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            label: {
              backgroundColor: '#6a7985',
            },
          },
        },
        legend: {
          data: ['应出勤人数', '实际出勤人数', '出勤率'],
          right: 10,
          top: 0,
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true,
        },
        xAxis: [
          {
            type: 'category',
            boundaryGap: true,
            data: props.data.days?.map((day) => day.toString()) || [],
          },
        ],
        yAxis: [
          {
            type: 'value',
            name: '人数',
            position: 'left',
            axisLine: {
              show: true,
              lineStyle: {
                color: '#5470C6',
              },
            },
          },
          {
            type: 'value',
            name: '出勤率(%)',
            position: 'right',
            axisLine: {
              show: true,
              lineStyle: {
                color: '#91CC75',
              },
            },
            axisLabel: {
              formatter: '{value}%',
            },
            max: 100,
          },
        ],
        series: [
          {
            name: '应出勤人数',
            type: 'line',
            data: props.data.shouldAttend || [],
            smooth: true,
            itemStyle: {
              color: '#5470C6',
            },
          },
          {
            name: '实际出勤人数',
            type: 'line',
            data: props.data.actualAttend || [],
            smooth: true,
            itemStyle: {
              color: '#91CC75',
            },
          },
          {
            name: '出勤率',
            type: 'bar',
            yAxisIndex: 1,
            data: props.data.attendRate || [],
            barWidth: '40%',
            itemStyle: {
              color: '#EE6666',
            },
          },
        ],
      };
    } else {
      // 工效统计图表配置
      option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            label: {
              backgroundColor: '#6a7985',
            },
          },
        },
        legend: {
          data: ['应考勤时长', '实际考勤时长', '考勤率'],
          right: 10,
          top: 0,
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true,
        },
        xAxis: [
          {
            type: 'category',
            boundaryGap: true,
            data: props.data.days?.map((day) => day.toString()) || [],
          },
        ],
        yAxis: [
          {
            type: 'value',
            name: '考勤时长',
            position: 'left',
            axisLine: {
              show: true,
              lineStyle: {
                color: '#5470C6',
              },
            },
          },
          {
            type: 'value',
            name: '考勤率',
            position: 'right',
            axisLine: {
              show: true,
              lineStyle: {
                color: '#91CC75',
              },
            },
            axisLabel: {
              formatter: '{value}%',
            },
            max: 100,
          },
        ],
        series: [
          {
            name: '应考勤时长',
            type: 'line',
            data: props.data.shouldEfficiency || [],
            smooth: true,
            itemStyle: {
              color: '#5470C6',
            },
          },
          {
            name: '实际考勤时长',
            type: 'line',
            data: props.data.actualEfficiency || [],
            smooth: true,
            itemStyle: {
              color: '#91CC75',
            },
          },
          {
            name: '考勤率',
            type: 'line',
            yAxisIndex: 1,
            data: props.data.efficiency || [],
            smooth: true,
            lineStyle: {
              width: 2,
              color: '#73C0DE',
            },
            itemStyle: {
              color: '#73C0DE',
            },
          },
        ],
      };
    }

    chart.setOption(option);
  };

  // 监听数据变化更新图表
  watch(
    () => props.data,
    () => {
      nextTick(() => {
        updateChart();
      });
    },
    { deep: true },
  );

  // 窗口大小变化时重新调整图表大小
  const handleResize = () => {
    if (chart) {
      chart.resize();
    }
  };

  onMounted(() => {
    initChart();
    window.addEventListener('resize', handleResize);
  });

  onUnmounted(() => {
    if (chart) {
      chart.dispose();
      chart = null;
    }
    window.removeEventListener('resize', handleResize);
  });
</script>

<style lang="less" scoped>
  .line-bar-chart {
    height: 100%;
    display: flex;
    flex-direction: column;

    .chart-header {
      font-size: 16px;
      font-weight: 500;
      padding: 0 0 12px 0;
      border-bottom: 1px solid #e8eaec;
      color: #17233d;
    }

    .chart-body {
      flex: 1;
      margin-top: 12px;
      min-height: 300px;

      .chart-container {
        width: 100%;
        height: 100%;
      }
    }
  }
</style>
