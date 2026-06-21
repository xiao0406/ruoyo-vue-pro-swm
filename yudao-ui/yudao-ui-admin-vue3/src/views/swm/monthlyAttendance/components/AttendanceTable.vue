<template>
  <div class="attendance-table-container">
    <BasicTable
      :columns="columns"
      :dataSource="dataSource"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
      :rowKey="(record) => record.id"
      :scroll="{ x: 'max-content', y: 'calc(100vh - 300px)' }"
    >
      <!-- 员工姓名列 -->
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'employeeName'">
          <!-- <a @click="handleEdit(record)" class="link">{{ record.employeeName }}</a> -->
          {{ record.employeeName }}
        </template>

        <!-- 应考勤时间范围列 -->
        <template v-else-if="column.dataIndex === 'workTimeRange'">
          {{ record.workTimeRange || '未排班' }}
        </template>

        <!-- 班次列 -->
        <template v-else-if="column.dataIndex === 'classes'">
          <DictLabel dictType="shift_type_enum" :dictValue="record.classes" />
        </template>

        <!-- 打卡时间列 -->
        <template v-else-if="column.dataIndex === 'clockInDate'">
          <span :class="{ 'text-danger': isLate(record) }">
            <!-- {{ formatTime(record.clockInTime) }} -->
            {{ record.clockInDate }}
          </span>
        </template>

        <template v-else-if="column.dataIndex === 'clockOutDate'">
          <!-- {{ formatTime(record.clockOutTime) }} -->
          {{ record.clockOutDate }}
        </template>

        <!-- 考勤状态列 -->
        <template v-else-if="column.dataIndex === 'attendanceNormal'">
          <DictLabel dictType="swm_attendance_status" :dictValue="record.attendanceNormal" />
        </template>

        <!-- 当前位置列 -->
        <!-- <template v-else-if="column.dataIndex === 'currentPosition'">
          <DictLabel dictType="swm_current_position" :dictValue="record.currentPosition" />
        </template> -->

        <!-- 创建时间列 -->
        <!-- <template v-else-if="column.dataIndex === 'createDate'">
          {{ formatDateTime(record.createDate) }}
        </template> -->

        <!-- 更新时间列 -->
        <!-- <template v-else-if="column.dataIndex === 'updateDate'">
          {{ formatDateTime(record.updateDate) }}
        </template> -->

        <!-- 操作列 -->
        <template v-else-if="column.dataIndex === 'action'">
          <!-- <div class="action-btns">
            <a-button type="link" @click="handleEdit(record)" class="action-btn">

            </a-button> -->
          <!-- <TableAction :actions="getActions(record)" /> -->
          <!-- </div> -->
        </template>
      </template>
      <!-- <template #action="{ record }">
        <TableAction
          :actions="[
            {
              icon: 'clarity:note-edit-line',
              tooltip: '编辑',
              onClick: handleEdit.bind(null, record),
            },
          ]"
        />
      </template> -->
    </BasicTable>
  </div>
</template>

