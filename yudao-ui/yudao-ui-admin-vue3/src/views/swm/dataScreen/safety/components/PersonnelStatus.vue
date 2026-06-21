<template>
  <div class="personnel-status">
    <div class="status-header">
      <div class="header-title">现场人员状态</div>
    </div>

    <div class="search-bar">
      <a-input placeholder="请输入" allowClear style="width: 200px; margin-right: 12px"></a-input>
      <a-button type="primary">搜索</a-button>
    </div>

    <div class="filter-bar">
      <a-select defaultValue="按人员类型" style="width: 200px">
        <a-select-option value="全部">全部</a-select-option>
        <a-select-option value="管理">管理</a-select-option>
        <a-select-option value="工人">工人</a-select-option>
      </a-select>
      <a-button type="primary" class="recall-btn">应急召回</a-button>
    </div>

    <div class="workshop-list">
      <!-- 一车间 -->
      <div class="workshop-group">
        <div class="workshop-header" @click="toggleWorkshop('one')">
          <div class="workshop-title">
            <span>二车间</span>
          </div>
          <div class="workshop-icon">
            <DownOutlined v-if="workshopOneExpanded" />
            <RightOutlined v-else />
          </div>
        </div>

        <div class="personnel-items" v-if="workshopOneExpanded">
          <div class="personnel-item" v-for="(person, index) in allActivePersons" :key="index">
            <div class="person-info-row">
              <div class="person-info">
                <div>姓名：{{ person.name }}</div>
                <div>工种：{{ person.job }}</div>
              </div>
              <div class="person-info">
                <div>设备编号：{{ person.deviceId }}</div>
                <div>电量：{{ person.batteryLevel }}%</div>
              </div>
            </div>
            <div class="person-location">
              <div>位置：{{ person.location }}</div>
              <div class="status-label" :class="person.status === '运动' ? 'active' : 'idle'"
                >状态：{{ person.status }}</div
              >
            </div>
            <div class="action-btns">
              <a-button size="small" type="primary">定位</a-button>
              <a-button size="small" type="primary" @click="showRecallModal(person)">召回</a-button>
              <a-button size="small" type="primary">轨迹</a-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 二车间 -->
      <!-- <div class="workshop-group">
        <div class="workshop-header" @click="toggleWorkshop('two')">
          <div class="workshop-title">
            <span>二车间</span>
          </div>
          <div class="workshop-icon">
            <DownOutlined v-if="workshopTwoExpanded" />
            <RightOutlined v-else />
          </div>
        </div>
        
        <div class="personnel-items" v-if="workshopTwoExpanded">
          <div class="personnel-item" v-for="(person, index) in workshopTwoPersonnel" :key="index">
            <div class="person-info-row">
              <div class="person-info">
                <div>姓名：{{ person.name }}</div>
                <div>工种：{{ person.job }}</div>
              </div>
              <div class="person-info">
                <div>设备编号：{{ person.deviceId }}</div>
                <div>电量：{{ person.batteryLevel }}%</div>
              </div>
            </div>
            <div class="person-location">
              <div>位置：{{ person.location }}</div>
              <div class="status-label" :class="person.status === '运动' ? 'active' : 'idle'">状态：{{ person.status }}</div>
            </div>
            <div class="action-btns">
              <a-button size="small" type="primary">定位</a-button>
              <a-button size="small" type="primary" @click="showRecallModal(person)">召回</a-button>
              <a-button size="small" type="primary">轨迹</a-button>
            </div>
          </div>
        </div>
      </div> -->

      <!-- 三车间 -->
      <!-- <div class="workshop-group">
        <div class="workshop-header" @click="toggleWorkshop('three')">
          <div class="workshop-title">
            <span>三车间</span>
          </div>
          <div class="workshop-icon">
            <DownOutlined v-if="workshopThreeExpanded" />
            <RightOutlined v-else />
          </div>
        </div>
        
        <div class="personnel-items" v-if="workshopThreeExpanded">
          <div class="personnel-item" v-for="(person, index) in workshopThreePersonnel" :key="index">
            <div class="person-info-row">
              <div class="person-info">
                <div>姓名：{{ person.name }}</div>
                <div>工种：{{ person.job }}</div>
              </div>
              <div class="person-info">
                <div>设备编号：{{ person.deviceId }}</div>
                <div>电量：{{ person.batteryLevel }}%</div>
              </div>
            </div>
            <div class="person-location">
              <div>位置：{{ person.location }}</div>
              <div class="status-label" :class="person.status === '运动' ? 'active' : 'idle'">状态：{{ person.status }}</div>
            </div>
            <div class="action-btns">
              <a-button size="small" type="primary">定位</a-button>
              <a-button size="small" type="primary" @click="showRecallModal(person)">召回</a-button>
              <a-button size="small" type="primary">轨迹</a-button>
            </div>
          </div>
        </div>
      </div> -->
    </div>

    <!-- 召回确认弹窗组件 -->
    <RecallModal
      v-model:visible="recallModalVisible"
      :personName="currentPerson?.name || '张三'"
      @confirm="handleRecallConfirm"
      @cancel="handleRecallCancel"
    />
  </div>
