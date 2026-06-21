import request from '@/config/axios';
import { useGlobSetting } from '@/hooks/setting';

const { apiUrl } = useGlobSetting();
// 文件上传
export const uploadFileApi = (params, prefix: string) =>
  request.uploadFile({ url: apiUrl + `${prefix}/oss/fileUpload/single` }, params);

// dms文件上传
export const dmsUploadFileApi = (params) =>
  request.uploadFile({ url: '/js/dms/oss/fileUpload/single' }, params);

// 深化文件上传
export const bimUploadFileApi = (params: any) =>
  request.uploadFile({ url: '/js/b/oss/fileUpload/single' }, params);

// swm文件上传
export const swmUploadFileApi = (params) =>
  request.uploadFile({ url: '/js/swm/fileUpload/upload' }, params);
