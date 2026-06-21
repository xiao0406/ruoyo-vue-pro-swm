/**
 * 缓存相关API接口
 * @author Shawn
 * @date 2025/07/09
 */
import request from '@/config/axios';

/**
 * 刷新所有缓存
 */
export const refreshAllCache = () => request.post({ url: '/iot/api/cache/refresh-all' });
