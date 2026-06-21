<template>
  <div class="danger-source-container">
    <!-- 顶部标签和选择器 -->
    <div class="filter-container">
      <div class="year-week-select">
        <div class="select-item">
          <span class="label">年月</span>
          <a-month-picker v-model:value="YearAndMonth" valueFormat="YYYY-MM" />
        </div>
        <a-button type="primary" @click="handleSearch">搜索</a-button>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="content-container">
      <!-- 顶部统计卡片 -->
      <div class="statistics-cards">
        <div class="statistic-card">
          <div class="statistic-value">已发现: {{ hazardData.statusCounts.WAIT }}</div>
        </div>
        <div class="statistic-card">
          <div class="statistic-value">整改中: {{ hazardData.statusCounts.IN_PROGRESS }}</div>
        </div>
        <div class="statistic-card">
          <div class="statistic-value">已整改: {{ hazardData.statusCounts.COMPLETED }}</div>
        </div>
      </div>

      <!-- 包含所有表格和图表的区域 -->
      <div class="all-tables-container">
        <!-- 第一部分：风险top10表格和饼图 -->
        <div class="top-section">
          <div class="left-content">
            <div class="section-title">危险区域top10</div>
            <a-table
              :columns="topColumns"
              :data-source="hazardData.top10Categories"
              :pagination="false"
              size="small"
              :scroll="{ y: 240 }"
              bordered
            />
          </div>
          <div class="right-content">
            <div class="section-title">危险区域统计</div>
            <div class="pie-chart" ref="pieChartRef"></div>
          </div>
        </div>
      </div>

      <!-- 第二部分：未制定巡检计划的风险 - 独立区域 -->
      <div class="inspection-section">
        <div class="section-title">未制定巡检计划的危险区域</div>
        <!-- 底部表格 -->
        <a-table
          :columns="inspectionColumns"
          :data-source="inspectionData"
          :pagination="false"
          size="small"
          bordered
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted, watch } from 'vue';
  import { Table, MonthPicker } from 'ant-design-vue';
  import { hazard } from '@/api/swm/dataScreen';
  import * as echarts from 'echarts';
  const AMonthPicker = MonthPicker;

  const selectedYear = ref<string>('2025');
  const selectedMonth = ref<string>('05');

  // 选择器数据
  const YearAndMonth = ref();

  // 数据模型
  const hazardData = ref({
    statusCounts: {
      IN_PROGRESS: 0,
      COMPLETED: 0,
      WAIT: 5,
    },
    top10Categories: [
      {
        count: 0,
        category: '',
      },
    ],
    categoryCounts: [
      {
        count: 0,
        category: '',
      },
    ],
  });

  // 监听 YearAndMonth 的变化并拆分年月
  watch(
    () => YearAndMonth.value,
    (newVal) => {
      if (newVal) {
        const [year, month] = newVal.split('-');
        selectedYear.value = year;
        selectedMonth.value = month;
      } else {
        selectedYear.value = '';
        selectedMonth.value = '';
      }
    },
  );

  // 调用风险统计接口
  const hazardDangerData = async () => {
    const res = await hazard({
      year: selectedYear.value,
      month: selectedMonth.value,
    });
    hazardData.value = res;
  };

  // 搜索事件
  const handleSearch = () => {
    hazardDangerData().then(() => {
      if (myChart) {
        myChart.setOption(getChartOption(), true);
      }
    });
  };

  // 风险top10表格配置
  const topColumns = [
    {
      title: '危险区域名称',
      dataIndex: 'category',
      key: 'category',
      width: '50%',
      customRender: ({ text }) => {
        return `危险区域${text}`;
      },
    },
    {
      title: '危险区域数量',
      dataIndex: 'count',
      key: 'count',
      width: '50%',
    },
  ];

  // 未制定巡检计划表格配置
  const inspectionColumns = [
    { title: '危险区域名称', dataIndex: 'name', key: 'name', width: '15%' },
    { title: '危险区域类型', dataIndex: 'type', key: 'type', width: '15%' },
    { title: '位置', dataIndex: 'location', key: 'location', width: '30%' },
    { title: '所属部门', dataIndex: 'department', key: 'department', width: '25%' },
    { title: '是否加入建链', dataIndex: 'isJoined', key: 'isJoined', width: '15%' },
  ];

  // 未制定巡检计划表格数据
  const inspectionData = [
    {
      key: '1',
      name: '油罐车',
      type: '易燃易爆',
      location: '一车间/油漆车间/油漆库',
      department: 'GWX123',
      isJoined: '是',
    },
    {
      key: '2',
      name: '粉尘',
      type: '作业车间',
      location: '一车间/油漆车间/吊装',
      department: '否',
      isJoined: '否',
    },
  ];

  // 饼图引用
  const pieChartRef = ref(null);

  let myChart = null;

  // 初始化饼图
  onMounted(() => {
    hazardDangerData().then(() => {
      const chartDom = pieChartRef.value;
      myChart = echarts.init(chartDom);
      myChart.setOption(getChartOption());

      window.addEventListener('resize', () => {
        myChart.resize();
      });
    });
  });

  // 构建 ECharts 图表配置
  function getChartOption() {
    return {
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} (个) ({d}%)',
      },
      legend: {
        orient: 'horizontal',
        bottom: 0,
        itemWidth: 15,
        itemHeight: 10,
        itemGap: 6,
        textStyle: {
          fontSize: 10,
        },
        data: hazardData.value.categoryCounts.map((item) => `危险区域${item.category}`),
      },
      series: [
        {
          name: '危险区域数量',
          type: 'pie',
          radius: '75%',
          center: ['50%', '45%'],
          data: hazardData.value.categoryCounts.map((item) => ({
            value: item.count,
            name: `危险区域${item.category}`,
          })),
          label: {
            show: true,
            formatter: '{b}\n{c} (个)',
          },
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowColor: 'rgba(0, 0, 0, 0.5)',
            },
          },
        },
      ],
      color: [
        '#5470c6',
        '#91cc75',
        '#fac858',
        '#ee6666',
        '#73c0de',
        '#3ba272',
        '#fc8452',
        '#9a60b4',
        '#ea7ccc',
        '#b6a2de',
      ],
    };
  }
