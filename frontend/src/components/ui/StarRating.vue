<template>
  <div class="modal-rating-stars">
    <button v-for="i in 5" :key="i"
      class="modal-star-btn"
      :class="{ selected: modelValue >= i * 2 }"
      @click="!readonly && $emit('update:modelValue', modelValue === i * 2 ? 0 : i * 2)"
      :disabled="readonly"
    >★</button>
    <span class="modal-rating-score">{{ modelValue || 0 }}/10</span>
  </div>
</template>

<script setup>
defineProps({
  modelValue: { type: Number, default: 0 },
  readonly: { type: Boolean, default: false },
})
defineEmits(['update:modelValue'])
</script>
