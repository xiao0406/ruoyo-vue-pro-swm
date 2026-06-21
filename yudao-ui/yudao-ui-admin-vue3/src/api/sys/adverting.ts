/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author 邹春艳
 */

import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';

const { fmsPath } = useGlobSetting();

//  主页内容新增或修改
export interface dvertisingMainSave {
  id?: string;
  isNewRecord?: string; // 是否新纪录
  areaOne?: string; // 必须
  areaTwo?: string; // 必须
  fmsAdvertisingMainThreeList: [
    title?: string, // 是否新纪录
    id?: string,
    content?: string, // 内容
    picture?: string, // 图片
    isNewRecord?: string, // 是否新纪录
  ];
}
//  产品内容新增或修改
export interface dvertisingMainSave {
  id?: string;
  isNewRecord?: string; // 是否新纪录
  subhead?: string; // 副标题
  title?: string; // 标题
  introducePicture?: string; //介绍图片
  introduceContent?: string; //介绍内容
  fmsAdvertisingProductPictureList: [
    picture?: string, // 图片
  ];
  fmsAdvertisingProductVideoList: [
    videoTitle?: string, // 图片
    video?: string,
  ];
}

//  专家新增
export interface advertExpertSave {
  fmsAdvertisingExpertList: [
    id?: string,
    isNewRecord?: string, // 是否新纪录
    picture?: string, // 图片
  ];
}
//  文件上传
export interface fileUpload {
  file?: string; //
}

//主页内容新增或修改
export const fmsAdvertMainSave = (params?: dvertisingMainSave | any) =>
  request.postJson<dvertisingMainSave>({ url: fmsPath + '/fms/fmsAdvertisingMain/save', params });

//主页内容查询
export const fmsAdvertMainList = (params?: advertExpertSave | any) =>
  request.postJson<advertExpertSave>({
    url: fmsPath + '/fms/fmsAdvertisingMain/listData ',
    params,
  });

//产品内容新增或修改
export const fmsAdvertProductSave = (params?: advertExpertSave | any) =>
  request.postJson<advertExpertSave>({ url: fmsPath + '/fms/fmsAdvertisingProduct/save', params });

//产品查询
export const fmsAdvertProductList = (params?: any) =>
  request.postJson({ url: fmsPath + '/fms/fmsAdvertisingProduct/listData', params });

//专家内容查询
export const fmsAdvertExpertList = (params?: any) =>
  request.postJson({ url: fmsPath + '/fms/fmsAdvertisingExpert/listData', params });

//专家内容新增或者修改
export const fmsAdvertExpertSave = (params?: advertExpertSave | any) =>
  request.postJson<advertExpertSave>({
    url: fmsPath + '/fms/fmsAdvertisingExpert/saveList',
    params,
  });

//文件上传
export const getFileAccessHttpUrl = (params?: any) =>
  request.postJson({ url: fmsPath + '/fms/fileUpload/single', params });

//获取摄像头下拉
export const fmsMonitorDeviceInfoList = (params?: any) =>
  request.postJson({ url: '/a' + '/sys/monitorDeviceInfo/listAll', params });

//获取产品内容详情
export const advertisingProduct = (params?: any) =>
  request.postJson({ url: '/f' + '/fms/fmsAdvertisingProduct/getById', params });

//获取产品内容删除
export const advertisingProductDelete = (params?: any) =>
  request.postJson({ url: '/f' + '/fms/fmsAdvertisingProduct/delete', params });
// /fms/fmsAdvertisingProduct/getById
// /fms/fmsAdvertisingProduct/delete

// <upload-image @getImageId="getDelImageId" @delimageId="delDelimageId" :multiple="99" :img="showContent" @draggable="handleDraggable"></upload-image>
// if (!res.result.content) {this.showContent = []}
// else {var ss = res.result.content.split(',')
// this.showContent = ss.map(item => ({ name: res.result.title, url: this.GLOBAL.imgUrl + item }))}
// //res.result.content 是我从接口获取的数据，把数据拼接一下，搞成我们需要的格式// 产品详情图片预览getDelImageId: function (val) {this.imageDelId.push(val)},// 删除产品详情封面图片delDelimageId: function (index) {this.imageDelId.forEach((val, key) => {if (index === key) {this.imageDelId.splice(key, 1)}})},   // 获取到重新排序后的图片handleDraggable (e) {const imgDrag = []for (var i = 0; i < e.length; i++) {var a = e[i].url.split('/')imgDrag.push(a[3])}this.imageDelId = imgDrag},
