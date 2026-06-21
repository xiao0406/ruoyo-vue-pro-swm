<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="'人员排班'"
    @ok="handleSubmit"
    @cancel="handleCancel"
    @close="handleCancel"
    :width="820"
  >
    <div v-if="fullscreenLoading" class="fullscreen-loading-container">
      <a-spin tip="数据处理中，请稍候..." size="large" />
    </div>
    <BasicForm @register="registerForm">
      <template #backActivityTable>
        <div class="transfer-box">
          <Transfer
            v-model:target-keys="targetKeys"
            class="tree-transfer"
            :data-source="leafDataSource"
            :render="renderTransferItem"
            :show-select-all="false"
            :titles="['请选择人员', `已选 ${targetKeys.length} 人`]"
            :operations="['添加', '移除']"
            @change="handleTransferChange"
            :list-style="{
              width: '45%',
              height: '400px',
            }"
          >
            <template #children="{ direction, selectedKeys, onItemSelect }">
              <Tree
                v-if="direction === 'left' && treeVisible"
                ref="treeRef"
                block-node
                checkable
                showIcon
                :checked-keys="getCheckedKeys([...selectedKeys], [...targetKeys])"
                :tree-data="treeData"
                @check="
                  (checkedKeys, { node, checked }) => {
                    console.log('树节点选择状态变化:', node.key, checked);
                    onChecked({ node, checked }, [...selectedKeys, ...targetKeys], onItemSelect);
                  }
                "
                @select="
                  (selectedKeys, { node }) => {
                    // 点击节点文字时直接触发check事件
                    const nodeKey = node.key as string;
                    const isSelected = targetKeys.indexOf(nodeKey) > -1;
                    // 将Key[]类型转换为string[]类型
                    const stringSelectedKeys = selectedKeys.map(k => String(k));
                    onChecked({ node, checked: !isSelected },
                      [...stringSelectedKeys, ...targetKeys], onItemSelect);
                  }
                "
                :expandedKeys="expandedKeys"
                @expand="onExpand"
                :showLine="{ showLeafIcon: false }"
              >
                >
                <template #title="{ key, title, nodeType, idCard }">
                  <span :title="nodeType === 'worker' && idCard ? idCard : title">{{ title }}</span>
                </template>
              </Tree>
            </template>
          </Transfer>
        </div>
      </template>
    </BasicForm>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, onMounted, h, nextTick } from 'vue';
  import { Transfer, Tree, Spin } from 'ant-design-vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicForm, useForm } from '@/components/swm/Form/index';
  import { formScheduling } from './AttendanceData';
  import { classesEnum } from '@/enums/swm/attendanceEnum';
  import {
    getStaffSchedule,
    batchSaveStaffSchedule,
    getStaffScheduleList,
  } from '@/api/swm/staffSchedule';
  import { useMessage } from '@/hooks/swm/useMessage';
  import {
    FolderOutlined,
    AppstoreOutlined,
    TeamOutlined,
    UserOutlined,
    SettingOutlined,
  } from '@ant-design/icons-vue';
  // 导入组织树API
  import { fetchOrgTreeData } from '@/api/swm/organizationTree';
  import { getPersonsByNodeType, batchGetPersonsByNodeTypes } from '@/api/swm/staffScheduling'; // 导入批量获取API

  // 定义人员数据类型
  interface PersonNode {
    key: string;
    title: string;
    disabled?: boolean;
    isLeaf?: boolean;
    children?: PersonNode[];
    idCard?: string; // 身份证号码字段
    nodeType?: string; // 节点类型
    id?: string; // 节点ID
  }

  export default defineComponent({
    name: 'StaffModal',
    components: { BasicModal, BasicForm, Transfer, Tree, Spin },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      // 人员数据源
      const tData = ref<PersonNode[]>([]);
      const transferDataSource = ref<PersonNode[]>([]);
      const treeRef = ref(null);
      const treeVisible = ref(true); // 控制树组件的可见性

      // 加载人员数据
      async function loadPersonData() {
        try {
          console.log('开始加载组织树根节点数据...');
          // 使用API动态获取组织树数据
          const result = await fetchOrgTreeData('root');
          if (result && Array.isArray(result)) {
            console.log('成功获取根节点数据:', result);

            // 为顶级节点添加isLeaf属性，只有worker类型是叶子节点
            const processedResult = result.map((node) => {
              const isLeaf = node.nodeType === 'worker';
              console.log(`处理节点: ${node.title}, 类型: ${node.nodeType}, isLeaf: ${isLeaf}`);

              return {
                ...node,
                isLeaf: isLeaf,
                idCard: node.idCard || '', // 保留idCard字段，如果有的话
              };
            });

            tData.value = processedResult;
            console.log('处理后的组织树数据:', tData.value);

            // 扁平化处理
            transferDataSource.value = [];
            flatten(JSON.parse(JSON.stringify(tData.value)));

            // 确保树组件可见
            treeVisible.value = true;
          } else {
            console.error('加载组织树数据格式不正确');
          }
        } catch (error) {
          console.error('加载人员数据失败', error);
        }
      }

      function flatten(list: PersonNode[] = []) {
        if (list.length > 0) {
          list.forEach((item) => {
            // 判断是否为叶子节点（没有子节点的即为叶子节点/人员）
            const isLeafNode = !item.children || item.children.length === 0;

            // 添加当前节点到数据源
            transferDataSource.value.push({
              key: item.key,
              title: item.title,
              // 只有叶子节点可以被传输到右侧
              disabled: false,
              // 标记是否为叶子节点（用于区分人员和组织结构）
              isLeaf: isLeafNode,
              // 保存身份证号码
              idCard: item.idCard || '',
            });

            // 递归处理子节点
            if (item.children && item.children.length > 0) {
              flatten(item.children);
            }
          });
        }
      }

      function isChecked(selectedKeys: string[], eventKey: string) {
        return selectedKeys.indexOf(eventKey) !== -1;
      }

      // 获取所有应该被选中的键（包括用户选择的和因子节点全选而应该选中的父节点）
      function getCheckedKeys(selectedKeys: string[], targetKeys: string[]) {
        // 合并用户选择的键
        const allKeys = [...selectedKeys, ...targetKeys];

        // 添加所有子节点全部被选中的父节点
        const extraKeys: string[] = [];

        // 遍历所有组织节点
        function checkParentNodes(nodes: PersonNode[]) {
          for (const node of nodes) {
            if (node.children && node.children.length > 0) {
              // 检查该节点是否所有叶子节点都被选中
              if (areAllChildrenSelected(node, allKeys)) {
                extraKeys.push(node.key);
              }
              // 递归检查子节点
              checkParentNodes(node.children);
            }
          }
        }

        // 开始从根节点检查
        checkParentNodes(tData.value);

        // 合并所有要选中的键
        return [...allKeys, ...extraKeys];
      }

      // 检查节点的所有子节点是否都被选中
      function areAllChildrenSelected(node: PersonNode, targetKeys: string[]): boolean {
        if (!node.children || node.children.length === 0) {
          return false;
        }

        // 检查所有叶子节点是否都被选中
        const leafNodes: string[] = [];

        // 递归查找所有叶子节点
        function findAllLeafNodes(nodes: PersonNode[]) {
          for (const node of nodes) {
            if (!node.children || node.children.length === 0) {
              leafNodes.push(node.key);
            } else if (node.children) {
              findAllLeafNodes(node.children);
            }
          }
        }

        findAllLeafNodes(node.children);

        // 检查是否所有叶子节点都在targetKeys中
        return leafNodes.every((key) => targetKeys.indexOf(key) !== -1);
      }

      function handleTreeData(treeNodes: PersonNode[], targetKeys: string[] = []) {
        if (treeNodes.length > 0) {
          return treeNodes.map(({ children, ...props }) => {
            // 检查本节点是否在targetKeys中
            const isSelected = targetKeys.indexOf(props.key) !== -1;

            // 检查所有子节点是否都被选择
            const childrenSelected =
              children && children.length > 0
                ? areAllChildrenSelected(
                    { key: props.key, title: props.title, children },
                    targetKeys,
                  )
                : false;

            // 如果所有子节点都被选中，将当前节点也标记为选中
            const shouldBeSelected = isSelected || childrenSelected;

            const processedChildren = children ? handleTreeData(children, targetKeys) : undefined;

            const icon = () => {
              // 根据节点类型返回不同的图标
              if (props.key.startsWith('factory')) {
                return h(AppstoreOutlined);
              } else if (props.key.startsWith('workshop')) {
                return h(FolderOutlined);
              } else if (props.key.startsWith('process')) {
                return h(SettingOutlined);
              } else if (props.key.startsWith('team')) {
                return h(TeamOutlined);
              } else {
                return h(UserOutlined);
              }
            };

            return {
              ...props,
              disabled: false, // 不禁用节点，允许选择
              icon,
              children: processedChildren,
              // 如果所有子节点都被选中，将此属性添加到节点上，供后续使用
              _allChildrenSelected: childrenSelected,
            };
          });
        }
        return [];
      }

      const targetKeys = ref<string[]>([]);
      const dataSource = computed(() => transferDataSource.value);

      // 只包含叶子节点的数据源，用于计算可选人员数
      const leafDataSource = computed(() => {
        // 过滤出所有叶子节点
        const nodes = transferDataSource.value.filter((item) => {
          // 判断是否为叶子节点
          const node = findNodeByKey(tData.value, item.key);
          // 检查是否是leafNode或者nodeType为worker
          return (
            (node && (!node.children || node.children.length === 0)) ||
            item.nodeType === 'worker' ||
            item.isLeaf === true
          );
        });
        console.log('当前叶子节点数据源:', nodes);
        return nodes;
      });

      const expandedKeys = ref<string[]>([]); // 默认展开的节点

      // 处理树节点展开/折叠事件
      const onExpand = async (expanded: string[], info: any) => {
        console.log('树节点展开/折叠:', expanded);
        // 更新展开的节点
        expandedKeys.value = expanded;

        // 仅在展开节点时加载子节点
        if (info.expanded) {
          // 获取当前展开的节点
          const node = info.node;
          if (!node) return;

          const nodeKey = node.key as string;
          const dataRef = node.dataRef || {}; // 确保dataRef不为undefined
          console.log('正在加载节点的子节点:', nodeKey, '节点数据:', dataRef);

          // 添加加载中状态
          const loadingKey = `${nodeKey}_loading`;
          const loadingIndicator = h(
            'span',
            { style: { marginLeft: '8px', color: '#1890ff' } },
            '加载中...',
          );

          try {
            // 获取节点数据引用
            const dataRef = node.dataRef || {};

            // 根据节点类型和ID加载子节点
            const nodeType = dataRef.nodeType || 'unknown';
            const nodeId = dataRef.id || nodeKey;

            console.log(`发起API请求: fetchOrgTreeData(${nodeType}, ${nodeId})`);

            // 调用API获取子节点，传递正确的参数
            const children = await fetchOrgTreeData(nodeType, nodeId);
            console.log(`API请求成功，获取到${children?.length || 0}个子节点`);

            // 处理返回的子节点数据，标记子节点是否为叶子节点
            const processedChildren =
              children?.map((child) => ({
                ...child,
                isLeaf: child.nodeType === 'worker', // 只有worker类型是叶子节点
              })) || [];

            console.log('获取到子节点数据:', processedChildren);

            // 保存当前已选人员数据
            const currentSelectedKeys = [...targetKeys.value];

            // 更新树数据
            if (processedChildren.length > 0) {
              updateTreeNodeChildren(tData.value, nodeKey, processedChildren);
              console.log('更新后的树数据:', tData.value);

              // 保存现有的transferDataSource中已选人员数据
              const selectedPersons = transferDataSource.value.filter((item) =>
                currentSelectedKeys.includes(item.key),
              );

              // 重新处理扁平化数据源
              transferDataSource.value = [];
              flatten(JSON.parse(JSON.stringify(tData.value)));

              // 确保已选人员数据不会丢失
              selectedPersons.forEach((person) => {
                // 检查是否已存在于新的数据源中
                const existingIndex = transferDataSource.value.findIndex(
                  (item) => item.key === person.key,
                );

                // 如果不存在，则添加回数据源
                if (existingIndex === -1) {
                  transferDataSource.value.push(person);
                } else {
                  // 如果存在，更新其属性（特别是idCard）
                  transferDataSource.value[existingIndex].idCard = person.idCard;
                }
              });

              // 确保targetKeys保持不变
              targetKeys.value = currentSelectedKeys;
            } else {
              console.log('该节点没有子节点');
            }
          } catch (error) {
            console.error('加载子节点数据失败', error);
          }
        }
      };

      // 更新树节点的子节点
      function updateTreeNodeChildren(nodes: PersonNode[], key: string, children: PersonNode[]) {
        for (let i = 0; i < nodes.length; i++) {
          if (nodes[i].key === key) {
            nodes[i].children = children;
            return true;
          }

          if (nodes[i].children && nodes[i].children.length > 0) {
            if (updateTreeNodeChildren(nodes[i].children || [], key, children)) {
              return true;
            }
          }
        }

        return false;
      }

      // 获取节点类型，用于确定API请求参数
      function getNodeType(node: any) {
        if (!node) return 'root';

        // 获取节点类型
        const nodeType = node.nodeType || '';
        const parentId = node.parentId || '';

        // 根据节点层级判断类型
        if (!parentId || parentId === '0') {
          // 厂间是最高级父节点，其parentId为"0"或为空
          return 'office'; // 返回厂间类型，加载其下属车间
        } else if (nodeType === 'office') {
          return 'office'; // 厂间下是车间
        } else if (nodeType === 'workshop') {
          return 'workshop'; // 车间下是产线
        } else if (nodeType === 'prodLine') {
          return 'prodLine'; // 产线下是班组
        } else if (nodeType === 'workGroup') {
          return 'workGroup'; // 班组下是人员
        }
        return 'unknown';
      }

      const treeData = computed(() => {
        return handleTreeData(tData.value, targetKeys.value);
      });

      // 递归获取所有子节点的key
      function getAllChildrenKeys(node: PersonNode): string[] {
        let keys: string[] = [node.key];

        if (node.children && node.children.length > 0) {
          node.children.forEach((child) => {
            keys = keys.concat(getAllChildrenKeys(child));
          });
        }

        return keys;
      }

      // 根据key查找节点
      function findNodeByKey(nodes: PersonNode[], key: string): PersonNode | null {
        for (const node of nodes) {
          if (node.key === key) {
            return node;
          }

          if (node.children && node.children.length > 0) {
            const foundNode = findNodeByKey(node.children, key);
            if (foundNode) return foundNode;
          }
        }

        return null;
      }

      const onChecked = (
        e: any,
        checkedKeys: string[],
        onItemSelect: (key: string, check: boolean) => void,
      ) => {
        const { eventKey } = e.node;
        const alreadySelected = isChecked(checkedKeys, eventKey);

        // 查找当前点击的节点
        const currentNode = findNodeByKey(tData.value, eventKey);
        if (!currentNode) return;

        // 如果是选择操作
        if (!alreadySelected) {
          console.log('选择节点:', eventKey);

          // 判断是否是叶子节点(人员)
          const isLeafNode = !currentNode.children || currentNode.children.length === 0;

          // 如果是叶子节点，直接选中
          if (isLeafNode) {
            onItemSelect(eventKey, true);
            return;
          }

          // 如果是非叶子节点，只选择其下的所有叶子节点
          if (currentNode.children && currentNode.children.length > 0) {
            // 获取所有叶子节点
            const leafKeys: string[] = [];

            // 递归查找所有叶子节点
            function findLeafNodes(nodes: PersonNode[]) {
              nodes.forEach((node) => {
                if (!node.children || node.children.length === 0) {
                  leafKeys.push(node.key);
                } else if (node.children) {
                  findLeafNodes(node.children);
                }
              });
            }

            if (currentNode.children) {
              findLeafNodes(currentNode.children);
            }

            // 选择所有未选中的叶子节点
            leafKeys.forEach((key) => {
              if (!isChecked(checkedKeys, key)) {
                onItemSelect(key, true);
              }
            });
          }
        } else {
          // 取消选择当前节点
          onItemSelect(eventKey, false);

          // 如果是叶子节点，直接取消选择
          const isLeafNode = !currentNode.children || currentNode.children.length === 0;
          if (isLeafNode) {
            return;
          }

          // 如果是非叶子节点，级联取消选中所有子节点
          if (currentNode.children && currentNode.children.length > 0) {
            const allChildrenKeys = getAllChildrenKeys(currentNode);

            // 去除当前节点key
            const keysToUnselect = allChildrenKeys.filter((key) => key !== eventKey);

            // 取消选中所有子节点
            keysToUnselect.forEach((key) => {
              if (isChecked(checkedKeys, key)) {
                onItemSelect(key, false);
              }
            });
          }
        }
      };

      const rowId = ref('');
      const { createMessage } = useMessage();
      const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
        labelWidth: 100,
        baseColProps: { span: 12 },
        schemas: formScheduling,
        showActionButtonGroup: false,
        actionColOptions: {
          span: 24,
        },
      });

      // 组件挂载时预加载数据
      onMounted(() => {
        // 预加载人员数据，确保打开弹窗时有数据
        loadPersonData();
      });

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        // 重置表单字段
        resetFields();
        // 重置模态框属性
        setModalProps({ confirmLoading: false });

        // 确保清空已选人员数据
        targetKeys.value = [];

        // 确保数据已加载
        if (transferDataSource.value.length === 0) {
          await loadPersonData();
        }

        // 打印当前数据状态，帮助调试
        console.log('模态框已打开，数据状态:', {
          treeData: treeData.value,
          dataSource: dataSource.value.length,
          expandedKeys: expandedKeys.value,
          targetKeys: targetKeys.value,
        });

        // 如果是编辑模式，加载排班数据
        if (data?.record?.id) {
          rowId.value = data.record.id;
          const scheduleData = await getStaffSchedule(rowId.value);

          // 设置表单值
          setFieldsValue({
            ...scheduleData,
          });

          // 设置已选人员
          if (scheduleData.personId) {
            targetKeys.value = [scheduleData.personId];
          }
        } else {
          // 新增模式，清除之前的所有数据
          rowId.value = ''; // 确保清除之前的ID

          // 强制重置表单的所有字段为初始值
          setFieldsValue({
            month: null, // 重置月份为空
            classes: classesEnum.early, // 默认设置为早班
            activityId: undefined, // 确保活动ID被清除
          });

          // 清空已选人员
          targetKeys.value = [];

          console.log('新建模式，已清空所有数据');
        }
      });

      // 全局loading状态
      const fullscreenLoading = ref(false);

      async function handleSubmit() {
        try {
          const values = await validate();

          if (!values.month) {
            createMessage.warning('请选择月份');
            return;
          }

          setModalProps({ confirmLoading: true });

          // 显示全屏加载
          fullscreenLoading.value = true;

          // 如果有ID则是更新操作
          if (rowId.value) {
            values.id = rowId.value;
          }

          // 过滤出所有的叶子节点(人员)
          const selectedLeafNodes = targetKeys.value.filter((key) => {
            const personNode = transferDataSource.value.find((item) => item.key === key);
            return personNode && personNode.isLeaf === true;
          });

          // 确保至少选择了一个人员
          if (selectedLeafNodes.length === 0) {
            createMessage.warning('请至少选择一名人员进行排班');
            setModalProps({ confirmLoading: false });
            fullscreenLoading.value = false; // 关闭全屏加载
            return;
          }

          // 检查选中的人员在所选月份是否已有排班数据
          if (!rowId.value) {
            // 只在新增模式下检查
            const personsWithSchedule: string[] = [];

            // 遍历选中的人员，检查每个人在所选月份是否已有排班
            for (const personKey of selectedLeafNodes) {
              const personNode = transferDataSource.value.find((item) => item.key === personKey);
              if (personNode && personNode.idCard) {
                // 查询该人员在所选月份是否已有排班
                const params: Record<string, any> = {
                  month: values.month,
                  idCard: personNode.idCard,
                };
                const response = await getStaffScheduleList(params);

                // 如果该人员在所选月份已有排班记录
                if (response && response.list && response.list.length > 0) {
                  personsWithSchedule.push(personNode.title);
                }
              }
            }

            // 如果有人员已有排班记录，则提示用户
            if (personsWithSchedule.length > 0) {
              const personNames = personsWithSchedule.join('、');
              createMessage.warning(
                `${personNames} 在${values.month}月份已有排班，无法重复排班。您可以通过编辑功能修改现有排班。`,
              );
              setModalProps({ confirmLoading: false });
              fullscreenLoading.value = false; // 关闭全屏加载
              return;
            }
          }

          console.log('正在保存人员排班信息:', {
            values,
            selectedPersons: selectedLeafNodes,
          });

          // 构建符合接口要求的数据格式
          // 将选中的人员ID转换为接口需要的格式
          const requestData = selectedLeafNodes
            .map((personKey) => {
              // 从transferDataSource中获取人员信息
              const personNode = transferDataSource.value.find((item) => item.key === personKey);

              if (!personNode) {
                console.warn(`未找到ID为${personKey}的人员信息`);
                return null; // 跳过无效数据
              }

              // 根据班次值设置对应的班次号
              let classesCode = '1'; // 默认早班
              if (values.classes === classesEnum.nightShift) {
                classesCode = '3';
              }

              return {
                personName: personNode?.title || '', // 人员姓名
                month: values.month, // 月份
                classes: classesCode, // 班次编码(1=早班，3=晚班)
                idCard: personNode?.idCard || '', // 身份证号码
                personId: personKey, // 人员ID (用于设置employeeId)
                remarks: `${values.classes}安排`, // 备注信息
              };
            })
            .filter((item): item is NonNullable<typeof item> => item !== null); // 过滤掉无效数据

          // 批量保存人员排班
          await batchSaveStaffSchedule(requestData);

          createMessage.success('保存成功!');

          // 保存成功后清除所有数据
          clearFormData();

          // 关闭模态框
          closeModal();

          // 触发成功事件
          emit('success');
        } catch (error) {
          console.error('保存排班信息失败', error);
          createMessage.error('保存失败，请重试!');
        } finally {
          setModalProps({ confirmLoading: false });
          fullscreenLoading.value = false; // 关闭全屏加载
        }
      }

      // 自定义穿梭框渲染函数
      const renderTransferItem = (item: PersonNode) => {
        // 检查是否在右侧面板
        const isInRightPanel = targetKeys.value.includes(item.key);

        // 如果在右侧面板，显示名称和身份证
        if (isInRightPanel) {
          if (item.idCard) {
            return {
              label: `${item.title} (${item.idCard})`,
              value: item.key,
            };
          }
          return {
            label: item.title,
            value: item.key,
          };
        }

        // 左侧面板只显示标题
        return item.title;
      };

      // 处理穿梭框的变化事件
      const handleTransferChange = async (
        nextTargetKeys: string[],
        direction: string,
        moveKeys: string[],
      ) => {
        console.log('穿梭框变化:', { nextTargetKeys, direction, moveKeys });

        // 添加操作
        if (direction === 'right') {
          try {
            // 显示加载状态
            fullscreenLoading.value = true;

            // 收集所有选中节点的类型和ID
            const nodeTypes: string[] = [];
            const nodeIds: string[] = [];

            // 遍历所有选中的节点
            for (const key of moveKeys) {
              // 查找选中节点的数据
              const selectedNode = findNodeByKey(tData.value, key);
              if (!selectedNode) continue;

              // 获取节点类型和ID
              const nodeType = selectedNode?.nodeType || getNodeType(selectedNode || {});
              const nodeId = selectedNode?.id || key;

              // 如果节点不是worker类型，并且有效类型
              if (
                nodeType &&
                nodeType !== 'worker' &&
                ['office', 'workshop', 'prodLine', 'workGroup'].includes(nodeType)
              ) {
                nodeTypes.push(nodeType);
                nodeIds.push(nodeId);
              }
            }

            // 如果有有效节点，批量获取人员数据
            if (nodeTypes.length > 0) {
              // 调用批量获取接口
              console.log('批量请求接口获取人员数据:', { nodeTypes, nodeIds });
              const result = await batchGetPersonsByNodeTypes(nodeTypes, nodeIds);

              if (result.success && result.list && result.list.length > 0) {
                console.log('批量获取到人员数据:', result.list);

                // 清空当前moveKeys中的组织节点（保留worker类型的节点）
                const newTargetKeys = nextTargetKeys.filter((key) => {
                  const node = findNodeByKey(tData.value, key);
                  return !node || node.nodeType === 'worker';
                });

                // 将从接口获取的人员添加到数据源和目标keys
                result.list.forEach((person) => {
                  // 检查人员是否已存在于数据源中
                  const existingIndex = transferDataSource.value.findIndex(
                    (item) => item.idCard === person.identityCard || item.key === person.id,
                  );

                  if (existingIndex >= 0) {
                    // 如果已存在，更新其属性
                    transferDataSource.value[existingIndex].idCard = person.identityCard;
                    // 添加到目标keys（如果不存在）
                    if (!newTargetKeys.includes(transferDataSource.value[existingIndex].key)) {
                      newTargetKeys.push(transferDataSource.value[existingIndex].key);
                    }
                  } else {
                    // 如果不存在，添加到数据源
                    const newPerson: PersonNode = {
                      key: person.id,
                      title: person.name,
                      disabled: false,
                      isLeaf: true,
                      idCard: person.identityCard,
                      nodeType: 'worker',
                    };

                    // 将新人员添加到数据源
                    transferDataSource.value.push(newPerson);

                    // 添加到目标keys
                    newTargetKeys.push(person.id);
                  }
                });

                // 更新目标keys
                targetKeys.value = [...newTargetKeys];

                // 显示提示
                createMessage.success(`已添加 ${result.list.length} 名人员`);
              } else {
                console.log('未找到符合条件的人员');
                createMessage.info('未找到符合条件的人员');

                // 从目标keys中移除所有非worker节点
                targetKeys.value = nextTargetKeys.filter((key) => {
                  const node = findNodeByKey(tData.value, key);
                  return !node || node.nodeType === 'worker';
                });
              }
            } else {
              // 没有有效节点，直接更新目标keys
              targetKeys.value = nextTargetKeys;
            }
          } catch (error) {
            console.error('获取人员数据失败:', error);
            createMessage.error('获取人员数据失败');

            // 从目标keys中移除所有非worker节点
            targetKeys.value = nextTargetKeys.filter((key) => {
              const node = findNodeByKey(tData.value, key);
              return !node || node.nodeType === 'worker';
            });
          } finally {
            // 关闭加载状态
            fullscreenLoading.value = false;
          }
        } else {
          // 移除操作，直接更新目标keys
          targetKeys.value = nextTargetKeys;
        }
      };

      // 清除所有表单数据和选择状态
      function clearFormData() {
        // 重置表单字段
        resetFields();

        // 清空已选人员
        targetKeys.value = [];

        // 重置树形选择框的状态 - 通过切换可见性实现彻底重置
        treeVisible.value = false;

        // 重置树形选择框的状态
        nextTick(() => {
          // 通过重绘的方式强制刷新树组件
          if (transferDataSource.value.length > 0) {
            // 临时保存原始数据
            const tempData = [...transferDataSource.value];
            // 清空数据
            transferDataSource.value = [];
            // 下一个事件循环中重新赋值，触发组件重绘
            setTimeout(() => {
              transferDataSource.value = tempData;

              // 确保展开节点状态恢复
              expandedKeys.value = [
                'factory-1',
                'workshop-1',
                'workshop-2',
                'workshop-3',
                'process-3-1',
                'team-3-1-1',
              ];

              // 重新显示树组件
              treeVisible.value = true;

              console.log('树形组件已完全重置');
            }, 10);
          }

          // 设置默认值 - 放在nextTick中确保DOM已更新
          setFieldsValue({
            month: null,
            classes: classesEnum.early,
            activityId: undefined,
          });
        });

        // 记录日志
        console.log('模态框关闭，已清除所有数据和选中状态');
      }

      // 处理取消按钮点击
      function handleCancel() {
        clearFormData();
        closeModal();
      }

      return {
        registerModal,
        registerForm,
        handleSubmit,
        handleCancel, // 导出取消处理函数
        targetKeys,
        dataSource,
        leafDataSource, // 添加叶子节点数据源
        treeData,
        expandedKeys,
        onChecked,
        onExpand,
        renderTransferItem,
        getCheckedKeys,
        fullscreenLoading, // 添加loading状态
        treeRef, // 添加树组件引用
        treeVisible, // 添加树组件可见性控制
        handleTransferChange, // 添加穿梭框变化处理函数
      };
    },
  });
