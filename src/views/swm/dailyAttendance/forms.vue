<!--
  @author Shawn
  @date 2025-05-31
-->
<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="titles"
    :width="1000"
    :showOkBtn="false"
    :showCancelBtn="true"
    cancelText="关闭"
  >
    <div class="safety-helmet-modal">
      <div class="helmet-table-container">
        <BasicTable @register="registerTable" rowKey="id" :loading="tableLoading">
          <template #helmetType="{ record }">
            <DictLabel dictType="helmet_type_enum" :dictValue="record.helmetType" />
          </template>
        </BasicTable>
      </div>
    </div>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicTable, useTable } from '@/components/swm/Table';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { getfindAttendanceRange } from '@/api/swm/dailyAttendance';
  import { DictLabel } from '@/components/swm/Dict';

  export default defineComponent({
    name: 'FormsModal',
    components: { BasicModal, BasicTable, DictLabel },
    setup() {
      const timeTypes = ref('');
      const titles = ref('');
      const tableLoading = ref(false);
      const { createMessage } = useMessage();

      // 表格注册
      const [registerTable, { setTableData }] = useTable({
        columns: [
          {
            title: '进入时间',
            dataIndex: 'enterTime',
            width: 150,
          },
          {
            title: '进入区域名称',
            dataIndex: 'enterAreaName',
            width: 120,
          },
          {
            title: '离开时间',
            dataIndex: 'outTime',
            width: 150,
          },
          {
            title: '离开区域名称',
            dataIndex: 'outAreaName',
            width: 120,
          },
          {
            title: '停留时长',
            dataIndex: 'durationOf',
            width: 150,
          },
        ],
        dataSource: [],
        canResize: false,
        pagination: false,
        showIndexColumn: true,
        bordered: true,
        showTableSetting: false,
        useSearchForm: false,
      });

      // 从后台获取可用的安全帽
      async function fetchAvailableHelmets(keyword?: string) {
        try {
          const result = await getfindAttendanceRange(keyword);
          return result;
        } catch (error) {
          console.error('获取安全帽列表失败', error);
          return [];
        }
      }

      // 模态框注册
      const [registerModal] = useModalInner(async (record: any) => {
        setTableData([]);
        // searchKeyword.value = '';
        timeTypes.value = record.types;
        titles.value = timeTypes.value == 'work' ? '工作时间详情' : '怠工时间详情';
        try {
          await loadAvailableHelmets(record);
        } catch (error) {
          console.error('初始化安全帽对话框失败', error);
          createMessage.error('加载安全帽列表失败，请刷新重试');
        }
      });

      // 加载可用的安全帽
      async function loadAvailableHelmets(record) {
        try {
          tableLoading.value = true;
          const helmets = await fetchAvailableHelmets(record);
          let tableList = timeTypes.value == 'work' ? helmets?.workList : helmets?.restList;
          setTableData(tableList);
        } catch (error) {
          console.error('加载安全帽列表失败', error);
          createMessage.error('加载安全帽列表失败，请刷新重试');
          setTableData([]);
        } finally {
          tableLoading.value = false;
        }
      }

      return {
        registerModal,
        registerTable,
        tableLoading,
        timeTypes,
        titles,
      };
    },
  });
</script>

<style lang="less" scoped>
  .safety-helmet-modal {
    padding: 0 30px;

    .helmet-table-container {
      .table-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;

        .title {
          font-size: 16px;
          font-weight: bold;
        }
      }
    }
  }
</style>
