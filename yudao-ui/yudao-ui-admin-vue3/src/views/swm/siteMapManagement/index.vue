<template>
  <div class="site-map-management-container">
    <div class="title-info"> 场地底图管理 </div>
    <!-- <PageWrapper title="" contentBackground> -->
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleCreate"> 新增 </a-button>
        <a-button type="default" class="ml-2" @click="() => reload()">
          <Icon icon="ant-design:reload-outlined" />
        </a-button>
        <a-button type="default" class="ml-2">
          <Icon icon="ant-design:setting-outlined" />
        </a-button>
      </template>
      <template #tableTitle>
        <div class="flex items-center">
          <a-input
            v-model:value="searchParams.mapName"
            placeholder="请输入底图名称"
            class="mr-2"
            style="width: 220px"
            allow-clear
            @keyup.enter="handleSearch"
          />
          <a-button type="primary" @click="handleSearch">搜索</a-button>
          <a-button class="ml-2" @click="handleReset">重置</a-button>
        </div>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="record.status === '启用' ? 'success' : 'error'">
            {{ record.status }}
          </a-tag>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <div class="action-btns">
            <a-tooltip title="编辑">
              <a-button type="link" class="action-btn edit-btn" @click="handleEdit(record)">
                <Icon icon="clarity:note-edit-line" class="action-icon" />
              </a-button>
            </a-tooltip>
            <a-tooltip title="删除">
              <a-button type="link" class="action-btn delete-btn" @click="handleDelete(record)">
                <Icon icon="ant-design:delete-outlined" class="action-icon" />
              </a-button>
            </a-tooltip>
            <a-tooltip :title="record.status === '启用' ? '禁用' : '启用'">
              <a-button
                type="link"
                class="action-btn"
                :class="record.status === '启用' ? 'disable-btn' : 'enable-btn'"
                @click="record.status === '启用' ? handleDisable(record) : handleEnable(record)"
              >
                <Icon
                  :icon="
                    record.status === '启用'
                      ? 'ant-design:stop-outlined'
                      : 'ant-design:check-circle-outlined'
                  "
                  class="action-icon"
                />
              </a-button>
            </a-tooltip>
          </div>
        </template>
      </template>
    </BasicTable>
    <SiteMapDrawer @register="registerDrawer" @success="handleSuccess" />
    <!-- </PageWrapper> -->
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref, onMounted, nextTick } from 'vue';
  import { BasicTable, useTable, TableAction } from '@/components/swm/Table';
  import { useDrawer } from '@/components/swm/Drawer';
  import { useI18n } from '@/hooks/swm/useI18n';
  import { useMessage } from '@/hooks/swm/useMessage';
  // import { PageWrapper } from '@/components/swm/Page';
  import { Icon } from '@/components/swm/Icon';
  import SiteMapDrawer from './components/SiteMapDrawer.vue';
  import { getSiteMapList, deleteSiteMap, enableSiteMap, disableSiteMap } from '@/api/swm/siteMap';
  import { Tag, Tooltip, Button, Input } from 'ant-design-vue';

  export default defineComponent({
    name: 'ViewsSwmSiteMapManagementIndex',
    components: {
      BasicTable,
      TableAction,
      SiteMapDrawer,
      // PageWrapper,
      Icon,
      ATag: Tag,
      ATooltip: Tooltip,
      AButton: Button,
      AInput: Input,
    },
    emits: ['register'],
    setup() {
      const { t } = useI18n();
      const { createMessage, createConfirm } = useMessage();
      const searchParams = ref({
        mapName: '', // 改为mapName更符合实际含义
      });

      const [registerDrawer, drawerMethods] = useDrawer();
      const openDrawerSafe = (visible = true, data?: any) => {
        // 使用时间戳确保每次都能正确触发
        const timestamp = Date.now();

        // 添加时间戳到数据对象
        const dataWithTimestamp = {
          ...data,
          _timestamp: timestamp,
        };

        // 多次尝试打开抽屉，采用不同的延迟策略
        // 第一次立即尝试
        nextTick(() => {
          drawerMethods.openDrawer(visible, dataWithTimestamp);

          // 第二次在短暂延迟后尝试(50ms)
          setTimeout(() => {
            dataWithTimestamp._timestamp = timestamp + 1; // 微调时间戳
            drawerMethods.openDrawer(visible, dataWithTimestamp);

            // 第三次在较长延迟后尝试(150ms)
            setTimeout(() => {
              dataWithTimestamp._timestamp = timestamp + 2; // 再次微调时间戳
              drawerMethods.openDrawer(visible, dataWithTimestamp);
            }, 100);
          }, 50);
        });
      };

      // 在组件挂载后执行初始化操作
      onMounted(() => {});

      const [registerTable, { reload }] = useTable({
        title: '场地底图管理',
        api: getSiteMapList,
        immediate: true,
        columns: [
          {
            title: '底图名称',
            dataIndex: 'name',
            width: 180,
            align: 'left',
          },
          {
            title: '所属项目',
            dataIndex: 'project',
            width: 160,
            align: 'left',
          },
          {
            title: '底图尺寸',
            dataIndex: 'size',
            width: 120,
            align: 'center',
          },
          {
            title: '图纸像素',
            width: 160,
            align: 'center',
            customRender: ({ record }) => {
              return `X:${record.drawingPixelX || 0}, Y:${record.drawingPixelY || 0}`;
            },
          },
          {
            title: '场地坐标(米)',
            width: 160,
            align: 'center',
            customRender: ({ record }) => {
              return `X:${record.siteCoordinateXM || 0}, Y:${record.siteCoordinateYM || 0}`;
            },
          },
          {
            title: '比例尺',
            dataIndex: 'scale',
            width: 120,
            align: 'center',
          },
          {
            title: '路径',
            dataIndex: 'url',
            width: 200,
            align: 'left',
            ellipsis: true,
          },
          {
            title: '状态',
            dataIndex: 'status',
            width: 100,
            align: 'center',
          },
        ],
        useSearchForm: false,
        showTableSetting: false,
        bordered: true,
        showIndexColumn: false,
        canResize: true,
        rowKey: 'id',
        fetchSetting: {
          pageField: 'pageNo',
          sizeField: 'pageSize',
          listField: 'items',
          totalField: 'total',
        },
        beforeFetch: (params) => {
          return {
            ...params,
            ...searchParams.value,
          };
        },
        afterFetch: (result) => {
          return result;
        },
        pagination: {
          pageSize: 20,
          current: 1,
          defaultPageSize: 20,
          defaultCurrent: 1,
          showQuickJumper: true,
          showTotal: (total) => `共 ${total} 条数据`,
          showSizeChanger: true,
          pageSizeOptions: ['10', '20', '50', '100'],
        },
        actionColumn: {
          width: 180,
          title: '操作',
          dataIndex: 'action',
          fixed: false,
        },
      });

      function handleCreate() {
        // 直接打开抽屉
        openDrawerSafe(true, {
          isUpdate: false,
        });
      }

      function handleEdit(record: Recordable) {
        // 直接打开抽屉
        openDrawerSafe(true, {
          record,
          isUpdate: true,
        });
      }

      function handleDelete(record: Recordable) {
        createConfirm({
          iconType: 'warning',
          title: '确认删除',
          content: '是否确认删除此条数据？',
          onOk: async () => {
            await deleteSiteMap(record.id);
            createMessage.success('删除成功');
            reload();
          },
        });
      }

      function handleEnable(record: Recordable) {
        createConfirm({
          iconType: 'warning',
          title: '确认启用',
          content: '是否确认启用此场地底图？',
          onOk: async () => {
            await enableSiteMap(record.id);
            createMessage.success('启用成功');
            reload();
          },
        });
      }

      function handleDisable(record: Recordable) {
        createConfirm({
          iconType: 'warning',
          title: '确认禁用',
          content: '是否确认禁用此场地底图？',
          onOk: async () => {
            await disableSiteMap(record.id);
            createMessage.success('禁用成功');
            reload();
          },
        });
      }

      function handleSuccess() {
        reload();
      }

      function handleSearch() {
        // 直接调用API发送请求，使用mapName参数名更符合后端API需求
        getSiteMapList({
          pageNo: 1,
          pageSize: 20,
          mapName: searchParams.value.mapName, // 明确指定参数名为mapName
        }).then((res) => {
          // 刷新表格数据
          reload();
        });
      }

      function handleReset() {
        searchParams.value.mapName = '';
        reload({
          page: 1,
        });
      }

      return {
        t,
        searchParams,
        registerTable,
        registerDrawer,
        handleCreate,
        handleEdit,
        handleDelete,
        handleEnable,
        handleDisable,
        handleSuccess,
        handleSearch,
        handleReset,
        reload,
      };
    },
  });
</script>

<style lang="less" scoped>
  .site-map-management-container {
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    .title-info {
      background-color: #fff;
      font-size: 16px;
      font-weight: normal;
      margin-bottom: 10px;
      padding: 10px;
    }
  }
  .ant-tag {
    margin-right: 0;
  }

  .action-btns {
    display: flex;
    align-items: center;
    justify-content: center;

    .action-btn {
      padding: 0;
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 4px;

      .action-icon {
        font-size: 16px;
      }

      &.edit-btn {
        color: #1890ff;

        &:hover {
          color: #40a9ff;
          background-color: #e6f7ff;
        }
      }

      &.delete-btn {
        color: #ff4d4f;

        &:hover {
          color: #ff7875;
          background-color: #fff1f0;
        }
      }

      &.enable-btn {
        color: #52c41a;

        &:hover {
          color: #73d13d;
          background-color: #f6ffed;
        }
      }

      &.disable-btn {
        color: #faad14;

        &:hover {
          color: #ffc53d;
          background-color: #fffbe6;
        }
      }
    }
  }
</style>
