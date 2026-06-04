<template>
  <div class="review-card">
    <div class="review-poster">
      <img v-if="review.posterPath" :src="`${imgBase}w500${review.posterPath}`" :alt="review.title">
      <div v-else style="width:100%;height:100%;display:flex;align-items:center;justify-content:center;font-size:28px;">🎬</div>
    </div>
    <div class="review-content">
      <div class="review-header">
        <div class="review-title">{{ review.title }} <span class="review-year">{{ year }}</span></div>
        <div class="review-actions">
          <button class="review-btn" @click="$emit('edit', review)" title="编辑">✎</button>
          <button class="review-btn delete" @click="$emit('delete', review)" title="删除">✕</button>
        </div>
      </div>
      <div class="review-rating">
        <span class="review-rating-stars">{{ stars }}</span>
        <span class="review-rating-value">{{ review.userRating }}/10</span>
      </div>
      <div class="review-text">{{ review.content }}</div>
      <div class="review-time">{{ review.updatedAt || review.createdAt }}</div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  review: { type: Object, required: true },
})
defineEmits(['edit', 'delete'])

const imgBase = 'https://image.tmdb.org/t/p/'

const year = computed(() => props.review.releaseDate ? props.review.releaseDate.slice(0, 4) : '')
const stars = computed(() => {
  const starCount = props.review.userRating ? Math.round(props.review.userRating / 2) : 0
  return '★'.repeat(starCount) + '☆'.repeat(5 - starCount)
})
</script>

<style scoped>
.review-card {
  background: var(--bg-card);
  backdrop-filter: blur(12px);
  border: 1px solid var(--frost-border);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  gap: 20px;
  transition: all 0.3s ease;
}
.review-card:hover {
  box-shadow: var(--shadow-md);
}
.review-poster {
  width: 80px;
  height: 112px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: linear-gradient(135deg, #e8e4e0 0%, #d8d4d0 100%);
}
.review-poster img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.review-content {
  flex: 1;
  min-width: 0;
}
.review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}
.review-title {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 600;
  color: var(--ink);
}
.review-year {
  font-size: 14px;
  color: var(--ink-muted);
  font-weight: 400;
}
.review-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
.review-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: 1px solid var(--frost-border);
  background: var(--frost);
  color: var(--ink-secondary);
  font-size: 16px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}
.review-btn:hover {
  background: var(--frost-hover);
  color: var(--ink);
}
.review-btn.delete:hover {
  background: rgba(220, 53, 69, 0.1);
  border-color: rgba(220, 53, 69, 0.2);
  color: #dc3545;
}
.review-rating {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
.review-rating-stars {
  color: #f59e0b;
  font-size: 16px;
  letter-spacing: 2px;
}
.review-rating-value {
  font-size: 14px;
  color: var(--ink-muted);
}
.review-text {
  font-size: 15px;
  line-height: 1.8;
  color: var(--ink-secondary);
  white-space: pre-wrap;
}
.review-time {
  margin-top: 12px;
  font-size: 12px;
  color: var(--ink-muted);
}
</style>
