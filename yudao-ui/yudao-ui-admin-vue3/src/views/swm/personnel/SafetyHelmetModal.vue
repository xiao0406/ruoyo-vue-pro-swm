<!--
  @author Shawn
  @date 2025-05-31
-->
<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    title="绑定安全帽"
    @ok="handleSubmit"
    :width="700"
  >
    <div class="safety-helmet-modal">
      <div class="person-info">
        <div class="info-item">
          <span class="label">工人姓名：</span>
          <span class="value">{{ personName }}</span>
        </div>
        <div class="info-item">
          <span class="label">人员类型：</span>
          <span class="value">
            <DictLabel dictType="person_type_enum" :dictValue="personType" />
          </span>
        </div>
      </div>

      <!-- 已绑定安全帽信息区域 -->
      <div v-if="currentHelmet" class="bound-helmet-info">
        <div class="info-title">当前已绑定的安全帽</div>
        <div class="helmet-details">
          <div class="helmet-detail-item">
            <span class="detail-label">设备号：</span>
            <span class="detail-value">{{ currentHelmet.deviceId }}</span>
          </div>
          <div class="helmet-detail-item">
            <span class="detail-label">类型：</span>
            <span class="detail-value">
              <DictLabel dictType="helmet_type_enum" :dictValue="currentHelmet.helmetType" />
            </span>
          </div>
        </div>
      </div>

      <div v-else class="bound-helmet-info">
        <div class="info-title">当前已绑定的安全帽</div>
        <div class="helmet-details">
          <div class="helmet-detail-item">
            <span class="detail-label">设备号：</span>
            <span class="detail-value">暂无</span>
          </div>
          <div class="helmet-detail-item">
            <span class="detail-label">类型：</span>
            <span class="detail-value"> 暂无 </span>
          </div>
        </div>
      </div>

      <div class="helmet-table-container">
        <div class="table-header">
          <div class="title">选择安全帽</div>
          <div class="search">
            <a-input-search
              v-model:value="searchKeyword"
              placeholder="搜索安全帽编号"
              style="width: 200px"
              @search="handleSearch"
            />
          </div>
        </div>

        <BasicTable
          @register="registerTable"
          rowKey="id"
          :selectedRowKeys="selectedRowKeys"
          :loading="tableLoading"
        >
          <template #helmetType="{ record }">
            <DictLabel dictType="helmet_type_enum" :dictValue="record.helmetType" />
          </template>
        </BasicTable>
      </div>
    </div>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, ref, unref, onMounted } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicTable, useTable } from '@/components/swm/Table';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { bindSafetyHelmet, createSafetyHelmetOrder } from '@/api/swm/person';
  import { findAvailableHelmets, unassignPerson, getByDeviceId } from '@/api/swm/helmetDevice';
  import { defHttp } from '@/utils/http/axios';
  import type { TableRowSelection } from 'ant-design-vue/lib/table/interface';
  import { DictLabel, useDict } from '@/components/swm/Dict';
  import { Modal } from 'ant-design-vue';

  // 定义安全帽记录类型
  interface HelmetRecord {
    id: string;
    code: string;
    type: string;
  }

  export default defineComponent({
    name: 'SafetyHelmetModal',
    components: { BasicModal, BasicTable, DictLabel },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const personId = ref('');
      const personName = ref('');
      const personType = ref('');
      const selectedHelmet = ref<string>('');
      const selectedRowKeys = ref<string[]>([]);
      const searchKeyword = ref('');
      const tableLoading = ref(false);
      const currentHelmet = ref<any>(null); // 当前绑定的安全帽信息
      const { createMessage } = useMessage();

      // 表格注册
      const [registerTable, { reload, setTableData, getDataSource, clearSelectedRowKeys }] = useTable({
        title: '安全帽列表',
        columns: [
          {
            title: '编号',
            dataIndex: 'deviceId',
            width: 100,
          },
          {
            title: '类型',
            dataIndex: 'helmetType',
            width: 100,
            slots: { customRender: 'helmetType' },
          },
        ],
        dataSource: [],
        canResize: false,
        pagination: false,
        showIndexColumn: true,
        bordered: true,
        showTableSetting: false,
        useSearchForm: false,
        rowSelection: {
          type: 'radio',
          onChange: onSelectChange,
        },
        rowClassName: (record: any) => {
          return '';
        },
      });

      // 初始化字典
      const { initDict } = useDict();
      onMounted(() => {
        initDict(['helmet_type_enum', 'person_type_enum']);
      });

      // 从后台获取可用的安全帽
      async function fetchAvailableHelmets(keyword?: string) {
        try {
          const result = await findAvailableHelmets(keyword);
          if (result && Array.isArray(result)) {
            // 格式化数据，使其与前端表格结构匹配
            return result.map((item) => ({
              ...item,
              id: item.id || '',
              code: item.deviceId || '',
              type: item.helmetType || '',
            }));
          }
          return [];
        } catch (error) {
          console.error('获取安全帽列表失败', error);

          // 生成一些临时数据以便界面可用
          // 注意：这仅在开发或测试环境中使用，生产环境应删除
          console.warn('使用模拟数据作为备选');
          const mockData = [
            { id: 'SH001', deviceId: 'SH001', helmetType: '标准型' },
            { id: 'SH002', deviceId: 'SH002', helmetType: '标准型' },
            { id: 'SH003', deviceId: 'SH003', helmetType: '轻型' },
          ];

          if (keyword) {
            const keywordLower = keyword.toLowerCase();
            return mockData.filter(
              (item) =>
                item.deviceId.toLowerCase().includes(keywordLower) ||
                item.helmetType.toLowerCase().includes(keywordLower),
            );
          }

          return mockData;
        }
      }

      // 获取安全帽详细信息
      async function getHelmetDetails(helmetId) {
        if (!helmetId) return null;

        try {
          // 使用安全帽ID获取详细信息
          const helmetData = await getByDeviceId(helmetId);
          if (helmetData) {
            return helmetData;
          }
          return null;
        } catch (err) {
          console.error('获取安全帽详情失败', err);
          return null;
        }
      }

      // 模态框注册
      const [registerModal, { setModalProps, closeModal }] = useModalInner(
        async (data: Recordable) => {
          // 首先重置所有状态
          selectedHelmet.value = '';
          selectedRowKeys.value = [];
          clearSelectedRowKeys(); // 清除表格选中行
          personId.value = '';
          personName.value = '';
          personType.value = '';
          searchKeyword.value = '';
          currentHelmet.value = null; // 重置当前安全帽信息

          // 停止加载状态
          setModalProps({
            confirmLoading: false,
            loading: false,
          });

          try {
            // 安全地从数据中提取属性
            if (data?.record) {
              personId.value = String(data.record.id || '');
              personName.value = String(data.record.name || '');
              personType.value = String(data.record.personType || '');

              // 只有当安全帽ID非空时才设置它
              const helmetId = data.record.safetyHelmetId;
              if (helmetId && typeof helmetId === 'string' && helmetId.trim() !== '') {
                selectedHelmet.value = helmetId;
                selectedRowKeys.value = [helmetId];

                // 获取并显示当前绑定的安全帽详情
                const helmetDetail = await getHelmetDetails(helmetId);
                if (helmetDetail) {
                  currentHelmet.value = helmetDetail;
                }
              }
            }

            // 加载安全帽列表 - 使用 setTimeout 确保 DOM 已更新
            setTimeout(() => {
              loadAvailableHelmets().catch((err) => {
                console.error('加载安全帽列表失败', err);
                createMessage.error('加载安全帽列表失败，请刷新重试');
                setTableData([]);
              });
            }, 100);
          } catch (error) {
            console.error('初始化安全帽对话框失败', error);
            createMessage.error('初始化安全帽对话框失败，请刷新重试');
            setTableData([]);
          }
        },
      );

      // 加载可用的安全帽
      async function loadAvailableHelmets() {
        try {
          tableLoading.value = true;

          // 从后台获取可用的安全帽列表
          const helmets = await fetchAvailableHelmets(searchKeyword.value);

          // 如果当前选中了安全帽，确保这个安全帽也在列表里
          if (selectedHelmet.value) {
            const helmetExists = helmets.some((item) => item.id === selectedHelmet.value);
            if (!helmetExists) {
              // 如果选中的安全帽不在列表中，要添加它
              try {
                const selectedHelmetData = await defHttp.get({
                  url: '/swm/swmHelmetDevice/get',
                  params: { id: selectedHelmet.value },
                });
                if (selectedHelmetData) {
                  helmets.unshift({
                    ...selectedHelmetData,
                    code: selectedHelmetData.deviceId || '',
                    type: selectedHelmetData.helmetType || '',
                  });
                }
              } catch (err) {
                console.error('获取已选安全帽数据失败', err);
              }
            }
          }

          // 确保选中的安全帽在列表的最前面
          if (selectedHelmet.value) {
            const selectedHelmetIndex = helmets.findIndex(
              (item) => item.id === selectedHelmet.value,
            );
            if (selectedHelmetIndex > 0) {
              const selectedItem = helmets.splice(selectedHelmetIndex, 1)[0];
              helmets.unshift(selectedItem);
            }
          }

          setTableData(helmets);
        } catch (error) {
          console.error('加载安全帽列表失败', error);
          createMessage.error('加载安全帽列表失败，请刷新重试');
          setTableData([]);
        } finally {
          tableLoading.value = false;
        }
      }

      // 选择行变化
      function onSelectChange(keys) {
        selectedRowKeys.value = Array.isArray(keys) ? keys : [];
        selectedHelmet.value = selectedRowKeys.value.length > 0 ? selectedRowKeys.value[0] : '';

        // 如果选中了行，同时记录deviceId
        if (selectedHelmet.value) {
          const tableData = getDataSource();
          const selectedRow = tableData.find((item) => item.id === selectedHelmet.value);
          if (selectedRow) {
            console.log('已选择安全帽:', selectedRow.deviceId);
          }
        }
      }

      // 搜索安全帽
      async function handleSearch(value) {
        try {
          tableLoading.value = true;

          // 通过API查询安全帽
          const helmets = await fetchAvailableHelmets(value);

          // 如果当前选中了安全帽，确保这个安全帽也在列表里
          if (selectedHelmet.value) {
            const helmetExists = helmets.some((item) => item.id === selectedHelmet.value);
            if (!helmetExists) {
              // 如果选中的安全帽不在列表中，要添加它
              try {
                const selectedHelmetData = await defHttp.get({
                  url: '/swm/swmHelmetDevice/get',
                  params: { id: selectedHelmet.value },
                });
                if (selectedHelmetData) {
                  helmets.unshift({
                    ...selectedHelmetData,
                    code: selectedHelmetData.deviceId || '',
                    type: selectedHelmetData.helmetType || '',
                  });
                }
              } catch (err) {
                console.error('获取已选安全帽数据失败', err);
              }
            }
          }

          // 确保选中的安全帽在列表的最前面
          if (selectedHelmet.value) {
            const selectedHelmetIndex = helmets.findIndex(
              (item) => item.id === selectedHelmet.value,
            );
            if (selectedHelmetIndex > 0) {
              const selectedItem = helmets.splice(selectedHelmetIndex, 1)[0];
              helmets.unshift(selectedItem);
            }
          }

          setTableData(helmets);
        } catch (error) {
          console.error('搜索安全帽失败', error);
          createMessage.error('搜索安全帽失败，请重试');
        } finally {
          tableLoading.value = false;
        }
      }

      // 提交表单
      async function handleSubmit() {
        // 检查必要的数据是否存在
        if (!selectedHelmet.value) {
          createMessage.warning('请选择一个安全帽');
          return Promise.resolve(false); // 阻止模态框关闭
        }

        if (!personId.value) {
          createMessage.warning('人员信息无效，请重新打开对话框');
          return Promise.resolve(false); // 阻止模态框关闭
        }

        try {
          // 从表格获取选中行的数据
          const tableData = getDataSource();
          const selectedRow = tableData.find((item) => item.id === selectedHelmet.value);

          if (!selectedRow || !selectedRow.deviceId) {
            createMessage.warning('选中的安全帽信息不完整，请重新选择');
            return Promise.resolve(false);
          }

          // 检查是否是同一个安全帽
          if (currentHelmet.value && currentHelmet.value.deviceId === selectedRow.deviceId) {
            createMessage.warning('该人员已绑定此安全帽，无需再次绑定');
            return Promise.resolve(false);
          }

          // 检查是否已有绑定的安全帽
          if (currentHelmet.value) {
            // 显示确认对话框
            return new Promise((resolve) => {
              const modal = Modal.confirm({
                title: '更换安全帽确认',
                content: `该人员已绑定安全帽 ${currentHelmet.value.deviceId}，是否解除当前绑定并绑定新的安全帽 ${selectedRow.deviceId}？`,
                okText: '确认更换',
                cancelText: '取消',
                onOk: async () => {
                  modal.destroy();

                  // 执行安全帽绑定流程
                  const result = await processSafetyHelmetBinding(selectedRow);
                  resolve(result);
                },
                onCancel: () => {
                  resolve(false);
                },
              });
            });
          } else {
            // 没有已绑定的安全帽，直接执行绑定流程
            return await processSafetyHelmetBinding(selectedRow);
          }
        } catch (error) {
          console.error('安全帽绑定失败', error);
          createMessage.error('安全帽绑定失败，请重试');

          // 阻止模态框关闭
          return Promise.resolve(false);
        } finally {
          // 无论成功还是失败，都需要停止加载
          setModalProps({ confirmLoading: false });
        }
      }

      // 处理安全帽绑定流程
      async function processSafetyHelmetBinding(selectedRow) {
        try {
          // 显示加载状态
          setModalProps({ confirmLoading: true });

          // 获取人员当前已绑定的安全帽ID并解除绑定
          let personIdentityCard = '';
          try {
            const personData = await defHttp.get({
              url: '/swm/swmPerson/form',
              params: { id: personId.value },
            });

            // 保存人员身份证信息
            personIdentityCard = personData?.identityCard || '';

            // 如果有原来的安全帽，并且不是当前选择的安全帽，先解除绑定
            const currentHelmetId = personData?.safetyHelmetId;
            if (currentHelmetId && currentHelmetId !== selectedRow.deviceId) {
              console.log('解除原安全帽绑定:', currentHelmetId);
              await unassignPerson(currentHelmetId);
            }
          } catch (err) {
            console.error('获取人员信息或解绑原安全帽失败', err);
            // 继续执行，不阻止新安全帽的绑定
          }

          // 准备请求参数 - 使用deviceId而不是id
          const params = {
            personId: personId.value,
            safetyHelmetId: selectedRow.deviceId, // 使用编号而不是ID
          };

          // 调用API
          await bindSafetyHelmet(params);

          // 同时尝试调用安全帽接口，将人员与安全帽进行双向绑定
          try {
            await defHttp.post(
              {
                url: '/swm/swmHelmetDevice/assignPerson',
                params: {
                  deviceId: selectedRow.deviceId, // 使用编号
                  personId: personId.value,
                  personName: personName.value,
                  binder: personIdentityCard,
                },
              },
              { isTransformResponse: false },
            );
          } catch (err) {
            console.error('安全帽分配人员失败，但人员已更新', err);
          }

          // 创建安全帽订购记录
          try {
            // 格式化日期为 yyyy-MM-dd HH:mm:ss 格式
            const now = new Date();
            const year = now.getFullYear();
            const month = String(now.getMonth() + 1).padStart(2, '0');
            const day = String(now.getDate()).padStart(2, '0');
            const hours = String(now.getHours()).padStart(2, '0');
            const minutes = String(now.getMinutes()).padStart(2, '0');
            const seconds = String(now.getSeconds()).padStart(2, '0');
            const formattedDate = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;

            await createSafetyHelmetOrder({
              personId: personId.value,
              deviceId: selectedRow.deviceId,
              orderByPerson: personName.value || '系统管理员',
              orderDate: formattedDate, // 使用格式化后的日期
              helmetModel: selectedRow.helmetType || '',
              helmetColor: '',
              quantity: 1,
              orderStatus: '2', // 已发放状态
              receivedDate: formattedDate, // 使用格式化后的日期
              receivedSign: personName.value || '',
              bindTime: formattedDate, // 绑定时间
              usageStatus: '0', // 使用状态：0-使用中
              binder: personIdentityCard, // 绑定人员身份证
              remarks: '通过绑定安全帽自动创建的订购记录',
            });
          } catch (error) {
            console.error('创建安全帽订购记录失败', error);
            // 不阻止主流程继续
          }

          // 成功提示
          createMessage.success(
            `安全帽 ${selectedRow.deviceId} 已成功绑定给 ${personName.value || '人员'}！`,
          );

          // 关闭模态框并通知父组件
          closeModal();
          emit('success');

          // 允许模态框关闭
          return Promise.resolve(true);
        } catch (error) {
          console.error('安全帽绑定失败', error);
          createMessage.error('安全帽绑定失败，请重试');

          // 阻止模态框关闭
          return Promise.resolve(false);
        } finally {
          // 无论成功还是失败，都需要停止加载
          setModalProps({ confirmLoading: false });
        }
      }

      return {
        registerModal,
        registerTable,
        personName,
        personType,
        selectedRowKeys,
        searchKeyword,
        tableLoading,
        handleSearch,
        handleSubmit,
        onSelectChange,
        currentHelmet,
        processSafetyHelmetBinding,
      };
    },
  });
