<!--
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author Shawn
 * @date 2025-05-22
-->
<template>
  <div class="tree-container">
    <div v-if="!isView" class="tree-transfer">
      <!-- 非查看模式：树形选择组件 -->
      <div class="tree-transfer-left">
        <div class="tree-transfer-header">
          <div class="header-title">可选项</div>
          <div class="header-subtitle" v-if="participationType">
            当前参与类型: {{ getParticipationTypeText }}
          </div>
          <!-- 搜索输入框 -->
          <div class="search-container">
            <a-input-search
              v-model:value="searchKeyword"
              placeholder="搜索人员（姓名/身份证/电话）"
              @search="handleSearch"
              @change="handleSearchChange"
              :loading="isSearching"
              size="small"
              allow-clear
            />
          </div>
        </div>
        <div class="tree-transfer-body">
          <!-- 搜索结果展示 -->
          <div v-if="showSearchResults" class="search-results">
            <div class="search-results-header">
              <span>搜索结果 ({{ searchResults.length }})</span>
              <a-button type="link" size="small" @click="clearSearch">返回组织树</a-button>
            </div>
            <div class="search-results-body">
              <template v-if="searchResults.length > 0">
                <div v-for="person in searchResults" :key="person.id" class="search-result-item">
                  <Checkbox
                    :checked="isPersonSelected(person.id)"
                    @change="(e) => handlePersonCheckChange(e, person)"
                  />
                  <div class="person-info">
                    <div class="person-name">{{ person.name }}</div>
                    <div class="person-detail">
                      {{ person.department }} - {{ person.team }}
                      <span v-if="person.identityCard" class="person-id"
                        >({{ person.identityCard }})</span
                      >
                      <span v-if="person.phoneNumber" class="person-phone"
                        >({{ person.phoneNumber }})</span
                      >
                    </div>
                  </div>
                  <a-button
                    type="link"
                    size="small"
                    @click="addSinglePerson(person)"
                    :disabled="isPersonSelected(person.id)"
                  >
                    <Icon icon="ant-design:plus-outlined" />
                  </a-button>
                </div>
              </template>
              <Empty v-else description="未找到匹配的人员" />
            </div>
          </div>
          <!-- 组织树 -->
          <div v-else class="org-tree">
            <Tree
              :treeData="localTreeData"
              checkable
              :fieldNames="{ title: 'title', key: 'value', children: 'children' }"
              v-model:checkedKeys="checkedKeys"
              v-model:expandedKeys="expandedKeys"
              @check="onTreeCheck"
              @expand="onTreeExpand"
              @load="onLoadData"
              :loadData="loadTreeData"
              :load-data-on-expand="true"
              style="width: 100%; overflow: auto; max-height: 350px"
              :tree-line="true"
              blockNode
              :autoExpandParent="true"
            />
          </div>
        </div>
      </div>
      <div class="tree-transfer-operation">
        <div class="operation-item">
          <a-button
            type="primary"
            size="small"
            shape="circle"
            @click="handleAddSelected"
            :disabled="!checkedKeys.length || isLoadingPersons"
            title="添加选中节点下的所有人员"
          >
            <LoadingOutlined v-if="isLoadingPersons" spin />
            <Icon v-else icon="ant-design:right-outlined" />
          </a-button>
        </div>
        <div class="operation-item">
          <a-button
            type="primary"
            size="small"
            shape="circle"
            @click="handleRemoveSelected"
            :disabled="!selectedPersons.length"
            title="清空所有已选人员"
          >
            <Icon icon="ant-design:left-outlined" />
          </a-button>
        </div>
      </div>
      <div class="tree-transfer-right">
        <div class="tree-transfer-header">
          <div class="header-title">已选人员</div>
          <div class="header-subtitle" v-if="selectedPersons.length > 0">
            共 {{ selectedPersons.length }} 人
          </div>
        </div>
        <div class="tree-transfer-body">
          <template v-if="selectedPersons && selectedPersons.length > 0">
            <div class="selected-items">
              <div v-for="person in selectedPersons" :key="person.id" class="selected-item">
                <Checkbox checked disabled />
                <div class="person-info">
                  <div class="person-name">{{ person.name }}</div>
                  <div class="person-detail">{{ person.department }} - {{ person.team }}</div>
                </div>
                <a-button type="link" size="small" @click="handleRemovePerson(person)">
                  <Icon icon="ant-design:delete-outlined" />
                </a-button>
              </div>
            </div>
          </template>
          <Empty v-else description="暂无已选人员" />
        </div>
      </div>
    </div>

    <!-- 查看模式：简单文本显示 -->
    <div v-else class="view-mode">
      <div class="participants-view">
        <template v-if="typeof selectedItems === 'string'">
          {{ selectedItems }}
        </template>
        <template v-else-if="selectedPersons && selectedPersons.length > 0">
          <div v-for="person in selectedPersons" :key="person.id" class="participant-item">
            {{ person.name }} ({{ person.department }} - {{ person.team }})
          </div>
        </template>
        <template v-else> 无参与对象 </template>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref, watch, defineProps, defineEmits, onMounted, computed, defineExpose } from 'vue';
  import { Tree, Checkbox, Empty, message } from 'ant-design-vue';
  import { Icon } from '@/components/swm/Icon';
  import { LoadingOutlined } from '@ant-design/icons-vue';
  import { fetchOrgTreeData, OrgTreeNode } from '@/api/swm/organizationTree';
  import { getPersonsByDepartmentCondition, PersonInfo, searchPersons } from '@/api/swm/person';
  import type { DataNode } from 'ant-design-vue/es/tree';

  const props = defineProps({
    treeData: {
      type: Array as () => OrgTreeNode[],
      required: true,
      default: () => [],
    },
    modelValue: {
      type: Array as () => string[],
      default: () => [],
    },
    isView: {
      type: Boolean,
      default: false,
    },
    participationType: {
      type: String,
      default: '1', // 默认为班组级别
    },
  });

  const emit = defineEmits(['update:modelValue', 'change', 'namesChange']);

  // 内部状态
  const checkedKeys = ref<string[]>([]);
  const selectedItems = ref<string[]>([]);
  const selectedPersons = ref<PersonInfo[]>([]); // 新增：选中的人员列表
  const isLoadingPersons = ref(false); // 新增：加载人员状态
  const localTreeData = ref<OrgTreeNode[]>([]);
  const expandedKeys = ref<string[]>([]); // 确保初始化为空数组
  // 添加节点信息缓存，用于快速查找节点类型
  const nodeInfoCache = ref<Record<string, { nodeType: string; title: string }>>({});

  // 搜索相关状态
  const searchKeyword = ref<string>('');
  const searchResults = ref<PersonInfo[]>([]);
  const isSearching = ref<boolean>(false);
  const showSearchResults = ref<boolean>(false);

  // 监听外部值变化
  watch(
    () => props.modelValue,
    (newVal) => {
      if (newVal && Array.isArray(newVal)) {
        selectedItems.value = [...newVal];
        checkedKeys.value = [...newVal];
        // 如果是查看模式且有数据，尝试解析人员信息
        if (props.isView && newVal.length > 0) {
          loadPersonsFromIds(newVal);
        }
      } else {
        selectedItems.value = [];
        selectedPersons.value = [];
        checkedKeys.value = [];
      }
    },
    { immediate: true, deep: true },
  );

  watch(
    () => props.treeData,
    (newVal) => {
      if (newVal && Array.isArray(newVal)) {
        localTreeData.value = [...newVal];
      }
    },
    { immediate: true, deep: true },
  );

  // 从ID列表加载人员信息（用于查看模式）
  const loadPersonsFromIds = async (personIds: string[]) => {
    try {
      const persons: PersonInfo[] = [];
      for (const id of personIds) {
        try {
          const result = await getPersonsByDepartmentCondition(id);
          if (result && Array.isArray(result)) {
            persons.push(...result);
          }
        } catch (error) {
          console.warn(`加载人员信息失败，ID: ${id}`, error);
        }
      }
      selectedPersons.value = persons;
    } catch (error) {
      console.error('加载人员信息失败:', error);
    }
  };

  // 根据参与类型确定是否是叶子节点
  const isNodeLeaf = (node: any) => {
    // 参与类型为"1"(班组级别)时，班组是叶子节点
    // 参与类型为"2"(产线级别)时，产线是叶子节点
    // 参与类型为"3"(车间级别)时，车间是叶子节点

    if (props.participationType === '1') {
      // 默认情况：班组是叶子节点
      return node.nodeType === 'workGroup';
    } else if (props.participationType === '2') {
      // 产线级别：产线是叶子节点
      return node.nodeType === 'prodLine' || node.nodeType === 'workGroup';
    } else if (props.participationType === '3') {
      // 车间级别：车间是叶子节点
      return (
        node.nodeType === 'workshop' ||
        node.nodeType === 'prodLine' ||
        node.nodeType === 'workGroup'
      );
    }

    // 默认返回false，允许展开
    return false;
  };

  // 监听参与类型变化，重新处理树数据
  watch(
    () => props.participationType,
    (newType) => {
      console.log('参与类型变化，重新处理树数据:', newType);

      // 清空已选人员
      selectedPersons.value = [];

      // 过滤已选项，只保留符合当前参与类型的节点
      if (selectedItems.value && selectedItems.value.length > 0) {
        // 过滤已选项
        const filteredItems = selectedItems.value.filter((item) => {
          const isMatch = isNodeMatchParticipationType(item, newType);
          if (!isMatch) {
          }
          return isMatch;
        });

        // 更新已选项
        if (filteredItems.length !== selectedItems.value.length) {
          selectedItems.value = filteredItems;

          // 通知父组件更新
          emit('update:modelValue', selectedItems.value);
          emit('change', selectedItems.value);
        }
      }

      // 安全措施：确保重置展开状态和选中状态
      expandedKeys.value = [];
      checkedKeys.value = [];

      // 重新获取根节点数据，完全刷新树结构
      fetchOrgTreeData('root')
        .then((result) => {
          if (result && Array.isArray(result)) {
            // 使用新的根节点数据替换，确保没有不应显示的子节点
            localTreeData.value = result.map((item) => ({
              ...item,
              isLeaf: isNodeLeaf(item),
              // 不预加载children，等待展开时再加载
              children: undefined,
              // 标记当前参与类型，用于判断是否需要重新加载
              _participationType: newType,
            }));

            console.log('参与类型变化后重置树数据:', localTreeData.value);
          }
        })
        .catch((error) => {
          message.error('刷新组织结构失败');
        });
    },
  );

  // 初始化组件时设置节点点击处理
  onMounted(() => {
    loadRootNodes();

    // 添加点击事件监听
    setTimeout(() => {
      addClickListeners();
    }, 500);
  });

  // 加载根节点数据
  async function loadRootNodes() {
    try {
      const result = await fetchOrgTreeData('root');

      if (result && Array.isArray(result)) {
        // 初始化根节点数据的同时更新缓存
        localTreeData.value = result.map((item) => {
          // 缓存节点信息
          const nodeKey = item.value || item.id;
          if (nodeKey) {
            nodeInfoCache.value[nodeKey] = {
              nodeType: item.nodeType,
              title: item.title,
            };
          }

          return {
            ...item,
            isLeaf: isNodeLeaf(item),
            _participationType: props.participationType,
          };
        });

        console.log('处理后的树形数据:', localTreeData.value);
        console.log('更新节点信息缓存:', nodeInfoCache.value);
      } else {
        console.error('API返回的数据不是数组格式:', result);
      }
    } catch (error) {
      console.error('加载组织树根节点失败:', error);
      message.error('加载组织结构数据失败');
    }
  }

  // 在DOM中添加展开图标点击事件监听
  function addClickListeners() {
    // 使用MutationObserver监听DOM变化，动态添加事件监听
    const observer = new MutationObserver((mutations) => {
      // 找到所有展开图标并添加点击事件监听
      document.querySelectorAll('.tree-transfer-left .ant-tree-switcher').forEach((el) => {
        if (!el.getAttribute('data-event-added')) {
          el.setAttribute('data-event-added', 'true');
          el.addEventListener('click', (event) => {
            // 获取关联节点的key
            const nodeItem = el.closest('.ant-tree-treenode');
            if (nodeItem) {
              console.log('点击了展开图标，准备加载子节点');
              // 可以在这里进行一些额外处理
            }
          });
        }
      });
    });

    // 监听树容器中的变化
    const treeContainer = document.querySelector('.tree-transfer-left .tree-transfer-body');
    if (treeContainer) {
      observer.observe(treeContainer, {
        childList: true,
        subtree: true,
      });
      console.log('已添加树节点变化监听');
    }
  }

  // 懒加载子节点数据
  const loadTreeData = (treeNode) => {
    return new Promise<void>(async (resolve) => {
      // 防护检查，确保 treeNode 和 dataRef 存在
      if (!treeNode || !treeNode.dataRef) {
        console.error('加载子节点失败: 节点数据不完整', treeNode);
        resolve();
        return;
      }

      console.log('尝试加载子节点:', treeNode.dataRef);

      // 始终检查节点是否应该是叶子节点
      if (isNodeLeaf(treeNode.dataRef)) {
        console.log(
          `节点${treeNode.dataRef.title}(${treeNode.dataRef.nodeType})是叶子节点，不加载子节点`,
        );
        treeNode.dataRef.isLeaf = true;
        resolve();
        return;
      }

      // 如果节点有加载标记且参与类型没变，则可以跳过重新加载
      // 但如果被onTreeExpand强制删除了children，则需重新加载
      const nodeKey = treeNode.dataRef.value || treeNode.dataRef.id || '';
      const participationTypeMark = treeNode.dataRef._participationType;
      const participationTypeChanged = participationTypeMark !== props.participationType;
      const hasChildren = treeNode.dataRef.children && treeNode.dataRef.children.length > 0;

      // 有子节点、参与类型一致且未被标记为需要重新加载，则使用现有数据
      if (hasChildren && !participationTypeChanged) {
        console.log(`使用缓存的子节点数据，节点:${treeNode.dataRef.title}`);
        resolve();
        return;
      }

      // 其他情况重新加载子节点
      try {
        console.log(
          `重新加载子节点数据，节点:${treeNode.dataRef.title}，参与类型:${props.participationType}`,
        );
        const nodeType = getNodeType(treeNode.dataRef);
        const nodeId = treeNode.dataRef.id;

        // 打印更详细的加载信息
        console.log(`发起API请求: fetchOrgTreeData(${nodeType}, ${nodeId})`);

        // 根据节点类型和ID加载子节点
        const childNodes = await fetchOrgTreeData(nodeType, nodeId);
        console.log(`API请求成功，获取到${childNodes.length}个子节点`);

        // 更新当前节点的子节点，并根据参与类型设置叶子节点属性
        treeNode.dataRef.children = childNodes.map((child) => {
          // 更新节点信息缓存
          const nodeKey = child.value || child.id;
          if (nodeKey) {
            nodeInfoCache.value[nodeKey] = {
              nodeType: child.nodeType,
              title: child.title,
            };
          }

          return {
            ...child,
            isLeaf: isNodeLeaf(child),
            _participationType: props.participationType, // 记录当前参与类型
          };
        });

        // 记录节点关联的参与类型
        treeNode.dataRef._participationType = props.participationType;

        // 更新当前节点的缓存
        const currentNodeKey = treeNode.dataRef.value || treeNode.dataRef.id;
        if (currentNodeKey) {
          nodeInfoCache.value[currentNodeKey] = {
            nodeType: treeNode.dataRef.nodeType,
            title: treeNode.dataRef.title,
          };
        }

        // 通知树组件数据已更新
        localTreeData.value = [...localTreeData.value];

        // 安全检查: 确保expandedKeys.value是数组，并且nodeKey不为空
        if (expandedKeys.value && nodeKey && !expandedKeys.value.includes(nodeKey)) {
          expandedKeys.value = [...expandedKeys.value, nodeKey];
          console.log(`已将节点${nodeKey}添加到展开状态`, expandedKeys.value);
        } else {
          console.log(`节点${nodeKey}已在展开状态中或无法添加`, expandedKeys.value);
        }

        console.log(
          `节点${treeNode.dataRef.title}的子节点数据已更新，当前展开状态:`,
          expandedKeys.value,
        );
        resolve();
      } catch (error) {
        console.error('加载子节点失败:', error);
        message.error('加载组织子节点数据失败');
        resolve();
      }
    });
  };

  // 获取节点类型
  function getNodeType(node) {
    // 根据节点层级判断类型
    if (!node.parentId || node.parentId === '0') {
      // 厂间是最高级父节点，其parentId为"0"或为空
      return 'office'; // 返回厂间类型，加载其下属车间
    } else if (node.nodeType === 'office') {
      return 'office'; // 厂间下是车间
    } else if (node.nodeType === 'workshop') {
      return 'workshop'; // 车间下是产线
    } else if (node.nodeType === 'prodLine') {
      return 'prodLine'; // 产线下是班组
    }
    return 'unknown';
  }

  // 加载子节点事件处理
  function onLoadData(expandedKeys, { node }) {
    console.log('展开节点:', node.eventKey, expandedKeys);
  }

  // 获取节点标题
  function getNodeTitle(key: string) {
    // 先从缓存中查找
    if (nodeInfoCache.value[key]) {
      return nodeInfoCache.value[key].title;
    }

    // 从treeData中查找对应key的节点标题
    const findTitle = (nodes) => {
      for (const node of nodes) {
        if (node.value === key) {
          // 添加到缓存
          nodeInfoCache.value[key] = {
            nodeType: node.nodeType || '',
            title: node.title,
          };
          return node.title;
        }
        if (node.children && node.children.length > 0) {
          const title = findTitle(node.children);
          if (title) return title;
        }
      }
      return '';
    };

    return findTitle(localTreeData.value) || key;
  }

  // 监听Tree选中变化
  function onTreeCheck(checkedKeys, { checked, node, checkedNodes }) {
    // 获取实际的checked keys（不包含半选状态的节点）
    const actualCheckedKeys = Array.isArray(checkedKeys) ? checkedKeys : checkedKeys.checked || [];

    // 检查新增选中的节点是否符合当前参与类型
    if (checked && node) {
      const nodeKey = node.key || node.eventKey;
      const isMatch = isNodeMatchParticipationType(nodeKey, props.participationType);

      if (!isMatch) {
        console.warn(`选中的节点 ${nodeKey} 不符合当前参与类型 ${props.participationType}`);
      }
    }

    // 只更新左侧选中状态，不直接修改右侧已选列表
    checkedKeys.value = actualCheckedKeys;
  }

  // 监听Tree展开变化
  function onTreeExpand(expandedKeys, { expanded, node }) {
    // 安全检查：确保node和eventKey存在
    if (!node) {
      return;
    }

    const nodeKey = node.eventKey || '';
    console.log('展开节点事件:', nodeKey, expanded ? '展开' : '收起');

    // 如果是展开操作，检查节点是否需要重新加载
    if (expanded) {
      // 获取节点数据
      const nodeData = node.dataRef;

      // 安全检查：确保节点数据存在
      if (!nodeData) {
        return;
      }

      // 如果节点已存在但参与类型不一致，或没有子节点，则需要重新加载
      if (nodeData && !isNodeLeaf(nodeData)) {
        const participationTypeChanged = nodeData._participationType !== props.participationType;
        const hasNoChildren = !nodeData.children || nodeData.children.length === 0;

        if (participationTypeChanged || hasNoChildren) {
          // 立即开始加载状态
          nodeData.loading = true;

          // 清除现有子节点数据，强制重新加载
          if (nodeData.children) {
            delete nodeData.children;
          }

          // 手动更新树数据，触发重新渲染
          localTreeData.value = [...localTreeData.value];

          // 立即触发加载操作，不再使用setTimeout延迟
          loadTreeData({
            dataRef: nodeData,
          }).then(() => {
            // 移除加载状态
            nodeData.loading = false;
            localTreeData.value = [...localTreeData.value];

            // 确保节点处于展开状态 - 添加安全检查
            if (expandedKeys.value && nodeKey && !expandedKeys.value.includes(nodeKey)) {
              expandedKeys.value = [...expandedKeys.value, nodeKey];
            }
          });
        } else {
        }
      }
    }
  }

  // 新增：根据节点查询人员
  async function loadPersonsForNodes(nodeKeys: string[]) {
    if (!nodeKeys || nodeKeys.length === 0) {
      return [];
    }

    isLoadingPersons.value = true;
    const allPersons: PersonInfo[] = [];

    try {
      for (const nodeKey of nodeKeys) {
        try {
          console.log('查询节点下的人员:', nodeKey);
          const persons = await getPersonsByDepartmentCondition(nodeKey);
          if (persons && Array.isArray(persons)) {
            console.log(`节点 ${nodeKey} 找到 ${persons.length} 名人员`);
            allPersons.push(...persons);
          }
        } catch (error) {
          console.error(`查询节点 ${nodeKey} 下的人员失败:`, error);
          message.error(`查询节点 ${getNodeTitle(nodeKey)} 下的人员失败`);
        }
      }

      // 去重处理（基于身份证号）
      const uniquePersons = allPersons.filter(
        (person, index, self) =>
          index === self.findIndex((p) => p.identityCard === person.identityCard),
      );

      console.log(`总共找到 ${allPersons.length} 名人员，去重后 ${uniquePersons.length} 名`);
      return uniquePersons;
    } catch (error) {
      console.error('查询人员失败:', error);
      message.error('查询人员失败');
      return [];
    } finally {
      isLoadingPersons.value = false;
    }
  }

  // 修改：添加所选项到右侧（查询人员）
  async function handleAddSelected() {
    if (checkedKeys.value && checkedKeys.value.length > 0) {
      // 先过滤出符合当前参与类型的节点
      const filteredKeys = checkedKeys.value.filter((key) =>
        isNodeMatchParticipationType(key, props.participationType),
      );

      if (filteredKeys.length !== checkedKeys.value.length) {
        console.warn(
          `有${checkedKeys.value.length - filteredKeys.length}个选中项不符合当前参与类型，已被过滤`,
        );

        // 可以添加提示

        // 更新选中状态
        checkedKeys.value = filteredKeys;
      }

      if (filteredKeys.length === 0) {
        message.warn(`没有符合当前参与类型"${getParticipationTypeText.value}"的节点可添加`);
        return;
      }

      // 查询选中节点下的所有人员
      const newPersons = await loadPersonsForNodes(filteredKeys);

      if (newPersons.length === 0) {
        message.warn('选中的节点下没有找到人员');
        return;
      }

      // 合并人员列表，去重（基于身份证号）
      const existingIdCards = new Set(selectedPersons.value.map((p) => p.identityCard));
      const uniqueNewPersons = newPersons.filter((p) => !existingIdCards.has(p.identityCard));

      if (uniqueNewPersons.length === 0) {
        message.warn('选中的人员已经在列表中');
        return;
      }

      selectedPersons.value = [...selectedPersons.value, ...uniqueNewPersons];

      // 更新表单数据（使用人员ID数组）
      const personIds = selectedPersons.value.map((p) => p.id);
      selectedItems.value = personIds;

      // 向父组件发送更新事件
      emit('update:modelValue', selectedItems.value);
      emit('change', selectedItems.value);

      // 发送人员名称变化事件
      const personNames = selectedPersons.value.map((p) => p.name);
      emit('namesChange', personNames);

      message.success(`已添加 ${uniqueNewPersons.length} 名人员`);
    }
  }

  // 修改：从右侧移除指定人员
  function handleRemovePerson(person: PersonInfo) {
    if (selectedPersons.value && person) {
      // 从已选人员中移除
      selectedPersons.value = selectedPersons.value.filter((p) => p.id !== person.id);

      // 更新表单数据
      const personIds = selectedPersons.value.map((p) => p.id);
      selectedItems.value = personIds;

      // 向父组件发送更新事件
      emit('update:modelValue', selectedItems.value);
      emit('change', selectedItems.value);

      // 发送人员名称变化事件
      const personNames = selectedPersons.value.map((p) => p.name);
      emit('namesChange', personNames);
    }
  }

  // 修改：清空所有已选人员
  function handleRemoveSelected() {
    if (selectedPersons.value && selectedPersons.value.length > 0) {
      // 清空所有已选人员
      selectedPersons.value = [];
      selectedItems.value = [];

      // 向父组件发送更新事件
      emit('update:modelValue', []);
      emit('change', []);
      emit('namesChange', []);

      console.log('已清空所有已选人员');
    }
  }

  // 获取参与类型的文本描述
  const getParticipationTypeText = computed(() => {
    const typeMapping = {
      '1': '班组',
      '2': '产线',
      '3': '车间',
    };
    return typeMapping[props.participationType] || props.participationType;
  });

  // 判断节点是否符合当前参与类型级别的函数
  function isNodeMatchParticipationType(nodeValue: string, participationType: string): boolean {
    // 先从缓存中查找节点信息
    if (nodeInfoCache.value[nodeValue]) {
      const nodeType = nodeInfoCache.value[nodeValue].nodeType;

      if (participationType === '1') {
        // 班组级别
        const isMatch = nodeType === 'workGroup';
        return isMatch; // 只保留班组级别节点
      } else if (participationType === '2') {
        // 产线级别
        const isMatch = nodeType === 'prodLine';
        return isMatch; // 只保留产线级别节点
      } else if (participationType === '3') {
        // 车间级别
        const isMatch = nodeType === 'workshop';
        return isMatch; // 只保留车间级别节点
      }

      return true;
    }

    // 如果缓存中没有，则从当前树数据中查找
    const node = findNodeByValue(nodeValue, localTreeData.value);
    if (!node) {
      return false; // 如果找不到节点信息，默认不符合
    }

    // 根据节点类型和参与类型判断是否符合
    const nodeType = node.nodeType;

    // 添加到缓存
    nodeInfoCache.value[nodeValue] = {
      nodeType,
      title: node.title || '',
    };

    if (participationType === '1') {
      // 班组级别
      const isMatch = nodeType === 'workGroup';
      return isMatch; // 只保留班组级别节点
    } else if (participationType === '2') {
      // 产线级别
      const isMatch = nodeType === 'prodLine';
      return isMatch; // 只保留产线级别节点
    } else if (participationType === '3') {
      // 车间级别
      const isMatch = nodeType === 'workshop';
      return isMatch; // 只保留车间级别节点
    }

    // 默认全部保留
    return true;
  }

  // 根据节点值查找节点信息
  function findNodeByValue(value: string, nodes: any[]): any {
    if (!nodes || nodes.length === 0) return null;

    // 遍历所有节点
    for (const node of nodes) {
      // 如果当前节点匹配，直接返回
      if (node.value === value || node.id === value) {
        return node;
      }

      // 递归查找子节点
      if (node.children && node.children.length > 0) {
        const found = findNodeByValue(value, node.children);
        if (found) return found;
      }
    }

    return null;
  }

  // 暴露getSelectedNames方法
  const exposeTo = {
    getSelectedNames: () => selectedPersons.value.map((p) => p.name),
    getSelectedValues: () => selectedItems.value,
    getParticipationType: () => props.participationType,
  };

  // 添加参与对象变化时触发名称更新事件
  watch(
    () => selectedPersons.value,
    (newVal) => {
      if (newVal && newVal.length > 0) {
        const names = newVal.map((p) => p.name);
        emit('namesChange', names);
      }
    },
    { deep: true },
  );

  // 搜索人员
  async function handleSearch(keyword: string) {
    if (!keyword || keyword.trim() === '') {
      clearSearch();
      return;
    }

    try {
      isSearching.value = true;
      const result = await searchPersons({
        keyword: keyword.trim(),
        searchType: 'all', // 搜索所有字段
        pageSize: 100,
      });

      if (result && result.success) {
        searchResults.value = result.list || [];
        showSearchResults.value = true;
      } else {
        searchResults.value = [];
        showSearchResults.value = true;
        message.warning('搜索失败，请重试');
      }
    } catch (error) {
      console.error('搜索人员失败:', error);
      message.error('搜索失败，请重试');
      searchResults.value = [];
      showSearchResults.value = true;
    } finally {
      isSearching.value = false;
    }
  }

  // 搜索输入框变化处理
  function handleSearchChange(e: any) {
    const value = e.target.value;
    if (!value || value.trim() === '') {
      clearSearch();
    }
  }

  // 清空搜索结果
  function clearSearch() {
    searchKeyword.value = '';
    searchResults.value = [];
    showSearchResults.value = false;
  }

  // 检查人员是否已被选中
  function isPersonSelected(personId: string): boolean {
    return selectedPersons.value.some((p) => p.id === personId);
  }

  // 处理搜索结果中人员的复选框变化
  function handlePersonCheckChange(e: any, person: PersonInfo) {
    if (e.target.checked) {
      addSinglePerson(person);
    } else {
      handleRemovePerson(person);
    }
  }

  // 添加单个人员到已选列表
  function addSinglePerson(person: PersonInfo) {
    if (isPersonSelected(person.id)) {
      message.warning('该人员已在选择列表中');
      return;
    }

    // 添加人员到已选列表
    selectedPersons.value.push(person);

    // 更新表单数据
    const personIds = selectedPersons.value.map((p) => p.id);
    selectedItems.value = personIds;

    // 向父组件发送更新事件
    emit('update:modelValue', selectedItems.value);
    emit('change', selectedItems.value);

    // 发送人员名称变化事件
    const personNames = selectedPersons.value.map((p) => p.name);
    emit('namesChange', personNames);

    message.success(`已添加 ${person.name}`);
  }

  // 清空已选人员
  function clearSelectedPersons() {
    selectedPersons.value = [];
    selectedItems.value = [];

    // 向父组件发送更新事件
    emit('update:modelValue', []);
    emit('change', []);
    emit('namesChange', []);

    console.log('已清空所有已选人员');
  }

  // 直接在<script setup>块末尾添加defineExpose
  defineExpose({
    // 获取选中项的名称
    getSelectedNames: () => selectedPersons.value.map((p) => p.name),
    // 获取选中项的值（人员ID数组）
    getSelectedValues: () => selectedItems.value,
    // 获取节点名称
    getNodeTitle,
    // 清空已选人员
    clearSelectedPersons,
  });
