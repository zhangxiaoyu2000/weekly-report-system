<template>
  <div 
    v-if="visible"
    :class="[
      'base-loading',
      `base-loading--${type}`,
      {
        'base-loading--overlay': overlay,
        'base-loading--fullscreen': fullscreen,
        'base-loading--spinning': spinning
      }
    ]"
    :style="loadingStyle"
  >
    <!-- Loading spinner -->
    <div class="base-loading__spinner">
      <!-- Custom spinner slot -->
      <slot name="spinner">
        <!-- Built-in spinner types -->
        <div v-if="type === 'dots'" class="base-loading__dots">
          <div class="base-loading__dot"></div>
          <div class="base-loading__dot"></div>
          <div class="base-loading__dot"></div>
        </div>
        
        <div v-else-if="type === 'circle'" class="base-loading__circle">
          <svg viewBox="0 0 50 50" class="base-loading__circular">
            <circle
              cx="25"
              cy="25"
              r="20"
              fill="none"
              stroke="currentColor"
              stroke-width="4"
              stroke-miterlimit="10"
              stroke-dasharray="31.416"
              stroke-dashoffset="31.416"
              class="base-loading__path"
            />
          </svg>
        </div>
        
        <div v-else-if="type === 'bars'" class="base-loading__bars">
          <div class="base-loading__bar"></div>
          <div class="base-loading__bar"></div>
          <div class="base-loading__bar"></div>
          <div class="base-loading__bar"></div>
        </div>
        
        <div v-else-if="type === 'pulse'" class="base-loading__pulse">
          <div class="base-loading__pulse-ring"></div>
          <div class="base-loading__pulse-ring"></div>
        </div>
        
        <!-- Element Plus loading icon as default -->
        <el-icon v-else class="base-loading__icon">
          <Loading />
        </el-icon>
      </slot>
    </div>

    <!-- Loading text -->
    <div v-if="text || $slots.text" class="base-loading__text">
      <slot name="text">{{ text }}</slot>
    </div>

    <!-- Progress indicator -->
    <div v-if="showProgress && progress !== null" class="base-loading__progress">
      <el-progress
        :percentage="progress"
        :type="progressType"
        :stroke-width="progressStrokeWidth"
        :show-text="showProgressText"
        :status="progressStatus"
      />
    </div>

    <!-- Additional content -->
    <div v-if="$slots.content" class="base-loading__content">
      <slot name="content" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { ElIcon, ElProgress } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import type { ComponentSize } from '@/types'

type LoadingType = 'default' | 'dots' | 'circle' | 'bars' | 'pulse'
type ProgressType = 'line' | 'circle' | 'dashboard'
type ProgressStatus = 'success' | 'exception' | 'warning'

interface Props {
  visible?: boolean
  type?: LoadingType
  text?: string
  size?: ComponentSize
  color?: string
  background?: string
  overlay?: boolean
  fullscreen?: boolean
  spinning?: boolean
  delay?: number
  minDuration?: number
  showProgress?: boolean
  progress?: number | null
  progressType?: ProgressType
  progressStrokeWidth?: number
  progressStatus?: ProgressStatus
  showProgressText?: boolean
  zIndex?: number
  customClass?: string
  lock?: boolean
}

interface Emits {
  show: []
  hide: []
}

const props = withDefaults(defineProps<Props>(), {
  visible: true,
  type: 'default',
  size: 'default',
  overlay: false,
  fullscreen: false,
  spinning: true,
  delay: 0,
  minDuration: 0,
  showProgress: false,
  progress: null,
  progressType: 'line',
  progressStrokeWidth: 6,
  showProgressText: true,
  zIndex: 2000,
  lock: false
})

const emit = defineEmits<Emits>()

// Refs
const delayTimer = ref<NodeJS.Timeout>()
const minDurationTimer = ref<NodeJS.Timeout>()
const actuallyVisible = ref(false)
const startTime = ref(0)

// Computed properties
const loadingStyle = computed(() => {
  const styles: Record<string, string> = {}
  
  if (props.color) {
    styles.color = props.color
  }
  
  if (props.background) {
    styles.backgroundColor = props.background
  }
  
  if (props.zIndex) {
    styles.zIndex = props.zIndex.toString()
  }
  
  if (props.customClass) {
    styles.className = props.customClass
  }
  
  return styles
})

// Watch visible prop and handle delays
const handleVisibilityChange = (visible: boolean) => {
  if (visible) {
    startTime.value = Date.now()
    
    if (props.delay > 0) {
      delayTimer.value = setTimeout(() => {
        actuallyVisible.value = true
        emit('show')
      }, props.delay)
    } else {
      actuallyVisible.value = true
      emit('show')
    }
  } else {
    // Clear delay timer if hiding before delay finishes
    if (delayTimer.value) {
      clearTimeout(delayTimer.value)
      delayTimer.value = undefined
    }
    
    const elapsed = Date.now() - startTime.value
    const remaining = Math.max(0, props.minDuration - elapsed)
    
    if (remaining > 0) {
      minDurationTimer.value = setTimeout(() => {
        actuallyVisible.value = false
        emit('hide')
      }, remaining)
    } else {
      actuallyVisible.value = false
      emit('hide')
    }
  }
}

// Handle body scroll lock
const handleBodyLock = (lock: boolean) => {
  if (!props.fullscreen) return
  
  if (lock && props.lock) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
}

// Lifecycle
onMounted(() => {
  if (props.visible) {
    handleVisibilityChange(true)
  }
})

