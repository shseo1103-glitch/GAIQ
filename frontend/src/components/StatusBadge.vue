<!--
  이중 인코딩(색상+아이콘) 상태 배지. High-Performance HMI 원칙에 따라
  상태색은 여기서만 사용하고 장식 목적으로는 쓰지 않는다.
-->
<template>
  <span class="badge" :class="badgeClass">
    <component :is="icon" class="h-3 w-3" />
    {{ label }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { CheckCircle2, XCircle, AlertTriangle, Clock, PlayCircle, Ban, HelpCircle } from 'lucide-vue-next'

const props = defineProps<{ status: string }>()

const config: Record<string, { label: string; cls: string; icon: any }> = {
  PASS: { label: 'PASS', cls: 'badge-pass', icon: CheckCircle2 },
  FAIL: { label: 'FAIL', cls: 'badge-fail', icon: XCircle },
  PLANNED: { label: '계획', cls: 'badge-neutral', icon: Clock },
  RUNNING: { label: '진행중', cls: 'badge-info', icon: PlayCircle },
  COMPLETED: { label: '완료', cls: 'badge-pass', icon: CheckCircle2 },
  FAILED: { label: '실패', cls: 'badge-fail', icon: XCircle },
  ABORTED: { label: '중단', cls: 'badge-warn', icon: Ban },
  ACTIVE: { label: '활성', cls: 'badge-pass', icon: CheckCircle2 },
  SUSPENDED: { label: '정지', cls: 'badge-warn', icon: AlertTriangle },
  PENDING: { label: '대기', cls: 'badge-warn', icon: Clock },
  A: { label: 'A등급', cls: 'badge-pass', icon: CheckCircle2 },
  B: { label: 'B등급', cls: 'badge-warn', icon: AlertTriangle },
  C: { label: 'C등급', cls: 'badge-fail', icon: XCircle },
  HIGH: { label: 'HIGH', cls: 'badge-info', icon: CheckCircle2 },
  MEDIUM: { label: 'MEDIUM', cls: 'badge-warn', icon: AlertTriangle },
  LOW: { label: 'LOW', cls: 'badge-fail', icon: AlertTriangle },
  DATA_INSUFFICIENT: { label: '데이터부족', cls: 'badge-fail', icon: XCircle },
}

const badgeClass = computed(() => config[props.status]?.cls || 'badge-neutral')
const label = computed(() => config[props.status]?.label || props.status)
const icon = computed(() => config[props.status]?.icon || HelpCircle)
</script>
