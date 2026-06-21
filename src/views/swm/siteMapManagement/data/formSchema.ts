import { FormSchema } from '@/components/swm/Form';
import { useI18n } from '@/hooks/swm/useI18n';

const { t } = useI18n();

export const formSchema: FormSchema[] = [
  {
    field: 'name',
    label: t('swm.siteMap.name'),
    component: 'Input',
    required: true,
    componentProps: {
      placeholder: t('swm.siteMap.name.placeholder'),
    },
    rules: [
      {
        required: true,
        message: t('swm.siteMap.name.required'),
      },
    ],
  },
  {
    field: 'project',
    label: t('swm.siteMap.project'),
    component: 'Input',
    required: true,
    componentProps: {
      placeholder: t('swm.siteMap.project.placeholder'),
    },
    rules: [
      {
        required: true,
        message: t('swm.siteMap.project.required'),
      },
    ],
  },
  {
    field: 'size',
    label: t('swm.siteMap.size'),
    component: 'Input',
    componentProps: {
      placeholder: t('swm.siteMap.size.placeholder'),
    },
  },
  {
    field: 'scale',
    label: t('swm.siteMap.scale'),
    component: 'Input',
    componentProps: {
      placeholder: t('swm.siteMap.scale.placeholder'),
    },
  },
  {
    field: 'url',
    label: t('swm.siteMap.url'),
    component: 'Input',
    required: true,
    componentProps: {
      placeholder: t('swm.siteMap.url.placeholder'),
    },
    rules: [
      {
        required: true,
        message: t('swm.siteMap.url.required'),
      },
    ],
  },
  {
    field: 'status',
    label: t('swm.siteMap.status'),
    component: 'RadioButtonGroup',
    defaultValue: '启用',
    componentProps: {
      options: [
        { label: '启用', value: '启用' },
        { label: '禁用', value: '禁用' },
      ],
    },
  },
];
