import request from '@/config/axios';

// 定义上传结果类型
interface UploadApiResult {
  result: string;
  message: string;
  fileId: string;
  fileName: string;
  url: string;
  businessType?: string;
  recordId?: string;
}

/**
 * 上传文件
 * @param formData 表单数据，包含file, recordId, businessType
 */
export function uploadFile(formData: FormData) {
  return request.post<UploadApiResult>(
    {
      url: '/swm/fileUpload/upload',
      timeout: 60 * 1000, // 60秒超时
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
 * 获取文件列表
 * @param params 查询参数，包含recordId和可选的businessType
 */
export function getFileList(params: { recordId: string; businessType?: string }) {
  return request.get<any[]>({
    url: '/swm/fileUpload/list',
    params,
  });
}

/**
 * 下载文件
 * @param params 下载参数
 */
export function downloadFile(params: { fileId: string; fileName?: string; objectName: string }) {
  return request.get(
    {
      url: '/swm/fileUpload/download',
      params,
      responseType: 'blob',
    },
    {
      isTransformResponse: false,
    },
  );
}

/**
 * 删除文件
 * @param params 删除参数
 */
export function deleteFile(params: { fileId: string; objectName: string }) {
  return request.post({
    url: '/swm/fileUpload/delete',
    params,
  });
}
