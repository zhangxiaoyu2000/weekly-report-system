<template>
  <el-button
    v-bind="buttonProps"
    :class="[
      'base-button',
      {
        'base-button--loading': loading,
        'base-button--block': block
      }
    ]"
    @click="handleClick"
  >
    <el-icon v-if="loading" class="is-loading">
      <Loading />
    </el-icon>
    <el-icon v-else-if="icon && !$slots.icon">
      <component :is="icon" />
    </el-icon>
    <slot name="icon" />
    <span v-if="$slots.default || text" class="base-button__text">
      <slot>{{ text }}</slot>
    </span>
  </el-button>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ElButton, ElIcon } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import type { ButtonProps } from '@/types'

interface Props extends ButtonProps {
  text?: string
  block?: boolean
  href?: string
  target?: string
  loading?: boolean
  loadingIcon?: string
  onClick?: (event: MouseEvent) => void | Promise<void>
}

interface Emits {
  click: [event: MouseEvent]
}

const props = withDefaults(defineProps<Props>(), {
  type: 'primary',
  size: 'default',
  nativeType: 'button',
  disabled: false,
  loading: false,
  plain: false,
  round: false,
  circle: false,
  autofocus: false,
  block: false,
  target: '_self'
})

const emit = defineEmits<Emits>()

// Computed properties
const buttonProps = computed(() => {
  const { text, block, href, target, onClick, loadingIcon, ...rest } = props
  return {
    ...rest,
    disabled: props.disabled || props.loading
  }
})

// Event handlers
const handleClick = async (event: MouseEvent) => {
  if (props.loading || props.disabled) {
    return
  }

  // Handle href navigation
  if (props.href) {
    if (props.target === '_blank') {
      window.open(props.href, '_blank')
    } else {
      window.location.href = props.href
    }
    return
  }

  // Emit click event
  emit('click', event)

  // Handle async onClick prop
  if (props.onClick) {
    try {
      await props.onClick(event)
    } catch (error) {
      console.error('Button click handler error:', error)
    }
  }
}

// Expose methods for template ref usage
defineExpose({
  focus: () => {
    // Focus method can be implemented if needed
  }
})
</script>

<style lang="scss" scoped>
.base-button {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.3s ease;

  &--block {
    width: 100%;
    display: flex;
  }

  &--loading {
    pointer-events: none;
  }

  &__text {
    display: inline-flex;
    align-items: center;
  }

  // Responsive design
  @media (max-width: 768px) {
    &:not(.base-button--block) {
      min-width: auto;
    }
  }

  // Accessibility improvements
  &:focus-visible {
    outline: 2px solid var(--el-color-primary);
    outline-offset: 2px;
  }

  // Loading state animation
  .is-loading {
    animation: rotating 2s linear infinite;
  }
}

@keyframes rotating {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

// High contrast mode support
@media (prefers-contrast: high) {
  .base-button {
    border-width: 2px;
  }
}

// Reduced motion support
@media (prefers-reduced-motion: reduce) {
  .base-button {
    transition: none;
  }
  
  .is-loading {
    animation: none;
  }
}

// Dark theme support
@media (prefers-color-scheme: dark) {
  .base-button {
    // Dark theme styles can be added here or handled by Element Plus theme
  }
}
</style>