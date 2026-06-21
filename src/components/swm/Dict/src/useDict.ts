import { ref } from 'vue';

export function useDict() {
  async function initDict(_dictTypes: string[]) {
    // no-op, will use ruoyi's dict system later
  }
  function getDictLabel(_dictType: string, value: string, defaultValue?: string) {
    return value || defaultValue || '';
  }
  function getDictList(_dictType: string) {
    return [];
  }
  async function initGetDictList(dictType: string) {
    return getDictList(dictType);
  }
  function initSelectOptions(optionsRef: any, _dictType?: string) {
    if (optionsRef) optionsRef.value = [];
  }
  function initSelectTreeData(treeDataRef: any, _dictType: string, _isListToTree?: boolean) {
    if (treeDataRef) treeDataRef.value = [];
  }
  return { initDict, getDictList, getDictLabel, initGetDictList, initSelectOptions, initSelectTreeData };
}
