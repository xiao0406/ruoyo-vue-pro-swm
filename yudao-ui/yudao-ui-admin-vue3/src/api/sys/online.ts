/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author ThinkGem
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import type { Result } from '/#/axios';

const { adminPath } = useGlobSetting();

export interface Online {
  id?: string;
  startTimestamp?: string;
  lastAccessTime?: string;
  timeout?: string;
  userCode?: string;
  userName?: string;
  userType?: string;
  deviceType?: string;
  host?: string;
}

export const onlineList = (params?: Online | any) =>
  request.get<Online>({ url: adminPath + '/sys/online/list', params });

export const onlineListData = (params?: Online | any) =>
  request.post<Online[]>({ url: adminPath + '/sys/online/listData', params });

export const onlineTickOut = (params?: Online | any) =>
  request.post<Result>({ url: adminPath + '/sys/online/tickOut', params });

export const onlineCount = () => request.post<Number>({ url: adminPath + '/sys/online/count' });
