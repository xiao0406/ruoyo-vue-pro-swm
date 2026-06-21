/**
 * @author zwf
 * @date 2025-05-15
 */
import request from '@/config/axios';
import { Page } from '@/api/model/baseModel';

/**
 * 安全教育信息接口
 */
export interface SafetyEducation {
  id: string; // 主键ID
  theme?: string; // 主题
  contentDescription?: string; // 内容描述
  safetyEducationType?: string; // 安全教育类型
  startTime?: string; // 开始时间
  participants?: string; // 参与对象
  status?: string; // 状态
  createTime?: string; // 创建时间
  updateTime?: string; // 更新时间
  participationType?: string; // 参与类型
  remarks?: string; // 备注
  attachmentUrl?: string; // 附件URL

  // 基础模型字段
  isNewRecord?: boolean;
  page?: any;
  dataMap?: any;
  sessionid?: string;
  permi?: string;
  userType?: string;
}

/**
 * 安全教育列表
 */
export function safetyEducationList(params?: any) {
  return request.get<any>({
    url: '/swm/safetyEducation/list',
    params,
  });
}

/**
 * 安全教育列表数据（分页）
 */
export function safetyEducationListData(params?: any) {
  return request.get<Page<SafetyEducation>>({
    url: '/swm/safetyEducation/listData',
    params,
  });
}

/**
 * 获取安全教育表单数据
 */
export function safetyEducationForm(params?: any) {
  return request.get<any>({
    url: '/swm/safetyEducation/form',
    params,
  });
}

/**
 * 保存安全教育数据
 */
export function safetyEducationSave(params?: any, data?: any) {
  return request.postJson<any>({
    url: '/swm/safetyEducation/save',
    params,
    data,
  });
}

/**
 * 删除安全教育数据
 */
export function safetyEducationDelete(params?: any) {
  return request.delete<any>({
    url: '/swm/safetyEducation/delete',
    params,
  });
}

/**
 * 完成安全教育
 */
export function safetyEducationComplete(params?: any) {
  return request.post<any>({
    url: '/swm/safetyEducation/complete',
    params,
  });
}

/**
 * 上传安全教育附件
 */
export function safetyEducationUpload(formData: FormData) {
  return request.post<any>(
    {
      url: '/swm/safetyEducation/upload',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    },
    {
      isTransformResponse: false,
      joinTime: false, // 不添加时间戳
      joinParamsToUrl: false, // 不将参数拼接到URL
    },
  );
}

/**
 * 下载安全教育附件
 */
export function safetyEducationDownload(params?: any) {
  return request.get(
    {
      url: '/swm/safetyEducation/download',
      params,
      responseType: 'blob',
    },
    { isReturnNativeResponse: true },
  );
}

/**
 * 获取安全教育附件列表
 */
export function safetyEducationFileList(params?: any) {
  return request.get<any>({
    url: '/swm/safetyEducation/fileList',
    params,
  });
}

/**
 * 获取枚举选项
 */
export function getEnumOptions() {
  return request.get({
    url: '/swm/safetyEducation/enumOptions',
  });
}
