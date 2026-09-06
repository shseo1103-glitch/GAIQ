<template>
  <Teleport to="body">
    <div v-if="modelValue" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" @click.self="close">
      <div class="surface-modal max-h-[85vh] w-full overflow-y-auto rounded-lg shadow-xl" :class="widthClass">
        <div class="flex items-center justify-between border-b border-slate-200 px-5 py-3 dark:border-slate-700/60">
          <h3 class="text-base font-semibold">{{ title }}</h3>
          <button class="rounded-full p-1 text-slate-400 hover:bg-slate-100 dark:hover:bg-elevation-3" @click="close">
            <X class="h-4 w-4" />
          </button>
        </div>
        <div class="p-5">
          <slot />
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { X } from 'lucide-vue-next'

const props = withDefaults(defineProps<{ modelValue: boolean; title: string; width?: 'md' | 'lg' | 'xl' }>(), {
  width: 'md',
})
const emit = defineEmits<{ (e: 'update:modelValue', v: boolean): void }>()

const widthClass = props.width === 'xl' ? 'max-w-3xl' : props.width === 'lg' ? 'max-w-2xl' : 'max-w-lg'

function close() {
  emit('update:modelValue', false)
}
</script>
