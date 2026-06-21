<!--
  @author Shawn
  @date 2025-05-14
-->
<template>
  <BasicModal
    v-bind="$attrs"
    :title="t('批量导入人员')"
    :okText="t('导入')"
    @register="registerModal"
    @ok="handleSubmit"
    @cancel="handleCancel"
    @afterClose="handleAfterClose"
    :minHeight="120"
    :width="800"
    :maskClosable="false"
    :canFullscreen="false"
  >
    <div style="padding: 0px 20px">
      <div class="upload-container">
        <Upload
          accept=".xls,.xlsx"
          :file-list="fileList"
          :remove="handleRemove"
          :before-upload="beforeUpload"
        >
          <a-button :loading="loading">
            <Icon icon="ant-design:upload-outlined" />
            {{ loading ? t('上传中...') : t('选择文件') }}
          </a-button>
          <span class="ml-4" v-if="uploadInfo">{{ uploadInfo }}</span>
          <span class="ml-4 text-green-500" v-else>{{ t('请选择Excel文件') }}</span>
        </Upload>
        <div class="mt-4">
          <a-button @click="handleDownloadTemplate()" type="primary" ghost>
            <Icon icon="fa:file-excel-o" />
            {{ t('下载模板') }}
          </a-button>
          <span class="ml-2 text-gray-500 text-sm">请先下载模板，按格式填写数据后再导入</span>
        </div>
        <div class="template-tips mt-2">
          <div class="tip-item">
            <Icon icon="ant-design:check-circle-outlined" class="tip-icon success" />
            <span class="tip-text">模板包含所有组织架构选项数据</span>
          </div>
          <div class="tip-item">
            <Icon icon="ant-design:info-circle-outlined" class="tip-icon info" />
            <span class="tip-text">支持级联关系验证，确保数据准确性</span>
          </div>
        </div>
        <div class="mt-4">
          {{ t('提示：仅允许导入"xls"或"xlsx"格式文件！') }}
        </div>

        <!-- 导入结果提示 - 放在显眼位置 -->
        <div v-if="showResult" class="result-container mt-4">
          <Alert
            :message="resultMessage"
            :type="resultType"
            show-icon
            :description="resultDescription"
          />
        </div>

        <a-divider />

        <div class="field-info">
          <div class="field-title">数据字段说明：</div>
          <div class="field-section">
            <div class="field-name">必填字段：</div>
            <div class="field-value">- 姓名：工人姓名，必填</div>
            <div class="field-value">- 人员类型：0-工人，1-管理者，必填</div>
            <div class="field-value">- 性别：男或女，必填</div>
            <div class="field-value">- 身份证号码：18位身份证号码，必填</div>
            <div class="field-value">- 手机号码：11位手机号码，必填</div>
            <div class="field-value">- 是否场内员工：是/否 或 1/0，必填</div>
          </div>
          <div class="field-section">
            <div class="field-name">组织架构字段：</div>
            <div class="field-value field-highlight">
              <Icon icon="ant-design:info-circle-outlined" class="mr-1" />
              当"是否场内员工"选择"是"时，以下字段必填：
            </div>
            <div class="field-value ml-4">- 所属单位：从模板中的单位选项中选择</div>
            <div class="field-value ml-4">- 所属车间：从模板中的车间选项中选择</div>
            <div class="field-value ml-4">- 所属产线：从模板中的产线选项中选择</div>
            <div class="field-value ml-4">- 所属班组：从模板中的班组选项中选择</div>
            <div class="field-value ml-4">- 所属工种：从模板中的工种选项中选择</div>
            <div class="field-value field-warning">
              <Icon icon="ant-design:warning-outlined" class="mr-1" />
              当"是否场内员工"选择"否"时，组织架构字段可以不填写，即使填写了也会被忽略
            </div>
          </div>
          <div class="field-section">
            <div class="field-name">可选字段：</div>
            <div class="field-value">- 备注：其他备注信息</div>
            <div class="field-value">
              - 安全帽编码：填写现有设备编号，如 <strong>4015869001</strong>；留空时不自动绑定安全帽
            </div>
          </div>
          <div class="field-section">
            <div class="field-name">导入规则说明：</div>
            <div class="field-value">- 系统会自动检查身份证号码是否重复</div>
            <div class="field-value">- 如果存在相同身份证的在职人员，导入将会失败</div>
            <div class="field-value">- 新导入的人员"入场安全教育"状态默认为"未开始"</div>
            <div class="field-value">- 新导入的人员"人员状态"默认为"在职"</div>
            <div class="field-value">- 系统会验证组织架构字段的级联关系（单位→车间→产线→班组）</div>
            <div class="field-value">- 非厂内员工的组织架构字段会被自动清空</div>
          </div>
        </div>
      </div>

      <!-- 详细错误信息展示 -->
      <div v-if="showDetailedErrors && validationResults.length > 0" class="error-details mt-4">
        <Collapse>
          <CollapsePanel key="1" header="详细错误信息">
            <div class="error-list">
              <div v-for="(result, index) in validationResults" :key="index" class="error-item">
                <div v-if="result.errors.length > 0" class="error-row">
                  <div class="error-row-header">
                    <Icon icon="ant-design:exclamation-circle-outlined" class="error-icon" />
                    第{{ result.rowIndex }}行错误：
                  </div>
                  <ul class="error-messages">
                    <li v-for="error in result.errors" :key="error" class="error-message">
                      {{ error }}
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </CollapsePanel>
        </Collapse>
      </div>
    </div>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, Ref, onMounted, onBeforeUnmount, nextTick } from 'vue';
  import { t } from '@/hooks/swm/useI18n';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { Upload, Collapse, CollapsePanel, Alert } from 'ant-design-vue';
  import { Icon } from '@/components/swm/Icon';
  import { useMessage } from '@/hooks/swm/useMessage';
  import {
    downloadPersonTemplate,
    downloadEnhancedPersonTemplate,
    importPersonExcel,
    importPersonExcelEnhanced,
  } from '@/api/swm/person';
  import { downloadByUrl } from '@/utils/file/download';
  import { Modal } from 'ant-design-vue';
  import { h } from 'vue';
  import { validateImportData, type ImportRowData } from '@/utils/swm/orgValidation';

  type AlertType = 'success' | 'error' | 'warning' | 'info';

  export default defineComponent({
    name: 'ImportModal',
    components: { BasicModal, Upload, Icon, Collapse, CollapsePanel, Alert },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { createMessage } = useMessage();

      // 上传文件列表
      const fileList = ref<any[]>([]);
      const uploadInfo = ref<string>('');

      // 导入结果相关
      const showResult = ref<boolean>(false);
      const resultMessage = ref<string>('');
      const resultType = ref<AlertType>('info');
      const resultDescription = ref<string>('');

      // 验证结果相关
      const showDetailedErrors = ref<boolean>(false);
      const validationResults = ref<
        Array<{ rowIndex: number; errors: string[]; processedData: ImportRowData }>
      >([]);

      // 加载状态
      const loading = ref<boolean>(false);

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        console.log('模态框打开，强制重置所有状态');

        // 使用 nextTick 确保状态重置在正确的时机
        await nextTick();

        // 强制重置所有状态
        fileList.value = [];
        uploadInfo.value = '';
        showResult.value = false;
        resultMessage.value = '';
        resultType.value = 'info';
        resultDescription.value = '';
        showDetailedErrors.value = false;
        validationResults.value = [];
        loading.value = false;

        // 确保模态框属性也被重置
        setModalProps({
          confirmLoading: false,
          loading: false,
        });

        console.log('模态框状态重置完成');
      });

      // 组件挂载后绑定事件
      onMounted(() => {
        console.log('ImportModal组件已挂载');
        resetFields();
      });

      function resetFields() {
        // 清空文件列表和上传信息
        fileList.value = [];
        uploadInfo.value = '';

        // 重置其他状态
        showResult.value = false;
        resultMessage.value = '';
        resultType.value = 'info';
        resultDescription.value = '';
        showDetailedErrors.value = false;
        validationResults.value = [];
        loading.value = false;
      }

      // 处理取消按钮
      function handleCancel() {
        resetFields();
      }

      // 组件卸载前也清理状态
      onBeforeUnmount(() => {
        resetFields();
      });

      // 处理文件上传前的验证
      function beforeUpload(file: File) {
        // 验证文件类型
        const isExcel =
          file.type === 'application/vnd.ms-excel' ||
          file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
        if (!isExcel) {
          createMessage.error('只能上传Excel文件!');
          return false;
        }

        // 验证文件大小 (小于10MB)
        const isLt10M = file.size / 1024 / 1024 < 10;
        if (!isLt10M) {
          createMessage.error('文件必须小于10MB!');
          return false;
        }

        // 设置上传文件信息
        fileList.value = [file];
        uploadInfo.value = file.name;
        return false; // 阻止自动上传
      }

      // 移除上传文件
      function handleRemove() {
        fileList.value = [];
        uploadInfo.value = '';
        return true;
      }

      // 下载Excel模板
      async function handleDownloadTemplate() {
        try {
          // 显示下载中提示
          const downloadLoading = createMessage.loading('模板下载中，请稍候...', 0);

          // 使用增强版模板下载API
          const res = await downloadEnhancedPersonTemplate();

          // 关闭下载中提示
          downloadLoading();

          const blob = new Blob([res.data], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          });
          const fileName = '人员信息导入模板.xlsx';
          const link = document.createElement('a');
          link.href = URL.createObjectURL(blob);
          link.download = fileName;
          link.click();
          URL.revokeObjectURL(link.href);

          // 下载成功提示
          createMessage.success('模板下载成功，包含组织架构选项数据');
        } catch (error) {
          // 如果增强版失败，尝试使用原版模板
          try {
            const fallbackRes = await downloadPersonTemplate();
            const blob = new Blob([fallbackRes.data], {
              type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
            });
            const fileName = '人员信息导入模板.xlsx';
            const link = document.createElement('a');
            link.href = URL.createObjectURL(blob);
            link.download = fileName;
            link.click();
            URL.revokeObjectURL(link.href);

            createMessage.success('模板下载成功（基础版）');
          } catch (fallbackError) {
            createMessage.error('下载模板失败，请重试');
            console.error('下载模板失败:', fallbackError);
          }
        }
      }

      // 客户端验证导入数据
      async function validateClientData(excelData: ImportRowData[]) {
        try {
          const validationResult = await validateImportData(excelData);
          return validationResult;
        } catch (error) {
          console.error('客户端验证失败:', error);
          return null;
        }
      }

      // 提交导入
      async function handleSubmit() {
        if (fileList.value.length === 0) {
          createMessage.warning('请选择要上传的Excel文件!');
          return;
        }

        try {
          // 设置加载状态
          setModalProps({ confirmLoading: true });
          loading.value = true;

          // 显示导入进行中提示
          const importLoading = createMessage.loading('数据导入中，请稍候...', 0);

          // 准备表单数据
          const formData = new FormData();
          formData.append('file', fileList.value[0]);

          // 发送请求 - 优先使用增强版导入API
          let res;
          try {
            res = await importPersonExcelEnhanced(formData);
          } catch (enhancedError) {
            console.warn('增强版导入失败，使用原版导入API:', enhancedError);
            res = await importPersonExcel(formData);
          }
          console.log('导入响应结果:', res);
          console.log('响应数据类型:', typeof res);
          console.log('响应success:', res?.success);
          console.log('响应errors:', res?.errors);
          console.log('响应errorCount:', res?.errorCount);

          // 关闭导入进行中提示
          importLoading();

          // 处理结果
          if (res && res.success) {
            showResult.value = true;
            resultType.value = 'success';
            resultMessage.value = res.message || '导入成功';
            const total = Number(res.total ?? res.summary?.total ?? 0);
            const successCount = Number(res.successCount ?? res.summary?.success ?? 0);
            const errorCount = Number(res.errorCount ?? res.summary?.failed ?? 0);
            const bindSuccess = Number(res.helmetBindSuccess ?? 0);
            const rebind = Number(res.helmetRebind ?? 0);
            const bindFail = Number(res.helmetBindFail ?? 0);

            const summaryParts: string[] = [];
            summaryParts.push(`共${total}条记录`);
            summaryParts.push(`成功${successCount}条`);
            summaryParts.push(`失败${errorCount}条`);
            if (bindSuccess > 0) {
              summaryParts.push(`绑定安全帽${bindSuccess}条`);
            }
            if (rebind > 0) {
              summaryParts.push(`重新绑定${rebind}条`);
            }
            if (bindFail > 0) {
              summaryParts.push(`安全帽绑定失败${bindFail}条`);
            }

            let detailText = summaryParts.join('，');
            if (Array.isArray(res.helmetBindFailRows) && res.helmetBindFailRows.length > 0) {
              detailText += `；失败行：${res.helmetBindFailRows.join(', ')}`;
            }
            if (Array.isArray(res.helmetRebindRows) && res.helmetRebindRows.length > 0) {
              detailText += `；重新绑定行：${res.helmetRebindRows.join(', ')}`;
            }
            resultDescription.value = detailText || '数据已成功导入系统';

            // 显示成功提示
            createMessage.success('数据导入成功');

            // 导入成功后延迟关闭
            setTimeout(() => {
              // 关闭前重置状态，确保下次打开时干净
              showResult.value = false;
              resultMessage.value = '';
              resultType.value = 'info';
              resultDescription.value = '';
              showDetailedErrors.value = false;
              validationResults.value = [];
              fileList.value = [];
              uploadInfo.value = '';

              closeModal();
              emit('success');
            }, 1500);
          } else {
            // 处理身份证重复的情况
            if (res && res.hasDuplicates) {
              console.log('检测到重复身份证:', res.duplicateIdentityCards);
              console.log('重复人员信息:', res.duplicatePersons);

              // 使用Modal.error显示重复身份证错误
              const errorMessage = res.message || '存在相同身份证的在职人员，导入失败';

              // 将错误消息按行分割
              const lines = errorMessage.split('\n');

              Modal.error({
                title: '导入失败',
                content: h('div', {}, [...lines.map((line) => h('div', {}, line))]),
                width: 500,
                okText: '知道了',
                onOk: () => {
                  // 重置上传状态，准备重新上传
                  fileList.value = [];
                  uploadInfo.value = '';
                },
              });

              // 仍然更新内部状态，以便在模态框中显示提示
              showResult.value = true;
              resultType.value = 'error';
              resultMessage.value = '导入失败，详情请查看提示窗口';
              resultDescription.value = '请修正错误后再次尝试导入';

              // 导入失败时清空文件列表
              fileList.value = [];
              uploadInfo.value = '';
            } else {
              if (res && res.helmetBindFail) {
                const failRowsText =
                  Array.isArray(res.helmetBindFailRows) && res.helmetBindFailRows.length > 0
                    ? `失败行：${res.helmetBindFailRows.join(', ')}`
                    : '请核对安全帽编码与设备绑定关系';
                Modal.error({
                  title: '安全帽绑定失败',
                  content: `共有 ${res.helmetBindFail} 条记录的安全帽无法绑定。${failRowsText}`,
                  okText: '知道了',
                });
              }

              // 处理导入失败的情况
              console.log('处理导入失败，开始分析错误信息...');

              // 无论什么情况，都显示错误状态
              showResult.value = true;
              resultType.value = 'error';

              // 导入失败时清空文件列表
              fileList.value = [];
              uploadInfo.value = '';

              // 处理有详细错误信息的情况
              if (res && res.errors && res.errors.length > 0) {
                console.log('发现详细错误信息，共', res.errors.length, '条');
                resultMessage.value = `导入失败，共发现${res.errors.length}个错误`;

                // 构建详细错误描述，使用文本换行符
                let errorDescription = '详细错误信息：\n';
                res.errors.forEach((error, index) => {
                  errorDescription += `${index + 1}. ${error}\n`;
                });
                resultDescription.value = errorDescription;
              }
              // 处理有errorCount但没有具体errors的情况
              else if (res && res.errorCount && res.errorCount > 0) {
                console.log('发现错误数量:', res.errorCount, '但没有详细错误信息');
                resultMessage.value = `导入失败，共${res.errorCount}个错误`;
                resultDescription.value = res.message || '存在数据错误，请检查数据格式';
              }
              // 处理客户端验证错误
              else if (res && res.validationErrors && res.validationErrors.length > 0) {
                console.log('发现客户端验证错误');
                validationResults.value = res.validationErrors;
                showDetailedErrors.value = true;
                resultMessage.value = `数据验证失败，共${res.validationErrors.length}个错误`;
                resultDescription.value = '请修正错误后再次尝试导入';
              }
              // 兜底处理
              else {
                console.log('使用通用错误处理');
                resultMessage.value = res?.message || '导入失败';
                resultDescription.value = '请检查数据格式或稍后重试';
              }
            }
          }
        } catch (error) {
          console.error('导入请求异常:', error);

          // 只有在没有设置过showResult的情况下才设置错误信息
          if (!showResult.value) {
            showResult.value = true;
            resultType.value = 'error';
            resultMessage.value = '导入请求失败';
            resultDescription.value = '网络请求失败或服务器异常，请检查网络连接后重试';

            console.log('设置网络异常错误信息');
          } else {
            console.log('已有错误信息，不覆盖现有的错误显示');
          }
        } finally {
          setModalProps({ confirmLoading: false });
          loading.value = false;
        }
      }

      async function handleAfterClose() {
        console.log('模态框关闭后，重置所有状态');

        // 使用 nextTick 确保状态重置在正确的时机
        await nextTick();

        // 强制重置所有状态
        fileList.value = [];
        uploadInfo.value = '';
        showResult.value = false;
        resultMessage.value = '';
        resultType.value = 'info';
        resultDescription.value = '';
        showDetailedErrors.value = false;
        validationResults.value = [];
        loading.value = false;

        console.log('模态框关闭后状态重置完成');
      }

      return {
        t,
        registerModal,
        fileList,
        uploadInfo,
        showResult,
        resultMessage,
        resultType,
        resultDescription,
        showDetailedErrors,
        validationResults,
        loading,
        handleRemove,
        beforeUpload,
        handleDownloadTemplate,
        handleSubmit,
        handleCancel,
        handleAfterClose,
      };
    },
  });
