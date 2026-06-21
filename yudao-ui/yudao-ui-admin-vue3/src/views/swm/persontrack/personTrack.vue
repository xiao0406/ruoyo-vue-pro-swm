<template>
  <div class="personTrack" :style="{ backgroundColor: backgroundColor }">
    <div class="canvas-container">
      <div class="select-box">
        <Select
          v-model:value="optionName"
          placeholder="Select a person"
          style="width: 200px"
          :options="optionsList"
          @change="handleDisplayTypeChange"
        />
      </div>
      <ShowUserColor :userType="optionName"></ShowUserColor>
      <div style="width: calc(100% - 320px); height: 100%; background-color: #000000">
        <Map
          :mapImageUrl="img.src"
          v-if="img?.src?.length > 0"
          :userPositions="userPoints.data"
          :activeUser="activeUser"
          :userType="optionName"
          @dotClick="handleDotClick"
        ></Map>
      </div>
      <div class="right-select">
        <div class="search-container">
          <a-input-search
            style="width: 300px; margin-bottom: 10px"
            v-model:value="staff"
            placeholder="请输入人员名称"
            enter-button="搜索"
            @search="onSearch"
            @input="onSearchInput"
          />
          <div v-if="searchResults.length > 0" class="search-results">
            <div
              class="search-result-item"
              v-for="person in searchResults"
              :key="person.id"
              @click="selectPerson(person)"
            >
              <div class="person-name">{{ person.name }}</div>
              <div class="person-info">{{ person.department }} - {{ person.team }}</div>
            </div>
          </div>
        </div>
        <div class="tree-container">
          <Tree
            :tree-data="treeData"
            v-model:expanded-keys="expandedKeys"
            v-model:selected-keys="selectedKeys"
            @select="handleTreeSelect"
            :load-data="onLoadData"
          />
        </div>
      </div>
    </div>
    <ViewModal @register="registerModal" @success="handleSuccess" />
    <TrajectoryModal @register="trajectoryModal" @success="handleSuccess" />
    <UserPath
      v-if="isShowUserPathModal"
      :mapObj="dataObj.mapObj"
      :activeUser="activeUser.obj"
      @changeIsShowUserPathModal="changeIsShowUserPathModal"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted, watch, h, reactive, onUnmounted } from 'vue';
  import { Select, Tree, Modal, message } from 'ant-design-vue';
  import ViewModal from '../checkAttendance/viewModal.vue';
  import TrajectoryModal from './trajectoryModal.vue';
  import { useModal } from '@/components/swm/Modal';
  import Map from './Maps.vue';
  import ShowUserColor from './ShowUserColor.vue';
  import UserPath from './UserPath.vue';
  import { getPersonPositions, getEnabledSiteMap } from '@/api/swm/personTrack';
  import { searchPersons } from '@/api/swm/person';
  import { fetchOrgTreeData } from '@/api/swm/organizationTree';
  import { processFileUrl } from '@/utils/file/fileUrlUtils';
  let activeUser = reactive({
    obj: {},
  });
  let isShowUserPathModal = ref(false);
  const changeIsShowUserPathModal = (bel) => {
    isShowUserPathModal.value = bel;
  };
  const handleDotClick = (shape) => {
    console.log(shape);
    activeUser.obj = shape;
    Modal.confirm({
      content: () =>
        h(
          'div',
          {
            style: {
              color: '#000',
            },
          },
          [
            h('span', { style: { display: 'inline-block', width: '45%' } }, `姓名：${shape.name}`),
            h(
              'span',
              { style: { display: 'inline-block', width: '45%' } },
              `工种：${shape.workType}`,
            ),
            h('hr'),
            h(
              'span',
              { style: { display: 'inline-block', width: '45%' } },
              `组织：${shape.organization}`,
            ),
            h('span', `车间：${shape.workShop}`),
            h('hr'),
            h('span', `班组：${shape.teamGroup}`),
            h('hr'),
            h(
              'span',
              { style: { display: 'inline-block', width: '45%' } },
              `进入工作时长：${shape.workHours}`,
            ),
            h('span', `考勤状态：${shape.attendanceStatus}`),
            h('hr'),
            h('span', `身份证号：${shape.idCard}`),
          ],
        ),
      icon: false,
      closable: true, // 添加关闭按钮
      cancelText: '考勤记录',
      okText: '行动轨迹',

      onOk() {
        isShowUserPathModal.value = true;

        // batchtrajectoryModal(true, { personId: shape.id, idCard: shape.idCard })
      },
      onCancel(e) {
        if (e.triggerCancel)
          //点击x或者按键盘esc关闭的弹窗
          return;
        e();
        openModal(true, {
          record: shape,
          isUpdate: true,
        });
      },
    });
  };
  interface Point {
    x: number;
    y: number;
    id: string;
    name: string;
    workType: string;
    organization: string;
    workShop: string;
    teamGroup: string;
    workHours: string;
    attendanceStatus: string;
    idCard: string;

    // 颜色信息字段 2025/06/24 Shawn 添加
    personTypeColor?: string; // 人员类型颜色
    workTypeColor?: string; // 工种颜色
    workerArchiveColor?: string; // 工人档案颜色
    officeCodeColor?: string; // 组织编码颜色
    positionArchiveColor?: string; // 车间颜色
    workGroupColor?: string; // 班组颜色
    prodLineColor?: string; // 产线颜色

    // ID字段 2025/06/24 Shawn 添加
    workerArchiveId?: string; // 工人档案ID
    officeCode?: string; // 组织编码
    positionArchiveId?: string; // 车间ID
    workGroupId?: string; // 班组ID
    prodLineId?: string; // 产线ID
  }

  const optionsList = ref([
    { value: '按车间展示', label: '按车间展示定位颜色' },
    { value: '按班组展示', label: '按班组展示定位颜色' },
    { value: '按人员类型展示', label: '按人员类型展示定位颜色' },
    { value: '按工种展示', label: '按工种展示定位颜色' },
  ]);
  const optionName = ref('按人员类型展示');
  const staff = ref('');
  const searchResults: any = ref([]);
  const treeData = ref<any[]>([]);
  const expandedKeys = ref<string[]>([]);
  const selectedKeys = ref<string[]>([]);
  const points = ref<Point[]>([]);

  let img = new Image();
  const siteMapInfo: any = ref(null); // 底图信息
  const [registerModal, { openModal }] = useModal();
  const [trajectoryModal, { openModal: batchtrajectoryModal }] = useModal();
  const backgroundColor = ref<string>('');
  // 初始化逻辑
  onMounted(async () => {
    // 加载底图信息
    await loadSiteMap();
    // 加载初始数据
    await loadTreeData();
    await loadPersonPositions();
  });
  let dataObj = reactive({
    mapObj: {},
  });
  // 加载底图信息
  async function loadSiteMap() {
    try {
      const response: any = await getEnabledSiteMap();
      if (response.success && response.data) {
        siteMapInfo.value = response.data;

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
        response.data.filePath = JSON.parse(response.data.filePath);
        response.data.filePath.previewUrl = processFileUrl(response.data.filePath.previewUrl);
        dataObj.mapObj = response.data;
        console.log(dataObj.mapObj);
      } else {
        message.error('获取底图失败：' + response.message);
        // 如果获取失败，可以设置一个默认图片或者显示错误状态
      }
    } catch (error) {
      console.error('加载底图失败:', error);
      message.error('加载底图失败');
    }
  }

  // 加载组织树数据
  async function loadTreeData() {
    try {
      // 加载根节点数据
      const result = await fetchOrgTreeData('root');
      if (result && Array.isArray(result)) {
        // 处理树节点数据
        treeData.value = result.map((item) => ({
          ...item,
          isLeaf: item.nodeType === 'workGroup',
          // 动态加载子节点
          children: [],
        }));
      } else {
        message.error('加载组织树数据失败');
      }
    } catch (error) {
      console.error('加载组织树数据失败:', error);
      message.error('加载组织树数据失败');
    }
  }
  // 处理树节点展开事件，动态加载子节点
  async function onLoadData(treeNode: any) {
    return new Promise<void>(async (resolve) => {
      if (treeNode.dataRef.children.length > 0) return resolve();
      const nodeType = treeNode.dataRef.nodeType;
      const nodeId = treeNode.dataRef.id || treeNode.dataRef.value;

      // 根据当前节点类型确定子节点类型
      let childNodeType = '';

      // 更新节点类型映射逻辑
      switch (nodeType) {
        case 'office':
          childNodeType = 'office'; // 公司/部门下级是车间
          break;
        case 'workshop':
          childNodeType = 'workshop'; // 车间下级是产线
          break;
        case 'prodLine':
          childNodeType = 'prodLine'; // 产线下级是班组
          break;
        case 'workGroup':
          childNodeType = 'workGroup'; // 班组下级是员工
          break;
        default:
          childNodeType = 'workshop'; // 默认查询车间
      }

      console.log('当前节点类型:', nodeType, '请求子节点类型:', childNodeType, '父节点ID:', nodeId);

      try {
        // 获取节点类型和ID
        const children = await fetchOrgTreeData(childNodeType, nodeId);
        if (children && Array.isArray(children)) {
          // 更新节点的子节点
          treeNode.dataRef.children = children.map((item: any) => ({
            ...item,
            isLeaf: item.nodeType === 'worker', // 员工节点是叶子节点
            children: [],
          }));
          // 更新树数据，触发重新渲染
          treeData.value = [...treeData.value];
        }
        resolve();
      } catch (error) {
        console.error('加载子节点失败:', error);
        message.error('加载子节点失败');
        resolve();
      }
    });
  }
  // 加载人员位置数据
  const userPoints = reactive({
    data: [] as any,
  });
  async function loadPersonPositions(searchName?: string, organizationKey?: string) {
    try {
      const response = await getPersonPositions({
        searchName,
        organizationKey,
        displayType: optionName.value,
      });

      if (response.success) {
        userPoints.data = response.data;
        // 转换API返回的数据格式为组件需要的格式
        points.value = response.data.map((item) => {
          item.attendanceStatus = item.attendanceStatus || item.attendance || '未知';
          item.idCard = item.idCard || item.identityCard || '';
          return item;
        });
      } else {
        message.error('加载人员位置数据失败：' + response.message);
      }
    } catch (error) {
      console.error('加载人员位置数据失败:', error);
      message.error('加载人员位置数据失败');
    }
  }
  /**
   * 处理展示类型切换事件
   * 当用户选择不同的展示方式（如按车间、班组等）时，
   * 调用 `loadPersonPositions` 方法重新加载人员位置数据。
   */
  function handleDisplayTypeChange() {
    loadPersonPositions();
  }
  /**
   * 处理树节点选择事件
   * 当用户选择组织树节点时，使用节点的key(ID)作为查询条件
   * 调用 `loadPersonPositions` 方法重新加载人员位置数据。
   * @param selectedKeysValue 选中的节点key数组
   * @param e 选择事件对象
   */
  async function handleTreeSelect(selectedKeysValue: string[], e: any) {
    if (selectedKeysValue.length > 0) {
      // 使用节点的key(ID)进行查询
      const organizationKey = selectedKeysValue[0];
      console.log('选中的组织节点Key:', organizationKey);
      await loadPersonPositions(undefined, organizationKey);
    } else {
      await loadPersonPositions();
    }
  }

  // 搜索功能
  async function onSearch() {
    if (staff.value.trim()) {
      await loadPersonPositions(staff.value.trim());
      // 清空搜索结果列表
      searchResults.value = [];
    } else {
      await loadPersonPositions();
    }
  }

  // 输入搜索时实时搜索人员
  async function onSearchInput() {
    const keyword = staff.value.trim();
    if (!keyword) {
      searchResults.value = [];
      return;
    }

    try {
      const response = await searchPersons({
        keyword,
        searchType: 'name',
        pageSize: 20,
      });

      if (response.success && response.list) {
        searchResults.value = response.list;
      } else {
        searchResults.value = [];
      }
    } catch (error) {
      console.error('搜索人员失败:', error);
      searchResults.value = [];
    }
  }

  // 选择搜索结果中的人员
  function selectPerson(person) {
    staff.value = person.name;
    searchResults.value = [];
    // 根据选中的人员搜索位置
    loadPersonPositions(person.name);
  }

  onUnmounted(() => {
    isShowUserPathModal.value = false;

    Modal.destroyAll();
  });
</script>

<style lang="less" scoped>
  .personTrack {
    height: 100%;
    padding: 0px;

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
        top: 20px;
        left: 20px;
      }
    }

    .right-select {
      height: 100%;
      background-color: #f6f6f6;
      padding: 10px;
      width: 320px;
      box-sizing: border-box;

      .search-container {
        position: relative;
        margin-bottom: 10px;

        .search-results {
          position: absolute;
          top: 100%;
          left: 0;
          right: 0;
          background: white;
          border: 1px solid #d9d9d9;
          border-radius: 6px;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
          max-height: 200px;
          overflow-y: auto;
          z-index: 1000;

          .search-result-item {
            padding: 8px 12px;
            cursor: pointer;
            border-bottom: 1px solid #f0f0f0;

            &:hover {
              background-color: #f5f5f5;
            }

            &:last-child {
              border-bottom: none;
            }

            .person-name {
              font-weight: 500;
              color: #262626;
              margin-bottom: 2px;
            }

            .person-info {
              font-size: 12px;
              color: #8c8c8c;
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
