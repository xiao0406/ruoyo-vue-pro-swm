import { BasicColumn } from '@/components/swm/Table';
import { useI18n } from '@/hooks/swm/useI18n';

const { t } = useI18n();

// 根据图片所示的表格列创建配置
export const siteMapColumns: BasicColumn[] = [
  {
    title: t('swm.siteMap.name'),
    dataIndex: 'name',
    key: 'name',
    width: 180,
    align: 'left',
  },
  {
    title: t('swm.siteMap.project'),
    dataIndex: 'project',
    key: 'project',
    width: 160,
    align: 'left',
  },
  {
    title: t('swm.siteMap.size'),
    dataIndex: 'size',
    key: 'size',
    width: 160,
    align: 'left',
  },
  {
    title: t('swm.siteMap.scale'),
    dataIndex: 'scale',
    key: 'scale',
    width: 160,
    align: 'left',
  },
  {
    title: t('swm.siteMap.url'),
    dataIndex: 'url',
    key: 'url',
    width: 160,
    align: 'left',
  },
  {
    title: t('swm.siteMap.status'),
    dataIndex: 'status',
    key: 'status',
    width: 120,
    align: 'center',
  },
];
