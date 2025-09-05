<template>
  <el-form
    ref="formRef"
    v-bind="formProps"
    :model="formModel"
    :rules="formRules"
    :class="[
      'base-form',
      {
        'base-form--inline': inline,
        'base-form--loading': loading,
        'base-form--readonly': readonly
      }
    ]"
    @validate="handleValidate"
    @submit.prevent="handleSubmit"
  >
    <el-row v-if="!inline" :gutter="gutter">
      <template v-for="field in visibleFields" :key="field.prop">
        <!-- Form field -->
        <el-col v-bind="getColProps(field)">
          <el-form-item
            :prop="field.prop"
            :label="field.label"
            :required="isFieldRequired(field)"
            :class="[
              'base-form__item',
              `base-form__item--${field.type || 'input'}`,
              {
                'base-form__item--readonly': isFieldReadonly(field),
                'base-form__item--disabled': isFieldDisabled(field)
              }
            ]"
          >
            <!-- Field label slot -->
            <template v-if="field.labelSlot || $slots[`label-${field.prop}`]" #label>
              <slot 
                :name="field.labelSlot || `label-${field.prop}`" 
                :field="field"
                :model="formModel"
              />
            </template>

            <!-- Field content -->
            <div class="base-form__field">
              <!-- Custom field slot -->
              <slot 
                v-if="field.slot || $slots[field.prop]"
                :name="field.slot || field.prop"
                :field="field"
                :model="formModel"
                :value="getFieldValue(field.prop)"
                :setValue="(value: any) => setFieldValue(field.prop, value)"
              />
              
              <!-- Built-in field types -->
              <component 
                v-else
                :is="getFieldComponent(field)"
                v-bind="getFieldProps(field)"
                :model-value="getFieldValue(field.prop)"
                :disabled="isFieldDisabled(field)"
                :readonly="isFieldReadonly(field)"
                @update:model-value="(value: any) => setFieldValue(field.prop, value)"
                @change="(value: any) => handleFieldChange(field.prop, value)"
                @blur="() => handleFieldBlur(field.prop)"
              />
            </div>

            <!-- Field help text -->
            <div v-if="field.help" class="base-form__help">
              {{ field.help }}
            </div>
          </el-form-item>
        </el-col>
      </template>
    </el-row>

    <!-- Inline form layout -->
    <template v-else>
      <template v-for="field in visibleFields" :key="field.prop">
        <el-form-item
          :prop="field.prop"
          :label="field.label"
          :required="isFieldRequired(field)"
          :class="[
            'base-form__item',
            'base-form__item--inline',
            `base-form__item--${field.type || 'input'}`
          ]"
        >
          <!-- Field content for inline form -->
          <component 
            :is="getFieldComponent(field)"
            v-bind="getFieldProps(field)"
            :model-value="getFieldValue(field.prop)"
            :disabled="isFieldDisabled(field)"
            :readonly="isFieldReadonly(field)"
            @update:model-value="(value: any) => setFieldValue(field.prop, value)"
            @change="(value: any) => handleFieldChange(field.prop, value)"
            @blur="() => handleFieldBlur(field.prop)"
          />
        </el-form-item>
      </template>
    </template>

    <!-- Form actions -->
    <div v-if="$slots.actions || showActions" class="base-form__actions">
      <slot name="actions" :model="formModel" :validate="validate" :reset="resetForm">
        <el-form-item v-if="showActions">
          <div class="base-form__action-buttons">
            <el-button
              v-if="showSubmit"
              type="primary"
              :loading="submitLoading"
              :disabled="loading || readonly"
              @click="handleSubmit"
            >
              {{ submitText }}
            </el-button>
            
            <el-button
              v-if="showReset"
              :disabled="loading || readonly"
              @click="resetForm"
            >
              {{ resetText }}
            </el-button>
            
            <el-button
              v-if="showCancel"
              :disabled="loading"
              @click="handleCancel"
            >
              {{ cancelText }}
            </el-button>
          </div>
        </el-form-item>
      </slot>
    </div>

    <!-- Loading overlay -->
    <base-loading 
      v-if="loading"
      :visible="loading"
      :text="loadingText"
      overlay
      class="base-form__loading"
    />
  </el-form>
