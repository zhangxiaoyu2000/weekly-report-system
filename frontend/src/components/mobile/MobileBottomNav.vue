<template>
  <div class="mobile-bottom-nav" :class="navClasses">
    <div class="nav-container">
      <div
        v-for="(item, index) in navItems"
        :key="item.path || index"
        class="nav-item"
        :class="{
          'is-active': isActive(item),
          'is-disabled': item.disabled,
          'has-badge': item.badge
        }"
        @click="handleClick(item)"
      >
        <!-- Icon -->
        <div class="nav-icon-wrapper">
          <el-icon class="nav-icon" :class="{ 'bounce': bounceAnimation && isActive(item) }">
            <component :is="item.icon" />
          </el-icon>
          
          <!-- Badge -->
          <el-badge
            v-if="item.badge"
            :value="item.badge"
            :type="item.badgeType || 'danger'"
            class="nav-badge"
            :class="{ 'is-dot': item.badgeDot }"
            :is-dot="item.badgeDot"
          />
        </div>

        <!-- Label -->
        <span class="nav-label" v-if="showLabels">{{ item.label }}</span>

        <!-- Active indicator -->
        <div class="active-indicator" v-if="showIndicator && isActive(item)"></div>
      </div>
    </div>

    <!-- Floating Action Button -->
    <div 
      v-if="fabItem"
      class="fab-container"
      :style="{ right: fabPosition.right, bottom: fabPosition.bottom }"
    >
      <el-button
        :type="fabItem.type || 'primary'"
        :size="fabItem.size || 'large'"
        circle
        class="fab-button"
        @click="handleClick(fabItem)"
      >
        <el-icon>
          <component :is="fabItem.icon" />
        </el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const props = defineProps({
  navItems: {
    type: Array,
    required: true
  },
  showLabels: {
    type: Boolean,
    default: true
  },
  showIndicator: {
    type: Boolean,
    default: false
  },
  variant: {
    type: String,
    default: 'default', // default, compact, minimal
    validator: (value) => ['default', 'compact', 'minimal'].includes(value)
  },
  theme: {
    type: String,
    default: 'light', // light, dark, auto
    validator: (value) => ['light', 'dark', 'auto'].includes(value)
  },
  bounceAnimation: {
    type: Boolean,
    default: true
  },
  fabItem: {
    type: Object,
    default: null
  },
  fabPosition: {
    type: Object,
    default: () => ({ right: '16px', bottom: '80px' })
  },
  hideOnScroll: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['item-click'])

const route = useRoute()
const router = useRouter()
const isVisible = ref(true)
const lastScrollY = ref(0)

// Computed
const navClasses = computed(() => ({
  [`nav-${props.variant}`]: true,
  [`nav-${props.theme}`]: true,
  'is-hidden': props.hideOnScroll && !isVisible.value,
  'no-labels': !props.showLabels
}))

const isActive = (item) => {
  if (!item.path) return false
  
  if (item.exact) {
    return route.path === item.path
  }
  
  // Check if current route starts with item path
  return route.path.startsWith(item.path)
}

// Methods
const handleClick = (item) => {
  if (item.disabled) return

  emit('item-click', item)

  if (item.handler) {
    item.handler()
  } else if (item.path) {
    router.push(item.path)
  }
}

// Handle scroll visibility
const handleScroll = () => {
  if (!props.hideOnScroll) return

  const currentScrollY = window.scrollY
  
  if (currentScrollY > lastScrollY.value && currentScrollY > 100) {
    // Scrolling down - hide nav
    isVisible.value = false
  } else {
    // Scrolling up - show nav
    isVisible.value = true
  }
  
  lastScrollY.value = currentScrollY
}

// Setup scroll listener if needed
watch(() => props.hideOnScroll, (newVal) => {
  if (newVal) {
    window.addEventListener('scroll', handleScroll, { passive: true })
  } else {
    window.removeEventListener('scroll', handleScroll)
    isVisible.value = true
  }
}, { immediate: true })
</script>

<style scoped>
.mobile-bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #ffffff;
  border-top: 1px solid #e8e8e8;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  transform: translateY(0);
  transition: transform 0.3s ease;
}

.mobile-bottom-nav.is-hidden {
  transform: translateY(100%);
}

