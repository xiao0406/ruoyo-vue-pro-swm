<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    @ok="handleSubmit"
    :width="800"
  >
    <div style="padding: 20px 20px">
      <div class="config-content">
        <div class="name-input mb-4">
          <span class="mr-2">名称</span>
          <a-input v-model:value="formModel.name" placeholder="请输入名称" style="width: 300px" />
        </div>

        <div class="subitem-table">
          <div class="flex justify-between mb-2 items-center">
            <div class="table-title">子项列表</div>
          </div>

          <a-table
            :dataSource="subItems"
            :columns="columns"
            :pagination="false"
            rowKey="id"
            bordered
            size="small"
            :loading="loading"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.key === 'color'">
                <div class="color-selector-wrapper">
                  <ColorPicker
                    :hex="record?.color || 'FF0000'"
                    @colorHex="(e) => handleSubitemColorChange(e, index)"
                    class="color-picker-inline"
                  />
                </div>
              </template>
            </template>
          </a-table>
        </div>
      </div>
    </div>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, ref, computed, unref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { ColorPicker } from '@/components/swm/ColorPicker';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { Input, Table, Button, Modal, Form } from 'ant-design-vue';
  import {
    getHelmetSubitemsByParentId,
    saveHelmetSubitem,
    saveHelmetConfig,
    getWorkshopData,
    getWorkshopLineGroupData,
    getPersonTypeEnumData,
    getWorkTypeEnumData,
  } from '@/api/swm/helmetConfig';

  export default defineComponent({
    name: 'HelmetsColorModal',
    components: {
      BasicModal,
      ColorPicker,
      'a-input': Input,
      'a-table': Table,
      'a-button': Button,
      'a-modal': Modal,
      'a-form': Form,
      'a-form-item': Form.Item,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { createMessage } = useMessage();
      const isUpdate = ref(false);
      const rowId = ref('');
      const formModel = reactive({
        name: '',
        count: 0,
      });

      // 子项相关数据
      const subItems = ref<any[]>([]);
      const loading = ref(false);

      const columns = [
        {
          title: '子项名称',
          dataIndex: 'subitemName',
          key: 'name',
          width: '70%',
        },
        {
          title: '颜色',
          dataIndex: 'color',
          key: 'color',
          width: '30%',
        },
      ];

      // 处理子项颜色变化
      function handleSubitemColorChange(e: any, index: number) {
        if (e && e.hex && subItems.value[index]) {
          const color = '#' + e.hex;
          subItems.value[index].color = color;
          console.log(`子项 ${index} 颜色更新为: ${color}`);
        }
      }

      // 获取子项数据
      async function fetchSubItems(parentId: string, parentName: string) {
        if (!parentId) {
          console.warn('没有获取到parentId, 无法请求子项数据');
          return;
        }

        try {
          loading.value = true; // 开始加载，显示加载动画
          console.log('正在请求接口获取子项数据，parentId:', parentId, 'parentName:', parentName);

          // 如果是"按车间展示"，查询车间数据
          if (parentName === '按车间展示') {
            const workshopRes = await getWorkshopData();
            console.log('车间数据接口返回:', workshopRes);

            if (workshopRes && Array.isArray(workshopRes)) {
              // 查询已保存的子项颜色信息
              const savedSubItems = await getHelmetSubitemsByParentId({ parentId });
              console.log('已保存的子项颜色信息:', savedSubItems);

              // 创建映射，便于查找已保存的颜色
              const savedSubItemsMapByKey = new Map();
              const savedSubItemsMapByName = new Map();
              if (savedSubItems && Array.isArray(savedSubItems)) {
                savedSubItems.forEach((item) => {
                  if (item.key) {
                    savedSubItemsMapByKey.set(item.key, item);
                  }
                  savedSubItemsMapByName.set(item.subitemName, item);
                });
              }

              // 转换车间数据格式，如果有已保存的颜色则使用，否则使用默认颜色
              subItems.value = workshopRes.map((workshop) => {
                // 优先通过key字段查找，其次通过名称查找
                const savedItem =
                  savedSubItemsMapByKey.get(workshop.id) ||
                  savedSubItemsMapByName.get(workshop.title);
                return {
                  id: workshop.id,
                  parentId: workshop.parent_id,
                  subitemName: workshop.title,
                  color: savedItem ? savedItem.color : '#FF0000', // 使用已保存的颜色或默认颜色（红色，与人员坐标一致）
                  subitemId: savedItem ? savedItem.id : null, // 保存子项的ID用于更新
                };
              });
            } else {
              subItems.value = [];
            }
          } else if (parentName === '按班组展示') {
            // 如果是"按班组展示"，查询车间-产线-班组数据
            const workshopLineGroupRes = await getWorkshopLineGroupData();
            console.log('车间-产线-班组数据接口返回:', workshopLineGroupRes);

            if (workshopLineGroupRes && Array.isArray(workshopLineGroupRes)) {
              // 查询已保存的子项颜色信息
              const savedSubItems = await getHelmetSubitemsByParentId({ parentId });
              console.log('已保存的子项颜色信息:', savedSubItems);

              // 创建映射，便于查找已保存的颜色
              const savedSubItemsMapByKey = new Map();
              const savedSubItemsMapByName = new Map();
              if (savedSubItems && Array.isArray(savedSubItems)) {
                savedSubItems.forEach((item) => {
                  if (item.key) {
                    savedSubItemsMapByKey.set(item.key, item);
                  }
                  savedSubItemsMapByName.set(item.subitemName, item);
                });
              }

              // 转换车间-产线-班组数据格式，如果有已保存的颜色则使用，否则使用默认颜色
              subItems.value = workshopLineGroupRes.map((workGroup) => {
                // 优先通过key字段查找，其次通过名称查找
                const savedItem =
                  savedSubItemsMapByKey.get(workGroup.id) ||
                  savedSubItemsMapByName.get(workGroup.title);
                return {
                  id: workGroup.id,
                  parentId: parentId,
                  subitemName: workGroup.title, // 使用拼接后的名称
                  color: savedItem ? savedItem.color : '#FF0000', // 使用已保存的颜色或默认颜色（红色，与人员坐标一致）
                  subitemId: savedItem ? savedItem.id : null, // 保存子项的ID用于更新
                };
              });
            } else {
              subItems.value = [];
            }
          } else if (parentName === '按人员类型展示') {
            // 如果是"按人员类型展示"，查询人员类型字典数据
            const personTypeRes = await getPersonTypeEnumData();
            console.log('人员类型字典数据接口返回:', personTypeRes);

            if (personTypeRes && Array.isArray(personTypeRes)) {
              // 查询已保存的子项颜色信息
              const savedSubItems = await getHelmetSubitemsByParentId({ parentId });
              console.log('已保存的子项颜色信息:', savedSubItems);

              // 创建映射，便于查找已保存的颜色
              const savedSubItemsMapByKey = new Map();
              const savedSubItemsMapByName = new Map();
              if (savedSubItems && Array.isArray(savedSubItems)) {
                savedSubItems.forEach((item) => {
                  if (item.key) {
                    savedSubItemsMapByKey.set(item.key, item);
                  }
                  savedSubItemsMapByName.set(item.subitemName, item);
                });
              }

              // 转换人员类型字典数据格式，如果有已保存的颜色则使用，否则使用默认颜色
              subItems.value = personTypeRes.map((personType) => {
                // 优先通过key字段查找，其次通过名称查找
                const savedItem =
                  savedSubItemsMapByKey.get(personType.id) ||
                  savedSubItemsMapByName.get(personType.title);
                return {
                  id: personType.id, // 使用字典的value值作为id
                  parentId: parentId,
                  subitemName: personType.title, // 使用字典的label作为名称
                  color: savedItem ? savedItem.color : '#FF0000', // 使用已保存的颜色或默认颜色（红色，与人员坐标一致）
                  subitemId: savedItem ? savedItem.id : null, // 保存子项的ID用于更新
                };
              });
            } else {
              subItems.value = [];
            }
          } else if (parentName === '按工种展示') {
            // 如果是"按工种展示"，查询工种字典数据
            const workTypeRes = await getWorkTypeEnumData();
            console.log('工种字典数据接口返回:', workTypeRes);

            if (workTypeRes && Array.isArray(workTypeRes)) {
              // 查询已保存的子项颜色信息
              const savedSubItems = await getHelmetSubitemsByParentId({ parentId });
              console.log('已保存的子项颜色信息:', savedSubItems);

              // 创建映射，便于查找已保存的颜色
              const savedSubItemsMapByKey = new Map();
              const savedSubItemsMapByName = new Map();
              if (savedSubItems && Array.isArray(savedSubItems)) {
                savedSubItems.forEach((item) => {
                  if (item.key) {
                    savedSubItemsMapByKey.set(item.key, item);
                  }
                  savedSubItemsMapByName.set(item.subitemName, item);
                });
              }

              // 转换工种字典数据格式，如果有已保存的颜色则使用，否则使用默认颜色
              subItems.value = workTypeRes.map((workType) => {
                // 优先通过key字段查找，其次通过名称查找
                const savedItem =
                  savedSubItemsMapByKey.get(workType.id) ||
                  savedSubItemsMapByName.get(workType.title);
                return {
                  id: workType.id, // 使用字典的label作为id
                  parentId: parentId,
                  subitemName: workType.title, // 使用字典的label作为名称
                  color: savedItem ? savedItem.color : '#FF0000', // 使用已保存的颜色或默认颜色（红色，与人员坐标一致）
                  subitemId: savedItem ? savedItem.id : null, // 保存子项的ID用于更新
                };
              });
            } else {
              subItems.value = [];
            }
          } else {
            // 其他情况使用原有逻辑
            const res = await getHelmetSubitemsByParentId({ parentId });
            console.log('接口返回数据:', res);

            if (res && Array.isArray(res)) {
              // 确保每个子项都有 subitemId 字段
              subItems.value = res.map((item) => ({
                ...item,
                subitemId: item.id, // 保存子项的ID用于更新
              }));
            } else {
              subItems.value = [];
            }
          }
        } catch (error) {
          console.error('获取子项数据失败', error);
          createMessage.error('获取子项数据失败');
          subItems.value = [];
        } finally {
          loading.value = false; // 结束加载，隐藏加载动画
        }
      }

      // 初始化模态窗数据
      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        resetForm();
        setModalProps({ confirmLoading: false });
        isUpdate.value = !!data?.isUpdate;

        if (unref(isUpdate)) {
          rowId.value = data.record.id;
          formModel.name = data.record.name;
          formModel.count = data.record.count;

          // 立即请求接口获取子项数据
          console.log('编辑模式，即将请求子项数据，ID:', data.record.id, 'name:', data.record.name);
          fetchSubItems(data.record.id, data.record.name);
        } else {
          subItems.value = [];
        }
      });

      function resetForm() {
        formModel.name = '';
        formModel.count = 0;
        subItems.value = [];
      }

      const getTitle = computed(() => (!unref(isUpdate) ? '新增安全帽配置' : '编辑安全帽配置'));

      async function handleSubmit() {
        try {
          if (!formModel.name) {
            createMessage.warning('请输入名称');
            return;
          }

          setModalProps({ confirmLoading: true });

          // 准备数据
          const data = {
            ...formModel,
            id: unref(isUpdate) ? rowId.value : undefined,
            count: subItems.value.length, // 根据子项数量设置子项数量
          };

          // 调用API保存配置
          const result = await saveHelmetConfig(data);
          console.log('保存安全帽配置结果:', result);

          // 保存子项颜色到 swm_helmet_subitem 表
          const currentParentId = unref(isUpdate) ? rowId.value : result?.id;

          if (currentParentId && subItems.value.length > 0) {
            // 保存或更新所有子项的颜色
            const savePromises = subItems.value.map((item) => {
              const saveData: any = {
                parentId: currentParentId,
                subitemName: item.subitemName,
                color: item.color,
                key: item.id, // 保存车间原始ID到key字段
              };

              // 如果存在子项ID，则添加到请求数据中进行更新
              if (item.subitemId) {
                saveData.id = item.subitemId;
              }

              return saveHelmetSubitem(saveData);
            });

            const results = await Promise.all(savePromises);

            // 更新子项的ID（用于下次更新时使用）
            results.forEach((result, index) => {
              if (result && result.id && !subItems.value[index].subitemId) {
                subItems.value[index].subitemId = result.id;
              }
            });

            console.log('子项颜色保存完成');
          }

          closeModal();
          createMessage.success(`${unref(isUpdate) ? '编辑' : '新增'}安全帽配置成功`);

          emit('success', {
            isUpdate: unref(isUpdate),
            values: {
              ...data,
              id: rowId.value,
              subItems: subItems.value,
            },
          });
        } catch (error) {
          console.error('保存安全帽配置失败', error);
          createMessage.error('保存安全帽配置失败，请重试');
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      return {
        registerModal,
        getTitle,
        handleSubmit,
        formModel,
        subItems,
        columns,
        fetchSubItems,
        loading,
        handleSubitemColorChange,
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

  .color-preview {
    width: 30px;
    height: 30px;
    border-radius: 4px;
    border: 1px solid #ddd;
    display: inline-block;
    margin-right: 10px;
  }

  .color-selector-wrapper {
    display: flex;
    align-items: center;
    justify-content: center;

    .color-picker-inline {
      :deep(.color_selector) {
        width: 30px;
        height: 30px;
        border-radius: 4px;
        cursor: pointer;
      }

      :deep(.vc-sketch) {
        position: fixed !important;
        top: 50% !important;
        left: 50% !important;
        transform: translate(-50%, -50%) !important;
        z-index: 10000 !important;
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3) !important;
        border-radius: 8px !important;
      }
    }
  }

  .table-title {
    font-weight: 500;
    font-size: 16px;
  }

  .color-input-wrapper {
    display: flex;
    flex-direction: column;
  }

  /* 确保颜色选择器弹出层始终在最上层 */
  :deep(.vc-sketch-container),
  :deep(.vc-container),
  :deep(.vc-sketch),
  :deep(.vc-popover) {
    z-index: 10000 !important;
  }

  /* 添加覆盖整个屏幕的遮罩层 */
  :deep(.vc-popover-content-wrapper) {
    position: fixed !important;
    top: 0 !important;
    left: 0 !important;
    width: 100vw !important;
    height: 100vh !important;
    display: flex !important;
    justify-content: center !important;
    align-items: center !important;
    z-index: 9999 !important;
    background-color: rgba(0, 0, 0, 0.5) !important;
  }
</style>
