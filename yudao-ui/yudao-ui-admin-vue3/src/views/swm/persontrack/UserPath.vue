<template>
  <div class="mask"></div>
  <div class="msgModal">
    <Spin :spinning="spinning" tip="加载中...">
      <div class="topTitle">
        <div> </div>
        <div class="tile">{{ props.activeUser.name }} 行动轨迹</div>
        <div class="icon" @click="emit('changeIsShowUserPathModal', false)">
          <CloseOutlined />
        </div>
      </div>
      <div class="contextBox">
        <div class="leftHistory">
          <div class="topTime">
            <div class="title">—— 请选择时间 ——</div>
            <div class="selectTime">
              <RangePicker
                class="timeSelect"
                @change="funGetPersonTrajectory"
                v-model:value="formObj.time"
                :show-time="true"
              />
            </div>
          </div>
          <div class="pathMsg">
            <!-- 时间节点 -->
            <div
              v-for="(item, index) in userPathList.data"
              :key="item.time + index"
              class="timelineItem"
            >
              <div class="timelineDot"></div>
              <div class="timelineContent">
                <div class="time">{{ item.time }}</div>
                <div class="desc">{{ props.activeUser.name }} 进入 {{ item.area_name }}</div>
              </div>
            </div>
          </div>
        </div>
        <div class="rightBigbox">
          <canvas
            ref="mapCanvasModal"
            class="mapBox"
            @wheel.prevent="handleWheel"
            @mousedown="startDragging"
            @mouseup="stopDragging"
            @mouseleave="stopDragging"
            @mousemove="onMouseMove"
          ></canvas>

          <!-- 新增：底部时间轴 -->
          <div class="timelineParent" v-if="trajectoryPoints.length >= 2">
            <!-- 起点时间 -->
            <div class="time-text">{{ startPoint?.time }}</div>

            <!-- 进度条主体 -->
            <div class="progress-container">
              <div class="progress-track">
                <!-- 已走过的进度 -->
                <div
                  class="progress-fill"
                  :style="{ width: `${(animationCompleted ? 1 : animationProgress) * 100}%` }"
                ></div>
                <!-- 进度条滑块与当前时间提示 -->
                <div
                  class="progress-thumb"
                  :style="{ left: `${(animationCompleted ? 1 : animationProgress) * 100}%` }"
                >
                  <div class="current-time-tooltip" v-show="isAnimating || animationCompleted">
                    {{ currentTimeStr }}
                  </div>
                </div>
              </div>
            </div>

            <!-- 终点时间 -->
            <div class="time-text">{{ endPoint?.time }}</div>
          </div>
        </div>
      </div>
    </Spin>
  </div>
</template>