</script>

<style lang="less" scoped>
  .tree-container {
    width: 100%;
    min-height: 100px;
    overflow: hidden;
  }

  // 查看模式样式
  .view-mode {
    min-height: 40px;
    padding: 8px 12px;
    border: 1px solid #e8e8e8;
    border-radius: 2px;
    background-color: #f9f9f9;

    .participants-view {
      font-size: 14px;
      line-height: 1.5;
    }

    .participant-item {
      margin: 4px 0;
      padding: 4px 8px;
      background-color: #f0f8ff;
      border-radius: 4px;
      display: inline-block;
      margin-right: 8px;
    }
  }

  .tree-transfer {
    display: flex;
    height: 100%;
    width: 100%;
    overflow: hidden;
  }

  .tree-transfer-left,
  .tree-transfer-right {
    flex: 1;
    height: 100%;
    min-height: 350px;
    border: 1px solid #e8e8e8;
    border-radius: 2px;
    background-color: #fff;
    margin: 0 4px;
    overflow: hidden;
  }

  .tree-transfer-header {
    padding: 8px;
    background-color: #f5f5f5;
    border-bottom: 1px solid #e8e8e8;
    font-weight: bold;

    .header-title {
      font-size: 14px;
      margin-bottom: 4px;
    }

    .header-subtitle {
      font-size: 12px;
      color: #722ed1;
      font-weight: normal;
    }

    .search-container {
      margin-top: 8px;
    }
  }

  .tree-transfer-body {
    padding: 8px;
    height: calc(100% - 40px);
    min-height: 300px;
    max-height: 400px;
    overflow-y: auto;
    background-color: #fff;
  }

  .selected-items {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .selected-item {
    display: flex;
    align-items: center;
    padding: 8px;
    border-radius: 4px;
    background-color: #f0f8ff;

    .person-info {
      flex: 1;
      margin: 0 8px;

      .person-name {
        font-weight: 500;
        color: #333;
        margin-bottom: 2px;
      }

      .person-detail {
        font-size: 12px;
        color: #666;
      }
    }
  }

  .operation-item {
    margin: 8px 0;
  }

  // 搜索结果样式
  .search-results {
    height: 100%;
    display: flex;
    flex-direction: column;

    .search-results-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;
      border-bottom: 1px solid #e8e8e8;
      margin-bottom: 8px;
      font-size: 12px;
      color: #666;
    }

    .search-results-body {
      flex: 1;
      overflow: auto;
    }
  }

  .search-result-item {
    display: flex;
    align-items: center;
    padding: 8px;
    border-radius: 4px;
    margin-bottom: 8px;
    border: 1px solid #e8e8e8;
    transition: all 0.2s;

    &:hover {
      background-color: #f5f5f5;
      border-color: #722ed1;
    }

    .person-info {
      flex: 1;
      margin: 0 8px;

      .person-name {
        font-weight: 500;
        color: #333;
        margin-bottom: 2px;
      }

      .person-detail {
        font-size: 12px;
        color: #666;

        .person-id,
        .person-phone {
          color: #999;
          margin-left: 8px;
        }
      }
    }
  }

  .org-tree {
    height: 100%;
  }
</style>
