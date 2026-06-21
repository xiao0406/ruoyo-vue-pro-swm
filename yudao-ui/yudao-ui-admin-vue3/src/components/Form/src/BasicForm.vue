<template>
  <a-form
    :model="model"
    :layout="formProps.layout || 'horizontal'"
    :label-col="{ style: { width: formProps.labelWidth ? formProps.labelWidth + 'px' : '100px' } }"
  >
    <a-row :gutter="24">
      <template v-for="schema in visibleSchemas" :key="schema.field">
        <a-col v-bind="schema.colProps || formProps.baseColProps || { span: 24 }">
          <a-form-item
            :label="schema.label"
            :name="schema.field"
            :rules="schema.required ? [{ required: true, message: `请输入${schema.label}` }] : schema.rules"
          >
            <!-- Input -->
            <a-input
              v-if="!schema.component || schema.component === 'Input'"
              v-model:value="model[schema.field]"
              :placeholder="`请输入${schema.label}`"
              :disabled="isDisabled(schema)"
              v-bind="getComponentProps(schema)"
            />
            <!-- InputTextArea -->
            <a-textarea
              v-else-if="schema.component === 'InputTextArea'"
              v-model:value="model[schema.field]"
              :placeholder="`请输入${schema.label}`"
              :disabled="isDisabled(schema)"
              v-bind="getComponentProps(schema)"
            />
            <!-- InputNumber -->
            <a-input-number
              v-else-if="schema.component === 'InputNumber'"
              v-model:value="model[schema.field]"
              :placeholder="`请输入${schema.label}`"
              :disabled="isDisabled(schema)"
              v-bind="getComponentProps(schema)"
              style="width: 100%"
            />
            <!-- Select -->
            <a-select
              v-else-if="schema.component === 'Select'"
              v-model:value="model[schema.field]"
              :placeholder="`请选择${schema.label}`"
              :disabled="isDisabled(schema)"
              :allow-clear="true"
              v-bind="getComponentProps(schema)"
            />
            <!-- TreeSelect -->
            <a-tree-select
              v-else-if="schema.component === 'TreeSelect'"
              v-model:value="model[schema.field]"
              :placeholder="`请选择${schema.label}`"
              :disabled="isDisabled(schema)"
              v-bind="getComponentProps(schema)"
            />
            <!-- RadioButtonGroup -->
            <a-radio-group
              v-else-if="schema.component === 'RadioButtonGroup'"
              v-model:value="model[schema.field]"
              :disabled="isDisabled(schema)"
              v-bind="getComponentProps(schema)"
            >
              <a-radio-button
                v-for="opt in getComponentProps(schema).options || []"
                :key="opt.value"
                :value="opt.value"
              >
                {{ opt.label }}
              </a-radio-button>
            </a-radio-group>
            <!-- RadioGroup -->
            <a-radio-group
              v-else-if="schema.component === 'RadioGroup'"
              v-model:value="model[schema.field]"
              :disabled="isDisabled(schema)"
              v-bind="getComponentProps(schema)"
            >
              <a-radio
                v-for="opt in getComponentProps(schema).options || []"
                :key="opt.value"
                :value="opt.value"
              >
                {{ opt.label }}
              </a-radio>
            </a-radio-group>
            <!-- CheckboxGroup -->
            <a-checkbox-group
              v-else-if="schema.component === 'CheckboxGroup'"
              v-model:value="model[schema.field]"
              :disabled="isDisabled(schema)"
              v-bind="getComponentProps(schema)"
            />
            <!-- Switch -->
            <a-switch
              v-else-if="schema.component === 'Switch'"
              v-model:checked="model[schema.field]"
              :disabled="isDisabled(schema)"
              v-bind="getComponentProps(schema)"
            />
            <!-- DatePicker -->
            <a-date-picker
              v-else-if="schema.component === 'DatePicker'"
              v-model:value="model[schema.field]"
              :placeholder="`请选择${schema.label}`"
              :disabled="isDisabled(schema)"
              style="width: 100%"
              v-bind="getComponentProps(schema)"
            />
            <!-- RangePicker -->
            <a-range-picker
              v-else-if="schema.component === 'RangePicker'"
              v-model:value="model[schema.field]"
              :disabled="isDisabled(schema)"
              style="width: 100%"
              v-bind="getComponentProps(schema)"
            />
            <!-- Slot -->
            <slot v-else-if="schema.slot" :name="schema.slot" v-bind="{ schema, model }" />
            <!-- Render function -->
            <template v-else-if="schema.render">
              <template v-if="false" />
            </template>
            <!-- Default: Input -->
            <a-input
              v-else
              v-model:value="model[schema.field]"
              :placeholder="`请输入${schema.label}`"
              :disabled="isDisabled(schema)"
            />
          </a-form-item>
        </a-col>
      </template>
    </a-row>
  </a-form>
