/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author 冼国文
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';

export interface ButtonInfo {
  href?: string;
  name?: string;
}
export interface ContentEntity {
  buttons: ButtonInfo[];
  content?: string;
  msgType?: string;
  title?: string;
}

export interface Notify {
  id?: string;
  bizKey?: string;
  bizType?: string;
  isMergePush?: string;
  msgContent?: string;
  msgContentEntity?: ContentEntity;
  msgTitle?: string;
  msgType?: string;
  planPushDate?: string;
  pushDate?: string;
  pushNumber?: number;
  pushReturnContent?: string;
  pushStatus?: string;
  readStatus?: string;
  receiveCode?: string;
  receiveUserCode?: string;
  receiveUserName?: string;
  sendDate?: string;
  sendUserCode?: string;
  sendUserName?: string;
}

const { adminPath } = useGlobSetting();

// 消息列表
export const unreadMsg = () =>
  request.get<Notify[]>({ url: adminPath + '/msg/msgInfo/unreadMsg', params: { msgType: 'pc' } });

// 拉取消息
export const pullPoolMsg = () =>
  request.get<Notify[]>({ url: adminPath + '/msg/msgInfo/pullPoolMsg' });

// 标记已读
export const readMsg = (params: Notify) =>
  request.get<any>({ url: adminPath + '/msg/msgInfo/readMsg', params });
