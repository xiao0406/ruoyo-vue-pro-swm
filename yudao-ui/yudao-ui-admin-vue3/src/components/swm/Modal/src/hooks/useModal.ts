import { ref, computed } from 'vue';

export interface ModalProps {
  visible?: boolean;
  title?: string;
  width?: string | number;
  height?: number;
  minHeight?: number;
  confirmLoading?: boolean;
  loading?: boolean;
  canFullscreen?: boolean;
  defaultFullscreen?: boolean;
  showOkBtn?: boolean;
  showCancelBtn?: boolean;
  destroyOnClose?: boolean;
  centered?: boolean;
  maskClosable?: boolean;
  footer?: any;
  bodyStyle?: any;
  zIndex?: number;
  draggable?: boolean;
  [key: string]: any;
}

export interface ReturnMethods {
  openModal: (visible?: boolean, data?: any) => void;
  closeModal: () => void;
  setModalProps: (props: Partial<ModalProps>) => void;
  setModalData: (data: any) => void;
  getVisible: any;
  redoModalHeight: () => void;
}

export interface ReturnInnerMethods {
  closeModal: () => void;
  changeLoading: (loading?: boolean) => void;
  changeOkLoading: (loading?: boolean) => void;
  setModalProps: (props: Partial<ModalProps>) => void;
  getData: () => any;
  getVisible: any;
  redoModalHeight: () => void;
}

export function useModal(): [(instance: any) => void, ReturnMethods] {
  const modalRef = ref<any>(null);
  const visible = ref(false);
  const modalData = ref<any>(null);
  const modalProps = ref<Partial<ModalProps>>({});

  function register(instance: any) {
    modalRef.value = instance;
  }

  const methods: ReturnMethods = {
    openModal(visibleVal?: boolean, data?: any) {
      visible.value = visibleVal !== false;
      if (data !== undefined) {
        modalData.value = data;
      }
    },
    closeModal() {
      visible.value = false;
    },
    setModalProps(props: Partial<ModalProps>) {
      modalProps.value = { ...modalProps.value, ...props };
    },
    setModalData(data: any) {
      modalData.value = data;
    },
    getVisible: computed(() => visible.value),
    redoModalHeight() {},
  };

  return [register, methods];
}

export function useModalInner(callback?: (data?: any) => void): [(instance: any) => void, ReturnInnerMethods] {
  const visible = ref(false);
  const confirmLoading = ref(false);
  const loading = ref(false);
  const innerData = ref<any>(null);
  const innerProps = ref<Partial<ModalProps>>({});

  function register(instance: any) {
    // When the BasicModal registers, we sync state
    if (instance) {
      instance.visibleRef = visible;
      instance.confirmLoadingRef = confirmLoading;
      instance.loadingRef = loading;
      instance.innerPropsRef = innerProps;
    }
  }

  const methods: ReturnInnerMethods = {
    closeModal() {
      visible.value = false;
    },
    changeLoading(loadingVal?: boolean) {
      loading.value = loadingVal ?? !loading.value;
    },
    changeOkLoading(loadingVal?: boolean) {
      confirmLoading.value = loadingVal ?? !confirmLoading.value;
    },
    setModalProps(props: Partial<ModalProps>) {
      innerProps.value = { ...innerProps.value, ...props };
    },
    getData() {
      return innerData.value;
    },
    getVisible: computed(() => visible.value),
    redoModalHeight() {},
  };

  return [register, methods];
}
