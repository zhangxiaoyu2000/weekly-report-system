<template>
  <div class="base-input">
    <el-input
      v-if="type !== 'textarea'"
      v-bind="inputProps"
      :model-value="modelValue"
      :class="[
        'base-input__field',
        {
          'base-input__field--error': hasError,
          'base-input__field--success': hasSuccess
        }
      ]"
      @input="handleInput"
      @change="handleChange"
      @focus="handleFocus"
      @blur="handleBlur"
      @clear="handleClear"
      @keydown="handleKeydown"
    >
      <template v-if="$slots.prefix" #prefix>
        <slot name="prefix" />
      </template>
      <template v-if="$slots.suffix" #suffix>
        <slot name="suffix" />
      </template>
      <template v-if="$slots.prepend" #prepend>
        <slot name="prepend" />
      </template>
      <template v-if="$slots.append" #append>
        <slot name="append" />
      </template>
    </el-input>
    
    <el-input
      v-else
      v-bind="inputProps"
      :model-value="modelValue"
      type="textarea"
      :class="[
        'base-input__field',
        'base-input__field--textarea',
        {
          'base-input__field--error': hasError,
          'base-input__field--success': hasSuccess
        }
      ]"
      @input="handleInput"
      @change="handleChange"
      @focus="handleFocus"
      @blur="handleBlur"
      @keydown="handleKeydown"
    />
    
    <!-- Error message -->
    <transition name="error-fade">
      <div v-if="hasError && showError" class="base-input__error">
        {{ errorMessage }}
      </div>
    </transition>
    
    <!-- Success message -->
    <transition name="success-fade">
      <div v-if="hasSuccess && showSuccess && successMessage" class="base-input__success">
        {{ successMessage }}
      </div>
    </transition>
    
    <!-- Help text -->
    <div v-if="helpText && !hasError" class="base-input__help">
      {{ helpText }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, nextTick } from 'vue'
import { ElInput } from 'element-plus'
import type { InputProps } from '@/types'

interface Props extends Omit<InputProps, 'modelValue'> {
  modelValue?: string | number
  errorMessage?: string
  successMessage?: string
  helpText?: string
  showError?: boolean
  showSuccess?: boolean
  validateOnBlur?: boolean
  validateOnChange?: boolean
  debounce?: number
  formatter?: (value: string | number) => string | number
  parser?: (value: string) => string | number
}

interface Emits {
  'update:modelValue': [value: string | number]
  input: [value: string | number, event: Event]
  change: [value: string | number]
  focus: [event: FocusEvent]
  blur: [event: FocusEvent]
  clear: []
  keydown: [event: KeyboardEvent]
  validate: [value: string | number, isValid: boolean]
}

const props = withDefaults(defineProps<Props>(), {
  type: 'text',
  size: 'default',
  disabled: false,
  readonly: false,
  clearable: false,
  showPassword: false,
  showWordLimit: false,
  resize: 'vertical',
  validateEvent: true,
  showError: true,
  showSuccess: false,
  validateOnBlur: true,
  validateOnChange: false,
  debounce: 0
})

const emit = defineEmits<Emits>()

// Refs
const isFocused = ref(false)
const debounceTimer = ref<NodeJS.Timeout>()

// Computed properties
const inputProps = computed(() => {
  const { 
    errorMessage, 
    successMessage, 
    helpText, 
    showError, 
    showSuccess, 
    validateOnBlur,
    validateOnChange,
    debounce,
    formatter,
    parser,
    ...rest 
  } = props
  return rest
})

const hasError = computed(() => Boolean(props.errorMessage))
const hasSuccess = computed(() => Boolean(props.successMessage))

// Event handlers
const handleInput = (value: string | number, event?: Event) => {
  let processedValue = value

  // Apply parser if provided
  if (props.parser && typeof value === 'string') {
    processedValue = props.parser(value)
  }

  // Apply formatter if provided
  if (props.formatter) {
    processedValue = props.formatter(processedValue)
  }

  // Clear existing debounce timer
  if (debounceTimer.value) {
    clearTimeout(debounceTimer.value)
  }

  // Debounce the update if debounce is set
  if (props.debounce > 0) {
    debounceTimer.value = setTimeout(() => {
      emit('update:modelValue', processedValue)
      if (event) {
        emit('input', processedValue, event)
      }
      
      // Validate on change if enabled
      if (props.validateOnChange) {
        validateValue(processedValue)
      }
    }, props.debounce)
  } else {
    emit('update:modelValue', processedValue)
    if (event) {
      emit('input', processedValue, event)
    }
    
    // Validate on change if enabled
    if (props.validateOnChange) {
      validateValue(processedValue)
    }
  }
}