</script>

<style lang="less" scoped>
  .danger-source-container {
    width: 100%;
    height: 100%;
    padding: 16px;
    background-color: #fff;
    overflow: hidden;
    display: flex;
    flex-direction: column;

    .filter-container {
      flex-shrink: 0;
      margin-bottom: 16px;

      .year-week-select {
        display: flex;
        gap: 20px;

        .select-item {
          display: flex;
          align-items: center;

          .label {
            margin-right: 10px;
            font-size: 14px;
            color: #1e63c0;
          }
        }
      }
    }

    .content-container {
      flex: 1;
      display: flex;
      flex-direction: column;
      min-height: 0;

      .statistics-cards {
        flex-shrink: 0;
        display: flex;
        gap: 16px;
        margin-bottom: 16px;

        .statistic-card {
          flex: 1;
          border: 1px solid #eee;
          padding: 10px 16px;
          text-align: center;
          background-color: #fff;

          .statistic-value {
            font-size: 16px;
            font-weight: normal;
          }
        }
      }

      .all-tables-container {
        flex-shrink: 0;
        display: flex;
        flex-direction: column;
      }

      .top-section {
        flex-shrink: 0;
        display: flex;
        gap: 16px;
        height: 330px;
        margin-bottom: 30px;

        .left-content {
          width: 38%;
          display: flex;
          flex-direction: column;
        }

        .right-content {
          flex: 1;
          display: flex;
          flex-direction: column;
        }

        .section-title {
          font-size: 16px;
          font-weight: 500;
          color: #1e63c0;
          margin-bottom: 12px;
          position: relative;
          padding-left: 12px;

          &::before {
            content: '';
            position: absolute;
            left: 0;
            top: 50%;
            transform: translateY(-50%);
            width: 4px;
            height: 16px;
            background-color: #1e63c0;
            border-radius: 2px;
          }
        }

        .pie-chart {
          flex: 1;
          height: 350px;
        }
      }

      .inspection-section {
        flex-shrink: 0;
        margin-top: 6vh;

        .section-title {
          font-size: 16px;
          font-weight: 500;
          color: #1e63c0;
          margin-bottom: 12px;
          position: relative;
          padding-left: 12px;

          &::before {
            content: '';
            position: absolute;
            left: 0;
            top: 50%;
            transform: translateY(-50%);
            width: 4px;
            height: 16px;
            background-color: #1e63c0;
            border-radius: 2px;
          }
        }

        :deep(.ant-table-wrapper) {
          .ant-table {
            border-radius: 0;
          }
        }
      }
    }

    // 覆盖 antd 样式
    :deep(.ant-table-thead > tr > th) {
      background-color: #f5f5f5;
      font-weight: normal;
      padding: 8px 16px;
      border-color: #e8e8e8;
    }

    :deep(.ant-table-tbody > tr > td) {
      padding: 8px 16px;
      border-color: #e8e8e8;
    }

    :deep(.ant-select-selector) {
      border-radius: 2px !important;
    }

    :deep(.ant-table) {
      border-radius: 0;
    }

    /* 隐藏滚动条（除表格） */
    :deep(::-webkit-scrollbar) {
      width: 0 !important;
      display: none !important;
    }

    :deep(.ant-table-body::-webkit-scrollbar) {
      width: 6px;
      display: block !important;
      background-color: #f5f5f5;
    }

    :deep(.ant-table-body::-webkit-scrollbar-thumb) {
      background-color: #ccc;
      border-radius: 3px;
    }
  }
</style>
