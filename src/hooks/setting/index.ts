/**
 * Global settings hook - provides upload URL and other config values.
 * Simplified for SWM integration.
 */
export function useGlobSetting() {
  return {
    uploadUrl: '/admin-api/infra/file/upload',
    apiUrl: '/admin-api',
    adminPath: '/admin-api',
    fmsPath: '/admin-api',
    siteName: 'SWM',
  };
}
