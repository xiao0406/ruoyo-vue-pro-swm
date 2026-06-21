<template>
  <div>
    <!-- 需要确认的警告弹窗 -->
    <div
      class="global-warning-popup"
      v-if="showPopup && confirmWarnings.length > 0 && isInSwmPath"
      :class="{ collapsed: isCollapsed }"
      :style="{ top: position.y + 'px', left: position.x + 'px' }"
      ref="popupRef"
    >
      <div class="warning-popup-header" @mousedown="startDrag" :class="{ dragging: isDragging }">
        <div class="header-title">
          <span>报警告警通知</span>
          <span class="warning-count" v-if="isCollapsed">({{ confirmWarnings.length }})</span>
        </div>
        <div class="header-actions">
          <span class="collapse-btn" @click="toggleCollapse">
            {{ isCollapsed ? '展开' : '折叠' }}
          </span>
          <span class="close-btn" @click="closePopup">
            <CloseOutlined />
          </span>
        </div>
      </div>
      <div class="warning-content" v-show="!isCollapsed">
        <a-list :data-source="confirmWarnings" :pagination="false">
          <template #renderItem="{ item }">
            <a-list-item>
              <div class="warning-item">
                <div class="warning-time">
                  {{ formatTime(item.warningTime) }}
                </div>
                <div class="warning-info">
                  <div class="warning-type">
                    <span class="label">报警性质：</span>
                    <span class="value">{{ getWarningTypeText(item.warningType) }}</span>
                  </div>
                  <div class="warning-detail">
                    <span class="label">人员姓名：</span>
                    <span class="value">{{ item.personName || '-' }}</span>
                  </div>
                  <div class="warning-detail">
                    <span class="label">报警类型：</span>
                    <span class="value">{{ item.warningContent || '-' }}</span>
                  </div>
                </div>
                <div class="warning-action">
                  <a-button type="primary" size="small" @click="handleConfirm(item.id)"
                    >确认</a-button
                  >
                </div>
              </div>
            </a-list-item>
          </template>
        </a-list>
      </div>
    </div>

    <!-- 只需通知的警告组件 -->
    <!-- <NotificationPopup :notificationList="notificationWarnings" :isInSwmPath="isInSwmPath" /> -->
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref, onMounted, onUnmounted, computed } from 'vue';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { getPopupWarnings, confirmWarning } from '@/api/swm/warning';
  import { List, Button } from 'ant-design-vue';
  import { CloseOutlined } from '@ant-design/icons-vue';
  import NotificationPopup from './NotificationPopup.vue';
  import { useRouter } from 'vue-router';
  import { WarningTypeEnum, WarningTypeTextMap } from '@/enums/swm/warningEnum';

  export default defineComponent({
    name: 'GlobalWarningPopup',
    components: {
      AList: List,
      AListItem: List.Item,
      AButton: Button,
      CloseOutlined,
      NotificationPopup,
    },
    setup() {
      const { createMessage } = useMessage();
      const router = useRouter(); // 使用路由
      const confirmWarnings = ref<any[]>([]); // 需要确认的警告
      const notificationWarnings = ref<any[]>([]); // 只需通知的警告
      const showPopup = ref<boolean>(false);
      const lastFetchTime = ref<string>('未获取');
      const lastError = ref<string>('');
      const isCollapsed = ref<boolean>(false); // 添加折叠状态
      let timer: ReturnType<typeof setInterval> | null = null;
      const popupRef = ref<HTMLElement | null>(null);

      // 拖动相关状态
      const isDragging = ref<boolean>(false);
      const position = ref({ x: window.innerWidth - 420, y: 50 }); // 右边留20px边距
      const dragOffset = ref({ x: 0, y: 0 });

      // 判断当前是否在swm路径下
      const isInSwmPath = computed(() => {
        const currentPath = router.currentRoute.value.path;
        console.log('当前路由路径:', currentPath);
        return currentPath.includes('/swm');
      });

      // 获取报警类型文本   zwf
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

      // 根据屏幕大小调整位置
      function updateDefaultPosition() {
        // 窗口大小改变时更新位置，确保弹窗始终在右边
        position.value.x = window.innerWidth - 420;
      }

      // 拖动开始
      function startDrag(e: MouseEvent) {
        if (!popupRef.value) return;

        isDragging.value = true;

        // 计算鼠标点击位置与弹窗左上角的偏移
        const rect = popupRef.value.getBoundingClientRect();
        dragOffset.value = {
          x: e.clientX - rect.left,
          y: e.clientY - rect.top,
        };

        // 禁用过渡效果，使拖动更加流畅
        if (popupRef.value) {
          popupRef.value.style.transition = 'none';
        }

        // 添加移动和释放事件监听器
        document.addEventListener('mousemove', onDrag);
        document.addEventListener('mouseup', stopDrag);

        // 阻止默认行为和冒泡
        e.preventDefault();
      }

      // 拖动中
      function onDrag(e: MouseEvent) {
        if (!isDragging.value || !popupRef.value) return;

        // 使用requestAnimationFrame来优化渲染
        requestAnimationFrame(() => {
          // 直接操作DOM以获得更好的性能
          if (popupRef.value) {
            // 考虑页面滚动位置
            const newLeft = e.clientX - dragOffset.value.x;
            const newTop = e.clientY - dragOffset.value.y;

            popupRef.value.style.left = `${newLeft}px`;
            popupRef.value.style.top = `${newTop}px`;

            // 同步更新ref值（用于组件重新渲染时保持位置）
            position.value = {
              x: newLeft,
              y: newTop,
            };
          }
        });
      }

      // 拖动结束
      function stopDrag() {
        isDragging.value = false;
        document.removeEventListener('mousemove', onDrag);
        document.removeEventListener('mouseup', stopDrag);

        // 恢复过渡效果，但仅用于折叠/展开
        if (popupRef.value) {
          popupRef.value.style.transition = 'height 0.3s ease, width 0.3s ease';
        }
      }

      // 切换折叠状态
      function toggleCollapse() {
        isCollapsed.value = !isCollapsed.value;
      }

      // 格式化时间
      function formatTime(time: string) {
        if (!time) return '-';
        const date = new Date(time);
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(
          date.getDate(),
        ).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(
          date.getMinutes(),
        ).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`;
      }

      // 获取告警数据
      async function fetchWarnings() {
        // 只有在swm路径下才获取数据
        if (!isInSwmPath.value) {
          console.log('当前不在swm路径下，跳过获取告警数据');
          return;
        }

        try {
          console.log('开始获取告警数据...');
          lastFetchTime.value = new Date().toLocaleString();

          const response = await getPopupWarnings();
          console.log('告警数据获取结果:', response);

          // 处理确认和通知告警数据
          let confirmList: any[] = [];
          let notificationList: any[] = [];

          if (response) {
            // 检查是否是包装格式 {code, msg, data}
            if (
              typeof response === 'object' &&
              response !== null &&
              'code' in response &&
              'data' in response
            ) {
              const wrappedResponse = response as any;
              if (wrappedResponse.code === 0 && typeof wrappedResponse.data === 'object') {
                const responseData = wrappedResponse.data;

                // 提取需要确认的告警列表(在告警通知页面显示)
                if (Array.isArray(responseData.confirmList)) {
                  confirmList = responseData.confirmList;
                  console.log('获取到需要确认的告警数据:', confirmList.length, '条');
                }

                // 提取通知类告警列表(在消息弹窗显示)
                if (Array.isArray(responseData.notificationList)) {
                  notificationList = responseData.notificationList;
                  console.log('获取到通知类告警数据:', notificationList.length, '条');
                }
              }
            }
            // 如果直接返回对象
            else if (typeof response === 'object' && !Array.isArray(response)) {
              console.log('直接获取到告警对象数据');

              // 尝试提取确认列表和通知列表
              if ('confirmList' in response && Array.isArray(response.confirmList)) {
                confirmList = response.confirmList;
              }

              if ('notificationList' in response && Array.isArray(response.notificationList)) {
                notificationList = response.notificationList;
              }
            }
          }

          // 更新需要确认的告警列表 - 用于告警通知页面
          if (confirmList.length > 0) {
            confirmWarnings.value = confirmList.slice(0, 5); // 最多显示5条需要确认的
            showPopup.value = true;
            console.log(
              `成功获取到${confirmWarnings.value.length}条需确认告警数据，用于告警通知页面`,
            );
          } else {
            confirmWarnings.value = [];
            console.log('未获取到需确认告警数据或数据为空');
            showPopup.value = false;
          }

          // 更新通知类告警列表 - 用于消息弹窗页面，将所有通知一次性传给消息弹窗组件
          // 消息弹窗组件内部会处理显示逻辑（3秒后消失）
          notificationWarnings.value = notificationList.slice(0, 10); // 最多显示10条通知
          console.log(`获取到${notificationList.length}条通知类告警，用于消息弹窗页面`);
        } catch (error) {
          console.error('获取告警数据失败', error);
          lastError.value = error instanceof Error ? error.message : '未知错误';
        }
      }

      // 确认告警
      async function handleConfirm(id: string) {
        try {
          await confirmWarning(id);
          createMessage.success('报警已确认');

          // 从列表中移除已确认的告警
          confirmWarnings.value = confirmWarnings.value.filter((item) => item.id !== id);

          // 如果没有告警了，关闭弹窗
          if (confirmWarnings.value.length === 0) {
            showPopup.value = false;
          }
        } catch (error) {
          console.error('确认告警失败', error);
          lastError.value = error instanceof Error ? error.message : '确认告警失败';
          createMessage.error('确认告警失败');
        }
      }

      // 关闭弹窗
      function closePopup() {
        showPopup.value = false;
      }

      // 组件挂载时启动轮询
      onMounted(() => {
        console.log('告警组件已挂载，准备获取数据');
        console.log('当前是否在swm路径下:', isInSwmPath.value);

        // 初始化弹窗位置为屏幕右侧
        updateDefaultPosition();

        // 添加窗口大小变化的监听器
        window.addEventListener('resize', updateDefaultPosition);

        // 添加全局刷新警告事件监听器
        window.addEventListener('refresh-warnings', fetchWarnings);

        // 添加路由变化监听器
        router.afterEach(() => {
          console.log('路由变化，当前是否在swm路径下:', isInSwmPath.value);
          // 如果进入了swm路径，立即获取数据
          if (isInSwmPath.value) {
            fetchWarnings();
          }
        });

        // 延迟1秒后获取第一次数据，确保应用已完全加载
        setTimeout(() => {
          // 仅在swm路径下才初始化获取数据
          if (isInSwmPath.value) {
            fetchWarnings();
          }

          // 初始化元素的过渡效果，仅用于折叠/展开
          if (popupRef.value) {
            popupRef.value.style.transition = 'height 0.3s ease, width 0.3s ease';
          }
        }, 1000);

        // 每30秒获取一次数据（但仍然只在swm路径下请求）
        timer = setInterval(() => {
          fetchWarnings(); // 函数内部会检查路径
        }, 30000);
      });

      // 组件卸载时清除轮询
      onUnmounted(() => {
        if (timer) {
          clearInterval(timer);
          timer = null;
        }

        // 清除拖动相关事件监听
        document.removeEventListener('mousemove', onDrag);
        document.removeEventListener('mouseup', stopDrag);

        // 移除窗口大小变化的监听器
        window.removeEventListener('resize', updateDefaultPosition);

        // 移除全局刷新警告事件监听器
        window.removeEventListener('refresh-warnings', fetchWarnings);
      });

      return {
        confirmWarnings,
        notificationWarnings,
        showPopup,
        lastFetchTime,
        lastError,
        formatTime,
        handleConfirm,
        closePopup,
        isCollapsed,
        toggleCollapse,
        getWarningTypeText,
        // 拖动相关
        popupRef,
        position,
        isDragging,
        startDrag,
        // 新增路径检查
        isInSwmPath,
      };
    },
  });
</script>

<style lang="less" scoped>
  .global-warning-popup {
    position: fixed;
    width: 400px;
    max-height: 500px;
    background-color: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    border-radius: 4px;
    z-index: 1000;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    // 移除全局transition，改为在JS中控制特定属性的过渡
    // transition: all 0.3s ease;

    // 移除默认位置，改为通过JS控制
    // top: 80px;
    // right: 20px;

    // 折叠状态样式
    &.collapsed {
      max-height: 50px;
      width: 250px;
    }
  }

  .warning-popup-header {
    background-color: #f56c6c;
    color: white;
    padding: 10px 15px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    cursor: move; // 添加拖动光标
    user-select: none; // 防止文字被选中

    &.dragging {
      cursor: grabbing; // 拖动时的光标
    }

    .header-title {
      display: flex;
      align-items: center;

      .warning-count {
        margin-left: 5px;
        font-size: 12px;
      }
    }

    .header-actions {
      display: flex;
      align-items: center;
    }

    .collapse-btn {
      cursor: pointer;
      margin-right: 10px;
      font-size: 14px;
      user-select: none;
    }

    .close-btn {
      cursor: pointer;
      font-size: 16px;
    }
  }

  .warning-content {
    padding: 10px;
    max-height: 400px;
    overflow-y: auto;
  }

  .warning-item {
    width: 100%;
    display: flex;
    flex-direction: column;

    .warning-time {
      font-size: 12px;
      color: #999;
      margin-bottom: 5px;
    }

    .warning-info {
      flex: 1;

      .warning-type {
        font-weight: bold;
        margin-bottom: 5px;

        .value {
          color: #f56c6c;
        }
      }

      .warning-detail {
        margin-bottom: 3px;

        .label {
          color: #666;
        }
      }
    }

    .warning-action {
      margin-top: 10px;
      text-align: right;
    }
  }

  /* 调试面板样式 */
  .debug-panel {
    position: fixed;
    bottom: 20px;
    right: 20px;
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    z-index: 999;
    overflow: hidden;
    width: 200px;
  }

  .debug-header {
    background-color: #1890ff;
    color: white;
    padding: 8px 12px;
    font-size: 14px;
  }

  .debug-content {
    padding: 12px;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
</style>
