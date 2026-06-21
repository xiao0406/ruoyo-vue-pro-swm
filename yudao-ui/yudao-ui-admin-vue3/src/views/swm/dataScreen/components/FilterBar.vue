<template>
  <div class="filter-bar">
    <div class="filter-item">
      <label>机构</label>
      <a-select v-model:value="filters.organization" style="width: 150px">
        <a-select-option value="大湾厂">大湾厂</a-select-option>
        <a-select-option value="其他厂">其他厂</a-select-option>
      </a-select>
    </div>
    <div class="filter-item">
      <label>车间</label>
      <a-select v-model:value="filters.workshop" style="width: 150px">
        <a-select-option value="一车间">一车间</a-select-option>
        <a-select-option value="二车间">二车间</a-select-option>
      </a-select>
    </div>
    <div class="filter-item">
      <label>工序</label>
      <a-select v-model:value="filters.process" style="width: 150px">
        <a-select-option value="涂漆工序">涂漆工序</a-select-option>
        <a-select-option value="组装工序">组装工序</a-select-option>
      </a-select>
    </div>
    <div class="filter-item">
      <label>班组</label>
      <a-select v-model:value="filters.team" style="width: 150px">
        <a-select-option value="一班">一班</a-select-option>
        <a-select-option value="二班">二班</a-select-option>
      </a-select>
    </div>
    <div class="filter-item">
      <label>工种</label>
      <a-select v-model:value="filters.workType" style="width: 150px">
        <a-select-option value="一组">一组</a-select-option>
        <a-select-option value="二组">二组</a-select-option>
      </a-select>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { reactive, watch } from 'vue';
  import { Select } from 'ant-design-vue';

  // 注册Select组件
  const ASelect = Select;
  const ASelectOption = Select.Option;

  // 筛选条件
  const filters = reactive({
    organization: '大湾厂',
    workshop: '一车间',
    process: '涂漆工序',
    team: '一班',
    workType: '一组',
  });

  const emit = defineEmits(['filter-change']);

  // 监听筛选条件变化
  watch(
    filters,
    (newVal) => {
      emit('filter-change', { ...newVal });
    },
    { deep: true },
  );
</script>

<style lang="less" scoped>
  .filter-bar {
    margin-top: 16px;
    padding: 16px;
    background: #fff;
    border-radius: 4px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
    display: flex;
    flex-wrap: wrap;
    gap: 20px;

    .filter-item {
      display: flex;
      align-items: center;

      label {
        margin-right: 8px;
        font-size: 14px;
        color: #515a6e;
        min-width: 40px;
      }
    }
  }
</style>
