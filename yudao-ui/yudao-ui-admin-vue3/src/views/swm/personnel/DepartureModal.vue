<!--
  @author Shawn
  @date 2024-10-22
-->
<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    title="处理离职"
    @ok="handleSubmit"
    width="600px"
  >
    <div style="padding: 0px 20px">
      <BasicForm @register="registerForm" />
    </div>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicForm, useForm } from '@/components/swm/Form';
  import { formSchema } from './departure.data';
  import { handlePersonDeparture } from '@/api/swm/person';
  import { useMessage } from '@/hooks/swm/useMessage';
  import { defHttp } from '@/utils/http/axios';
  import { unassignPerson } from '@/api/swm/helmetDevice';

  // 定义人员信息类型
  interface PersonInfo {
    id: string;
    name: string;
    safetyHelmetId?: string;
    [key: string]: any;
  }

  export default defineComponent({
    name: 'DepartureModal',
    components: { BasicModal, BasicForm },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { createMessage } = useMessage();
      const personInfo = ref<PersonInfo | null>(null);

      const [registerForm, { setFieldsValue, validate, resetFields }] = useForm({
        labelWidth: 80,
        schemas: formSchema,
        showActionButtonGroup: false,
      });

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        resetFields();
        setModalProps({ confirmLoading: false });

        // 初始化表单数据
        if (data) {
          personInfo.value = data.record as PersonInfo;
          setFieldsValue({
            id: data.record.id,
            name: data.record.name,
          });
        }
      });

      async function handleSubmit() {
        try {
          const values = await validate();
          setModalProps({ confirmLoading: true });

          // 如果选择了已归还安全帽，需要解绑安全帽
          if (values.helmetReturned === '1' && personInfo.value) {
            try {
              // 获取人员当前绑定的安全帽信息
              const personData = await defHttp.get({
                url: '/swm/swmPerson/form',
                params: { id: personInfo.value.id },
              });

              // 如果存在绑定的安全帽，解除绑定
              if (personData && personData.safetyHelmetId) {
                console.log('解除安全帽绑定:', personData.safetyHelmetId);

                // 调用解绑API
                await unassignPerson(personData.safetyHelmetId);

                // 同时将人员的safetyHelmetId设为空
                await defHttp.post(
                  {
                    url: '/swm/swmPerson/clearSafetyHelmet',
                    params: { id: personInfo.value.id },
                  },
                  { isTransformResponse: false },
                );

                console.log('安全帽解绑成功');
              }
            } catch (error) {
              console.error('解绑安全帽失败', error);
              // 继续处理离职流程，不因解绑失败而中断
            }
          }

          // 调用离职处理API
          await handlePersonDeparture(values);

          createMessage.success('离职处理成功');
          closeModal();
          emit('success');
        } catch (error) {
          createMessage.error('离职处理失败');
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      return {
        registerModal,
        registerForm,
        handleSubmit,
      };
    },
  });
</script>
