<!--
  @author
  @date 2025-05-20
-->
<template>
  <div class="personnel-board-container">
    <!-- 考勤记录模态框 -->
    <ViewModal @register="registerModal" @success="handleSuccess" />
    <!-- 行动轨迹模态框 -->
    <TrajectoryModal @register="trajectoryModal" @success="handleSuccess" />
    <!-- 安全帽领用记录模态框 -->
    <HelmetOrderModal @register="registerHelmetOrderModal" @success="handleSuccess" />
    <UserPath
      v-if="isShowUserPathModal"
      :mapObj="dataObj.mapObj"
      :activeUser="activeUser.obj"
      @changeIsShowUserPathModal="changeIsShowUserPathModal"
    >
    </UserPath>
    <BasicTable @register="registerTable">
      <template #bodyCell="{ column, record, index }">
        <template v-if="column.key === 'index'">
          {{ calculateRowIndex(index) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <div class="action-buttons">
            <a-button
              type="primary"
              size="small"
              class="track-btn"
              title="轨迹"
              @click="handleTrack(record)"
            >
              <Icon icon="ant-design:environment-outlined" />
              轨迹
            </a-button>
            <a-button
              type="success"
              size="small"
              title="考勤"
              class="attendance-btn"
              @click="handleAttendance(record)"
            >
              <Icon icon="ant-design:calendar-outlined" />
              考勤
            </a-button>
            <a-button
              type="warning"
              size="small"
              title="安全帽记录"
              class="helmet-btn"
              @click="handleHelmetOrder(record)"
            >
              <Icon icon="ant-design:safety-outlined" />
              安全帽记录
            </a-button>
          </div>
        </template>
        <template v-else-if="column.key === 'workStatus'">
          <DictLabel dictType="work_status_enum" :dictValue="record.workStatus" />
        </template>
        <template v-else-if="column.key === 'personnelStatus'">
          <DictLabel dictType="person_status_enum" :dictValue="record.personnelStatus" />
        </template>
        <template v-else-if="column.key === 'helmetStatus'">
          <DictLabel dictType="helmet_status_enum" :dictValue="record.helmetStatus" />
        </template>
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref, onMounted, reactive, computed } from 'vue';
  import { BasicTable, useTable } from '@/components/swm/Table';
  import { getPersonnelBoardList } from '@/api/swm/personnelBoard';
  import { fetchOrgTreeData } from '@/api/swm/organizationTree';
  import { getEnabledSiteMap } from '@/api/swm/personTrack';
  import { Icon } from '@/components/swm/Icon';
  import { Tag } from 'ant-design-vue';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { DictLabel, useDict } from '@/components/swm/Dict';
  import { useModal } from '@/components/swm/Modal';
  import ViewModal from '../checkAttendance/viewModal.vue';
  import TrajectoryModal from '../persontrack/trajectoryModal.vue';
  import HelmetOrderModal from './HelmetOrderModal.vue';
  import UserPath from '../persontrack/UserPath.vue';
  import { processFileUrl } from '@/utils/file/fileUrlUtils';
  import { router } from '@/router';

  export default defineComponent({
    name: 'ViewsSwmPersonnelBoardPersonnelBoardList',
    components: {
      BasicTable,
      Icon,
      Tag,
      DictLabel,
      ViewModal,
      TrajectoryModal,
      HelmetOrderModal,
      UserPath,
    },
    setup() {
      let isShowUserPathModal = ref(false);
      const { createMessage } = useMessage();
      const { initDict } = useDict();
      let activeUser = reactive({
        obj: {},
      });

      // 时间选择相关状态
      const timeType = ref('month'); // 默认按月查询
      const selectedDate = ref(new Date()); // 默认当前日期
      const timeRangeDisplay = computed(() => {
        switch (timeType.value) {
          case 'day':
            return '本日';
          case 'week':
            return '本周';
          case 'month':
            return '本月';
          default:
            return '本月';
        }
      });

      // 选项数据
      const companyOptions = ref<{ label: string; value: string }[]>([]);
      const departmentOptions = ref<{ label: string; value: string }[]>([]);
      const prodLineOptions = ref<{ label: string; value: string }[]>([]);
      const teamOptions = ref<{ label: string; value: string }[]>([]);

      // 注册考勤记录模态框
      const [registerModal, { openModal }] = useModal();
      // 注册行动轨迹模态框
      const [trajectoryModal, { openModal: batchtrajectoryModal }] = useModal();
      // 注册安全帽领用记录模态框
      const [registerHelmetOrderModal, { openModal: openHelmetOrderModal }] = useModal();
      let dataObj = reactive({
        mapObj: {},
      });

      // 加载底图信息
      async function loadSiteMap() {
        try {
          const response = await getEnabledSiteMap();
          if (response.success && response.data) {
            // 将response.data直接赋值给dataObj.mapObj，不进行额外处理

            response.data.filePath = JSON.parse(response.data.filePath);
            response.data.filePath.previewUrl = processFileUrl(response.data.filePath.previewUrl);
            dataObj.mapObj = response.data;
          } else {
            createMessage.error('获取底图失败：' + response.message);
          }
        } catch (error) {
          console.error('加载底图失败:', error);
          createMessage.error('加载底图失败');
        }
      }

      // 标题和上面导航栏保持一致
      const pageTitle = computed(() => {
        return router.currentRoute.value.meta.title || '人员看板';
      });

      const [registerTable, { getPaginationRef, getForm }] = useTable({
        title: pageTitle.value,
        api: getPersonnelBoardList,
        rowKey: 'id',
        columns: computed(() => [
          {
            title: '序号',
            dataIndex: 'index',
            key: 'index',
            width: 60,
          },
          {
            title: '姓名',
            dataIndex: 'name',
            width: 80,
          },
          {
            title: '所属单位',
            dataIndex: 'organization',
            width: 120,
          },
          {
            title: '所属车间',
            dataIndex: 'workshop',
            width: 100,
          },
          {
            title: '所属产线',
            dataIndex: 'process',
            width: 100,
          },
          {
            title: '所属班组',
            dataIndex: 'team',
            width: 100,
          },
          {
            title: '人员状态',
            dataIndex: 'personnelStatus',
            key: 'personnelStatus',
            width: 100,
          },
          {
            title: '工作状态',
            dataIndex: 'workStatus',
            key: 'workStatus',
            width: 100,
          },
          {
            title: '安全帽编号',
            dataIndex: 'deviceId',
            width: 120,
          },
          {
            title: '安全帽状态',
            dataIndex: 'helmetStatus',
            key: 'helmetStatus',
            width: 100,
          },
          {
            title: `${timeRangeDisplay.value}出勤次数`,
            dataIndex: 'attendanceCount',
            width: 120,
          },
          {
            title: `${timeRangeDisplay.value}工作时长(h)`,
            dataIndex: 'workingHours',
            width: 120,
          },
          {
            title: `${timeRangeDisplay.value}怠工时长(h)`,
            dataIndex: 'idleHours',
            width: 120,
          },
          // {
          //   title: `${timeRangeDisplay.value}进入休闲次数`,
          //   dataIndex: 'leisureCount',
          //   width: 120,
          // },
          // {
          //   title: `${timeRangeDisplay.value}休闲区总逗留时长(min)`,
          //   dataIndex: 'leisureDurationMin',
          //   width: 140,
          // },
          {
            title: '操作',
            dataIndex: 'action',
            key: 'action',
            width: 230,
            fixed: 'right',
            align: 'center',
          },
        ]),
        showTableSetting: true,
        useSearchForm: true,
        // 添加beforeFetch钩子处理时间参数
        beforeFetch: (params) => {
          // 添加时间参数
          params.timeType = params.timeType || timeType.value;

          // 根据时间类型设置时间值
          if (!params.timeValue) {
            if (timeType.value === 'month') {
              params.timeValue = new Date().toISOString().slice(0, 7); // YYYY-MM
            } else if (timeType.value === 'day') {
              params.timeValue = new Date().toISOString().slice(0, 10); // YYYY-MM-DD
            } else if (timeType.value === 'week') {
              // 获取当前日期的年份和周数
              const now = new Date();
              const year = now.getFullYear();
              const onejan = new Date(year, 0, 1);
              const weekNum = Math.ceil(
                ((now.getTime() - onejan.getTime()) / 86400000 + onejan.getDay() + 1) / 7,
              );
              // 格式化为YYYY-WW
              params.timeValue = `${year}-${String(weekNum).padStart(2, '0')}`;
            }
          } else if (params.timeType === 'week' && params.timeValue) {
            // 如果是周查询，转换格式
            const parts = params.timeValue.split('-');
            if (parts.length === 2) {
              const year = parts[0];
              const week = parts[1];
              // 确保周数是两位数
              params.timeValue = `${year}-${week.padStart(2, '0')}`;
            }
          }

          return params;
        },
        formConfig: {
          baseColProps: { lg: 6, md: 8 },
          labelWidth: 100,
          schemas: [
            {
              field: 'name',
              label: '姓名',
              component: 'Input',
              colProps: { span: 8 },
            },
            {
              field: 'timeType',
              label: '时间类型',
              component: 'Select',
              defaultValue: 'month',
              componentProps: {
                options: [
                  { label: '按天', value: 'day' },
                  { label: '按周', value: 'week' },
                  { label: '按月', value: 'month' },
                ],
                onChange: (value) => {
                  timeType.value = value;
                },
              },
              colProps: { span: 8 },
            },
            {
              field: 'timeValue',
              label: '时间选择',
              component: 'DatePicker',
              componentProps: ({ formModel }) => {
                const type = formModel.timeType || 'month';
                switch (type) {
                  case 'day':
                    return {
                      valueFormat: 'YYYY-MM-DD',
                      format: 'YYYY-MM-DD',
                      onChange: (value) => {
                        if (value) {
                          selectedDate.value = new Date(value);
                        }
                      },
                    };
                  case 'week':
                    return {
                      picker: 'week',
                      valueFormat: 'YYYY-ww',
                      format: 'YYYY-ww周',
                      onChange: (value) => {
                        if (value) {
                          // 不在这里转换格式，在beforeFetch时处理
                          selectedDate.value = new Date(value);
                        }
                      },
                    };
                  case 'month':
                    return {
                      picker: 'month',
                      valueFormat: 'YYYY-MM',
                      format: 'YYYY-MM',
                      onChange: (value) => {
                        if (value) {
                          selectedDate.value = new Date(value);
                        }
                      },
                    };
                }
              },
              colProps: { span: 8 },
            },
            {
              field: 'organization',
              label: '所属单位',
              component: 'Select',
              componentProps: () => ({
                placeholder: '请选择单位',
                allowClear: true,
                showSearch: true,
                fieldNames: { label: 'label', value: 'value' },
                options: companyOptions.value,
                onChange: (value: string) => {
                  // 延迟执行，避免干扰表单值设置
                  setTimeout(() => {
                    handleCompanyChange(value);
                  }, 100);
                },
              }),
              colProps: { span: 8 },
            },
            {
              field: 'workshop',
              label: '所属车间',
              component: 'Select',
              componentProps: () => ({
                placeholder: '请选择车间',
                allowClear: true,
                showSearch: true,
                fieldNames: { label: 'label', value: 'value' },
                options: departmentOptions.value,
                onChange: (value: string) => {
                  // 延迟执行，避免干扰表单值设置
                  setTimeout(() => {
                    handleDepartmentChange(value);
                  }, 100);
                },
              }),
              colProps: { span: 8 },
            },
            {
              field: 'process',
              label: '所属产线',
              component: 'Select',
              componentProps: () => ({
                placeholder: '请选择产线',
                allowClear: true,
                showSearch: true,
                fieldNames: { label: 'label', value: 'value' },
                options: prodLineOptions.value,
                onChange: (value: string) => {
                  // 延迟执行，避免干扰表单值设置
                  setTimeout(() => {
                    handleProdLineChange(value);
                  }, 100);
                },
              }),
              colProps: { span: 8 },
            },
            {
              field: 'team',
              label: '所属班组',
              component: 'Select',
              componentProps: () => ({
                placeholder: '请选择班组',
                allowClear: true,
                showSearch: true,
                fieldNames: { label: 'label', value: 'value' },
                options: teamOptions.value,
              }),
              colProps: { span: 8 },
            },
            {
              field: 'workStatus',
              label: '工作状态',
              component: 'Select',
              componentProps: {
                dictType: 'work_status_enum',
                allowClear: true,
              },
              colProps: { span: 8 },
            },
            {
              field: 'personnelStatus',
              label: '人员状态',
              component: 'Select',
              componentProps: {
                dictType: 'person_status_enum',
                placeholder: '请选择人员状态',
                allowClear: true,
              },
              colProps: { span: 8 },
            },
          ],
        },
        canResize: true,
        scroll: { x: 1500 },
        showIndexColumn: false,
        pagination: {
          pageSize: 10,
          defaultPageSize: 10,
          showSizeChanger: true,
          showQuickJumper: true,
        },
      });

      // 计算行序号，考虑分页
      function calculateRowIndex(index: number) {
        const paginationInfo = getPaginationRef();
        if (paginationInfo && typeof paginationInfo !== 'boolean') {
          const current = paginationInfo.current || 1;
          const pageSize = paginationInfo.pageSize || 10;
          return (current - 1) * pageSize + index + 1;
        }
        return index + 1;
      }

      function handleExport() {
        createMessage.success('导出功能待实现');
      }

      function handleTrack(record: Recordable) {
        activeUser.obj = record;
        // console.log(dataObj.mapObj);

        isShowUserPathModal.value = true;
      }

      function handleAttendance(record: Recordable) {
        // 获取当前查询表单的值
        const formInstance = getForm();
        const formValues = formInstance.getFieldsValue();

        // 获取当前查询表单中的时间值
        let queryMonth = formValues.timeValue;
        const queryTimeType = formValues.timeType || 'month';

        // 根据timeType处理不同的时间格式，统一转换为月份格式
        if (queryTimeType === 'week' && queryMonth) {
          // 如果是周查询，从周格式中提取年月
          // 格式可能是 YYYY-ww 或 YYYY-MM-DD (周的第一天)
          if (queryMonth.includes('-') && queryMonth.length > 7) {
            // 如果是日期格式，提取年月
            queryMonth = queryMonth.substring(0, 7);
          } else {
            // 如果是YYYY-ww格式，需要转换为对应的月份
            // 暂时使用当前月份，后续可以根据周数计算具体月份
            const parts = queryMonth.split('-');
            if (parts.length === 2) {
              // 简化处理：使用年份加当前月份
              queryMonth = `${parts[0]}-${new Date().getMonth() + 1 < 10 ? '0' : ''}${
                new Date().getMonth() + 1
              }`;
            }
          }
        } else if (queryTimeType === 'day' && queryMonth) {
          // 如果是日查询，提取月份部分 YYYY-MM-DD -> YYYY-MM
          queryMonth = queryMonth.substring(0, 7);
        }
        // 如果是月查询，直接使用 YYYY-MM 格式

        // 如果没有时间值，使用当前月份
        if (!queryMonth) {
          queryMonth = new Date().toISOString().slice(0, 7);
        }

        // console.log('传递给弹窗的月份:', queryMonth);

        openModal(true, {
          record: {
            id: record.id,
            name: record.name,
            month: queryMonth, // 传递月份
          },
          isUpdate: true,
        });
      }

      // 处理安全帽领用记录
      function handleHelmetOrder(record: Recordable) {
        openHelmetOrderModal(true, {
          record: {
            id: record.id,
            name: record.name,
          },
        });
      }

      // 处理模态框操作成功的回调
      function handleSuccess() {
        // 可以在这里添加刷新表格数据的逻辑
      }

      const changeIsShowUserPathModal = (bel: boolean) => {
        isShowUserPathModal.value = bel;
      };

      // 联动事件处理函数
      // 存储完整的组织数据，用于查找ID
      const orgDataMap = ref<Map<string, any>>(new Map());

      // 处理所属单位变化
      async function handleCompanyChange(value: string) {
        // console.log('所属单位变化:', value);

        // 清空下级选项数据
        departmentOptions.value = [];
        prodLineOptions.value = [];
        teamOptions.value = [];

        // 使用 setTimeout 延迟清空表单字段，避免干扰当前字段值设置
        setTimeout(() => {
          const formInstance = getForm();
          const currentValues = formInstance.getFieldsValue();
          formInstance.setFieldsValue({
            ...currentValues,
            workshop: undefined,
            process: undefined,
            team: undefined,
          });
        }, 150);

        if (!value) {
          return;
        }

        try {
          // 查找单位ID
          const companyData = Array.from(orgDataMap.value.values()).find(
            (item) => item.title === value && item.nodeType === 'office',
          );
          if (!companyData) {
            // console.warn('未找到对应的单位数据');
            return;
          }

          // 根据单位ID加载车间数据
          const result = await fetchOrgTreeData('office', companyData.value || companyData.id);
          if (result && Array.isArray(result)) {
            // 存储车间数据到映射中
            result.forEach((item) => {
              orgDataMap.value.set(`${item.title}-workshop`, item);
            });

            departmentOptions.value = result.map((item) => ({
              label: item.title,
              value: item.title,
            }));
          }
        } catch (error) {
          console.error('加载车间数据失败:', error);
        }
      }

      // 处理所属车间变化
      async function handleDepartmentChange(value: string) {
        // console.log('所属车间变化:', value);

        // 清空下级选项数据
        prodLineOptions.value = [];
        teamOptions.value = [];

        // 使用 setTimeout 延迟清空表单字段，避免干扰当前字段值设置
        setTimeout(() => {
          const formInstance = getForm();
          const currentValues = formInstance.getFieldsValue();
          formInstance.setFieldsValue({
            ...currentValues,
            process: undefined,
            team: undefined,
          });
        }, 150);

        if (!value) {
          return;
        }

        try {
          // 查找车间ID
          const departmentData = orgDataMap.value.get(`${value}-workshop`);
          if (!departmentData) {
            console.warn('未找到对应的车间数据');
            return;
          }

          // 根据车间ID加载产线数据
          const result = await fetchOrgTreeData(
            'workshop',
            departmentData.value || departmentData.id,
          );
          if (result && Array.isArray(result)) {
            // 存储产线数据到映射中
            result.forEach((item) => {
              orgDataMap.value.set(`${item.title}-prodLine`, item);
            });

            prodLineOptions.value = result.map((item) => ({
              label: item.title,
              value: item.title,
            }));
          }
        } catch (error) {
          console.error('加载产线数据失败:', error);
        }
      }

      // 处理所属产线变化
      async function handleProdLineChange(value: string) {
        // console.log('所属产线变化:', value);

        // 清空下级选项数据
        teamOptions.value = [];

        // 使用 setTimeout 延迟清空表单字段，避免干扰当前字段值设置
        setTimeout(() => {
          const formInstance = getForm();
          const currentValues = formInstance.getFieldsValue();
          formInstance.setFieldsValue({
            ...currentValues,
            team: undefined,
          });
        }, 150);

        if (!value) {
          return;
        }

        try {
          // 查找产线ID
          const prodLineData = orgDataMap.value.get(`${value}-prodLine`);
          if (!prodLineData) {
            // console.warn('未找到对应的产线数据');
            return;
          }

          // 根据产线ID加载班组数据
          const result = await fetchOrgTreeData('prodLine', prodLineData.value || prodLineData.id);
          if (result && Array.isArray(result)) {
            // 存储班组数据到映射中
            result.forEach((item) => {
              orgDataMap.value.set(`${item.title}-workGroup`, item);
            });

            teamOptions.value = result.map((item) => ({
              label: item.title,
              value: item.title,
            }));
          }
        } catch (error) {
          console.error('加载班组数据失败:', error);
        }
      }

      // 监听表单字段变化的逻辑将通过 onChange 事件处理

      // 初始化字典数据和下拉选项
      onMounted(async () => {
        initDict(['work_status_enum', 'helmet_status_enum', 'person_status_enum']);

        // 加载下拉选项数据
        try {
          loadSiteMap();
          // 只加载单位数据，其他选项通过联动加载
          const result = await fetchOrgTreeData('root');
          // console.log('fetchOrgTreeData 返回的原始数据:', result);
          if (result && Array.isArray(result)) {
            companyOptions.value = result.map((item) => ({
              label: item.title,
              value: item.title,
            }));
            // console.log('转换后的 companyOptions:', companyOptions.value);
            // 将所有加载的数据存储到orgDataMap中
            result.forEach((item) => {
              orgDataMap.value.set(`${item.title}-office`, item);
            });
          }
        } catch (error) {
          console.error('加载选项数据失败:', error);
        }
      });

      return {
        registerTable,
        registerModal,
        trajectoryModal,
        registerHelmetOrderModal,
        handleExport,
        handleTrack,
        handleAttendance,
        handleHelmetOrder,
        handleSuccess,
        calculateRowIndex,
        companyOptions,
        departmentOptions,
        prodLineOptions,
        teamOptions,
        isShowUserPathModal,
        dataObj,
        activeUser,
        changeIsShowUserPathModal,
        timeType,
        selectedDate,
        timeRangeDisplay,
        handleCompanyChange,
        handleDepartmentChange,
        handleProdLineChange,
      };
    },
  });
</script>

<style lang="less" scoped>
  .personnel-board-container {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .page-header {
    margin-bottom: 16px;
  }

  .page-title {
    font-size: 18px;
    font-weight: bold;
    color: #303133;
  }

  .action-buttons {
    display: flex;
    gap: 6px;
    justify-content: center;

    .ant-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 4px;
      transition: all 0.3s;
      min-width: 60px;
      padding: 0 8px;

      .anticon {
        margin-right: 2px;
        font-size: 12px;
      }

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
      }
    }

    .track-btn {
      background-color: #1890ff;
      border-color: #1890ff;
    }

    .attendance-btn {
      background-color: #52c41a;
      border-color: #52c41a;
    }

    .helmet-btn {
      background-color: #fa8c16;
      border-color: #fa8c16;
    }
  }
</style>
