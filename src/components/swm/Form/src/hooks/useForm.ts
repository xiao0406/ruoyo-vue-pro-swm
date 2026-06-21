import { ref, unref } from 'vue';

export type ComponentType = string;

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
  [key: string]: any;
}

export interface FormProps {
  schemas?: FormSchema[];
  layout?: 'vertical' | 'inline' | 'horizontal';
  labelWidth?: number | string;
  baseColProps?: any;
  showActionButtonGroup?: boolean;
  disabled?: boolean;
  compact?: boolean;
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
  const modelRef = ref<Recordable>(props?.model || {});
  const schemasRef = ref<FormSchema[]>(props?.schemas || []);

  function register(_instance: any) {
    const schemas = unref(schemasRef);
    const model = { ...unref(modelRef) };
    schemas.forEach((s) => {
      if (s.defaultValue !== undefined && model[s.field] === undefined) model[s.field] = s.defaultValue;
    });
    modelRef.value = model;
  }

  const actions: FormActionType = {
    async submit() {},
    async resetFields() {
      const model: Recordable = {};
      unref(schemasRef).forEach((s) => { model[s.field] = s.defaultValue ?? undefined; });
      modelRef.value = model;
    },
    getFieldsValue() { return { ...unref(modelRef) }; },
    async setFieldsValue(values) { modelRef.value = { ...unref(modelRef), ...values }; },
    async validate() { return unref(modelRef); },
    async validateFields() { return unref(modelRef); },
    async clearValidate() {},
    async setProps() {},
    async updateSchema(data) {
      const list = Array.isArray(data) ? data : [data];
      const schemas = [...unref(schemasRef)];
      list.forEach((item) => {
        const idx = schemas.findIndex((s) => s.field === item.field);
        if (idx !== -1) schemas[idx] = { ...schemas[idx], ...item };
      });
      schemasRef.value = schemas;
    },
    async resetSchema(data) { schemasRef.value = (Array.isArray(data) ? data : [data]) as FormSchema[]; },
    async removeSchemaByFiled(field) {
      const fields = Array.isArray(field) ? field : [field];
      schemasRef.value = unref(schemasRef).filter((s) => !fields.includes(s.field));
    },
    async appendSchemaByField(schema, prefixField, first) {
      const schemas = [...unref(schemasRef)];
      const idx = schemas.findIndex((s) => s.field === prefixField);
      if (idx !== -1) schemas.splice(first ? idx : idx + 1, 0, schema);
      else schemas.push(schema);
      schemasRef.value = schemas;
    },
    async scrollToField() {},
    isLoaded() { return true; },
  };

  return [register, actions];
}
