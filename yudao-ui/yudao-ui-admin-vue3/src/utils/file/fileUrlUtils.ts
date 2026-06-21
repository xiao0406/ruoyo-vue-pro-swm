/**
 * 文件URL处理工具函数
 * @author Shawn
 * @date 2025-01-14
 */

/**
 * 处理文件预览URL，为相对路径添加正确的前缀
 * @param url 原始URL
 * @param modulePrefix 模块前缀，默认为 'swm'
 * @returns 处理后的完整URL
 */
export function processFileUrl(url: string, modulePrefix = 'swm'): string {
  if (!url) return '';

  // 如果已经是完整的URL（包含协议），直接返回
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) {
    return url;
  }

  // 如果是以 fileUpload/ 开头的相对路径，添加前缀
  if (url.startsWith('fileUpload/')) {
    return `/js/${modulePrefix}/${url}`;
  }

  // 其他情况直接返回原URL
  return url;
}
