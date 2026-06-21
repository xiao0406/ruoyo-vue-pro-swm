<template>
  <BasicModal
    v-bind="$attrs"
    @register="trajectoryModal"
    @ok="handleSubmit"
    title="行动轨迹"
    width="80%"
    okText="关闭"
    :mask-closable="false"
    :footer="null"
  >
    <div class="rotundity-content" :style="{ backgroundColor: backgroundColor }">
      <div class="canvas-container" ref="containerRef" @wheel="handleWheel">
        <div class="left-content" style="pointer-events: none">
          <div class="slect-time" style="pointer-events: auto">请选择时间</div>
          <div style="pointer-events: auto; margin-bottom: 15px">
            <DatePicker
              v-model:value="selectedDate"
              format="YYYY-MM-DD"
              :disabled-date="disabledDate"
              :allow-clear="false"
              placeholder="请选择日期"
              style="width: 100%"
              size="middle"
            />
          </div>
          <div class="time-box" style="pointer-events: auto">
            <Timeline>
              <TimelineItem v-for="item in TimelineList" :key="item.time">
                <template #dot>
                  <div
                    :class="
                      item.type === 1
                        ? 'rotundity-blue'
                        : item.type === 2
                        ? 'rotundity-red'
                        : 'rotundity-yellow'
                    "
                  ></div>
                </template>
                <p class="time">{{ item.time }}</p>
                <p class="content">{{ item.name }} {{ item.content }}</p>
              </TimelineItem>
            </Timeline>
          </div>
        </div>
        <canvas ref="canvasRef1" id="responsiveCanvas" class="canvas-item"></canvas>
        <div class="right-content" style="pointer-events: none">
          <div class="demo-container" style="pointer-events: auto">
            <Slider
              range
              v-model:value="timeRange"
              :min="0"
              :max="86400"
              :step="300"
              :tipFormatter="formatTimeTooltip"
              @afterChange="onTimeRangeChange"
            />
            <!-- <TimeSlider v-model="timeRange" :min="0" :max="86400" :step="300" /> -->
          </div>
        </div>
      </div>
    </div>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, watch, onMounted } from 'vue';
  import { DatePicker, Timeline, TimelineItem, message } from 'ant-design-vue';
  import dayjs, { Dayjs } from 'dayjs';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  // import TimeSlider from './TimeSlider.vue'
  import { Slider } from 'ant-design-vue';
  // import tuzhiUrl from './tuZhi.png'; // 注释掉本地图片引用
  import {
    getPersonTrajectory,
    getAreaFenceDataByIdCard,
    getPersonByIdCard,
    getEnabledSiteMap,
    type PersonPosition,
    type SiteMapInfo,
  } from '@/api/swm/personTrack';
  import { processFileUrl } from '@/utils/file/fileUrlUtils';

  const emit = defineEmits(['success']);
  const timeRange = ref<[number, number]>([0, 61200]); // 默认0:00-17:00
  const selectedDate = ref<Dayjs>(dayjs()); // 默认选择今天
  const TimelineList = ref<
    Array<{
      time: string;
      name: string;
      content: string;
      type: 1 | 2 | 3;
    }>
  >([]);

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

  const isDraggingCanvas = ref<boolean>(false); // 画布拖动状态标志
  let dragStartX = 0,
    dragStartY = 0; // 拖动开始时的鼠标坐标
  const points = ref<Point[]>([]);
  const scale = ref<number>(1);
  const offsetX = ref(0); // X 轴偏移量（用于模拟视图平移）
  const offsetY = ref(0); // Y 轴偏移量
  const isDragging = ref<boolean>(false);
  const canvasRef1 = ref<HTMLCanvasElement | null>(null) as any;
  const ctx = ref<CanvasRenderingContext2D | null>(null) as any;
  let currentPersonId = ref<number>(1); // 当前人员ID
  let currentIdCard = ref<string>(''); // 当前身份证ID
  const siteMapInfo = ref<SiteMapInfo | null>(null); // 底图信息

  const backgroundColor = ref<string>('#ffffff'); // 默认背景色

  // 新增：缓存背景图
  let backgroundImage: HTMLImageElement | null = null;

  const [trajectoryModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false });
    // 获取传入的人员ID和身份证ID
    if (data && data.personId) {
      currentPersonId.value = data.personId;
    }
    if (data && data.idCard) {
      currentIdCard.value = data.idCard;
    }

    if (canvasRef1.value) {
      ctx.value = canvasRef1.value.getContext('2d');
      enableDragCanvas(); // 启用画布的拖动功能
    }

    await loadSiteMapAndImage(); // 加载地图并缓存图像

    await loadTrajectoryData(); // 加载轨迹数据

    console.log('接收到的记录数据:', data);
  });
  onMounted(() => {
    if (canvasRef1.value) {
      ctx.value = canvasRef1.value.getContext('2d');
      enableDragCanvas(); // 启用拖动功能
      //resizeCanvas(); // 初始化 canvas 尺寸
    }

    window.addEventListener('resize', () => {
      resizeCanvas();
      fitToView(); // 窗口变化后重新居中
      redrawAll(); // 窗口变化后重绘
    });

    // 移除这两行，避免页面一加载就执行数据请求
    // loadSiteMapAndImage();
    // loadTrajectoryData();
  });
  function resizeCanvas() {
    const canvas = canvasRef1.value;
    if (!canvas) return;

    const devicePixelRatio = window.devicePixelRatio || 1;

    // 设置 Canvas 实际绘制的分辨率
    canvas.width = window.innerWidth * devicePixelRatio;
    canvas.height = window.innerHeight * devicePixelRatio;

    // 设置 Canvas 显示尺寸
    canvas.style.width = `${window.innerWidth}px`;
    canvas.style.height = `${window.innerHeight}px`;

    // 应用缩放以匹配实际绘制分辨率
    const ctx = canvas.getContext('2d');
    if (ctx) ctx.scale(devicePixelRatio, devicePixelRatio);
  }
  // 加载底图并缓存图片
  async function loadSiteMapAndImage() {
    try {
      const response = await getEnabledSiteMap();
      if (response.success && response.data) {
        siteMapInfo.value = response.data;

        let imageUrl = '';
        try {
          const filePathData = JSON.parse(siteMapInfo.value.filePath);
          imageUrl = processFileUrl(filePathData.previewUrl ?? siteMapInfo.value.url);
        } catch (e) {
          imageUrl = processFileUrl(siteMapInfo.value.url);
        }

        if (!backgroundImage || backgroundImage.src !== imageUrl) {
          backgroundImage = new Image();
          backgroundImage.src = imageUrl;

          backgroundImage.onload = async () => {
            resizeCanvas(); // 确保图片加载后重新设置画布尺寸
            fitToView(); // 图片加载完成后调用居中方法
            redrawAll(); // 图片加载完成后触发首次绘制
            // 图片加载完成后获取主色调并设置背景色
            if (backgroundImage) {
              const dominantColor = await getImagePalette(backgroundImage);
              backgroundColor.value = dominantColor;
            }
          };
        }
      } else {
        message.error('获取底图失败：' + response.message);
      }
    } catch (error) {
      console.error('加载底图失败:', error);
      message.error('加载底图失败');
    }
  }

  // 获取图片主色调
  async function getImagePalette(img: HTMLImageElement): Promise<string> {
    return new Promise((resolve) => {
      const canvas = document.createElement('canvas');
      const ctx = canvas.getContext('2d');
      if (!ctx) {
        resolve('#ffffff');
        return;
      }

      canvas.width = img.width;
      canvas.height = img.height;
      ctx.drawImage(img, 0, 0);

      const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
      const data = imageData.data;
      const colorCount: Record<string, number> = {};

      for (let i = 0; i < data.length; i += 4) {
        const r = data[i];
        const g = data[i + 1];
        const b = data[i + 2];
        const a = data[i + 3];

        if (a < 128) continue; // 忽略半透明或透明像素

        const color = `rgb(${r},${g},${b})`;
        colorCount[color] = (colorCount[color] || 0) + 1;
      }

      let dominantColor = '#ffffff';
      let maxCount = 0;
      for (const color in colorCount) {
        if (colorCount[color] > maxCount) {
          maxCount = colorCount[color];
          dominantColor = color;
        }
      }

      resolve(dominantColor);
    });
  }

  // 新增 fitToView 函数，用于调整图像显示以适应画布大小并居中
  function fitToView() {
    const canvas = canvasRef1.value;
    if (!canvas || !backgroundImage?.complete || !backgroundImage.naturalWidth) return;

    const imgWidth = backgroundImage.naturalWidth;
    const imgHeight = backgroundImage.naturalHeight;

    const canvasWidth = canvas.width;
    const canvasHeight = canvas.height;

    // 计算缩放比例以完全覆盖画布
    const scaleX = canvasWidth / imgWidth;
    const scaleY = canvasHeight / imgHeight;
    scale.value = Math.max(scaleX, scaleY);

    // 居中图像
    offsetX.value = (canvasWidth - imgWidth * scale.value) / 2;
    offsetY.value = (canvasHeight - imgHeight * scale.value) / 2;

    redrawAll();
  }
  // 加载轨迹数据
  async function loadTrajectoryData() {
    try {
      const params = {
        personId: currentPersonId.value,
        idCard: currentIdCard.value,
        startDate: selectedDate.value?.format('YYYY-MM-DD'),
        endDate: selectedDate.value?.format('YYYY-MM-DD'),
        startTime: timeRange.value[0],
        endTime: timeRange.value[1],
      };

      const areaFenceParams = {
        idCard: currentIdCard.value,
        startDate: selectedDate.value?.format('YYYY-MM-DD'),
        endDate: selectedDate.value?.format('YYYY-MM-DD'),
        startTime: timeRange.value[0],
        endTime: timeRange.value[1],
      };

      console.log('查询区域围栏数据参数:', areaFenceParams);

      // 并行获取轨迹数据、区域围栏数据和人员信息
      const [trajectoryResponse, areaFenceResponse, personResponse] = await Promise.all([
        getPersonTrajectory(params),
        getAreaFenceDataByIdCard(areaFenceParams),
        getPersonByIdCard(currentIdCard.value),
      ]);

      console.log('区域围栏数据响应:', areaFenceResponse);

      if (trajectoryResponse.success) {
        // 更新轨迹点数据（保持原有逻辑）
        points.value = trajectoryResponse.data.trajectoryPoints.map((item: PersonPosition) => ({
          x: item.x,
          y: item.y,
          id: item.id,
          name: item.name,
          workType: item.workType,
          organization: item.organization,
          workShop: item.workShop,
          teamGroup: item.teamGroup,
          workHours: item.workHours,
          attendanceStatus: item.attendanceStatus || item.attendance || '未知', // 兼容两种字段名，提供默认值
          idCard: item.idCard || item.identityCard || '', // 修复：优先使用idCard，如果不存在则使用identityCard，提供默认值
        }));

        // 使用区域围栏数据更新时间线事件数据
        if (
          areaFenceResponse.success &&
          areaFenceResponse.data &&
          areaFenceResponse.data.length > 0
        ) {
          // 获取人员姓名
          let personName = '未知人员';
          if (personResponse.success && personResponse.data) {
            personName = personResponse.data.name;
          }

          // 将区域围栏数据转换为时间线事件数据
          TimelineList.value = areaFenceResponse.data.map((item) => {
            // 根据区域名称智能判断事件类型
            let eventType: 1 | 2 | 3 = 1; // 默认正常
            const areaName = item.area_name.toLowerCase();

            // 如果区域名称包含危险、警告等关键词，设置为警告类型
            if (
              areaName.includes('危险') ||
              areaName.includes('禁止') ||
              areaName.includes('警告')
            ) {
              eventType = 3; // 警告
            } else if (areaName.includes('报警') || areaName.includes('紧急')) {
              eventType = 2; // 报警
            }

            return {
              time: formatAreaFenceTime(item.time),
              name: personName,
              content: `进入 ${item.area_name}`,
              type: eventType,
            };
          });

          console.log('区域围栏数据转换完成，时间线事件数量:', TimelineList.value.length);
        } else {
          // 如果没有区域围栏数据，显示空的时间线
          TimelineList.value = [];
          console.log('未找到区域围栏数据，时间线为空');

          // 输出调试信息
          if (areaFenceResponse.success) {
            console.log('区域围栏查询成功，但数据为空:', areaFenceResponse);
          } else {
            console.log('区域围栏查询失败:', areaFenceResponse.message);
          }
        }

        redrawAll(); // 数据变化后重新绘制
      } else {
        message.error('加载轨迹数据失败：' + trajectoryResponse.message);
      }
    } catch (error) {
      console.error('加载轨迹数据失败:', error);
      message.error('加载轨迹数据失败');
    }
  }

  async function handleSubmit() {
    closeModal();
    emit('success');
  }

  const disabledDate = (current: Dayjs) => {
    // 禁用未来的日期，允许选择今天及以前的日期
    return current && current > dayjs().endOf('day');
  };
  // 画背景图
  function drawBackground() {
    if (!ctx.value || !backgroundImage) return;

    const canvas = canvasRef1.value;
    if (!canvas) return;

    const canvasWidth = canvas.width;
    const canvasHeight = canvas.height;
    const imgWidth = backgroundImage.naturalWidth;
    const imgHeight = backgroundImage.naturalHeight;

    // 计算图片居中时的偏移量
    const offsetX = (canvasWidth - imgWidth) / 2;
    const offsetY = (canvasHeight - imgHeight) / 2;

    // 在居中位置绘制背景图
    ctx.value.drawImage(backgroundImage, offsetX, offsetY);

    if (points.value.length > 0) {
      // 创建线性渐变对象
      const gradient = ctx.value.createLinearGradient(
        points.value[0].x + offsetX,
        points.value[0].y + offsetY,
        points.value[points.value.length - 1].x + offsetX,
        points.value[points.value.length - 1].y + offsetY,
      );
      // 添加颜色渐变点
      gradient.addColorStop(0, 'blue'); // 起点颜色
      gradient.addColorStop(0.5, 'cyan'); // 中间颜色
      gradient.addColorStop(1, 'lime'); // 终点颜色

      // 开始绘制路径线
      ctx.value.beginPath();
      // 移动到第一个点
      ctx.value.moveTo(points.value[0].x + offsetX, points.value[0].y + offsetY);
      // 连接所有点，但不回到起点
      points.value.forEach((item) => {
        ctx.value.lineTo(item.x + offsetX, item.y + offsetY);
      });
      // 设置线条样式为渐变
      ctx.value.strokeStyle = gradient;
      ctx.value.lineWidth = 4 / scale.value; // 根据缩放调整线宽
      ctx.value.stroke();
      // 绘制所有点
      points.value.forEach((point) => {
        //drawPoint(point.x + offsetX, point.y + offsetY);
      });
    }
  }
  // 画点
  function drawPoint(x: number, y: number): void {
    const pointRadius = 2;
    if (!ctx.value) return;
    ctx.value.fillStyle = 'red';
    ctx.value.beginPath();
    ctx.value.arc(x, y, pointRadius / scale.value, 0, Math.PI * 2); // 根据缩放调整点大小
    ctx.value.fill();
  }
  // 重新绘制所有内容
  function redrawAll() {
    const canvas = canvasRef1.value;
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
    drawBackground(); // 已包含底图和轨迹线、点绘制
    // 恢复状态
    ctx.restore();
  }
  // 当需要改变缩放比例时，调用此函数
  function changeScale(newScale) {
    scale.value = newScale;
    redrawAll();
  }

  function handleWheel(event: WheelEvent) {
    event.preventDefault();

    const canvas = canvasRef1.value;
    if (!canvas) return;

    const rect = canvas.getBoundingClientRect();
    // 计算鼠标在 canvas 内的坐标
    const mouseX = event.clientX - rect.left;
    const mouseY = event.clientY - rect.top;

    const zoomFactor = 1.1;
    // 记录缩放前的缩放值
    const oldScale = scale.value;

    // 根据滚轮方向改变缩放值
    if (event.deltaY < 0) {
      // 向上滚动，放大
      scale.value = Math.min(20, scale.value * zoomFactor);
    } else {
      // 向下滚动，缩小
      scale.value = Math.max(0.1, scale.value / zoomFactor);
    }

    // 如果缩放值没有变化，直接返回
    if (scale.value === oldScale) {
      return;
    }

    // 计算缩放前鼠标在画布坐标系中的位置
    const preScaleX = (mouseX - offsetX.value) / oldScale;
    const preScaleY = (mouseY - offsetY.value) / oldScale;

    // 重新计算偏移量，保证缩放中心为鼠标位置
    offsetX.value = mouseX - preScaleX * scale.value;
    offsetY.value = mouseY - preScaleY * scale.value;

    // 修正偏移量计算，考虑缩放后的变化
    const scaleDiffX = preScaleX * scale.value - preScaleX * oldScale;
    const scaleDiffY = preScaleY * scale.value - preScaleY * oldScale;
    offsetX.value -= scaleDiffX;
    offsetY.value -= scaleDiffY;

    redrawAll();
  }
  // 拖动功能
  function enableDragCanvas() {
    if (!canvasRef1.value) return;
    const canvas = canvasRef1.value;
    // 移除旧的事件监听器，避免重复绑定
    canvas.removeEventListener('mousedown', handleCanvasMouseDown);
    window.removeEventListener('mousemove', handleCanvasMouseMove);
    window.removeEventListener('mouseup', handleCanvasMouseUp);

    // 添加新的事件监听器
    canvas.addEventListener('mousedown', handleCanvasMouseDown);
    window.addEventListener('mousemove', handleCanvasMouseMove);
    window.addEventListener('mouseup', handleCanvasMouseUp);
  }
  // 处理鼠标按下事件（用于拖动画布）
  function handleCanvasMouseDown(event: MouseEvent) {
    if (event.button !== 0) return; // 只允许鼠标左键拖动
    const canvas = canvasRef1.value;
    if (!canvas) return;
    const rect = canvas.getBoundingClientRect();
    // 计算鼠标在 canvas 内的相对坐标
    dragStartX = (event.clientX - rect.left - offsetX.value) / scale.value;
    dragStartY = (event.clientY - rect.top - offsetY.value) / scale.value;
    isDraggingCanvas.value = true;
  }
  // 处理鼠标移动事件（用于拖动画布）
  function handleCanvasMouseMove(event: MouseEvent) {
    if (!isDraggingCanvas.value) return;
    const canvas = canvasRef1.value;
    if (!canvas) return;
    const rect = canvas.getBoundingClientRect();

    // 计算新的偏移量，考虑缩放比例
    offsetX.value = event.clientX - rect.left - dragStartX * scale.value;
    offsetY.value = event.clientY - rect.top - dragStartY * scale.value;

    redrawAll();
  }
  // 处理鼠标释放事件（用于停止拖动画布）
  function handleCanvasMouseUp() {
    // setTimeout(() => {
    //   isDraggingCanvas.value = false;
    //   isDragging.value = false; // 设置拖拽结束
    // }, 100);

    // 直接更新状态，移除 setTimeout
    isDraggingCanvas.value = false;
    isDragging.value = false; // 设置拖拽结束
  }

  const formatTime = (seconds: number) => {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
  };

  // 格式化滑动条的提示时间
  const formatTimeTooltip = (value: number | undefined) => {
    if (value === undefined) return '';
    return formatTime(value);
  };

  // 格式化区域围栏时间数据
  const formatAreaFenceTime = (timeStr: string) => {
    // 数据库中的时间本身就是北京时间，接口返回时已经是正确的时间
    // 只需要格式化显示即可，不需要时区转换
    if (!timeStr) return timeStr;

    try {
      // 处理不同的时间格式
      let dateObj: Date;

      if (timeStr.includes('T')) {
        // 格式：2025-06-13T10:27:27
        // 直接解析为本地时间，不添加Z（避免被当作UTC时间）
        dateObj = new Date(timeStr);
      } else {
        // 其他格式，直接解析
        dateObj = new Date(timeStr);
      }

      // 检查日期是否有效
      if (isNaN(dateObj.getTime())) {
        console.error('无效的日期:', timeStr);
        return timeStr;
      }

      // 直接格式化，不进行时区转换
      const year = dateObj.getFullYear();
      const month = (dateObj.getMonth() + 1).toString().padStart(2, '0');
      const day = dateObj.getDate().toString().padStart(2, '0');
      const hours = dateObj.getHours().toString().padStart(2, '0');
      const minutes = dateObj.getMinutes().toString().padStart(2, '0');
      const seconds = dateObj.getSeconds().toString().padStart(2, '0');

      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    } catch (error) {
      console.error('时间格式化失败:', error, '原始时间:', timeStr);
      return timeStr; // 如果转换失败，返回原始时间
    }
  };
  // 处理滑动条放手后的事件
  const onTimeRangeChange = async (value: [number, number]) => {
    console.log('时间范围改变:', formatTime(value[0]), '-', formatTime(value[1]));
    // 重新加载轨迹数据
    await loadTrajectoryData();
  };
  // 监听日期变化
  watch(selectedDate, async () => {
    if (selectedDate.value) {
      await loadTrajectoryData();
    }
  });