<script lang="ts" setup>
  import { ref, onMounted, watch, reactive, computed, onUnmounted } from 'vue';
  import { CloseOutlined } from '@ant-design/icons-vue';
  import { RangePicker, message, Spin } from 'ant-design-vue';
  import dayjs from 'dayjs';
  import {
    getPersonTrajectoryByDateTime,
    getAreaFenceDataByIdCardByDateTime,
  } from '@/api/swm/newDataScree';

  const props = defineProps({
    mapObj: Object,
    activeUser: Object as any,
  });

  const emit = defineEmits(['changeIsShowUserPathModal']);

  let spinning = ref(false);

  // 表单数据
  let formObj = reactive({
    time: [dayjs().subtract(1, 'day'), dayjs()],
    personId: props.activeUser?.id,
    idCard: props.activeUser?.id_card,
    startDate: '',
    endDate: '',
    timeRange: [0, 0],
    minVal: 0,
    maxVal: 0,
  });

  // 轨迹数据
  let userPositions = reactive({ data: [] });
  let userPathList = reactive({ data: [] });

  // ================= 动画相关数据 =================
  const trajectoryPoints = ref<Array<{ x: number; y: number; time?: string }>>([]);
  const startPoint = ref<{ x: number; y: number; time?: string } | null>(null);
  const endPoint = ref<{ x: number; y: number; time?: string } | null>(null);
  const animationProgress = ref(0);
  let animationId: number | null = null;
  const isAnimating = ref(false);
  const animationCompleted = ref(false);
  const currentAnimatedPoint = ref<{ x: number; y: number; angle: number } | null>(null);

  // ================= 计算当前动画执行到的时间 =================
  const currentTimeStr = computed(() => {
    if (trajectoryPoints.value.length < 2) return '';
    if (animationProgress.value <= 0) return startPoint.value?.time || '';
    if (animationProgress.value >= 1 || animationCompleted.value) return endPoint.value?.time || '';

    const progress = animationProgress.value;
    const totalSegments = trajectoryPoints.value.length - 1;
    const exactIndex = progress * totalSegments;
    const index = Math.floor(exactIndex);
    const t = exactIndex - index;

    const p1 = trajectoryPoints.value[index];
    const p2 = trajectoryPoints.value[index + 1];

    if (!p1.time || !p2.time) return '';

    const t1 = dayjs(p1.time).valueOf();
    const t2 = dayjs(p2.time).valueOf();
    const currentTime = t1 + (t2 - t1) * t;

    return dayjs(currentTime).format('YYYY-MM-DD HH:mm:ss');
  });

  // Canvas 相关
  const mapCanvasModal = ref<HTMLCanvasElement | null>(null);
  const ctx = ref<CanvasRenderingContext2D | null>(null);
  const img = ref<HTMLImageElement | null>(null);
  const imgSrc = ref<string>('');

  // 视图状态
  const transform = ref({
    scale: 1,
    offsetX: 0,
    offsetY: 0,
  });

  let isDragging = false;
  let lastMouse = { x: 0, y: 0 };
  let initialScale: number;

  const funGetPersonTrajectory = async () => {
    if (!formObj.time || formObj.time.length < 2) {
      console.warn('时间范围未选择');
      return;
    }

    formObj.startDate = dayjs(formObj.time[0]).format('YYYY-MM-DD HH:mm:ss');
    formObj.endDate = dayjs(formObj.time[1]).format('YYYY-MM-DD HH:mm:ss');

    // 重置动画状态
    animationCompleted.value = false;
    animationProgress.value = 0;
    currentAnimatedPoint.value = null;
    stopAnimation();

    spinning.value = true;
    try {
      const result = await getPersonTrajectoryByDateTime({
        personId: props.activeUser.id,
        idCard: props.activeUser.identityCard || props.activeUser.id_card,
        startDate: formObj.startDate,
        endDate: formObj.endDate,
      });

      if (result?.data?.trajectoryPoints?.length > 0) {
        let rawPoints = result.data.trajectoryPoints;
        let points: any = [];

        // 【核心修改】：过滤掉连续坐标相同的点，防止动画原地停顿
        if (rawPoints.length > 0) {
          points.push(rawPoints[0]);
          for (let i = 1; i < rawPoints.length; i++) {
            const currentPoint = rawPoints[i];
            const lastPoint = points[points.length - 1];
            if (currentPoint.x !== lastPoint.x || currentPoint.y !== lastPoint.y) {
              points.push(currentPoint);
            }
          }
        }

        userPositions.data = points.map((point: any, index: number) => ({
          ...point,
          index: index + 1,
        }));

        trajectoryPoints.value = points;

        if (points.length >= 1) {
          startPoint.value = {
            x: points[0].x,
            y: points[0].y,
            time: points[0].time,
          };
        }

        if (points.length >= 2) {
          endPoint.value = {
            x: points[points.length - 1].x,
            y: points[points.length - 1].y,
            time: points[points.length - 1].time,
          };
        }

        fitViewToTrajectory();
        startAnimation();
      } else {
        userPositions.data = [];
        trajectoryPoints.value = [];
        startPoint.value = null;
        endPoint.value = null;
        message.warning('该时间段内没有轨迹数据');
      }

      const fenceResult = await getAreaFenceDataByIdCardByDateTime({
        idCard: props.activeUser.identityCard || props.activeUser.id_card,
        startDate: formObj.startDate,
        endDate: formObj.endDate,
      });

      userPathList.data = fenceResult.data || [];
    } catch (err) {
      console.error('获取轨迹数据失败:', err);
      message.error('获取轨迹数据失败');
    } finally {
      setTimeout(() => {
        spinning.value = false;
      }, 300);
    }
  };

  // 调整视图以适应轨迹
  function fitViewToTrajectory() {
    if (!mapCanvasModal.value || trajectoryPoints.value.length < 2) return;

    const canvas = mapCanvasModal.value;
    const points = trajectoryPoints.value;

    let minX = points[0].x;
    let maxX = points[0].x;
    let minY = points[0].y;
    let maxY = points[0].y;

    for (const point of points) {
      minX = Math.min(minX, point.x);
      maxX = Math.max(maxX, point.x);
      minY = Math.min(minY, point.y);
      maxY = Math.max(maxY, point.y);
    }

    const trajectoryWidth = maxX - minX;
    const trajectoryHeight = maxY - minY;

    const padding = 50;
    const scaleX = (canvas.width - padding * 2) / trajectoryWidth;
    const scaleY = (canvas.height - padding * 2) / trajectoryHeight;
    const scale = Math.min(scaleX, scaleY, 1);

    const offsetX = (canvas.width - trajectoryWidth * scale) / 2 - minX * scale;
    const offsetY = (canvas.height - trajectoryHeight * scale) / 2 - minY * scale;

    transform.value.scale = scale;
    transform.value.offsetX = offsetX;
    transform.value.offsetY = offsetY;
  }

  // 开始动画（动态时长）
  function startAnimation() {
    if (trajectoryPoints.value.length < 2) return;

    if (animationId) {
      cancelAnimationFrame(animationId);
    }

    isAnimating.value = true;
    animationCompleted.value = false;
    animationProgress.value = 0;
    let lastTime = 0;

    // 动态计算动画总时长：每段(两个点之间) 1秒 (1000ms)
    const totalSegments = trajectoryPoints.value.length - 1;
    const dynamicDuration = totalSegments * 1000;

    function animate(currentTime: number) {
      if (!lastTime) lastTime = currentTime;
      const deltaTime = currentTime - lastTime;
      lastTime = currentTime;

      animationProgress.value += deltaTime / dynamicDuration;

      if (animationProgress.value >= 1) {
        animationProgress.value = 1;
        animationCompleted.value = true;
        isAnimating.value = false;
        currentAnimatedPoint.value = getAnimatedPoint();
        draw();
        return;
      }

      animationId = requestAnimationFrame(animate);
      draw();
    }

    animationId = requestAnimationFrame(animate);
  }

  function stopAnimation() {
    isAnimating.value = false;
    if (animationId) {
      cancelAnimationFrame(animationId);
      animationId = null;
    }
  }

  // 计算动画当前点位置和角度
  function getAnimatedPoint(): { x: number; y: number; angle: number } {
    if (trajectoryPoints.value.length < 2) {
      return { x: 0, y: 0, angle: 0 };
    }

    const progress = animationProgress.value;
    const totalSegments = trajectoryPoints.value.length - 1;
    const exactIndex = progress * totalSegments;
    const index = Math.floor(exactIndex);
    const t = exactIndex - index;

    if (index >= trajectoryPoints.value.length - 1) {
      const p1 = trajectoryPoints.value[trajectoryPoints.value.length - 2];
      const p2 = trajectoryPoints.value[trajectoryPoints.value.length - 1];
      return {
        x: p2.x,
        y: p2.y,
        angle: Math.atan2(p2.y - p1.y, p2.x - p1.x),
      };
    }

    const p1 = trajectoryPoints.value[index];
    const p2 = trajectoryPoints.value[index + 1];

    return {
      x: p1.x + (p2.x - p1.x) * t,
      y: p1.y + (p2.y - p1.y) * t,
      angle: Math.atan2(p2.y - p1.y, p2.x - p1.x),
    };
  }

  // 绘制起点
  function drawStartPoint(x: number, y: number) {
    if (!ctx.value) return;
    const c = ctx.value;
    const screenX = transform.value.offsetX + x * transform.value.scale;
    const screenY = transform.value.offsetY + y * transform.value.scale;

    c.save();
    c.beginPath();
    c.arc(screenX, screenY, 12, 0, Math.PI * 2);
    c.fillStyle = '#52c41a';
    c.fill();

    c.beginPath();
    c.arc(screenX, screenY, 12, 0, Math.PI * 2);
    c.strokeStyle = '#ffffff';
    c.lineWidth = 3;
    c.stroke();

    c.beginPath();
    c.arc(screenX, screenY, 8, 0, Math.PI * 2);
    c.fillStyle = '#ffffff';
    c.fill();

    c.fillStyle = '#52c41a';
    c.font = 'bold 14px Arial';
    c.textAlign = 'center';
    c.textBaseline = 'middle';
    c.fillText('起', screenX, screenY);
    c.restore();
  }

  // 绘制终点
  function drawEndPoint(x: number, y: number) {
    if (!ctx.value) return;
    const c = ctx.value;
    const screenX = transform.value.offsetX + x * transform.value.scale;
    const screenY = transform.value.offsetY + y * transform.value.scale;

    c.save();
    c.beginPath();
    c.arc(screenX, screenY, 12, 0, Math.PI * 2);
    c.fillStyle = '#fa8c16';
    c.fill();

    c.beginPath();
    c.arc(screenX, screenY, 12, 0, Math.PI * 2);
    c.strokeStyle = '#ffffff';
    c.lineWidth = 3;
    c.stroke();

    c.beginPath();
    c.arc(screenX, screenY, 8, 0, Math.PI * 2);
    c.fillStyle = '#ffffff';
    c.fill();

    c.fillStyle = '#fa8c16';
    c.font = 'bold 14px Arial';
    c.textAlign = 'center';
    c.textBaseline = 'middle';
    c.fillText('终', screenX, screenY);
    c.restore();
  }

  // 绘制科技感箭头
  function drawMovingLight(x: number, y: number, angle: number) {
    if (!ctx.value) return;
    const c = ctx.value;
    const screenX = transform.value.offsetX + x * transform.value.scale;
    const screenY = transform.value.offsetY + y * transform.value.scale;

    c.save();
    if (!animationCompleted.value) {
      c.translate(screenX, screenY);
      c.rotate(angle);

      c.beginPath();
      c.arc(0, 0, 20, 0, Math.PI * 2);
      const gradient1 = c.createRadialGradient(0, 0, 0, 0, 0, 20);
      gradient1.addColorStop(0, 'rgba(64, 169, 255, 0.6)');
      gradient1.addColorStop(1, 'rgba(64, 169, 255, 0)');
      c.fillStyle = gradient1;
      c.fill();

      c.beginPath();
      c.moveTo(12, 0);
      c.lineTo(-8, 10);
      c.lineTo(-4, 0);
      c.lineTo(-8, -10);
      c.closePath();

      const arrowGradient = c.createLinearGradient(-8, 0, 12, 0);
      arrowGradient.addColorStop(0, '#1890ff');
      arrowGradient.addColorStop(1, '#ffffff');
      c.fillStyle = arrowGradient;
      c.fill();

      c.shadowColor = '#40a9ff';
      c.shadowBlur = 10;
      c.strokeStyle = '#ffffff';
      c.lineWidth = 1.5;
      c.stroke();
    }
    c.restore();
  }

  // 绘制路径线
  function drawPathLine() {
    if (!ctx.value || trajectoryPoints.value.length < 2) return;

    const c = ctx.value;
    const progress = animationCompleted.value ? 1 : animationProgress.value;
    const animatedPoint = getAnimatedPoint();

    c.save();

    if (animationCompleted.value) {
      c.beginPath();
      const firstPoint = trajectoryPoints.value[0];
      let screenX = transform.value.offsetX + firstPoint.x * transform.value.scale;
      let screenY = transform.value.offsetY + firstPoint.y * transform.value.scale;
      c.moveTo(screenX, screenY);

      for (let i = 1; i < trajectoryPoints.value.length; i++) {
        screenX = transform.value.offsetX + trajectoryPoints.value[i].x * transform.value.scale;
        screenY = transform.value.offsetY + trajectoryPoints.value[i].y * transform.value.scale;
        c.lineTo(screenX, screenY);
      }

      c.strokeStyle = '#1890ff';
      c.lineWidth = 3;
      c.lineCap = 'round';
      c.lineJoin = 'round';
      c.stroke();
    } else {
      c.beginPath();
      const firstPoint = trajectoryPoints.value[0];
      let screenX = transform.value.offsetX + firstPoint.x * transform.value.scale;
      let screenY = transform.value.offsetY + firstPoint.y * transform.value.scale;
      c.moveTo(screenX, screenY);

      for (let i = 1; i < trajectoryPoints.value.length; i++) {
        screenX = transform.value.offsetX + trajectoryPoints.value[i].x * transform.value.scale;
        screenY = transform.value.offsetY + trajectoryPoints.value[i].y * transform.value.scale;
        c.lineTo(screenX, screenY);
      }

      c.strokeStyle = 'rgba(150, 150, 150, 0.4)';
      c.lineWidth = 2;
      c.lineCap = 'round';
      c.lineJoin = 'round';
      c.stroke();

      const totalSegments = trajectoryPoints.value.length - 1;
      const exactIndex = progress * totalSegments;
      const completeSegments = Math.floor(exactIndex);
      const currentSegmentProgress = exactIndex - completeSegments;

      c.beginPath();
      screenX = transform.value.offsetX + firstPoint.x * transform.value.scale;
      screenY = transform.value.offsetY + firstPoint.y * transform.value.scale;
      c.moveTo(screenX, screenY);

      for (let i = 1; i <= completeSegments && i < trajectoryPoints.value.length; i++) {
        screenX = transform.value.offsetX + trajectoryPoints.value[i].x * transform.value.scale;
        screenY = transform.value.offsetY + trajectoryPoints.value[i].y * transform.value.scale;
        c.lineTo(screenX, screenY);
      }

      if (completeSegments < totalSegments) {
        const currentIndex = completeSegments;
        const p1 = trajectoryPoints.value[currentIndex];
        const p2 = trajectoryPoints.value[currentIndex + 1];
        const currentX = p1.x + (p2.x - p1.x) * currentSegmentProgress;
        const currentY = p1.y + (p2.y - p1.y) * currentSegmentProgress;
        screenX = transform.value.offsetX + currentX * transform.value.scale;
        screenY = transform.value.offsetY + currentY * transform.value.scale;
        c.lineTo(screenX, screenY);
      }

      c.strokeStyle = '#1890ff';
      c.lineWidth = 3;
      c.lineCap = 'round';
      c.lineJoin = 'round';
      c.stroke();

      drawMovingLight(animatedPoint.x, animatedPoint.y, animatedPoint.angle);
    }

    c.restore();
  }

  // Canvas 绘制函数
  function draw() {
    if (!ctx.value || !img.value) return;

    const c = ctx.value;
    const canvas = mapCanvasModal.value!;

    c.clearRect(0, 0, canvas.width, canvas.height);

    c.save();
    c.translate(transform.value.offsetX, transform.value.offsetY);
    c.scale(transform.value.scale, transform.value.scale);
    c.drawImage(img.value, 0, 0);
    c.restore();

    if (trajectoryPoints.value.length >= 2) {
      drawPathLine();
      if (startPoint.value) drawStartPoint(startPoint.value.x, startPoint.value.y);
      if (endPoint.value) drawEndPoint(endPoint.value.x, endPoint.value.y);
    }
  }

  // Canvas 交互函数
  function handleWheel(e: WheelEvent) {
    if (!img.value || !mapCanvasModal.value) return;

    const canvas = mapCanvasModal.value;
    const rect = canvas.getBoundingClientRect();
    const mouseX = e.clientX - rect.left;
    const mouseY = e.clientY - rect.top;
    const canvasX = (mouseX - transform.value.offsetX) / transform.value.scale;
    const canvasY = (mouseY - transform.value.offsetY) / transform.value.scale;
    const zoomFactor = e.deltaY < 0 ? 1.1 : 1 / 1.1;

    let newScale = transform.value.scale * zoomFactor;
    const minScale = initialScale;
    const maxScale = 5;
    newScale = Math.max(minScale, Math.min(newScale, maxScale));

    transform.value.offsetX = mouseX - canvasX * newScale;
    transform.value.offsetY = mouseY - canvasY * newScale;
    transform.value.scale = newScale;

    draw();
  }

  function startDragging(e: MouseEvent) {
    isDragging = true;
    lastMouse = { x: e.clientX, y: e.clientY };
  }

  function onMouseMove(e: MouseEvent) {
    if (!isDragging) return;

    const dx = e.clientX - lastMouse.x;
    const dy = e.clientY - lastMouse.y;

    transform.value.offsetX += dx;
    transform.value.offsetY += dy;

    lastMouse = { x: e.clientX, y: e.clientY };
    draw();
  }

  function stopDragging() {
    isDragging = false;
  }

  // 视图控制
  function resetView() {
    if (!img.value || !mapCanvasModal.value) return;

    const canvas = mapCanvasModal.value;
    const imgWidth = img.value.width;
    const imgHeight = img.value.height;

    initialScale = canvas.width / imgWidth;
    const scaledImgHeight = imgHeight * initialScale;
    const offsetY = (canvas.height - scaledImgHeight) / 2;

    transform.value.scale = initialScale;
    transform.value.offsetX = 0;
    transform.value.offsetY = offsetY;
  }

  function loadImageAndDraw() {
    if (!mapCanvasModal.value || !imgSrc.value) return;

    const image = new Image();
    image.src = imgSrc.value;
    image.onload = () => {
      img.value = image;
      resetView();
      draw();
    };
  }

  function resizeCanvas() {
    const canvas = mapCanvasModal.value;
    if (!canvas) return;

    const bigbox = canvas.parentElement;
    if (!bigbox) return;

    const rect = bigbox.getBoundingClientRect();
    canvas.width = rect.width;
    canvas.height = rect.height;

    if (img.value) {
      resetView();
    }
  }

  // 初始化
  onMounted(() => {
    funGetPersonTrajectory();
    resizeCanvas();

    if (mapCanvasModal.value) {
      ctx.value = mapCanvasModal.value.getContext('2d');

      if (props.mapObj?.filePath?.previewUrl) {
        imgSrc.value = props.mapObj?.filePath?.previewUrl;
        loadImageAndDraw();
      }
    }

    window.addEventListener('resize', resizeCanvas);
  });

  onUnmounted(() => {
    stopAnimation();
    window.removeEventListener('resize', resizeCanvas);
  });

  // 监听数据变化
  watch(
    () => props.mapObj?.filePath?.previewUrl,
    (newUrl) => {
      if (newUrl && newUrl !== imgSrc.value) {
        imgSrc.value = newUrl;
        loadImageAndDraw();
      }
    },
    { immediate: true },
  );

  watch(
    () => props.activeUser,
    (newActiveUser) => {
      if (!newActiveUser || !userPositions.data.length) return;

      const targetDot: any = userPositions.data.find(
        (dot: any) => dot.id_card === newActiveUser.id_card,
      );
      if (!targetDot) return;

      const canvas = mapCanvasModal.value;
      if (!canvas) return;

      const fixedScale = 1.5;
      const centerX = canvas.width / 2;
      const centerY = canvas.height / 2;

      transform.value.offsetX = centerX - targetDot.x * fixedScale;
      transform.value.offsetY = centerY - targetDot.y * fixedScale;
      transform.value.scale = fixedScale;
      draw();
    },
  );
