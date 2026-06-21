export interface ActionItem {
  icon?: string;
  label?: string;
  tooltip?: string;
  color?: 'success' | 'error' | 'info' | 'warning' | 'primary';
  onClick?: (...args: any[]) => void;
  disabled?: boolean;
  divider?: boolean;
  auth?: string;
  ifShow?: boolean | ((action: any) => boolean);
  popConfirm?: {
    title: string;
    confirm: (...args: any[]) => void;
    cancel?: (...args: any[]) => void;
    okText?: string;
    icon?: string;
  };
}