<script lang="ts">
  import { defineComponent, PropType, ref, computed } from 'vue';
  import { BasicTable, BasicColumn, TableAction } from '@/components/swm/Table';
  import { DictLabel } from '@/components/swm/Dict';
  import dayjs from 'dayjs';

  // 考勤记录接口
  interface AttendanceRecord {
    id: string;
    employeeId: string;
    employeeName: string;
    deviceId: string;
    attendanceDate: string;
    workTimeRange: string;
    clockInTime: string;
    clockOutTime: string;
    scheduledHours: number;
    actualHours: number;
    idleHours: number;
    dailyEfficiency: number;
    dailyAchievementRate: number;
    attendanceNormal: string;
    currentPosition: string;
    createBy?: string;
    createDate?: string;
    updateBy?: string;
    updateDate?: string;
    [key: string]: any;
  }

  export default defineComponent({
    name: 'AttendanceTable',
    components: {
      BasicTable,
      DictLabel,
      TableAction,
    },
    props: {
      dataSource: {
        type: Array as PropType<AttendanceRecord[]>,
        default: () => [],
      },
      loading: {
        type: Boolean,
        default: false,
      },
      pagination: {
        type: Object,
      },
    },
    emits: ['change', 'edit'],
    setup(props, { emit }) {
      function getActions(record: Recordable) {
        const actions = [
          {
            icon: 'ant-design:eye-outlined',
            tooltip: '查看',
            onClick: handleEdit.bind(null, record),
          },
        ];

        return actions;
      }
      const localPagination = computed(() => ({
        current: 1,
        pageSize: 10,
        total: 0,
        showSizeChanger: true,
        showQuickJumper: true,
        showTotal: (total: number) => `共 ${total} 条`,
        ...props.pagination, // 覆盖默认值
      }));

      // 表格列配置
      const columns = ref<BasicColumn[]>([
        {
          title: '月份',
          dataIndex: 'monthly',
          key: 'monthly',
          width: 120,
          sorter: true,
        },
        {
          title: '姓名',
          dataIndex: 'employeeName',
          key: 'employeeName',
          width: 120,
          fixed: 'left',
        },
        {
          title: '手机号码',
          dataIndex: 'phoneNumber',
          key: 'phoneNumber',
          width: 150,
        },
        {
          title: '班组',
          dataIndex: 'team',
          key: 'team',
          width: 80,
        },
        {
          title: '工种',
          dataIndex: 'jobType',
          key: 'jobType',
          width: 150,
          sorter: true,
        },
        {
          title: '出勤天数(天)',
          dataIndex: 'attendanceDay',
          key: 'attendanceDay',
          width: 150,
          sorter: true,
        },
        {
          title: '月出勤率',
          dataIndex: 'monthlyAttendanceRate',
          key: 'monthlyAttendanceRate',
          width: 120,
        },
        {
          title: '有效考勤天数(天)',
          dataIndex: 'validAttendanceDays',
          key: 'validAttendanceDays',
          width: 120,
        },
        {
          title: '本月有效考勤时长(h)',
          dataIndex: 'actualHours',
          key: 'actualHours',
          width: 120,
          sorter: true,
        },
        {
          title: '本月怠工时长',
          dataIndex: 'idleHours',
          key: 'idleHours',
          width: 130,
          sorter: true,
        },
        {
          title: '操作',
          dataIndex: 'action',
          key: 'action',
          width: 100,
          fixed: 'right',
        },
      ]);

      // 表格变化处理
      const handleTableChange = (pagination: any, filters: any, sorter: any) => {
        // console.log(pagination);

        emit('change', pagination, filters, sorter);
      };

      // 编辑记录
      const handleEdit = (record: AttendanceRecord) => {
        emit('edit', record);
      };

      // 日期格式化
      const formatDate = (date: string) => {
        if (!date) return '';
        return dayjs(date).format('YYYY-MM-DD');
      };

      const formatDateTime = (time: string) => {
        if (!time) return '';
        return dayjs(time).format('YYYY-MM-DD HH:mm:ss');
      };

      // 时间格式化
      const formatTime = (time: string) => {
        if (!time) return '暂无';

        // console.log('格式化时间前:', time);

        // 如果时间已经是HH:MM:SS格式，直接返回
        if (time.includes(':') && !time.includes('-') && !time.includes(' ')) {
          // console.log('已是时间格式，直接返回:', time);
          return time;
        }

        // 如果是完整的日期时间格式 YYYY-MM-DD HH:MM:SS
        if (time.includes(' ') && time.includes('-')) {
          const timePart = time.split(' ')[1];
          // console.log('从日期时间中提取时间部分:', timePart);
          return timePart;
        }

        // 其他情况下尝试用dayjs解析
        try {
          const formatted = dayjs(time).format('HH:mm:ss');
          // console.log('用dayjs解析后:', formatted);
          return formatted;
        } catch (e) {
          console.error('解析时间失败:', time, e);
          return '格式错误';
        }
      };

      // 判断是否迟到
      const isLate = (record: AttendanceRecord) => {
        if (!record.clockInTime || !record.workTimeRange) return false;

        const clockInTime = dayjs(record.clockInTime);
        const workTimeStart = record.workTimeRange.split('-')[0];

        if (!workTimeStart) return false;

        const [hour, minute] = workTimeStart.split(':').map(Number);
        const scheduledStartTime = dayjs(record.attendanceDate).hour(hour).minute(minute).second(0);

        return clockInTime.isAfter(scheduledStartTime);
      };

      return {
        getActions,
        columns,
        handleTableChange,
        handleEdit,
        formatDate,
        formatTime,
        formatDateTime,
        isLate,
        localPagination,
      };
    },
  });
</script>

<style lang="less" scoped>
  .attendance-table-container {
    flex: 1;
    //  height: 100%;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    border-radius: 4px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);

    .link {
      color: #1890ff;
      cursor: pointer;
    }

    .text-danger {
      color: #ff4d4f;
    }

    .action-btns {
      display: flex;
      gap: 8px;

      .action-btn {
        padding: 0 4px;
      }
    }
  }

  :deep(.ant-table-wrapper) {
    overflow: hidden;
  }
</style>
