<template>
  <div style="width: 100%; height: 100%">
    <canvas
      ref="mapCanvas"
      class="mapBox"
      @wheel.prevent="handleWheel"
      @mousedown.stop="startDragging"
      @mouseup.stop="stopDragging"
      @mouseleave.stop="stopDragging"
      @mousemove.stop="onMouseMove"
      @click.stop="handleCanvasClick"
    ></canvas>
    <div
      v-if="hoverTooltip.show"
      class="name-tooltip"
      :style="{
        left: hoverTooltip.x + 'px',
        top: hoverTooltip.y + 'px',
        transform: 'translate(-50%, -100%)',
      }"
    >
      {{ hoverTooltip.name }}
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref, watch, onMounted, onUnmounted, reactive } from 'vue';
  import { useUserStore } from '@/store/modules/user';

  const props = defineProps({
    mapImageUrl: String,
    userPositions: Array as any,
    activeUser: Object,
    userType: String,
    highlightColor: {
      type: String,
      default: 'blue',
    },
    normalColor: {
      type: String,
      default: '#FF6B6B',
    },
  });

  const emit = defineEmits(['dotClick']);

  // Canvas 引用
  const mapCanvas = ref<HTMLCanvasElement | null>(null);
  const ctx = ref<CanvasRenderingContext2D | null>(null);

  // 地图图片
  const img = ref<HTMLImageElement | null>(null);

  // 【优化1】将 transform 改为普通对象，消除 Vue 响应式 Proxy 带来的高频性能损耗
  const transform = {
    scale: 1,
    offsetX: 0,
    offsetY: 0,
  };

  let isDragging = false;
  let lastMouse = { x: 0, y: 0 };
  let animationFrameId: number | null = null;

  // 【优化2】引入脏检查标记，统一控制渲染频率
  let isDirty = true;
  // 用于节流 hover 检测
  let hoverTicking = false;

  // ================= 新增：SVG图标缓存与加载 =================
  const svgIconsCache = new Map();
  const loadingIcons = new Set(); // 新增：记录正在加载的颜色，防止重复触发加载

  const loadSvgWithColor = (color: string) => {
    // 【关键修复】如果缓存中已经有了，或者该颜色正在加载中，直接 return，绝不重复创建！
    if (svgIconsCache.has(color) || loadingIcons.has(color)) {
      return;
    }

    // 标记该颜色正在加载
    loadingIcons.add(color);

    const svgString = `
      <svg width="50" height="50" viewBox="0 0 50 50" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M24.998 1C36.3012 1 45.4627 10.1658 45.4629 21.46C45.4629 31.03 38.8958 39.0644 30.0215 41.3037L29.6543 41.3965L29.4404 41.71L24.999 48.2246L20.5596 41.707L20.3457 41.3936L19.9775 41.3008L19.5635 41.1914C10.9005 38.8099 4.53711 30.877 4.53711 21.46C4.53733 10.1657 13.6952 1.00018 24.998 1Z" stroke="${color}" stroke-width="2"/>
        <path d="M34.7811 24.0631V24.5782C34.6096 30.9288 30.3188 35.9061 24.9986 35.9061C19.5061 35.9061 15.0438 30.585 15.0438 24.0595L34.7811 24.0631ZM20.023 9.29877V17.0224C20.023 17.5376 20.5382 18.0518 21.0524 18.0518H28.9502C29.4653 18.0518 29.9796 17.5376 29.9796 17.0224V9.29877C34.099 11.1835 36.6738 15.1323 36.8453 19.5973V20.1142H37.8755C38.3907 20.1142 38.905 20.6294 38.905 21.1445C38.905 21.6597 38.3907 22.1739 37.8755 22.1739H12.1271C11.6128 22.1739 11.0977 21.6597 11.0977 21.1445C11.0977 20.6294 11.6128 20.1142 12.1271 20.1142H13.1574C12.9859 15.4806 15.7322 11.1889 20.023 9.30147V9.29877ZM24.829 8.26758C25.8593 8.26758 26.8887 8.439 27.7467 8.61131V16.1653H21.7399V8.61221C22.9407 8.43989 23.971 8.26758 24.829 8.26758Z" fill="${color}"/>
      </svg>
    `;
    const svgBlob = new Blob([svgString], { type: 'image/svg+xml' });
    const url = URL.createObjectURL(svgBlob);
    const img = new Image();

    img.onload = () => {
      svgIconsCache.set(color, img);
      loadingIcons.delete(color); // 加载完成，移除加载中状态
      URL.revokeObjectURL(url);

      // 如果你使用了上一条回复中的 isDirty 优化，这里写 isDirty = true;
      // 如果你还在用原来的 draw()，这里写 draw();
      isDirty = true;
    };
    img.src = url;
  };
  // ==========================================================

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
      isDirty = true; // 标记重绘
    },
    { deep: true },
  );

  // ================= 聚焦功能：监听活动用户变化 =================
  watch(
    () => props.activeUser,
    (newActiveUser) => {
      if (!newActiveUser || !props.userPositions || !props.userPositions.length) return;

      const targetDot = props.userPositions.find(
        (dot: any) => dot.identityCard === newActiveUser.identityCard,
      );
      if (!targetDot || !mapCanvas.value) return;

      const canvas = mapCanvas.value;
      const centerX = canvas.width / 2;
      const centerY = canvas.height / 2;

      // 聚焦：让目标点出现在中心，并放大
      transform.scale = 1.5; // 聚焦时的缩放比例
      transform.offsetX = centerX - targetDot.x * transform.scale;
      transform.offsetY = centerY - targetDot.y * transform.scale;

      isDirty = true; // 标记重绘
    },
    { deep: true },
  );
  // ==========================================================

  onMounted(() => {
    resizeCanvas();
    if (mapCanvas.value) {
      ctx.value = mapCanvas.value.getContext('2d');
    }
    window.addEventListener('resize', resizeCanvas);
    startAnimationLoop(); // 启动统一渲染循环
  });

  onUnmounted(() => {
    window.removeEventListener('resize', resizeCanvas);
    if (animationFrameId) cancelAnimationFrame(animationFrameId);
  });

  // 【优化3】统一渲染循环：接管所有重绘操作
  function startAnimationLoop() {
    const hasActiveUser = props.activeUser && props.activeUser.identityCard;

    // 只有当画面有变动（拖拽、缩放）或者有呼吸灯动画时，才执行重绘
    if (isDirty || hasActiveUser) {
      draw();
      isDirty = false; // 重置标记
    }

    animationFrameId = requestAnimationFrame(startAnimationLoop);
  }

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
      isDirty = true; // 标记重绘
    };
  }

  function getDotColor(dot: any) {
    if (props.userType === '按人员类型展示' && dot.personTypeColor) {
      return dot.personTypeColor;
    } else if (props.userType === '按车间展示' && dot.positionArchiveColor) {
      return dot.positionArchiveColor;
    } else if (props.userType === '按班组展示' && dot.workGroupColor) {
      return dot.workGroupColor;
    } else if (props.userType === '按工种展示' && dot.workTypeColor) {
      return dot.workTypeColor;
    }
    return props.normalColor;
  }

  function handleCanvasClick(e: MouseEvent) {
    if (!mapCanvas.value || !img.value || !props.userPositions) return;

    const canvas = mapCanvas.value;
    const rect = canvas.getBoundingClientRect();
    const mouseX = e.clientX - rect.left;
    const mouseY = e.clientY - rect.top;

    for (const dot of props.userPositions) {
      const screenX = transform.offsetX + dot.x * transform.scale;
      const screenY = transform.offsetY + dot.y * transform.scale;

      const dx = mouseX - screenX;
      const dy = mouseY - screenY;
      const distance = Math.sqrt(dx * dx + dy * dy);

      // 扩大点击判定范围以适应图标大小
      if (distance <= 20) {
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
    c.translate(transform.offsetX, transform.offsetY);
    c.scale(transform.scale, transform.scale);
    c.drawImage(img.value, 0, 0);
    c.restore();

    // 绘制大屏图标
    drawIcons();
  }

  function drawIcons() {
    if (!ctx.value || !props.userPositions) return;
    const c = ctx.value;

    const normalIconSize = 40;
    const highlightedIconSize = 65;

    // 计算呼吸灯缩放因子 (仅当有选中人员时生效)
    const hasActiveUser = props.activeUser && props.activeUser.identityCard;
    const pulseFactor = hasActiveUser ? Math.sin(Date.now() / 300) * 0.15 + 1 : 1;

    for (const dot of props.userPositions) {
      const screenX = transform.offsetX + dot.x * transform.scale;
      const screenY = transform.offsetY + dot.y * transform.scale;

      const isActive = props.activeUser && dot.identityCard === props.activeUser.identityCard;
      const fillColor = getDotColor(dot) || '#FF6B6B';

      const icon = svgIconsCache.get(fillColor);
      if (icon) {
        const size = isActive ? highlightedIconSize * pulseFactor : normalIconSize;
        // 居中绘制图标
        c.drawImage(icon, screenX - size / 2, screenY - size / 2, size, size);
      } else {
        // 如果缓存中没有该颜色的图标，则去加载
        loadSvgWithColor(fillColor);
      }
    }
  }

  function handleWheel(e: WheelEvent) {
    if (!img.value || !mapCanvas.value) return;

    const canvas = mapCanvas.value;
    const rect = canvas.getBoundingClientRect();
    const mouseX = e.clientX - rect.left;
    const mouseY = e.clientY - rect.top;

    const canvasX = (mouseX - transform.offsetX) / transform.scale;
    const canvasY = (mouseY - transform.offsetY) / transform.scale;

    const zoomFactor = e.deltaY < 0 ? 1.1 : 1 / 1.1;
    let newScale = transform.scale * zoomFactor;

    let minScale;
    if (BASE_CONFIG_ALL[currentCorpCode]) {
      minScale =
        BASE_CONFIG_ALL[currentCorpCode].scale *
        (canvas.width / BASE_CONFIG_ALL[currentCorpCode].baseWidth);
    } else {
      minScale = canvas.width / img.value.width;
    }

    newScale = Math.max(minScale, Math.min(newScale, 3));

    transform.offsetX = mouseX - canvasX * newScale;
    transform.offsetY = mouseY - canvasY * newScale;
    transform.scale = newScale;

    isDirty = true; // 标记重绘
  }

  function startDragging(e: MouseEvent) {
    isDragging = true;
    lastMouse = { x: e.clientX, y: e.clientY };
  }

  const hoverTooltip = reactive({
    show: false,
    x: 0,
    y: 0,
    name: '',
    dot: null,
  });

  function onMouseMove(e: MouseEvent) {
    if (isDragging) {
      const dx = e.clientX - lastMouse.x;
      const dy = e.clientY - lastMouse.y;

      transform.offsetX += dx;
      transform.offsetY += dy;

      lastMouse = { x: e.clientX, y: e.clientY };

      isDirty = true; // 标记重绘
      hoverTooltip.show = false;
      return;
    }

    // 【优化4】非拖拽状态下的 Hover 性能优化 (使用 requestAnimationFrame 节流)
    if (!mapCanvas.value || !img.value) {
      hoverTooltip.show = false;
      return;
    }

    if (!hoverTicking) {
      requestAnimationFrame(() => {
        checkHover(e);
        hoverTicking = false;
      });
      hoverTicking = true;
    }
  }

  // 抽离出的 Hover 检测逻辑
  function checkHover(e: MouseEvent) {
    const canvas = mapCanvas.value;
    if (!canvas) return;
    const rect = canvas.getBoundingClientRect();
    const mouseX = e.clientX - rect.left;
    const mouseY = e.clientY - rect.top;

    let hoveredDot = null;

    for (const dot of props.userPositions) {
      const screenX = transform.offsetX + dot.x * transform.scale;
      const screenY = transform.offsetY + dot.y * transform.scale;

      // 【优化5】快速包围盒测试 (Bounding Box)，减少开平方运算
      if (Math.abs(mouseX - screenX) > 20 || Math.abs(mouseY - screenY) > 20) {
        continue;
      }

      const dx = mouseX - screenX;
      const dy = mouseY - screenY;
      const distance = Math.sqrt(dx * dx + dy * dy);

      if (distance <= 20) {
        hoveredDot = dot;
        break;
      }
    }

    if (hoveredDot) {
      hoverTooltip.show = true;
      hoverTooltip.x = e.clientX;
      hoverTooltip.y = e.clientY - 25;
      hoverTooltip.name = hoveredDot.name;
      hoverTooltip.dot = hoveredDot;
    } else {
      hoverTooltip.show = false;
      hoverTooltip.dot = null;
    }
  }

  function stopDragging() {
    isDragging = false;
    hoverTooltip.show = false;
  }

  const userStore = useUserStore();
  const currentCorpCode = userStore.getPageCacheByKey('currentCorpCode', '0');

  const BASE_CONFIG_ALL = {
    ZJGGGD: { scale: 0.169, offsetX: -700, offsetY: -42, baseWidth: 983 },
    ZHY: {
      scale: 0.11406670000000002,
      offsetX: -145.2180000000002,
      offsetY: -36.49775000000005,
      baseWidth: 857,
    },
  };

  function resetView() {
    if (!img.value || !mapCanvas.value) return;

    const canvas = mapCanvas.value;
    let scale, offsetX, offsetY;

    if (BASE_CONFIG_ALL[currentCorpCode]) {
      scale =
        BASE_CONFIG_ALL[currentCorpCode].scale *
        (canvas.width / BASE_CONFIG_ALL[currentCorpCode].baseWidth);
      offsetX =
        BASE_CONFIG_ALL[currentCorpCode].offsetX *
        (canvas.width / BASE_CONFIG_ALL[currentCorpCode].baseWidth);
      offsetY =
        BASE_CONFIG_ALL[currentCorpCode].offsetY *
        (canvas.width / BASE_CONFIG_ALL[currentCorpCode].baseWidth);
    } else {
      scale = canvas.width / img.value.width;
      offsetX = 0;
      offsetY = (canvas.height - img.value.height * scale) / 2;
    }

    transform.scale = scale;
    transform.offsetX = offsetX;
    transform.offsetY = offsetY;

    isDirty = true; // 标记重绘
  }

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
      const screenFirstX = transform.offsetX + first.x * transform.scale;
      const screenFirstY = transform.offsetY + first.y * transform.scale;
      c.moveTo(screenFirstX, screenFirstY);

      for (let i = 1; i < dots.length; i++) {
        const dot = dots[i];
        const screenX = transform.offsetX + dot.x * transform.scale;
        const screenY = transform.offsetY + dot.y * transform.scale;
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

  .name-tooltip {
    position: fixed;
    z-index: 9999;
    background-color: rgba(0, 0, 0, 0.8);
    color: white;
    padding: 6px 12px;
    border-radius: 6px;
    font-size: 14px;
    pointer-events: none;
    transform: translate(-50%, -100%);
    white-space: nowrap;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    max-width: 200px;
    text-overflow: ellipsis;
    overflow: hidden;
  }

  .name-tooltip::after {
    content: '';
    position: absolute;
    top: 100%;
    left: 50%;
    margin-left: -5px;
    border-width: 5px;
    border-style: solid;
    border-color: rgba(0, 0, 0, 0.8) transparent transparent transparent;
  }
</style>
