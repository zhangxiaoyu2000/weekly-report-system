/**
 * useDialog composable for dialog state management
 * Provides dialog visibility, loading states, and data management
 */

import { ref, reactive, computed, nextTick } from 'vue'
import type { UseDialogReturn, UseDialogOptions, DialogState } from '@/types'

export function useDialog(options: UseDialogOptions = {}): UseDialogReturn {
  const {
    defaultVisible = false,
    defaultTitle = '',
    defaultData = null,
    onOpen,
    onClose
  } = options

  // Dialog state
  const state = reactive<DialogState>({
    visible: defaultVisible,
    loading: false,
    title: defaultTitle,
    data: defaultData
  })

  // Computed properties for convenient access
  const visible = computed(() => state.visible)
  const loading = computed(() => state.loading)
  const title = computed(() => state.title)
  const data = computed(() => state.data)

  // Open dialog
  const open = async (options: Partial<DialogState> = {}) => {
    // Update state
    Object.assign(state, {
      visible: true,
      loading: false,
      title: options.title ?? state.title,
      data: options.data ?? state.data,
      ...options
    })

    // Call open callback
    if (onOpen) {
      try {
        await onOpen(state)
      } catch (error) {
        console.error('Dialog onOpen callback error:', error)
      }
    }
  }

  // Close dialog
  const close = async () => {
    // Call close callback before closing
    if (onClose) {
      try {
        await onClose(state)
      } catch (error) {
        console.error('Dialog onClose callback error:', error)
      }
    }

    // Update state
    state.visible = false
    state.loading = false
  }

  // Set loading state
  const setLoading = (loading: boolean) => {
    state.loading = loading
  }

  // Set title
  const setTitle = (title: string) => {
    state.title = title
  }

  // Set data
  const setData = (data: any) => {
    state.data = data
  }

  // Reset dialog state to defaults
  const reset = () => {
    state.visible = defaultVisible
    state.loading = false
    state.title = defaultTitle
    state.data = defaultData
  }

  return {
    state: computed(() => state),
    visible,
    loading,
    title,
    data,
    open,
    close,
    setLoading,
    setTitle,
    setData,
    reset
  }
}

// Specialized dialog hooks
export function useConfirmDialog(options: UseDialogOptions & {
  confirmText?: string
  cancelText?: string
  type?: 'warning' | 'info' | 'success' | 'error'
} = {}) {
  const {
    confirmText = '确定',
    cancelText = '取消',
    type = 'warning',
    ...dialogOptions
  } = options

  const dialog = useDialog(dialogOptions)

  // Confirm function that returns a promise
  const confirm = (
    title: string,
    content?: string,
    data?: any
  ): Promise<boolean> => {
    return new Promise((resolve) => {
      dialog.open({
        title,
        data: {
          content,
          type,
          confirmText,
          cancelText,
          ...data
        }
      })

      // Store resolve function for later use
      ;(dialog.state as any)._resolve = resolve
    })
  }

  // Handle confirm action
  const handleConfirm = () => {
    const resolve = (dialog.state as any)._resolve
    if (resolve) {
      resolve(true)
      delete (dialog.state as any)._resolve
    }
    dialog.close()
  }

  // Handle cancel action
  const handleCancel = () => {
    const resolve = (dialog.state as any)._resolve
    if (resolve) {
      resolve(false)
      delete (dialog.state as any)._resolve
    }
    dialog.close()
  }

  return {
    ...dialog,
    confirm,
    handleConfirm,
    handleCancel
  }
}

export function useFormDialog<T = any>(options: UseDialogOptions & {
  initialFormData?: T
  onSubmit?: (data: T) => Promise<void> | void
  onReset?: () => void
  resetOnClose?: boolean
} = {}) {
  const {
    initialFormData = {} as T,
    onSubmit,
    onReset,
    resetOnClose = true,
    ...dialogOptions
  } = options

  const dialog = useDialog(dialogOptions)
  const formData = ref<T>({ ...initialFormData })
  const formErrors = reactive<Record<string, string[]>>({})

  // Open dialog with form data
  const openForm = (data?: Partial<T>, title?: string) => {
    if (data) {
      formData.value = { ...initialFormData, ...data }
    } else {
      formData.value = { ...initialFormData }
    }
    
    // Clear form errors
    Object.keys(formErrors).forEach(key => {
      delete formErrors[key]
    })

    dialog.open({
      title,
      data: formData.value
    })
  }

  // Submit form
  const submitForm = async (): Promise<boolean> => {
    if (dialog.loading.value) return false

    dialog.setLoading(true)

    try {
      if (onSubmit) {
        await onSubmit(formData.value)
      }
      
      dialog.close()
      return true
    } catch (error) {
      console.error('Form submission error:', error)
      
      // Handle validation errors
      if (error && typeof error === 'object' && 'errors' in error) {
        Object.assign(formErrors, (error as any).errors)
      }
      
      return false
    } finally {
      dialog.setLoading(false)
    }
  }

  // Reset form
  const resetForm = () => {
    formData.value = { ...initialFormData }
    
    // Clear form errors
    Object.keys(formErrors).forEach(key => {
      delete formErrors[key]
    })

    if (onReset) {
      onReset()
    }
  }

  // Override close to handle reset
  const close = async () => {
    await dialog.close()
    
    if (resetOnClose) {
      resetForm()
    }
  }

  // Set form field value
  const setFieldValue = (field: keyof T, value: any) => {
    (formData.value as any)[field] = value
  }

  // Set form field error
  const setFieldError = (field: string, errors: string[]) => {
    formErrors[field] = errors
  }

  // Clear form field error
  const clearFieldError = (field: string) => {
    if (formErrors[field]) {
      delete formErrors[field]
    }
  }

  return {
    ...dialog,
    formData,
    formErrors: computed(() => formErrors),
    openForm,
    submitForm,
    resetForm,
    close,
    setFieldValue,
    setFieldError,
    clearFieldError
  }
}

