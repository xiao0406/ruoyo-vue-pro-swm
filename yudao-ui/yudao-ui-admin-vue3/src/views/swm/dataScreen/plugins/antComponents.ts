import { App } from 'vue';
import { Table, Select } from 'ant-design-vue';

export function setupAntComponents(app: App<Element>) {
  app.component('ATable', Table);
  app.component('ASelect', Select);
  app.component('ASelectOption', Select.Option);
}
