<!--
  @author Shawn
  @date 2025-05-14
-->
<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="离职归档记录" :width="1200">
    <div class="departure-record-container">
      <Tabs v-model:activeKey="activeTab" type="card" class="departure-tabs">
        <!-- 离职记录Tab -->
        <TabPane key="departure" tab="离职记录" class="tab-pane">
          <div class="tab-content">
            <BasicTable @register="registerTable" @row-click="handleView">
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'action'">
                  <TableAction
                    :actions="[
                      {
                        icon: 'clarity:info-standard-line',
                        label: '查看',
                        onClick: handleView.bind(null, record),
                      },
                    ]"
                  />
                </template>
              </template>
            </BasicTable>
          </div>
        </TabPane>

        <!-- 安全教育记录Tab -->
        <TabPane key="education" class="tab-pane">
          <template #tab>
            <span>
              安全教育记录
              <Badge
                v-if="educationCount > 0"
                :count="educationCount"
                :number-style="{ backgroundColor: '#52c41a' }"
                style="margin-left: 8px"
              />
            </span>
          </template>
          <div class="tab-content">
            <BasicTable @register="registerEducationTable" :loading="educationLoading">
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'safetyEducationType'">
                  <span>{{
                    record.safetyEducationTypeText || record.safetyEducationType || '--'
                  }}</span>
                </template>
                <template v-else-if="column.key === 'participationType'">
                  <span>{{
                    record.participationTypeText || record.participationType || '--'
                  }}</span>
                </template>
                <template v-else-if="column.key === 'safetyStatus'">
                  <span>{{ record.safetyStatusText || record.safetyStatus || '--' }}</span>
                </template>
                <template v-else-if="column.key === 'startTime'">
                  <span>{{ record.startTime ? formatDate(record.startTime) : '--' }}</span>
                </template>
              </template>
            </BasicTable>
          </div>
        </TabPane>
      </Tabs>
    </div>
  </BasicModal>

  <!-- 离职详情弹窗 -->
  <DepartureDetail @register="registerDetailModal" />
</template>

<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { BasicTable, useTable, TableAction } from '@/components/swm/Table';
  import { BasicModal, useModal, useModalInner } from '@/components/swm/Modal';
  import { Tabs, TabPane, Badge } from 'ant-design-vue';
  import { departureRecordColumns } from '@/views/swm/personnel/departure.data';
  import { getDepartureDetail, getSafetyEducationByIdentityCard } from '@/api/swm/person';
  import DepartureDetail from './DepartureDetail.vue';
  import { formatToDateTime } from '@/utils/dateUtil';

  export default defineComponent({
    name: 'DepartureRecordList',
    components: { BasicModal, BasicTable, TableAction, DepartureDetail, Tabs, TabPane, Badge },
    emits: ['register'],
    setup() {
      const recordList = ref([]);
      const educationLoading = ref(false);
      const activeTab = ref('departure');
      const educationCount = ref(0);

      // 安全教育记录表格列定义
      const educationColumns = [
        {
          title: '主题',
          dataIndex: 'theme',
          key: 'theme',
          width: 200,
        },
        {
          title: '教育类型',
          dataIndex: 'safetyEducationType',
          key: 'safetyEducationType',
          width: 120,
        },
        {
          title: '参与类型',
          dataIndex: 'participationType',
          key: 'participationType',
          width: 100,
        },
        {
          title: '状态',
          dataIndex: 'safetyStatus',
          key: 'safetyStatus',
          width: 80,
        },
        {
          title: '开始时间',
          dataIndex: 'startTime',
          key: 'startTime',
          width: 150,
        },
        {
          title: '内容描述',
          dataIndex: 'contentDescription',
          key: 'contentDescription',
          ellipsis: true,
        },
      ];

      const [registerTable, { setTableData }] = useTable({
        columns: departureRecordColumns,
        canResize: true,
        showTableSetting: true,
        showIndexColumn: false,
        rowKey: 'id',
        bordered: true,
        actionColumn: {
          width: 120,
          title: '操作',
          dataIndex: 'action',
          key: 'action',
          fixed: 'right',
        },
      });

      const [registerEducationTable, { setTableData: setEducationTableData }] = useTable({
        columns: educationColumns,
        canResize: true,
        showTableSetting: true,
        showIndexColumn: true,
        rowKey: 'id',
        bordered: true,
        pagination: {
          pageSize: 10,
        },
      });

      const [registerModal, { setModalProps }] = useModalInner(async (data) => {
        recordList.value = data || [];
        setTableData(recordList.value);

        // 重置Tab到离职记录
        activeTab.value = 'departure';

        // 加载安全教育记录
        await loadSafetyEducationRecords(data);
      });

      // 注册详情弹窗
      const [registerDetailModal, { openModal: openDetailModal }] = useModal();

      // 加载安全教育记录
      async function loadSafetyEducationRecords(departureRecords) {
        if (!departureRecords || departureRecords.length === 0) {
          setEducationTableData([]);
          return;
        }

        // 获取第一条离职记录的身份证号
        const identityCard = departureRecords[0]?.identityCard;
        if (!identityCard) {
          setEducationTableData([]);
          return;
        }

        try {
          educationLoading.value = true;
          const response = await getSafetyEducationByIdentityCard(identityCard);

          if (response.success) {
            const educationData = response.data || [];
            setEducationTableData(educationData);
            educationCount.value = educationData.length;
          } else {
            console.error('查询安全教育记录失败:', response.message);
            setEducationTableData([]);
            educationCount.value = 0;
          }
        } catch (error) {
          console.error('查询安全教育记录异常:', error);
          setEducationTableData([]);
          educationCount.value = 0;
        } finally {
          educationLoading.value = false;
        }
      }

      // 查看离职详情
      async function handleView(record) {
        if (!record?.id) return;

        try {
          const detailData = await getDepartureDetail(record.id);
          if (detailData.success) {
            openDetailModal(true, detailData.data);
          }
        } catch (error) {
          console.error('获取离职详情失败', error);
        }
      }

      // 格式化日期
      function formatDate(date) {
        if (!date) return '--';
        return formatToDateTime(date);
      }

      return {
        registerModal,
        registerTable,
        registerEducationTable,
        registerDetailModal,
        educationLoading,
        activeTab,
        educationCount,
        handleView,
        formatDate,
      };
    },
  });
</script>

<style lang="less" scoped>
  .departure-record-container {
    height: 100%;
    padding: 0 20px;

    .departure-tabs {
      height: 100%;

      :deep(.ant-tabs-content-holder) {
        height: calc(100% - 44px);
        overflow: hidden;
      }

      :deep(.ant-tabs-tabpane) {
        height: 100%;
        overflow: hidden;
      }
    }

    .tab-pane {
      height: 100%;

      .tab-content {
        height: 100%;
        overflow: auto;
      }
    }

    // Tab标签样式优化
    :deep(.ant-tabs-tab) {
      font-weight: 500;

      &.ant-tabs-tab-active {
        .ant-tabs-tab-btn {
          color: #1890ff;
          font-weight: 600;
        }
      }
    }

    // Badge样式调整
    :deep(.ant-badge) {
      .ant-badge-count {
        font-size: 12px;
        height: 18px;
        line-height: 18px;
        min-width: 18px;
        padding: 0 4px;
      }
    }
  }
</style>
