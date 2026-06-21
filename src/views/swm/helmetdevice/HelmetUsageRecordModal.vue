<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="title"
    @ok="handleSubmit"
    :showOkBtn="false"
    :showCancelBtn="false"
    :width="1000"
  >
    <div class="usage-record-container">
      <BasicTable @register="registerTable" />
    </div>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, ref, computed, h } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicTable, useTable } from '@/components/swm/Table';
  import { getHelmetDevice, getHelmetUsageRecords } from '@/api/swm/helmetDevice';
  import { Tag } from 'ant-design-vue';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { formatToDateTime } from '@/utils/dateUtil';

  export default defineComponent({
    name: 'HelmetUsageRecordModal',
    components: { BasicModal, BasicTable, Tag },
    emits: ['register', 'success'],
    setup(_, { emit }) {
      const helmetId = ref('');
      const recordData = ref<any>({});
      const { createMessage } = useMessage();

      const title = '使用记录';

      const [registerTable, { setTableData }] = useTable({
        title: '使用记录',
        columns: [
          {
            title: '人员名称',
            dataIndex: 'personName',
            width: 120,
          },
          {
            title: '身份证号',
            dataIndex: 'binder',
            width: 180,
          },
          {
            title: '绑定时间',
            dataIndex: 'bindTime',
            width: 180,
            customRender: ({ text }) => {
              return text ? formatToDateTime(text) : '-';
            },
          },
          {
            title: '解绑时间',
            dataIndex: 'unbindTime',
            width: 180,
            customRender: ({ text }) => {
              return text ? formatToDateTime(text) : '-';
            },
          },
          {
            title: '绑定时长',
            dataIndex: 'bindDuration',
            width: 120,
            customRender: ({ text, record }) => {
              if (text && text > 0) {
                return `${text}天`;
              }
              // 如果是使用中状态且有绑定时间，计算当前时长
              if (record.usageStatus === '0' && record.bindTime) {
                const bindTime = new Date(record.bindTime);
                const now = new Date();
                const diffTime = now.getTime() - bindTime.getTime();
                const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
                return `${diffDays}天`;
              }
              return '-';
            },
          },
          {
            title: '使用状态',
            dataIndex: 'usageStatus',
            width: 100,
            customRender: ({ text, record }) => {
              // 使用状态：0-使用中, 1-已解绑
              if (text === '0') {
                return h(Tag, { color: 'success' }, () => '使用中');
              } else if (text === '1') {
                return h(Tag, { color: 'default' }, () => '已解绑');
              } else {
                return h(Tag, { color: 'warning' }, () => '未知状态');
              }
            },
          },
        ],
        bordered: true,
        showIndexColumn: false,
        pagination: false,
        useSearchForm: false,
      });

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: false });
        const { record } = data || {};
        if (record) {
          helmetId.value = record.deviceId;
          await fetchData(record.deviceId);
        }
      });

      async function fetchData(deviceId: string) {
        try {
          setModalProps({ confirmLoading: true });
          const res = await getHelmetUsageRecords(deviceId);

          if (res.success && res.data) {
            setTableData(res.data);
          } else {
            setTableData([]);
            createMessage.warning('未找到相关使用记录');
          }
        } catch (error) {
          console.error('获取使用记录失败:', error);
          createMessage.error('获取使用记录失败');
          setTableData([]);
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      function handleSubmit() {
        closeModal();
      }

      return {
        registerModal,
        registerTable,
        title,
        handleSubmit,
      };
    },
  });
</script>

<style lang="less" scoped>
  .usage-record-container {
    width: 100%;
    min-height: 400px;
    padding: 0px 20px;
  }
</style>