</template>

<script setup lang="ts">
import { computed, ref, reactive, nextTick, watch } from 'vue'
import { 
  ElForm, 
  ElFormItem, 
  ElRow, 
  ElCol, 
  ElButton, 
  ElInput,
  ElSelect,
  ElOption,
  ElRadio,
  ElRadioGroup,
  ElCheckbox,
  ElCheckboxGroup,
  ElSwitch,
  ElDatePicker,
  ElTimePicker,
  ElTimeSelect,
  ElSlider,
  ElRate,
  ElColorPicker,
  ElUpload,
  ElCascader,
  ElTransfer
} from 'element-plus'
import BaseLoading from '../base/BaseLoading.vue'
import type { FormProps, FormField, FormRule } from '@/types'

interface Props extends Omit<FormProps, 'modelValue' | 'fields'> {
  modelValue: Record<string, any>
  fields: FormField[]
  loading?: boolean
  readonly?: boolean
  gutter?: number
  showActions?: boolean
  showSubmit?: boolean
  showReset?: boolean
  showCancel?: boolean
  submitText?: string
  resetText?: string
  cancelText?: string
  submitLoading?: boolean
  loadingText?: string
  validateOnFieldChange?: boolean
  validateOnFieldBlur?: boolean
  resetOnSubmit?: boolean
  onSubmit?: (model: Record<string, any>) => void | Promise<void>
  onReset?: () => void
  onCancel?: () => void
  onFieldChange?: (prop: string, value: any, model: Record<string, any>) => void
  onFieldBlur?: (prop: string, model: Record<string, any>) => void
}

interface Emits {
  'update:modelValue': [value: Record<string, any>]
  submit: [model: Record<string, any>]
  reset: []
  cancel: []
  validate: [prop: string, valid: boolean, message?: string]
  'field-change': [prop: string, value: any, model: Record<string, any>]
  'field-blur': [prop: string, model: Record<string, any>]
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  readonly: false,
  gutter: 20,
  showActions: true,
  showSubmit: true,
  showReset: false,
  showCancel: false,
  submitText: '提交',
  resetText: '重置',
  cancelText: '取消',
  submitLoading: false,
  loadingText: '处理中...',
  validateOnFieldChange: false,
  validateOnFieldBlur: true,
  resetOnSubmit: false
})

const emit = defineEmits<Emits>()

// Refs
const formRef = ref<InstanceType<typeof ElForm>>()

// Computed properties
const formModel = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const formProps = computed(() => {
  const { 
    modelValue,
    fields,
    loading,
    readonly,
    gutter,
    showActions,
    showSubmit,
    showReset,
    showCancel,
    submitText,
    resetText,
    cancelText,
    submitLoading,
    loadingText,
    validateOnFieldChange,
    validateOnFieldBlur,
    resetOnSubmit,
    onSubmit,
    onReset,
    onCancel,
    onFieldChange,
    onFieldBlur,
    ...rest 
  } = props
  return rest
})

const formRules = computed(() => {
  const rules: Record<string, FormRule[]> = {}
  
  // Merge field rules with form rules
  props.fields.forEach(field => {
    if (field.rules && field.rules.length > 0) {
      rules[field.prop] = field.rules
    }
  })
  
  // Add form-level rules
  if (props.rules) {
    Object.keys(props.rules).forEach(prop => {
      if (rules[prop]) {
        rules[prop] = [...rules[prop], ...props.rules![prop]]
      } else {
        rules[prop] = props.rules![prop]
      }
    })
  }
  
  return rules
})

