import { ref, reactive, unref, nextTick } from 'vue';

export type ComponentType =
  | 'Input' | 'InputGroup' | 'InputPassword' | 'InputSearch' | 'InputTextArea'
  | 'InputNumber' | 'Select' | 'TreeSelect' | 'RadioButtonGroup' | 'RadioGroup'
  | 'Checkbox' | 'CheckboxGroup' | 'DatePicker' | 'MonthPicker' | 'RangePicker'
  | 'WeekPicker' | 'TimePicker' | 'Switch' | 'Upload' | 'FileUpload'
  | 'ImageUpload' | 'Cascader' | 'Render' | 'Slider' | 'Rate'
  | 'None' | 'Divider' | 'ApiTreeSelect' | string;

export interface FormSchema {
  field: string;
  label: string;
  component: ComponentType;
  required?: boolean;
  rules?: any[];
  defaultValue?: any;
  componentProps?: any | ((params: any) => any);
  colProps?: any;
  labelWidth?: number | string;
  itemProps?: any;
  ifShow?: boolean | ((params: any) => boolean);
  show?: boolean | ((params: any) => boolean);
  dynamicDisabled?: boolean | ((params: any) => boolean);
  dynamicRules?: (params: any) => any[];
  render?: (params: any) => any;
  slot?: string;
  colSlot?: string;
  subLabel?: string;
  helpMessage?: string | string[];
  isAdvanced?: boolean;
  changeEvent?: string;
  valueField?: string;
  labelField?: string;
  fieldLabel?: string;
  [key: string]: any;
}

export interface FormProps {
  schemas?: FormSchema[];
  layout?: 'vertical' | 'inline' | 'horizontal';
  labelWidth?: number | string;
  labelAlign?: 'left' | 'right';
  baseColProps?: any;
  baseRowStyle?: any;
  showActionButtonGroup?: boolean;
  showResetButton?: boolean;
  showSubmitButton?: boolean;
  showAdvancedButton?: boolean;
  autoAdvancedLine?: number;
  compact?: boolean;
  submitOnReset?: boolean;
  disabled?: boolean;
  size?: 'default' | 'small' | 'large';
  autoSetPlaceHolder?: boolean;
  autoSubmitOnEnter?: boolean;
  resetButtonOptions?: any;
  submitButtonOptions?: any;
  actionColOptions?: any;
  model?: Recordable;
  [key: string]: any;
}

type Recordable = Record<string, any>;

export interface FormActionType {
  submit: () => Promise<void>;
  resetFields: () => Promise<void>;
  getFieldsValue: () => Recordable;
  setFieldsValue: <T>(values: T) => Promise<void>;
  validate: (nameList?: string[]) => Promise<any>;
  validateFields: (nameList?: string[]) => Promise<any>;
  clearValidate: (name?: string | string[]) => Promise<void>;
  setProps: (formProps: Partial<FormProps>) => Promise<void>;
  updateSchema: (data: Partial<FormSchema> | Partial<FormSchema>[]) => Promise<void>;
  resetSchema: (data: Partial<FormSchema> | Partial<FormSchema>[]) => Promise<void>;
  removeSchemaByFiled: (field: string | string[]) => Promise<void>;
  appendSchemaByField: (schema: FormSchema, prefixField: string, first?: boolean) => Promise<void>;
  scrollToField: (name: string, options?: any) => Promise<void>;
  isLoaded: () => boolean;
}

export function useForm(props?: Partial<FormProps>): [(instance: any) => void, FormActionType] {
  const formRef = ref<any>(null);
  const modelRef = ref<Recordable>(props?.model || {});
  const schemasRef = ref<FormSchema[]>(props?.schemas || []);
  const propsRef = ref<Partial<FormProps>>(props || {});

  // Initialize default values from schemas
  function initDefault() {
    const schemas = unref(schemasRef);
    const model = { ...unref(modelRef) };
    schemas.forEach((schema) => {
      if (schema.defaultValue !== undefined && model[schema.field] === undefined) {
        model[schema.field] = schema.defaultValue;
      }
    });
    modelRef.value = model;
  }

  function register(instance: any) {
    formRef.value = instance;
    initDefault();
  }

  const actions: FormActionType = {
    async submit() {
      // no-op, handled by parent
    },
    async resetFields() {
      const schemas = unref(schemasRef);
      const model: Recordable = {};
      schemas.forEach((schema) => {
        model[schema.field] = schema.defaultValue ?? undefined;
      });
      modelRef.value = model;
    },
    getFieldsValue() {
      return { ...unref(modelRef) };
    },
    async setFieldsValue<T>(values: T) {
      modelRef.value = { ...unref(modelRef), ...values };
    },
    async validate(_nameList?: string[]) {
      // Simplified validation
      return unref(modelRef);
    },
    async validateFields(_nameList?: string[]) {
      return unref(modelRef);
    },
    async clearValidate(_name?: string | string[]) {
      // no-op
    },
    async setProps(formProps: Partial<FormProps>) {
      propsRef.value = { ...unref(propsRef), ...formProps };
    },
    async updateSchema(data: Partial<FormSchema> | Partial<FormSchema>[]) {
      const list = Array.isArray(data) ? data : [data];
      const schemas = [...unref(schemasRef)];
      list.forEach((item) => {
        const idx = schemas.findIndex((s) => s.field === item.field);
        if (idx !== -1) {
          schemas[idx] = { ...schemas[idx], ...item };
        }
      });
      schemasRef.value = schemas;
    },
    async resetSchema(data: Partial<FormSchema> | Partial<FormSchema>[]) {
      const list = Array.isArray(data) ? data : [data];
      schemasRef.value = list as FormSchema[];
    },
    async removeSchemaByFiled(field: string | string[]) {
      const fields = Array.isArray(field) ? field : [field];
      schemasRef.value = unref(schemasRef).filter((s) => !fields.includes(s.field));
    },
    async appendSchemaByField(schema: FormSchema, prefixField: string, first?: boolean) {
      const schemas = [...unref(schemasRef)];
      const idx = schemas.findIndex((s) => s.field === prefixField);
      if (idx !== -1) {
        schemas.splice(first ? idx : idx + 1, 0, schema);
      } else {
        schemas.push(schema);
      }
      schemasRef.value = schemas;
    },
    async scrollToField(_name: string, _options?: any) {
      // no-op
    },
    isLoaded() {
      return true;
    },
  };

  // Expose model and schemas for BasicForm to use
  return [
    register,
    {
      ...actions,
      // Internal properties for BasicForm to access
      get _model() {
        return modelRef;
      },
      get _schemas() {
        return schemasRef;
      },
      get _props() {
        return propsRef;
      },
    } as any,
  ];
}
