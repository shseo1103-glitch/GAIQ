<!-- 슬라이더+숫자입력 병행. 실측 데이터 범위(min~max) 밖 입력 시 하드블록 대신 경고 배지 -->
<template>
  <div>
    <div class="mb-1 flex items-center justify-between">
      <label class="label !mb-0">{{ label }} <span class="text-slate-400">({{ unit }})</span></label>
      <span v-if="outOfRange" class="badge badge-warn">
        <AlertTriangle class="h-3 w-3" />
        범위 외 입력 (실측 {{ min }}~{{ max }})
      </span>
    </div>
    <div class="flex items-center gap-3">
      <input
        type="range"
        :min="sliderMin"
        :max="sliderMax"
        :step="step"
        :value="modelValue"
        class="h-1.5 flex-1 cursor-pointer accent-cyan-600"
        @input="onSlide"
      />
      <input
        type="number"
        :step="step"
        :value="modelValue"
        class="input w-28 !py-1 text-right"
        @input="onNumber"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { AlertTriangle } from 'lucide-vue-next'

const props = defineProps<{
  modelValue: number
  label: string
  unit: string
  min: number
  max: number
  step?: number
}>()
const emit = defineEmits<{ (e: 'update:modelValue', v: number): void }>()

const step = computed(() => props.step ?? (props.max - props.min > 50 ? 1 : 0.1))
// 슬라이더 자체는 실측범위의 20% 여유를 두어 외삽 입력도 슬라이더로 가능하게 함
const sliderMin = computed(() => props.min - (props.max - props.min) * 0.2)
const sliderMax = computed(() => props.max + (props.max - props.min) * 0.2)

const outOfRange = computed(() => props.modelValue < props.min || props.modelValue > props.max)

function onSlide(e: Event) {
  emit('update:modelValue', Number((e.target as HTMLInputElement).value))
}
function onNumber(e: Event) {
  emit('update:modelValue', Number((e.target as HTMLInputElement).value))
}
</script>