</template>

<script lang="ts" setup>
  import { ref, computed, watch, reactive, provide, toRaw } from 'vue';
  import {
    Form as AForm,
    FormItem as AFormItem,
    Row as ARow,
    Col as ACol,
    Input as AInput,
    InputNumber as AInputNumber,
    Textarea as ATextarea,
    Select as ASelect,
    TreeSelect as ATreeSelect,
    Radio as ARadio,
    RadioButton as ARadioButton,
    RadioGroup as ARadioGroup,
    CheckboxGroup as ACheckboxGroup,
    Switch as ASwitch,
    DatePicker as ADatePicker,
    RangePicker as ARangePicker,
  } from 'ant-design-vue';
  import type { FormSchema } from './hooks/useForm';

  const props = withDefaults(
    defineProps<{
      schemas?: FormSchema[];
      labelWidth?: number | string;
      layout?: 'vertical' | 'inline' | 'horizontal';
      showActionButtonGroup?: boolean;
      disabled?: boolean;
      compact?: boolean;
      baseColProps?: any;
      size?: string;
      model?: Record<string, any>;
    }>(),
    {
      showActionButtonGroup: false,
      disabled: false,
      compact: false,
      layout: 'horizontal',
    },
  );

  const emit = defineEmits(['register', 'submit', 'reset']);

  const formProps = computed(() => props);

  const model = reactive<Record<string, any>>(props.model || {});

  const visibleSchemas = computed(() => {
    return (props.schemas || []).filter((schema) => {
      if (schema.ifShow === false) return false;
      if (typeof schema.ifShow === 'function') {
        return schema.ifShow({ schema, values: model, model, field: schema.field });
      }
      if (schema.show === false) return false;
      return true;
    });
  });

  function isDisabled(schema: FormSchema) {
    if (props.disabled) return true;
    if (schema.dynamicDisabled === true) return true;
    if (typeof schema.dynamicDisabled === 'function') {
      return schema.dynamicDisabled({ schema, values: model, model, field: schema.field });
    }
    return false;
  }

  function getComponentProps(schema: FormSchema) {
    if (!schema.componentProps) return {};
    if (typeof schema.componentProps === 'function') {
      return schema.componentProps({ schema, values: model, model, field: schema.field });
    }
    return schema.componentProps;
  }

  // Expose model for parent to set values
  defineExpose({
    model,
    getFieldsValue: () => ({ ...model }),
    setFieldsValue: (values: Record<string, any>) => {
      Object.assign(model, values);
    },
    resetFields: () => {
      (props.schemas || []).forEach((schema) => {
        model[schema.field] = schema.defaultValue ?? undefined;
      });
    },
    validate: async () => ({ ...model }),
  });

  emit('register', {
    model,
    setFieldsValue: async (values: any) => {
      Object.assign(model, values);
    },
    resetFields: async () => {
      (props.schemas || []).forEach((schema) => {
        model[schema.field] = schema.defaultValue ?? undefined;
      });
    },
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
