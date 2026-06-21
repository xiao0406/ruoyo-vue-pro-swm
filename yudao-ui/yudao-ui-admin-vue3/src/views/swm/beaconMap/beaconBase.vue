<template>
  <div class="personTrack">
    <Spin
      :spinning="spinning"
      tip="加载中..."
      size="large"
      style="position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%); z-index: 1000"
      fullscreen
    />
    <div class="canvas-container">
      <div class="select-box">
        <CheckboxGroup
          v-model:value="checkedList"
          :options="plainOptions"
          @change="checkedListOnChange"
        />
        <div class="topBtn">
          <Button class="btn" type="primary" @click="funGetAllAreaList" size="small">刷新</Button>
          <Button class="btn" type="primary" @click="showAddRegionIcon" size="small"
            >新增信标</Button
          >
          <Button class="btn" type="primary" @click="showAddRegionType(false)" size="small"
            >新增区域</Button
          >
          <Button class="btn" type="text" @click="closeAreaType" size="small">
            <LeftOutlined />返回
          </Button>
        </div>
      </div>
      <div style="width: calc(100% - 320px); height: 100%; background-color: #000000">
        <Map
          :mapImageUrl="img.src"
          v-if="img?.src?.length > 0"
          :userPositions="userPositions.data"
          :activeUser="activeUser.obj"
          @dot-click="handleDotClick"
          :userType="'按车间展示'"
          :checkedList="checkedList"
          @get-click-xy="getClickXy"
          :highlightDots="
            userPositions.selectType?.child?.length > 0 ? userPositions.selectType?.child : []
          "
        />
      </div>
      <div class="right-select">
        <div class="search-container" v-if="shoeRightBox == 'regionType'">
          <div
            v-for="(item, index) in userPositions.typeList"
            class="pathItems"
            :class="userPositions.selectType.id == item.id ? 'activeType' : ''"
            :key="item.id"
          >
            <div class="xuhaoIndex">
              <B>{{ index + 1 }}</B>
            </div>
            <div class="itemBox">
              <div class="title"> 区域名称: </div>
              <div class="value">
                {{ item.areaName }}
              </div>
            </div>
            <!-- <div class="itemBox">
              <div class="title"> 语音: </div>
              <div class="value">
                {{ item.voicePrompt || '无' }}
              </div>
            </div> -->
            <div class="itemBox">
              <div class="title"> 大屏是否展示: </div>
              <div class="value">
                {{ item.isScreenShow ? '是' : '否' }}
              </div>
            </div>
            <div class="itemBox">
              <div class="title"> 颜色: </div>
              <div class="value">
                <div
                  v-if="item?.areaColor?.length > 0"
                  class="colorBtn"
                  :style="`background-color: ${item.areaColor};`"
                >
                </div>
                <div v-else>无</div>
              </div>
            </div>
            <div class="itemBox">
              <div class="title"> 信标列表: </div>
              <div class="value"> </div>
            </div>
            <div class="itemBoxTag">
              <div v-for="ele in item.child" class="userTag" :key="ele.id">
                {{ ele.pixelX }},{{ ele.pixelY }}
              </div>
              <div style="width: 30%"></div>
              <div style="width: 30%"></div>
            </div>
            <div class="itemBoxBtn">
              <Button class="btn" type="text" size="small" @click="showType(item)">展示区域</Button>
              <Button class="btn" type="primary" size="small" @click="showAddRegionType(true, item)"
                >编辑</Button
              >
              <Button class="btn" type="primary" size="small" danger @click="deleteType(item)"
                >删除</Button
              >
            </div>
          </div>
        </div>
        <div class="search-container" v-if="shoeRightBox == 'addRegionIcon'">
          <BeaconForm
            :beacon-data="activeUser.obj"
            :is-edit-mode="isEditMode"
            :submit-btn-text="activeUser.obj.submitBtnText"
            @submit="exitAddIconForm"
            @delete="exitAddIconForm"
            @exit="exitAddIconForm"
            @changexy="changexy"
            @changeFormStateValue="changeactiveUserValue"
          />
        </div>
        <div class="search-container" v-if="shoeRightBox == 'addRegionType'">
          <AreaForm
            ref="areaFormRef"
            :initial-data="activeUser.formState"
            @changeFormStateValue="changeFormStateValue"
            @delete-icon="deleteIcon"
            @close="closeAreaType"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted, reactive, onUnmounted } from 'vue';
  import { Modal, message, CheckboxGroup, Button, Spin } from 'ant-design-vue';
  import Map from './Maps.vue';
  import { LeftOutlined } from '@ant-design/icons-vue';

  import { getEnabledSiteMap } from '@/api/swm/personTrack';
  import { getAllAreaList, deleteAreaWithBeacons } from '@/api/swm/area';
  import { getAllBeaconList } from '@/api/swm/beacon';
  import { processFileUrl } from '@/utils/file/fileUrlUtils';
  import BeaconForm from './BeaconForm.vue';
  import AreaForm from './AreaForm.vue';
  import { refreshAllCache } from '@/api/swm/cache';

  let spinning = ref(false);
  let shoeRightBox = ref('regionType'); //regionType展示区域列表 ， addRegionIcon, 新增或信标表单 ,addRegionType 新增或者删除区域
  let activeUser: any = reactive({
    obj: {},
    formState: {
      id: '',
      areaName: '',
      beaconColor: '',
      voicePrompt: '',
      areaType: '',
      filePath: '',
      beaconList: [],
      isEdit: true, //true 编辑， false, 新增
      isScreenShow: '',
    },
    initObj: {
      id: '',
      areaName: '',
      beaconColor: '',
      voicePrompt: '',
      areaType: '',
      filePath: '',
      beaconList: [],
      isEdit: false,
    },
  });
  const plainOptions = [
    { label: '常规信标', value: '常规信标' },
    { label: '电子围栏', value: '电子围栏' },
    { label: '风险信标', value: '风险信标' },
    // { label: '摄像头', value: '摄像头' },
    { label: '考勤信标', value: '考勤信标' },
    { label: '生产区域提示', value: '生产区域提示' },
  ];
  // let checkedList = ref(['常规信标', '电子围栏', '风险信标', '摄像头', '考勤信标', '生产区域提示']);
  let checkedList = ref(['常规信标', '电子围栏', '风险信标', '考勤信标', '生产区域提示']);
  let isShowUserPathModal = ref(false);
  const checkedListOnChange = (e) => {
    checkedList.value = e;
  };

  const changeFormStateValue = (key, value) => {
    activeUser.formState[key] = value;
  };
  const changeactiveUserValue = (key, value) => {
    activeUser.obj[key] = value;
  };
  const handleDotClick = (shape) => {
    if (shoeRightBox.value == 'addRegionType') {
      if (shape.area?.length > 0 && shape.area != activeUser.formState.id) {
        message.warning('该信标已经被选过');
        return;
      }
      let index = activeUser.formState?.beaconList?.findIndex((ele) => {
        return ele.id == shape.id;
      });

      if (index != -1) {
        message.warning('已添加此信标');
      } else {
        activeUser.formState.beaconList.push(JSON.parse(JSON.stringify(shape)));
        activeUser.formState.child = JSON.parse(JSON.stringify(activeUser.formState?.beaconList));
        userPositions.selectType = activeUser.formState;
      }
      return;
    }
    if (shoeRightBox.value == 'addRegionIcon') return;
    activeUser.obj = JSON.parse(JSON.stringify(shape));
    activeUser.obj.submitBtnText = '保存修改';
    shoeRightBox.value = 'addRegionIcon';
  };
  const deleteIcon = (id) => {
    // 先确保 beaconList 存在
    if (!activeUser.formState?.beaconList) return;

    // 找到要删除的索引
    const index = activeUser.formState.beaconList.findIndex((ele) => ele.id === id);

    // 如果找到了，就直接删除
    if (index !== -1) {
      activeUser.formState.beaconList.splice(index, 1);
    }

    activeUser.formState.child = JSON.parse(JSON.stringify(activeUser.formState?.beaconList));
    userPositions.selectType = activeUser.formState;
  };

  const getClickXy = (x, y) => {
    activeUser.obj.pixelX = x;
    activeUser.obj.pixelY = y;
  };
  const changexy = (x, y, color) => {
    if (shoeRightBox.value == 'addRegionIcon') {
      activeUser.obj.pixelX = x;
      activeUser.obj.pixelY = y;
      activeUser.obj.beaconColor = color;
    }
  };
  let img = new Image();
  // 初始化逻辑
  onMounted(async () => {
    // 加载底图信息
    await loadSiteMap();
    funGetAllAreaList();
  });

  // 加载底图信息
  async function loadSiteMap() {
    try {
      const response = await getEnabledSiteMap();
      if (response.success && response.data) {
        // 从filePath中解析previewUrl
        let imageUrl = '';
        try {
          // 解析filePath JSON字符串
          const filePathData = JSON.parse(response.data.filePath);
          imageUrl = processFileUrl(filePathData.previewUrl || response.data.url);
        } catch (error) {
          console.error('解析filePath失败，使用url字段:', error);
          // 如果解析失败，则使用原来的url字段
          imageUrl = processFileUrl(response.data.url);
        }
        // 设置底图URL并加载
        img.src = imageUrl;
        console.log(img.src);
      } else {
        message.error('获取底图失败：' + response.message);
        // 如果获取失败，可以设置一个默认图片或者显示错误状态
      }
    } catch (error) {
      console.error('加载底图失败:', error);
      message.error('加载底图失败');
    }
  }

  let userPositions: any = reactive({
    data: [] as any,
    selectType: {} as any,
  });
  const funGetAllAreaList = () => {
    console.log('1111');
    spinning.value = true;
    userPositions.selectType = {};
    activeUser.obj = {};
    activeUser.formState = JSON.parse(JSON.stringify(activeUser.initObj));
    getAllBeaconList()
      .then((result) => {
        console.log(result.list, '信标列表');
        userPositions.data = result.list;
        getAllAreaList()
          .then((res) => {
            if (res.list?.length > 0) {
            } else {
              spinning.value = false;
              return;
            }
            res.list = res.list.map((element) => {
              if (element.bids) {
                try {
                  const bidIds = JSON.parse(element.bids);

                  const allChildren = result.list.filter((ele) => ele.area == element.id);
                  const childMap = {};
                  allChildren.forEach((child) => {
                    childMap[child.id] = child;
                  });

                  const orderedChildren = bidIds
                    .map((id) => childMap[id]) // 用对象取值代替 map.get()
                    .filter((child) => child !== undefined); // 过滤无效项

                  element.child = orderedChildren;
                } catch (e) {
                  element.child = [];
                }
              } else {
                element.child = [];
              }

              return element;
            });
            // 确保在处理完所有的元素后更新 userPositions.typeList 和停止 loading 状态
            userPositions.typeList = res.list;
            spinning.value = false;
          })
          .catch((_err: any) => {
            spinning.value = false;
          });
      })
      .catch((_err: any) => {
        spinning.value = false;
      });
  };

  onUnmounted(() => {
    isShowUserPathModal.value = false;

    Modal.destroyAll();
  });
  const showType = (item) => {
    userPositions.selectType = JSON.parse(JSON.stringify(item));
  };

  const deleteType = (item) => {
    Modal.confirm({
      title: '确定要删除该区域吗？',
      content: `删除区域"${item.areaName}"后，该区域下的所有信标关联也将被清空，此操作无法恢复。`,
      okText: '确认删除',
      cancelText: '取消',
      onOk: async () => {
        try {
          const response = await deleteAreaWithBeacons(item.id);
          if (response && response.success) {
            message.success('删除区域成功！');
            // 刷新缓存
            try {
              await refreshAllCache();
            } catch (error) {
              console.error('刷新缓存失败:', error);
            }
            funGetAllAreaList();
          } else {
            message.error('删除区域失败：' + (response?.message || '未知错误'));
          }
        } catch (error) {
          console.error('删除区域失败:', error);
          message.error('删除区域失败，请重试');
        }
      },
    });
  };

  const showAddRegionIcon = () => {
    shoeRightBox.value = 'addRegionIcon';
    userPositions.data.push({
      id: 'testaaa',
      pixelX: 0,
      pixelY: 0,
      beaconColor: '#ff0000', // 添加信标颜色字段，默认红色
      beaconType: '2',
      beaconStatus: '在线',
      beaconTypeText: '电子围栏',
    });
    activeUser.obj = {
      id: 'testaaa',
      pixelX: 0,
      pixelY: 0,
      beaconColor: '#ff0000', // 添加信标颜色字段，默认红色
      beaconType: '2',
      beaconStatus: '在线',
      beaconTypeText: '电子围栏',
    };
    activeUser.formState = JSON.parse(JSON.stringify(activeUser.initObj));
  };
  const showAddRegionType = (isEdit, item: any = {}) => {
    shoeRightBox.value = 'addRegionType';
    activeUser.obj = {};
    activeUser.formState.isEdit = isEdit;
    if (isEdit == true) {
      activeUser.formState = {
        id: item.id,
        areaName: item.areaName,
        beaconColor: item.areaColor,
        voicePrompt: item.voicePrompt,
        areaType: item.areaType,
        filePath: item.filePath,
        beaconList: item.child,
        isEdit: true, //edit 编辑， false, 新增}
        isScreenShow: item.isScreenShow,
      };
      activeUser.formState.child = JSON.parse(JSON.stringify(activeUser.formState?.beaconList));
      userPositions.selectType = activeUser.formState;
    }
  };

  const exitAddIconForm = () => {
    shoeRightBox.value = 'regionType';
    activeUser.obj = {};
    funGetAllAreaList();
    userPositions.selectType = {};
  };
  const closeAreaType = () => {
    shoeRightBox.value = 'regionType';
    activeUser.obj = {};
    funGetAllAreaList();
    userPositions.selectType = {};
    activeUser.formState = JSON.parse(JSON.stringify(activeUser.initObj));
  };
