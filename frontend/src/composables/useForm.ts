/**
 * useForm composable for form state management and validation
 * Provides form data binding, validation, submission handling
 */

import { ref, reactive, computed, watch, nextTick, unref, type Ref, type MaybeRef } from 'vue'
import type { UseFormReturn, UseFormOptions, FormRule, ValidationResult } from '@/types'

export function useForm(options: UseFormOptions = {}): UseFormReturn {
  const {
    initialValues = {},
    rules: initialRules = {},
    validateOnRuleChange = true,
    validateOnChange = false,
    onSubmit,
    onReset
  } = options

  // Form state
  const model = ref<Record<string, any>>({ ...initialValues })
  const rules = ref<Record<string, FormRule[]>>({ ...initialRules })
  const loading = ref(false)
  const errors = reactive<Record<string, string[]>>({})
  
  // Field touched state for better UX
  const touched = reactive<Record<string, boolean>>({})

  // Clear errors when rules change
  if (validateOnRuleChange) {
    watch(() => rules.value, () => {
      Object.keys(errors).forEach(prop => {
        delete errors[prop]
      })
    }, { deep: true })
  }

  // Watch for field changes and validate if enabled
  if (validateOnChange) {
    watch(() => model.value, (newModel, oldModel) => {
      if (!oldModel) return
      
      Object.keys(newModel).forEach(prop => {
        if (newModel[prop] !== oldModel[prop]) {
          touched[prop] = true
          if (rules.value[prop]) {
            validateField(prop)
          }
        }
      })
    }, { deep: true })
  }

  // Validation methods
  const validateField = async (prop: string, callback?: (valid: boolean, error?: string) => void): Promise<boolean> => {
    const fieldRules = rules.value[prop]
    if (!fieldRules || fieldRules.length === 0) {
      clearFieldError(prop)
      callback?.(true)
      return true
    }

    const value = getFieldValue(prop)
    const fieldErrors: string[] = []

    try {
      // Execute validation rules sequentially
      for (const rule of fieldRules) {
        const isValid = await validateRule(rule, value, prop, model.value)
        if (!isValid) {
          fieldErrors.push(rule.message || `${prop} validation failed`)
          break // Stop at first failed rule
        }
      }

      if (fieldErrors.length > 0) {
        errors[prop] = fieldErrors
        callback?.(false, fieldErrors[0])
        return false
      } else {
        clearFieldError(prop)
        callback?.(true)
        return true
      }
    } catch (error) {
      const errorMsg = error instanceof Error ? error.message : 'Validation error'
      errors[prop] = [errorMsg]
      callback?.(false, errorMsg)
      return false
    }
  }

  const validate = async (callback?: (valid: boolean, errors?: Record<string, string[]>) => void): Promise<ValidationResult> => {
    const validationPromises = Object.keys(rules.value).map(prop => 
      validateField(prop).then(valid => ({ prop, valid }))
    )

    try {
      const results = await Promise.all(validationPromises)
      const invalid = results.filter(r => !r.valid)
      const isValid = invalid.length === 0

      const validationResult: ValidationResult = {
        valid: isValid,
        errors: { ...errors }
      }

      callback?.(isValid, isValid ? undefined : validationResult.errors)
      return validationResult
    } catch (error) {
      const validationResult: ValidationResult = {
        valid: false,
        errors: { ...errors }
      }
      callback?.(false, validationResult.errors)
      return validationResult
    }
  }

  // Field value management
  const getFieldValue = (prop: string) => {
    return prop.split('.').reduce((obj, key) => obj?.[key], model.value)
  }

  const setFieldValue = (prop: string, value: any) => {
    const keys = prop.split('.')
    const lastKey = keys.pop()!
    const target = keys.reduce((obj, key) => {
      if (!obj[key]) obj[key] = {}
      return obj[key]
    }, model.value)
    
    target[lastKey] = value
    touched[prop] = true
  }

  const setFieldError = (prop: string, error: string) => {
    errors[prop] = [error]
  }

  const clearFieldError = (prop: string) => {
    if (errors[prop]) {
      delete errors[prop]
    }
  }

  // Form management
  const resetFields = () => {
    // Reset model to initial values
    Object.keys(model.value).forEach(key => {
      if (key in initialValues) {
        model.value[key] = initialValues[key]
      } else {
        delete model.value[key]
      }
    })

    // Add any missing initial values
    Object.keys(initialValues).forEach(key => {
      if (!(key in model.value)) {
        model.value[key] = initialValues[key]
      }
    })

    // Clear validation state
    clearValidate()
    
    // Clear touched state
    Object.keys(touched).forEach(key => {
      touched[key] = false
    })

    if (onReset) {
      onReset()
    }
  }

  const clearValidate = (props?: string | string[]) => {
    if (!props) {
      // Clear all errors
      Object.keys(errors).forEach(prop => {
        delete errors[prop]
      })
    } else {
      // Clear specific field errors
      const propsArray = Array.isArray(props) ? props : [props]
      propsArray.forEach(prop => {
        if (errors[prop]) {
          delete errors[prop]
        }
      })
    }
  }

  const submit = async (onValid?: (model: Record<string, any>) => void | Promise<void>): Promise<boolean> => {
    loading.value = true

    try {
      const result = await validate()
      
      if (result.valid) {
        // Execute custom validation callback
        if (onValid) {
          await onValid(model.value)
        }
        
        // Execute form submit callback
        if (onSubmit) {
          await onSubmit(model.value)
        }
        
        return true
      }
      
      return false
    } catch (error) {
      console.error('Form submission error:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  const reset = () => {
    resetFields()
  }

  // Computed properties
  const hasErrors = computed(() => Object.keys(errors).length > 0)
  const isFieldTouched = (prop: string) => touched[prop] || false

  return {
    model,
    rules,
    loading,
    errors: computed(() => errors),
    validate,
    validateField,
    resetFields,
    clearValidate,
    setFieldValue,
    getFieldValue,
    setFieldError,
    clearFieldError,
    submit,
    reset,
    hasErrors,
    isFieldTouched
  }
}

// Validation rule executor
async function validateRule(
  rule: FormRule, 
  value: any, 
  prop: string, 
  model: Record<string, any>
): Promise<boolean> {
  // Required validation
  if (rule.required) {
    if (value === undefined || value === null || value === '') {
      return false
    }
    if (Array.isArray(value) && value.length === 0) {
      return false
    }
  }

  // Skip other validations if value is empty and not required
  if (!rule.required && (value === undefined || value === null || value === '')) {
    return true
  }

  // Type validation
  if (rule.type) {
    if (!validateType(value, rule.type)) {
      return false
    }
  }

  // Length validation
  if (rule.min !== undefined) {
    const length = getValueLength(value)
    if (length < rule.min) {
      return false
    }
  }

  if (rule.max !== undefined) {
    const length = getValueLength(value)
    if (length > rule.max) {
      return false
    }
  }

  if (rule.len !== undefined) {
    const length = getValueLength(value)
    if (length !== rule.len) {
      return false
    }
  }

  // Pattern validation
  if (rule.pattern) {
    if (!rule.pattern.test(String(value))) {
      return false
    }
  }

  // Enum validation
  if (rule.enum && Array.isArray(rule.enum)) {
    if (!rule.enum.includes(value)) {
      return false
    }
  }

  // Transform value if needed
  let transformedValue = value
  if (rule.transform) {
    transformedValue = rule.transform(value)
  }

  // Custom validator
  if (rule.validator) {
    return new Promise((resolve) => {
      rule.validator!(rule, transformedValue, (error) => {
        resolve(!error)
      })
    })
  }

  // Async validator
  if (rule.asyncValidator) {
    return new Promise((resolve) => {
      rule.asyncValidator!(rule, transformedValue, (error) => {
        resolve(!error)
      }, model, {})
    })
  }

  return true
}

// Type validation helper
function validateType(value: any, type: string): boolean {
  switch (type) {
    case 'string':
      return typeof value === 'string'
    case 'number':
      return typeof value === 'number' && !isNaN(value)
    case 'boolean':
      return typeof value === 'boolean'
    case 'array':
      return Array.isArray(value)
    case 'object':
      return value !== null && typeof value === 'object' && !Array.isArray(value)
    case 'date':
      return value instanceof Date || !isNaN(Date.parse(value))
    case 'url':
      try {
        new URL(value)
        return true
      } catch {
        return false
      }
    case 'email':
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      return emailRegex.test(value)
    case 'integer':
      return Number.isInteger(Number(value))
    case 'float':
      return typeof value === 'number' && !isNaN(value)
    default:
      return true
  }
}

// Value length helper
function getValueLength(value: any): number {
  if (typeof value === 'string' || Array.isArray(value)) {
    return value.length
  }
  if (typeof value === 'number') {
    return String(value).length
  }
  return 0
}

// Utility function to create common validation rules
export const createFormRules = {
  required: (message?: string): FormRule => ({
    required: true,
    message: message || '此字段为必填项'
  }),

  minLength: (min: number, message?: string): FormRule => ({
    min,
    message: message || `最少输入 ${min} 个字符`
  }),

  maxLength: (max: number, message?: string): FormRule => ({
    max,
    message: message || `最多输入 ${max} 个字符`
  }),

  email: (message?: string): FormRule => ({
    type: 'email',
    message: message || '请输入有效的邮箱地址'
  }),

  phone: (message?: string): FormRule => ({
    pattern: /^1[3-9]\d{9}$/,
    message: message || '请输入有效的手机号码'
  }),

  url: (message?: string): FormRule => ({
    type: 'url',
    message: message || '请输入有效的URL地址'
  }),

  number: (message?: string): FormRule => ({
    type: 'number',
    message: message || '请输入数字'
  }),

  integer: (message?: string): FormRule => ({
    type: 'integer',
    message: message || '请输入整数'
  }),

  range: (min: number, max: number, message?: string): FormRule => ({
    validator: (rule, value, callback) => {
      const num = Number(value)
      if (isNaN(num) || num < min || num > max) {
        callback(new Error(message || `请输入 ${min} 到 ${max} 之间的数字`))
      } else {
        callback()
      }
    }
  })
}

export default useForm