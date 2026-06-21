<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="'查看考勤记录 - ' + (recordRef?.employeeName || '')"
    @ok="handleSubmit"
    :footer="null"
    :width="800"
  >
    <div style="padding: 0 20px">
      <BasicForm @register="registerForm" />
    </div>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { BasicModal, useModalInner } from '@/components/swm/Modal';
  import { BasicForm, useForm } from '@/components/swm/Form/index';
  import { defHttp } from '@/utils/http/axios';
  import { useMessage } from '@/hooks/swm/useMessage';
  import dayjs from 'dayjs';
  import { formSchema } from './attendance.data';

  // 考勤记录接口
  interface AttendanceRecord {
    id: string;
    employeeId: string;
    employeeName: string;
    attendanceDate: string;
    workTimeRange: string;
    clockInTime: string;
    clockOutTime: string;
    scheduledHours: number;
    actualHours: number;
    idleHours: number;
    effectiveWorkHours: number;
    dailyEfficiency: number;
    dailyAchievementRate: number;
    attendanceNormal: string;
    currentPosition: string;
    status: string;
    remarks: string;
    [key: string]: any;
  }

  export default defineComponent({
    name: 'EditModal',
    components: {
      BasicModal,
      BasicForm,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const recordRef = ref<Partial<AttendanceRecord>>({});
      const { createMessage } = useMessage();

      const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
        labelWidth: 120,
        baseColProps: { span: 12 },
        schemas: formSchema,
        showActionButtonGroup: false,
        actionColOptions: {
          span: 24,
        },
      });

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        resetFields();
        setModalProps({ confirmLoading: false });

        if (data?.record) {
          recordRef.value = data.record;

          // 获取详细信息
          await fetchEmployeeDetail(data.record.id);
        }
      });

      // 获取考勤详情
      const fetchEmployeeDetail = async (id: string) => {
        try {
          setModalProps({ confirmLoading: true });

          console.log('开始获取考勤详情, id:', id);
          const res = await defHttp.get({
            url: '/swm/swmDailyAttendance/get',
            params: { id },
          });

          console.log('获取考勤详情结果:', res);

          if (res) {
            // 更新recordRef
            recordRef.value = {
              ...recordRef.value,
              ...res,
            };

            // 处理日期和时间格式
            // 确保提取时间字符串，无论格式如何

            // 获取上班打卡时间
            let clockInTimeStr = null as string | null;
            if (res.clockInTime) {
              console.log('原始上班打卡时间:', res.clockInTime);

              // 尝试提取时间部分
              try {
                let timeStr;

                // 如果是时间字符串 (HH:MM:SS)
                if (res.clockInTime.includes(':') && !res.clockInTime.includes('-')) {
                  timeStr = res.clockInTime;
                }
                // 如果是日期时间格式 (YYYY-MM-DD HH:MM:SS)
                else if (res.clockInTime.includes(' ') && res.clockInTime.includes('-')) {
                  timeStr = res.clockInTime.split(' ')[1];
                }
                // 其他情况尝试用dayjs解析
                else {
                  timeStr = dayjs(res.clockInTime).format('HH:mm:ss');
                }

                // 使用dayjs解析时间字符串，并创建当天日期的时间对象
                // TimePicker需要dayjs对象而不是字符串
                clockInTimeStr = timeStr;
                console.log('处理后的上班打卡时间:', clockInTimeStr);
              } catch (e) {
                console.error('解析上班打卡时间失败:', e);
                clockInTimeStr = null;
              }
            }

            // 获取下班打卡时间
            let clockOutTimeStr = null as string | null;
            if (res.clockOutTime) {
              console.log('原始下班打卡时间:', res.clockOutTime);

              // 尝试提取时间部分
              try {
                let timeStr;

                // 如果是时间字符串 (HH:MM:SS)
                if (res.clockOutTime.includes(':') && !res.clockOutTime.includes('-')) {
                  timeStr = res.clockOutTime;
                }
                // 如果是日期时间格式 (YYYY-MM-DD HH:MM:SS)
                else if (res.clockOutTime.includes(' ') && res.clockOutTime.includes('-')) {
                  timeStr = res.clockOutTime.split(' ')[1];
                }
                // 其他情况尝试用dayjs解析
                else {
                  timeStr = dayjs(res.clockOutTime).format('HH:mm:ss');
                }

                // 使用dayjs解析时间字符串，并创建当天日期的时间对象
                clockOutTimeStr = timeStr;
                console.log('处理后的下班打卡时间:', clockOutTimeStr);
              } catch (e) {
                console.error('解析下班打卡时间失败:', e);
                clockOutTimeStr = null;
              }
            }

            // 设置表单数据
            const formData = {
              ...res,
              attendanceDate: res.attendanceDate
                ? dayjs(res.attendanceDate).format('YYYY-MM-DD')
                : null,
              // 时间拾取器需要dayjs对象，而不是字符串
              clockInTime: clockInTimeStr ? dayjs(`2000-01-01 ${clockInTimeStr}`) : null,
              clockOutTime: clockOutTimeStr ? dayjs(`2000-01-01 ${clockOutTimeStr}`) : null,
              // 确保数值字段为数值
              scheduledHours: Number(res.scheduledHours || 0),
              actualHours: Number(res.actualHours || 0),
              idleHours: Number(res.idleHours || 0),
              effectiveWorkHours: Number(res.effectiveWorkHours || 0),
              dailyEfficiency: Number(res.dailyEfficiency || 0),
              dailyAchievementRate: Number(res.dailyAchievementRate || 0),
            };

            console.log('设置表单数据:', formData);
            setFieldsValue(formData);
          }
        } catch (error) {
          console.error('获取考勤详情失败:', error);
          createMessage.error('获取考勤详情失败');
        } finally {
          setModalProps({ confirmLoading: false });
        }
      };

      async function handleSubmit() {
        try {
          const values = await validate();
          setModalProps({ confirmLoading: true });

          // 处理日期和时间
          const params = { ...values };

          // 添加记录ID
          params.id = recordRef.value.id;

          // 处理时间数据
          if (params.clockInTime) {
            // 如果是dayjs对象，则格式化为时间字符串
            if (typeof params.clockInTime === 'object' && params.clockInTime.format) {
              params.clockInTime = params.clockInTime.format('HH:mm:ss');
            }
            console.log('提交的上班打卡时间:', params.clockInTime);
          }

          if (params.clockOutTime) {
            // 如果是dayjs对象，则格式化为时间字符串
            if (typeof params.clockOutTime === 'object' && params.clockOutTime.format) {
              params.clockOutTime = params.clockOutTime.format('HH:mm:ss');
            }
            console.log('提交的下班打卡时间:', params.clockOutTime);
          }

          // 发送请求
          const res = await defHttp.post({
            url: '/swm/swmDailyAttendance/save',
            params,
          });

          if (res) {
            createMessage.success('保存成功！');
            closeModal();
            emit('success');
          }
        } catch (error) {
          console.error('保存失败:', error);
          createMessage.error('保存失败');
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      return {
        registerModal,
        registerForm,
        handleSubmit,
        recordRef,
      };
    },
  });
</script>

<style>
  /* 可以在这里添加自定义样式 */
</style>
