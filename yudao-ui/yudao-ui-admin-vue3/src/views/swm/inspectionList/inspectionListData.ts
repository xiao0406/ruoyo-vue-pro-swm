/**
 * 巡检列表数据定义
 *
 * @author Shawn
 * @date 2025-05-22
 */
import { FormSchema } from '@/components/swm/Form';
import { BasicColumn } from '@/components/swm/Table';
import { formatToDate } from '@/utils/dateUtil';
import { getAllPersons } from '@/api/swm/person';
import { h } from 'vue';
import { createImgPreview } from '@/components/swm/Preview';
import { Popover } from 'ant-design-vue';
import { useDict } from '@/components/swm/Dict';

/**
 * 表格列定义
 */
export const columns: BasicColumn[] = [
  {
    title: '巡检计划编号',
    dataIndex: 'planCode',
    width: 150,
  },
  {
    title: '巡检计划名称',
    dataIndex: 'planName',
    width: 180,
  },
  {
    title: '巡检类型',
    dataIndex: 'inspectionType',
    width: 120,
    dictType: 'inspection_type',
  },
  {
    title: '巡检人',
    dataIndex: 'inspector',
    width: 120,
  },
  {
    title: '开始时间',
    dataIndex: 'startTime',
    width: 150,
    customRender: ({ text }) => {
      return text ? formatToDate(text, 'YYYY-MM-DD HH:mm:ss') : '';
    },
  },
  {
    title: '结束时间',
    dataIndex: 'endTime',
    width: 150,
    customRender: ({ text }) => {
      return text ? formatToDate(text, 'YYYY-MM-DD HH:mm:ss') : '';
    },
  },
  {
    title: '状态',
    dataIndex: 'inspectionListStatus',
    width: 100,
    dictType: 'inspection_list_status_enum',
  },
  {
    title: '附件',
    dataIndex: 'attachmentPath',
    width: 150,
    customRender: ({ text }) => {
      return renderAttachmentPopover(text);
    },
  },
  {
    title: '备注',
    dataIndex: 'remarks',
    width: 200,
  },
];

/**
 * 搜索表单
 */
export const searchFormSchema: FormSchema[] = [
  {
    field: 'planCode',
    label: '巡检计划编号',
    component: 'Input',
    componentProps: {
      placeholder: '请输入巡检计划编号',
      allowClear: true,
    },
    colProps: { span: 12 },
  },
  {
    field: 'inspector',
    label: '巡检人',
    component: 'Select',
    componentProps: {
      api: async () => {
        const res = await getAllPersons();
        return (
          res.data?.map((item) => ({
            label: item.name,
            value: item.name,
          })) || []
        );
      },
      showSearch: true,
      filterOption: (input, option) => {
        return option.label?.toLowerCase().indexOf(input.toLowerCase()) >= 0;
      },
      placeholder: '请选择巡检人',
      allowClear: true,
    },
    colProps: { span: 12 },
  },
];

/**
 * 表单数据
 */
export const formSchema: FormSchema[] = [
  {
    field: 'id',
    label: 'ID',
    component: 'Input',
    show: false,
  },
  {
    field: 'planName',
    label: '计划名称',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: '请输入计划名称' }],
  },
  {
    field: 'inspectionType',
    label: '巡检类型',
    component: 'Select',
    componentProps: {
      dictType: 'inspection_type',
      allowClear: true,
    },
    required: true,
    rules: [{ required: true, message: '请选择巡检类型' }],
  },
  {
    field: 'inspectorId',
    label: '巡检人',
    component: 'Select',
    required: true,
    componentProps: {
      api: async () => {
        const res = await getAllPersons();
        return (
          res.data?.map((item) => ({
            label: item.name,
            value: item.id,
          })) || []
        );
      },
      showSearch: true,
      filterOption: (input, option) => {
        return option.label?.toLowerCase().indexOf(input.toLowerCase()) >= 0;
      },
      placeholder: '请选择巡检人',
    },
    rules: [{ required: true, message: '请选择巡检人' }],
  },
  {
    field: 'inspector',
    label: '巡检人',
    component: 'Input',
    componentProps: {
      readonly: true,
    },
    show: false, // 默认隐藏，在查看模式下通过 updateSchema 显示
  },
  {
    field: 'startTime',
    label: '开始时间',
    component: 'DatePicker',
    componentProps: {
      showTime: true,
      format: 'YYYY-MM-DD HH:mm:ss',
      style: { width: '100%' },
    },
  },
  {
    field: 'endTime',
    label: '结束时间',
    component: 'DatePicker',
    componentProps: {
      showTime: true,
      format: 'YYYY-MM-DD HH:mm:ss',
      style: { width: '100%' },
    },
  },
  {
    field: 'inspectionListStatus',
    label: '状态',
    component: 'Select',
    componentProps: {
      dictType: 'inspection_list_status_enum',
      allowClear: true,
    },
    required: true,
    rules: [{ required: true, message: '请选择状态' }],
  },
  {
    field: 'files',
    label: '附件上传',
    component: 'Input',
    slot: 'fileUpload',
    rules: [{ required: false }],
  },
  {
    field: 'fileIds',
    label: '附件ID',
    component: 'Input',
    show: false,
  },
  {
    field: 'attachmentPath',
    label: '附件路径',
    component: 'Input',
    show: false,
  },
  {
    field: 'remarks',
    label: '备注',
    component: 'InputTextArea',
    componentProps: {
      rows: 4,
      maxlength: 500,
    },
  },
];

