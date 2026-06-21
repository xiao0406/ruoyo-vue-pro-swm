<template>
  <canvas
    ref="mapCanvas"
    class="mapBox"
    @wheel.prevent="handleWheel"
    @mousedown.stop="startDragging"
    @mouseup.stop="stopDragging"
    @mouseleave.stop="stopDragging"
    @mousemove.stop="onMouseMove"
    @click.stop="handleCanvasClick"
    @dblclick.stop="handleCanvasDbClick"
    :style="{ cursor: isDragging ? 'grabbing' : 'grab' }"
  ></canvas>
</template>

<script lang="ts" setup>
  import { ref, watch, onMounted, defineProps, defineEmits, onUnmounted } from 'vue';
  import { useUserStore } from '@/store/modules/user';
  let isMouseDown = false; // 新增：跟踪鼠标是否按下
  let dragStartPos = { x: 0, y: 0 };
  const DRAG_THRESHOLD = 5;
  const props = defineProps({
    mapImageUrl: String,
    userPositions: Array,
    activeUser: Object,
    userType: String,
    highlightColor: {
      type: String,
      default: 'blue',
    },
    checkedList: {
      type: Array,
    },
    normalColor: {
      type: String,
      default: 'red',
    },
    dotRadius: {
      type: Number,
      default: 5,
    },
    highlightRadius: {
      type: Number,
      default: 5,
    },
    highlightDots: {
      type: Array,
    },
  });
  // 膝盖

  const emit = defineEmits(['dotClick', 'getClickXy']);

  // Canvas 引用
  const mapCanvas = ref<HTMLCanvasElement | null>(null);
  const ctx = ref<CanvasRenderingContext2D | null>(null);

  // 地图图片
  const img = ref<HTMLImageElement | null>(null);
  const transform = ref({
    scale: 1,
    offsetX: 0,
    offsetY: 0,
  });

  let isDragging = false;
  let lastMouse = { x: 0, y: 0 };

  // 监听图片URL变化
  watch(
    () => props.mapImageUrl,
    (newUrl) => {
      if (newUrl) {
        loadImage(newUrl);
      }
    },
    { immediate: true },
  );

  // 监听用户位置数据变化
  watch(
    () => props.userPositions,
    () => {
      draw();
    },
    { deep: true },
  );
  // 监听用户位置数据变化
  watch(
    () => props.highlightDots,
    () => {
      draw();
    },
    { deep: true },
  );
  watch(
    () => props.checkedList,
    () => {
      draw();
    },
    { deep: true },
  );

  // 监听活动用户变化
  watch(
    () => props.activeUser,
    (newActiveUser) => {
      if (!newActiveUser || !props.userPositions || !props.userPositions.length) return;

      const targetDot = props.userPositions.find((dot) => dot.id === newActiveUser.id);

      // if (!targetDot || !mapCanvas.value) return;

      // const canvas = mapCanvas.value;
      // const centerX = canvas.width / 2;
      // const centerY = canvas.height / 2;

      // // 让目标点出现在中心
      // transform.value.offsetX = centerX - targetDot?.pixelX * 1.5;
      // transform.value.offsetY = centerY - targetDot?.pixelY * 1.5;
      // transform.value.scale = 1.5;
      draw();
    },
    { deep: true },
  );

  onMounted(() => {
    resizeCanvas();
    if (mapCanvas.value) {
      ctx.value = mapCanvas.value.getContext('2d');
    }
    // 添加 resize 监听器
    window.addEventListener('resize', resizeCanvas);
  });
  onUnmounted(() => {
    // 组件销毁时移除监听器
    window.removeEventListener('resize', resizeCanvas);
  });

  function resizeCanvas() {
    if (!mapCanvas.value) return;

    const canvas = mapCanvas.value;
    const container = canvas.parentElement;
    if (!container) return;

    const rect = container.getBoundingClientRect();
    canvas.width = rect.width;
    canvas.height = rect.height;

    if (img.value) {
      resetView();
    }
  }

  function loadImage(url: string) {
    const image = new Image();
    image.src = url;
    image.onload = () => {
      img.value = image;
      resetView();
      draw();
    };
  }

  function getDotColor(dot: any) {
    return dot.beaconColor || props.normalColor;
  }
  function handleCanvasDbClick(e) {
    if (isDragging) {
      return;
    }
    if (!mapCanvas.value || !img.value || !props.userPositions) return;

    const canvas = mapCanvas.value;
    const rect = canvas.getBoundingClientRect();
    const mouseX = e.clientX - rect.left;
    const mouseY = e.clientY - rect.top;
    // 转换为画布原始坐标（去除缩放和偏移的影响）
    const canvasX = (mouseX - transform.value.offsetX) / transform.value.scale;
    const canvasY = (mouseY - transform.value.offsetY) / transform.value.scale;
    emit('getClickXy', canvasX, canvasY);
  }
  function handleCanvasClick(e: MouseEvent) {
    if (isDragging) {
      return;
    }
    if (!mapCanvas.value || !img.value || !props.userPositions) return;

    const canvas = mapCanvas.value;
    const rect = canvas.getBoundingClientRect();
    const mouseX = e.clientX - rect.left;
    const mouseY = e.clientY - rect.top;
    // 转换为画布原始坐标（去除缩放和偏移的影响）
    const canvasX = (mouseX - transform.value.offsetX) / transform.value.scale;
    const canvasY = (mouseY - transform.value.offsetY) / transform.value.scale;

    for (const dot of props.userPositions) {
      const screenX = transform.value.offsetX + dot.pixelX * transform.value.scale;
      const screenY = transform.value.offsetY + dot.pixelY * transform.value.scale;

      const dx = mouseX - screenX;
      const dy = mouseY - screenY;
      const distance = Math.sqrt(dx * dx + dy * dy);

      if (distance <= 11) {
        emit('dotClick', dot);
        return;
      }
    }
  }

  function draw() {
    if (!ctx.value || !img.value) return;

    const c = ctx.value;
    const canvas = mapCanvas.value!;

    // 清空画布
    c.clearRect(0, 0, canvas.width, canvas.height);

    // 绘制地图变换层
    c.save();
    c.translate(transform.value.offsetX, transform.value.offsetY);
    c.scale(transform.value.scale, transform.value.scale);
    c.drawImage(img.value, 0, 0);
    c.restore();

    // 绘制固定大小的标点
    drawDots();
  }
  function drawDots() {
    if (!ctx.value || !props.userPositions) return;

    const c = ctx.value;
    const highlightScale = 2; // 高亮点和数字的放大倍数
    const activeUser = props.activeUser; // 缓存activeUser

    // 1. 先绘制所有普通点
    for (const dot of props.userPositions) {
      if (props.checkedList?.includes(dot.beaconTypeText)) {
        const isHighlighted = props.highlightDots?.some((h) => h.id === dot.id);

        // 判断是否是当前活动用户
        const isActive = activeUser && dot.id === activeUser.id;

        // 使用活动用户坐标或原始点坐标
        const targetX = isActive ? activeUser.pixelX : dot.pixelX;
        const targetY = isActive ? activeUser.pixelY : dot.pixelY;

        const screenX = transform.value.offsetX + targetX * transform.value.scale;
        const screenY = transform.value.offsetY + targetY * transform.value.scale;

        let radius = isActive ? props.highlightRadius * highlightScale : props.dotRadius;
        const color = getDotColor(dot);

        // 绘制点
        c.beginPath();
        c.arc(screenX, screenY, radius, 0, Math.PI * 2);
        c.shadowColor = color;
        c.shadowBlur = radius / 2;
        c.fillStyle = color;
        c.fill();
        c.closePath();

        // 如果是活动用户，添加额外效果
        if (isActive) {
          c.beginPath();
          c.arc(screenX, screenY, radius * 1.3, 0, Math.PI * 2);
          c.strokeStyle = color;
          c.lineWidth = 2;
          c.stroke();
        }
      }
    }

    // 2. 绘制高亮点的连接线和序号（保持不变）
    if (props.highlightDots?.length > 1) {
      c.lineWidth = 3;
      c.lineCap = 'round';

      for (let i = 0; i < props.highlightDots.length; i++) {
        const currentDot = props.highlightDots[i];
        const nextDot = props.highlightDots[(i + 1) % props.highlightDots.length];

        // 这里也要考虑活动用户坐标
        const currentX =
          activeUser && currentDot.id === activeUser.id ? activeUser.pixelX : currentDot.pixelX;
        const currentY =
          activeUser && currentDot.id === activeUser.id ? activeUser.pixelY : currentDot.pixelY;

        const nextX =
          activeUser && nextDot.id === activeUser.id ? activeUser.pixelX : nextDot.pixelX;
        const nextY =
          activeUser && nextDot.id === activeUser.id ? activeUser.pixelY : nextDot.pixelY;

        const startX = transform.value.offsetX + currentX * transform.value.scale;
        const startY = transform.value.offsetY + currentY * transform.value.scale;
        const endX = transform.value.offsetX + nextX * transform.value.scale;
        const endY = transform.value.offsetY + nextY * transform.value.scale;

        c.strokeStyle = getDotColor(currentDot);
        c.beginPath();
        c.moveTo(startX, startY);
        c.lineTo(endX, endY);
        c.stroke();

        drawHighlightOrderNumber(c, startX, startY, i + 1, getDotColor(currentDot));
      }
    }
  }

  // 绘制清晰的高亮序号
  function drawHighlightOrderNumber(
    ctx: CanvasRenderingContext2D,
    x: number,
    y: number,
    order: number,
    color: string,
  ) {
    const baseRadius = props.dotRadius;
    const fontSize = Math.max(12, baseRadius * 1.5); // 字体大小适中

    // 1. 绘制圆形背景
    ctx.beginPath();
    ctx.arc(x, y, baseRadius * 1.8, 0, Math.PI * 2);
    ctx.fillStyle = color;
    ctx.fill();

    // 2. 设置精细字体
    ctx.font = `500 ${fontSize}px 'Segoe UI', 'PingFang SC', system-ui, sans-serif`; // 使用更精细的字体
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';

    // 3. 仅填充文字（不要描边）
    ctx.fillStyle = 'white';
    ctx.fillText(order.toString(), x, y + 1); // 微调垂直位置
  }

  function handleWheel(e: WheelEvent) {
    if (!img.value || !mapCanvas.value) return;

    const canvas = mapCanvas.value;
    const rect = canvas.getBoundingClientRect();
    const mouseX = e.clientX - rect.left;
    const mouseY = e.clientY - rect.top;

    const canvasX = (mouseX - transform.value.offsetX) / transform.value.scale;
    const canvasY = (mouseY - transform.value.offsetY) / transform.value.scale;

    const zoomFactor = e.deltaY < 0 ? 1.1 : 1 / 1.1;
    let newScale = transform.value.scale * zoomFactor;

    // 设置最小和最大缩放比例
    let minScale;
    if (BASE_CONFIG_ALL[currentCorpCode]) {
      // 如果有租户配置，使用配置的缩放比例作为最小值
      minScale = BASE_CONFIG.scale * (canvas.width / BASE_CONFIG.baseWidth);
    } else {
      // 否则使用动态计算的最小缩放（图片宽度等于容器宽度）
      minScale = canvas.width / img.value.width;
    }

    newScale = Math.max(minScale, Math.min(newScale, 3));

    transform.value.offsetX = mouseX - canvasX * newScale;
    transform.value.offsetY = mouseY - canvasY * newScale;
    transform.value.scale = newScale;

    draw();
  }

  function startDragging(e: MouseEvent) {
    isMouseDown = true; // 鼠标按下
    isDragging = false; // 初始未拖动
    lastMouse = { x: e.clientX, y: e.clientY };
    dragStartPos = { x: e.clientX, y: e.clientY };
  }
  function onMouseMove(e: MouseEvent) {
    if (!isMouseDown) return; // 关键：只有鼠标按下时才处理

    // 如果还未确定为拖动，先检查距离
    if (!isDragging) {
      const dx = e.clientX - dragStartPos.x;
      const dy = e.clientY - dragStartPos.y;
      const distance = Math.sqrt(dx * dx + dy * dy);
      if (distance > DRAG_THRESHOLD) {
        isDragging = true;
      } else {
        return; // 未达到阈值，不处理
      }
    }

    // 处理拖动
    const dx = e.clientX - lastMouse.x;
    const dy = e.clientY - lastMouse.y;

    transform.value.offsetX += dx;
    transform.value.offsetY += dy;

    lastMouse = { x: e.clientX, y: e.clientY };
    draw();
  }
  function stopDragging() {
    setTimeout(() => {
      isMouseDown = false; // 鼠标松开
      isDragging = false;
    }, 50);
  }

  function resetView() {
    if (!img.value || !mapCanvas.value) return;

    const canvas = mapCanvas.value;
    let scale, offsetX, offsetY;

    // 如果有特定租户配置，使用配置值
    if (BASE_CONFIG_ALL[currentCorpCode]) {
      // 使用配置的缩放比例和偏移
      scale = BASE_CONFIG.scale * (canvas.width / BASE_CONFIG.baseWidth);
      offsetX = BASE_CONFIG.offsetX * (canvas.width / BASE_CONFIG.baseWidth);
      offsetY = BASE_CONFIG.offsetY * (canvas.width / BASE_CONFIG.baseWidth);
    } else {
      // 动态计算缩放比例，让图片宽度等于容器宽度
      scale = canvas.width / img.value.width;
      offsetX = 0;
      offsetY = (canvas.height - img.value.height * scale) / 2;
    }

    transform.value = {
      scale: scale,
      offsetX: offsetX,
      offsetY: offsetY,
    };
    draw();
  }
  // 租户配置
  const userStore = useUserStore();
  const currentCorpCode = userStore.getPageCacheByKey('currentCorpCode', '0');

  const BASE_CONFIG_ALL = {
    ZJGGGD: { scale: 0.169, offsetX: -700, offsetY: -42, baseWidth: 983 },
    ZJGGJS: {
      scale: 0.13722216245833344,
      offsetX: -14.015876517201377,
      offsetY: 54.5230522749996,
      baseWidth: 1352,
    },
    ZJGGSC: {
      scale: 0.15552925142857152,
      offsetX: 104.32931074380122,
      offsetY: 55.327227051947716,
      baseWidth: 1352,
    },
    ZHY: {
      scale: 0.18995120000000007,
      offsetX: -282.00100000000043,
      offsetY: -158.96400000000017,
      baseWidth: 1352,
    },
  };

  const BASE_CONFIG = BASE_CONFIG_ALL[currentCorpCode] || {
    scale: 0.1,
    offsetX: 0,
    offsetY: 0,
    baseWidth: 1352,
  };

  // 控制台打印transform信息的方法，baseWidth就是你当前map元素宽度，不用乱写，要和当前的transform元素所在盒子宽度保持一致
  (window as any).printBaseConfig = () => {
    console.log('当前图片的缩放比例，位置信息:', transform.value);
  };

  // 暴露方法给父组件
  defineExpose({
    resetView,
    connectDots: (...dots: any[]) => {
      if (dots.length < 2 || !ctx.value) return;

      const c = ctx.value;
      c.save();
      c.strokeStyle = 'blue';
      c.lineWidth = 2;
      c.beginPath();

      const first = dots[0];
      const screenFirstX = transform.value.offsetX + first.x * transform.value.scale;
      const screenFirstY = transform.value.offsetY + first.y * transform.value.scale;
      c.moveTo(screenFirstX, screenFirstY);

      for (let i = 1; i < dots.length; i++) {
        const dot = dots[i];
        const screenX = transform.value.offsetX + dot.pixelX * transform.value.scale;
        const screenY = transform.value.offsetY + dot.pixelY * transform.value.scale;
        c.lineTo(screenX, screenY);
      }

      c.stroke();
      c.restore();
    },
  });
</script>

<style scoped>
  .mapBox {
    width: 100%;
    height: 100%;
    cursor: grab;
  }
</style>