const visibleFields = computed(() => {
  return props.fields.filter(field => {
    if (field.show === undefined) return true
    if (typeof field.show === 'boolean') return field.show
    return field.show(formModel.value)
  })
})

// Field helper methods
const getFieldValue = (prop: string) => {
  return prop.split('.').reduce((obj, key) => obj?.[key], formModel.value)
}

const setFieldValue = (prop: string, value: any) => {
  const keys = prop.split('.')
  const lastKey = keys.pop()!
  const target = keys.reduce((obj, key) => {
    if (!obj[key]) obj[key] = {}
    return obj[key]
  }, formModel.value)
  target[lastKey] = value
  
  emit('update:modelValue', { ...formModel.value })
}

const isFieldRequired = (field: FormField) => {
  return field.rules?.some(rule => rule.required) || false
}

const isFieldDisabled = (field: FormField) => {
  if (props.disabled) return true
  if (field.disabled === undefined) return false
  if (typeof field.disabled === 'boolean') return field.disabled
  return field.disabled(formModel.value)
}

const isFieldReadonly = (field: FormField) => {
  if (props.readonly) return true
  return field.readonly || false
}

const getColProps = (field: FormField) => {
  return {
    span: field.span || 24,
    offset: field.offset || 0,
    xs: field.xs,
    sm: field.sm,
    md: field.md,
    lg: field.lg,
    xl: field.xl
  }
}

const getFieldComponent = (field: FormField) => {
  const componentMap = {
    input: ElInput,
    textarea: ElInput,
    select: ElSelect,
    radio: ElRadioGroup,
    checkbox: ElCheckboxGroup,
    switch: ElSwitch,
    date: ElDatePicker,
    datetime: ElDatePicker,
    time: ElTimePicker,
    timeSelect: ElTimeSelect,
    slider: ElSlider,
    rate: ElRate,
    color: ElColorPicker,
    upload: ElUpload,
    cascader: ElCascader,
    transfer: ElTransfer,
    custom: field.component
  }
  
  return componentMap[field.type || 'input'] || ElInput
}

const getFieldProps = (field: FormField) => {
  const commonProps = {
    placeholder: field.placeholder,
    size: props.size,
    ...field.componentProps
  }
  
  // Type-specific props
  switch (field.type) {
    case 'textarea':
      return { ...commonProps, type: 'textarea', rows: 4 }
    case 'select':
      return commonProps
    case 'date':
      return { ...commonProps, type: 'date', valueFormat: 'YYYY-MM-DD' }
    case 'datetime':
      return { ...commonProps, type: 'datetime', valueFormat: 'YYYY-MM-DD HH:mm:ss' }
    case 'time':
      return { ...commonProps, valueFormat: 'HH:mm:ss' }
    default:
      return commonProps
  }
}

// Event handlers
const handleFieldChange = async (prop: string, value: any) => {
  emit('field-change', prop, value, formModel.value)
  
  if (props.onFieldChange) {
    props.onFieldChange(prop, value, formModel.value)
  }
  
  if (props.validateOnFieldChange) {
    await nextTick()
    formRef.value?.validateField(prop)
  }
}

const handleFieldBlur = async (prop: string) => {
  emit('field-blur', prop, formModel.value)
  
  if (props.onFieldBlur) {
    props.onFieldBlur(prop, formModel.value)
  }
  
  if (props.validateOnFieldBlur) {
    await nextTick()
    formRef.value?.validateField(prop)
  }
}

const handleValidate = (prop: string, valid: boolean, message?: string) => {
  emit('validate', prop, valid, message)
}

const handleSubmit = async () => {
  if (props.loading || props.readonly) return
  
  try {
    const valid = await validate()
    if (!valid) return
    
    emit('submit', formModel.value)
    
    if (props.onSubmit) {
      await props.onSubmit(formModel.value)
    }
    
    if (props.resetOnSubmit) {
      resetForm()
    }
  } catch (error) {
    console.error('Form submit error:', error)
  }
}