</script>
<style lang="less" scoped>
  .rotundity-content {
    overflow: hidden;
    height: 100%;

    .canvas-container {
      position: relative;
      // overflow: hidden;
      height: 100%;
      overflow: hidden;

      .canvas-item {
        height: 100vh;

        /* 确保 canvas 元素在最上层 */
        z-index: 0;
      }

      .left-content {
        background-color: #fff;
        position: absolute;
        z-index: 1;
        top: 20px;
        left: 20px;
        padding: 15px;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        min-width: 250px;

        .slect-time {
          font-size: 16px;
          line-height: 40px;
          font-weight: 500;
          color: #333;
          margin-bottom: 8px;
        }

        .time-box {
          margin-top: 20px;

          .rotundity-blue {
            width: 18px;
            height: 18px;
            background-color: #169bd5;
            border-radius: 50%;
          }

          .rotundity-red {
            width: 18px;
            height: 18px;
            background-color: red;
            border-radius: 50%;
          }

          .rotundity-yellow {
            width: 18px;
            height: 18px;
            background-color: yellow;
            border-radius: 50%;
          }

          .time {
            height: 26px;
            color: #cacaca;
            font-size: 13px;
            display: flex;
            align-items: center;
            margin-left: 10px;
          }

          .content {
            font-weight: 500;
            margin-left: 10px;
            font-size: 12px;
          }
        }
      }

      .right-content {
        margin-right: 50px;
        height: 100%;
        position: absolute;
        right: -10px;
        top: 0;
        transform: translateX(-50%); // 微调水平居中

        left: 50%; // 水平居中
        bottom: 20px; // 距离底部的距离
        z-index: 1;

        .demo-container {
          position: absolute;
          width: 100%;
          bottom: 0px;
          // top: 10px;
          height: 20%;
          right: 40px;
        }
      }
    }
  }
</style>