</script>

<style lang="less" scoped>
  .safety-helmet-modal {
    display: flex;
    flex-direction: column;
    height: 100%;
    padding: 0 30px;

    .person-info {
      display: flex;
      margin-bottom: 16px;
      padding: 12px;
      background-color: #f5f5f5;
      border-radius: 4px;

      .info-item {
        margin-right: 24px;
        display: flex;

        .label {
          font-weight: bold;
          margin-right: 8px;
        }

        .value {
          color: #1890ff;
        }
      }
    }

    /* 已绑定安全帽信息区域样式 */
    .bound-helmet-info {
      margin-bottom: 16px;
      padding: 12px;
      background-color: #fff1f0;
      /* 浅红色背景 */
      border: 1px solid #ffccc7;
      /* 红色边框 */
      border-radius: 4px;

      .info-title {
        font-size: 14px;
        font-weight: bold;
        color: #cf1322;
        /* 红色文字 */
        margin-bottom: 8px;
      }

      .helmet-details {
        display: flex;
        flex-wrap: wrap;

        .helmet-detail-item {
          margin-right: 24px;
          display: flex;
          margin-bottom: 4px;

          .detail-label {
            font-weight: bold;
            margin-right: 8px;
          }

          .detail-value {
            color: #1890ff;
          }
        }
      }
    }

    .helmet-table-container {
      flex: 1;

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
