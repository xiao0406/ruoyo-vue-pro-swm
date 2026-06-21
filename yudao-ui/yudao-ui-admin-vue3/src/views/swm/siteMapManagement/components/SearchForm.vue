<template>
  <div class="search-form">
    <a-input
      v-model:value="searchParams.keyword"
      :placeholder="t('swm.siteMap.search.placeholder')"
      style="width: 200px"
    />
    <a-button type="primary" @click="handleSearch">{{ t('common.searchText') }}</a-button>
    <a-button @click="handleReset">{{ t('common.resetText') }}</a-button>
  </div>
</template>

<script lang="ts">
  import { defineComponent, reactive, ref } from 'vue';
  import { useI18n } from '@/hooks/swm/useI18n';

  export default defineComponent({
    name: 'SearchForm',
    emits: ['search', 'reset'],
    setup(_, { emit }) {
      const { t } = useI18n();

      const searchParams = reactive({
        keyword: '',
      });

      function handleSearch() {
        emit('search', searchParams);
      }

      function handleReset() {
        searchParams.keyword = '';
        emit('reset');
      }

      return {
        t,
        searchParams,
        handleSearch,
        handleReset,
      };
    },
  });
</script>

<style lang="less" scoped>
  .search-form {
    display: flex;
    align-items: center;
    margin-bottom: 16px;

    .ant-btn {
      margin-left: 8px;
    }
  }
</style>
