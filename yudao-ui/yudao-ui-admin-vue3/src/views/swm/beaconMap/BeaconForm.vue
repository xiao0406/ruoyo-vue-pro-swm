<template>
  <div class="beacon-form-container">
    <Form
      style="width: 290px"
      :model="formBeacon"
      :label-col="labelCol"
      :wrapper-col="wrapperCol"
      ref="formBeaconRef"
      :rules="beaconRules"
    >
      <FormItem label="mac地址" v-if="submitBtnText == '保存修改'" name="mac">
        <Input
          @change="() => changeFormStateValue('beaconId', formState.beaconId)"
          disabled="true"
          v-model:value="formBeacon.beaconId"
          placeholder="请选择"
        />
      </FormItem>
      <FormItem label="设备名称" v-if="submitBtnText == '保存修改'" name="deviceName">
        <Input
          @change="() => changeFormStateValue('deviceName', formState.deviceName)"
          disabled="true"
          v-model:value="formBeacon.deviceName"
          placeholder="请选择"
        />
      </FormItem>
      <FormItem label="信标类型" name="beaconType">
        <Select
          @change="
            (e) => {
              changeFormStateValue('beaconType', e);
            }
          "
          :options="beaconList"
          placeholder="请选择信标类型"
          v-model:value="formBeacon.beaconType"
        />
      </FormItem>
      <FormItem
        v-if="formBeacon.beaconType !== '2' && formBeacon.beaconType !== '电子围栏'"
        label="MAC地址"
        name="beaconId"
      >
        <Input
          @change="() => changeFormStateValue('beaconId', formState.beaconId)"
          v-model:value="formBeacon.beaconId"
          placeholder="请输入MAC地址"
        />
      </FormItem>
      <!-- <FormItem label="围栏类型" name="controlType">
        <Select @change="(e)=>{
          changeFormStateValue('controlType', e)
        }" v-model:value="formBeacon.controlType" placeholder="请选择" :options="controlOptions" />
      </FormItem> -->

      <FormItem label="所在位置" name="location">
        <Input
          @change="() => changeFormStateValue('location', formState.location)"
          v-model:value="formBeacon.location"
          placeholder="请输入"
        />
      </FormItem>

      <FormItem label="横坐标" name="x">
        <InputNumber
          style="width: 100%"
          v-model:value="formBeacon.pixelX"
          @change="changeXy(formBeacon.pixelY, formBeacon.pixelX, formBeacon.beaconColor)"
          placeholder="请输入"
        />
      </FormItem>
      <FormItem label="纵坐标" name="y">
        <InputNumber
          style="width: 100%"
          v-model:value="formBeacon.pixelY"
          @change="changeXy(formBeacon.pixelY, formBeacon.pixelX, formBeacon.beaconColor)"
          placeholder="请输入"
        />
      </FormItem>
      <FormItem label="GPS经度" name="longitudeGPS">
        <Input v-model:value="formBeacon.longitudeGPS" :disabled="true" placeholder="请输入" />
      </FormItem>
      <FormItem label="GPS纬度" name="latitudeGPS">
        <Input v-model:value="formBeacon.latitudeGPS" :disabled="true" placeholder="请输入" />
      </FormItem>
      <FormItem label="信标颜色" name="beaconColor">
        <Input
          :disabled="formBeacon.area?.length > 0"
          @change="changeXy(formBeacon.pixelY, formBeacon.pixelX, formBeacon.beaconColor)"
          v-model:value="formBeacon.beaconColor"
          type="color"
          style="width: 60px"
        />
      </FormItem>
    </Form>
    <div class="region-btns">
      <Button type="primary" size="small" :size="footerSize" @click="handleSubmit">{{
        submitBtnText
      }}</Button>
      <Button
        size="small"
        v-if="submitBtnText == '保存修改'"
        type="primary"
        danger
        :size="footerSize"
        @click="beaconDelete"
        >删除</Button
      >
      <Button size="small" :size="footerSize" @click="handleExit">关闭</Button>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive, watch, defineProps, defineEmits } from 'vue';
  import { Form, Input, Select, Button, message, Modal, InputNumber } from 'ant-design-vue';
  import { getAllBeaconList, deleteBeacon, saveBeacon } from '@/api/swm/beacon';
  import { refreshAllCache } from '@/api/swm/cache';

  import { useDict } from '@/components/swm/Dict';

  const FormItem = Form.Item;

  // 定义props
  const props = defineProps({
    beaconData: {
      type: Object,
      default: () => ({}),
    },
    isEditMode: {
      type: Boolean,
      default: false,
    },
    submitBtnText: {
      type: String,
      default: '新增信标',
    },
  });

  // 定义emits
  const emits = defineEmits(['submit', 'delete', 'exit', 'changexy', 'changeFormStateValue']);

  // 使用字典管理信标类型
  const beaconList = ref([
    { label: '常规信标', value: '常规信标' },
    { label: '电子围栏', value: '电子围栏' },
    { label: '风险信标', value: '风险信标' },
  ]);
  // 使用字典管理信标类型
  const { initDict, getDictList } = useDict();

  // 表单布局
  const labelCol = ref({ span: 8 });
  const wrapperCol = ref({ span: 18 });
  const footerSize = ref('large');

  // 表单数据
  const formBeacon = reactive({
    id: '',
    location: '',
    beaconId: '',
    mapCoord: '',
    gpsCoord: '0,0',
    beaconStatus: '在线',
    beaconStatusText: '',
    deployStatus: '',
    deployStatusText: '',
    beaconType: '',
    controlType: '一车间',
    y: 0,
    x: 0,
    pixelX: 0,
    pixelY: 0,
    longitudeGPS: '',
    latitudeGPS: '',
    beaconColor: '#ff0000',
  });

  // 围栏类型选项
  const controlOptions = ref([
    { label: '一车间', value: '一车间' },
    { label: '茶水间', value: '茶水间' },
    { label: '成品堆场', value: '成品堆场' },
    { label: '半成品堆场', value: '半成品堆场' },
    { label: '包装车间', value: '包装车间' },
  ]);

  // 表单验证规则
  const beaconRules = {
    beaconId: [
      {
        required: ({ formModel }) =>
          formModel.beaconType !== '2' && formModel.beaconType !== '电子围栏',
        message: '请输入MAC地址',
        trigger: 'blur',
      },
    ],
    beaconType: [{ required: true, message: '请选择信标类型', trigger: 'change' }],
  };

  const formBeaconRef = ref();

  // 监听props.beaconData变化，更新表单数据
  watch(
    () => props.beaconData,
    (newVal) => {
      if (newVal) {
        console.log(newVal);

        Object.assign(formBeacon, newVal);
      }
    },
    { immediate: true, deep: true },
  );

  // 初始化字典数据
  async function initDictData() {
    await initDict(['beacon_type_enum', 'area_type']);

    const beaconTypeDict = getDictList('beacon_type_enum');
    if (beaconTypeDict && beaconTypeDict.length > 0) {
      beaconList.value = beaconTypeDict.map((item) => ({
        label: item.name,
        value: item.value,
      }));
    }
  }

  // 提交表单
  const handleSubmit = async () => {
    // 信标新增
    try {
      // 表单验证
      await formBeaconRef.value.validate();

      // 构建提交数据
      const submitData = {
        ...formBeacon,
        mapCoord: `(${formBeacon.pixelX}, ${formBeacon.pixelY})`, // 添加括号并加空格
      };
      submitData.id = submitData.id == 'testaaa' ? null : submitData.id;
      // 调用保存接口
      await saveBeacon(submitData);
      message.success(formBeacon.id ? '保存信标成功' : '新增信标成功！');
      // 刷新缓存
      try {
        await refreshAllCache();
      } catch (error) {
        console.error('刷新缓存失败:', error);
      }
      emits('submit');
    } catch (error) {
      console.log(error);

      if (error.errorFields) {
        // 表单验证失败
        message.error('请填写必填项');
      } else {
        message.error('保存失败，请重试');
      }
    }
  };

  //删除信标基点
  const beaconDelete = async () => {
    Modal.confirm({
      title: '确定要删除该信标吗？',
      content: '删除后将无法恢复，请确认操作。',
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        try {
          await deleteBeacon(formBeacon.id);
          message.success('删除成功');
          // 刷新缓存
          try {
            await refreshAllCache();
          } catch (error) {
            console.error('刷新缓存失败:', error);
          }
          emits('exit');
        } catch (error) {
          message.error('删除失败');
        }
      },
    });
  };

  // 退出编辑
  const handleExit = () => {
    formBeaconRef.value?.resetFields();
    emits('exit');
  };

  // 初始化
  initDictData();
  const changeXy = (y, x, color) => {
    emits('changexy', x, y, color);
  };
  const changeFormStateValue = (key, value) => {
    console.log(key, value);

    emits('changeFormStateValue', key, value);
  };
</script>

<style lang="less" scoped>
  .beacon-form-container {
    background-color: #f6f6f6;
    padding: 10px 0px;
  }

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
