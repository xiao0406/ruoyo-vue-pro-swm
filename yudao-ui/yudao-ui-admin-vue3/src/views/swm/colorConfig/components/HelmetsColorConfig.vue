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
      </template>
    </BasicTable>
    <HelmetsColorModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref, onMounted } from 'vue';
  import { BasicTable, useTable, TableAction } from '@/components/swm/Table';
  import { useModal } from '@/components/swm/Modal';
  import HelmetsColorModal from './HelmetsColorModal.vue';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { getHelmetConfigList, getHelmetSubitemsByParentId } from '@/api/swm/helmetConfig';
  import { useDict } from '@/components/swm/Dict';

  /**
   * 安全帽颜色配置组件
   * @author Shawn
   * @date 2025/06/23
   * @description 从字典表person_track_color获取安全帽颜色配置数据，保持原有表格结构
   */
  export default defineComponent({
    name: 'HelmetsColorConfig',
    components: { BasicTable, TableAction, HelmetsColorModal },
    setup() {
      const { createMessage } = useMessage();
      const { initGetDictList } = useDict();
      const dataSource = ref<any[]>([]);
      const loading = ref(false);

      const [registerTable, { reload, setLoading, setTableData }] = useTable({
        title: '安全帽颜色配置',
        columns: [
          {
            title: '名称',
            dataIndex: 'name',
            width: 200,
            align: 'left',
          },
          {
            title: '子项数量',
            dataIndex: 'subitemCount',
            width: 150,
            align: 'left',
          },
          {
            title: '操作',
            dataIndex: 'action',
            width: 150,
            key: 'action',
            align: 'center',
          },
        ],
        immediate: false,
        dataSource: dataSource,
        pagination: false,
        striped: false,
        bordered: true,
        showIndexColumn: false,
        useSearchForm: false,
        showTableSetting: false,
        canResize: false,
      });

      const [registerModal, { openModal }] = useModal();

      // 加载字典数据
      async function loadDictData() {
        try {
          setLoading(true);

          // 从字典表获取person_track_color类型的数据
          const dictList = await initGetDictList('person_track_color');

          if (dictList && Array.isArray(dictList)) {
            // 转换字典数据格式为表格需要的格式
            const tableData = await Promise.all(
              dictList.map(async (item: any) => {
                let subitemCount = 0;

                try {
                  // 获取每个字典项的子项数量
                  const subItems = await getHelmetSubitemsByParentId({ parentId: item.id });
                  if (subItems && Array.isArray(subItems)) {
                    subitemCount = subItems.length;
                  }
                } catch (error) {
                  console.warn(`获取字典项 ${item.name || item.dictLabel} 的子项数量失败:`, error);
                  subitemCount = 0;
                }

                return {
                  id: item.id,
                  name: item.name || item.dictLabel || '未命名',
                  subitemCount: subitemCount, // 实际统计的子项数量
                  dictCode: item.dictCode,
                  dictValue: item.value,
                };
              }),
            );

            dataSource.value = tableData;
            setTableData(tableData);

            console.log('安全帽颜色配置数据加载完成:', tableData);
          } else {
            dataSource.value = [];
            setTableData([]);
            createMessage.warning('未找到安全帽颜色配置字典数据');
          }
        } catch (error) {
          console.error('加载安全帽颜色配置失败', error);
          createMessage.error('加载安全帽颜色配置失败');
          dataSource.value = [];
          setTableData([]);
        } finally {
          setLoading(false);
        }
      }

      function handleEdit(record: Recordable) {
        openModal(true, {
          record,
          isUpdate: true,
        });
      }

      function handleSuccess() {
        // 重新加载字典数据
        loadDictData();
      }

      // 初始化时加载数据
      onMounted(() => {
        loadDictData();
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