const handleCancel = () => {
  emit('cancel')
  
  if (props.onCancel) {
    props.onCancel()
  }
}

// Form methods
const validate = async (): Promise<boolean> => {
  if (!formRef.value) return false
  
  try {
    await formRef.value.validate()
    return true
  } catch (error) {
    return false
  }
}

const validateField = async (prop: string): Promise<boolean> => {
  if (!formRef.value) return false
  
  try {
    await formRef.value.validateField(prop)
    return true
  } catch (error) {
    return false
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
  emit('reset')
  
  if (props.onReset) {
    props.onReset()
  }
}

const clearValidation = (props?: string | string[]) => {
  formRef.value?.clearValidate(props)
}

// Expose methods for template ref usage
defineExpose({
  validate,
  validateField,
  resetFields: resetForm,
  clearValidate: clearValidation,
  formRef
})
</script>

<style lang="scss" scoped>
.base-form {
  position: relative;
  width: 100%;

  &--loading {
    pointer-events: none;
  }

  &--readonly {
    :deep(.el-input),
    :deep(.el-select),
    :deep(.el-textarea) {
      .el-input__wrapper,
      .el-select__wrapper {
        background-color: var(--el-disabled-bg-color);
        cursor: not-allowed;
      }
    }
  }

  &__item {
    margin-bottom: 22px;

    &--inline {
      display: inline-block;
      margin-right: 16px;
      margin-bottom: 0;
    }

    &--readonly {
      :deep(.el-form-item__label) {
        color: var(--el-text-color-regular);
      }
    }

    &--disabled {
      :deep(.el-form-item__label) {
        color: var(--el-text-color-disabled);
      }
    }

    // Field type specific styles
    &--upload {
      :deep(.el-upload) {
        width: 100%;
      }
    }

    &--slider {
      :deep(.el-slider) {
        margin: 12px 0;
      }
    }

    &--rate {
      :deep(.el-rate) {
        margin: 8px 0;
      }
    }
  }

  &__field {
    width: 100%;

    // Select field with options
    :deep(.el-select) {
      width: 100%;
    }

    // Radio and checkbox groups
    :deep(.el-radio-group),
    :deep(.el-checkbox-group) {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
    }

    // Date picker full width
    :deep(.el-date-editor) {
      width: 100%;
    }
  }

  &__help {
    margin-top: 4px;
    font-size: 12px;
    color: var(--el-color-info);
    line-height: 1.4;
  }

  &__actions {
    margin-top: 32px;
    padding-top: 24px;
    border-top: 1px solid var(--el-border-color-lighter);

    :deep(.el-form-item) {
      margin-bottom: 0;
    }
  }

  &__action-buttons {
    display: flex;
    gap: 12px;
    justify-content: flex-start;

    @media (max-width: 768px) {
      flex-direction: column;
      width: 100%;

      .el-button {
        width: 100%;
      }
    }
  }

  &__loading {
    background-color: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(2px);
  }

  // Responsive design
  @media (max-width: 768px) {
    &__item {
      margin-bottom: 18px;

      &--inline {
        display: block;
        margin-right: 0;
        margin-bottom: 18px;
      }
    }

    &__actions {
      margin-top: 24px;
      padding-top: 16px;
    }

    :deep(.el-form-item__label) {
      line-height: 1.4;
      word-break: break-word;
    }
  }

  // Accessibility improvements
  :deep(.el-form-item__label) {
    &[aria-required="true"]::before {
      content: "*";
      color: var(--el-color-danger);
      margin-right: 4px;
    }
  }
}

// High contrast mode support
@media (prefers-contrast: high) {
  .base-form {
    &__actions {
      border-top-width: 2px;
    }
  }
}

// Dark theme support
@media (prefers-color-scheme: dark) {
  .base-form {
    &__loading {
      background-color: rgba(0, 0, 0, 0.8);
    }
  }
}
</style>