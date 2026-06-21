<template>
  <a-form :model="model" :layout="props.layout || 'horizontal'" :label-col="{ style: { width: (props.labelWidth || 100) + 'px' } }">
    <a-row :gutter="24">
      <template v-for="schema in visibleSchemas" :key="schema.field">
        <a-col v-bind="schema.colProps || props.baseColProps || { span: 24 }">
          <a-form-item :label="schema.label" :name="schema.field" :rules="schema.required ? [{ required: true, message: '请输入' + schema.label }] : schema.rules">
            <a-input v-if="!schema.component || schema.component === 'Input'" v-model:value="model[schema.field]" :placeholder="'请输入' + schema.label" :disabled="isDisabled(schema)" />
            <a-textarea v-else-if="schema.component === 'InputTextArea'" v-model:value="model[schema.field]" :placeholder="'请输入' + schema.label" :disabled="isDisabled(schema)" />
            <a-input-number v-else-if="schema.component === 'InputNumber'" v-model:value="model[schema.field]" :placeholder="'请输入' + schema.label" :disabled="isDisabled(schema)" style="width:100%" />
            <a-select v-else-if="schema.component === 'Select'" v-model:value="model[schema.field]" :placeholder="'请选择' + schema.label" :disabled="isDisabled(schema)" allow-clear />
            <a-tree-select v-else-if="schema.component === 'TreeSelect'" v-model:value="model[schema.field]" :placeholder="'请选择' + schema.label" :disabled="isDisabled(schema)" />
            <a-radio-group v-else-if="schema.component === 'RadioButtonGroup'" v-model:value="model[schema.field]" :disabled="isDisabled(schema)">
              <a-radio-button v-for="opt in (getComponentProps(schema).options || [])" :key="opt.value" :value="opt.value">{{ opt.label }}</a-radio-button>
            </a-radio-group>
            <a-radio-group v-else-if="schema.component === 'RadioGroup'" v-model:value="model[schema.field]" :disabled="isDisabled(schema)">
              <a-radio v-for="opt in (getComponentProps(schema).options || [])" :key="opt.value" :value="opt.value">{{ opt.label }}</a-radio>
            </a-radio-group>
            <a-checkbox-group v-else-if="schema.component === 'CheckboxGroup'" v-model:value="model[schema.field]" :disabled="isDisabled(schema)" />
            <a-switch v-else-if="schema.component === 'Switch'" v-model:checked="model[schema.field]" :disabled="isDisabled(schema)" />
            <a-date-picker v-else-if="schema.component === 'DatePicker'" v-model:value="model[schema.field]" :placeholder="'请选择' + schema.label" :disabled="isDisabled(schema)" style="width:100%" />
            <a-range-picker v-else-if="schema.component === 'RangePicker'" v-model:value="model[schema.field]" :disabled="isDisabled(schema)" style="width:100%" />
            <slot v-else-if="schema.slot" :name="schema.slot" v-bind="{ schema, model }" />
            <a-input v-else v-model:value="model[schema.field]" :placeholder="'请输入' + schema.label" :disabled="isDisabled(schema)" />
          </a-form-item>
        </a-col>
      </template>
    </a-row>
  </a-form>
</template>

<script lang="ts" setup>
  import { computed, reactive } from 'vue';
  import {
    Form as AForm, FormItem as AFormItem, Row as ARow, Col as ACol,
    Input as AInput, InputNumber as AInputNumber, Textarea as ATextarea,
    Select as ASelect, TreeSelect as ATreeSelect,
    Radio as ARadio, RadioButton as ARadioButton, RadioGroup as ARadioGroup,
    CheckboxGroup as ACheckboxGroup, Switch as ASwitch,
    DatePicker as ADatePicker, RangePicker as ARangePicker,
  } from 'ant-design-vue';
  import type { FormSchema } from './hooks/useForm';

  const props = withDefaults(
    defineProps<{
      schemas?: FormSchema[];
      labelWidth?: number | string;
      layout?: string;
      showActionButtonGroup?: boolean;
      disabled?: boolean;
      baseColProps?: any;
      model?: Record<string, any>;
    }>(),
    { showActionButtonGroup: false, disabled: false, layout: 'horizontal' },
  );

  const emit = defineEmits(['register', 'submit', 'reset']);
  const model = reactive<Record<string, any>>(props.model || {});

  const visibleSchemas = computed(() => {
    return (props.schemas || []).filter((s) => {
      if (s.ifShow === false) return false;
      if (typeof s.ifShow === 'function') return s.ifShow({ schema: s, values: model, model, field: s.field });
      if (s.show === false) return false;
      return true;
    });
  });

  function isDisabled(schema: FormSchema) {
    if (props.disabled) return true;
    if (typeof schema.dynamicDisabled === 'function') return schema.dynamicDisabled({ schema, values: model, model, field: schema.field });
    return schema.dynamicDisabled === true;
  }

  function getComponentProps(schema: FormSchema) {
    if (!schema.componentProps) return {};
    return typeof schema.componentProps === 'function' ? schema.componentProps({ schema, values: model, model, field: schema.field }) : schema.componentProps;
  }

  defineExpose({
    model,
    getFieldsValue: () => ({ ...model }),
    setFieldsValue: (v: any) => Object.assign(model, v),
    resetFields: () => { (props.schemas || []).forEach((s) => { model[s.field] = s.defaultValue ?? undefined; }); },
    validate: async () => ({ ...model }),
  });

  emit('register', {
    model,
    setFieldsValue: async (v: any) => Object.assign(model, v),
    resetFields: async () => { (props.schemas || []).forEach((s) => { model[s.field] = s.defaultValue ?? undefined; }); },
    getFieldsValue: () => ({ ...model }),
    validate: async () => ({ ...model }),
    validateFields: async () => ({ ...model }),
    clearValidate: async () => {},
    setProps: async () => {},
    updateSchema: async () => {},
    resetSchema: async () => {},
    removeSchemaByFiled: async () => {},
    appendSchemaByField: async () => {},
    scrollToField: async () => {},
    isLoaded: () => true,
    submit: async () => {},
  });
</script>
