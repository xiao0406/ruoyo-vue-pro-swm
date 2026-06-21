<template>
  <div class="daily-attendance-container">
    <!-- <PageWrapper title="日考勤记录" contentBackground contentClass="p-4">
      <template #headerContent>
        <a-alert type="info" show-icon>
          <template #message>
            <span>默认展示当天考勤记录，可通过上方查询条件筛选。</span>
          </template>
        </a-alert>
      </template>
    </PageWrapper> -->

    <!-- 搜索表单组件 -->
    <SearchForm
      :exportLoading="exportLoading"
      @search="handleSearch"
      @reset="handleSearchReset"
      @export="handleExport"
    />

    <!-- 数据表格组件 -->
    <AttendanceTable
      :loading="loading"
      :dataSource="dataSource"
      :pagination="pagination"
      @change="handleTableChange"
      @edit="handleEdit"
    />

    <!-- 编辑考勤记录弹窗 -->
    <EditModal @register="registerEditModal" @success="handleDetailSuccess" />
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref, reactive, onMounted } from 'vue';
  import { message } from 'ant-design-vue';
  // import { PageWrapper } from '@/components/swm/Page';
  import { defHttp } from '@/utils/http/axios';
  import dayjs from 'dayjs';
  import SearchForm from './components/SearchForm.vue';
  import AttendanceTable from './components/AttendanceTable.vue';
  import EditModal from './components/EditModal.vue';
  import { useModal } from '@/components/swm/Modal';
  import { useDict } from '@/components/swm/Dict';

  export default defineComponent({
    name: 'ViewsSwmDailyAttendanceIndex',
    components: {
      // PageWrapper,
      SearchForm,
      AttendanceTable,
      EditModal,
    },
    setup() {
      // 初始化字典（不等待）
      const { initDict } = useDict();
      initDict(['swm_attendance_status', 'swm_current_position', 'shift_type_enum']);

      const loading = ref<boolean>(false);
      const dataSource = ref<any[]>([]);
      const selectedRecord = ref<Recordable | null>(null);
      const exportLoading = ref<boolean>(false);

      // 防止无限循环标记
      const preventLoop = ref(false);

      // 注册编辑弹窗
      const [registerEditModal, { openModal: openEditModal }] = useModal();

      // 分页配置
      const pagination = reactive({
        current: 1,
        pageSize: 10,
        total: 0,
        showTotal: (total: number) => `共 ${total} 条`,
        showSizeChanger: true,
        pageSizeOptions: ['10', '20', '50', '100'],
      });

      // 查询条件
      const searchParams = reactive({
        employeeName: '',
        startDate: dayjs().startOf('month').format('YYYY-MM-DD'),
        endDate: dayjs().endOf('month').format('YYYY-MM-DD'),
        currentMonth: dayjs().startOf('month').format('YYYY-MM-DD'),
        pageNo: 1,
        pageSize: 10,
        orderByColumn: '', // 排序字段
        isAsc: '', // 排序方式
      });

      // 获取日考勤记录列表
      const fetchData = async () => {
        try {
          loading.value = true;
          const params = {
            ...searchParams,
            pageNo: pagination.current,
            pageSize: pagination.pageSize,
          };

          const res = await defHttp.get({
            url: '/swm/swmDailyAttendance/monthlyListData',
            params,
          });

          if (res && res.list) {
            dataSource.value = res.list;
            pagination.total = res.count;
          }
        } catch (error) {
          console.error('获取日考勤记录失败:', error);
          message.error('获取日考勤记录失败');
        } finally {
          loading.value = false;
        }
      };

      // 搜索处理
      const handleSearch = (values: any) => {
        Object.assign(searchParams, values);
        pagination.current = 1; // 重置到第一页
        fetchData();
      };

      // 重置搜索
      const handleSearchReset = (values) => {
        // console.log('重置搜索条件...', values);

        // 如果正在处理重置，直接返回
        if (preventLoop.value) {
          console.log('防止重复请求，跳过');
          return;
        }

        // 设置标记，防止重复处理
        preventLoop.value = true;

        // 清空搜索条件
        if (values) {
          // 如果传入了值，使用传入的值
          Object.assign(searchParams, values);
        } else {
          // 否则使用默认值
          searchParams.employeeName = '';
          searchParams.currentMonth = dayjs().format('YYYY-MM');
        }

        // 重置排序条件
        searchParams.orderByColumn = '';
        searchParams.isAsc = '';

        pagination.current = 1;

        // console.log('重置后的搜索条件:', JSON.stringify(searchParams));

        // 使用setTimeout来确保重置完成后再请求数据，避免连续多次调用
        setTimeout(() => {
          console.log('调用接口获取数据...');
          fetchData();
          // 重置标记
          preventLoop.value = false;
        }, 0);
      };

      // 表格变化（分页、排序等）
      const handleTableChange = (pag: any, filters: any, sorter: any) => {
        pagination.current = pag.current;
        pagination.pageSize = pag.pageSize;

        // 处理排序
        if (sorter && sorter.order) {
          searchParams.orderByColumn = sorter.field;
          searchParams.isAsc = sorter.order === 'ascend' ? 'asc' : 'desc';
        } else {
          searchParams.orderByColumn = '';
          searchParams.isAsc = '';
        }
        fetchData();
      };

      // 打开编辑弹窗
      const handleEdit = (record: any) => {
        openEditModal(true, {
          record,
        });
      };

      // 查看详情
      const handleViewDetail = (record: Recordable) => {
        openEditModal(true, {
          record,
        });
      };

      // 详情模态框关闭
      const handleDetailClose = () => {
        selectedRecord.value = null;
      };

      // 详情保存成功
      const handleDetailSuccess = () => {
        fetchData();
      };

      // 导出Excel
      const handleExport = async (values: any) => {
        try {
          console.log(values, 'valuesvalues');
          exportLoading.value = true;

          const startDate = dayjs(values.startDate);
          const endDate = dayjs(values.endDate);

          if (endDate.isBefore(startDate)) {
            message.warning('结束时间不能早于开始时间');
            return;
          }
          values.currentMonth = values.startDate;
          const res = await defHttp.get({
            url: '/swm/swmDailyAttendance/exportMonthlyData',
            params: values,
          });

          // 检查响应结果
          console.log('导出响应数据:', res);

          if (res && (res.result === true || res.result === 'true') && res.data) {
            // console.log('预览URL:', res.data);

            try {
              // 拼接完整的下载URL
              const baseURL = import.meta.env.VITE_GLOB_API_URL || '';
              const fullDownloadUrl = baseURL + '/swm/' + res.data;
              // console.log('完整下载URL:', fullDownloadUrl);

              // 直接打开下载链接
              window.open(fullDownloadUrl, '_blank');

              message.success('导出成功，文件正在下载...');
            } catch (downloadError) {
              console.error('下载失败:', downloadError);
              message.error('下载失败，请检查网络连接');
            }
          } else {
            console.error('导出失败，响应数据:', res);
            message.error(res.message || '导出失败');
          }
        } catch (error) {
          console.error('导出失败:', error);
          message.error('导出失败');
        } finally {
          exportLoading.value = false;
        }
      };

      // 初始加载
      onMounted(() => {
        fetchData();
      });

      return {
        loading,
        dataSource,
        pagination,
        selectedRecord,
        exportLoading,
        registerEditModal,
        handleSearch,
        handleSearchReset,
        handleTableChange,
        handleEdit,
        handleViewDetail,
        handleDetailClose,
        handleDetailSuccess,
        handleExport,
      };
    },
  });
</script>

<style lang="less" scoped>
  .daily-attendance-container {
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }
</style>