</script>
<style lang="less" scoped>
  .upload-container {
    padding: 16px;
  }

  .result-container {
    margin-top: 16px;
  }

  .field-info {
    margin-top: 16px;
    background-color: #f5f5f5;
    padding: 12px;
    border-radius: 4px;
  }

  .field-title {
    font-weight: bold;
    margin-bottom: 8px;
    color: #333;
  }

  .field-section {
    margin-bottom: 12px;
  }

  .field-name {
    font-weight: 500;
    margin-bottom: 4px;
    color: #666;
  }

  .field-value {
    margin-left: 12px;
    color: #555;
    line-height: 1.5;
  }

  .field-highlight {
    color: #1890ff;
    font-weight: 500;
  }

  .field-warning {
    color: #fa8c16;
    font-weight: 500;
  }

  .error-details {
    margin-top: 16px;
  }

  .error-list {
    max-height: 300px;
    overflow-y: auto;
  }

  .error-item {
    margin-bottom: 12px;
  }

  .error-row {
    border-left: 3px solid #ff4d4f;
    padding-left: 12px;
    margin-bottom: 8px;
  }

  .error-row-header {
    font-weight: 500;
    color: #ff4d4f;
    margin-bottom: 4px;
  }

  .error-icon {
    margin-right: 4px;
  }

  .error-messages {
    margin: 0;
    padding-left: 16px;
  }

  .error-message {
    color: #666;
    font-size: 13px;
    line-height: 1.4;
  }

  .template-tips {
    background-color: #f8f9fa;
    border: 1px solid #e9ecef;
    border-radius: 4px;
    padding: 8px 12px;
  }

  .tip-item {
    display: flex;
    align-items: center;
    margin-bottom: 4px;
  }

  .tip-item:last-child {
    margin-bottom: 0;
  }

  .tip-icon {
    margin-right: 8px;
    font-size: 14px;
  }

  .tip-icon.success {
    color: #52c41a;
  }

  .tip-icon.info {
    color: #1890ff;
  }

  .tip-text {
    font-size: 12px;
    color: #666;
  }
</style>