export function useDetailDialog<T = any>(options: UseDialogOptions & {
  loadData?: (id: string | number) => Promise<T>
  onEdit?: (data: T) => void
  onDelete?: (data: T) => Promise<void>
} = {}) {
  const {
    loadData,
    onEdit,
    onDelete,
    ...dialogOptions
  } = options

  const dialog = useDialog(dialogOptions)
  const detailData = ref<T | null>(null)

  // Open detail dialog
  const openDetail = async (id?: string | number, data?: T) => {
    dialog.open()
    
    if (data) {
      // Use provided data
      detailData.value = data
    } else if (id && loadData) {
      // Load data by ID
      dialog.setLoading(true)
      try {
        detailData.value = await loadData(id)
      } catch (error) {
        console.error('Failed to load detail data:', error)
        detailData.value = null
      } finally {
        dialog.setLoading(false)
      }
    } else {
      detailData.value = null
    }

    dialog.setData(detailData.value)
  }

  // Handle edit action
  const handleEdit = () => {
    if (detailData.value && onEdit) {
      onEdit(detailData.value)
    }
  }

  // Handle delete action
  const handleDelete = async () => {
    if (!detailData.value || !onDelete) return

    dialog.setLoading(true)
    try {
      await onDelete(detailData.value)
      dialog.close()
    } catch (error) {
      console.error('Failed to delete:', error)
    } finally {
      dialog.setLoading(false)
    }
  }

  // Refresh detail data
  const refreshDetail = async () => {
    if (!detailData.value || !loadData) return

    // Try to get ID from current data
    const id = (detailData.value as any).id
    if (id) {
      dialog.setLoading(true)
      try {
        detailData.value = await loadData(id)
        dialog.setData(detailData.value)
      } catch (error) {
        console.error('Failed to refresh detail data:', error)
      } finally {
        dialog.setLoading(false)
      }
    }
  }

  return {
    ...dialog,
    detailData: computed(() => detailData.value),
    openDetail,
    handleEdit,
    handleDelete,
    refreshDetail
  }
}

// Dialog manager for handling multiple dialogs
export function useDialogManager() {
  const dialogs = new Map<string, UseDialogReturn>()

  const register = (name: string, dialog: UseDialogReturn) => {
    dialogs.set(name, dialog)
  }

  const unregister = (name: string) => {
    dialogs.delete(name)
  }

  const open = (name: string, options?: Partial<DialogState>) => {
    const dialog = dialogs.get(name)
    if (dialog) {
      dialog.open(options)
    } else {
      console.warn(`Dialog '${name}' not found`)
    }
  }

  const close = (name: string) => {
    const dialog = dialogs.get(name)
    if (dialog) {
      dialog.close()
    } else {
      console.warn(`Dialog '${name}' not found`)
    }
  }

  const closeAll = () => {
    dialogs.forEach(dialog => {
      if (dialog.visible.value) {
        dialog.close()
      }
    })
  }

  const get = (name: string) => {
    return dialogs.get(name)
  }

  const has = (name: string) => {
    return dialogs.has(name)
  }

  const getOpenDialogs = () => {
    const openDialogs: string[] = []
    dialogs.forEach((dialog, name) => {
      if (dialog.visible.value) {
        openDialogs.push(name)
      }
    })
    return openDialogs
  }

  return {
    register,
    unregister,
    open,
    close,
    closeAll,
    get,
    has,
    getOpenDialogs,
    dialogs: computed(() => Array.from(dialogs.keys()))
  }
}

export default useDialog