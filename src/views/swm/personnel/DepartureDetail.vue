<!--
  @author Shawn
  @date 2025-05-14
-->
<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="'离职归档详情'"
    :width="1000"
    :footer="null"
    :canFullscreen="true"
  >
    <!-- <Description size="large" :column="2" :data="detailData" :schema="schema" class="m-4 pt-4" :bordered="true" /> -->
    <div class="record-box">
      <div v-for="(item, index) in schema" class="record-item" :key="item.field + index">
        <div class="record-label">{{ item.label }}</div>
        <div class="record-content" :title="detailData[item.field] || '---'">{{
          detailData[item.field] || '---'
        }}</div>
      </div>
    </div>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, ref, computed } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { Description, DescItem } from '@/components/swm/Description/index';
  import { formatToDate } from '@/utils/dateUtil';
  import { getHelmetReturnedText, getDepartureTypeText } from '@/enums/swm/personEnum';

  export default defineComponent({
    name: 'DepartureDetail',
    components: { BasicModal, Description },
    emits: ['register'],
    setup() {
      const rawData = ref<Recordable>({});

      // 处理枚举值显示
      const detailData = computed(() => {
        const data = { ...rawData.value };

        // 处理是否归还安全帽
        if (data.helmetReturned && !data.helmetReturnedText) {
          data.helmetReturnedText = getHelmetReturnedText(data.helmetReturned);
          console.log('data.helmetReturnedText:', data.helmetReturnedText);
        }

        // 处理离职类型
        if (data.departureType && !data.departureTypeText) {
          data.departureTypeText = getDepartureTypeText(data.departureType);
          console.log('data.departureTypeText:', data.departureTypeText);
        }

        return data;
      });

      const schema = [
        {
          field: 'name',
          label: '姓名',
        },
        {
          field: 'identityCard',
          label: '身份证号码',
        },
        {
          field: 'phoneNumber',
          label: '手机号码',
        },
        {
          field: 'company',
          label: '所属单位',
        },
        {
          field: 'department',
          label: '所属车间',
        },
        {
          field: 'workProcess',
          label: '所属工序',
        },
        {
          field: 'team',
          label: '所属班组',
        },
        {
          field: 'jobType',
          label: '所属工种',
        },
        {
          field: 'departureDate',
          label: '离职时间',
          render: (val) => {
            return val ? formatToDate(val, 'YYYY-MM-DD HH:mm') : '';
          },
        },
        {
          field: 'departureTypeText',
          label: '离职类型',
        },
        {
          field: 'helmetReturnedText',
          label: '是否归还安全帽',
        },
        {
          field: 'departureReason',
          label: '离职原因',
          span: 2,
        },
        {
          field: 'remarks',
          label: '备注',
          span: 2,
        },
      ] as DescItem[];

      const [registerModal, { setModalProps }] = useModalInner((data) => {
        rawData.value = data || {};
        setModalProps({ loading: false });
      });

      return { registerModal, detailData, schema };
    },
  });
</script>
<style lang="less" scoped>
  .record-box {
    display: flex;
    align-items: center;
    padding: 0px 30px;
    flex-wrap: wrap;

    .record-item {
      display: flex;
      align-items: center;
      width: 50%;
      margin-bottom: 20px;

      .record-label {
        font-weight: bold;
        width: 120px;
        height: 32px;
        line-height: 32px;
        text-align: right;
        padding: 0 8px;
      }

      .record-content {
        border: 1px solid #d9d9d9;
        border-radius: 4px;
        height: 32px;
        background-color: #fafafa;
        width: calc(100% - 120px);
        line-height: 32px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        padding: 0px 11px;
      }
    }
  }
</style>
