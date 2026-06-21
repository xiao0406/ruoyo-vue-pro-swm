<template>
  <div class="search-form-container">
    <div class="search-form">
      <div style="display: flex; align-items: center; gap: 16px">
        <div class="form-item">
          <span class="label">员工姓名:</span>
          <a-input
            v-model:value="searchForm.employeeName"
            placeholder="请输入员工姓名"
            allow-clear
          />
        </div>

        <div class="form-item">
          <span class="label">开始时间:</span>
          <a-date-picker
            v-model:value="searchForm.startDate"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            placeholder="请选择开始时间"
            style="width: 100%"
          />
        </div>

        <div class="form-item">
          <span class="label">结束时间:</span>
          <a-date-picker
            v-model:value="searchForm.endDate"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            placeholder="请选择结束时间"
            style="width: 100%"
          />
        </div>

        <!-- <div class="form-item">
          <span class="label">班次:</span>
          <a-select
            v-model:value="searchForm.team"
            placeholder="请选择班次"
            :allowClear="true"
            style="width: 100%"
          >
            <a-select-option v-for="item in shiftOptions" :key="item.key" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </div> -->
      </div>

      <div class="rightBtn">
        <a-button type="primary" class="btn" @click="handleSubmit">查询</a-button>
        <a-button @click="handleReset" class="btn">重置</a-button>
        <a-button type="default" class="btn" :loading="exportLoading" @click="handleExport">
          <template #icon>
            <DownloadOutlined />
          </template>
          导出Excel
        </a-button>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
  import { defineComponent, reactive, computed } from 'vue';
  import { DatePicker, Input, Button, Select } from 'ant-design-vue';
  import { DownloadOutlined } from '@ant-design/icons-vue';
  import { useDict } from '@/components/swm/Dict';
  import dayjs from 'dayjs';
  import { message } from 'ant-design-vue';

  export default defineComponent({
    name: 'SearchForm',
    components: {
      'a-date-picker': DatePicker,
      'a-input': Input,
      'a-button': Button,
      'a-select': Select,
      'a-select-option': Select.Option,
      DownloadOutlined,
    } as any,
    emits: ['search', 'reset', 'export'],
    props: {
      exportLoading: {
        type: Boolean,
        default: false,
      },
    },
    setup(_, { emit }) {
      // 获取字典数据
      const { getDictList } = useDict();

      // 班次选项数据（使用computed同步获取）
      const shiftOptions = computed(() => {
        return getDictList('shift_type_enum').map((item) => ({
          label: item.name,
          value: item.value,
          key: item.id,
        }));
      });

      // 简单的表单状态
      const searchForm = reactive({
        employeeName: '',
        startDate: dayjs().startOf('month').format('YYYY-MM-DD'),
        endDate: dayjs().endOf('month').format('YYYY-MM-DD'),
        currentMonth: dayjs().startOf('month').format('YYYY-MM-DD'),
        team: '',
      });

      // 表单提交
      const handleSubmit = () => {
        console.log('提交表单数据:', searchForm);

        // 日期校验：结束时间不能早于开始时间
        if (searchForm.startDate && searchForm.endDate) {
          const startDate = dayjs(searchForm.startDate);
          const endDate = dayjs(searchForm.endDate);

          if (endDate.isBefore(startDate)) {
            message.warning('结束时间不能早于开始时间');
            return;
          }
        }

        // 创建一个普通对象而不是响应式对象
        const values = {
          employeeName: searchForm.employeeName,
          startDate: searchForm.startDate,
          endDate: searchForm.endDate,
          currentMonth: searchForm.startDate,
          team: '',
        };

        emit('search', values);
      };

      // 表单重置
      const handleReset = () => {
        console.log('执行表单重置...');

        // 重置表单字段
        searchForm.employeeName = '';
        searchForm.startDate = dayjs().startOf('month').format('YYYY-MM-DD');
        searchForm.endDate = dayjs().endOf('month').format('YYYY-MM-DD');
        searchForm.team = '';

        console.log('重置完成，触发父组件reset事件');

        // 创建一个普通对象
        const resetValues = {
          employeeName: '',
          startDate: dayjs().startOf('month').format('YYYY-MM-DD'),
          endDate: dayjs().endOf('month').format('YYYY-MM-DD'),
          team: '',
        };

        // 触发父组件的重置事件
        emit('reset', resetValues);
      };

      // 导出处理
      const handleExport = () => {
        console.log('触发导出事件，当前表单数据:', searchForm);

        // 日期校验：结束时间不能早于开始时间
        if (searchForm.startDate && searchForm.endDate) {
          const startDate = dayjs(searchForm.startDate);
          const endDate = dayjs(searchForm.endDate);

          if (endDate.isBefore(startDate)) {
            message.warning('结束时间不能早于开始时间');
            return;
          }
        }

        // 创建一个普通对象
        const exportValues = {
          employeeName: searchForm.employeeName,
          startDate: searchForm.startDate,
          endDate: searchForm.endDate,
          team: '',
        };

        // 触发父组件的导出事件
        emit('export', exportValues);
      };

      return {
        searchForm,
        shiftOptions,
        handleSubmit,
        handleReset,
        handleExport,
      };
    },
  });
</script>

<style lang="less" scoped>
  .search-form-container {
    margin-bottom: 16px;
    padding: 16px;
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);

    .search-form {
      display: flex;
      align-items: center;
      justify-content: space-between;
      width: 100%;

      .form-item {
        display: flex;
        align-items: center;
        flex: 0 0 280px;

        .label {
          min-width: 65px;
          text-align: right;
          margin-right: 8px;
        }
      }

      .rightBtn {
        .btn {
          margin: 0px 5px;
        }
      }

      .form-actions {
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }
  }
</style>
