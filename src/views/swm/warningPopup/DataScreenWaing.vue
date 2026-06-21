<template>
  <div>
    <div v-if="isShowModal || isShowvideoBox" class="mask"></div>
    <div class="newDataScreenWaningBox" v-if="isShowModal">
      <div class="topTitle">
        <div></div>
        <div class="tile">{{ wangingDataList.obj.alarmName }}</div>
        <div class="icon" @click="isShowModal = false">
          <CloseOutlined />
        </div>
      </div>
      <div class="imgBox">
        <img class="img" :src="wangingDataList.obj.img" alt="" />
      </div>
      <div class="msg">
        工人{{ wangingDataList.obj.personName }}，{{ wangingDataList.obj.triggerReason }}
      </div>
      <div class="bottomBtn">
        <div class="btn1" @click="isShowvideoBox = true">查看监控</div>
        <div class="btn" @click="funConfirmWarning(wangingDataList.obj.id)"> 确定 </div>
      </div>
    </div>
    <div class="videoBox" v-if="isShowvideoBox">
      <div class="topTitle">
        <div></div>
        <div class="tile">现场监控</div>
        <div class="icon" @click="isShowvideoBox = false">
          <CloseOutlined />
        </div>
      </div>
      <div class="videoItemBox">
        <div v-for="(item, index) in videoList.data" :key="item.id" class="videoWrapper">
          <video ref="videoRefs" class="videoPlayer" controls></video>
        </div>
        <div style="width: 45%; height: 10px"></div>
        <div style="width: 45%; height: 10px"></div>
        <div style="width: 45%; height: 10px"></div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { CloseOutlined } from '@ant-design/icons-vue';
  import { ref, onMounted, reactive, watch, nextTick } from 'vue';
  import { getPopupWarnings, confirmWarning } from '@/api/swm/warning';
  import { getOfficeDeviceList } from '@/api/swm/newDataScree';
  import Img1 from '@/assets/swm/sosWanging1.png';
  import Img2 from '@/assets/swm/sosWanging2.png';
  import Img3 from '@/assets/swm/sosWanging3.png';
  import Img4 from '@/assets/swm/sosWanging4.png';
  import Img5 from '@/assets/swm/sosWanging5.png';
  import { useMessage } from '@/hooks/swm/useMessage';
  import Hls from 'hls.js';

  let videoList = reactive({
    data: [
      {
        isNewRecord: false,
        id: '1937406578888474624',
        createBy: 'yaoshengyuan',
        updateDate: '2025-06-24 15:04',
        updateBy: 'yaoshengyuan',
        status: '0',
        createDate: '2025-06-24 15:04',
        recType: '0',
        parentId: '1935523556185722880',
        treeLeaf: '1',
        parentName: '武汉厂',
        name: '武汉厂成品堆场',
        code: 'BlCO5ZxdMN9JEjVLmROUp8cW02dK4R8UV2N0q00cHLDrTJNa732dmIqisaBINtyzT0vklTFELCUmsej3XudSXKdqHG9ixuMQ9zyKhCbx7xdBw/juaj7gkVY7tnFlG+rRIc91WuoVhYIs7SzMG4X1hHtS/mhNG0DCxn3j9Z/ClEn/HXIv1bcxh7iWDUf2e6nAg0pm9jeX1WZO3ft5rWckvzez9hsLHB0frEdTlWQN2RxjCJmLx1rEqIkErcC2ZAz/G95Ab1cNFeOPO8gC1aNIFittomHz5gVLZ9knLYAnZjeZKWlM3/muXHNqEUArfLziivrFes7q4fISD6c9lHWr+w==',
        deviceType: 'MDP_01',
        loadSource: '/hls/000175/index.m3u8',
        streamUrl: 'rtsp://null:null@null:nullnull?channel=null&subtype=null',
        cameraIndexCode: 'd267def6c028442a87ac2bf3c55c78fd',
      },
    ],
  });
  const getVideo = () => {
    getOfficeDeviceList()
      .then((result) => {
        console.log(result.list);
        if (result?.list?.length > 0) {
          videoList.data = result.list;
        }
      })
      .catch((err) => {
        videoList.data = [];
      });
  };
  getVideo();

  const { createMessage } = useMessage();
  let isShowvideoBox = ref(false);
  let isShowModal = ref(false);
  let wangingDataList = reactive({
    obj: {},
    dataList: [],
  });
  let waingType = reactive([
    // { type: '一键SOS', img: Img1 },
    { type: '应急呼叫', img: Img1 },
    { type: '脱帽报警', img: Img3 },
    // { type: '静默报警', img: Img5 },
    { type: '长时间静止报警', img: Img5 },
    { type: '危险区域闯入提示', img: Img4 },
    { type: '跌落报警', img: Img2 },
    // { type: '近电报警', img: Img4 },
  ]);
  const getMsg = () => {
    getPopupWarnings()
      .then((result) => {
        if (result?.data?.confirmList?.length > 0) {
          wangingDataList.dataList = result.data.confirmList;
          wangingDataList.obj = result.data.confirmList[0];
          let index = waingType.findIndex((ele) => {
            return ele.type == wangingDataList.obj.alarmName;
          });
          if (index != -1) {
            wangingDataList.obj.img = waingType[index].img;
            isShowModal.value = true;
          }
        }
      })
      .catch((err) => {});
  };
  // 用于保存每个 video DOM 节点的引用
  const videoRefs = ref<HTMLVideoElement | null>([]);
  // 进入全屏
  const toggleFullscreen = (item) => {
    const video = document.querySelector(`.videoPlayer`);
    if (!document.fullscreenElement) {
      video.requestFullscreen().catch((err) => {
        alert(`进入全屏失败: ${err.message}`);
      });
    } else {
      document.exitFullscreen();
    }
  };

  onMounted(() => {
    videoList.data.forEach((item, index) => {
      const video = videoRefs.value[index];
      console.log(video);

      if (video && item.loadSource) {
        if (Hls.isSupported()) {
          const hls = new Hls();
          const finalUrl = `${hlsUrl}${item.loadSource}?token=${item.code}`;
          console.log(finalUrl);

          hls.loadSource(finalUrl);
          hls.attachMedia(video);
          hls.on(Hls.Events.MANIFEST_PARSED, () => video.play());
        } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
          // Safari 原生支持 HLS
          video.src = item.loadSource;
          video.addEventListener('loadedmetadata', () => video.play());
        }
      }
    });
    setInterval(() => {
      getMsg();
    }, 5000);
  });
  const hlsUrl = 'http://183.63.182.172:9301';
  // 监听 mapObj.url 的变化
  watch(
    () => isShowvideoBox.value,
    (newUrl) => {
      console.log(newUrl);

      nextTick(() => {
        videoList.data.forEach((item, index) => {
          const video = videoRefs.value[index];
          console.log(video);

          if (video && item.loadSource) {
            if (Hls.isSupported()) {
              const hls = new Hls();
              const finalUrl = `${hlsUrl}${item.loadSource}?token=${item.code}`;
              console.log(finalUrl);

              hls.loadSource(finalUrl);
              hls.attachMedia(video);
              hls.on(Hls.Events.MANIFEST_PARSED, () => video.play());
            } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
              // Safari 原生支持 HLS
              video.src = item.loadSource;
              video.addEventListener('loadedmetadata', () => video.play());
            }
          }
        });
      }); // 确保 DOM 已更新
    },
  );

  // 确认告警方法
  const funConfirmWarning = (id) => {
    confirmWarning(id)
      .then((result) => {
        createMessage.success(result.data);

        // 🧠 处理数据更新逻辑
        const indexToRemove = wangingDataList.dataList.findIndex((item) => item.id === id);
        if (indexToRemove !== -1) {
          // 移除该条数据
          wangingDataList.dataList.splice(indexToRemove, 1);

          // 判断是否还有剩余数据
          if (wangingDataList.dataList.length > 0) {
            // 还有数据，更新当前对象为第一个
            wangingDataList.obj = wangingDataList.dataList[0];

            // 更新图片
            const imgIndex = waingType.findIndex(
              (ele) => ele.type === wangingDataList.obj.alarmName,
            );
            if (imgIndex !== -1) {
              wangingDataList.obj.img = waingType[imgIndex].img;
            }
          } else {
            // 没有数据了，关闭弹窗
            isShowModal.value = false;
          }
        }
      })
      .catch((err) => {
        createMessage.error(err.message);
      });
  };
