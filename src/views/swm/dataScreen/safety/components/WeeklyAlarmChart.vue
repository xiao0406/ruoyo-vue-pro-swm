<template>
  <div class="weekly-alarm-chart">
    <div class="chart-header">
      <div class="title">近七日报警统计</div>
      <div class="link">
        <a href="javascript:void(0)">查看记录</a>
      </div>
    </div>
    <div class="chart-container" ref="chartRef"></div>
  </div>
</template>

<script lang="ts" setup>
  import { ref, onMounted, onBeforeUnmount } from 'vue';
  import * as echarts from 'echarts/core';
  import { LineChart } from 'echarts/charts';
  import {
    TitleComponent,
    TooltipComponent,
    GridComponent,
    LegendComponent,
  } from 'echarts/components';
  import { CanvasRenderer } from 'echarts/renderers';
  import { warningStatisticsForPast7Days } from '@/api/swm/dataScreen';

  // 注册 ECharts 组件
  echarts.use([
    TitleComponent,
    TooltipComponent,
    GridComponent,
    LegendComponent,
    LineChart,
    CanvasRenderer,
  ]);

  const chartRef = ref<HTMLElement | null>(null);
  let chart: echarts.ECharts | null = null;

  // 初始化图表
  const initChart = async () => {
    if (!chartRef.value) return;

    // 调用接口获取数据
    const res = await warningStatisticsForPast7Days();
    const chartData = res.chartData;

    // 提取日期和系列数据
    const dates = chartData.dates;
    const series = Object.keys(chartData.series).map((key) => ({
      name: key,
      type: 'line',
      data: chartData.series[key],
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: {
        width: 2,
      },
    }));

    // 创建图表
    chart = echarts.init(chartRef.value);

    const option = {
      color: ['#5B8FF9', '#5AD8A6', '#F6BD16', '#E8684A'],
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross',
        },
      },
      legend: {
        data: series.map((s) => s.name),
        top: 0,
        right: 10,
        textStyle: {
          fontSize: 12,
        },
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        top: '30px',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: dates,
        axisLine: {
          lineStyle: {
            color: '#E9E9E9',
          },
        },
        axisLabel: {
          fontSize: 12,
        },
      },
      yAxis: {
        type: 'value',
        axisLine: {
          show: false,
        },
        axisLabel: {
          fontSize: 12,
        },
        splitLine: {
          lineStyle: {
            color: '#E9E9E9',
          },
        },
      },
      series: series,
    };

    chart.setOption(option);
  };

  // 窗口大小变化时重绘图表
  const handleResize = () => {
    chart?.resize();
  };

  onMounted(async () => {
    await initChart();
    window.addEventListener('resize', handleResize);
  });

  onBeforeUnmount(() => {
    if (chart) {
      chart.dispose();
      chart = null;
    }
    window.removeEventListener('resize', handleResize);
  });
</script>

<style lang="less" scoped>
  .weekly-alarm-chart {
    height: 100%;
    width: 100%;
    display: flex;
    flex-direction: column;

    .chart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0 16px;
      margin-bottom: 10px;

      .title {
        font-size: 14px;
        font-weight: 500;
        color: #17233d;
      }

      .link {
        a {
          font-size: 12px;
          color: #2d8cf0;
        }
      }
    }

    .chart-container {
      flex: 1;
      min-height: 220px;
    }
  }
</style>
