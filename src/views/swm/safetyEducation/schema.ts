/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author Shawn
 * @date 2025-06-21
 */
import type { Ref, ComputedRef } from 'vue';
import { computed } from 'vue';
import type { FormSchema } from '@/components/swm/Form';
import { DICT } from '@/enums/swm/safetyEducationEnum';

/**
 * 获取表单配置
 * @param t 国际化函数
 * @param isView 是否为查看模式
 * @param onParticipationTypeChange 参与类型变化回调
 * @param treeSelectTransferRef 树选择组件引用，用于获取已选人员
 */
export function getFormSchemas(
  t: Function,
  isView: Ref<boolean>,
  onParticipationTypeChange?: Function,
  treeSelectTransferRef?: Ref<any>,
): ComputedRef<FormSchema[]> {
  return computed(() => {
    // 根据查看模式返回不同的表单配置
    if (isView.value) {
      // 直接渲染纯文本字段
      return [
        {
          label: t('主题'),
          field: 'theme',
          component: 'Input',
          componentProps: {
            disabled: true,
            readonly: true,
          },
          colProps: { span: 12 },
        },
        {
          label: t('安全教育类型'),
          field: 'safetyEducationTypeText',
          component: 'Input',
          componentProps: {
            disabled: true,
            readonly: true,
          },
          colProps: { span: 12 },
        },
        {
          label: t('开始时间'),
          field: 'startTime',
          component: 'Input',
          componentProps: {
            disabled: true,
            readonly: true,
          },
          colProps: { span: 12 },
        },
        {
          label: t('参与类型'),
          field: 'participationTypeText',
          component: 'Input',
          componentProps: {
            disabled: true,
            readonly: true,
          },
          colProps: { span: 12 },
        },
        {
          label: t('状态'),
          field: 'safetyStatusText',
          component: 'Input',
          componentProps: {
            disabled: true,
            readonly: true,
          },
          colProps: { span: 12 },
        },
        {
          label: t('参与对象'),
          field: 'participantsName',
          component: 'Input',
          slot: 'participantsView',
          colProps: { span: 24 },
        },
        {
          label: t('内容描述'),
          field: 'contentDescription',
          component: 'InputTextArea',
          componentProps: {
            rows: 4,
            disabled: true,
            readonly: true,
          },
          colProps: { span: 24 },
        },
        {
          label: t('附件'),
          field: 'files',
          component: 'Input',
          slot: 'fileUpload',
          colProps: { span: 24 },
        },
      ] as FormSchema[];
    } else {
      // 编辑模式配置
      return [
        {
          label: t('主题'),
          field: 'theme',
          component: 'Input',
          required: true,
          colProps: { span: 12 },
        },
        {
          label: t('安全教育类型'),
          field: 'safetyEducationType',
          component: 'Select',
          componentProps: {
            dictType: DICT.EDUCATION_TYPE,
          },
          required: true,
          colProps: { span: 12 },
        },
        {
          label: t('开始时间'),
          field: 'startTime',
          component: 'DatePicker',
          componentProps: {
            showTime: true,
            format: 'YYYY-MM-DD HH:mm',
            valueFormat: 'YYYY-MM-DD HH:mm:ss',
          },
          required: true,
          colProps: { span: 12 },
        },
        {
          label: t('参与类型'),
          field: 'participationType',
          component: 'Select',
          componentProps: {
            dictType: DICT.PARTICIPATION_TYPE,
            onChange: (e: any) => {
              console.log('参与类型选择变更:', e);
              if (onParticipationTypeChange) {
                onParticipationTypeChange(e);
              }
            },
          },
          required: true,
          colProps: { span: 12 },
        },
        {
          label: t('参与对象'),
          field: 'participants',
          component: 'Input',
          slot: 'participantsTree',
          helpText: (values) => {
            if (values._participationTypeHelpText) {
              return values._participationTypeHelpText;
            }
            return isView.value
              ? ''
              : '请在左侧选择节点，点击箭头查询并添加该节点下的所有人员到右侧';
          },
          // 移除简单的required: true，使用自定义规则校验右侧框内容
          rules: [
            {
              required: true,
              validator: (_rule: any, value: any) => {
                return new Promise((resolve, reject) => {
                  // 通过组件引用获取已选人员数量
                  if (treeSelectTransferRef && treeSelectTransferRef.value) {
                    const selectedNames = treeSelectTransferRef.value.getSelectedNames();
                    if (selectedNames && selectedNames.length > 0) {
                      resolve();
                    } else {
                      reject(new Error('请选择参与对象，确保右侧框中有已选人员'));
                    }
                  } else {
                    // 兜底校验：如果组件引用不可用，检查表单值是否为有效数组
                    if (Array.isArray(value) && value.length > 0) {
                      resolve();
                    } else {
                      reject(new Error('请选择参与对象'));
                    }
                  }
                });
              },
              trigger: 'change', // 在值变化时触发校验
            },
          ],
          defaultValue: [],
          colProps: { span: 24 },
        },
        {
          label: t('内容描述'),
          field: 'contentDescription',
          component: 'InputTextArea',
          componentProps: {
            rows: 4,
          },
          colProps: { span: 24 },
        },
        {
          label: t('附件'),
          field: 'files',
          component: 'Input',
          slot: 'fileUpload',
          helpText: '最多上传5个文件，支持图片、文档和压缩包等格式',
          colProps: { span: 24 },
        },
      ] as FormSchema[];
    }
  });
}

// 空的树形数据，将通过API动态加载
export const treeSelectData = [];
