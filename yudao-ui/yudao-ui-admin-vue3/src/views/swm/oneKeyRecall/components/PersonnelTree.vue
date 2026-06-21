<!--
  @description 人员树选择组件
  @date 2024-06-10
-->
<template>
  <div class="personnel-tree">
    <div v-if="showSearch" class="search-section">
      <a-input-search
        v-model:value="searchValue"
        placeholder="请输入人员姓名进行搜索"
        style="width: 100%"
        @search="handleSearch"
        allowClear
        :loading="treeLoading"
      />
    </div>
    <div class="tree-section">
      <div v-if="treeLoading" class="tree-loading">
        <a-spin tip="加载中..."></a-spin>
      </div>
      <div v-else-if="disabled" class="tree-disabled-notice">
        <div class="notice-content">
          <div class="notice-icon">🚫</div>
          <div class="notice-text">
            <div class="notice-title">全体撤离模式</div>
            <div class="notice-desc">当前为全体撤离方案，无需选择具体人员</div>
          </div>
        </div>
      </div>

      <!-- 搜索结果容器 - 使用动态高度分配 -->
      <div
        v-if="showSearch"
        class="search-results-container"
        :class="{
          'has-results': hasAnySearchResults,
          'both-expanded':
            !currentResultsCollapsed &&
            !allResultsCollapsed &&
            searchResults.length > 0 &&
            allSearchResults.length > 0,
        }"
      >
        <!-- 按人员撤离模式：显示当前搜索结果 -->
        <div
          v-if="searchResults.length > 0"
          class="current-search-results"
          :class="{ collapsed: currentResultsCollapsed }"
        >
          <div class="search-results-header" @click="toggleCurrentResults">
            <div class="header-left">
              <span class="collapse-icon" :class="{ collapsed: currentResultsCollapsed }">
                <svg width="12" height="12" viewBox="0 0 12 12" fill="currentColor">
                  <path
                    d="M4.5 3L7.5 6L4.5 9"
                    stroke="currentColor"
                    stroke-width="1.5"
                    fill="none"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
              </span>
              <span class="results-title"
                >当前搜索结果 ({{ Math.min(searchResults.length, maxDisplayResults)
                }}{{ searchResults.length > maxDisplayResults ? '+' : '' }})</span
              >
            </div>
            <div class="header-actions" @click.stop>
              <a-button type="primary" size="small" @click="addCurrentResults">添加到列表</a-button>
              <a-button type="link" size="small" @click="clearCurrentSearch">清空</a-button>
            </div>
          </div>
          <div v-if="!currentResultsCollapsed" class="results-list">
            <div v-if="searchResults.length > maxDisplayResults" class="results-overflow-notice">
              <span
                >显示前 {{ maxDisplayResults }} 条结果，共找到 {{ searchResults.length }} 条</span
              >
            </div>

            <div
              v-for="person in displayedSearchResults"
              :key="person.id"
              class="result-item"
              :class="{ 'already-added': isPersonInAllResults(person.id) }"
            >
              <div class="person-info">
                <div class="person-name">{{ person.name }}</div>
                <div class="person-details">
                  {{ person.company }} - {{ person.department }}
                  <span v-if="person.team"> - {{ person.team }}</span>
                </div>
              </div>
              <div class="item-actions">
                <span v-if="isPersonInAllResults(person.id)" class="already-added-text"
                  >已添加</span
                >
                <a-button v-else type="link" size="small" @click="addSinglePerson(person)"
                  >添加</a-button
                >
              </div>
            </div>
          </div>
        </div>

        <!-- 按人员撤离模式：显示累积的搜索结果 -->
        <div
          v-if="allSearchResults.length > 0"
          class="all-search-results"
          :class="{ collapsed: allResultsCollapsed }"
        >
          <div class="search-results-header" @click="toggleAllResults">
            <div class="header-left">
              <span class="collapse-icon" :class="{ collapsed: allResultsCollapsed }">
                <svg width="12" height="12" viewBox="0 0 12 12" fill="currentColor">
                  <path
                    d="M4.5 3L7.5 6L4.5 9"
                    stroke="currentColor"
                    stroke-width="1.5"
                    fill="none"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
              </span>
              <span class="results-title">已添加人员 ({{ allSearchResults.length }})</span>
            </div>
            <div class="header-actions" @click.stop>
              <a-button type="link" size="small" @click="clearAllResults">清空全部</a-button>
            </div>
          </div>
          <div v-if="!allResultsCollapsed" class="results-list">
            <div
              v-for="person in allSearchResults"
              :key="person.id"
              class="result-item"
              :class="{ selected: checkedKeysRef.includes(`search-${person.id}`) }"
              @click="togglePersonSelection(person)"
            >
              <a-checkbox
                :checked="checkedKeysRef.includes(`search-${person.id}`)"
                @change="(e) => handlePersonCheck(person, e.target.checked)"
              />
              <div class="person-info">
                <div class="person-name">{{ person.name }}</div>
                <div class="person-details">
                  {{ person.company }} - {{ person.department }}
                  <span v-if="person.team"> - {{ person.team }}</span>
                </div>
              </div>
              <div class="item-actions">
                <a-button type="link" size="small" danger @click="removePerson(person)"
                  >移除</a-button
                >
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 显示组织树（全体撤离时禁用，其他模式正常显示） -->
      <div v-if="!disabled" class="tree-container">
        <Tree
          :key="treeKey"
          :tree-data="treeData"
          v-model:expandedKeys="expandedKeys"
          v-model:selectedKeys="selectedKeys"
          :checkable="checkable"
          v-model:checkedKeys="checkedKeysRef"
          :checkStrictly="false"
          :fieldNames="{ title: 'title', key: 'value', children: 'children' }"
          @check="handleCheck"
          @select="handleTreeSelect"
          :loadData="onLoadData"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref, computed, onMounted, defineProps, defineEmits, watch, nextTick } from 'vue';
  import { Tree, Input, Spin, message } from 'ant-design-vue';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { EvacuationPlanEnum } from '../oneKeyRecall.data';
  import { fetchOrgTreeData, type OrgTreeNode } from '@/api/swm/organizationTree';
  import { searchPersons, type PersonInfo } from '@/api/swm/person';

  const { createMessage } = useMessage();

  const props = defineProps({
    evacuationPlan: {
      type: String,
      required: true,
    },
    modelValue: {
      type: Array as () => string[],
      default: () => [],
    },
  });

  const emit = defineEmits(['update:modelValue', 'check']);

  // 内部状态
  const searchValue = ref<string>('');
  const treeData = ref<any[]>([]);
  const expandedKeys = ref<string[]>([]);
  const selectedKeys = ref<string[]>([]);
  const checkedKeysRef = ref<string[]>([]);
  const treeLoading = ref<boolean>(false);
  const searchResults = ref<PersonInfo[]>([]);
  const showSearchResults = ref<boolean>(false);
  const allSearchResults = ref<PersonInfo[]>([]);

  // 添加树组件的key，用于强制重新渲染
  const treeKey = ref<number>(0);

  // 新增：搜索结果显示控制
  const maxDisplayResults = ref<number>(10);
  const currentResultsCollapsed = ref<boolean>(false);
  const allResultsCollapsed = ref<boolean>(false);

  // 根据撤离方案确定是否是叶子节点
  const isNodeLeaf = (node: any) => {
    if (props.evacuationPlan === EvacuationPlanEnum.BY_WORKSHOP) {
      return node.nodeType === 'workshop';
    } else if (props.evacuationPlan === EvacuationPlanEnum.BY_GROUP) {
      return node.nodeType === 'workGroup';
    } else if (props.evacuationPlan === EvacuationPlanEnum.BY_AREA) {
      return node.nodeType === 'prodLine';
    } else if (props.evacuationPlan === EvacuationPlanEnum.BY_PERSONNEL) {
      return node.nodeType === 'worker';
    }
    return node.nodeType === 'worker';
  };

  // 判断节点是否可以被选中（按人员撤离时只有worker节点可选）
  const isNodeCheckable = (node: any) => {
    if (props.evacuationPlan === EvacuationPlanEnum.BY_PERSONNEL) {
      return node.nodeType === 'worker';
    }
    // 其他撤离方案下，根据isNodeLeaf判断
    return isNodeLeaf(node);
  };

  // 判断节点的复选框是否应该被禁用
  const shouldDisableCheckbox = (node: any) => {
    if (props.evacuationPlan === EvacuationPlanEnum.BY_PERSONNEL) {
      return node.nodeType !== 'worker';
    }
    // 其他撤离方案下，非叶子节点禁用复选框
    return !isNodeLeaf(node);
  };

  // 计算属性
  const checkable = computed(() => props.evacuationPlan !== EvacuationPlanEnum.ALL);
  const disabled = computed(() => props.evacuationPlan === EvacuationPlanEnum.ALL);
  const showSearch = computed(() => props.evacuationPlan === EvacuationPlanEnum.BY_PERSONNEL);
  const hasAnySearchResults = computed(
    () => searchResults.value.length > 0 || allSearchResults.value.length > 0,
  );
  const displayedSearchResults = computed(() =>
    searchResults.value.slice(0, maxDisplayResults.value),
  );

  // 监听父组件传入的modelValue变化
  watch(
    () => props.modelValue,
    (newValue) => {
      checkedKeysRef.value = newValue || [];
    },
    { immediate: true },
  );

  // 监听撤离方案变化，重新加载树数据
  watch(
    () => props.evacuationPlan,
    async () => {
      console.log('撤离方案变化，重新加载树数据');
      // 清空所有状态
      expandedKeys.value = [];
      selectedKeys.value = [];
      checkedKeysRef.value = [];
      treeData.value = [];
      emit('update:modelValue', []);
      clearSearch();

      // 强制重新渲染树组件
      treeKey.value += 1;

      // 使用 nextTick 确保DOM更新后再加载数据
      await nextTick();

      if (props.evacuationPlan !== EvacuationPlanEnum.ALL) {
        await loadTreeData();
      }
    },
  );

  // 组件挂载时加载组织树数据
  onMounted(async () => {
    if (props.evacuationPlan !== EvacuationPlanEnum.ALL) {
      await loadTreeData();
    }
  });

  // 加载组织树数据
  async function loadTreeData() {
    treeLoading.value = true;
    try {
      const result = await fetchOrgTreeData('root');
      if (result && Array.isArray(result)) {
        treeData.value = result.map((item) => ({
          ...item,
          isLeaf: isNodeLeaf(item),
          checkable: isNodeCheckable(item),
          disableCheckbox: shouldDisableCheckbox(item),
          children: [],
        }));

        console.log('加载的树数据:', treeData.value);
        console.log('当前撤离方案:', props.evacuationPlan);
        console.log('checkable状态:', checkable.value);

        // 打印第一个节点的详细信息
        if (treeData.value.length > 0) {
          console.log('第一个根节点详细信息:', treeData.value[0]);
        }
      } else {
        message.error('加载组织树数据失败');
      }
    } catch (error) {
      console.error('加载组织树数据失败:', error);
      message.error('加载组织树数据失败');
    } finally {
      treeLoading.value = false;
    }
  }

  // 处理树节点展开事件，动态加载子节点
  async function onLoadData(treeNode: any) {
    return new Promise<void>(async (resolve) => {
      // 检查是否已经加载过
      if (treeNode.dataRef.children && treeNode.dataRef.children.length > 0) {
        resolve();
        return;
      }

      // 检查是否是叶子节点
      if (isNodeLeaf(treeNode.dataRef)) {
        console.log(
          `节点${treeNode.dataRef.title}(${treeNode.dataRef.nodeType})是叶子节点，不加载子节点`,
        );
        treeNode.dataRef.isLeaf = true;
        resolve();
        return;
      }

      try {
        const nodeType = treeNode.dataRef.nodeType;
        const nodeId = treeNode.dataRef.id || treeNode.dataRef.value;

        console.log(`加载 ${nodeType} 节点(${nodeId})的子节点`);
        // API需要传入父节点的nodeType，而不是期望的子节点类型
        const children = await fetchOrgTreeData(nodeType, nodeId);

        if (children && Array.isArray(children)) {
          console.log(`获取到 ${children.length} 个子节点`);
          if (children.length > 0) {
            console.log('第一个子节点示例:', children[0]);
            console.log(
              '子节点的nodeType列表:',
              children.map((c) => c.nodeType),
            );
          }

          treeNode.dataRef.children = children.map((item) => ({
            ...item,
            isLeaf: isNodeLeaf(item),
            checkable: isNodeCheckable(item),
            disableCheckbox: shouldDisableCheckbox(item),
            children: [],
          }));

          // 强制更新树数据
          treeData.value = [...treeData.value];
        }

        resolve();
      } catch (error) {
        console.error('加载子节点失败:', error);
        message.error('加载子节点失败');
        resolve();
      }
    });
  }

  // 处理搜索
  async function handleSearch(value: string) {
    if (!value.trim()) {
      clearCurrentSearch();
      return;
    }

    try {
      treeLoading.value = true;

      const result = await searchPersons({
        keyword: value.trim(),
        searchType: 'name',
        pageSize: 100,
      });

      if (result && result.success) {
        searchResults.value = result.list || [];

        if (searchResults.value.length === 0) {
          createMessage.info('未找到匹配的人员');
        } else {
          createMessage.success(`找到 ${searchResults.value.length} 名匹配的人员`);
        }
      } else {
        searchResults.value = [];
        createMessage.warning('搜索失败，请重试');
      }
    } catch (error) {
      console.error('搜索人员失败:', error);
      createMessage.error('搜索失败，请重试');
      searchResults.value = [];
    } finally {
      treeLoading.value = false;
    }
  }

  // 清空当前搜索
  function clearCurrentSearch() {
    searchValue.value = '';
    searchResults.value = [];
  }

  // 清空所有搜索结果
  function clearAllResults() {
    // 先获取要清除的人员虚拟节点ID（格式：search-{personId}）
    const searchPersonIds = allSearchResults.value.map((p) => `search-${p.id}`);
    // 清空显示列表
    allSearchResults.value = [];
    // 正确过滤掉搜索相关的选中项
    checkedKeysRef.value = checkedKeysRef.value.filter((id) => !searchPersonIds.includes(id));
    emit('update:modelValue', checkedKeysRef.value);
    emit('check', checkedKeysRef.value);
  }

  // 清空搜索（兼容旧的调用）
  function clearSearch() {
    clearCurrentSearch();
    clearAllResults();
  }

  // 折叠/展开控制方法
  function toggleCurrentResults() {
    currentResultsCollapsed.value = !currentResultsCollapsed.value;
  }

  function toggleAllResults() {
    allResultsCollapsed.value = !allResultsCollapsed.value;
  }

  // 检查人员是否已在累积结果中
  function isPersonInAllResults(personId: string): boolean {
    return allSearchResults.value.some((person) => person.id === personId);
  }

  // 添加单个人员到累积结果
  function addSinglePerson(person: PersonInfo) {
    if (!isPersonInAllResults(person.id)) {
      allSearchResults.value.push(person);
      const virtualNodeId = `search-${person.id}`;
      if (!checkedKeysRef.value.includes(virtualNodeId)) {
        checkedKeysRef.value.push(virtualNodeId);
        emit('update:modelValue', checkedKeysRef.value);
        emit('check', checkedKeysRef.value);
      }
      createMessage.success(`已添加并选中 ${person.name}`);
    }
  }

  // 添加当前搜索结果到累积结果
  function addCurrentResults() {
    let addedCount = 0;
    const newlyAddedIds: string[] = [];

    searchResults.value.forEach((person) => {
      if (!isPersonInAllResults(person.id)) {
        allSearchResults.value.push(person);
        const virtualNodeId = `search-${person.id}`;
        newlyAddedIds.push(virtualNodeId);
        addedCount++;
      }
    });

    if (addedCount > 0) {
      newlyAddedIds.forEach((virtualNodeId) => {
        if (!checkedKeysRef.value.includes(virtualNodeId)) {
          checkedKeysRef.value.push(virtualNodeId);
        }
      });

      emit('update:modelValue', checkedKeysRef.value);
      emit('check', checkedKeysRef.value);

      createMessage.success(`已添加并选中 ${addedCount} 名人员`);
    } else {
      createMessage.info('所有人员都已在列表中');
    }

    clearCurrentSearch();
  }

  // 从累积结果中移除人员
  function removePerson(person: PersonInfo) {
    const index = allSearchResults.value.findIndex((p) => p.id === person.id);
    if (index > -1) {
      allSearchResults.value.splice(index, 1);

      const virtualNodeId = `search-${person.id}`;
      const checkedIndex = checkedKeysRef.value.indexOf(virtualNodeId);
      if (checkedIndex > -1) {
        checkedKeysRef.value.splice(checkedIndex, 1);
        emit('update:modelValue', checkedKeysRef.value);
        emit('check', checkedKeysRef.value);
      }

      createMessage.success(`已移除 ${person.name}`);
    }
  }

  // 切换人员选择状态
  function togglePersonSelection(person: PersonInfo) {
    const virtualNodeId = `search-${person.id}`;
    const isSelected = checkedKeysRef.value.includes(virtualNodeId);
    handlePersonCheck(person, !isSelected);
  }

  // 处理人员选择
  function handlePersonCheck(person: PersonInfo, checked: boolean) {
    const virtualNodeId = `search-${person.id}`;

    if (checked) {
      if (!checkedKeysRef.value.includes(virtualNodeId)) {
        checkedKeysRef.value.push(virtualNodeId);
      }
    } else {
      const index = checkedKeysRef.value.indexOf(virtualNodeId);
      if (index > -1) {
        checkedKeysRef.value.splice(index, 1);
      }
    }

    emit('update:modelValue', checkedKeysRef.value);
    emit('check', checkedKeysRef.value);
  }

  // 处理树节点选择
  function handleTreeSelect(selectedKeysValue: string[], e: any) {
    selectedKeys.value = selectedKeysValue;
    if (selectedKeysValue.length > 0) {
      const selectedKey = selectedKeysValue[0];
      const node = e.node;
      console.log('选中节点:', node);
    }
  }

  // 处理复选框选择
  function handleCheck(checkedKeysValue: any, e: any) {
    console.log('handleCheck 被调用:', checkedKeysValue, e);
    console.log('当前撤离方案:', props.evacuationPlan);
    console.log('checkable状态:', checkable.value);

    let actualCheckedKeys = Array.isArray(checkedKeysValue)
      ? checkedKeysValue
      : checkedKeysValue.checked || [];

    // 在按人员撤离模式下，过滤掉非worker节点
    if (props.evacuationPlan === EvacuationPlanEnum.BY_PERSONNEL) {
      // 获取所有节点的映射
      const nodeMap = new Map();
      const buildNodeMap = (nodes: any[]) => {
        nodes.forEach((node) => {
          nodeMap.set(node.value || node.key, node);
          if (node.children && node.children.length > 0) {
            buildNodeMap(node.children);
          }
        });
      };
      buildNodeMap(treeData.value);

      // 过滤只保留worker节点
      actualCheckedKeys = actualCheckedKeys.filter((key) => {
        const node = nodeMap.get(key);
        return node && node.nodeType === 'worker';
      });
    }

    console.log('过滤后的选中keys:', actualCheckedKeys);
    console.log('当前树数据:', treeData.value);

    checkedKeysRef.value = actualCheckedKeys;

    emit('update:modelValue', actualCheckedKeys);
    emit('check', actualCheckedKeys);
  }

  // 获取原始树形数据的方法（供父组件调用）
  function getOriginalTreeData() {
    if (
      props.evacuationPlan === EvacuationPlanEnum.BY_PERSONNEL &&
      allSearchResults.value.length > 0
    ) {
      const searchTreeData = allSearchResults.value.map((person) => {
        const virtualNodeId = `search-${person.id}`;
        return {
          id: virtualNodeId,
          name: person.name,
          title: person.name,
          value: virtualNodeId,
          key: virtualNodeId,
          nodeType: 'worker',
          isLeaf: true,
          leaf: true,
          personInfo: person,
          identityCard: person.identityCard,
          idCard: person.identityCard,
          personId: person.id,
          parentId: 'search-root',
          children: [],
        };
      });

      return [...searchTreeData, ...treeData.value];
    }
    return treeData.value;
  }

  // 暴露方法给父组件调用
  defineExpose({
    getOriginalTreeData,
  });
