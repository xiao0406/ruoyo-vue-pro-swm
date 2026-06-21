import request from '@/config/axios';
import { UploadFileParams } from '/#/axios';
import { useGlobSetting } from '@/hooks/setting';
import { BasicModel } from '../model/baseModel';

const { apiUrl, adminPath, uploadUrl, fmsPath } = useGlobSetting();

export interface UploadApiResult {
  code: string;
  // url: string;
  data: FileUpload;
  result: string;
  message: string;
  fileEntityId: string;
  fileUploadId: string;
  fileUpload: FileUpload;
}
export interface FileUpload extends BasicModel<FileUpload> {
  fileEntity: FileEntity;
  fileName: string;
  fileType: string;
  fileSort: number;
  bizKey: string;
  bizType: string;
  bizKeyIsLike: string;
  fileUrl?: string;
}
export interface FileEntity extends BasicModel<FileEntity> {
  fileId: string;
  fileMd5: string;
  filePath: string;
  fileContentType: string;
  fileExtension: string;
  suffix: string;
  fileSize: number;
  fileMeta: string;
  fileMetaMap: any;
  filePreview: string;
}

/**
 * @description: Upload interface
 */
export function uploadFile(
  params: UploadFileParams,
  onUploadProgress: (progressEvent: ProgressEvent) => void,
) {
  if (params.file != undefined) {
    return request.uploadFile<UploadApiResult>(
      {
        url: apiUrl + adminPath + '/file/' + uploadUrl,
        onUploadProgress,
      },
      params,
    );
  } else {
    return request.post(
      { url: adminPath + '/file/' + uploadUrl, params },
      { errorMessageMode: 'none' },
    );
  }
}
/**
 * @description: Upload interface
 */
export function uploadFileSingle(
  params: UploadFileParams,
  onUploadProgress: (progressEvent: ProgressEvent) => void,
) {
  if (params.file != undefined) {
    // url: apiUrl + fmsPath + '/oss/fileUpload/single',
    return request.uploadFile<UploadApiResult>(
      {
        url: apiUrl + '/dms/oss/fileUpload/single',
        onUploadProgress,
      },
      params,
    );
  } else {
    return request.post(
      { url: fmsPath + '/oss/fileUpload/single', params },
      { errorMessageMode: 'none' },
    );
  }
}

export const uploadFileList = (params?: FileUpload | any) =>
  request.get({ url: adminPath + '/file/fileList', params }, { errorMessageMode: 'none' });