</script>

<style lang="less" scoped>
  .mask {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background-color: rgba(0, 0, 0, 0.5);
    z-index: 998;
  }

  ::v-deep .ant-slider {
    margin: 0;
    padding: 0;
  }

  ::v-deep .ant-spin-nested-loading {
    width: 100% !important;
    height: 100% !important;
    position: static;

    .ant-spin-container {
      width: 100%;
      height: 100%;
    }
  }

  .msgModal {
    width: 80vw;
    height: 80vh;
    position: fixed;
    z-index: 999;
    left: 10%;
    top: 10%;
    background: url('@/assets/swm/modalBg.png') no-repeat left top / 100% 100%;

    .contextBox {
      display: flex;
      align-items: center;
      justify-content: space-between;
      width: 100%;
      padding: 0px 20px;
      height: calc(100% - 85px);
      margin-top: 20px;

      .leftHistory {
        width: 35%;
        height: 100%;

        .topTime {
          width: 100%;
          height: 101px;
          background-color: rgb(32, 65, 102);

          .title {
            width: 100%;
            font-family: Noto Sans SC, Noto Sans SC;
            font-weight: bold;
            font-size: 14px;
            color: #ffffff;
            text-align: center;
            font-style: normal;
            text-transform: none;
            padding-top: 14px;
          }

          .selectTime {
            width: 80%;
            margin: 0 auto;
            margin-top: 15px;

            .timeSelect {
              width: 100%;
              height: 30px;
              line-height: 30px;
              font-family: Noto Sans SC, Noto Sans SC;
              font-weight: bold;
              font-size: 14px;
              color: #24f2ff;
              text-align: left;
              font-style: normal;
              text-transform: none;
              background-color: rgb(39, 78, 118);
              border: 1px solid #7294a3;
            }
          }
        }

        .pathMsg {
          width: 100%;
          height: calc(100% - 121px);
          overflow-y: scroll;
          scrollbar-width: none;
          padding-left: 32px;
          padding-top: 12px;
          padding-bottom: 12px;
          background-color: rgb(15, 38, 70);

          &::-webkit-scrollbar {
            display: none;
          }

          .timelineItem {
            position: relative;
            height: 71px;
            border-left: 2px solid #71829c;

            .timelineDot {
              position: absolute;
              left: -6px;
              top: 0px;
              width: 12px;
              height: 12px;
              border-radius: 50%;
              background-color: #409eff;
            }

            .timelineContent {
              margin-left: 10px;

              .time {
                font-family: Noto Sans SC, Noto Sans SC;
                font-weight: bold;
                font-size: 14px;
                color: #f3f3f3;
                text-align: left;
                font-style: normal;
                text-transform: none;
                position: relative;
                top: -6px;
              }

              .desc {
                font-family: Noto Sans SC, Noto Sans SC;
                font-weight: bold;
                font-size: 14px;
                color: #f3f3f3;
                text-align: left;
                font-style: normal;
                text-transform: none;
              }
            }
          }
        }
      }

      .rightBigbox {
        width: 65%;
        box-sizing: border-box;
        height: 100%;
        user-select: none;
        position: relative; /* 新增：为了让底部时间轴绝对定位 */

        .mapBox {
          width: 100%;
          height: 100%;
          cursor: grab;
          background-color: #000000;
        }

        /* 新增：底部时间轴样式 */
        .timelineParent {
          position: absolute;
          bottom: 0;
          left: 0;
          width: 100%;
          height: 60px;
          background: rgba(5, 64, 98, 0.8);
          border-top: 2px solid #39585d;
          display: flex;
          align-items: center;
          padding: 0 20px;
          box-sizing: border-box;
          z-index: 10;

          .time-text {
            font-family: Noto Sans SC, Noto Sans SC;
            font-weight: bold;
            font-size: 14px;
            color: #24f2ff;
            white-space: nowrap;
          }

          .progress-container {
            flex: 1;
            margin: 0 30px;
            position: relative;

            .progress-track {
              width: 100%;
              height: 8px;
              background-color: #1a324a;
              border-radius: 4px;
              position: relative;
              border: 1px solid #39585d;

              .progress-fill {
                height: 100%;
                background: linear-gradient(90deg, #0f4c8a 0%, #1890ff 100%);
                border-radius: 4px;
                position: absolute;
                left: 0;
                top: 0;
              }

              .progress-thumb {
                position: absolute;
                top: 50%;
                transform: translate(-50%, -50%);
                width: 16px;
                height: 16px;
                background-color: #ffffff;
                border: 4px solid #1890ff;
                border-radius: 50%;
                box-shadow: 0 0 10px rgba(24, 144, 255, 0.8);
                cursor: pointer;

                .current-time-tooltip {
                  position: absolute;
                  top: -40px;
                  left: 50%;
                  transform: translateX(-50%);
                  background: rgba(5, 64, 98, 0.9);
                  border: 1px solid #24f2ff;
                  color: #24f2ff;
                  padding: 4px 8px;
                  border-radius: 4px;
                  font-size: 12px;
                  font-weight: bold;
                  white-space: nowrap;
                  box-shadow: 0 0 8px rgba(36, 242, 255, 0.3);

                  /* 小尖角 */
                  &::after {
                    content: '';
                    position: absolute;
                    bottom: -6px;
                    left: 50%;
                    transform: translateX(-50%);
                    border-width: 6px 6px 0;
                    border-style: solid;
                    border-color: #24f2ff transparent transparent transparent;
                  }
                }
              }
            }
          }
        }
      }
    }

    .topTitle {
      margin-top: 26px;
      height: 32px;
      font-family: Noto Sans SC, Noto Sans SC;
      font-weight: bold;
      font-size: 22px;
      color: #7be5ff;
      letter-spacing: 1px;
      text-align: left;
      font-style: normal;
      text-transform: none;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 44px;

      .icon {
        font-size: 25px;
        cursor: pointer;
      }
    }
  }
</style>
