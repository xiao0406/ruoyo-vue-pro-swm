/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author ThinkGem
 */
import request from '@/config/axios';
import { UserInfo } from '/#/store';
// ErrorMessageMode type
import { useGlobSetting } from '@/hooks/setting';
import { encryptByMd5, encryptByRSA } from '@/utils/cipher';
import { Menu } from '@/router/types';
import { useAppStore } from '@/store/modules/app';

const { adminPath } = useGlobSetting();

export interface LoginParams {
  username: string;
  password: string;
  validCode?: string;
  rememberMe?: boolean;
  captchaSerialNo?: string;
}

export interface LoginResult {
  result: string;
  message: string;
  sessionid: string;
  user: UserInfo;
  demoMode: boolean;
  useCorpModel: boolean;
  currentCorpCode: string;
  currentCorpName: string;
  sysCode: string;
  isValidCodeLogin: boolean;
}

export interface AuthInfo {
  stringPermissions: string[];
  roles: string[];
}

export const loginApi = (params: LoginParams, mode: ErrorMessageMode = 'none') => {
  params.username = encryptByRSA(params.username);
  params.password = encryptByRSA(encryptByMd5(params.password));
  if (params.validCode) {
    params.validCode = encryptByRSA(params.validCode.toLocaleLowerCase());
  }
  return request.post<LoginResult>(
    { url: adminPath + '/login', params, timeout: 20 * 1000 },
    { errorMessageMode: mode },
  );
};

export const getValidCode = () => {
  return request.get({ url: adminPath + '/validCode' });
};

export const switchSys = (sysCode: string) => {
  return request.get({ url: adminPath + '/switch/' + sysCode });
};

export const switchRole = (roleCode: string) => {
  return request.get({ url: adminPath + '/switchRole/' + roleCode });
};

export const switchSkin = (name = '') => {
  if (name == '') {
    const appStore = useAppStore();
    if (appStore.getDarkMode === 'dark') {
      name = 'skin-dark';
    } else {
      const themeColor = appStore.getProjectConfig.themeColor;
      name = themeColor == '#1890ff' ? 'skin-blue-light3' : 'skin-blue3';
    }
  }
  return request.get({ url: adminPath + '/switchSkin/' + name });
};

export const userInfoApi = (mode: ErrorMessageMode = 'message') =>
  request.get<LoginResult>(
    { url: adminPath + '/index', timeout: 10 * 1000 },
    { errorMessageMode: mode },
  );

export const authInfoApi = () => request.get<AuthInfo>({ url: adminPath + '/authInfo' });

export const menuRouteApi = (params = {}) =>
  request.get<Menu[]>({ url: adminPath + '/menuRoute', params });

export const logoutApi = () => request.get({ url: adminPath + '/logout' });
// 根据用户名获取系统列表
export const loginSysApi = (params = {}) =>
  request.get({ url: adminPath + '/sys/layout/treeDict', params });
// 查询系统名称
export const getSystemName = (params = {}) =>
  request.get({ url: adminPath + '/sys/layout/getSystemName', params });

//获取会话key
export const getWeChatQRKey = () => {
  return request.get({ url: adminPath + '/weChatQRKey' });
};
//二维码获取后获取登录状态接口
export const getIsLoginSusses = (params) => {
  return request.get({ url: adminPath + '/isLoginSusses', params });
};

export const weChenrLoginSussion = (params) => {
  return request.get({ url: adminPath + '/weChenrLoginSussion', params });
};

export const roleOfficeGetUserOffice = (params) => {
  return request.get({ url: adminPath + '/sys/roleOffice/getUserOffice', params });
};

// 用户是否需要修改密码
export const isNeedChangePwd = () => {
  return request.get({ url: adminPath + '/firstLogin' });
};

//获取成本系统地址
export const goCostSys = (params) => {
  return request.post({ url: adminPath + '/getIndex', params });
};

//认证
export const getAuth = (params) => {
  return request.post({ url: adminPath + '/loginJeesite', params });
};

//获取虚拟系统
export const getCloudIndex = (params) => {
  return request.get({ url: adminPath + '/getCloudIndex', params });
};

//获取虚拟系统
export const redirect = (params) => {
  return request.get({ url: adminPath + '/redirect', params });
};

// 获取智能建造
export const getIntelligentConstruction = (params) => {
  return request.get({ url: adminPath + '/loginPicmp', params });
};

//获取虚拟系统
export const loginSsoIms = (params) => {
  return request.get({ url: adminPath + '/loginSsoIms', params });
};
//获取钢材系统
export const loginOldSteel = (params) => {
  return request.get({ url: adminPath + '/loginOldSteel', params });
};

// 获取AI门户
export const loginAI = (params) => {
  return request.get({ url: adminPath + '/loginAI', params });
};
