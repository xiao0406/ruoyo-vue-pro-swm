<template>
  <div class="liquid-fill-chart">
    <div class="chart-header">{{ title }}</div>
    <div class="chart-body">
      <div class="chart-container" ref="chartRef"></div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { defineProps, ref, onMounted, onBeforeUnmount } from 'vue';
  import * as echarts from 'echarts/core';
  import { CanvasRenderer } from 'echarts/renderers';
  import 'echarts-liquidfill';

  // 注册必要组件
  echarts.use([CanvasRenderer]);

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
      default: '#008be1',
    },
  });

  const chartRef = ref<HTMLElement | null>(null);
  let chartInstance: echarts.ECharts | null = null;

  const initChart = () => {
    if (!chartRef.value) return;

    chartInstance = echarts.init(chartRef.value);

    const value = (props.percentage / 100).toFixed(2);
    const outColor = props.color;

    const option = {
      backgroundColor: 'transparent',
      series: [
        {
          type: 'liquidFill',
          radius: '80%',
          color: [outColor, outColor, outColor],
          data: [value],
          center: ['50%', '50%'],
          backgroundStyle: {
            borderWidth: 0,
            borderColor: false,
            shadowColor: false,
            color: '#e3f7ff',
          },
          outline: {
            itemStyle: {
              borderColor: outColor,
              borderWidth: 10,
            },
            borderDistance: 6,
          },
          label: {
            formatter: function (val) {
              return Math.ceil(val.value * 100) + '%';
            },
            fontSize: 45,
            fontWeight: 400,
            fontFamily: '微软雅黑',
            color: outColor,
            insideColor: '#fff',
          },
        },
      ],
    };

    chartInstance.setOption(option);
  };

  const handleResize = () => {
    chartInstance?.resize();
  };

  onMounted(() => {
    initChart();
    window.addEventListener('resize', handleResize);
  });

  onBeforeUnmount(() => {
    if (chartInstance) {
      chartInstance.dispose();
      chartInstance = null;
    }
    window.removeEventListener('resize', handleResize);
  });
</script>

<style lang="less" scoped>
  .liquid-fill-chart {
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
      position: relative;

      .chart-container {
        width: 100%;
        height: 100%;
      }
    }
  }
</style>
