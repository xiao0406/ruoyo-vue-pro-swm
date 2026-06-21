<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="title"
    @ok="handleSubmit"
    :showOkBtn="false"
    :showCancelBtn="false"
    width="900px"
  >
    <div class="helmet-order-container">
      <BasicTable @register="registerTable" />
    </div>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, ref, h } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicTable, useTable } from '@/components/swm/Table';
  import { getPersonHelmetOrders } from '@/api/swm/personnelBoard';
  import { Tag } from 'ant-design-vue';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { formatToDateTime } from '@/utils/dateUtil';
  import { DictLabel, useDict } from '@/components/swm/Dict';
  import { onMounted } from 'vue';

  export default defineComponent({
    name: 'HelmetOrderModal',
    components: { BasicModal, BasicTable, Tag, DictLabel },
    emits: ['register', 'success'],
    setup(_, { emit }) {
      const personId = ref('');
      const personName = ref('');
      const { createMessage } = useMessage();
      const { initDict } = useDict();

      // 初始化字典数据
      onMounted(() => {
        initDict(['helmet_type_enum']);
      });

      const title = ref('安全帽领用记录');

      const [registerTable, { setTableData, setLoading }] = useTable({
        title: '安全帽领用记录',
        columns: [
          {
            title: '序号',
            dataIndex: 'index',
            width: 60,
            customRender: ({ index }) => index + 1,
          },
          {
            title: '安全帽编号',
            dataIndex: 'deviceId',
            width: 150,
          },
          {
            title: '类型',
            dataIndex: 'helmetModel',
            width: 120,
            customRender: ({ text, record }) => {
              return h(DictLabel, {
                dictType: 'helmet_type_enum',
                dictValue: text,
              });
            },
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
            title: '关联状态',
            dataIndex: 'usageStatus',
            width: 100,
            customRender: ({ text }) => {
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
          {
            title: '绑定时长',
            dataIndex: 'bindDuration',
            width: 100,
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
        ],
        bordered: true,
        showIndexColumn: false,
        pagination: {
          pageSize: 10,
          showSizeChanger: true,
          showQuickJumper: true,
        },
        useSearchForm: false,
      });

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: false });
        const { record } = data || {};
        if (record) {
          personId.value = record.id;
          personName.value = record.name || '未知';
          title.value = `${personName.value} - 安全帽领用记录`;
          await fetchData(record.id);
        }
      });

      async function fetchData(id: string) {
        try {
          setLoading(true);
          const res = await getPersonHelmetOrders({ personId: id });

          if (res.success && res.data) {
            setTableData(res.data);
          } else {
            setTableData([]);
            createMessage.info('该人员暂无安全帽领用记录');
          }
        } catch (error) {
          console.error('获取安全帽领用记录失败:', error);
          createMessage.error('获取安全帽领用记录失败');
          setTableData([]);
        } finally {
          setLoading(false);
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
  .helmet-order-container {
    width: 100%;
    min-height: 400px;
    padding: 0px 20px;
  }
</style>