</script>

<style lang="less" scoped>
  .mask {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    z-index: 9998;
  }

  .newDataScreenWaningBox {
    position: fixed;
    width: 1023px;
    height: 549px;
    overflow: hidden;

    z-index: 9999;
    background: url('@/assets/swm/waingModalBgRed.png') no-repeat left top / 100% 100%;
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);

    .topTitle {
      margin-top: 26px;
      height: 32px;
      font-family: Noto Sans SC, Noto Sans SC;
      font-weight: bold;
      font-size: 22px;
      color: #ffffff;
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

    .imgBox {
      width: 200px;
      height: 200px;
      overflow: hidden;
      margin: 0 auto;
      margin-top: 69px;

      .img {
        width: 100%;
        height: 100%;
      }
    }

    .msg {
      width: 90%;
      height: 58px;
      font-family: Noto Sans SC, Noto Sans SC;
      font-weight: bold;
      font-size: 20px;
      color: #ffffff;
      letter-spacing: 1px;
      text-align: left;
      font-style: normal;
      text-transform: none;
      text-align: center;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      margin-top: 35px;
    }

    .bottomBtn {
      display: flex;
      align-items: center;
      justify-content: space-around;

      .btn1 {
        width: 347px;
        height: 40px;
        line-height: 40px;
        background: #2a50ec;
        border-radius: 7px 7px 7px 7px;
        margin: 0 auto;
        text-align: center;
        font-family: Noto Sans SC, Noto Sans SC;
        font-weight: bold;
        font-size: 20px;
        color: #ffffff;
        letter-spacing: 1px;
        font-style: normal;
        text-transform: none;
        margin-top: 51px;
        cursor: pointer;
        user-select: none;
      }

      .btn {
        width: 347px;
        height: 40px;
        line-height: 40px;
        background: linear-gradient(180deg, #c21919 0%, #67210f 100%);
        border-radius: 7px 7px 7px 7px;
        margin: 0 auto;
        text-align: center;
        font-family: Noto Sans SC, Noto Sans SC;
        font-weight: bold;
        font-size: 20px;
        color: #ffffff;
        letter-spacing: 1px;
        font-style: normal;
        text-transform: none;
        margin-top: 51px;
        cursor: pointer;
        user-select: none;
      }
    }
  }

  .videoBox {
    position: fixed;
    width: 80%;
    height: 80%;
    overflow: hidden;

    z-index: 10000;
    background: url('@/assets/swm/rectangle.png') no-repeat left top / 100% 100%;
    border-radius: 15px;
    background-color: #000000;
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);

    .topTitle {
      margin-top: 26px;
      height: 32px;
      font-family: Noto Sans SC, Noto Sans SC;
      font-weight: bold;
      font-size: 22px;
      color: #ffffff;
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

    .videoItemBox {
      width: 100%;
      height: calc(100% - 58px);
      display: flex;
      align-items: flex-start;
      justify-content: space-around;
      flex-wrap: wrap;
      overflow-y: scroll;
      scrollbar-width: none;

      .videoWrapper {
        position: relative;
        width: 45%;
        min-width: 600px;
        margin-top: 20px;
      }

      .videoPlayer {
        width: 100%;
        height: auto;
        background-color: #111;
        border-radius: 4px;
      }
    }
  }
</style>