</script>

<style lang="less" scoped>
  .personnel-tree {
    display: flex;
    flex-direction: column;
    height: 100%;

    .search-section {
      padding: 12px;
      border-bottom: 1px solid #f0f0f0;
    }

    .tree-section {
      flex: 1;
      display: flex;
      flex-direction: column;
      overflow: hidden;
      padding: 12px;
      position: relative;
      min-height: 0;

      .tree-loading {
        display: flex;
        justify-content: center;
        align-items: center;
        height: 100%;
      }

      .tree-disabled-notice {
        display: flex;
        justify-content: center;
        align-items: center;
        height: 100%;
        min-height: 200px;

        .notice-content {
          text-align: center;
          padding: 24px;
          background-color: #f8f9fa;
          border-radius: 8px;
          border: 1px solid #e9ecef;
          max-width: 280px;

          .notice-icon {
            font-size: 48px;
            margin-bottom: 16px;
            opacity: 0.6;
          }

          .notice-text {
            .notice-title {
              font-size: 16px;
              font-weight: 600;
              color: #495057;
              margin-bottom: 8px;
            }

            .notice-desc {
              font-size: 14px;
              color: #6c757d;
              line-height: 1.5;
            }
          }
        }
      }

      /* 搜索结果容器 - 动态高度分配 */
      .search-results-container {
        display: flex;
        flex-direction: column;
        gap: 8px;
        margin-bottom: 12px;

        /* 当有搜索结果时，限制最大高度为75% */
        &.has-results {
          max-height: 75%;
          overflow: hidden;
        }

        /* 当两个搜索结果区域都展开时，增加最大高度 */
        &.both-expanded {
          max-height: 80%;
        }
      }

      /* 树容器 - 占用剩余空间 */
      .tree-container {
        flex: 1;
        overflow: auto;
        min-height: 0;

        /* 美化滚动条 */
        &::-webkit-scrollbar {
          width: 6px;
        }

        &::-webkit-scrollbar-track {
          background: #f1f1f1;
          border-radius: 3px;
        }

        &::-webkit-scrollbar-thumb {
          background: #c1c1c1;
          border-radius: 3px;

          &:hover {
            background: #a8a8a8;
          }
        }
      }

      .current-search-results,
      .all-search-results {
        display: flex;
        flex-direction: column;
        border: 1px solid #e9ecef;
        border-radius: 6px;
        background-color: #fff;
        overflow: hidden;
        transition: all 0.3s ease;

        /* 动态高度分配 */
        &:not(.collapsed) {
          flex: 1;
          min-height: 150px;
          max-height: 400px;
        }

        &.collapsed {
          flex: none;
          height: auto;
        }
      }

      /* 当两个搜索结果区域都展开时的特殊处理 */
      .search-results-container.both-expanded {
        .current-search-results:not(.collapsed),
        .all-search-results:not(.collapsed) {
          max-height: 300px;
        }
      }

      .current-search-results {
        border-color: #1890ff;

        .search-results-header {
          background-color: #e6f7ff;
        }
      }

      .all-search-results {
        border-color: #52c41a;

        .search-results-header {
          background-color: #f6ffed;
        }
      }

      .search-results-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 16px;
        border-bottom: 1px solid #f0f0f0;
        cursor: pointer;
        user-select: none;
        transition: background-color 0.2s ease;

        &:hover {
          opacity: 0.9;
        }

        .header-left {
          display: flex;
          align-items: center;
          gap: 8px;
          flex: 1;
        }

        .collapse-icon {
          display: inline-flex;
          align-items: center;
          justify-content: center;
          width: 16px;
          height: 16px;
          transition: transform 0.2s ease;
          color: #666;

          &.collapsed {
            transform: rotate(-90deg);
          }
        }

        .results-title {
          font-weight: 500;
          color: #1f2937;
        }

        .header-actions {
          display: flex;
          gap: 8px;
          flex-shrink: 0;
        }
      }

      .no-results {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .results-list {
        flex: 1;
        overflow-y: auto;
        padding: 8px;
        min-height: 0;
        max-height: 300px;

        /* 美化滚动条 */
        &::-webkit-scrollbar {
          width: 4px;
        }

        &::-webkit-scrollbar-track {
          background: #f8f9fa;
          border-radius: 2px;
        }

        &::-webkit-scrollbar-thumb {
          background: #d1d5db;
          border-radius: 2px;

          &:hover {
            background: #9ca3af;
          }
        }

        .results-overflow-notice {
          background-color: #fff7e6;
          border: 1px solid #ffd591;
          border-radius: 4px;
          padding: 8px 12px;
          margin-bottom: 8px;
          font-size: 12px;
          color: #d46b08;
          text-align: center;
        }

        .result-item {
          display: flex;
          align-items: center;
          padding: 6px 10px;
          border-radius: 6px;
          margin-bottom: 3px;
          border: 1px solid #f0f0f0;
          cursor: pointer;
          transition: all 0.2s;
          background-color: #fff;
          min-height: 50px;

          &:hover {
            background-color: #f5f7fa;
            border-color: #d1d5db;
            transform: translateY(-1px);
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
          }

          &.selected {
            background-color: #e6f7ff;
            border-color: #1890ff;
          }

          &.already-added {
            background-color: #f6f6f6;
            border-color: #d9d9d9;
            cursor: default;

            .person-name {
              color: #8c8c8c;
            }
          }

          .person-info {
            margin-left: 8px;
            flex: 1;
            min-width: 0;

            .person-name {
              font-weight: 500;
              color: #1f2937;
              margin-bottom: 1px;
              font-size: 13px;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
            }

            .person-details {
              font-size: 10px;
              color: #6b7280;
              line-height: 1.2;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
            }
          }

          .item-actions {
            margin-left: 8px;
            flex-shrink: 0;

            .already-added-text {
              color: #52c41a;
              font-size: 11px;
              font-weight: 500;
            }
          }
        }
      }
    }
  }
</style>
