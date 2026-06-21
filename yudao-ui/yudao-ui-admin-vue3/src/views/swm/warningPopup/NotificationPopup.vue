<template>
  <div>
    <!-- 通知容器 -->
    <div class="notification-container" v-if="isInSwmPath">
      <transition-group name="notification-fade">
        <div
          v-for="item in notifications"
          :key="item.id"
          class="notification-item"
          :class="getWarningClass(item.warningContent)"
        >
          <!-- 标题栏区域 -->
          <div class="notification-title-bar">
            <div class="notification-title">
              <AlertOutlined style="margin-right: 5px" />
              <span>{{ item.alarmName || '报警通知' }}</span>
            </div>
            <!-- 关闭按钮放在标题栏中 -->
            <div class="notification-close" @click="closeNotification(item.id)">
              <CloseOutlined />
            </div>
          </div>
          <!-- 时间单独放一行 -->
          <div class="notification-time">{{ formatTime(item.warningTime) }}</div>
          <!-- 通知内容 -->
          <div class="notification-body">
            <div class="notification-detail">
              <div><strong>人员：</strong>{{ item.personName || '-' }}</div>
              <div><strong>内容：</strong>{{ item.warningContent || '-' }}</div>
            </div>
          </div>
        </div>
      </transition-group>
    </div>
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref, onMounted, onUnmounted, watch } from 'vue';
  import { AlertOutlined, CloseOutlined } from '@ant-design/icons-vue';
  import { WarningTypeEnum, WarningTypeTextMap } from '@/enums/swm/warningEnum';

  export default defineComponent({
    name: 'NotificationPopup',
    components: {
      AlertOutlined,
      CloseOutlined,
    },
    props: {
      // 传入的通知数据
      notificationList: {
        type: Array,
        default: () => [],
      },
      // 是否在swm路径下
      isInSwmPath: {
        type: Boolean,
        default: false,
      },
    },
    setup(props) {
      // 当前显示的通知列表
      const notifications = ref<any[]>([]);

      // 用于跟踪已处理的通知ID
      const processedIds = ref(new Set<string>());

      // 监听props变化，处理新的通知
      watch(
        [() => props.notificationList, () => props.isInSwmPath],
        ([newList, isInPath]) => {
          console.log('消息弹窗-通知列表变化，长度:', newList?.length || 0);
          console.log('消息弹窗-当前是否在swm路径下:', isInPath);

          // 如果不在swm路径下，不处理通知
          if (!isInPath) {
            console.log('消息弹窗-不在swm路径下，不显示通知');
            return;
          }

          // 遍历新的通知列表，找出未处理的通知
          if (newList && newList.length > 0) {
            newList.forEach((notification: any) => {
              // 检查通知是否已处理
              if (!processedIds.value.has(notification.id)) {
                console.log('消息弹窗-处理新通知:', notification.id);

                // 处理预警类型，确保显示文本而不是数字
                if (notification.warningType === '2' || notification.warningType === 2) {
                  notification.warningTypeText = '被动报警';
                } else if (notification.warningType === '1' || notification.warningType === 1) {
                  notification.warningTypeText = '主动报警';
                } else if (!notification.warningTypeText) {
                  // 如果没有warningTypeText，使用枚举映射获取
                  notification.warningTypeText = getWarningTypeText(notification.warningType);
                }

                // 添加到通知列表
                notifications.value.push({ ...notification });

                // 标记为已处理
                processedIds.value.add(notification.id);

                // 设置自动移除定时器 - 所有消息弹窗通知3秒后自动消失
                setTimeout(() => {
                  notifications.value = notifications.value.filter(
                    (item) => item.id !== notification.id,
                  );
                }, 3000); // 3秒后自动移除
              }
            });
          }
        },
        { immediate: true, deep: true },
      );

      // 获取预警类型文本
      function getWarningTypeText(type: string) {
        // 如果已经是文本，直接返回
        if (type && (type === '主动报警' || type === '被动报警')) {
          return type;
        }

        // 使用枚举映射获取文本
        if (type === WarningTypeEnum.ACTIVE) {
          return WarningTypeTextMap[WarningTypeEnum.ACTIVE]; // '主动报警'
        } else if (type === WarningTypeEnum.PASSIVE) {
          return WarningTypeTextMap[WarningTypeEnum.PASSIVE]; // '被动报警'
        } else {
          // 默认返回被动报警
          return '被动报警';
        }
      }

      // 获取预警内容对应的CSS类
      const getWarningClass = (content: string) => {
        switch (content) {
          case '跌落报警':
            return 'warning-fall';
          case '脱帽报警':
            return 'warning-helmet-off';
          case '安全帽报警':
            return 'warning-helmet';
          case '静默报警':
            return 'warning-silent';
          case '风险提示':
            return 'warning-hazard';
          case 'SIP 应急呼叫':
            return 'warning-sip-sos';
          case '近电报警':
            return 'warning-electric';
          case '应急呼叫':
            return 'warning-sos';
          default:
            return 'warning-default';
        }
      };

      // 格式化时间
      const formatTime = (time: string) => {
        if (!time) return '-';
        const date = new Date(time);
        return `${date.getHours().toString().padStart(2, '0')}:${date
          .getMinutes()
          .toString()
          .padStart(2, '0')}:${date.getSeconds().toString().padStart(2, '0')}`;
      };

      // 关闭指定通知
      const closeNotification = (id: string) => {
        notifications.value = notifications.value.filter((item) => item.id !== id);
        console.log('手动关闭通知:', id);
      };

      // 组件卸载时清理
      onUnmounted(() => {
        processedIds.value.clear();
        notifications.value = [];
      });

      return {
        notifications,
        getWarningClass,
        formatTime,
        closeNotification, // 导出关闭函数
      };
    },
  });
