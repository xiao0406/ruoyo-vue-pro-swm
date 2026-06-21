<template>
  <div class="voice-template-table">
    <BasicForm
      @register="registerForm"
      @submit="handleSubmit"
      @reset="handleReset"
      class="search-form"
    />

    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleAddClick">新增</a-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column && column.key === 'templateCode'">
          <DictLabel dictType="voice_template_type" :dictValue="record.templateCode" />
        </template>
        <template v-else-if="column && column.key === 'status'">
          <a-tag :color="record.status === '0' ? 'blue' : 'orange'">
            <DictLabel dictType="swm_voice_template_status" :dictValue="record.status" />
          </a-tag>
        </template>
        <template v-else-if="column && column.key === 'action'">
          <TableAction
            :actions="[
              {
                icon: 'clarity:note-edit-line',
                color: 'info',
                tooltip: '编辑',
                onClick: handleEdit.bind(null, record),
              },
              {
                icon:
                  record.status === '0'
                    ? 'ant-design:stop-outlined'
                    : 'ant-design:play-circle-outlined',
                color: record.status === '0' ? 'warning' : 'success',
                tooltip: record.status === '0' ? '禁用' : '启用',
                onClick: handleToggleStatus.bind(null, record),
              },
              {
                icon: 'ant-design:delete-outlined',
                color: 'error',
                tooltip: '删除',
                popConfirm: {
                  title: '是否确认删除',
                  confirm: handleDelete.bind(null, record),
                },
              },
            ]"
          />
        </template>
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref, onMounted, reactive, nextTick, computed } from 'vue';
  import { BasicTable, useTable, TableAction } from '@/components/swm/Table';
  import { BasicForm, useForm, FormSchema } from '@/components/swm/Form';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { Tag, Button } from 'ant-design-vue';
  import {
    getVoiceTemplateList,
    updateVoiceTemplate,
    deleteVoiceTemplate,
    updateTemplateStatus,
  } from '@/api/swm/voiceTemplate';
  import { router } from '@/router';
  import type { BasicColumn } from '@/components/swm/Table';
  import { DictLabel, useDict } from '@/components/swm/Dict';

  // 表单模式
  const searchFormSchema: FormSchema[] = [
    {
      field: 'templateName',
      label: '模板名称',
      component: 'Input',
      colProps: { span: 5 },
      componentProps: {
        placeholder: '请输入',
      },
    },
    {
      field: 'templateCode',
      label: '模板类型',
      component: 'Select',
      colProps: { span: 5 },
      componentProps: {
        placeholder: '请选择',
        dictType: 'voice_template_type',
        allowClear: true,
        showSearch: true,
      },
    },
    {
      field: 'status',
      label: '应用状态',
      component: 'Select',
      colProps: { span: 5 },
      componentProps: {
        placeholder: '请选择',
        dictType: 'swm_voice_template_status',
        allowClear: true,
      },
    },
  ];

  export default defineComponent({
    name: 'VoiceTemplateTable',
    components: {
      BasicTable,
      TableAction,
      BasicForm,
      'a-tag': Tag,
      'a-button': Button,
      DictLabel,
    },
    emits: ['edit', 'delete', 'add'],
    setup(_, { emit }) {
      const { createMessage } = useMessage();
      const loading = ref(false);
      const searchParams = reactive({
        templateName: '',
        templateCode: '',
        status: '',
      });
      const { initDict } = useDict();

      // 初始化字典数据
      onMounted(() => {
        initDict(['voice_template_type', 'swm_voice_template_status']);
      });

      // 注册搜索表单
      const [registerForm, { getFieldsValue, resetFields }] = useForm({
        labelWidth: 80,
        baseColProps: { lg: 6, md: 8, style: { padding: '0 8px' } },
        schemas: searchFormSchema,
        showActionButtonGroup: true,
        actionColOptions: {
          span: 9,
          style: { textAlign: 'right', paddingRight: '20px' },
        },
        submitButtonOptions: {
          text: '查询',
          color: 'primary',
        },
      });
      //标题和上面导航栏保持一致
      const pageTitle = computed(() => {
        // 假设你通过路由元信息定义了页面标题
        return router.currentRoute.value.meta.title || '语音模版管理';
      });

      // 定义操作列配置
      const actionColumn: BasicColumn = {
        width: 150,
        actions: (record: Recordable) => [
          {
            icon: 'clarity:note-edit-line',
            color: 'info',
            tooltip: '编辑',
            onClick: handleEdit.bind(null, record),
          },
          {
            icon:
              record.status === '0'
                ? 'ant-design:stop-outlined'
                : 'ant-design:play-circle-outlined',
            color: record.status === '0' ? 'warning' : 'success',
            tooltip: record.status === '0' ? '禁用' : '启用',
            onClick: handleToggleStatus.bind(null, record),
          },
          {
            icon: 'ant-design:delete-outlined',
            color: 'error',
            tooltip: '删除',
            popConfirm: {
              title: '是否确认删除',
              confirm: handleDelete.bind(null, record),
            },
          },
        ],
      };

      // 注册表格
      const [registerTable, { reload: reloadTable, setLoading }] = useTable({
        title: pageTitle.value,
        columns: [
          {
            title: '模板名称',
            dataIndex: 'templateName',
            width: 180,
            align: 'left',
          },
          {
            title: '模板类型',
            key: 'templateCode',
            width: 100,
            align: 'left',
          },
          {
            title: '推送方式',
            dataIndex: 'pushMethod',
            width: 150,
            align: 'left',
            customRender: ({ text }) => {
              if (!text) return '无';
              const methods = text.split(',');
              const methodMap = {
                '1': '定位模块推送',
                '2': '短信推送',
              };
              return methods.map((m) => methodMap[m] || m).join('、');
            },
          },
          {
            title: '推送频次',
            dataIndex: 'pushFrequency',
            width: 150,
            align: 'left',
            customRender: ({ text }) => {
              const frequencyMap = {
                '1': '仅一次',
                '2': '按固定时间间隔推送',
              };
              return frequencyMap[text] || '仅一次';
            },
          },
          {
            title: '语音文字',
            dataIndex: 'voiceText',
            width: 250,
            align: 'left',
            ellipsis: true,
          },
          {
            title: '应用状态',
            dataIndex: 'status',
            key: 'status',
            width: 100,
            align: 'left',
          },
          {
            title: '操作',
            key: 'action',
            width: 150,
            align: 'left',
          },
        ],
        // 使用真实API
        api: getVoiceTemplateList,
        // 格式化请求参数
        beforeFetch: (params) => {
          const searchParameters = { ...searchParams };

          // 移除空值
          Object.keys(searchParameters).forEach((key) => {
            if (!searchParameters[key]) {
              delete searchParameters[key];
            }
          });

          // 无需手动设置 pageNo 和 pageSize，让组件自行处理
          // 通过 fetchSetting 配置已经指定了正确的参数名称
          return {
            ...searchParameters,
          };
        },
        // 正确处理后端返回数据结构
        fetchSetting: {
          pageField: 'pageNo',
          sizeField: 'pageSize',
          listField: 'list',
          totalField: 'count',
        },
        pagination: {
          current: 1,
          pageSize: 10,
          defaultPageSize: 10,
          showSizeChanger: true,
          showQuickJumper: true,
          pageSizeOptions: ['5', '10', '20', '50'],
        },
        striped: false,
        bordered: true,
        showIndexColumn: false,
        useSearchForm: false,
        showTableSetting: false,
        canResize: true,
      });

      // 对外暴露的刷新方法
      const reload = () => {
        reloadTable();
      };

      function handleAddClick() {
        emit('add');
      }

      function handleEdit(record: Recordable) {
        emit('edit', record);
      }

      async function handleToggleStatus(record: Recordable) {
        try {
          setLoading(true);
          const newStatus = record.status === '0' ? '1' : '0';

          await updateTemplateStatus(record.id, newStatus);

          createMessage.success(`${newStatus === '0' ? '启用' : '禁用'}成功`);
          reloadTable();
        } catch (error: any) {
          createMessage.error(`操作失败: ${error.message || '未知错误'}`);
        } finally {
          setLoading(false);
        }
      }

      async function handleDelete(record: Recordable) {
        try {
          setLoading(true);
          await deleteVoiceTemplate({ id: record.id });
          createMessage.success('删除成功');
          reloadTable();
        } catch (error: any) {
          createMessage.error(`删除失败: ${error.message || '未知错误'}`);
        } finally {
          setLoading(false);
        }
      }

      // 处理表单提交
      function handleSubmit() {
        const values = getFieldsValue();
        Object.assign(searchParams, values);
        reloadTable();
      }

      // 处理表单重置
      function handleReset() {
        // 重置搜索参数
        searchParams.templateName = '';
        searchParams.templateCode = '';
        searchParams.status = '';

        // 使用nextTick确保DOM更新后再触发表格刷新
        nextTick(() => {
          reloadTable();
        });
      }

      // 初始化时加载数据
      onMounted(() => {
        reloadTable();
      });

      return {
        registerTable,
        registerForm,
        handleAddClick,
        handleEdit,
        handleToggleStatus,
        handleDelete,
        handleSubmit,
        handleReset,
        reload, // 暴露刷新方法给父组件使用
      };
    },
  });
</script>

<style lang="less" scoped>
  :deep(.ant-table-wrapper) {
    overflow: hidden;
  }

  .search-form {
    margin-bottom: 16px;
    background-color: #fff;
    border-radius: 5px;
  }

  .voice-template-table {
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  :deep(.jeesite-basic-form) {
    padding-top: 15px;
  }
</style>
