<template>
  <div class="ranking-table">
    <div class="table-header">{{ title }}</div>
    <div class="table-body">
      <a-table
        :columns="columns"
        :data-source="data"
        :pagination="false"
        :row-key="(record) => record.rank"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <!-- 序号列带样式 -->
          <template v-if="column.dataIndex === 'rank'">
            <span :class="['rank-number', getRankClass(record.rank)]">
              {{ record.rank }}
            </span>
          </template>

          <!-- 出勤率列带百分比 -->
          <template v-else-if="column.dataIndex === 'attendRate'">
            <span>{{ record.attendRate }}</span>
          </template>

          <!-- 工效列带百分比 -->
          <template v-else-if="column.dataIndex === 'efficiency'">
            <span>{{ record.efficiency }}</span>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { defineProps, computed } from 'vue';
  import { Table } from 'ant-design-vue';

  // Table组件
  const ATable = Table;

  // Props定义
  const props = defineProps({
    title: {
      type: String,
      required: true,
    },
    data: {
      type: Array,
      required: true,
    },
    type: {
      type: String,
      required: true,
      validator: (value: string) => ['today', 'month', 'workType'].includes(value),
    },
  });

  // 根据类型生成不同的列配置
  const columns = computed(() => {
    const baseColumns = [
      {
        title: '序号',
        dataIndex: 'rank',
        width: 60,
        align: 'center' as const,
      },
      {
        title: props.type === 'workType' ? '工种名称' : '班组名称',
        dataIndex: 'name',
        width: 80, // 设置固定宽度
        align: 'left' as const, // 左对齐
        ellipsis: true,
      },
    ];

    baseColumns.push({
      title: '出勤人数',
      dataIndex: 'attendees',
      width: 80,
      align: 'center' as const,
    });

    return [
      ...baseColumns,
      {
        title: '出勤率',
        dataIndex: 'attendRate',
        width: 80,
        align: 'center' as const,
      },
      {
        title: '工效',
        dataIndex: 'efficiency',
        width: 60,
        align: 'center' as const,
      },
    ];
  });

  // 获取排名样式类
  const getRankClass = (rank: number) => {
    if (rank === 1) return 'rank-first';
    if (rank === 2) return 'rank-second';
    if (rank === 3) return 'rank-third';
    return '';
  };
</script>

<style lang="less" scoped>
  .ranking-table {
    height: 100%;

    .table-header {
      font-size: 16px;
      font-weight: 500;
      padding: 0 0 12px 0;
      border-bottom: 1px solid #e8eaec;
      color: #17233d;
    }

    .table-body {
      margin-top: 12px;
      height: calc(100% - 45px);
      overflow: auto;

      :deep(.ant-table-thead > tr > th) {
        background-color: #f8f8f9;
      }

      :deep(.ant-table-small) {
        font-size: 14px;
      }

      :deep(.ant-table-tbody > tr > td) {
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      :deep(.ant-table-cell) {
        padding: 8px 12px;
      }
    }

    .rank-number {
      display: inline-block;
      width: 24px;
      height: 24px;
      line-height: 24px;
      text-align: center;
      border-radius: 50%;
      font-weight: bold;

      &.rank-first {
        background-color: #f5222d;
        color: white;
      }

      &.rank-second {
        background-color: #fa8c16;
        color: white;
      }

      &.rank-third {
        background-color: #faad14;
        color: white;
      }
    }
  }
</style>
