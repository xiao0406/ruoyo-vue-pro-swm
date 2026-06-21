<template>
  <div class="safety-map">
    <div class="canvas-container" ref="containerRef" @wheel="handleWheel">
      <canvas ref="canvasRef" id="safetyCanvas"></canvas>
    </div>

    <!-- 人员详情弹窗 -->
    <PersonDetailPopup
      :visible="popupVisible"
      :personData="selectedPerson"
      @close="hidePersonDetail"
      @view-attendance="handleViewAttendance"
      @view-track="handleViewTrack"
    />
  </div>
</template>

<script lang="ts" setup>
  import { ref, onMounted, onBeforeUnmount } from 'vue';
  import PersonDetailPopup from './PersonDetailPopup.vue';
  import {
    getEnabledSiteMap,
    getPersonPositions,
    type PersonPosition,
  } from '@/api/swm/personTrack';
  import { processFileUrl } from '@/utils/file/fileUrlUtils';
  import { message } from 'ant-design-vue';

  interface Point {
    x: number;
    y: number;
    id: number;
    name: string;
    workType: string;
    organization: string;
    workShop: string;
    teamGroup: string;
    workHours: string;
    attendanceStatus: string;
    idCard: string;
  }

  // Canvas 相关引用
  const containerRef = ref<HTMLElement | null>(null);
  const canvasRef = ref<HTMLCanvasElement | null>(null);
  let ctx: CanvasRenderingContext2D | null = null;

  // 画布状态
  const scale = ref<number>(1);
  const offsetX = ref(0);
  const offsetY = ref(0);
  const isDragging = ref<boolean>(false);
  const isDraggingCanvas = ref(false);
  let dragStartX = 0;
  let dragStartY = 0;

  // 底图相关
  let img = new Image();
  const backgroundColor = ref<string>('');

  // 弹窗状态
  const popupVisible = ref(false);
  const selectedPerson = ref<any>({});

  // 人员数据 - 从接口获取
  const points = ref<Point[]>([]);

  // 初始化
  onMounted(async () => {
    if (canvasRef.value) {
      ctx = canvasRef.value.getContext('2d');
      canvasRef.value.addEventListener('click', handleClick);
      enableDragCanvas();
    }

    await loadSiteMap();
    await loadPersonPositions();

    // 监听容器尺寸变化
    if (containerRef.value) {
      const resizeObserver = new ResizeObserver(() => {
        handleResize();
      });
      resizeObserver.observe(containerRef.value);

      // 在组件卸载时清理观察器
      onBeforeUnmount(() => {
        resizeObserver.disconnect();
      });
    }

    // 初始化canvas尺寸
    setTimeout(() => {
      handleResize();
    }, 100);
  });

  onBeforeUnmount(() => {
    // ResizeObserver会在上面的onMounted中清理
  });

  // 调整 Canvas 画布尺寸
  function resizeCanvas() {
    const canvas = canvasRef.value;
    const container = containerRef.value;
    if (!canvas || !container) return;

    const devicePixelRatio = window.devicePixelRatio || 1;

    // 使用容器的尺寸而不是整个窗口
    const containerRect = container.getBoundingClientRect();
    const width = containerRect.width;
    const height = containerRect.height;

    canvas.width = width * devicePixelRatio;
    canvas.height = height * devicePixelRatio;

    canvas.style.width = `${width}px`;
    canvas.style.height = `${height}px`;

    const context = canvas.getContext('2d');
    if (context) context.scale(devicePixelRatio, devicePixelRatio);
  }

  // 加载底图
  async function loadSiteMap() {
    try {
      const response = await getEnabledSiteMap();
      if (response.success && response.data) {
        let imageUrl = '';
        try {
          const filePathData = JSON.parse(response.data.filePath);
          imageUrl = processFileUrl(filePathData.previewUrl || response.data.url);
        } catch (error) {
          console.error('解析filePath失败，使用url字段:', error);
          imageUrl = processFileUrl(response.data.url);
        }

        img.src = imageUrl;
        img.onload = () => {
          // 获取图片背景颜色（简化版本）
          backgroundColor.value = '#f0f2f5';
          drawBackground();
          fitToView();
        };
      } else {
        message.error('获取底图失败：' + response.message);
      }
    } catch (error) {
      console.error('加载底图失败:', error);
      message.error('加载底图失败');
    }
  }

  // 加载人员位置数据 - 使用与personTrack.vue相同的接口和逻辑
  async function loadPersonPositions(searchName?: string, organizationKey?: string) {
    try {
      const response = await getPersonPositions({
        searchName,
        organizationKey,
        displayType: '按车间展示',
      });

      if (response.success) {
        // 转换API返回的数据格式为组件需要的格式
        points.value = response.data.map((item: PersonPosition) => ({
          x: item.x,
          y: item.y,
          id: item.id,
          name: item.name,
          workType: item.workType,
          organization: item.organization,
          workShop: item.workShop,
          teamGroup: item.teamGroup,
          workHours: item.workHours,
          attendanceStatus: item.attendanceStatus || item.attendance || '未知',
          idCard: item.idCard || item.identityCard || '',
        }));

        // 重新绘制画布
        redrawAll();

        console.log(`安全地图加载了 ${points.value.length} 个人员位置`);
      } else {
        message.error('加载人员位置数据失败：' + response.message);
      }
    } catch (error) {
      console.error('加载人员位置数据失败:', error);
      message.error('加载人员位置数据失败');
    }
  }

  // 调整图像显示以适应画布大小
  function fitToView() {
    const canvas = canvasRef.value;
    const container = containerRef.value;
    if (!canvas || !container || !img.complete || !img.naturalWidth) return;

    const imgWidth = img.naturalWidth;
    const imgHeight = img.naturalHeight;
    const containerRect = container.getBoundingClientRect();
    const canvasWidth = containerRect.width;
    const canvasHeight = containerRect.height;

    const scaleX = canvasWidth / imgWidth;
    const scaleY = canvasHeight / imgHeight;
    scale.value = Math.max(scaleX, scaleY);

    redrawAll();
  }

  // 绘制背景图
  function drawBackground() {
    const canvas = canvasRef.value;
    if (!ctx || !img.complete) return;

    ctx.clearRect(0, 0, canvas!.width, canvas!.height);

    ctx.save();
    ctx.translate(offsetX.value, offsetY.value);
    ctx.scale(scale.value, scale.value);
    ctx.drawImage(img, 0, 0);
    ctx.restore();

    // 绘制所有人员点位
    points.value.forEach((point) => {
      drawPoint(point.x, point.y);
    });
  }

  // 绘制人员点位 - 使用与personTrack.vue相同的样式
  function drawPoint(x: number, y: number): void {
    if (!ctx) return;

    ctx.save();
    ctx.translate(offsetX.value, offsetY.value);
    ctx.scale(scale.value, scale.value);
    ctx.fillStyle = 'red';
    ctx.shadowBlur = 20;
    ctx.shadowColor = 'yellow';
    ctx.beginPath();
    ctx.arc(x, y, 35, 0, Math.PI * 2);
    ctx.fill();
    ctx.restore();
  }

  // 点击事件处理
  function handleClick(event: MouseEvent) {
    const canvas = canvasRef.value;
    if (!canvas || isDragging.value) return;

    const rect = canvas.getBoundingClientRect();
    const x = Math.round((event.clientX - rect.left - offsetX.value) / scale.value);
    const y = Math.round((event.clientY - rect.top - offsetY.value) / scale.value);

    // 检查是否点击了人员点位
    points.value.forEach((point) => {
      const dx = x - point.x;
      const dy = y - point.y;
      if (dx * dx + dy * dy <= 50 * 50) {
        showPersonDetail(point);
      }
    });
  }

  // 重新绘制所有内容
  function redrawAll() {
    const canvas = canvasRef.value;
    if (!canvas || !ctx) return;

    ctx.clearRect(0, 0, canvas.width, canvas.height);
    drawBackground();
  }

  // 处理鼠标滚轮事件（缩放）
  function handleWheel(event: WheelEvent) {
    event.preventDefault();
    const canvas = canvasRef.value;
    if (!canvas) return;

    const rect = canvas.getBoundingClientRect();
    const mouseX = event.clientX - rect.left;
    const mouseY = event.clientY - rect.top;
    const zoomFactor = 1.1;

    const px = (mouseX - offsetX.value) / scale.value;
    const py = (mouseY - offsetY.value) / scale.value;

    if (event.deltaY < 0) scale.value *= zoomFactor;
    else scale.value /= zoomFactor;
    scale.value = Math.max(0.1, Math.min(20, scale.value));

    offsetX.value = mouseX - px * scale.value;
    offsetY.value = mouseY - py * scale.value;
    redrawAll();
  }

  // 启用画布拖动
  function enableDragCanvas() {
    if (!canvasRef.value) return;
    canvasRef.value.addEventListener('mousedown', handleCanvasMouseDown);
    window.addEventListener('mousemove', handleCanvasMouseMove);
    window.addEventListener('mouseup', handleCanvasMouseUp);
  }

  // 处理鼠标按下事件
  function handleCanvasMouseDown(event: MouseEvent) {
    if (event.button !== 0) return;
    dragStartX = event.clientX - offsetX.value;
    dragStartY = event.clientY - offsetY.value;
    isDraggingCanvas.value = true;
  }

  // 处理鼠标移动事件
  function handleCanvasMouseMove(event: MouseEvent) {
    if (!isDraggingCanvas.value) return;
    isDragging.value = true;
    offsetX.value = event.clientX - dragStartX;
    offsetY.value = event.clientY - dragStartY;
    redrawAll();
  }

  // 处理鼠标释放事件
  function handleCanvasMouseUp() {
    setTimeout(() => {
      isDraggingCanvas.value = false;
      isDragging.value = false;
    }, 100);
  }

  // 窗口大小变化处理
  function handleResize() {
    resizeCanvas();
    fitToView();
  }

  // 显示人员详情
  const showPersonDetail = (point: Point) => {
    selectedPerson.value = {
      ...point,
      jobType: point.workType,
      workshop: point.workShop,
      team: point.teamGroup,
      process: '未知工序', // 当前接口没有工序字段，设置默认值
    };
    popupVisible.value = true;
  };

  // 隐藏人员详情
  const hidePersonDetail = () => {
    popupVisible.value = false;
  };

  // 查看考勤记录
  const handleViewAttendance = (person: Point) => {
    console.log('查看考勤记录:', person);
    hidePersonDetail();
  };

  // 查看行动轨迹
  const handleViewTrack = (person: Point) => {
    console.log('查看行动轨迹:', person);
    hidePersonDetail();
  };
</script>

<style lang="less" scoped>
  .safety-map {
    width: 100%;
    height: 100%;
    position: relative;

    .canvas-container {
      position: relative;
      width: 100%;
      height: 100%;
      overflow: hidden;
      border-radius: 4px;

      canvas {
        display: block;
        cursor: grab;
        width: 100%;
        height: 100%;

        &:active {
          cursor: grabbing;
        }
      }
    }
  }
</style>
