<template>
  <el-dialog
    v-bind="dialogProps"
    :model-value="modelValue"
    :class="[
      'base-dialog',
      {
        'base-dialog--fullscreen': fullscreen,
        'base-dialog--draggable': draggable,
        'base-dialog--loading': loading
      }
    ]"
    @open="handleOpen"
    @opened="handleOpened"
    @close="handleClose"
    @closed="handleClosed"
    @update:model-value="handleUpdateModelValue"
  >
    <!-- Dialog header -->
    <template v-if="$slots.header || showHeader" #header="{ close, titleId, titleClass }">
      <div :id="titleId" :class="['base-dialog__header', titleClass]">
        <slot name="header" :close="close" :title="title">
          <div class="base-dialog__title">
            <el-icon v-if="titleIcon" class="base-dialog__title-icon">
              <component :is="titleIcon" />
            </el-icon>
            <span>{{ title }}</span>
            <el-tag v-if="badge" :type="badgeType" size="small" class="base-dialog__badge">
              {{ badge }}
            </el-tag>
          </div>
          
          <!-- Custom header actions -->
          <div v-if="$slots.headerActions" class="base-dialog__header-actions">
            <slot name="headerActions" :close="close" />
          </div>
        </slot>
      </div>
    </template>

    <!-- Dialog content -->
    <div 
      v-loading="loading"
      :element-loading-text="loadingText"
      :element-loading-spinner="loadingSpinner"
      :element-loading-background="loadingBackground"
      class="base-dialog__content"
    >
      <slot :close="handleClose" :confirm="handleConfirm" :cancel="handleCancel" />
      
      <!-- Empty state -->
      <div v-if="showEmpty" class="base-dialog__empty">
        <slot name="empty">
          <div class="base-dialog__empty-content">
            <el-icon class="base-dialog__empty-icon"><DocumentIcon /></el-icon>
            <p class="base-dialog__empty-text">{{ emptyText || '暂无内容' }}</p>
          </div>
        </slot>
      </div>
    </div>

    <!-- Dialog footer -->
    <template v-if="$slots.footer || showFooter" #footer>
      <div class="base-dialog__footer">
        <slot name="footer" :close="handleClose" :confirm="handleConfirm" :cancel="handleCancel">
          <div class="base-dialog__footer-actions">
            <!-- Custom left actions -->
            <div class="base-dialog__footer-left">
              <slot name="footerLeft" />
            </div>
            
            <!-- Default actions -->
            <div class="base-dialog__footer-right">
              <el-button 
                v-if="showCancel"
                :size="buttonSize"
                :disabled="loading || confirmLoading"
                @click="handleCancel"
              >
                {{ cancelText }}
              </el-button>
              <el-button 
                v-if="showConfirm"
                :type="confirmType"
                :size="buttonSize"
                :loading="confirmLoading"
                :disabled="loading || !confirmEnabled"
                @click="handleConfirm"
              >
                {{ confirmText }}
              </el-button>
            </div>
          </div>
        </slot>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref, nextTick } from 'vue'
import { ElDialog, ElButton, ElIcon, ElTag } from 'element-plus'
import { Document as DocumentIcon } from '@element-plus/icons-vue'
import type { DialogProps, ComponentSize, ComponentVariant } from '@/types'

interface Props extends DialogProps {
  title?: string
  titleIcon?: string | object
  badge?: string
  badgeType?: ComponentVariant
  showHeader?: boolean
  showFooter?: boolean
  showCancel?: boolean
  showConfirm?: boolean
  cancelText?: string
  confirmText?: string
  confirmType?: ComponentVariant
  confirmEnabled?: boolean
  confirmLoading?: boolean
  buttonSize?: ComponentSize
  loadingText?: string
  loadingSpinner?: string
  loadingBackground?: string
  showEmpty?: boolean
  emptyText?: string
  maxHeight?: string | number
  minHeight?: string | number
  persistent?: boolean
  keyboard?: boolean
  onOpen?: () => void | Promise<void>
  onOpened?: () => void | Promise<void>
  onClose?: () => void | Promise<void>
  onClosed?: () => void | Promise<void>
  onConfirm?: () => void | Promise<void>
  onCancel?: () => void | Promise<void>
}

interface Emits {
  'update:modelValue': [value: boolean]
  open: []
  opened: []
  close: []
  closed: []
  confirm: []
  cancel: []
}

const props = withDefaults(defineProps<Props>(), {
  title: '',
  showHeader: true,
  showFooter: true,
  showCancel: true,
  showConfirm: true,
  cancelText: '取消',
  confirmText: '确定',
  confirmType: 'primary',
  confirmEnabled: true,
  confirmLoading: false,
  buttonSize: 'default',
  loadingBackground: 'rgba(255, 255, 255, 0.8)',
  showEmpty: false,
  keyboard: true,
  persistent: false
})

const emit = defineEmits<Emits>()

// Refs
const dialogRef = ref<InstanceType<typeof ElDialog>>()

// Computed properties
const dialogProps = computed(() => {
  const { 
    title,
    titleIcon,
    badge,
    badgeType,
    showHeader,
    showFooter,
    showCancel,
    showConfirm,
    cancelText,
    confirmText,
    confirmType,
    confirmEnabled,
    confirmLoading,
    buttonSize,
    loadingText,
    loadingSpinner,
    loadingBackground,
    showEmpty,
    emptyText,
    maxHeight,
    minHeight,
    persistent,
    keyboard,
    onOpen,
    onOpened,
    onClose,
    onClosed,
    onConfirm,
    onCancel,
    ...rest 
  } = props

  return {
    ...rest,
    closeOnClickModal: !persistent && rest.closeOnClickModal !== false,
    closeOnPressEscape: keyboard && rest.closeOnPressEscape !== false
  }
})

