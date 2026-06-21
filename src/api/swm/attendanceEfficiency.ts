/**
 * 考勤工效相关API
 * @author 系统生成
 */
import request from '@/config/axios';
import dayjs from 'dayjs';

/**
 * 获取个人工效数据
 * @param userId 用户ID
 * @param month 月份，格式YYYY-MM
 * @returns 返回个人工效数据
 */
export async function getPersonalEfficiency(userId: string, month?: string) {
  // 默认使用当前月份
  const queryMonth = month || dayjs().format('YYYY-MM');

  try {
    const res = await request.get({
      url: '/swm/swmAttendanceSummary/listData',
      params: {
        userId: userId,
        month: queryMonth,
      },
    });

    if (res && res.list && res.list.length > 0) {
      // 查找匹配的用户记录
      const userRecord = res.list.find((item) => item.userId === userId);

      if (userRecord && userRecord.efficiency !== undefined) {
        return {
          success: true,
          efficiency: Number(userRecord.efficiency),
          record: userRecord,
        };
      }
    }

    return {
      success: false,
      efficiency: 0,
      message: '未找到数据',
    };
  } catch (error) {
    console.error('获取个人工效数据失败:', error);
    return {
      success: false,
      efficiency: 0,
      message: '请求失败',
    };
  }
}
