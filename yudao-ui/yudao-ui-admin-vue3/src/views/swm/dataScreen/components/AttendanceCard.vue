<template>
  <div class="attendance-card">
    <div class="card-header">{{ title }}</div>
    <div class="card-body">
      <div class="stat-row">
        <div class="stat-label">应出人数</div>
        <div class="stat-value">{{ data.totalCount }}</div>
      </div>
      <div class="stat-row">
        <div class="stat-label">实出人数</div>
        <div class="stat-value">{{ data.presentCount }}</div>
      </div>
      <div class="stat-row">
        <div class="stat-label">出勤率</div>
        <div class="stat-value">{{ data.attendanceRate }}</div>
      </div>
      <div class="stat-row">
        <div class="stat-label">在场人数</div>
        <div class="stat-value">{{ data.onSiteCount }}</div>
      </div>
      <div class="stat-row">
        <div class="stat-label">工作中人数</div>
        <div class="stat-value">{{ data.workingCount }}</div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { defineProps } from 'vue';

  interface AttendanceData {
    totalCount: number;
    onSiteCount: number;
    attendanceRate: string;
    presentCount: number;
    workingCount: number;
  }

  defineProps({
    title: {
      type: String,
      required: true,
    },
    data: {
      type: Object as () => AttendanceData,
      required: true,
    },
  });
</script>

<style lang="less" scoped>
  .attendance-card {
    height: 100%;
    background: #fff;
    border-radius: 4px;

    .card-header {
      font-size: 16px;
      font-weight: 500;
      padding: 0 0 12px 0;
      border-bottom: 1px solid #e8eaec;
      color: #17233d;
    }

    .card-body {
      margin-top: 12px;
    }

    .stat-row {
      display: flex;
      justify-content: space-between;
      margin-bottom: 10px;

      &:last-child {
        margin-bottom: 0;
      }

      .stat-label {
        color: #808695;
        font-size: 14px;
      }

      .stat-value {
        color: #17233d;
        font-weight: 500;
      }
    }
  }
</style>
