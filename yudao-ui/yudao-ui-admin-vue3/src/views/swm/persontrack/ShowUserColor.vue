<template>
  <div class="bigbox">
    <div class="btn" @click="initDataList"> <ExclamationCircleOutlined /> &nbsp; 查看颜色 </div>
    <div v-if="isShowModal" class="mask"></div>

    <div v-if="isShowModal" class="msgModal">
      <div class="topTitle">
        <div></div>
        <div class="tile">颜色信息</div>
        <div class="icon" @click="isShowModal = false">
          <CloseOutlined />
        </div>
      </div>
      <Spin
        :spinning="projectList.loadingAre"
        tip="加载中..."
        size="large"
        style="text-align: center; margin-top: 25%; font-size: 36px; color: #fff"
      >
        <div class="colorBigBox">
          <div v-for="item in projectList.dataList" class="colorItem" :key="item.id">
            <div class="colorNmae" :title="item.subitemName">{{ item.subitemName }}</div>
            <div class="color" :style="`background-color:${item.color} ;`"></div>
          </div>
          <div style="width: 200px"></div>
          <div style="width: 200px"></div>
          <div style="width: 200px"></div>
          <div style="width: 200px"></div>
          <div style="width: 200px"></div>
          <div style="width: 200px"></div>
        </div>
      </Spin>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { reactive, ref } from 'vue';
  import { Spin } from 'ant-design-vue';
  import { CloseOutlined } from '@ant-design/icons-vue';
  import { getHelmetSubitemsByParentId } from '@/api/swm/helmetConfig';
  import { ExclamationCircleOutlined } from '@ant-design/icons-vue';

  const props = defineProps({
    userType: String,
  });

  let isShowModal = ref(false);

  let projectList = reactive({
    optionList1: [
      { label: '按车间展示', value: '1937057853333651456' },
      { label: '按班组展示', value: '1937057908643938304' },
      { label: '按人员类型展示', value: '1937058054295339008' },
      { label: '按工种展示', value: '1937058127666298880' },
    ],

    dataList: [] as any,
    waingType: null,
    waingMsg: null,
    current: 1,
    size: 20,
    count: 0,
    loadingAre: false,
  });
  const initDataList = () => {
    console.log(props.userType);

    isShowModal.value = !isShowModal.value;
    getMsgList();
  };
  const handlePageChange = (page) => {
    if (projectList.current == page) return;
    projectList.current = page;
    getMsgList();
  };
  const getMsgList = () => {
    projectList.loadingAre = true;
    let parentId = '';
    projectList.optionList1.forEach((element) => {
      if (element.label == props.userType) {
        parentId = element.value;
      }
    });
    if (parentId == '') {
      return;
    }

    // 颜色信息
    getHelmetSubitemsByParentId({ parentId: parentId })
      .then((result) => {
        console.log(result);
        projectList.dataList = result;
      })
      .catch((err) => {})
      .finally(() => {
        projectList.loadingAre = false;
      });
  };
  getMsgList();
</script>

<style lang="less" scoped>
  .bigbox {
    box-sizing: border-box;

    .btn {
      position: absolute;
      left: 247px;
      top: 23px;
      width: 103px;
      height: 28px;
      line-height: 28px;
      display: flex;
      align-items: center;
      background: linear-gradient(180deg, #19acc2 0%, #0f5c67 100%);
      border-radius: 51px;
      font-family: Noto Sans SC, Noto Sans SC;
      font-weight: 500;
      font-size: 13px;
      color: #f2f2f2;
      text-align: center;
      font-style: normal;
      text-transform: none;
      padding: 0px 14px;
      user-select: none;
      cursor: pointer;

      .img {
        width: 18px;
        height: 18px;
        margin-right: 4px;
      }
    }

    .mask {
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background-color: rgba(0, 0, 0, 0.5);
      z-index: 9998;
    }

    .msgModal {
      width: 1023px;
      min-height: 300px;
      max-height: 691px;
      position: fixed;
      z-index: 9999;
      left: calc(50vw - 1023px / 2);
      top: 218px;
      margin: 0 auto;
      background: url('@/assets/swm/modalBg.png') no-repeat left top / 100% 100%;
    }

    .colorBigBox {
      width: 100%;
      padding: 15px 44px;
      padding-bottom: 44px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      flex-wrap: wrap;
      max-height: 570px;
      overflow-y: auto;
      scrollbar-width: none;

      .colorItem {
        width: 200px;
        font-weight: 500;
        font-size: 15px;
        color: #ffffff;
        line-height: 20px;
        letter-spacing: 0.5px;
        text-align: center;
        margin-top: 13px;
        display: flex;
        align-items: center;
        justify-content: space-between;

        .color {
          width: 20px;
          height: 20px;
          border-radius: 50%;
        }

        .colorNmae {
          margin-top: 8px;
          color: #7be5ff;
          width: 175px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          text-align: left;
        }
      }
    }

    .topTitle {
      margin-top: 26px;
      height: 32px;
      font-family: Noto Sans SC, Noto Sans SC;
      font-weight: bold;
      font-size: 22px;
      color: #7be5ff;
      letter-spacing: 1px;
      text-align: left;
      font-style: normal;
      text-transform: none;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 44px;

      .icon {
        font-size: 25px;
        cursor: pointer;
      }
    }
  }
</style>
