<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    @ok="handleSubmit"
    width="80%"
  >
    <!-- 离职归档提示 -->
    <div style="padding: 0 15px">
      <div v-if="hasDepartureRecord" class="mt-4 ml-4">
        <a-alert type="warning" message="该人员存在离职归档记录" show-icon />
        <a-button type="link" @click="viewDepartedRecords" class="ml-2">
          查看离职归档记录
        </a-button>
      </div>
      <BasicForm @register="registerForm" />
    </div>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicForm, useForm, FormSchema } from '@/components/swm/Form';
  import {
    getPerson,
    savePerson,
    findDepartedByIdentityCard,
    checkIdentityCard,
  } from '@/api/swm/person';
  import { findDepartureByIdentityCard } from '@/api/swm/personDeparture';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { useDict } from '@/components/swm/Dict';
  import { GenderEnum } from '@/enums/swm/personEnum';
  import { organizationTreeGetNodes } from '@/api/swm/organizationTree';
  import { dictDataListData, dictDataTreeData } from '@/api/swm/dictData'; // 获取人员类型

  const emit = defineEmits(['success', 'view-departed-records']);
  const { createMessage } = useMessage();

  const { initDict, getDictList } = useDict();

  const isUpdate = ref(false);
  const rowId = ref('');
  const hasDepartureRecord = ref(false);
  const departedData = ref([]);
  const identityCardValue = ref('');

  const jobTypeOptions = ref<Recordable[]>([]);

  const formSchema: FormSchema[] = [
    {
      field: 'id',
      component: 'Input',
      show: false,
    },
    {
      field: 'personNumber',
      label: '人员编码',
      component: 'Input',
      // required: true,
      // rules: [{ required: true, message: '请输入人员编码' }],
    },
    {
      field: 'name',
      label: '工人姓名',
      component: 'Input',
      required: true,
      rules: [{ required: true, message: '请输入姓名' }],
    },
    {
      field: 'gender',
      label: '性别',
      component: 'Select',
      required: true,
      defaultValue: GenderEnum.MALE,
      componentProps: {
        options: [
          { label: GenderEnum.MALE, value: GenderEnum.MALE },
          { label: GenderEnum.FEMALE, value: GenderEnum.FEMALE },
        ],
      },
    },
    {
      field: 'age',
      label: '年龄',
      component: 'InputNumber',
      // required: true,
      // rules: [{ required: true, message: '请输入年龄' }],
      componentProps: {
        min: 0,
        max: 120,
      },
    },
    {
      field: 'bloodType',
      label: '血型',
      component: 'Input',
    },
    {
      field: 'identityCard',
      label: '身份证号码',
      component: 'Input',
      required: true,
      rules: [
        { required: true, message: '请输入身份证号码' },
        {
          pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/,
          message: '身份证号码格式不正确',
        },
      ],
    },
    {
      field: 'phoneNumber',
      label: '手机号码',
      component: 'Input',
      required: true,
      rules: [
        { required: true, message: '请输入手机号码' },
        {
          pattern: /^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\d{8}$/,
          message: '手机号码格式不正确',
        },
      ],
    },
    {
      field: 'urgentPerson',
      label: '紧急联系人',
      component: 'Input',
      // required: true,
      // rules: [{ required: true, message: '请输入紧急联系人' }],
    },
    {
      field: 'urgentPhoneNumber',
      label: '紧急联系人电话',
      component: 'Input',
      // required: true,
      // rules: [{ required: true, message: '请输入紧急联系人手机号' }],
    },
    {
      field: 'personType',
      label: '人员类型',
      component: 'Select',
      required: true,
      // componentProps: ({ formModel }) => {
      //   return {
      //     dictType: 'person_type_enum',
      //     onChange: async (val) => {
      //       formModel.jobType = '';

      //       const dictType = `swm_job_type_${val}`;
      //       await initDict([dictType]);
      //       jobTypeOptions.value = getDictList(dictType) || [];
      //     },
      //   };
      // },
      componentProps: ({ formModel }) => {
        return {
          api: dictDataListData,
          params: { dictType: 'person_type_enum' },
          fieldNames: {
            label: 'dictLabel',
            value: 'dictValue',
          },
          immediate: true,
          allowClear: true,
          showSearch: true,
          filterOption: (input: string, option: any) => {
            return option.title.toLowerCase().indexOf(input.toLowerCase()) >= 0;
          },
          onChange: async (val) => {
            formModel.jobType = '';

            //手动获取工种类型
            const dictType = `swm_job_type_${val}`;
            const jobTypeData = await dictDataTreeData({ dictType });
            jobTypeOptions.value = jobTypeData || [];
          },
        };
      },
    },
    {
      field: 'company',
      label: '所属单位',
      component: 'Select',
      required: true,
      ifShow: ({ model }) => model.isExternalPersonnel == '1',
      componentProps: ({ formModel }) => {
        return {
          api: organizationTreeGetNodes,
          params: { nodeType: 'root' },
          fieldNames: {
            label: 'title',
            value: 'value',
          },
          immediate: true,
          allowClear: true,
          showSearch: true,
          filterOption: (input: string, option: any) => {
            return option.title.toLowerCase().indexOf(input.toLowerCase()) >= 0;
          },
          onChange: () => {
            // 清空所属车间、所属产线、所属班组
            formModel.department = '';
            formModel.prodLine = '';
            formModel.team = '';
          },
        };
      },
    },
    {
      field: 'jobType',
      label: '所属工种',
      component: 'Select',
      required: true,
      ifShow: ({ model }) => model.isExternalPersonnel == '1',
      componentProps: ({ formModel }) => {
        return {
          options: jobTypeOptions.value,
          disabled: !formModel.personType,
          allowClear: true,
          fieldNames: {
            label: 'name',
            value: 'name',
          },
        };
      },
    },
    {
      field: 'department',
      label: '所属车间',
      component: 'Select',
      required: true,
      ifShow: ({ model }) => {
        // 非内部员工不显示
        if (model.isExternalPersonnel != '1') return false;

        // 不是管理员直接通过
        if (model.personType != '1') return true;

        return false;
      },
      componentProps: ({ formModel }) => {
        return {
          api: organizationTreeGetNodes,
          params: {
            nodeType: 'office',
            parentId: formModel.company || '',
          },
          disabled: !formModel.company,
          immediate: true,
          allowClear: true,
          showSearch: true,
          fieldNames: {
            label: 'title',
            value: 'value',
          },
          filterOption: (input: string, option: any) => {
            return option.title.toLowerCase().indexOf(input.toLowerCase()) >= 0;
          },
          onChange: () => {
            // 清空所属产线、所属班组
            formModel.prodLine = '';
            formModel.team = '';
          },
        };
      },
    },
    {
      field: 'prodLine',
      label: '所属产线',
      component: 'Select',
      required: true,
      ifShow: ({ model }) => {
        // 非内部员工不显示
        if (model.isExternalPersonnel != '1') return false;

        // 不是管理员直接通过
        if (model.personType != '1') return true;

        return false;
      },
      componentProps: ({ formModel }) => {
        return {
          api: organizationTreeGetNodes,
          params: {
            nodeType: 'workshop',
            parentId: formModel.department || '',
          },
          disabled: !formModel.department,
          immediate: true,
          allowClear: true,
          showSearch: true,
          fieldNames: {
            label: 'title',
            value: 'value',
          },
          filterOption: (input: string, option: any) => {
            return option.title.toLowerCase().indexOf(input.toLowerCase()) >= 0;
          },
          onChange: () => {
            // 清空所属班组
            formModel.team = '';
          },
        };
      },
    },
    {
      field: 'team',
      label: '所属班组',
      component: 'Select',
      required: true,
      ifShow: ({ model }) => {
        // 非内部员工不显示
        if (model.isExternalPersonnel != '1') return false;

        // 不是管理员直接通过
        if (model.personType != '1') return true;

        return false;
      },
      componentProps: ({ formModel }) => {
        return {
          api: organizationTreeGetNodes,
          params: {
            nodeType: 'prodLine',
            parentId: formModel.prodLine || '',
          },
          disabled: !formModel.prodLine,
          immediate: true,
          allowClear: true,
          showSearch: true,
          fieldNames: {
            label: 'title',
            value: 'value',
          },
          filterOption: (input: string, option: any) => {
            return option.title.toLowerCase().indexOf(input.toLowerCase()) >= 0;
          },
        };
      },
    },
    {
      field: 'dept',
      label: '部门',
      component: 'Select',
      required: true,
      ifShow: ({ model }) => model.jobType == '管理人员',
      componentProps: {
        allowClear: true,
        dictType: 'swm_dept',
      },
    },
    {
      field: 'position',
      label: '职务',
      component: 'Select',
      required: true,
      ifShow: ({ model }) => model.jobType == '管理人员',
      componentProps: {
        allowClear: true,
        dictType: 'swm_position', // 新增职务字典
      },
    },
    {
      field: 'safetyEducation',
      label: '入场安全教育',
      component: 'Select',
      componentProps: {
        dictType: 'safety_education_enum',
      },
    },
    {
      field: 'isExternalPersonnel',
      label: '是否厂内员工',
      component: 'Select',
      required: true,
      componentProps: {
        dictType: 'external_personnel_enum',
      },
    },
    {
      field: 'personnelStatus',
      label: '人员状态',
      component: 'Select',
      defaultValue: '1',
      componentProps: {
        dictType: 'person_status_enum',
        disabled: true,
      },
    },
    {
      field: 'remarks',
      label: '备注',
      component: 'InputTextArea',
      colProps: { span: 24 },
    },
  ];

  const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
    labelWidth: 140,
    baseColProps: { span: 12 },
    schemas: formSchema, // 使用静态的 formSchema
    showActionButtonGroup: false,
    actionColOptions: {
      span: 24,
    },
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    resetFields();
    setModalProps({ confirmLoading: false });

    // 每次打开模态框时重置离职归档记录状态
    hasDepartureRecord.value = false;
    departedData.value = [];
    identityCardValue.value = '';

    // 检查是否为离职人员，如果是离职人员则禁止编辑
    if (data?.record?.personnelStatus === '0') {
      // '0' 表示离职
      createMessage.error('离职人员不能编辑！');
      closeModal();
      return;
    }

    isUpdate.value = !!data?.isUpdate;

    if (unref(isUpdate)) {
      rowId.value = data.record.id;
      if (rowId.value) {
        const personData = await getPerson(rowId.value);

        setFieldsValue({
          ...personData,
        });

        // 保存身份证号码，用于查询离职记录
        identityCardValue.value = personData.identityCard || '';
        if (identityCardValue.value) {
          await checkDepartureRecords(identityCardValue.value);
        }

        // 初始化工种字典
        const swmJobType = `swm_job_type_${personData.personType}`;
        const jobTypeData = await dictDataTreeData({ dictType: swmJobType });
        jobTypeOptions.value = jobTypeData || [];
      }
    } else {
      // 新建时的默认值设置
      setFieldsValue({
        status: '0',
        safetyEducation: '0', // 默认安全教育状态为"未开始"
        gender: '男',
        personnelStatus: '1', // 在职
        personType: '0', // 默认人员类型为"工人"
      });

      // 初始化工种字典
      const swmJobType = 'swm_job_type_0';
      const jobTypeData = await dictDataTreeData({ dictType: swmJobType });
      jobTypeOptions.value = jobTypeData || [];
    }

    // 更新身份证字段的onBlur事件
    setTimeout(() => {
      updateSchema({
        field: 'identityCard',
        componentProps: {
          onBlur: (e: any) => handleIdentityCardBlur(e?.target?.value || ''),
        },
      });
    }, 100);
  });

  // 检查离职记录
  async function checkDepartureRecords(identityCard: string) {
    try {
      // 首先尝试从离职表查询
      const departureRes = await findDepartureByIdentityCard(identityCard);
      if (departureRes.success && departureRes.hasRecord) {
        hasDepartureRecord.value = true;
        departedData.value = departureRes.data || [];
        createMessage.warning(`该人员存在离职归档记录，请查看离职记录信息`);
        return;
      }

      // 如果离职表没有记录，从人员表查询
      const personRes = await findDepartedByIdentityCard(identityCard);
      if (personRes.success && personRes.hasRecord) {
        hasDepartureRecord.value = true;
        departedData.value = personRes.data || [];
        createMessage.warning(`该人员存在离职归档记录，请查看离职记录信息`);
      } else {
        hasDepartureRecord.value = false;
        departedData.value = [];
      }
    } catch (error) {
      console.error('查询离职记录失败', error);
      hasDepartureRecord.value = false;
      departedData.value = [];
    }
  }

  // 身份证失去焦点时检查离职记录和重复
  async function handleIdentityCardBlur(value: string) {
    if (!value || value.length < 15) {
      hasDepartureRecord.value = false;
      departedData.value = [];
      identityCardValue.value = '';
      return;
    }

    identityCardValue.value = value;

    // 检查身份证重复（在职人员）
    try {
      const excludeId = unref(isUpdate) ? rowId.value : undefined;
      const checkRes = await checkIdentityCard(value, excludeId);

      if (checkRes.success && checkRes.exists) {
        createMessage.error(checkRes.message);
        return;
      }

      // 如果有离职记录提示
      if (checkRes.success && checkRes.hasInactivePerson) {
        createMessage.warning(checkRes.message);
      }
    } catch (error) {
      console.error('检查身份证重复失败', error);
    }

    // 检查离职记录
    await checkDepartureRecords(value);
  }

  const getTitle = computed(() => {
    return unref(isUpdate) ? '编辑人员' : '新增人员';
  });

  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });

      // 提交前再次检查身份证重复（防止并发操作）
      if (values.identityCard) {
        const excludeId = unref(isUpdate) ? rowId.value : undefined;
        const checkRes = await checkIdentityCard(values.identityCard, excludeId);

        if (checkRes.success && checkRes.exists) {
          createMessage.error(checkRes.message);
          setModalProps({ confirmLoading: false });
          return;
        }
      }

      const params = Object.assign(
        {},
        {
          company: '',
          jobType: '',
          department: '',
          prodLine: '',
          team: '',
          dept: '',
          position: '',
        },
        values,
      );

      // 保存更新
      await savePerson(params);

      createMessage.success(`${unref(isUpdate) ? '更新' : '新增'}成功！`);
      closeModal();
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }

  // 查看离职记录 - 查询离职表数据
  async function viewDepartedRecords() {
    if (!identityCardValue.value) return;

    try {
      // 直接从离职表查询数据
      const res = await findDepartureByIdentityCard(identityCardValue.value);
      if (res.success && res.hasRecord && res.data.length > 0) {
        emit('view-departed-records', res.data);
      } else {
        // 如果离职表没有记录，则查人员表
        const personRes = await findDepartedByIdentityCard(identityCardValue.value);
        if (personRes.success && personRes.hasRecord && personRes.data.length > 0) {
          emit('view-departed-records', personRes.data);
        } else {
          createMessage.warning('未找到该人员的离职记录');
        }
      }
    } catch (error) {
      console.error('查询离职记录失败', error);
      createMessage.error('查询离职记录失败');
    }
  }
</script>