</script>

<style scoped>
  .notification-container {
    position: fixed;
    top: 80px;
    right: 20px;
    z-index: 9999;
    width: 320px;
    max-height: 80vh;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .notification-item {
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
    padding: 12px;
    margin-bottom: 10px;
    position: relative;
    border-left: 4px solid #1890ff;
    animation: slide-in 0.3s ease;
    max-width: 100%;
  }

  /* 标题栏样式 */
  .notification-title-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 4px;
  }

  .notification-title {
    font-weight: bold;
    display: flex;
    align-items: center;
    color: #1890ff;
    max-width: 85%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  /* 关闭按钮放在标题栏中 */
  .notification-close {
    cursor: pointer;
    color: #999;
    font-size: 14px;
    padding: 2px;
    border-radius: 50%;
    transition: all 0.2s;
    margin-left: 8px;
  }

  .notification-close:hover {
    color: #f56c6c;
    background: rgba(0, 0, 0, 0.05);
  }

  /* 时间放在单独一行 */
  .notification-time {
    font-size: 12px;
    color: #999;
    margin-bottom: 8px;
    text-align: right;
  }

  .notification-body {
    font-size: 14px;
    color: #333;
  }

  .notification-detail {
    display: flex;
    flex-direction: column;
    gap: 5px;
  }

  /* 预警类型样式 */
  .warning-fall {
    border-left-color: #f56c6c;
  }
  .warning-fall .notification-title {
    color: #f56c6c;
  }

  .warning-helmet-off {
    border-left-color: #e6a23c;
  }
  .warning-helmet-off .notification-title {
    color: #e6a23c;
  }

  .warning-helmet {
    border-left-color: #faad14;
  }
  .warning-helmet .notification-title {
    color: #faad14;
  }

  .warning-silent {
    border-left-color: #722ed1;
  }
  .warning-silent .notification-title {
    color: #722ed1;
  }

  .warning-hazard {
    border-left-color: #fa541c;
  }
  .warning-hazard .notification-title {
    color: #fa541c;
  }

  .warning-sip-sos {
    border-left-color: #eb2f96;
  }
  .warning-sip-sos .notification-title {
    color: #eb2f96;
  }

  .warning-electric {
    border-left-color: #13c2c2;
  }
  .warning-electric .notification-title {
    color: #13c2c2;
  }

  .warning-sos {
    border-left-color: #f56c6c;
  }
  .warning-sos .notification-title {
    color: #f56c6c;
  }

  .warning-default {
    border-left-color: #1890ff;
  }

  /* 动画效果 */
  .notification-fade-enter-active,
  .notification-fade-leave-active {
    transition: all 0.3s ease;
  }

  .notification-fade-enter-from {
    opacity: 0;
    transform: translateX(30px);
  }

  .notification-fade-leave-to {
    opacity: 0;
    transform: translateX(30px);
  }

  @keyframes slide-in {
    0% {
      transform: translateX(30px);
      opacity: 0;
    }
    100% {
      transform: translateX(0);
      opacity: 1;
    }
  }
</style>
