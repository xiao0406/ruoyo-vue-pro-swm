<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="modalTitle"
    :width="1000"
    @ok="handleSubmit"
  >
    <div class="config-container">
      <div class="config-section">
        <div class="section-header">
          <h4>报警配置 ({{ configList.length }}个)</h4>
          <a-button
            type="primary"
            size="small"
            @click="handleAddConfig"
            :disabled="isLoadingBaseData"
          >
            <Icon icon="ant-design:plus-outlined" />
            添加配置
          </a-button>
        </div>

        <div v-if="isLoadingBaseData" class="loading-config">
          <a-spin size="large">
            <div class="loading-text">正在加载配置数据...</div>
          </a-spin>
        </div>

        <div v-else-if="configList.length === 0" class="empty-config">
          <a-empty description="暂无配置，请点击添加配置按钮" />
        </div>

        <div v-else class="config-list">
          <!-- 强制重新渲染的关键标识 -->
          <div key="config-list-force-rerender-v2">
            <div
              v-for="(config, index) in configList"
              :key="`config-${config.id || index}-v2`"
              class="config-item"
            >
              <div
                :style="{
                  border: '1px solid #d9d9d9',
                  borderRadius: '6px',
                  padding: '16px',
                  marginBottom: '16px',
                  background: isExistingConfig(config) ? '#fafafa' : 'white',
                  borderColor: isExistingConfig(config) ? '#e8e8e8' : '#d9d9d9',
                }"
              >
                <div
                  style="
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-bottom: 16px;
                    border-bottom: 1px solid #f0f0f0;
                    padding-bottom: 8px;
                  "
                >
                  <div style="display: flex; align-items: center; gap: 8px">
                    <h4 style="margin: 0; font-size: 14px">配置项 {{ index + 1 }}</h4>
                    <span
                      v-if="isExistingConfig(config)"
                      style="
                        background: #f0f0f0;
                        color: #666;
                        padding: 2px 6px;
                        border-radius: 3px;
                        font-size: 11px;
                      "
                      >已保存</span
                    >
                    <span
                      v-else
                      style="
                        background: #e6f7ff;
                        color: #1890ff;
                        padding: 2px 6px;
                        border-radius: 3px;
                        font-size: 11px;
                      "
                      >新增</span
                    >
                  </div>
                  <button
                    style="
                      border: none;
                      background: none;
                      color: #ff4d4f;
                      cursor: pointer;
                      font-size: 12px;
                    "
                    @click="handleRemoveConfig(index)"
                  >
                    删除
                  </button>
                </div>

                <div style="display: flex; gap: 16px">
                  <div style="flex: 1">
                    <div style="margin-bottom: 8px">
                      <label style="font-weight: 500; color: #262626">报警类型：</label>
                    </div>
                    <select
                      v-model="config.alarmConfigId"
                      @change="validateConfig(config)"
                      :disabled="!!(isLoadingBaseData || isExistingConfig(config))"
                      :style="{
                        width: '100%',
                        height: '32px',
                        border: '1px solid #d9d9d9',
                        borderRadius: '6px',
                        padding: '4px 8px',
                        backgroundColor: isExistingConfig(config) ? '#f5f5f5' : 'white',
                        cursor: isExistingConfig(config) ? 'not-allowed' : 'pointer',
                      }"
                    >
                      <option value="">请选择报警类型</option>
                      <option
                        v-for="option in alarmConfigOptions"
                        :key="option.id"
                        :value="option.id"
                      >
                        {{ option.alarmName }}
                      </option>
                    </select>
                    <div
                      v-if="config.alarmConfigIdError"
                      style="color: #ff4d4f; font-size: 12px; margin-top: 4px"
                    >
                      {{ config.alarmConfigIdError }}
                    </div>
                  </div>

                  <div style="flex: 1">
                    <div style="margin-bottom: 8px">
                      <label style="font-weight: 500; color: #262626">语音模板：</label>
                    </div>
                    <select
                      v-model="config.voiceTemplateId"
                      :disabled="!!(isLoadingBaseData || isExistingConfig(config))"
                      :style="{
                        width: '100%',
                        height: '32px',
                        border: '1px solid #d9d9d9',
                        borderRadius: '6px',
                        padding: '4px 8px',
                        backgroundColor: isExistingConfig(config) ? '#f5f5f5' : 'white',
                        cursor: isExistingConfig(config) ? 'not-allowed' : 'pointer',
                      }"
                    >
                      <option value="">请选择语音模板</option>
                      <option
                        v-for="option in voiceTemplateOptions"
                        :key="option.id"
                        :value="option.id"
                      >
                        {{ option.templateName }}
                      </option>
                    </select>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, ref, reactive, computed } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { Icon } from '@/components/swm/Icon';
  import { useMessage } from '@/hooks/swm/useMessage';
  import {
    getAlarmLightConfigList,
    saveAlarmLightConfig,
    deleteAlarmLightConfig,
    getAlarmConfigList,
    getVoiceTemplateList,
    type AlarmLightConfigInfo,
    type AlarmConfigInfo,
    type VoiceTemplateInfo,
  } from '@/api/swm/alarmLight';

  interface ConfigItem extends AlarmLightConfigInfo {
    tempId?: string;
    alarmConfigIdError?: string;
  }

  export default defineComponent({
    name: 'AlarmLightConfigModal',
    components: { BasicModal, Icon },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { createMessage } = useMessage();
      const deviceInfo = ref<any>({});
      const configList = ref<ConfigItem[]>([]);
      const deletedConfigs = ref<ConfigItem[]>([]);
      const alarmConfigOptions = ref<AlarmConfigInfo[]>([]);
      const voiceTemplateOptions = ref<VoiceTemplateInfo[]>([]);
      const isLoadingBaseData = ref(false);
      let tempIdCounter = 0;

      const modalTitle = computed(
        () => `配置报警灯 [${deviceInfo.value.lightName}] (${configList.value.length}个配置)`,
      );

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: false });
        deviceInfo.value = data.record;

        // 先清空配置列表和下拉框选项，避免显示缓存数据
        configList.value = [];
        deletedConfigs.value = [];
        alarmConfigOptions.value = [];
        voiceTemplateOptions.value = [];

        // 先加载基础数据（下拉框选项），再加载配置数据
        await loadBaseData();
        await loadConfigData();
      });

      async function loadConfigData() {
        try {
          const result = await getAlarmLightConfigList(deviceInfo.value.id);

          if (result && Array.isArray(result.list)) {
            // 确保每个配置项都有响应式的错误属性
            configList.value = result.list.map((item) =>
              reactive({
                ...item,
                alarmConfigIdError: '',
              }),
            );
          } else {
            configList.value = [];
          }
        } catch (error) {
          createMessage.error('加载配置数据失败');
          configList.value = [];
        }
      }

      async function loadBaseData() {
        try {
          // 立即清空选项，避免显示旧数据
          alarmConfigOptions.value = [];
          voiceTemplateOptions.value = [];
          isLoadingBaseData.value = true;

          const [alarmConfigs, voiceTemplates] = await Promise.all([
            getAlarmConfigList({ pageSize: 1000, status: '0' }),
            getVoiceTemplateList({ pageSize: 1000, status: '0' }),
          ]);

          // 设置报警配置数据
          if (alarmConfigs && Array.isArray(alarmConfigs.list)) {
            alarmConfigOptions.value = alarmConfigs.list;
          } else {
            alarmConfigOptions.value = [];
          }

          // 设置语音模板数据
          if (voiceTemplates && Array.isArray(voiceTemplates.list)) {
            voiceTemplateOptions.value = voiceTemplates.list;
          } else {
            voiceTemplateOptions.value = [];
          }
        } catch (error) {
          createMessage.error('加载基础数据失败');
          alarmConfigOptions.value = [];
          voiceTemplateOptions.value = [];
        } finally {
          isLoadingBaseData.value = false;
        }
      }

      function handleAddConfig() {
        const newConfig = reactive({
          id: '',
          lightId: deviceInfo.value.id,
          alarmConfigId: '',
          voiceTemplateId: '',
          tempId: `temp_${++tempIdCounter}`,
          alarmConfigIdError: '',
        });
        configList.value.push(newConfig);
      }

      function handleRemoveConfig(index: number) {
        const config = configList.value[index];

        // 如果是已存在的配置（有id），添加到删除列表
        if (config.id) {
          deletedConfigs.value.push(config);
        }

        // 从配置列表中移除
        configList.value.splice(index, 1);
      }

      function isExistingConfig(config: ConfigItem) {
        // 判断是否为现有配置：有id且没有tempId的是现有配置
        return config.id && !config.tempId;
      }

      function validateConfig(config: ConfigItem) {
        // 现有配置不需要验证，因为它们不能修改
        if (isExistingConfig(config)) {
          return true;
        }

        // 清除当前配置的错误状态
        config.alarmConfigIdError = '';

        if (!config.alarmConfigId) {
          config.alarmConfigIdError = '请选择报警类型';
          return false;
        }

        // 检查重复配置（包括与现有配置的重复检查）
        const duplicateCount = configList.value.filter(
          (item) => item.alarmConfigId === config.alarmConfigId,
        ).length;
        if (duplicateCount > 1) {
          config.alarmConfigIdError = '该报警类型已配置，不能重复';
          return false;
        }

        return true;
      }

      function validateAllConfigs() {
        let isValid = true;

        // 先清理所有错误状态（只清理新增配置的错误状态）
        configList.value.forEach((config) => {
          if (!isExistingConfig(config)) {
            config.alarmConfigIdError = '';
          }
        });

        // 然后重新校验所有配置（现有配置会自动通过验证）
        configList.value.forEach((config) => {
          if (!validateConfig(config)) {
            isValid = false;
          }
        });

        return isValid;
      }

      async function handleSubmit() {
        if (!validateAllConfigs()) {
          // 收集具体的错误信息
          const errors = configList.value
            .map((config, index) =>
              config.alarmConfigIdError ? `配置项${index + 1}: ${config.alarmConfigIdError}` : '',
            )
            .filter((error) => error);

          if (errors.length > 0) {
            createMessage.error(`配置有误：${errors.join('；')}`);
          } else {
            createMessage.error('请检查配置项');
          }
          return;
        }

        try {
          setModalProps({ confirmLoading: true });

          // 1. 先处理删除操作
          for (const deletedConfig of deletedConfigs.value) {
            await deleteAlarmLightConfig(deletedConfig.id);
          }

          // 2. 再处理新增操作（只保存新增的配置，现有配置不处理）
          for (const config of configList.value) {
            if (!isExistingConfig(config) && config.alarmConfigId) {
              // 新增配置：没有id或有tempId的配置
              await saveAlarmLightConfig({
                lightId: deviceInfo.value.id,
                alarmConfigId: config.alarmConfigId,
                voiceTemplateId: config.voiceTemplateId || '',
              } as any);
            }
          }

          // 3. 成功后清空删除列表
          deletedConfigs.value = [];

          createMessage.success('保存配置成功');
          closeModal();
          emit('success');
        } catch (error) {
          createMessage.error('保存配置失败');
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      return {
        registerModal,
        modalTitle,
        deviceInfo,
        configList,
        alarmConfigOptions,
        voiceTemplateOptions,
        isLoadingBaseData,
        handleAddConfig,
        handleRemoveConfig,
        validateConfig,
        handleSubmit,
        isExistingConfig,
      };
    },
  });
</script>

<style lang="less" scoped>
  .config-container {
    padding: 20px 30px;
    min-height: 450px;

    .device-info {
      margin-bottom: 16px;
      padding: 14px;
    }

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      h4 {
        margin: 0;
        font-size: 16px;
        font-weight: 500;
      }
    }

    .loading-config {
      text-align: center;

      .loading-text {
        margin-top: 16px;
        color: #666;
        font-size: 14px;
      }
    }

    .empty-config {
      text-align: center;
      padding: 40px 0;
    }

    .config-list {
      .config-item {
        margin-bottom: 16px;

        &:last-child {
          margin-bottom: 0;
        }

        :deep(.ant-card) {
          border-radius: 6px;
          box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
        }
      }
    }
  }
</style>