const handleChange = (value: string | number) => {
  emit('change', value)
}

const handleFocus = (event: FocusEvent) => {
  isFocused.value = true
  emit('focus', event)
}

const handleBlur = (event: FocusEvent) => {
  isFocused.value = false
  emit('blur', event)
  
  // Validate on blur if enabled
  if (props.validateOnBlur && props.modelValue !== undefined) {
    validateValue(props.modelValue)
  }
}

const handleClear = () => {
  emit('clear')
  emit('update:modelValue', '')
}

const handleKeydown = (event: KeyboardEvent) => {
  emit('keydown', event)
  
  // Handle enter key for single line inputs
  if (event.key === 'Enter' && props.type !== 'textarea') {
    validateValue(props.modelValue || '')
  }
}

// Validation
const validateValue = (value: string | number) => {
  // Basic validation logic - can be extended
  let isValid = true
  
  // Required validation
  if (props.required && (!value || value.toString().trim() === '')) {
    isValid = false
  }
  
  // Length validation
  if (value && typeof value === 'string') {
    if (props.minlength && value.length < props.minlength) {
      isValid = false
    }
    if (props.maxlength && value.length > props.maxlength) {
      isValid = false
    }
  }
  
  emit('validate', value, isValid)
}

// Expose methods for template ref usage
defineExpose({
  focus: () => {
    nextTick(() => {
      const inputEl = document.querySelector('.base-input__field input, .base-input__field textarea') as HTMLInputElement
      inputEl?.focus()
    })
  },
  blur: () => {
    nextTick(() => {
      const inputEl = document.querySelector('.base-input__field input, .base-input__field textarea') as HTMLInputElement
      inputEl?.blur()
    })
  },
  select: () => {
    nextTick(() => {
      const inputEl = document.querySelector('.base-input__field input, .base-input__field textarea') as HTMLInputElement
      inputEl?.select()
    })
  }
})
</script>

<style lang="scss" scoped>
.base-input {
  position: relative;
  width: 100%;

  &__field {
    width: 100%;
    transition: all 0.3s ease;

    &--error {
      :deep(.el-input__wrapper) {
        border-color: var(--el-color-danger);
        box-shadow: 0 0 0 1px var(--el-color-danger) inset;
      }
    }

    &--success {
      :deep(.el-input__wrapper) {
        border-color: var(--el-color-success);
        box-shadow: 0 0 0 1px var(--el-color-success) inset;
      }
    }

    &--textarea {
      :deep(.el-textarea__inner) {
        resize: vertical;
      }
    }
  }

  &__error {
    margin-top: 4px;
    font-size: 12px;
    color: var(--el-color-danger);
    line-height: 1.4;
    display: flex;
    align-items: flex-start;
  }

  &__success {
    margin-top: 4px;
    font-size: 12px;
    color: var(--el-color-success);
    line-height: 1.4;
    display: flex;
    align-items: flex-start;
  }

  &__help {
    margin-top: 4px;
    font-size: 12px;
    color: var(--el-color-info);
    line-height: 1.4;
  }

  // Responsive design
  @media (max-width: 768px) {
    &__error,
    &__success,
    &__help {
      font-size: 11px;
    }
  }

  // Accessibility improvements
  &__field {
    :deep(input),
    :deep(textarea) {
      &:focus-visible {
        outline: 2px solid var(--el-color-primary);
        outline-offset: 2px;
      }
    }
  }
}

// Transitions
.error-fade-enter-active,
.error-fade-leave-active {
  transition: all 0.3s ease;
}

.error-fade-enter-from,
.error-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.success-fade-enter-active,
.success-fade-leave-active {
  transition: all 0.3s ease;
}

.success-fade-enter-from,
.success-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

// High contrast mode support
@media (prefers-contrast: high) {
  .base-input {
    &__field {
      &--error {
        :deep(.el-input__wrapper) {
          border-width: 2px;
        }
      }
      
      &--success {
        :deep(.el-input__wrapper) {
          border-width: 2px;
        }
      }
    }
  }
}

// Reduced motion support
@media (prefers-reduced-motion: reduce) {
  .base-input__field {
    transition: none;
  }
  
  .error-fade-enter-active,
  .error-fade-leave-active,
  .success-fade-enter-active,
  .success-fade-leave-active {
    transition: none;
  }
}
</style>