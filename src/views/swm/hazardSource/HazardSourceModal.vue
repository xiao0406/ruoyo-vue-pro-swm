<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    @ok="handleSubmit"
    :width="800"
    :okText="'确认'"
    :cancelText="'取消'"
    :closeFunc="handleClose"
  >
    <template #title>
      <Icon :icon="getTitle.icon" class="pr-1 m-1" />
      <span> {{ getTitle.value }} </span>
    </template>
    <template #footer>
      <a-space>
        <a-button @click="closeModal">取消</a-button>
        <a-button type="primary" @click="handleSubmit" :loading="confirmLoading">确认</a-button>
        <a-button @click="handleDraft" :loading="confirmLoading">保存草稿</a-button>
      </a-space>
    </template>
    <div style="padding: 0px 30px">
      <BasicForm @register="registerForm" />
    </div>
  </BasicModal>
</template>
<script lang="ts">
  /**
   * 风险编辑对话框
   * @author Shawn
   * @date 2025-05-22
   */
  import { defineComponent, ref, computed, unref, onMounted, watch, nextTick } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicForm, useForm } from '@/components/swm/Form/index';
  import { router } from '@/router';
  import { Icon } from '@/components/swm/Icon';
  import { formSchema, editFormSchema, setCurrentEditingHazardSourceId } from './hazardSourceData';
  import {
    getHazardSource,
    saveHazardSource,
    tempSaveHazardSource,
    HazardSourceModel,
  } from '@/api/swm/hazardSource';
  import { getAvailableDangerousSourceBeaconSelectList } from '@/api/swm/beacon';
  import { getAllPersons } from '@/api/swm/person';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { useDict } from '@/components/swm/Dict';

  export default defineComponent({
    name: 'HazardSourceModal',
    components: { BasicModal, BasicForm, Icon },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const isUpdate = ref(false);
      const rowId = ref('');
      const { createMessage } = useMessage();
      const { initDict } = useDict();
      const confirmLoading = ref(false);
      const originalIsPatrolIncluded = ref('');
      const personList = ref<any[]>([]); // 添加 personList 的定义

      // 初始化字典数据
      onMounted(() => {
        initDict(['hazard_category_enum', 'is_patrol_included_enum', 'hazard_status_enum']);
      });

      // 获取人员列表的函数
      const fetchPersonList = async () => {
        try {
          const personsRes = await getAllPersons();
          personList.value =
            personsRes.data?.map((item) => ({
              label: item.name,
              value: item.id,
              identityCard: item.identityCard,
              personnelName: item.name,
            })) || [];
        } catch (error) {
          console.error('获取人员列表失败:', error);
          personList.value = [];
        }
      };

      const getTitle = computed(() => ({
        icon: router.currentRoute.value.meta.icon || 'ant-design:book-outlined',
        value: isUpdate ? '编辑风险' : '新增风险',
      }));

      const [
        registerForm,
        { resetFields, setFieldsValue, validate, updateSchema, getFieldsValue },
      ] = useForm({
        labelWidth: 100,
        baseColProps: { span: 12 },
        schemas: formSchema,
        showActionButtonGroup: false,
        actionColOptions: {
          span: 24,
        },
      });

      // 监听是否加入巡检的变化，动态设置相关字段的校验规则
      watch(
        () => {
          const values = getFieldsValue();
          return values && values.isPatrolIncluded;
        },
        (val) => {
          if (val === '1') {
            // 如果选择加入巡检，设置默认值并更新校验规则
            const now = new Date();
            const values = getFieldsValue();
            // 如果首次巡检时间未设置，则默认为当前时间
            if (values && !values.firstInspectionTime) {
              setFieldsValue({ firstInspectionTime: now });
            }
          }
        },
        {
          immediate: false, // 避免组件初始化时执行
        },
      );

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        resetFields();
        setModalProps({ confirmLoading: false });
        isUpdate.value = !!data?.isUpdate;

        // 设置当前编辑的风险ID
        const currentRecordId = data?.record?.id || null;
        setCurrentEditingHazardSourceId(currentRecordId);

        // 获取人员列表用于白名单
        await fetchPersonList();

        // 更新表单schema，设置人员白名单的选项
        updateSchema([
          {
            field: 'personWhiteList',
            componentProps: {
              options: personList.value,
            },
          },
        ]);

        // 强制刷新表单，触发重新加载
        updateSchema([
          {
            field: 'beaconIdentifier',
            componentProps: {
              // 添加一个时间戳来强制刷新
              key: Date.now(),
            },
          },
        ]);

        if (unref(isUpdate)) {
          rowId.value = data.record.id;
          if (rowId.value) {
            const hazardData = await getHazardSource({ id: rowId.value });

            // 处理人员白名单回显
            let personWhiteListValue: string[] = [];
            if (hazardData.filterPersonnel) {
              // 如果有人员名称，尝试从 personList 中找到对应的ID
              const personnelNames = hazardData.filterPersonnel
                .split(',')
                .filter((name: string) => name.trim());
              personnelNames.forEach((name: string) => {
                const person = personList.value.find((item) => item.personnelName === name.trim());
                if (person) {
                  personWhiteListValue.push(person.value);
                }
              });
            }

            // 处理巡检负责人回显
            if (hazardData.responsiblePersonId && hazardData.isPatrolIncluded === '1') {
              try {
                // 获取所有人员列表
                const personsRes = await getAllPersons();
                const persons = personsRes.data || [];

                // 查找对应的负责人
                const responsiblePerson = persons.find(
                  (p) => p.id === hazardData.responsiblePersonId,
                );

                if (responsiblePerson) {
                  // 创建初始选项，确保能立即显示
                  const currentOption = {
                    label: responsiblePerson.name,
                    value: responsiblePerson.id,
                  };

                  // 更新负责人字段的选项
                  updateSchema([
                    {
                      field: 'responsiblePersonId',
                      componentProps: {
                        // 设置包含当前负责人的选项列表
                        api: async () => {
                          const res = await getAllPersons();
                          return (
                            res.data?.map((item) => ({
                              label: item.name,
                              value: item.id,
                            })) || []
                          );
                        },
                        immediate: true,
                      },
                    },
                  ]);

                  // 等待schema更新
                  await nextTick();
                }
              } catch (error) {
                console.error('获取负责人信息失败:', error);
              }
            }

            setFieldsValue({
              ...hazardData,
              personWhiteList: personWhiteListValue, // 设置多选值
            });

            // 记录原始的"是否加入巡检"状态（仅用于显示，不强制保持）
            originalIsPatrolIncluded.value = hazardData.isPatrolIncluded;

            // 移除强制禁用逻辑，允许用户根据实际情况修改巡检状态
            // 这样当巡检计划被删除后，用户可以重新设置风险的巡检状态
            updateSchema([
              {
                field: 'isPatrolIncluded',
                componentProps: {
                  disabled: false, // 始终允许编辑
                },
              },
              {
                field: 'frequencyDays',
                componentProps: {
                  disabled: false,
                },
              },
              {
                field: 'responsiblePersonId',
                componentProps: {
                  disabled: false,
                },
              },
              {
                field: 'firstInspectionTime',
                componentProps: {
                  disabled: false,
                },
              },
            ]);
          }
        } else {
          // 新建时的默认值设置
          const now = new Date();
          setFieldsValue({
            isPatrolIncluded: '1', // 默认加入巡检
            hazardStatus: '0', // 默认待处理
            registrationTime: now,
            firstInspectionTime: now, // 默认首次巡检时间为当前时间
            personWhiteList: [], // 确保新建时是空数组
          });

          // 确保在新增模式下，相关字段不被禁用
          updateSchema([
            {
              field: 'isPatrolIncluded',
              componentProps: {
                disabled: false,
              },
            },
            {
              field: 'frequencyDays',
              componentProps: {
                disabled: false,
              },
            },
            {
              field: 'responsiblePersonId',
              componentProps: {
                disabled: false,
              },
            },
            {
              field: 'firstInspectionTime',
              componentProps: {
                disabled: false,
              },
            },
          ]);
        }
      });

      // 处理关闭
      function handleClose() {
        closeModal();
        return Promise.resolve(true);
      }

      // 人员白名单数据处理函数
      const processPersonWhiteList = (values: any) => {
        // 确保 personWhiteList 是数组
        let personWhiteListArray: string[] = [];

        if (values.personWhiteList) {
          if (Array.isArray(values.personWhiteList)) {
            personWhiteListArray = values.personWhiteList;
          } else if (typeof values.personWhiteList === 'string') {
            // 如果是字符串，尝试按逗号分割
            personWhiteListArray = values.personWhiteList
              .split(',')
              .filter((item: string) => item.trim());
          }
        }

        // 处理人员白名单字段 - 将选中的选项拆分为身份证和人员名称
        if (personWhiteListArray.length > 0) {
          // 从选项数据中提取身份证和人员名称
          const selectedIdentities: string[] = [];
          const selectedPersonnel: string[] = [];

          personWhiteListArray.forEach((personId: string) => {
            // 找到对应的选项数据
            const personOption = personList.value.find((item) => item.value === personId);
            if (personOption && personOption.identityCard && personOption.personnelName) {
              selectedIdentities.push(personOption.identityCard);
              selectedPersonnel.push(personOption.personnelName);
            }
          });

          // 转换为逗号分隔的字符串
          values.filterIdentityCard = selectedIdentities.join(',');
          values.filterPersonnel = selectedPersonnel.join(',');
        } else {
          // 如果没有选择人员白名单，清空字段
          values.filterIdentityCard = '';
          values.filterPersonnel = '';
        }
        return values;
      };

      // 暂存功能
      async function handleDraft() {
        try {
          // 获取表单当前值，不进行验证
          const values = getFieldsValue();
          confirmLoading.value = true;

          // 如果是编辑，需要传入ID
          if (unref(isUpdate)) {
            values.id = rowId.value;
          }

          // 处理人员白名单字段
          processPersonWhiteList(values);

          // 暂存数据，作为JSON对象发送
          await tempSaveHazardSource(values as unknown as HazardSourceModel);

          createMessage.success('风险暂存成功！');
          closeModal();
          emit('success');
        } catch (error) {
          console.error('暂存风险失败:', error);
          createMessage.error('暂存失败，请检查数据后重试!');
        } finally {
          confirmLoading.value = false;
        }
      }

      async function handleSubmit() {
        try {
          const values = await validate();
          confirmLoading.value = true;

          // 如果是编辑，需要传入ID
          if (unref(isUpdate)) {
            values.id = rowId.value;
          }

          // 处理人员白名单字段
          processPersonWhiteList(values);

          // 如果选择不加入巡检，清除巡检相关字段
          if (values.isPatrolIncluded !== '1') {
            values.frequencyDays = null;
            values.responsiblePersonId = null;
            values.firstInspectionTime = null;
          } else {
            // 如果选择加入巡检，确保必填字段已填写
            // 注意：frequencyDays可以为0，所以使用 !== undefined 和 !== null 来检查
            if (
              values.frequencyDays === undefined ||
              values.frequencyDays === null ||
              !values.responsiblePersonId ||
              !values.firstInspectionTime
            ) {
              createMessage.error('请填写巡检相关必填信息！');
              confirmLoading.value = false;
              return;
            }
          }

          // 保存更新，并标记为非草稿状态
          values.isDraft = '0';
          await saveHazardSource(values as unknown as HazardSourceModel);

          createMessage.success(`${unref(isUpdate) ? '更新' : '新增'}成功！`);
          closeModal();
          emit('success');
        } finally {
          confirmLoading.value = false;
        }
      }

      return {
        registerModal,
        registerForm,
        getTitle,
        handleSubmit,
        handleDraft,
        closeModal,
        confirmLoading,
        handleClose,
      };
    },
  });
</script>
<style lang="css" scoped>
  :deep(.ant-form-item-label) {
    min-width: 120px;
  }
</style>
