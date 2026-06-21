<template>
  <div>
    <BasicTable @register="registerTable">
      <template #tableTitle>
        <Icon :icon="getTitle.icon" class="m-1 pr-1" />
        <span> {{ getTitle.value }} </span>
      </template>
      <template #toolbar>
        <a-button type="primary" @click="handleForm({})">
          <Icon icon="fluent:add-12-filled" /> {{ t('新增') }}
        </a-button>
      </template>
      <!-- 视频封面自定义渲染插槽 -->
      <template #coverUrl="{ record }">
        <template v-if="record.coverUrl">
          <Image
            v-if="parseCoverUrl(record.coverUrl)"
            :src="parseCoverUrl(record.coverUrl)"
            :preview="true"
            style="width: 80px; height: 80px; object-fit: cover; border-radius: 4px; cursor: pointer;"
          />
          <span v-else>{{ record.coverUrl }}</span>
        </template>
      </template>
    </BasicTable>

    <InputForm @register="registerModal" @success="handleSuccess" />

    <!-- 视频播放组件 -->
    <VideoPlayer ref="videoPlayerRef" />
  </div>
</template>
<script lang="ts">
  export default defineComponent({
    name: 'ViewsSwmSafetyVideosList',
  });
</script>
<script lang="ts" setup>
  import { defineComponent, ref } from 'vue';
  import { Image } from 'ant-design-vue';
  import { useI18n } from '@/hooks/swm/useI18n';
  import { router } from '@/router';
  import { Icon } from '@/components/swm/Icon';
  import { BasicTable, BasicColumn, useTable } from '@/components/swm/Table';
  import { useModal } from '@/components/swm/Modal';
  import { FormProps } from '@/components/swm/Form';
  import InputForm from './form.vue';
  import VideoPlayer from './components/videoPlayer.vue';
  import { swmSafetyFileManageListData, swmSafetyFileManageDelete } from '@/api/swm/safetyVideos';
  import { useMessage } from '@/hooks/swm/useMessage';

  const { showMessage } = useMessage();

  const { t } = useI18n();
  const getTitle = {
    icon: router.currentRoute.value.meta.icon || 'ant-design:book-outlined',
    value: router.currentRoute.value.meta.title || t('安全教育视频管理'),
  };

  // 视频播放器组件引用
  const videoPlayerRef = ref<InstanceType<typeof VideoPlayer>>();

  // 播放视频
  const handlePlayVideo = (record: Recordable) => {
    if (record.fileUrl && videoPlayerRef.value) {
      videoPlayerRef.value.playVideo(record.title, record.fileUrl);
    }
  };

  // 解析封面URL
  const parseCoverUrl = (coverUrl: string): string => {
    if (!coverUrl) return '';
    try {
      const coverList = JSON.parse(coverUrl);
      if (coverList && coverList.length > 0) {
        return coverList[0].url;
      }
    } catch (e) {
      console.warn('Failed to parse coverUrl:', e);
    }
    return '';
  };

  const searchForm: FormProps = {
    baseColProps: { lg: 6, md: 8 },
    labelWidth: 100,
    schemas: [
      {
        label: t('视频标题'),
        field: 'title',
        component: 'Input',
      },
      {
        label: t('推送日期'),
        field: 'pushDate',
        component: 'DatePicker',
      },
    ],
  };

  const tableColumns: BasicColumn[] = [
    {
      title: t('视频封面'),
      dataIndex: 'coverUrl',
      key: 'a.cover_url',
      width: 130,
      align: 'center',
      slots: { customRender: 'coverUrl' },
    },
    {
      title: t('视频标题'),
      dataIndex: 'title',
      key: 'a.title',
      sorter: true,
      width: 230,
      align: 'center',
    },
    {
      title: t('视频分类'),
      dataIndex: 'type',
      key: 'a.type',
      sorter: true,
      width: 130,
      align: 'center',
      dictType: 'swm_safety_type',
    },
    {
      title: t('目标工种'),
      dataIndex: 'jobType',
      key: 'a.job_type',
      sorter: true,
      width: 230,
      align: 'center',
    },
    {
      title: t('视频时长（秒）'),
      dataIndex: 'duration',
      key: 'a.duration',
      sorter: true,
      width: 130,
      align: 'center',
    },
    {
      title: t('推送日期'),
      dataIndex: 'pushDate',
      key: 'a.push_date',
      sorter: true,
      width: 130,
      align: 'center',
    },
    {
      title: t('推送状态'),
      dataIndex: 'pushStatus',
      key: 'a.push_status',
      sorter: true,
      width: 130,
      align: 'center',
      dictType: 'swm_push_status',
    },
    {
      title: t('更新时间'),
      dataIndex: 'updateDate',
      key: 'a.update_date',
      sorter: true,
      width: 180,
      align: 'center',
    },
  ];

  const actionColumn: BasicColumn = {
    width: 130,
    actions: (record: Recordable) => [
      {
        icon: 'solar:video-frame-play-horizontal-linear',
        title: t('播放'),
        onClick: handlePlayVideo.bind(this, record),
      },
      {
        icon: 'clarity:note-edit-line',
        title: t('编辑'),
        onClick: handleForm.bind(this, record),
      },
      {
        icon: 'ant-design:delete-outlined',
        color: 'error',
        title: t('删除'),
        popConfirm: {
          title: t('是否确认删除？'),
          confirm: handleDelete.bind(this, { id: record.id }),
        },
      },
    ],
  };

  const [registerModal, { openModal }] = useModal();

  const [registerTable, { reload }] = useTable({
    api: swmSafetyFileManageListData,
    columns: tableColumns,
    actionColumn: actionColumn,
    formConfig: searchForm,
    showTableSetting: true,
    useSearchForm: true,
    canResize: true,
  });

  const handleForm = (record: Recordable) => {
    openModal(true, record);
  };

  async function handleDelete(record: Recordable) {
    const res = await swmSafetyFileManageDelete(record);
    showMessage(res.message);
    handleSuccess();
  }

  const handleSuccess = () => {
    reload();
  };
</script>
