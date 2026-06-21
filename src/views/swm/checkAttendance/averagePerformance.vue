<template>
  <h1 style="text-align: center">本月个人工效</h1>
  <div>
    <div ref="chart" style="width: 100%; height: 250px"></div>
  </div>
</template>
<script lang="ts" setup>
  import { ref, onMounted, onBeforeUnmount, watch, defineProps } from 'vue';
  import * as echarts from 'echarts/core';
  import { CanvasRenderer } from 'echarts/renderers';
  import 'echarts-liquidfill';
  import { useUserStore } from '@/store/modules/user';

  // 定义props接收记录ID和月份
  const props = defineProps({
    recordData: {
      type: Object,
      default: () => ({}),
    },
  });

  // 注册必要组件
  echarts.use([CanvasRenderer]);
  const chart = ref(null);
  let chartInstance = null as any;
  let personalEfficiency = 0.9; // 默认值
  // 获取考勤记录数据
  const fetchAttendanceData = async () => {
    // 如果没有recordId，不发送请求
    if (!props.recordData) {
      return;
    }

    try {
      // 使用传入的月份，如果没有则使用当前月份

      // 直接使用返回的数据
      if (props.recordData && props.recordData.attendanceSummary) {
        personalEfficiency = Number(props.recordData.attendanceSummary.efficiency);
        // 限制在0-1之间
        personalEfficiency = Math.max(0, Math.min(1, personalEfficiency));
        // 更新图表
        updateChart();
      } else {
        // 使用默认值
        personalEfficiency = 0.9;
        updateChart();
      }
    } catch (error) {
      console.error('获取考勤记录数据失败:', error);
      // 出错时使用默认值
      personalEfficiency = 0.9;
      updateChart();
    }
  };
  // 监听props变化，重新获取数据，但不立即触发
  watch(
    () => [props.recordData],
    ([newRecordId]) => {
      if (newRecordId) {
        fetchAttendanceData();
      } else {
        // 如果没有ID，初始化图表但不请求数据
        initChart();
      }
    },
    { immediate: false }, // 设为false避免组件初始化时自动触发
  );

  const updateChart = () => {
    if (!chartInstance) return;

    let value = personalEfficiency.toFixed(2);
    let data = [value];

    chartInstance.setOption({
      series: [
        {
          data: data,
          label: {
            formatter: function (val) {
              return Math.ceil(val.value * 100) + '%';
            },
          },
        },
      ],
    });
  };

  const initChart = () => {
    chartInstance = echarts.init(chart.value);
    let value = personalEfficiency.toFixed(2);
    let color = ['#008be1', '#008be1', '#008be1'];
    let outcolor = '#008be1';
    let data = [] as any;
    data.push(value);
    let option = {
      graphic: {
        elements: [
          {
            type: 'image',
            style: {
              width: 40,
              height: 40,
            },
            left: 'center',
            top: '110px',
          },
        ],
      },
      backgroundColor: 'transparent',
      series: [
        {
          type: 'liquidFill',
          radius: '80%',
          color: color,
          data: data,
          center: ['50%', '50%'],
          backgroundStyle: {
            borderWidth: 0,
            borderColor: false,
            shadowColor: false,
            color: '#e3f7ff',
          },
          outline: {
            itemStyle: {
              borderColor: outcolor,
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
            color: outcolor,
            insideColor: '#fff',
          },
        },
      ],
    };
    chartInstance.setOption(option);
  };

  onMounted(() => {
    // 初始化图表
    initChart();

    // 只有当recordId存在时才获取数据
    if (props.recordData) {
      fetchAttendanceData();
    } else {
      console.log('组件挂载时recordId为空，跳过数据请求');
    }

    window.addEventListener('resize', () => chartInstance?.resize());
  });

  onBeforeUnmount(() => {
    chartInstance?.dispose();
  });
</script>
