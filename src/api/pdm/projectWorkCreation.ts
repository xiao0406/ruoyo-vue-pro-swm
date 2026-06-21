import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel, Page } from '../model/baseModel';

const { mdmPath, apiUrl } = useGlobSetting();

export interface ProjectWorkCreation extends BasicModel<ProjectWorkCreation> {
  creationCategory?: string; // 创效类别
  projectId?: string; // 项目id
  frontAsk?: string; // 优化前要求
  afterPractice?: string; // 优化后做法
  money?: number; // 预计创效额(元)
  contactPerson?: string; // 对接方
  fileId?: string; // 附件id
  proposer?: string; // 提出人
  remark?: string; // 备注
  fileUrl?: string; // 附件url
  projectDate?: string; // 日期时间
}
// 分页
export const projectWorkCreationListData = (params?: ProjectWorkCreation | any) =>
  request.post<Page<ProjectWorkCreation>>({
    url: mdmPath + '/mdm/project/projectWorkCreation/pageList',
    params,
  });
// 文件上传
export const uploadFile = (params: any) =>
  request.uploadFile({ url: apiUrl + '/m/oss/fileUpload/single' }, params);
//保存
export const projectWorkCreationSave = (params?: any, data?: ProjectWorkCreation | any) =>
  request.postJson<ProjectWorkCreation>({
    url: mdmPath + '/mdm/project/projectWorkCreation/save',
    params,
    data,
  });
//删除
export const projectWorkCreationDelete = (params?: ProjectWorkCreation | any) =>
  request.get<ProjectWorkCreation>({
    url: mdmPath + '/mdm/project//projectWorkCreation/delete',
    params,
  });
//导出
export const workCreationExcelExport = (params?: ProjectWorkCreation | any) =>
  request.post<ProjectWorkCreation>({
    url: mdmPath + '/mdm/project/projectWorkCreation/WorkCreationExcelExport',
    params,
  });