</template>

<script lang="ts" setup>
  import { ref, onMounted } from 'vue';
  import {
    Input as AInput,
    Button as AButton,
    Select as ASelect,
    Modal as AModal,
    message,
  } from 'ant-design-vue';
  import { DownOutlined, RightOutlined } from '@ant-design/icons-vue';
  import { getAllActivePersonsWithIdCardFromCache } from '@/api/swm/person';

  import RecallModal from './RecallModal.vue';

  const ASelectOption = ASelect.Option;
  const ATextarea = AInput.TextArea;
  const allActivePersons = ref([
    {
      name: '',
      job: '',
      deviceId: '',
      batteryLevel: 0,
      location: '',
      status: '',
    },
  ]);

  // 控制车间展开/折叠状态
  const workshopOneExpanded = ref(true);
  const workshopTwoExpanded = ref(false);
  const workshopThreeExpanded = ref(false);

  // 召回弹窗相关状态
  const recallModalVisible = ref(false);
  const currentPerson = ref<PersonInfo | null>(null);

  // 人员类型定义
  interface PersonInfo {
    name: string;
    job: string;
    deviceId: string;
    batteryLevel: number;
    location: string;
    status: string;
  }

  // 切换车间展开/折叠状态
  const toggleWorkshop = (workshop: string) => {
    if (workshop === 'one') {
      workshopOneExpanded.value = !workshopOneExpanded.value;
    } else if (workshop === 'two') {
      workshopTwoExpanded.value = !workshopTwoExpanded.value;
    } else if (workshop === 'three') {
      workshopThreeExpanded.value = !workshopThreeExpanded.value;
    }
  };

  // 显示召回确认弹窗
  const showRecallModal = (person: PersonInfo) => {
    currentPerson.value = person;
    recallModalVisible.value = true;
  };

  // 处理召回取消
  const handleRecallCancel = () => {
    recallModalVisible.value = false;
  };

  // 处理召回确认
  const handleRecallConfirm = (data: { template: string; content: string }) => {
    console.log('召回确认', data, currentPerson.value);
    // 这里可以添加调用API的逻辑
  };

  // 一车间人员数据
  const workshopOnePersonnel = ref([
    {
      name: '张三',
      job: '普工',
      deviceId: 'wgg-1',
      batteryLevel: 89,
      location: '天富厂一车间道道',
      status: '运动',
    },
    {
      name: '张三',
      job: '普工',
      deviceId: 'wgg-1',
      batteryLevel: 89,
      location: '天富厂一车间道道',
      status: '运动',
    },
    {
      name: '张三',
      job: '普工',
      deviceId: 'wgg-1',
      batteryLevel: 89,
      location: '天富厂一车间道道',
      status: '运动',
    },
  ]);

  // 二车间人员数据
  const workshopTwoPersonnel = ref([
    {
      name: '李四',
      job: '电工',
      deviceId: 'wgg-2',
      batteryLevel: 75,
      location: '天富厂二车间',
      status: '运动',
    },
    {
      name: '王五',
      job: '电工',
      deviceId: 'wgg-3',
      batteryLevel: 82,
      location: '天富厂二车间',
      status: '静止',
    },
  ]);

  // 三车间人员数据
  const workshopThreePersonnel = ref([
    {
      name: '赵六',
      job: '机修工',
      deviceId: 'wgg-4',
      batteryLevel: 65,
      location: '天富厂三车间',
      status: '运动',
    },
    {
      name: '钱七',
      job: '安装工',
      deviceId: 'wgg-5',
      batteryLevel: 91,
      location: '天富厂三车间',
      status: '静止',
    },
  ]);
  const allActivePersonsData = async () => {
    const res = await getAllActivePersonsWithIdCardFromCache(); // 接口调用
    console.log('res', res);

    // 将返回的对象转换为数组格式，并添加UI需要的字段
    if (res.data) {
      const personsArray = Object.values(res.data).map((person: any) => ({
        name: person.name || '',
        job: person.jobType || '普工',
        deviceId: person.deviceId || person.safetyHelmetId || 'wgg-1', // 优先使用新的deviceId字段
        batteryLevel: person.batteryLevel || Math.floor(Math.random() * 30) + 70, // 优先使用真实电量，无数据时随机生成
        location:
          person.location || `${person.company || '天津厂'}${person.department || '一车间'}`, // 优先使用真实位置，无数据时使用默认位置
        status: person.motionStatus || '运动', // 优先使用真实运动状态，无数据时默认为运动
      }));
      allActivePersons.value = personsArray;
    }
  };
  onMounted(() => {
    allActivePersonsData();
  });
  const handleAction = (action: string) => {
    console.log(`执行操作: ${action}`);
  };