/**
 * 解析附件JSON数据
 */
function parseAttachments(attachmentPath: string): any[] {
  if (!attachmentPath || attachmentPath === '[]' || attachmentPath === '') {
    return [];
  }
  try {
    return JSON.parse(attachmentPath);
  } catch (e) {
    console.warn('解析附件数据失败:', e);
    return [];
  }
}

/**
 * 判断是否为图片文件
 */
function isImageFile(fileName: string): boolean {
  if (!fileName) return false;
  return /\.(jpeg|jpg|gif|png|bmp|webp)$/i.test(fileName);
}

/**
 * 处理文件预览URL
 */
function processPreviewUrl(url: string): string {
  if (!url) return '';

  // 如果是相对路径，添加前缀
  if (url.startsWith('fileUpload/')) {
    return '/js/swm/' + url;
  }

  return url;
}

/**
 * 处理文件点击事件
 */
function handleFileClick(file: any, event: Event) {
  event.stopPropagation(); // 阻止事件冒泡

  const fileName = file.fileName || '';

  if (isImageFile(fileName)) {
    // 图片文件：预览
    const previewUrl = processPreviewUrl(file.previewUrl || file.url);
    if (previewUrl) {
      createImgPreview({
        imageList: [previewUrl],
        defaultWidth: 800,
      });
    } else {
      console.warn('图片预览URL不存在');
    }
  } else {
    // 非图片文件：下载
    const downloadUrl = file.url;
    if (downloadUrl) {
      const link = document.createElement('a');
      link.href = downloadUrl;
      link.download = fileName || '下载文件';
      link.target = '_blank';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } else {
      console.warn('文件下载URL不存在');
    }
  }
}

/**
 * 渲染附件气泡提示
 */
export function renderAttachmentPopover(attachmentPath: string) {
  const attachments = parseAttachments(attachmentPath);

  if (attachments.length === 0) {
    return '-';
  }

  // 如果只有一个文件，直接显示文件名链接
  if (attachments.length === 1) {
    const file = attachments[0];
    const fileName = file.fileName || '文件';
    const isImage = isImageFile(fileName);

    // 截断显示文件名
    const displayName = fileName.length > 12 ? fileName.substring(0, 9) + '...' : fileName;

    return h(
      'a',
      {
        style: {
          color: '#1890ff',
          textDecoration: 'none',
          cursor: 'pointer',
          fontSize: '12px',
        },
        title: `${fileName} ${isImage ? '(点击预览)' : '(点击下载)'}`,
        onClick: (event: Event) => handleFileClick(file, event),
        onMouseenter: (e: Event) => {
          (e.target as HTMLElement).style.textDecoration = 'underline';
        },
        onMouseleave: (e: Event) => {
          (e.target as HTMLElement).style.textDecoration = 'none';
        },
      },
      displayName,
    );
  }

  // 多个文件时，使用气泡提示
  const fileCount = attachments.length;

  // 创建气泡内容
  const popoverContent = h(
    'div',
    {
      style: {
        maxWidth: '300px',
        maxHeight: '200px',
        overflowY: 'auto',
      },
    },
    [
      h(
        'div',
        {
          style: {
            marginBottom: '8px',
            fontWeight: 'bold',
            fontSize: '12px',
            color: '#666',
          },
        },
        `共 ${fileCount} 个附件`,
      ),

      ...attachments.map((file, index) => {
        const fileName = file.fileName || `文件${index + 1}`;
        const isImage = isImageFile(fileName);

        return h(
          'div',
          {
            key: file.fileId || file.id || index,
            style: {
              marginBottom: '6px',
              padding: '4px 8px',
              borderRadius: '4px',
              backgroundColor: '#f8f9fa',
              border: '1px solid #e9ecef',
            },
          },
          [
            h(
              'a',
              {
                style: {
                  color: '#1890ff',
                  textDecoration: 'none',
                  cursor: 'pointer',
                  fontSize: '12px',
                  display: 'block',
                  wordBreak: 'break-all',
                },
                title: `${fileName} ${isImage ? '(点击预览)' : '(点击下载)'}`,
                onClick: (event: Event) => {
                  event.stopPropagation();
                  handleFileClick(file, event);
                },
                onMouseenter: (e: Event) => {
                  (e.target as HTMLElement).style.textDecoration = 'underline';
                },
                onMouseleave: (e: Event) => {
                  (e.target as HTMLElement).style.textDecoration = 'none';
                },
              },
              fileName,
            ),

            h(
              'div',
              {
                style: {
                  fontSize: '10px',
                  color: '#999',
                  marginTop: '2px',
                },
              },
              isImage ? '图片 - 点击预览' : '文件 - 点击下载',
            ),
          ],
        );
      }),
    ],
  );

  // 返回气泡组件
  return h(
    Popover,
    {
      content: popoverContent,
      title: '附件列表',
      trigger: 'hover',
      placement: 'topLeft',
      overlayStyle: {
        maxWidth: '350px',
      },
    },
    {
      default: () =>
        h(
          'span',
          {
            style: {
              color: '#1890ff',
              cursor: 'pointer',
              fontSize: '12px',
              padding: '2px 6px',
              backgroundColor: '#f0f8ff',
              border: '1px solid #d4edda',
              borderRadius: '4px',
              display: 'inline-block',
            },
          },
          `📎 ${fileCount}个附件`,
        ),
    },
  );
}
