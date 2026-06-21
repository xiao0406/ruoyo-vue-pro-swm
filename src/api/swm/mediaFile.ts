import request from '@/config/axios';

/**
 * 获取所有媒体文件
 */
export const listAllMediaFiles = () => {
  return request.get({ url: '/swm/swmMediaFile/listAll' });
};
