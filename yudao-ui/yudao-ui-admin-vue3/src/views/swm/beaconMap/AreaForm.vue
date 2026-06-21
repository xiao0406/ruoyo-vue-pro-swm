<template>
  <div>
    <Form
      :model="formState"
      :label-col="labelCol"
      :wrapper-col="wrapperCol"
      ref="formRef"
      :rules="rules"
    >
      <FormItem label="区域名称" name="areaName">
        <Input
          v-model:value="formState.data.areaName"
          @change="() => changeFormStateValue('areaName', formState.data.areaName)"
        />
      </FormItem>
      <FormItem label="信标颜色" name="beaconColor">
        <Input
          v-model:value="formState.data.beaconColor"
          type="color"
          @change="(e) => changeFormStateValue('beaconColor', e.target.value)"
          style="width: 60px"
        />
      </FormItem>
      <FormItem label="是否大屏展示" name="isScreenShow">
        <Switch
          v-model:checked="formState.data.isScreenShow"
          @change="(e) => changeFormStateValue('isScreenShow', e)"
        />
      </FormItem>
      <!-- <FormItem label="开启语音报警" name="voiceEnabled">
        <Switch v-model:checked="formState.data.checked"
          @change="(e) =>changeFormStateValue('voiceEnabled', e  ? '1' :'0' )" />
      </FormItem>
      <FormItem label="报警语音" name="voicePrompt">
        <Select v-model:value="formState.data.voicePrompt" placeholder="请选择语音模板" :options="voiceTemplateList.data"
          :fieldNames="{label:'templateName',value:'id'}"
          @change="(value) => changeFormStateValue('voicePrompt', value)" />
      </FormItem> -->
      <FormItem label="区域类型" name="areaType">
        <Select
          v-model:value="formState.data.areaType"
          placeholder="请选择"
          :options="areaTypeList"
          @change="(value) => changeFormStateValue('areaType', value)"
        />
      </FormItem>
      <FormItem label="文件路径" name="filePath">
        <Input
          v-model:value="formState.data.filePath"
          @change="() => changeFormStateValue('filePath', formState.data.filePath)"
          placeholder="请输入文件路径"
        />
      </FormItem>
      <FormItem label="信标列表" name="beaconList">
        <div>
          <Tag
            :title="item.pixelX + ',' + item.pixelY"
            v-for="item in formState.data.beaconList"
            :key="item.id"
            :bordered="false"
            style="margin: 5px"
            closable
            @close="closeIcon(item)"
          >
            {{ item.pixelX }}, {{ item.pixelY }}
          </Tag>
        </div>
      </FormItem>
      <!-- <FormItem label=" " name="accessPersonIds">
        <div>
          <Tag v-for="item in formState.data.accessPersonIds" :key="item.id" :bordered="false" style="margin: 5px;" closable
            @close="closeIcon(item)">
            {{ item.pixelX }}, {{ item.pixely }}
          </Tag>
          <Button type="primary" :disabled="formState.data.isEdit == false" size="small"
            @click="batchManagementModal(true)">查看区域人员</Button>
        </div>
      </FormItem> -->
      <div class="region-btns">
        <Button type="primary" size="small" @click="handleSubmit">{{
          formState.data.isEdit == false ? '新增区域' : '保存编辑'
        }}</Button>
        <Button size="small" @click="emit('close')">关闭</Button>
      </div>
    </Form>
    <div style="color: #000000; padding: 13px 32px; user-select: none; font-size: 11px">
      <p>提示：</p>
      <p>1.点击选择图上未被选择的“电子围栏”信标，组成区域。</p>
      <p>1.信标之间有顺序之分，点击信标可连成线，若顺序混乱，建议删除之前信标，重新添加</p>
      <p>1.连成区域的信标将统一颜色，您可再次标记信标颜色来修改成你想要的颜色</p>
    </div>
    <!-- <StaffListModal
      @register="register"
      @success="batchSuccess"
      :formState="formState"
      :areaTypeList="areaTypeList"
    /> -->
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, watch } from 'vue';
  import { Form, Input, Select, Button, Tag, message, Switch } from 'ant-design-vue';
  import { useDict } from '@/components/swm/Dict';
  // import StaffModal from './staffModal.vue';
  // import StaffListModal from './StaffListModal.vue';
  // import { useModal } from '@/components/swm/Modal';
  import { getVoiceTemplateList } from '@/api/swm/voiceTemplate';
  // import { checkBeaconConflicts } from '@/api/swm/area';
  import { saveAreaWithBeaconIds } from '@/api/swm/newDataScree';
  import { refreshAllCache } from '@/api/swm/cache';
  // const [register, { openModal: batchManagementModal }] = useModal();

  const FormItem = Form.Item;
  // 定义props
  const props = defineProps({
    initialData: {
      type: Object,
      default: () => ({}),
    },
  });
  // let staffListModalVisible = ref(false);
  // 定义emits
  const emit = defineEmits(['changeFormStateValue', 'colorChange', 'close', 'deleteIcon']);

  // 表单验证规则
  const rules = {
    areaName: [{ required: true, message: '请输入区域名称', trigger: 'blur' }],
    areaType: [
      {
        required: true,
        message: '请选择区域类型',
        trigger: 'change',
      },
    ],
    list: [{ required: true, message: '请输入信标列表', trigger: 'blur' }],
  };

  // 表单布局
  const labelCol = ref({ span: 8 });
  const wrapperCol = ref({ span: 18 });

  // 表单状态
  const formState = reactive({
    data: {
      id: '',
      areaName: '',
      beaconColor: '',
      voicePrompt: '',
      areaType: '',
      filePath: '',
      beaconList: '' as any,
      isEdit: false,
      isScreenShow: false,
    } as any,
  });

  // 表单引用
  const formRef = ref();

  // 监听props变化，更新表单数据
  watch(
    () => props.initialData,
    (newVal) => {
      Object.assign(formState.data, newVal);
    },
    { immediate: true, deep: true },
  );

  // 颜色变化处理
  // const logColor = (e) => {
  //   emit('colorChange', e.target.value);
  // };

  // 暴露方法：验证表单
  const validate = async () => {
    try {
      await formRef.value.validate();
      return formState;
    } catch (error) {
      throw error;
    }
  };

  // 暴露方法：重置表单
  const resetFields = () => {
    formRef.value?.resetFields();
  };

  // 暴露组件方法
  defineExpose({
    validate,
    resetFields,
  });

  let areaTypeList: any = ref([]);
  // 使用字典管理信标类型
  const { initDict, getDictList } = useDict();
  let voiceTemplateList = reactive({
    data: [],
  });
  // 初始化字典数据
  async function initDictData() {
    await initDict(['area_type']);
    // 获取区域类型字典数据
    const areaTypeDict = getDictList('area_type');
    if (areaTypeDict && areaTypeDict.length > 0) {
      areaTypeList.value = areaTypeDict.map((item) => ({
        label: item.name,
        value: item.value,
      }));
    } else {
      // 如果字典加载失败，使用默认值（匹配字典值）
      areaTypeList.value = [
        { label: '工作区', value: '0' },
        { label: '休息区', value: '1' },
      ];
    }
    getVoiceTemplateList()
      .then((result) => {
        console.log('语音模板', result.list);
        voiceTemplateList.data = result.list;
      })
      .catch((_err) => {});
  }
  initDictData();
  const closeIcon = (item) => {
    emit('deleteIcon', item.id);
  };
  const isSubmitting = ref(false);
  const handleSubmit = () => {
    if (isSubmitting.value) return;
    isSubmitting.value = true;
    formState.data.bids = formState.data.beaconList.map((ele) => {
      return ele.id;
    });
    if (formState.data.areaName?.length > 0) {
    } else {
      message.warning('区域名称不能为空');
      isSubmitting.value = false;
      return;
    }
    if (formState.data.bids?.length > 2) {
    } else {
      message.warning('最少选择三个信标');
      isSubmitting.value = false;
      return;
    }

    saveAreaWithBeaconIds(formState.data)
      .then(async (_result) => {
        message.success(formState.data.isEdit ? '保存区域成功' : '新增区域成功！');
        // 刷新缓存
        try {
          await refreshAllCache();
        } catch (error) {
          console.error('刷新缓存失败:', error);
        }
        emit('close');
        isSubmitting.value = false;
      })
      .catch((_err) => {
        isSubmitting.value = false;
      });
  };
  const changeFormStateValue = (key, value) => {
    emit('changeFormStateValue', key, value);
  };
</script>

<style scoped>
  .region-btns {
    width: 260px;
    display: flex;
    flex-direction: row;
    justify-content: center;
    align-items: center;
    gap: 10px;
    margin: auto;

    .ant-btn-dangerous.ant-btn-primary {
      border-color: red;
      background: red;
    }
  }
</style>
