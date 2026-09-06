<!-- Glanceability(즉시 파악)를 위한 대시보드 KPI 카드: 색상+숫자+아이콘 -->
<template>
  <div class="surface-card flex items-center gap-4 rounded-lg p-4 shadow-sm">
    <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-full" :class="toneClasses.iconBg">
      <component :is="icon" class="h-5 w-5" :class="toneClasses.iconColor" />
    </div>
    <div class="min-w-0">
      <div class="text-xs font-medium text-slate-500 dark:text-slate-400">{{ label }}</div>
      <div class="text-2xl font-bold tabular-nums" :class="toneClasses.valueColor">{{ value }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    label: string
    value: string | number
    icon: any
    tone?: 'neutral' | 'pass' | 'fail' | 'warn' | 'info'
  }>(),
  { tone: 'neutral' },
)

const toneMap: Record<string, { iconBg: string; iconColor: string; valueColor: string }> = {
  neutral: {
    iconBg: 'bg-slate-100 dark:bg-elevation-3',
    iconColor: 'text-slate-500 dark:text-slate-300',
    valueColor: 'text-slate-800 dark:text-slate-100',
  },
  pass: {
    iconBg: 'bg-green-100 dark:bg-green-900/30',
    iconColor: 'text-green-600 dark:text-green-400',
    valueColor: 'text-green-700 dark:text-green-400',
  },
  fail: {
    iconBg: 'bg-red-100 dark:bg-red-900/30',
    iconColor: 'text-red-600 dark:text-red-400',
    valueColor: 'text-red-700 dark:text-red-400',
  },
  warn: {
    iconBg: 'bg-amber-100 dark:bg-amber-900/30',
    iconColor: 'text-amber-600 dark:text-amber-400',
    valueColor: 'text-amber-700 dark:text-amber-400',
  },
  info: {
    iconBg: 'bg-blue-100 dark:bg-blue-900/30',
    iconColor: 'text-blue-600 dark:text-blue-400',
    valueColor: 'text-blue-700 dark:text-blue-400',
  },
}

const toneClasses = computed(() => toneMap[props.tone] || toneMap.neutral)
</script>
