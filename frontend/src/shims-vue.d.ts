/* eslint-disable */
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

// Global component type augmentation for Element Plus auto-import
declare module '@vue/runtime-core' {
  export interface GlobalComponents {
    ElButton: typeof import('element-plus')['ElButton']
    ElInput: typeof import('element-plus')['ElInput']
    ElForm: typeof import('element-plus')['ElForm']
    ElFormItem: typeof import('element-plus')['ElFormItem']
    ElTable: typeof import('element-plus')['ElTable']
    ElTableColumn: typeof import('element-plus')['ElTableColumn']
    ElDialog: typeof import('element-plus')['ElDialog']
    ElLoading: typeof import('element-plus')['ElLoading']
    ElCard: typeof import('element-plus')['ElCard']
    ElMenu: typeof import('element-plus')['ElMenu']
    ElMenuItem: typeof import('element-plus')['ElMenuItem']
    ElSubMenu: typeof import('element-plus')['ElSubMenu']
    ElHeader: typeof import('element-plus')['ElHeader']
    ElMain: typeof import('element-plus')['ElMain']
    ElAside: typeof import('element-plus')['ElAside']
    ElContainer: typeof import('element-plus')['ElContainer']
    ElRow: typeof import('element-plus')['ElRow']
    ElCol: typeof import('element-plus')['ElCol']
    ElPagination: typeof import('element-plus')['ElPagination']
    ElSelect: typeof import('element-plus')['ElSelect']
    ElOption: typeof import('element-plus')['ElOption']
    ElDatePicker: typeof import('element-plus')['ElDatePicker']
    ElTextarea: typeof import('element-plus')['ElTextarea']
    ElSwitch: typeof import('element-plus')['ElSwitch']
    ElCheckbox: typeof import('element-plus')['ElCheckbox']
    ElRadio: typeof import('element-plus')['ElRadio']
    ElRadioGroup: typeof import('element-plus')['ElRadioGroup']
    ElTag: typeof import('element-plus')['ElTag']
    ElBadge: typeof import('element-plus')['ElBadge']
    ElAlert: typeof import('element-plus')['ElAlert']
    ElMessage: typeof import('element-plus')['ElMessage']
    ElMessageBox: typeof import('element-plus')['ElMessageBox']
    ElDropdown: typeof import('element-plus')['ElDropdown']
    ElDropdownMenu: typeof import('element-plus')['ElDropdownMenu']
    ElDropdownItem: typeof import('element-plus')['ElDropdownItem']
    ElIcon: typeof import('element-plus')['ElIcon']
    ElTooltip: typeof import('element-plus')['ElTooltip']
    ElBreadcrumb: typeof import('element-plus')['ElBreadcrumb']
    ElBreadcrumbItem: typeof import('element-plus')['ElBreadcrumbItem']
    ElTabs: typeof import('element-plus')['ElTabs']
    ElTabPane: typeof import('element-plus')['ElTabPane']
  }
}

export {}