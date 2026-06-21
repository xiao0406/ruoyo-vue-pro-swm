import { ref, onMounted, onUnmounted, watch, type Ref, shallowRef } from 'vue';
import * as echarts from 'echarts';

export function useECharts(chartRef: Ref<HTMLElement | null>, theme: string = 'light') {
  const chartInstance = shallowRef<echarts.ECharts | null>(null);

  function initChart() {
    if (chartRef.value) {
      chartInstance.value = echarts.init(chartRef.value, theme);
    }
  }

  function setOption(option: any, notMerge?: boolean) {
    if (chartInstance.value) {
      chartInstance.value.setOption(option, notMerge);
    }
  }

  function resize() {
    chartInstance.value?.resize();
  }

  function getInstance() {
    return chartInstance.value;
  }

  function dispose() {
    chartInstance.value?.dispose();
    chartInstance.value = null;
  }

  onMounted(() => {
    initChart();
    window.addEventListener('resize', resize);
  });

  onUnmounted(() => {
    window.removeEventListener('resize', resize);
    dispose();
  });

  return { setOption, resize, getInstance, dispose, chartInstance };
}
