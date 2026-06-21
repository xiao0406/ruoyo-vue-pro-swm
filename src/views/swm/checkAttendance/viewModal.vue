<template>
  <BasicModal
    v-bind="$attrs"
    @register="managementModal"
    :title="'个人考勤记录明细'"
    width="80%"
    :showCancelBtn="false"
    :showOkBtn="false"
  >
    <Row class="ml" v-if="isShowData">
      <Col span="12">
        <Tables :recordData="recordData" />
      </Col>
      <Col span="12">
        <Row>
          <Col span="12">
            <averagePerformance :recordData="recordData" />
          </Col>
          <Col span="12">
            <attendanceAchievementRate :recordData="recordData" />
          </Col>
        </Row>
      </Col>
    </Row>
    <Row class="echarts-box" v-if="isShowData">
      <Col span="24">
        <h1 class="title">{{ monthDisplayText }}考勤时间统计</h1>
        <echartsTime :recordData="recordData" />
      </Col>
    </Row>
    <Row class="echarts-box" v-if="isShowData">
      <Col span="24">
        <h1 class="title">{{ monthDisplayText }}工效统计</h1>
        <EchartsEffect :recordData="recordData" />
      </Col>
    </Row>
    <template #footer>
      <div style="text-align: right">
        <a-button type="primary" @click="handleSubmit">关闭</a-button>
      </div>
    </template>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed } from 'vue';
  import { Col, Row, Button as AButton } from 'ant-design-vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import averagePerformance from './averagePerformance.vue';
  import attendanceAchievementRate from './attendanceAchievementRate.vue';
  import { attendanceDetails } from '@/api/swm/personnelBoard';
  import Tables from './tables.vue';
  import echartsTime from './echartsTime.vue';
  import EchartsEffect from './echatrsEffect.vue';
  import dayjs from 'dayjs';

  const emit = defineEmits(['success']);
  const rowId = ref('');
  const month = ref('');
  // 表格数据及图表数据
  const recordData = ref({});
  let isShowData = ref(false);

  // 计算属性：显示月份文本
  const monthDisplayText = computed(() => {
    if (month.value) {
      const date = dayjs(month.value);
      return date.format('YYYY年MM月');
    }
    return '本月';
  });

  const [managementModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false });
    rowId.value = data.record.id;
    // 使用传入的月份，如果没有则使用当前月份
    month.value = data.record.month || dayjs().format('YYYY-MM');

    console.log('接收到的记录数据:', data.record);
    console.log('查询的月份:', month.value);
    isShowData.value = false;

    // 加载数据
    await loadAttendanceData();
  });

  // 提取加载数据的逻辑为独立函数
  async function loadAttendanceData() {
    try {
      const response = await attendanceDetails({
        employeeId: rowId.value,
        month: month.value,
      });
      recordData.value = response;
    } catch (error) {
      console.error('调用 attendanceDetails 接口出错:', error);
    } finally {
      setTimeout(() => {
        isShowData.value = true;
      }, 10);
    }
  }

  async function handleSubmit() {
    closeModal();
    emit('success');
  }
</script>
<style lang="less" scoped>
  .kq-border-header {
    text-align: center;
    border: solid #999;
    border-width: 1px 1px 0px 1px;
  }

  .kq-border-header-top {
    text-align: center;
    border: solid #999;
    border-width: 1px 1px 0px 1px;
  }

  .kq-border-header-center {
    text-align: center;
    border: solid #999;
    border-width: 1px 1px 0px 0px;
  }

  .kq-border-header-end {
    text-align: center;
    border: solid #999;
    border-width: 1px 1px 1px 0px;
  }

  .color-1 {
    background-color: #efefef;
  }

  .color-2 {
    background-color: red;
  }

  .ml {
    margin-left: 40px;
  }

  .echarts-box {
    margin-left: 10px;
  }

  .title {
    line-height: 100px;
    margin-left: 60px;
  }
</style>
