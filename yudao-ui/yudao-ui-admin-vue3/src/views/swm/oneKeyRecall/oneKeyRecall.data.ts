// 一键召回模块数据定义
import { ref } from 'vue';
import { defHttp } from '@/utils/http/axios';

// 自定义模板代码
export const CUSTOM_TEMPLATE_CODE = 'CUSTOM';

// 撤离方案枚举
export enum EvacuationPlanEnum {
  /** 全体撤离 */
  ALL = '1',
  /** 按车间撤离 */
  BY_WORKSHOP = '2',
  /** 按班组撤离 */
  BY_GROUP = '3',
  /** 按区域撤离 */
  BY_AREA = '4',
  /** 按人员撤离 */
  BY_PERSONNEL = '5',
  /** 按人员类型撤离 */
  BY_PERSONNEL_TYPE = '6',
}

// 获取撤离方案文本
export function getEvacuationPlanText(value: string): string {
  switch (value) {
    case EvacuationPlanEnum.ALL:
      return '全体撤离';
    case EvacuationPlanEnum.BY_WORKSHOP:
      return '按车间撤离';
    case EvacuationPlanEnum.BY_GROUP:
      return '按班组撤离';
    case EvacuationPlanEnum.BY_AREA:
      return '按区域撤离';
    case EvacuationPlanEnum.BY_PERSONNEL:
      return '按人员撤离';
    case EvacuationPlanEnum.BY_PERSONNEL_TYPE:
      return '按人员类型撤离';
    default:
      return '';
  }
}

// 获取撤离方案列表
export function getEvacuationPlanList() {
  return [
    { name: '全体撤离', value: EvacuationPlanEnum.ALL },
    { name: '按车间撤离', value: EvacuationPlanEnum.BY_WORKSHOP },
    { name: '按产线撤离', value: EvacuationPlanEnum.BY_AREA },
    { name: '按班组撤离', value: EvacuationPlanEnum.BY_GROUP },
    { name: '按人员撤离', value: EvacuationPlanEnum.BY_PERSONNEL },
    { name: '按人员类型撤离', value: EvacuationPlanEnum.BY_PERSONNEL_TYPE },
  ];
}