</script>
<style scoped lang="less">
  .transfer-box {
    min-width: 680px;
    text-align: center;
    margin: 30px 0 0 50px;
  }

  /* 全屏加载样式 */
  .fullscreen-loading-container {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(255, 255, 255, 0.7);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
  }

  /* 树形结构样式优化 */
  :deep(.ant-tree) {
    min-height: 300px;
    max-height: 400px;
    overflow-y: auto;
  }

  :deep(.ant-transfer-list) {
    min-height: 400px;
    width: 45% !important;
  }

  :deep(.ant-transfer-operation) {
    width: 10% !important;
  }

  :deep(.ant-transfer-list-header-selected) {
    span:last-child {
      font-weight: bold;
    }
  }

  :deep(.ant-transfer-list-header-title) {
    color: #1890ff;
    font-weight: bold;
  }

  /* 隐藏左侧Transfer列表头部的项数显示 */
  :deep(.ant-transfer-list:first-child .ant-transfer-list-header-selected) {
    span:first-child {
      display: none;
    }
  }

  /* 修改右侧Transfer列表头部的项数显示 */
  :deep(.ant-transfer-list:last-child .ant-transfer-list-header-selected) {
    span:first-child {
      display: none;
      /* 隐藏原来的数字 */
    }
  }

  :deep(.ant-tree-treenode) {
    padding: 8px 0;

    &:hover {
      background-color: #f5f5f5;
    }
  }

  :deep(.ant-tree-node-content-wrapper) {
    width: 100%;
    display: flex !important;
    align-items: center;
  }

  /* 优化图标显示 */
  :deep(.ant-tree-iconEle) {
    margin-right: 6px;

    .anticon {
      font-size: 16px;
    }
  }

  /* 层级缩进优化 */
  :deep(.ant-tree-indent-unit) {
    width: 20px;
  }

  /* 优化选择框样式 */
  :deep(.ant-tree-checkbox) {
    margin-top: 2px;
  }

  /* 修改右侧Transfer列表内容样式 */
  :deep(.ant-transfer-list-content) {
    height: calc(100% - 55px) !important;
  }

  :deep(.ant-transfer-list-content-item) {
    padding: 8px 12px;
    min-height: 32px;
    line-height: 1.5;
  }
</style>
