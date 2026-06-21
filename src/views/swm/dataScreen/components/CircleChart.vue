<template>
  <div class="circle-chart">
    <div class="chart-header">{{ title }}</div>
    <div class="chart-body">
      <div class="chart-container" ref="chartRef"></div>
      <div class="percentage-display">{{ percentage }}%</div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { defineProps, ref, onMounted, watch } from 'vue';
  import * as echarts from 'echarts/core';
  import { PieChart } from 'echarts/charts';
  import { TitleComponent, TooltipComponent } from 'echarts/components';
  import { CanvasRenderer } from 'echarts/renderers';

  // 注册必要的组件
  echarts.use([TitleComponent, TooltipComponent, PieChart, CanvasRenderer]);

  const props = defineProps({
    title: {
      type: String,
      required: true,
    },
    percentage: {
      type: Number,
      required: true,
      validator: (value: number) => value >= 0 && value <= 100,
    },
    color: {
      type: String,
      default: '#2d8cf0',
    },
  });

  const chartRef = ref<HTMLElement | null>(null);
  let chart: echarts.ECharts | null = null;

  const initChart = () => {
    if (!chartRef.value) return;

    chart = echarts.init(chartRef.value);
    updateChart();

    window.addEventListener('resize', () => {
      chart?.resize();
    });
  };

  const updateChart = () => {
    if (!chart) return;

    const option = {
      series: [
        {
          type: 'pie',
          radius: ['70%', '90%'],
          avoidLabelOverlap: false,
          label: {
            show: false,
          },
          labelLine: {
            show: false,
          },
          data: [
            {
              value: props.percentage,
              name: '完成率',
              itemStyle: {
                color: props.color,
              },
            },
            {
              value: 100 - props.percentage,
              name: '未完成',
              itemStyle: {
                color: '#f0f2f5',
              },
            },
          ],
          animation: true,
        },
      ],
    };

    chart.setOption(option);
  };

  // 监听数据变化更新图表
  watch(
    () => props.percentage,
    (newVal) => {
      updateChart();
    },
  );

  onMounted(() => {
    initChart();
  });
</script>

<style lang="less" scoped>
  .circle-chart {
    height: 100%;

    .chart-header {
      font-size: 16px;
      font-weight: 500;
      padding: 0 0 12px 0;
      border-bottom: 1px solid #e8eaec;
      color: #17233d;
    }

    .chart-body {
      height: calc(100% - 40px);
      display: flex;
      justify-content: center;
      align-items: center;
      position: relative;

      .chart-container {
        width: 100%;
        height: 100%;
      }

      .percentage-display {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        font-size: 24px;
        color: #17233d;
        font-weight: bold;
      }
    }
  }
</style>