// 模拟树形结构数据，按照公司->车间->工序->班组->人员组织
export const treeData = [
  {
    id: 'company-1',
    name: '中铁一局',
    children: [
      {
        id: 'department-1-1',
        name: '隧道工区',
        children: [
          {
            id: 'process-1-1-1',
            name: '掘进',
            children: [
              {
                id: 'team-1-1-1-1',
                name: '甲班',
                children: [
                  {
                    id: 'person-1-1-1-1-1',
                    name: '张伟',
                    job_type: '掘进工',
                  },
                  {
                    id: 'person-1-1-1-1-2',
                    name: '李强',
                    job_type: '掘进工',
                  },
                  {
                    id: 'person-1-1-1-1-3',
                    name: '王刚',
                    job_type: '掘进工',
                  },
                ],
              },
              {
                id: 'team-1-1-1-2',
                name: '乙班',
                children: [
                  {
                    id: 'person-1-1-1-2-1',
                    name: '赵勇',
                    job_type: '掘进工',
                  },
                  {
                    id: 'person-1-1-1-2-2',
                    name: '孙明',
                    job_type: '掘进工',
                  },
                ],
              },
            ],
          },
          {
            id: 'process-1-1-2',
            name: '支护',
            children: [
              {
                id: 'team-1-1-2-1',
                name: '甲班',
                children: [
                  {
                    id: 'person-1-1-2-1-1',
                    name: '周健',
                    job_type: '支护工',
                  },
                  {
                    id: 'person-1-1-2-1-2',
                    name: '吴刚',
                    job_type: '支护工',
                  },
                ],
              },
              {
                id: 'team-1-1-2-2',
                name: '乙班',
                children: [
                  {
                    id: 'person-1-1-2-2-1',
                    name: '郑华',
                    job_type: '支护工',
                  },
                ],
              },
            ],
          },
        ],
      },
      {
        id: 'department-1-2',
        name: '桥梁工区',
        children: [
          {
            id: 'process-1-2-1',
            name: '钢结构',
            children: [
              {
                id: 'team-1-2-1-1',
                name: '甲班',
                children: [
                  {
                    id: 'person-1-2-1-1-1',
                    name: '马超',
                    job_type: '钢结构工',
                  },
                  {
                    id: 'person-1-2-1-1-2',
                    name: '杨涛',
                    job_type: '钢结构工',
                  },
                ],
              },
              {
                id: 'team-1-2-1-2',
                name: '乙班',
                children: [
                  {
                    id: 'person-1-2-1-2-1',
                    name: '林强',
                    job_type: '钢结构工',
                  },
                ],
              },
            ],
          },
          {
            id: 'process-1-2-2',
            name: '混凝土',
            children: [
              {
                id: 'team-1-2-2-1',
                name: '甲班',
                children: [
                  {
                    id: 'person-1-2-2-1-1',
                    name: '陈勇',
                    job_type: '混凝土工',
                  },
                  {
                    id: 'person-1-2-2-1-2',
                    name: '黄刚',
                    job_type: '混凝土工',
                  },
                ],
              },
              {
                id: 'team-1-2-2-2',
                name: '乙班',
                children: [
                  {
                    id: 'person-1-2-2-2-1',
                    name: '刘伟',
                    job_type: '混凝土工',
                  },
                  {
                    id: 'person-1-2-2-2-2',
                    name: '徐明',
                    job_type: '混凝土工',
                  },
                ],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    id: 'company-2',
    name: '中铁二局',
    children: [
      {
        id: 'department-2-1',
        name: '路基工区',
        children: [
          {
            id: 'process-2-1-1',
            name: '土石方',
            children: [
              {
                id: 'team-2-1-1-1',
                name: '甲班',
                children: [
                  {
                    id: 'person-2-1-1-1-1',
                    name: '张建国',
                    job_type: '土石方工',
                  },
                  {
                    id: 'person-2-1-1-1-2',
                    name: '王志强',
                    job_type: '土石方工',
                  },
                ],
              },
              {
                id: 'team-2-1-1-2',
                name: '乙班',
                children: [
                  {
                    id: 'person-2-1-1-2-1',
                    name: '李翔',
                    job_type: '土石方工',
                  },
                  {
                    id: 'person-2-1-1-2-2',
                    name: '王明',
                    job_type: '土石方工',
                  },
                ],
              },
            ],
          },
        ],
      },
    ],
  },
];

// 默认的自定义音频内容
export const DEFAULT_CUSTOM_CONTENT = '';

// 一键召回API函数
export async function sendOneKeyRecall(data: {
  templateName: string;
  templateContent: string;
  evacuationPlan: string;
  evacueeList: string;
  recallResult: string;
}) {
  return defHttp.postJson({
    url: '/swm/swmOneClickRecall/save',
    // url: 'http://10.50.135.20:8980/js/swm/swmOneClickRecall/save',
    data,
  });
}

// IOT模块全体广播API函数
export async function sendBroadcastToAll(templateContent: string, voiceText?: string) {
  return defHttp.post({
    url: '/iot/oneKeyRecall/broadcast/all',
    params: {
      templateContent: templateContent,
      voiceText: voiceText,
    },
  });
}

// IOT模块选中目标召回API函数
export async function sendBroadcastToSelectedTargets(data: {
  selectedTargets: string[];
  templateContent: string;
  voiceText?: string;
  originalTreeData: any[];
}) {
  return defHttp.post({
    url: '/iot/oneKeyRecall/broadcast/selected/post',
    params: {
      selectedTargets: JSON.stringify(data.selectedTargets),
      templateContent: data.templateContent,
      voiceText: data.voiceText,
      originalTreeData: JSON.stringify(data.originalTreeData),
    },
  });
}
const oneKkeyText = '请立刻撤回安全区域，请立刻撤回安全区域，请立刻撤回安全区域';
export const tempVoiceOptions = [
  { label: '应急召回', value: '应急召回', voiceText: oneKkeyText },
  { label: '其他', value: '其他', voiceText: '' },
];