</script>

<style lang="less" scoped>
  .ant-spin-nested-loading {
    height: 100%;

    .ant-spin-container {
      height: 100%;
    }
  }

  // 使加载指示器居中
  .ant-spin {
    max-height: none !important;

    .ant-spin-dot {
      position: fixed;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
    }

    .ant-spin-text {
      position: fixed;
      top: calc(50% + 30px);
      left: 50%;
      transform: translateX(-50%);
    }
  }

  .personTrack {
    height: 100%;
    width: 100%;
    overflow: hidden;

    .canvas-container {
      display: flex;
      align-items: center;
      justify-content: space-between;
      width: 100%;
      height: 100%;
      overflow: hidden;

      .select-box {
        position: absolute;
        z-index: 1;
        top: 14px;
        left: 15px;
        width: calc(100% - 349px);
        height: 40px;
        padding: 10px;

        background-color: #ffffff;
        user-select: none;
        display: flex;
        align-items: center;
        justify-content: space-between;

        .topBtn {
          display: flex;

          .btn {
            margin: 0px 7px;
          }
        }
      }
    }

    .right-select {
      height: 100%;
      padding: 10px;
      width: 320px;
      box-sizing: border-box;
      height: calc(100vh - 100px);
      overflow-y: auto;
      scrollbar-width: none;
      background-color: #ffffff;

      &::-webkit-scrollbar {
        display: none;
      }

      .search-container {
        position: relative;
        margin-bottom: 10px;

        .activeType {
          border: 1px solid #7eafc1 !important;
          box-shadow: 0 0 8px 4px #bee0e7;
        }

        .pathItems {
          width: 100%;
          margin-top: 10px;
          border-radius: 5px;
          border: 1px solid #e6e6e6;
          padding: 8px 10px;
          position: relative;

          .xuhaoIndex {
            position: absolute;
            right: 0;
            top: 0;
            padding: 0px 5px 4px 10px;
            /* background-color: #18cd67; */
            background-color: #2a50ec;
            border-radius: 0px 5px 0px 80%;
            color: #ffffff;
            font-size: 16px;
          }

          .itemBoxTag {
            width: 100%;
            display: flex;
            align-items: center;
            justify-content: space-between;
            flex-wrap: wrap;
            margin-top: 7px;

            .userTag {
              width: 30%;
              font-size: 9px;
              padding: 4px;
              border: 1px solid #e9e9e9;
              border-radius: 5px;
              margin-top: 5px;
              text-align: center;
            }
          }

          .itemBox {
            width: 100%;
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-top: 7px;

            .title {
              width: 100px;
              text-align: left;
            }

            .value {
              width: 200px;

              .colorBtn {
                width: 25px;
                height: 25px;
                border-radius: 5px;
              }
            }
          }

          .itemBoxBtn {
            display: flex;
            align-items: center;
            justify-content: space-around;
            margin-top: 7px;

            .btn {
              margin: 0px 7px;
            }
          }
        }
      }

      .tree-container {
        height: calc(100vh - 200px);
        overflow-y: scroll;
      }
    }
  }
</style>

<style lang="less">
  .ant-modal-confirm .ant-modal-confirm-btns {
    display: flex;
    justify-content: center;
    margin-top: 24px;
    float: none;

    button {
      margin-right: 60px;
    }
  }

  .ant-modal .ant-modal-body > .scrollbar {
    padding: 0 !important;
  }

  .ant-tree {
    background-color: #f6f6f6;
  }
</style>
