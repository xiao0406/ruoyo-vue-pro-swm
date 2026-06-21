/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 * @author wxy
 */
import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { adminPath, mdmPath } = useGlobSetting();

export interface YcPuPulanB extends BasicModel<YcPuPulanB> {
  pkPuplanB?: string; // 需用计划表体主键
  vrowno?: string; // 行号
  cmaterialid?: string; // 物料最新版本
  cmaterialvidName?: string; // 物料名称
  cmaterialvid?: string; // 物料
  nnum?: number; // 主数量
  nastnum?: number; // 数量
  cunitid?: string; // 主单位
  castunitid?: string; // 单位
  vchangerate?: string; // 换算率
  nysprice?: number; // 预算单价
  nyszprice?: number; // 预算金额
  djcdate?: string; // 进场时间
  vzlyq?: string; // 质量要求
  vaqhbyq?: string; // 安全/环保要求
  ncz?: string; // 材质
  nhd?: number; // 厚度
  nkd?: number; // 宽度
  ncd?: number; // 长度
  njs?: number; // 件数
  nzl?: number; // 重量（吨）
  vjhzt?: string; // 交货状态
  vgcbw?: string; // 分部工程
  vppcj?: string; // 品牌/厂家
  vghxx?: string; // 收货信息
  vjsyq?: string; // 技术要求
  vmemo?: string; // 备注
  vbdef1?: string; // 探伤要求
  vbdef2?: string; // 自定义项2
  vbdef3?: string; // 采购批次江苏必填其他厂忽略
  vbdef4?: string; // 自定义项4
  vbdef5?: string; // 自定义项5
  vbdef6?: string; // 自定义项6
  vbdef7?: string; // 自定义项7
  vbdef8?: string; // 自定义项8
  vbdef9?: string; // 自定义项9
  vbdef10?: string; // 自定义项10
  vbdef11?: string; // 自定义项11
  vbdef12?: string; // 自定义项12
  vbdef13?: string; // 自定义项13
  vbdef14?: string; // 自定义项14
  vbdef15?: string; // 自定义项15
  vbdef16?: string; // 自定义项16
  vbdef17?: string; // 累计出库申请数量
  vbdef18?: string; // 累计出库申请主数量
  vbdef19?: string; // 自定义项19
  vbdef20?: string; // 自定义项20
  pkPupart?: string; // 框架协议
  nprice?: number; // 单价
  vzjmemo?: string; // 采购用途
  noutnum?: number; // 累计出库数量
  noutzhunum?: number; // 累计出库主数量
  isshut?: string; // 是否关闭
  cmanufactur?: string; // 生产厂商
  creceunit?: string; // 收货单位
  vfree1?: string; // 自由辅助属性1
  vfree2?: string; // 自由辅助属性2
  vfree3?: string; // 自由辅助属性3
  vfree4?: string; // 自由辅助属性4
  vfree5?: string; // 自由辅助属性5
  vfree6?: string; // 自由辅助属性6
  vfree7?: string; // 自由辅助属性7
  vfree8?: string; // 自由辅助属性8
  vfree9?: string; // 自由辅助属性9
  vfree10?: string; // 自由辅助属性10
  nprepnum?: number; // 累计采购主数量
  nprepnumber?: number; // 累计采购数量
  nqfqd?: number; // 屈服强度
  ndxhl?: number; // 镀锌含量
  vhwlx?: string; // 货物类型
  vtsdj?: string; // 探伤
  vys?: string; // 颜色
  ishq?: string; // 是否含铅
  isgdcc?: string; // 钢锭成材
  vysfs?: string; // 运输方式
  vzffs?: string; // 支付方式
  nysdj?: number; // 运输单价
  vyssm?: string; // 运输说明
  nqtjj?: number; // 其他加价
  vqtsm?: string; // 其他说明
  issdc?: string; // 是否双定尺
  iscddc?: string; // 是否长度定尺
  iskddc?: string; // 是否宽度定尺
  iscddfw?: string; // 是否长度定范围
  iskddfw?: string; // 是否宽度定范围
  njjtz?: number; // 基价调整
  vbdef21?: string; // 自定义项21
  vbdef22?: string; // 自定义项22
  vbdef23?: string; // 自定义项23
  vbdef24?: string; // 自定义项24
  vbdef25?: string; // 自定义项25
  vbdef26?: string; // 自定义项26
  vbdef27?: string; // 自定义项27
  vbdef28?: string; // 自定义项28
  vbdef29?: string; // 自定义项29
  vbdef30?: string; // 自定义项30
  vbdef40?: string; // 自定义项40
  vbdef39?: string; // 自定义项39
  vbdef38?: string; // 自定义项38
  vbdef37?: string; // 自定义项37
  vbdef36?: string; // 自定义项36
  vbdef35?: string; // 自定义项35
  vbdef34?: string; // 自定义项34
  vbdef33?: string; // 自定义项33
  vbdef32?: string; // 自定义项32
  vbdef31?: string; // 自定义项31
  vbdef50?: string; // 自定义项50
  vbdef49?: string; // 自定义项49
  vbdef48?: string; // 自定义项48
  vbdef47?: string; // 自定义项47
  vbdef46?: string; // 自定义项46
  vbdef45?: string; // 自定义项45
  vbdef44?: string; // 自定义项44
  vbdef43?: string; // 自定义项43
  vbdef42?: string; // 自定义项42
  vbdef41?: string; // 自定义项41
  pkPuplan?: string; // 需用计划主实体_主键
  cmaterialvidSpecifications?: string; // 规格
  remark?: string; // 备注
}

// export const ycPuPulanBList = (params?: YcPuPulanB | any) =>
//   request.get<YcPuPulanB>({ url: mdmPath + '/ycPuPulanB/list', params });

// export const ycPuPulanBListData = (params?: YcPuPulanB | any) =>
//   request.post<Page<YcPuPulanB>>({ url: mdmPath + /ycPuPulanB/pageList', params });

// export const ycPuPulanBForm = (params?: YcPuPulanB | any) =>
//   request.get<YcPuPulanB>({ url: mdmPath + '/ycPuPulanB/form', params });

// export const ycPuPulanBSave = (params?: any, data?: YcPuPulanB | any) =>
//   request.postJson<YcPuPulanB>({ url: mdmPath + '/ycPuPulanB/save', params, data });

// export const ycPuPulanBDelete = (params?: YcPuPulanB | any) =>
//   request.get<YcPuPulanB>({ url: mdmPath + '/ycPuPulanB/delete', params });

// export const ycPuPulanList = (params?: YcPuPulan | any) =>
//   request.get<YcPuPulan>({ url: mdmPath + '/ycPuPulan/list', params });

export const ycPuPulanListData = (params?: YcPuPulan | any) =>
  request.post<Page<YcPuPulan>>({ url: mdmPath + '/mdm/ycPuPulan/pageList', params });

// export const ycPuPulanForm = (params?: YcPuPulan | any) =>
//   request.get<YcPuPulan>({ url: mdmPath + '/ycPuPulan/form', params });

// export const ycPuPulanSave = (params?: any, data?: YcPuPulan | any) =>
//   request.postJson<YcPuPulan>({ url: mdmPath + '/ycPuPulan/save', params, data });

// export const ycPuPulanDelete = (params?: YcPuPulan | any) =>
//   request.get<YcPuPulan>({ url: mdmPath + '/ycPuPulan/delete', params });