</script>

<style lang="less" scoped>
  .personnel-status {
    height: 100%;
    padding: 16px;
    display: flex;
    flex-direction: column;

    .status-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 16px;

      .header-title {
        font-size: 16px;
        font-weight: 500;
        color: #17233d;
      }
    }

    .search-bar {
      display: flex;
      margin-bottom: 12px;
    }

    .filter-bar {
      display: flex;
      justify-content: space-between;
      margin-bottom: 16px;

      .recall-btn {
        background-color: #1890ff;
        color: white;
      }
    }

    .workshop-list {
      flex: 1;
      overflow-y: auto;

      .workshop-group {
        margin-bottom: 12px;
        border: 1px solid #e8e8e8;
        border-radius: 4px;

        .workshop-header {
          padding: 8px 12px;
          background-color: #f7f7f7;
          cursor: pointer;
          display: flex;
          justify-content: space-between;
          align-items: center;

          .workshop-title {
            font-weight: 500;
          }

          .workshop-icon {
            font-size: 12px;
          }
        }

        .personnel-items {
          .personnel-item {
            padding: 12px;
            border-bottom: 1px solid #f0f0f0;

            &:last-child {
              border-bottom: none;
            }

            .person-info-row {
              display: flex;
              justify-content: space-between;
              margin-bottom: 8px;

              .person-info {
                font-size: 12px;
                line-height: 1.5;

                div {
                  display: flex;
                }
              }
            }

            .person-location {
              display: flex;
              justify-content: space-between;
              font-size: 12px;
              margin-bottom: 8px;

              .status-label {
                &.active {
                  color: #1890ff;
                }

                &.idle {
                  color: #faad14;
                }
              }
            }

            .action-btns {
              display: flex;
              gap: 8px;

              button {
                padding: 0 8px;
                height: 24px;
                font-size: 12px;
              }
            }
          }
        }
      }
    }
  }
</style>
