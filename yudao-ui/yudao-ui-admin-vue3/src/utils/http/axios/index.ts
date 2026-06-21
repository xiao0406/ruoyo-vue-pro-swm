/**
 * Axios adapter for JeeSite SWM pages.
 * Bridges JeeSite's `defHttp` API to ruoyi-vue-pro's `request` module.
 */
import request from '@/config/axios';

// Re-export as defHttp for backward compatibility
export const defHttp = request;
export default request;

// Common types
export type ErrorMessageMode = 'none' | 'modal' | 'message' | undefined;
