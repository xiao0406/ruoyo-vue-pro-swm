<!--
  @description 一键召回页面
  @date 2024-06-10
-->
<template>
  <div class="one-key-recall-container">
    <div class="recall-main-content">
      <div class="recall-left-section">
        <!-- 语音模板选择 -->
        <TemplateSelector
          :modelValue="selectedTemplate"
          :templateContent="templateContent"
          @update:modelValue="selectedTemplate = $event"
          @update:templateContent="templateContent = $event"
          @update:voiceText="voiceText = $event"
          @update:pushMethod="pushMethod = $event"
          @template-change="handleTemplateChange"
        />

        <!-- 撤离方案 -->
        <EvacuationPlanSelector
          v-model="evacuationPlan"
          :selectedTargets="selectedTargets"
          @plan-change="handleEvacuationPlanChange"
        />

        <!-- 推送按钮 -->
        <PushButton :loading="sending" @click="handleSendRecall" />
      </div>

      <!-- 可拖动的分隔条 -->
      <!-- <ResizeHandle 
        @resize-start="startResize"
        @reset-width="resetWidth"
      /> -->

      <!-- 右侧树形结构和搜索框 -->
      <div class="recall-right-section">
        <div class="p-3" v-if="evacuationPlan == EvacuationPlanEnum.BY_PERSONNEL_TYPE">
          <Tree
            v-model:checkedKeys="selectedPersonnelTypes"
            checkable
            :treeData="personnelTypesOptions"
          />
        </div>

        <PersonnelTree
          v-else
          v-model="selectedTargets"
          :evacuationPlan="evacuationPlan"
          @check="handleCheck"
          ref="personnelTreeRef"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref, onUnmounted, reactive, provide, onMounted } from 'vue';
  import { useMessage } from '@/hooks/swm/useMessage';
  import {
    EvacuationPlanEnum,
    sendOneKeyRecall,
    sendBroadcastToAll,
    sendBroadcastToSelectedTargets,
    CUSTOM_TEMPLATE_CODE,
  } from './oneKeyRecall.data';
  import { Tree } from 'ant-design-vue';

  // 导入子组件
  import TemplateSelector from './components/TemplateSelector.vue';
  import EvacuationPlanSelector from './components/EvacuationPlanSelector.vue';
  import PersonnelTree from './components/PersonnelTree.vue';
  import ResizeHandle from './components/ResizeHandle.vue';
  import PushButton from './components/PushButton.vue';
  import { useDict } from '@/components/swm/Dict';

  const { initDict, getDictList } = useDict();

  // 引入消息提示
  const { createMessage } = useMessage();

  // 语音模板相关
  const selectedTemplate = ref<string>('');
  const templateContent = ref<string>('');
  const voiceText = ref<string>('');
  const pushInfo = reactive({
    // 频率、次数
    frequency: 0,
    count: 1,
  });
  provide('pushInfo', pushInfo);
  const pushMethod = ref<string>('');
  // 撤离方案相关
  const evacuationPlan = ref<string>(EvacuationPlanEnum.ALL);

  // 树形数据相关
  const selectedTargets = ref<string[]>([]);
  const personnelTreeRef = ref<InstanceType<typeof PersonnelTree> | null>(null);

  const selectedPersonnelTypes = ref<string[]>([]); // 选中的人员类型
  const personnelTypesOptions = ref<any>([]);

  onMounted(async () => {
    await initDict(['person_type_enum']);
    personnelTypesOptions.value = getDictList('person_type_enum').map((item) => ({
      key: item.value,
      title: item.name,
    }));
  });

  // 右侧区域宽度调整相关
  const rightSectionWidth = ref<number>(280);
  const initialWidth = 280;
  const minWidth = 180;
  const maxWidth = 600;
  let isResizing = false;

  // 开始调整宽度
  const startResize = (e: MouseEvent) => {
    isResizing = true;
    document.addEventListener('mousemove', handleResize);
    document.addEventListener('mouseup', stopResize);
    e.preventDefault(); // 阻止默认行为
  };

  // 处理拖动调整宽度
  const handleResize = (e: MouseEvent) => {
    if (!isResizing) return;

    const container = document.querySelector('.recall-main-content') as HTMLElement;
    if (!container) return;

    // 计算容器相对于视口的位置
    const containerRect = container.getBoundingClientRect();

    // 计算右侧区域的新宽度
    let newWidth = containerRect.right - e.clientX;

    // 限制宽度在合理范围内
    newWidth = Math.max(minWidth, Math.min(maxWidth, newWidth));

    rightSectionWidth.value = newWidth;
  };

  // 停止调整宽度
  const stopResize = () => {
    isResizing = false;
    document.removeEventListener('mousemove', handleResize);
    document.removeEventListener('mouseup', stopResize);
  };

  // 重置宽度
  const resetWidth = () => {
    rightSectionWidth.value = initialWidth;
  };

  // 处理语音模板变更
  const handleTemplateChange = (value: string) => {
    // 此处可以添加额外的处理逻辑
  };

  // 处理撤离方案变更
  const handleEvacuationPlanChange = () => {
    // 清空已选目标
    selectedTargets.value = [];
  };

  // 处理目标检查变更
  const handleCheck = (checkedKeys: string[]) => {
    selectedTargets.value = checkedKeys;
  };

  // 其他状态
  const sending = ref<boolean>(false);

  // 卸载时移除事件监听
  onUnmounted(() => {
    document.removeEventListener('mousemove', handleResize);
    document.removeEventListener('mouseup', stopResize);
  });

  // 发送一键召回请求
  const handleSendRecall = async () => {
    // 表单验证
    if (!voiceText.value.trim()) {
      createMessage.warning('请输入通知内容');
      return;
    }
    // 表单验证
    if (!pushMethod.value.trim()) {
      createMessage.warning('请选择推送方式');
      return;
    }

    if (
      evacuationPlan.value == EvacuationPlanEnum.BY_PERSONNEL_TYPE &&
      selectedPersonnelTypes.value.length === 0
    ) {
      createMessage.warning('请选择人员类型');
      return;
    }

    if (
      evacuationPlan.value !== EvacuationPlanEnum.ALL &&
      selectedTargets.value.length === 0 &&
      evacuationPlan.value != EvacuationPlanEnum.BY_PERSONNEL_TYPE
    ) {
      createMessage.warning('请选择撤离对象');
      return;
    }

    // 获取模板名称 (从下拉选择框选项中获取对应的label)
    const templateNameObj = document.querySelector(`[title="${selectedTemplate.value}"]`);
    const templateName = templateNameObj
      ? templateNameObj.textContent?.trim() || '自定义通知'
      : '自定义通知';

    // 构建evacueeList (根据所选撤离方案和选定目标生成)
    let evacueeList: string[] = [];

    // 获取原始树形数据
    const originalTreeData = personnelTreeRef.value?.getOriginalTreeData() || [];

    // 如果是全体撤离，获取所有叶子节点
    if (evacuationPlan.value === EvacuationPlanEnum.ALL) {
      // 递归获取所有叶子节点的名称
      const getAllLeafNodes = (nodes: any[]): string[] => {
        let result: string[] = [];
        for (const node of nodes) {
          if (node.isLeaf) {
            result.push(node.name);
          } else if (node.children && node.children.length > 0) {
            result = result.concat(getAllLeafNodes(node.children));
          }
        }
        return result;
      };

      evacueeList = getAllLeafNodes(originalTreeData);
    } else if (evacuationPlan.value === EvacuationPlanEnum.BY_PERSONNEL) {
      // 按人员撤离模式，只收集叶子节点（人员）
      console.log('按人员撤离模式 - selectedTargets:', selectedTargets.value);
      console.log('原始树数据:', originalTreeData);

      for (const targetId of selectedTargets.value) {
        // 检查是否是搜索添加的人员（以 search- 开头）
        if (targetId.startsWith('search-')) {
          // 从搜索结果中查找
          const personId = targetId.replace('search-', '');
          const searchNode = originalTreeData.find(
            (node) => node.id === targetId && node.personInfo,
          );
          if (searchNode && searchNode.personInfo) {
            evacueeList.push(searchNode.personInfo.name);
            console.log(`添加搜索人员: ${searchNode.personInfo.name}`);
          }
          continue;
        }

        // 递归查找节点
        const findNode = (nodeId: string, nodes: any[]): any => {
          for (const node of nodes) {
            if (node.id === nodeId || node.value === nodeId || node.key === nodeId) {
              return node;
            }
            if (node.children && node.children.length > 0) {
              const found = findNode(nodeId, node.children);
              if (found) return found;
            }
          }
          return null;
        };

        const node = findNode(targetId, originalTreeData);
        console.log(`查找节点 ${targetId}:`, node);

        if (node) {
          // 检查是否是人员节点
          if (node.nodeType === 'worker' || node.isLeaf) {
            evacueeList.push(node.name || node.title);
            console.log(`添加人员: ${node.name || node.title}`);
          } else {
            console.log(
              `节点 ${targetId} 不是人员节点, nodeType: ${node.nodeType}, isLeaf: ${node.isLeaf}`,
            );
          }
        } else {
          console.log(`未找到节点: ${targetId}`);
        }
      }

      // 按人员撤离模式下，如果没有找到有效人员，提示用户
      if (evacueeList.length === 0) {
        createMessage.warning('未找到有效的撤离人员，请重新选择');
        return;
      }
    } else {
      // 其他撤离模式（按车间、按班组、按区域）
      // 根据选中的节点ID获取对应的名称
      const getNodeNameById = (nodeId: string, nodes: any[]): string | null => {
        for (const node of nodes) {
          if (node.id === nodeId) {
            return node.name;
          }
          if (node.children && node.children.length > 0) {
            const name = getNodeNameById(nodeId, node.children);
            if (name) return name;
          }
        }
        return null;
      };

      for (const targetId of selectedTargets.value) {
        const name = getNodeNameById(targetId, originalTreeData);
        if (name) {
          evacueeList.push(name);
        }
      }
    }

    // 构建请求数据
    let requestData = {
      templateName,
      templateContent: templateContent.value,
      evacuationPlan: evacuationPlan.value,
      evacueeList: JSON.stringify(evacueeList), // 序列化为JSON字符串
      recallResult: '0', // 初始状态为"进行中"
      pushMethod: pushMethod.value, // 推送方式
      recallFrequency: pushInfo.frequency,
      recallCount: pushInfo.count,
      voiceText: voiceText.value, // 语音文本
      originalTreeData: personnelTreeRef.value?.getOriginalTreeData() || [],
      selectedTargets: selectedTargets.value, // 选中的人员ID列表
    };

    if (evacuationPlan.value === EvacuationPlanEnum.BY_PERSONNEL_TYPE) {
      Object.assign(requestData, {
        selectedTargets: selectedPersonnelTypes.value,
        originalTreeData: [],
      });
    }

    // 发送请求
    try {
      sending.value = true;

      // 首先保存记录到数据库
      const result = await sendOneKeyRecall(requestData);
      const { success, message } = result;
      if (success) {
        createMessage.success(message || '一键撤离成功');
      } else {
        createMessage.warning(message);
      }
      console.log('sendOneKeyRecall===', result);
      return;
      // 如果是全体撤离，则同时发送WebSocket广播
      try {
        const broadcastResult = await sendBroadcastToAll(templateContent.value, voiceText.value);

        if (broadcastResult && broadcastResult.success) {
          const { tcp } = broadcastResult;
          let message = '已推送给全体成员';

          // TCP结果显示
          if (tcp) {
            if (tcp.skipped) {
              message += `\nTCP：${tcp.reason || '已跳过'}`;
            } else {
              // 显示推送成功的人员
              if (tcp.successPersons && tcp.successPersons.length > 0) {
                message += `\n✅ 推送成功人员：${tcp.successPersons.join('、')}`;
              }
              // 显示推送失败的人员
              if (tcp.failedPersons && tcp.failedPersons.length > 0) {
                message += `\n❌ 推送失败人员（设备未开机）：${tcp.failedPersons.join('、')}`;
              }
              // 如果没有人员数据，显示总数
              if (
                (!tcp.successPersons || tcp.successPersons.length === 0) &&
                (!tcp.failedPersons || tcp.failedPersons.length === 0)
              ) {
                if (tcp.totalDevices && tcp.totalDevices > 0) {
                  message += `\n共向${tcp.totalDevices}台设备推送`;
                }
              }
            }
          }

          createMessage.success(message);
        } else {
          createMessage.warning(`推送失败：${broadcastResult?.message || '未知错误'}`);
        }
      } catch (broadcastError) {
        console.error('WebSocket广播失败:', broadcastError);
        createMessage.warning('未推送成功');
      }
      if (evacuationPlan.value === EvacuationPlanEnum.ALL) {
      } else {
        // 非全体撤离，向选中的目标发送召回消息
        try {
          const broadcastData = {
            selectedTargets: selectedTargets.value,
            templateContent: templateContent.value,
            voiceText: voiceText.value,
            originalTreeData: personnelTreeRef.value?.getOriginalTreeData() || [],
          };

          const broadcastResult = await sendBroadcastToSelectedTargets(broadcastData);

          if (broadcastResult && broadcastResult.success) {
            const { totalPersonnel, totalDevices, websocket, tcp } = broadcastResult;

            let message = `查询到${totalPersonnel}名人员，${totalDevices}台设备。`;

            // WebSocket结果
            if (websocket) {
              // 当前不需要下发websocket，润德预留的。
              // message += `\nWebSocket：成功${websocket.successCount}条，失败${websocket.failCount}条。`;
            }

            // TCP结果
            if (tcp) {
              if (tcp.skipped) {
                message += `\nTCP：${tcp.reason || '已跳过'}`;
              } else {
                // 显示推送成功的人员
                if (tcp.successPersons && tcp.successPersons.length > 0) {
                  message += `\n✅ 推送成功人员：${tcp.successPersons.join('、')}`;
                }
                // 显示推送失败的人员
                if (tcp.failedPersons && tcp.failedPersons.length > 0) {
                  message += `\n❌ 推送失败人员（设备未开机）：${tcp.failedPersons.join('、')}`;
                }
                // 如果没有人员数据，保留原有显示格式作为兜底
                if (
                  (!tcp.successPersons || tcp.successPersons.length === 0) &&
                  (!tcp.failedPersons || tcp.failedPersons.length === 0)
                ) {
                  message += `\nTCP：成功${tcp.successCount}条，失败${tcp.failCount}条。`;
                }
              }
            }

            createMessage.success(message);
          } else {
            createMessage.warning(`推送失败：${broadcastResult?.message || '未知错误'}`);
          }
        } catch (broadcastError) {
          createMessage.warning('推送失败');
        }
      }
    } catch (error) {
      createMessage.error('推送失败');
      console.error('推送失败:', error);
    } finally {
      sending.value = false;
    }
  };
</script>

<style lang="less" scoped>
  .one-key-recall-container {
    height: 100%;
    // padding: 24px;
    display: flex;
    flex-direction: column;
    background-color: #f5f7fa;
  }

  .recall-main-content {
    display: flex;
    height: 100%;
    background-color: #ffffff;
    position: relative;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    overflow: hidden;
  }

  .recall-left-section {
    flex: 1;
    padding: 24px 32px;
    overflow-y: auto;
    overflow-x: hidden;
    display: flex;
    flex-direction: column;
    gap: 24px;
    max-height: calc(100vh - 112px);
    /* 根据实际布局调整 */

    /* 确保滚动条样式美观 */
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

  .recall-right-section {
    width: 280px;
    max-height: calc(100vh - 112px);
    /* 根据实际布局调整 */

    // padding: 24px;
    overflow: auto;
    // display: flex;
    // flex-direction: column;
    gap: 16px;
    transition: width 0.2s ease;
    min-width: 180px;
    max-width: 600px;
    background-color: #fafafa;
  }
</style>
