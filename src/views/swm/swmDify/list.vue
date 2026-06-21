<template>
  <div>
    <BasicTable @register="registerTable">
      <template #tableTitle>
        <Icon :icon="getTitle.icon" class="m-1 pr-1" />
        <span> {{ getTitle.value }} </span>
      </template>
    </BasicTable>
    <InputForm @register="registerModal" @success="handleSuccess" />
  </div>
</template>
<script lang="ts">
  export default defineComponent({
    name: 'ViewsSwmSwmDifyList',
  });
</script>
<script lang="ts" setup>
  import { defineComponent } from 'vue';
  import { useI18n } from '@/hooks/swm/useI18n';
  import { router } from '@/router';
  import { Icon } from '@/components/swm/Icon';
  import { BasicTable, BasicColumn, useTable } from '@/components/swm/Table';
  import { swmDifyDownloadReport, swmDifyListData } from '@/api/swm/swmDify';
  import { useModal } from '@/components/swm/Modal';
  import { FormProps } from '@/components/swm/Form';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { downloadByData } from '@/utils/file/download';
  import InputForm from './form.vue';

  const { t } = useI18n('swm.swmDify');
  const { showMessage } = useMessage();
  const reportDir = 'ai-report/ZJGGJS/';

  const getTitle = {
    icon: router.currentRoute.value.meta.icon || 'ant-design:book-outlined',
    value: router.currentRoute.value.meta.title || t('安全帽：ai日报表管理'),
  };

  const searchForm: FormProps = {
    baseColProps: { lg: 8, md: 8 },
    labelWidth: 90,
    schemas: [
      {
        label: t('日期'),
        field: 'date',
        component: 'RangePicker',
        componentProps: {
          format: ['YYYY-MM-DD', 'YYYY-MM-DD'],
        },
      },
    ],
    fieldMapToTime: [['date', ['startDate', 'endDate']]],
  };

  const tableColumns: BasicColumn[] = [
    {
      title: t('请求时间'),
      dataIndex: 'date',
      key: 'a.date',
      sorter: true,
      width: 180,
      align: 'left',
    },
    {
      title: t('项目名称'),
      dataIndex: 'projectName',
      key: 'a.project_name',
      sorter: true,
      width: 180,
      align: 'left',
    },
    {
      title: t('制造单位'),
      dataIndex: 'manufacture',
      key: 'a.manufacture',
      sorter: true,
      width: 180,
      align: 'left',
    },
    {
      title: t('安全合规指数'),
      dataIndex: 'safetyIndex',
      key: 'a.safety_index',
      sorter: true,
      width: 130,
      align: 'center',
    },
    {
      title: t('人员活动指数'),
      dataIndex: 'workerIndex',
      key: 'a.worker_index',
      sorter: true,
      width: 130,
      align: 'center',
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
        icon: 'ri:file-ai-line',
        title: t('ai日报'),
        onClick: handleForm.bind(this, { id: record.id }),
      },
      {
        icon: 'ant-design:download-outlined',
        title: t('下载'),
        onClick: handleDownload.bind(this, record),
      },
    ],
  };

  const [registerModal, { openModal }] = useModal();
  const [registerTable, { reload }] = useTable({
    api: swmDifyListData,
    beforeFetch: (params) => {
      return params;
    },
    columns: tableColumns,
    actionColumn: actionColumn,
    formConfig: searchForm,
    showTableSetting: true,
    useSearchForm: true,
    canResize: true,
  });

  function handleForm(record: Recordable) {
    openModal(true, record);
  }

  function getReportObjectName(fileName?: string) {
    let objectName = (fileName || '').trim();
    if (!objectName) {
      return '';
    }

    if (objectName.includes('objectName=')) {
      try {
        const url = new URL(objectName, window.location.origin);
        objectName = url.searchParams.get('objectName') || objectName;
      } catch (e) {
        objectName = objectName.replace(/^.*objectName=/, '');
      }
    }

    objectName = decodeURIComponent(objectName).replace(/^\/+/, '');
    return objectName.includes('/') ? objectName : `${reportDir}${objectName}`;
  }

  async function handleDownload(record: Recordable) {
    const objectName = getReportObjectName(record.remarks);
    if (!objectName) {
      showMessage(t('未找到可下载的报告文件'));
      return;
    }

    try {
      const data = await swmDifyDownloadReport({ objectName });
      const fileName = objectName.split('/').pop() || 'ai-report.pdf';
      downloadByData(data as BlobPart, fileName, 'application/pdf');
    } catch (e) {
      showMessage(t('下载失败，请稍后重试'));
    }
  }

  function handleSuccess() {
    reload();
  }
</script>
