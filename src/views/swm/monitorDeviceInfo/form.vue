<template>
  <BasicModal
    v-bind="$attrs"
    :showFooter="true"
    :okAuth="'app:appUpgrade:edit'"
    @register="registerModal"
    @ok="handleSubmit"
    width="40%"
  >
    <template #title>
      <Icon :icon="getTitle.icon" class="pr-1 m-1" />
      <span> {{ getTitle.value }} </span>
    </template>
    <BasicForm @register="registerForm">
      <template #cameraIndexCodeSlot="{ model, field }">
        <Input placeholder="请选择" v-model:value="model[field]">
          <template #addonAfter>
            <Icon
              icon="ant-design:unordered-list-outlined"
              size="16"
              class="cursor-pointer"
              @click="openModal(true, {})"
            />
          </template>
        </Input>
      </template>
    </BasicForm>

    <HikvisionDev @register="hikvisionModal" @success="selectSuccess" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed } from 'vue';
  import { useI18n } from '@/hooks/swm/useI18n';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { router } from '@/router';
  import { Icon } from '@/components/swm/Icon';
  import { BasicForm, FormSchema, useForm } from '@/components/swm/Form';
  import { BasicModal, useModalInner, useModal } from '@/components/swm/Modal';
  import {
    MonitorDeviceInfo,
    companyTreeData,
    monitorDeviceInfoForm,
    monitorDeviceInfoSave,
  } from '@/api/sys/monitorDeviceInfo';
  import { Input } from 'ant-design-vue';
  import HikvisionDev from './hikvisionDev//index.vue';

  const emit = defineEmits(['success', 'register']);

  const { t } = useI18n();
  const { showMessage } = useMessage();
  const record = ref<MonitorDeviceInfo>({} as MonitorDeviceInfo);
  const getTitle = computed(() => ({
    icon: router.currentRoute.value.meta.icon || 'ant-design:book-outlined',
    value: !record.value.id ? t('新增设备监控信息') : t('编辑设备监控信息'),
  }));

  const inputFormSchemas: FormSchema[] = [
    {
      label: t('机构'),
      field: 'value',
      fieldLabel: 'label',
      component: 'TreeSelect',
      required: true,
    },
    {
      label: t('监控设备类型'),
      field: 'deviceType',
      component: 'Select',
      componentProps: {
        dictType: 'monitor_device_type',
      },
      required: true,
    },
    {
      label: t('监控设备编号'),
      field: 'code',
      component: 'Input',
      componentProps: {
        disabled: true,
      },
    },
    {
      label: t('监控设备名称'),
      field: 'name',
      component: 'Input',
      required: true,
    },
    {
      label: t('监控点唯一标识'),
      field: 'cameraIndexCode',
      component: 'Input',
      required: true,
      ifShow: ({ values }) => {
        return values.deviceType === 'MDP_01'; // 海康
      },
      componentProps: {
        maxlength: 200,
        placeholder: 'cameraIndexCode',
      },
      slot: 'cameraIndexCodeSlot',
    },
    {
      label: 'IP',
      field: 'ip',
      component: 'Input',
      required: true,
      ifShow: ({ values }) => {
        return values.deviceType === 'MDP_02'; // 大华
      },
    },
    {
      label: t('端口'),
      field: 'rtspPort',
      component: 'Input',
      required: true,
      ifShow: ({ values }) => {
        return values.deviceType === 'MDP_02'; // 大华
      },
    },
    {
      label: t('设备管理用户'),
      field: 'adminUser',
      component: 'Input',
      required: true,
      ifShow: ({ values }) => {
        return values.deviceType === 'MDP_02'; // 大华
      },
    },
    {
      label: t('设备管理用户密码'),
      field: 'adminPassword',
      component: 'Input',
      required: true,
      ifShow: ({ values }) => {
        return values.deviceType === 'MDP_02'; // 大华
      },
    },
    {
      label: t('设备资源路径'),
      field: 'rtspUri',
      component: 'Input',
      required: true,
      ifShow: ({ values }) => {
        return values.deviceType === 'MDP_02'; // 大华
      },
    },
    {
      label: t('直播通道'),
      field: 'channel',
      component: 'Input',
      required: true,
      ifShow: ({ values }) => {
        return values.deviceType === 'MDP_02'; // 大华
      },
    },
    {
      label: t('码流'),
      field: 'subtype',
      component: 'Select',
      componentProps: {
        options: [
          { label: '主码', value: '0' },
          { label: '辅码', value: '1' },
        ],
      },
      required: true,
      ifShow: ({ values }) => {
        return values.deviceType === 'MDP_02'; // 大华
      },
    },
    {
      label: t('加载路径'),
      field: 'loadSource',
      component: 'Input',
      componentProps: {
        disabled: true,
      },
      ifShow: ({ values }) => {
        return values.deviceType === 'MDP_02'; // 大华
      },
    },
    {
      label: t('直播流获取路径'),
      field: 'streamUrl',
      component: 'Input',
      componentProps: {
        disabled: true,
      },
      ifShow: ({ values }) => {
        return values.deviceType === 'MDP_02'; // 大华
      },
    },
  ];

  const [registerForm, { resetFields, setFieldsValue, updateSchema, validate, getFieldsValue }] =
    useForm({
      labelWidth: 130,
      schemas: inputFormSchemas,
      baseColProps: { lg: 24, md: 24 },
    });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    resetFields();
    setModalProps({ loading: true });
    const res = await monitorDeviceInfoForm(data);
    record.value = (res.monitorDeviceInfo || {}) as MonitorDeviceInfo;
    record.value.__t = new Date().getTime();
    const companyRes = await companyTreeData();
    updateSchema([
      {
        field: 'parentId',
        componentProps: {
          treeData: companyRes,
        },
      },
    ]);
    setFieldsValue(record.value);
    setModalProps({ loading: false });
  });

  const [hikvisionModal, { openModal }] = useModal();

  const selectSuccess = (data: {
    cameraName: string; // 监控设备名称
    cameraIndexCode: string; // 监控点唯一标识
  }) => {
    setFieldsValue({
      ...getFieldsValue(),
      name: data.cameraName,
      cameraIndexCode: data.cameraIndexCode,
    });
  };

  async function handleSubmit() {
    try {
      const data = await validate();
      setModalProps({ confirmLoading: true });
      const params: any = {
        isNewRecord: !record.value.id,
        id: record.value.id,
        recType: 0,
      };
      const res = await monitorDeviceInfoSave({ ...data, ...params });
      showMessage(res.message);
      setTimeout(closeModal);
      emit('success', data);
    } catch (error: any) {
      if (error && error.errorFields) {
        showMessage(t('您填写的信息有误，请根据提示修正。'));
      }
      console.log('error', error);
    } finally {
      // setModalProps({ confirmLoading: false });
    }
  }
</script>