.nav-container {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 8px 0;
  min-height: 60px;
  max-width: 100%;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  padding: 6px 4px;
  cursor: pointer;
  position: relative;
  transition: all 0.3s ease;
  min-height: 48px;
}

.nav-item.is-disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.nav-item:not(.is-disabled):active {
  transform: scale(0.95);
}

.nav-icon-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 2px;
}

.nav-icon {
  font-size: 20px;
  color: #666;
  transition: all 0.3s ease;
}

.nav-item.is-active .nav-icon {
  color: #1890ff;
  transform: scale(1.1);
}

.nav-icon.bounce {
  animation: bounce 0.6s ease;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: scale(1.1) translateY(0);
  }
  40% {
    transform: scale(1.1) translateY(-4px);
  }
  60% {
    transform: scale(1.1) translateY(-2px);
  }
}

.nav-label {
  font-size: 10px;
  color: #999;
  text-align: center;
  line-height: 1.2;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.3s ease;
  margin-top: 2px;
}

.nav-item.is-active .nav-label {
  color: #1890ff;
  font-weight: 500;
}

.nav-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  z-index: 1;
}

.active-indicator {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 3px;
  background-color: #1890ff;
  border-radius: 0 0 2px 2px;
}

/* Variants */
.nav-compact {
  min-height: 50px;
}

.nav-compact .nav-container {
  min-height: 50px;
  padding: 4px 0;
}

.nav-compact .nav-item {
  padding: 4px 2px;
  min-height: 42px;
}

.nav-compact .nav-icon {
  font-size: 18px;
}

.nav-compact .nav-label {
  font-size: 9px;
}

.nav-minimal {
  background-color: transparent;
  border-top: none;
  box-shadow: none;
}

.nav-minimal .nav-item.is-active {
  background-color: rgba(24, 144, 255, 0.1);
  border-radius: 12px;
}

/* Themes */
.nav-dark {
  background-color: #1f1f1f;
  border-top-color: #303030;
  color: #ffffff;
}

.nav-dark .nav-icon {
  color: #cccccc;
}

.nav-dark .nav-item.is-active .nav-icon {
  color: #69c0ff;
}

.nav-dark .nav-label {
  color: #999999;
}

.nav-dark .nav-item.is-active .nav-label {
  color: #69c0ff;
}

.nav-dark .active-indicator {
  background-color: #69c0ff;
}

/* No labels variant */
.no-labels .nav-container {
  min-height: 48px;
}

.no-labels .nav-item {
  min-height: 36px;
}

.no-labels .nav-icon-wrapper {
  margin-bottom: 0;
}

/* Floating Action Button */
.fab-container {
  position: fixed;
  z-index: 1001;
}

.fab-button {
  width: 56px !important;
  height: 56px !important;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.fab-button:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

.fab-button:active {
  transform: scale(0.95);
}

/* Safe area support for devices with notches */
@supports (padding-bottom: constant(safe-area-inset-bottom)) {
  .mobile-bottom-nav {
    padding-bottom: constant(safe-area-inset-bottom);
  }
}

@supports (padding-bottom: env(safe-area-inset-bottom)) {
  .mobile-bottom-nav {
    padding-bottom: env(safe-area-inset-bottom);
  }
}

/* Landscape mode adjustments */
@media (orientation: landscape) and (max-height: 500px) {
  .mobile-bottom-nav {
    min-height: 40px;
  }
  
  .nav-container {
    min-height: 40px;
    padding: 2px 0;
  }
  
  .nav-item {
    min-height: 36px;
    padding: 2px 4px;
  }
  
  .nav-icon {
    font-size: 18px;
  }
  
  .nav-label {
    font-size: 9px;
  }
}

/* High contrast mode */
@media (prefers-contrast: high) {
  .mobile-bottom-nav {
    border-top-width: 2px;
  }
  
  .nav-item.is-active {
    outline: 2px solid #1890ff;
    outline-offset: -2px;
  }
}

/* Reduced motion */
@media (prefers-reduced-motion: reduce) {
  .nav-item,
  .nav-icon,
  .nav-label,
  .fab-button {
    transition: none;
  }
  
  .nav-icon.bounce {
    animation: none;
  }
  
  .nav-item:not(.is-disabled):active {
    transform: none;
  }
}
</style>