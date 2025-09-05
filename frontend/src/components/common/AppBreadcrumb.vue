<template>
  <div class="app-breadcrumb" :class="{ 'is-mobile': isMobile }">
    <el-breadcrumb :separator="separator" class="breadcrumb-container">
      <!-- Home breadcrumb -->
      <el-breadcrumb-item :to="homePath" v-if="showHome">
        <el-icon class="breadcrumb-icon">
          <HomeFilled />
        </el-icon>
        <span class="breadcrumb-text">{{ homeText }}</span>
      </el-breadcrumb-item>
      
      <!-- Dynamic breadcrumbs -->
      <el-breadcrumb-item
        v-for="(item, index) in breadcrumbs"
        :key="item.path || index"
        :to="item.to || item.path"
        :class="{ 'is-last': index === breadcrumbs.length - 1 }"
      >
        <el-icon v-if="item.icon" class="breadcrumb-icon">
          <component :is="item.icon" />
        </el-icon>
        <span class="breadcrumb-text" :title="item.title">
          {{ item.title }}
        </span>
      </el-breadcrumb-item>
    </el-breadcrumb>
    
    <!-- Actions slot -->
    <div class="breadcrumb-actions" v-if="$slots.actions">
      <slot name="actions"></slot>
    </div>
  </div>
</template>

<script setup>
import { computed, watch, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useLayoutStore } from '@/stores/layout'
import { HomeFilled } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  separator: {
    type: String,
    default: '/'
  },
  showHome: {
    type: Boolean,
    default: true
  },
  homePath: {
    type: String,
    default: '/dashboard'
  },
  homeText: {
    type: String,
    default: '首页'
  },
  customBreadcrumbs: {
    type: Array,
    default: () => []
  },
  maxItems: {
    type: Number,
    default: 5
  },
  showIcons: {
    type: Boolean,
    default: true
  }
})

const route = useRoute()
const router = useRouter()
const layoutStore = useLayoutStore()

// Computed
const isMobile = computed(() => layoutStore.isMobile)

const breadcrumbs = computed(() => {
  // Use custom breadcrumbs if provided
  if (props.customBreadcrumbs.length > 0) {
    return processBreadcrumbs(props.customBreadcrumbs)
  }

  // Generate breadcrumbs from route
  const matched = route.matched.filter(record => {
    return record.meta && record.meta.title && !record.meta.hideBreadcrumb
  })

  const breadcrumbItems = matched.map((record, index) => {
    const isLast = index === matched.length - 1
    const item = {
      title: record.meta.title,
      path: record.path,
      icon: props.showIcons ? record.meta.icon : null,
      to: isLast ? null : record.path, // Don't make the last item clickable
      meta: record.meta
    }

    // Handle dynamic route parameters
    if (record.path.includes(':')) {
      item.path = route.path
      item.to = isLast ? null : route.path
    }

    return item
  })

  return processBreadcrumbs(breadcrumbItems)
})

// Process breadcrumbs (truncate if too many)
function processBreadcrumbs(items) {
  if (items.length <= props.maxItems) {
    return items
  }

  // Keep first item, last 2 items, and add ellipsis
  const first = items[0]
  const lastTwo = items.slice(-2)
  const ellipsis = { title: '...', icon: null, isEllipsis: true }
  
  return [first, ellipsis, ...lastTwo]
}

// Get breadcrumb title from route meta or generate from path
function getBreadcrumbTitle(routeRecord) {
  if (routeRecord.meta?.breadcrumbTitle) {
    return routeRecord.meta.breadcrumbTitle
  }
  
  if (routeRecord.meta?.title) {
    return routeRecord.meta.title
  }
  
  // Generate title from path
  const pathSegment = routeRecord.path.split('/').pop()
  return pathSegment.charAt(0).toUpperCase() + pathSegment.slice(1)
}

// Handle breadcrumb click
function handleBreadcrumbClick(item) {
  if (item.isEllipsis) {
    // Show all breadcrumbs in a dropdown or modal
    showAllBreadcrumbs()
    return
  }
  
  if (item.to && item.to !== route.path) {
    router.push(item.to)
  }
}

// Show all breadcrumbs (for ellipsis click)
function showAllBreadcrumbs() {
  // This could open a dropdown or modal with all breadcrumbs
  console.log('Show all breadcrumbs')
}

// Emit events
const emit = defineEmits(['breadcrumb-click'])

watch(() => route.path, () => {
  emit('breadcrumb-click', breadcrumbs.value)
})
</script>

<style scoped>
.app-breadcrumb {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0;
  min-height: 32px;
}

.breadcrumb-container {
  flex: 1;
  min-width: 0; /* Allow flex item to shrink */
}

.breadcrumb-container :deep(.el-breadcrumb__inner) {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #666;
  transition: color 0.3s;
  text-decoration: none;
}

.breadcrumb-container :deep(.el-breadcrumb__inner:hover) {
  color: #1890ff;
}

.breadcrumb-container :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #333;
  font-weight: 500;
  cursor: default;
}

.breadcrumb-container :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner:hover) {
  color: #333;
}

.breadcrumb-icon {
  font-size: 14px;
}

.breadcrumb-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 150px;
}

.breadcrumb-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

/* Mobile styles */
.app-breadcrumb.is-mobile {
  padding: 0;
}

.app-breadcrumb.is-mobile .breadcrumb-text {
  max-width: 100px;
}

.app-breadcrumb.is-mobile .breadcrumb-icon {
  font-size: 12px;
}

.app-breadcrumb.is-mobile .breadcrumb-container :deep(.el-breadcrumb__separator) {
  margin: 0 4px;
}

/* Dark theme */
:deep(.dark) .breadcrumb-container .el-breadcrumb__inner {
  color: #999;
}

:deep(.dark) .breadcrumb-container .el-breadcrumb__inner:hover {
  color: #69c0ff;
}

:deep(.dark) .breadcrumb-container .el-breadcrumb__item:last-child .el-breadcrumb__inner {
  color: #ffffff;
}

:deep(.dark) .breadcrumb-container .el-breadcrumb__item:last-child .el-breadcrumb__inner:hover {
  color: #ffffff;
}

:deep(.dark) .breadcrumb-container .el-breadcrumb__separator {
  color: #666;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .breadcrumb-text {
    max-width: 80px;
  }
}

@media (max-width: 480px) {
  .app-breadcrumb {
    gap: 8px;
  }
  
  .breadcrumb-text {
    max-width: 60px;
  }
  
  .breadcrumb-container :deep(.el-breadcrumb__separator) {
    margin: 0 2px;
  }
}
</style>