// Event handlers
const handleUpdateModelValue = (value: boolean) => {
  emit('update:modelValue', value)
}

const handleOpen = async () => {
  emit('open')
  if (props.onOpen) {
    try {
      await props.onOpen()
    } catch (error) {
      console.error('Dialog onOpen error:', error)
    }
  }
}

const handleOpened = async () => {
  emit('opened')
  if (props.onOpened) {
    try {
      await props.onOpened()
    } catch (error) {
      console.error('Dialog onOpened error:', error)
    }
  }
}

const handleClose = async () => {
  emit('close')
  if (props.onClose) {
    try {
      await props.onClose()
    } catch (error) {
      console.error('Dialog onClose error:', error)
    }
  }
  emit('update:modelValue', false)
}

const handleClosed = async () => {
  emit('closed')
  if (props.onClosed) {
    try {
      await props.onClosed()
    } catch (error) {
      console.error('Dialog onClosed error:', error)
    }
  }
}

const handleConfirm = async () => {
  if (props.loading || props.confirmLoading || !props.confirmEnabled) {
    return
  }

  emit('confirm')
  if (props.onConfirm) {
    try {
      await props.onConfirm()
    } catch (error) {
      console.error('Dialog onConfirm error:', error)
      return // Don't close dialog if confirm handler fails
    }
  }
  
  // Close dialog after successful confirmation
  emit('update:modelValue', false)
}

const handleCancel = async () => {
  if (props.loading) {
    return
  }

  emit('cancel')
  if (props.onCancel) {
    try {
      await props.onCancel()
    } catch (error) {
      console.error('Dialog onCancel error:', error)
    }
  }
  
  emit('update:modelValue', false)
}

// Expose methods for template ref usage
defineExpose({
  focus: () => {
    nextTick(() => {
      const dialogEl = dialogRef.value?.$el?.querySelector('.el-dialog')
      if (dialogEl) {
        (dialogEl as HTMLElement).focus()
      }
    })
  }
})
</script>

<style lang="scss" scoped>
.base-dialog {
  :deep(.el-dialog) {
    border-radius: 8px;
    overflow: hidden;

    @media (max-width: 768px) {
      width: 95% !important;
      margin: 5vh auto;
    }
  }

  :deep(.el-dialog__header) {
    padding: 20px 24px 16px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  :deep(.el-dialog__body) {
    padding: 0;
  }

  :deep(.el-dialog__footer) {
    padding: 16px 24px 20px;
    border-top: 1px solid var(--el-border-color-lighter);
  }

  &--fullscreen {
    :deep(.el-dialog) {
      border-radius: 0;
    }
  }

  &--draggable {
    :deep(.el-dialog__header) {
      cursor: move;
      user-select: none;
    }
  }

  &--loading {
    :deep(.el-dialog__body) {
      position: relative;
    }
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  &__title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    font-weight: 600;
    color: var(--el-text-color-primary);

    &-icon {
      font-size: 20px;
      color: var(--el-color-primary);
    }
  }

  &__badge {
    margin-left: 8px;
  }

  &__header-actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__content {
    padding: 24px;
    min-height: 100px;
    max-height: 60vh;
    overflow-y: auto;

    // Custom scrollbar
    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-track {
      background: var(--el-fill-color-lighter);
      border-radius: 3px;
    }

    &::-webkit-scrollbar-thumb {
      background: var(--el-border-color-base);
      border-radius: 3px;
    }

    &::-webkit-scrollbar-thumb:hover {
      background: var(--el-border-color-dark);
    }
  }

  &__empty {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 200px;

    &-content {
      text-align: center;
    }

    &-icon {
      font-size: 48px;
      color: var(--el-color-info-light-3);
      margin-bottom: 12px;
    }

    &-text {
      color: var(--el-color-info);
      font-size: 14px;
      margin: 0;
    }
  }

  &__footer {
    &-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    &-left {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    &-right {
      display: flex;
      align-items: center;
      gap: 12px;

      @media (max-width: 768px) {
        flex-direction: column-reverse;
        width: 100%;
        gap: 8px;

        .el-button {
          width: 100%;
        }
      }
    }
  }

  // Responsive adjustments
  @media (max-width: 768px) {
    &__header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
    }

    &__title {
      font-size: 16px;
    }

    &__content {
      padding: 16px;
      max-height: 50vh;
    }

    &__footer-actions {
      flex-direction: column;
      gap: 12px;
    }
  }

  // Accessibility improvements
  :deep(.el-dialog) {
    &:focus-visible {
      outline: 2px solid var(--el-color-primary);
      outline-offset: 2px;
    }
  }
}

// Animation improvements
:deep(.el-dialog__wrapper) {
  .el-dialog {
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  }
}

// High contrast mode support
@media (prefers-contrast: high) {
  .base-dialog {
    :deep(.el-dialog__header),
    :deep(.el-dialog__footer) {
      border-width: 2px;
    }
  }
}

// Reduced motion support
@media (prefers-reduced-motion: reduce) {
  .base-dialog {
    :deep(.el-dialog__wrapper) {
      .el-dialog {
        transition: none;
      }
    }
  }
}

// Dark theme support (handled by Element Plus variables)
@media (prefers-color-scheme: dark) {
  .base-dialog {
    &__content {
      &::-webkit-scrollbar-track {
        background: var(--el-fill-color-dark);
      }
    }
  }
}
</style>