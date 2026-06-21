import { message, Modal, notification } from 'ant-design-vue';

export function useMessage() {
  const createMessage = {
    success(msg: string, duration = 3) { return message.success(msg, duration); },
    error(msg: string, duration = 3) { return message.error(msg, duration); },
    warning(msg: string, duration = 3) { return message.warning(msg, duration); },
    info(msg: string, duration = 3) { return message.info(msg, duration); },
    loading(msg: string, duration = 0) { return message.loading(msg, duration); },
  };
  function createConfirm(options: any) { return Modal.confirm({ title: options.title || '提示', content: options.content, okText: options.okText || '确定', cancelText: options.cancelText || '取消', onOk: options.onOk, onCancel: options.onCancel, ...options }); }
  function createSuccessModal(options: any) { return Modal.success({ title: '成功', ...options }); }
  function createErrorModal(options: any) { return Modal.error({ title: '错误', ...options }); }
  function createInfoModal(options: any) { return Modal.info({ title: '提示', ...options }); }
  function createWarningModal(options: any) { return Modal.warning({ title: '警告', ...options }); }
  function showMessageModal(options: any, type?: string) { return (Modal as any)[type || 'info']({ title: '提示', ...options }); }
  function showMessage(content: string, type: string = 'info', duration?: number, onClose?: () => void) { return (message as any)[type]?.(content, duration, onClose); }
  return { createMessage, notification, createConfirm, createSuccessModal, createErrorModal, createInfoModal, createWarningModal, showMessageModal, showMessage };
}
