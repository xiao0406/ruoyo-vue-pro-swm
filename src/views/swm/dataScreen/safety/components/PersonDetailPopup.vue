<template>
  <div class="person-detail-popup" v-if="visible">
    <div class="popup-content">
      <div class="close-btn" @click="handleClose">✕</div>

      <div class="avatar">
        <svg viewBox="0 0 24 24" fill="#1890ff" width="100%" height="100%">
          <path
            d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"
          ></path>
        </svg>
        <div class="avatar-bar"></div>
      </div>

      <div class="info-grid">
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">姓名：</div>
            <div class="info-value">{{ personData.name || '张三' }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">工种：</div>
            <div class="info-value">{{ personData.jobType || '钢焊工' }}</div>
          </div>
        </div>

        <div class="info-row">
          <div class="info-item">
            <div class="info-label">组织：</div>
            <div class="info-value">{{ personData.organization || '天津厂' }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">车间：</div>
            <div class="info-value">{{ personData.workshop || '一车间' }}</div>
          </div>
        </div>

        <div class="info-row">
          <div class="info-item">
            <div class="info-label">工序：</div>
            <div class="info-value">{{ personData.process || '焊接工序' }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">班组：</div>
            <div class="info-value">{{ personData.team || '钢焊一班' }}</div>
          </div>
        </div>

        <div class="info-row">
          <div class="info-item">
            <div class="info-label">今日工作时长：</div>
            <div class="info-value">{{ personData.workHours || '8小时' }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">考勤状态：</div>
            <div class="info-value">{{ personData.attendanceStatus || '正常考勤' }}</div>
          </div>
        </div>
      </div>

      <div class="action-buttons">
        <button class="action-btn attendance-btn" @click="viewAttendance">考勤记录</button>
        <button class="action-btn track-btn" @click="viewTrack">行动轨迹</button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { defineProps, defineEmits } from 'vue';

  const props = defineProps({
    visible: {
      type: Boolean,
      default: false,
    },
    personData: {
      type: Object,
      default: () => ({}),
    },
  });

  const emits = defineEmits(['close', 'view-attendance', 'view-track']);

  const handleClose = () => {
    emits('close');
  };

  const viewAttendance = () => {
    emits('view-attendance', props.personData);
  };

  const viewTrack = () => {
    emits('view-track', props.personData);
  };
</script>

<style lang="less" scoped>
  .person-detail-popup {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.1);
    z-index: 1000;
    display: flex;
    align-items: center;
    justify-content: center;

    .popup-content {
      position: relative;
      width: 400px;
      background-color: rgba(255, 214, 102, 0.03);
      backdrop-filter: none;
      border-radius: 4px;
      padding: 20px;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);

      .close-btn {
        position: absolute;
        top: 10px;
        right: 10px;
        width: 24px;
        height: 24px;
        line-height: 24px;
        text-align: center;
        cursor: pointer;
        color: #999;
        font-size: 16px;

        &:hover {
          color: #333;
        }
      }

      .avatar {
        position: absolute;
        right: 20px;
        top: 20px;
        width: 80px;
        height: 80px;

        .avatar-bar {
          height: 4px;
          background-color: #1890ff;
          position: absolute;
          bottom: 0;
          left: 20%;
          right: 20%;
        }
      }

      .info-grid {
        margin-top: 10px;

        .info-row {
          display: flex;
          margin-bottom: 12px;

          .info-item {
            flex: 1;
            display: flex;

            .info-label {
              color: #333;
              font-weight: normal;
              margin-right: 5px;
            }

            .info-value {
              color: #000;
              font-weight: normal;
            }
          }
        }
      }

      .action-buttons {
        display: flex;
        justify-content: center;
        gap: 20px;
        margin-top: 20px;

        .action-btn {
          padding: 8px 16px;
          border-radius: 4px;
          cursor: pointer;
          border: none;
          color: white;
          font-size: 14px;

          &.attendance-btn {
            background-color: #1890ff;

            &:hover {
              background-color: #40a9ff;
            }
          }

          &.track-btn {
            background-color: #1890ff;

            &:hover {
              background-color: #40a9ff;
            }
          }
        }
      }
    }
  }
</style>