onUnmounted(() => {
  if (delayTimer.value) {
    clearTimeout(delayTimer.value)
  }
  if (minDurationTimer.value) {
    clearTimeout(minDurationTimer.value)
  }
  handleBodyLock(false)
})

// Watch props changes
const { visible } = toRefs(props)
watch(visible, (newVal) => {
  handleVisibilityChange(newVal)
  handleBodyLock(newVal)
}, { immediate: true })

watch(actuallyVisible, (newVal) => {
  handleBodyLock(newVal)
})

// Expose computed visible state
const computedVisible = computed(() => actuallyVisible.value)
defineExpose({
  visible: computedVisible
})
</script>

<script lang="ts">
import { toRefs, watch } from 'vue'
export default {
  name: 'BaseLoading'
}
</script>

<style lang="scss" scoped>
.base-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  
  &--overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(2px);
    z-index: 1000;
  }
  
  &--fullscreen {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(4px);
    z-index: 2000;
  }

  &__spinner {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__icon {
    font-size: 24px;
    color: var(--el-color-primary);
    animation: rotating 2s linear infinite;
  }

  // Dots spinner
  &__dots {
    display: flex;
    gap: 4px;
  }

  &__dot {
    width: 8px;
    height: 8px;
    background-color: currentColor;
    border-radius: 50%;
    animation: dot-bounce 1.4s ease-in-out infinite both;

    &:nth-child(1) { animation-delay: -0.32s; }
    &:nth-child(2) { animation-delay: -0.16s; }
    &:nth-child(3) { animation-delay: 0s; }
  }

  // Circle spinner
  &__circle {
    width: 32px;
    height: 32px;
  }

  &__circular {
    width: 100%;
    height: 100%;
    animation: circular-rotate 2s linear infinite;
  }

  &__path {
    stroke-linecap: round;
    animation: circular-dash 1.5s ease-in-out infinite;
  }

  // Bars spinner
  &__bars {
    display: flex;
    gap: 2px;
  }

  &__bar {
    width: 3px;
    height: 20px;
    background-color: currentColor;
    border-radius: 1.5px;
    animation: bar-scale 1s ease-in-out infinite;

    &:nth-child(1) { animation-delay: -0.4s; }
    &:nth-child(2) { animation-delay: -0.3s; }
    &:nth-child(3) { animation-delay: -0.2s; }
    &:nth-child(4) { animation-delay: -0.1s; }
  }

  // Pulse spinner
  &__pulse {
    position: relative;
    width: 32px;
    height: 32px;
  }

  &__pulse-ring {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    border: 2px solid currentColor;
    border-radius: 50%;
    opacity: 0;
    animation: pulse-scale 2s ease-in-out infinite;

    &:nth-child(2) {
      animation-delay: 1s;
    }
  }

  &__text {
    font-size: 14px;
    color: var(--el-text-color-regular);
    text-align: center;
    line-height: 1.4;
  }

  &__progress {
    width: 200px;
    max-width: 80%;
  }

  &__content {
    text-align: center;
    color: var(--el-text-color-regular);
  }

  // Size variants
  &--large {
    .base-loading__icon { font-size: 32px; }
    .base-loading__circle { width: 40px; height: 40px; }
    .base-loading__dot { width: 10px; height: 10px; }
    .base-loading__bar { width: 4px; height: 24px; }
    .base-loading__pulse { width: 40px; height: 40px; }
    .base-loading__text { font-size: 16px; }
    gap: 20px;
  }

  &--small {
    .base-loading__icon { font-size: 16px; }
    .base-loading__circle { width: 24px; height: 24px; }
    .base-loading__dot { width: 6px; height: 6px; }
    .base-loading__bar { width: 2px; height: 16px; }
    .base-loading__pulse { width: 24px; height: 24px; }
    .base-loading__text { font-size: 12px; }
    gap: 12px;
  }

  // Responsive design
  @media (max-width: 768px) {
    &__progress {
      width: 160px;
    }
  }
}

// Animations
@keyframes rotating {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes dot-bounce {
  0%, 80%, 100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes circular-rotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes circular-dash {
  0% {
    stroke-dasharray: 1, 200;
    stroke-dashoffset: 0;
  }
  50% {
    stroke-dasharray: 90, 200;
    stroke-dashoffset: -35;
  }
  100% {
    stroke-dasharray: 90, 200;
    stroke-dashoffset: -124;
  }
}

@keyframes bar-scale {
  0%, 40%, 100% {
    transform: scaleY(0.4);
  }
  20% {
    transform: scaleY(1);
  }
}

@keyframes pulse-scale {
  0% {
    transform: scale(0);
    opacity: 1;
  }
  100% {
    transform: scale(1);
    opacity: 0;
  }
}

// High contrast mode support
@media (prefers-contrast: high) {
  .base-loading {
    &--overlay,
    &--fullscreen {
      background-color: rgba(255, 255, 255, 0.95);
    }
  }
}

// Reduced motion support
@media (prefers-reduced-motion: reduce) {
  .base-loading {
    &__icon,
    &__circular,
    &__dot,
    &__bar,
    &__pulse-ring {
      animation: none;
    }
    
    &--spinning &__icon {
      animation: none;
    }
  }
}

// Dark theme support
@media (prefers-color-scheme: dark) {
  .base-loading {
    &--overlay,
    &--fullscreen {
      background-color: rgba(0, 0, 0, 0.8);
    }
  }
}
</style>