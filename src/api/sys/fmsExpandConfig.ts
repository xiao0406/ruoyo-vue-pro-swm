import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel } from '../model/baseModel';

const { fmsPath, adminPath } = useGlobSetting();

export interface FmsExpandConfig extends BasicModel<FmsExpandConfig> {
  tableKey?: string; // 表名
  fieldKey?: string; // 字段名
  fieldName?: string; // 字段描述
  isRequired?: number; // 是否必填
  isShowPc?: number; // 是否显示
  isShowMobile?: number; // 移动端
  projectId?: number; // 项目id
}

export const fmsExpandConfigListData = (params?: FmsExpandConfig | any) =>
  request.get<FmsExpandConfig>({ url: fmsPath + '/nc/fmsExpandData/selectFieldName', params });

// 分页信息
export const fmsExpandConfigListDataparm = (params?: FmsExpandConfig | any) =>
  request.get<FmsExpandConfig>({ url: adminPath + '/sys/fmsExpandData/selectByTableKey', params });

// export const fmsExpandConfigListBinding = (params?: FmsExpandConfig | any) =>
//   request.post<FmsExpandConfig>({ url: fmsPath + '/nc/fmsExpandData/binding', params });

// 保存关联信息
export const fmsExpandConfigListBinding = (params?: any, data?: FmsExpandConfig | any) =>
  request.postJson<FmsExpandConfig>({
    url: adminPath + '/sys/fmsExpandData/binding',
    params,
    data,
  });

// 搜索框 所有表名字段
export const fmsExpandConfigListTableKey = (params?: any) =>
  request.get<FmsExpandConfig>({ url: adminPath + '/sys/fmsExpandData/tableKeyList', params });
// 搜索框 已经关联表名字段
export const fmsExpandConfigAssociationList = (params?: any) =>
  request.get<FmsExpandConfig>({ url: adminPath + '/sys/fmsExpandData/associationList', params });

// export const fmsExpandConfigEnable = (params?: FmsExpandConfig | any) =>
//   request.get<FmsExpandConfig>({ url: adminPath + '/fms/fmsExpandConfig/enable', params });
// request.get({ url: fmsPath + '/nc/fmsExpandData/tableKeyList', params });

// export const fmsExpandConfigAssociationList = (params?: any) =>
//   request.get({ url: fmsPath + '/nc/fmsExpandData/associationList', params });

// 获取字典
export const fmsExpandConfigListDict = (params?: Recordable<any>) =>
  request.get({ url: adminPath + '/sys/fmsExpandData/findDictType', params });
