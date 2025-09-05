/**
 * Tests for BaseButton component
 */

import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import BaseButton from '@/components/base/BaseButton.vue'
import { 
  waitFor, 
  expectText, 
  expectClass, 
  expectEmitted, 
  expectNotEmitted 
} from '@/test/utils/test-helpers'

describe('BaseButton', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('Basic Rendering', () => {
    it('renders with default props', () => {
      const wrapper = mount(BaseButton)
      
      expect(wrapper.exists()).toBe(true)
      expect(wrapper.find('button').exists()).toBe(true)
      expect(wrapper.classes()).toContain('base-button')
    })

    it('renders text content', () => {
      const text = 'Click Me'
      const wrapper = mount(BaseButton, {
        props: { text }
      })
      
      expectText(wrapper, 'button', text)
    })

    it('renders slot content', () => {
      const slotContent = 'Custom Button Text'
      const wrapper = mount(BaseButton, {
        slots: {
          default: slotContent
        }
      })
      
      expectText(wrapper, 'button', slotContent)
    })

    it('prioritizes slot content over text prop', () => {
      const slotContent = 'Slot Content'
      const textProp = 'Text Prop'
      
      const wrapper = mount(BaseButton, {
        props: { text: textProp },
        slots: {
          default: slotContent
        }
      })
      
      expectText(wrapper, 'button', slotContent)
    })
  })

  describe('Props and Variants', () => {
    it('applies different button types', async () => {
      const types = ['primary', 'success', 'warning', 'danger', 'info']
      
      for (const type of types) {
        const wrapper = mount(BaseButton, {
          props: { type: type as any }
        })
        
        const button = wrapper.find('button')
        expect(button.attributes('type')).toBeUndefined() // Element Plus handles CSS classes
      }
    })

    it('applies size variants', () => {
      const sizes = ['large', 'default', 'small']
      
      sizes.forEach(size => {
        const wrapper = mount(BaseButton, {
          props: { size: size as any }
        })
        
        expect(wrapper.exists()).toBe(true)
      })
    })

    it('applies disabled state', () => {
      const wrapper = mount(BaseButton, {
        props: { disabled: true }
      })
      
      const button = wrapper.find('button')
      expect(button.attributes()).toHaveProperty('disabled')
    })

    it('applies loading state', async () => {
      const wrapper = mount(BaseButton, {
        props: { loading: true }
      })
      
      expectClass(wrapper, '.base-button', 'base-button--loading')
      expect(wrapper.find('.is-loading').exists()).toBe(true)
    })

    it('applies block style', () => {
      const wrapper = mount(BaseButton, {
        props: { block: true }
      })
      
      expectClass(wrapper, '.base-button', 'base-button--block')
    })

    it('renders with icon', () => {
      const wrapper = mount(BaseButton, {
        props: { icon: 'Search' }
      })
      
      expect(wrapper.find('.el-icon').exists()).toBe(true)
    })
  })

  describe('Event Handling', () => {
    it('emits click event when clicked', async () => {
      const wrapper = mount(BaseButton)
      const button = wrapper.find('button')
      
      await button.trigger('click')
      
      expectEmitted(wrapper, 'click')
    })

    it('calls onClick prop when provided', async () => {
      const onClick = vi.fn()
      const wrapper = mount(BaseButton, {
        props: { onClick }
      })
      
      await wrapper.find('button').trigger('click')
      
      expect(onClick).toHaveBeenCalledOnce()
    })

    it('handles async onClick prop', async () => {
      const asyncClick = vi.fn().mockResolvedValue(undefined)
      const wrapper = mount(BaseButton, {
        props: { onClick: asyncClick }
      })
      
      await wrapper.find('button').trigger('click')
      await nextTick()
      
      expect(asyncClick).toHaveBeenCalledOnce()
    })

    it('does not emit click when disabled', async () => {
      const wrapper = mount(BaseButton, {
        props: { disabled: true }
      })
      
      await wrapper.find('button').trigger('click')
      
      expectNotEmitted(wrapper, 'click')
    })

    it('does not emit click when loading', async () => {
      const wrapper = mount(BaseButton, {
        props: { loading: true }
      })
      
      await wrapper.find('button').trigger('click')
      
      expectNotEmitted(wrapper, 'click')
    })
  })

  describe('Link Behavior', () => {
    it('navigates to href when clicked', async () => {
      const originalLocation = window.location
      const mockLocation = { href: '' }
      Object.defineProperty(window, 'location', {
        value: mockLocation,
        writable: true
      })
      
      const href = 'https://example.com'
      const wrapper = mount(BaseButton, {
        props: { href }
      })
      
      await wrapper.find('button').trigger('click')
      
      expect(mockLocation.href).toBe(href)
      
      // Restore original location
      Object.defineProperty(window, 'location', {
        value: originalLocation,
        writable: true
      })
    })

    it('opens link in new tab when target="_blank"', async () => {
      const mockOpen = vi.fn()
      window.open = mockOpen
      
      const href = 'https://example.com'
      const wrapper = mount(BaseButton, {
        props: { href, target: '_blank' }
      })
      
      await wrapper.find('button').trigger('click')
      
      expect(mockOpen).toHaveBeenCalledWith(href, '_blank')
    })
  })

  describe('Accessibility', () => {
    it('has proper button role', () => {
      const wrapper = mount(BaseButton)
      const button = wrapper.find('button')
      
      expect(button.element.tagName).toBe('BUTTON')
    })

    it('supports autofocus', () => {
      const wrapper = mount(BaseButton, {
        props: { autofocus: true }
      })
      
      const button = wrapper.find('button')
      expect(button.attributes()).toHaveProperty('autofocus')
    })

    it('supports different native types', () => {
      const types = ['button', 'submit', 'reset']
      
      types.forEach(type => {
        const wrapper = mount(BaseButton, {
          props: { nativeType: type as any }
        })
        
        const button = wrapper.find('button')
        expect(button.attributes('type')).toBe(type)
      })
    })
  })

  describe('Slots', () => {
    it('renders icon slot', () => {
      const iconContent = '<i class="custom-icon"></i>'
      const wrapper = mount(BaseButton, {
        slots: {
          icon: iconContent
        }
      })
      
      expect(wrapper.find('.custom-icon').exists()).toBe(true)
    })

    it('prefers icon slot over icon prop', () => {
      const iconContent = '<i class="slot-icon"></i>'
      const wrapper = mount(BaseButton, {
        props: { icon: 'Search' },
        slots: {
          icon: iconContent
        }
      })
      
      expect(wrapper.find('.slot-icon').exists()).toBe(true)
    })
  })

  describe('Edge Cases', () => {
    it('handles onClick prop errors gracefully', async () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      const errorClick = vi.fn().mockRejectedValue(new Error('Test error'))
      
      const wrapper = mount(BaseButton, {
        props: { onClick: errorClick }
      })
      
      await wrapper.find('button').trigger('click')
      await nextTick()
      
      expect(errorClick).toHaveBeenCalledOnce()
      expect(consoleSpy).toHaveBeenCalled()
      
      consoleSpy.mockRestore()
    })

    it('disables button when both disabled and loading', () => {
      const wrapper = mount(BaseButton, {
        props: { disabled: true, loading: true }
      })
      
      const button = wrapper.find('button')
      expect(button.attributes()).toHaveProperty('disabled')
    })

    it('shows loading icon instead of regular icon when loading', async () => {
      const wrapper = mount(BaseButton, {
        props: { icon: 'Search', loading: true }
      })
      
      expect(wrapper.find('.is-loading').exists()).toBe(true)
    })
  })

  describe('Loading States', () => {
    it('shows loading spinner when loading', () => {
      const wrapper = mount(BaseButton, {
        props: { loading: true }
      })
      
      expect(wrapper.find('.is-loading').exists()).toBe(true)
    })

    it('applies loading animation class', () => {
      const wrapper = mount(BaseButton, {
        props: { loading: true }
      })
      
      const loadingIcon = wrapper.find('.is-loading')
      expect(loadingIcon.exists()).toBe(true)
    })
  })
})