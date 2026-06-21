<template>
  <div>
    <BasicTable @register="registerTable">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <TableAction
            :actions="[
              {
                icon: 'clarity:note-edit-line',
                color: 'info',
                tooltip: '编辑',
                onClick: handleEdit.bind(null, record),
              },
            ]"
          />
        </template>
        <template v-else-if="column.key === 'color'">
          <div class="color-block" :style="{ backgroundColor: record.color || '#ff0000' }"></div>
        </template>
      </template>
    </BasicTable>
    <BeaconColorModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref, onMounted, reactive } from 'vue';
  import { BasicTable, useTable, TableAction } from '@/components/swm/Table';
  import { useModal } from '@/components/swm/Modal';
  import BeaconColorModal from './BeaconColorModal.vue';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { getAllAreaList } from '@/api/swm/area';
  import { getAllBeaconList } from '@/api/swm/beacon';

  export default defineComponent({
    name: 'BeaconColorConfig',
    components: { BasicTable, TableAction, BeaconColorModal },
    setup() {
      const { createMessage } = useMessage();
      const loading = ref(false);
      // 提供初始数据避免首次加载时的问题
      const tableData = reactive({
        items: [] as any[],
        total: 0,
      });

      const [registerTable, { reload, setLoading, setTableData }] = useTable({
        title: '信标颜色配置（区域管理）',
        columns: [
          {
            title: '区域名称',
            dataIndex: 'name',
            width: 200,
            align: 'left',
          },
          {
            title: '颜色',
            dataIndex: 'color',
            width: 150,
            key: 'color',
            align: 'center',
          },
          {
            title: '备注',
            dataIndex: 'remarks',
            width: 250,
            align: 'left',
          },
          {
            title: '操作',
            dataIndex: 'action',
            width: 100,
            key: 'action',
            align: 'center',
          },
        ],
        // 使用手动数据加载方式，避免直接绑定api
        immediate: false,
        dataSource: tableData.items,
        pagination: false,
        striped: false,
        bordered: true,
        showIndexColumn: false,
        useSearchForm: false,
        showTableSetting: false,
        canResize: false,
      });

      const [registerModal, { openModal }] = useModal();

      // 手动加载数据
      const loadTableData = async () => {
        try {
          setLoading(true);

          // 1. 获取所有区域数据
          const areaRes = await getAllAreaList();
          if (!areaRes || !areaRes.success || !Array.isArray(areaRes.list)) {
            console.error('获取区域数据失败:', areaRes);
            tableData.items = [];
            setTableData([]);
            return;
          }

          // 2. 获取所有信标数据
          const beaconRes = await getAllBeaconList();
          if (!beaconRes || !beaconRes.success || !Array.isArray(beaconRes.list)) {
            console.error('获取信标数据失败:', beaconRes);
            tableData.items = [];
            setTableData([]);
            return;
          }

          const areas = areaRes.list;
          const beacons = beaconRes.list;

          // 3. 处理数据：为每个区域找到关联的信标，获取颜色
          const processedData = areas.map((area: any) => {
            // 查找该区域关联的信标
            const regionBeacons = beacons.filter((beacon: any) => beacon.area === area.id);

            // 获取该区域第一个信标的颜色，如果没有信标或颜色为空则使用默认红色
            let areaColor = '#ff0000'; // 默认红色
            if (regionBeacons.length > 0) {
              const firstBeaconColor = regionBeacons[0].beaconColor;
              if (firstBeaconColor && firstBeaconColor.trim() !== '') {
                areaColor = firstBeaconColor;
              }
            }

            return {
              id: area.id,
              name: area.areaName || '未命名区域',
              color: areaColor,
              remarks: area.remarks || area.voicePrompt || '',
              areaType: area.areaType,
              voicePrompt: area.voicePrompt,
              beaconCount: regionBeacons.length, // 记录信标数量，供编辑时参考
            };
          });

          tableData.items = processedData;
          tableData.total = processedData.length;
          setTableData(processedData);

          console.log('区域颜色配置数据加载完成:', processedData);
        } catch (error) {
          console.error('加载区域颜色配置失败', error);
          createMessage.error('加载区域颜色配置失败');
          tableData.items = [];
          setTableData([]);
        } finally {
          setLoading(false);
        }
      };

      // 处理编辑
      function handleEdit(record: any) {
        openModal(true, {
          record,
        });
      }

      // 处理成功回调
      function handleSuccess() {
        // 重新加载数据以显示最新的颜色信息
        loadTableData();
      }

      // 初始化时加载数据
      onMounted(() => {
        loadTableData();
      });

      return {
        registerTable,
        registerModal,
        handleEdit,
        handleSuccess,
      };
    },
  });
</script>

<style lang="less" scoped>
  .color-block {
    width: 60px;
    height: 20px;
    border-radius: 2px;
    display: inline-block;
  }
</style>
