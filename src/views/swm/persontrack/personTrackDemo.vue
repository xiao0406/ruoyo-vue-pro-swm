<template>
  <div class="canvas-container" ref="containerRef" @wheel="handleWheel">
    <canvas ref="canvasRef" width="800" height="600"></canvas>
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted } from 'vue';
  import tuzhiUrl from './tuZhi.png'; // 确保路径正确

  interface Point {
    x: number;
    y: number;
  }

  const canvasRef = ref<HTMLCanvasElement | null>(null);
  const ctx = ref<CanvasRenderingContext2D | null>(null);
  const isDraggingCanvas = ref<boolean>(false); // 画布拖动状态标志
  let dragStartX = 0,
    dragStartY = 0; // 拖动开始时的鼠标坐标
  const points = ref<Point[]>([]);
  const scale = ref<number>(1);
  const offsetX = ref(0); // X 轴偏移量（用于模拟视图平移）
  const offsetY = ref(0); // Y 轴偏移量
  let img = new Image();

  const isDragging = ref<boolean>(false);
  let dragPointIndex: number | null = null;
  let lastX = 0,
    lastY = 0;

  img.src = tuzhiUrl;

  onMounted(() => {
    if (canvasRef.value) {
      ctx.value = canvasRef.value.getContext('2d');
      img.onload = () => drawBackground();

      canvasRef.value.addEventListener('click', handleClick);
      // 可以拖拽改动点的位置，注释后不生效，后面做标记的时候用
      enableDragPoints();
      enableDragCanvas(); // 启用画布的拖动功能
    }
  });
  function drawBackground() {
    if (!ctx.value || !img.complete) return;

    ctx.value.drawImage(img, 0, 0, img.width, img.height);
  }
  function drawPoint(x: number, y: number): void {
    if (!ctx.value) return;
    ctx.value.beginPath();
    ctx.value.arc(x, y, 5, 0, Math.PI * 2); // 注意这里去掉了 / scale.value
    ctx.value.fill();
  }
  // 假设 scale 是一个响应式的变量，用于存储当前的缩放比例。
  // let scale = 1.0; // 初始缩放比例

  function handleClick(event) {
    if (!canvasRef.value || isDragging.value) return;

    const rect = canvasRef.value.getBoundingClientRect();
    // 计算鼠标点击位置相对于canvas的实际位置，并根据当前缩放和平移进行调整
    // 使用Math.round确保坐标为整数像素值
    const x = Math.round((event.clientX - rect.left - offsetX.value) / scale.value);
    const y = Math.round((event.clientY - rect.top - offsetY.value) / scale.value);

    points.value.push({ x: x, y: y });
    redrawAll();
  }

  function redrawAll() {
    const canvas = canvasRef.value;
    const ctx = canvas?.getContext('2d');
    if (!canvas || !ctx) return;

    // 清除整个画布
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // 保存状态
    ctx.save();

    // 先平移画布
    ctx.translate(offsetX.value, offsetY.value);

    // 再应用缩放
    ctx.scale(scale.value, scale.value);

    // 绘制背景图
    drawBackground();

    // 绘制所有点和线
    points.value.forEach((point, index) => {
      drawPoint(point.x, point.y);
      if (index > 0) {
        const prev = points.value[index - 1];
        drawLine(prev.x, prev.y, point.x, point.y);
      }
    });

    // 恢复状态
    ctx.restore();
  }

  // 当需要改变缩放比例时，调用此函数
  function changeScale(newScale) {
    scale.value = newScale;
    redrawAll();
  }

  function drawLine(x1: number, y1: number, x2: number, y2: number): void {
    if (!ctx.value) return;
    ctx.value.beginPath();
    ctx.value.moveTo(x1, y1);
    ctx.value.lineTo(x2, y2);
    // 设置路径的颜色
    ctx.value.strokeStyle = 'red';
    ctx.value.stroke();
  }

  function handleWheel(event: WheelEvent) {
    event.preventDefault();

    const canvas = canvasRef.value;
    if (!canvas) return;

    const rect = canvas.getBoundingClientRect();
    const mouseX = event.clientX - rect.left;
    const mouseY = event.clientY - rect.top;

    const zoomFactor = 1.1;

    // 计算缩放前的相对坐标
    const px = (mouseX - offsetX.value) / scale.value;
    const py = (mouseY - offsetY.value) / scale.value;

    // 改变缩放值
    if (event.deltaY < 0) {
      scale.value *= zoomFactor;
    } else {
      scale.value /= zoomFactor;
    }
    scale.value = Math.max(0.1, Math.min(20, scale.value));

    // 重新计算偏移，使得缩放中心为鼠标位置
    offsetX.value = mouseX - px * scale.value;
    offsetY.value = mouseY - py * scale.value;

    redrawAll();
  }
  function enableDragPoints() {
    if (!canvasRef.value) return;

    canvasRef.value.addEventListener('mousedown', handleMouseDown);
    window.addEventListener('mousemove', handleMouseMove);
    window.addEventListener('mouseup', handleMouseUp);
  }

  function enableDragCanvas() {
    if (!canvasRef.value) return;

    canvasRef.value.addEventListener('mousedown', handleCanvasMouseDown);
    window.addEventListener('mousemove', handleCanvasMouseMove);
    window.addEventListener('mouseup', handleCanvasMouseUp);
  }
  // 处理鼠标按下事件（用于拖动画布）
  function handleCanvasMouseDown(event: MouseEvent) {
    if (event.button !== 0) return; // 只允许鼠标左键拖动
    const rect = canvasRef.value.getBoundingClientRect();
    dragStartX = event.clientX - offsetX.value;
    dragStartY = event.clientY - offsetY.value;
    isDraggingCanvas.value = true;
  }

  // 处理鼠标移动事件（用于拖动画布）
  function handleCanvasMouseMove(event: MouseEvent) {
    if (!isDraggingCanvas.value) return;
    isDragging.value = true; // 设置拖拽结束

    offsetX.value = event.clientX - dragStartX;
    offsetY.value = event.clientY - dragStartY;

    redrawAll(); // 重新绘制所有内容
  }

  // 处理鼠标释放事件（用于停止拖动画布）
  function handleCanvasMouseUp() {
    setTimeout(() => {
      isDraggingCanvas.value = false;
      isDragging.value = false; // 设置拖拽结束
    }, 100);
  }

  function handleMouseDown(event: MouseEvent) {
    const rect = canvasRef.value.getBoundingClientRect();
    // 计算鼠标点击位置相对于canvas的实际位置，并根据当前缩放和平移进行调整
    // 使用Math.round确保坐标为整数像素值
    const x = Math.round((event.clientX - rect.left - offsetX.value) / scale.value);
    const y = Math.round((event.clientY - rect.top - offsetY.value) / scale.value);

    for (let i = 0; i < points.value.length; i++) {
      const dx = x - points.value[i].x;
      const dy = y - points.value[i].y;
      if (Math.sqrt(dx * dx + dy * dy) < 10 / scale.value) {
        // 点击范围为半径为10/scale的圆
        isDragging.value = true;
        dragPointIndex = i;
        lastX = x;
        lastY = y;
        break;
      }
    }
  }

  // 处理鼠标移动事件
  function handleMouseMove(event: MouseEvent) {
    if (!isDragging.value || dragPointIndex === null) return;

    const rect = canvasRef.value.getBoundingClientRect();
    const newX = (event.clientX - rect.left) / scale.value;
    const newY = (event.clientY - rect.top) / scale.value;

    // 更新点的位置，确保坐标为整数像素值
    points.value[dragPointIndex].x = Math.round(points.value[dragPointIndex].x + newX - lastX);
    points.value[dragPointIndex].y = Math.round(points.value[dragPointIndex].y + newY - lastY);

    lastX = newX;
    lastY = newY;

    redrawAll(); // 重新绘制所有内容
  }

  // 现有的 handleMouseUp 方法
  function handleMouseUp() {
    setTimeout(() => {
      isDragging.value = false; // 设置拖拽结束
    }, 100);
    dragPointIndex = null;
  }

  // 添加点击事件监听器
  onMounted(() => {
    if (canvasRef.value) {
      canvasRef.value.addEventListener('click', handleClick);
    }
  });
</script>

<style scoped>
  .canvas-container {
    position: relative;
    width: 800px;
    height: 600px;
    overflow: hidden;
  }
</style>
