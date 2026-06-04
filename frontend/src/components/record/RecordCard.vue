<template>
  <div class="record-card">
    <div class="record-poster">
      <img v-if="record.posterPath" :src="`${imgBase}w500${record.posterPath}`" :alt="record.title">
      <div v-else style="width:100%;height:100%;display:flex;align-items:center;justify-content:center;font-size:32px;">🎬</div>
    </div>
    <div class="record-info">
      <div class="record-title" :title="record.title">{{ record.title }}</div>
      <div class="record-meta">
        <span class="record-meta-item rating">★ {{ record.tmdbRating ? record.tmdbRating.toFixed(1) : '—' }}</span>
        <span class="record-status" :class="record.status">{{ record.status === 'watched' ? '已看' : '想看' }}</span>
      </div>
    </div>
    <div class="record-actions">
      <button class="record-btn write" @click="$emit('write', record)" title="写影评">✎</button>
      <button class="record-btn delete" @click="$emit('delete', record)" title="删除">✕</button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  record: { type: Object, required: true },
})
defineEmits(['write', 'delete'])

const imgBase = 'https://image.tmdb.org/t/p/'
</script>

<style scoped>
.record-card {
  display: flex;
  flex-direction: column;
  background: var(--bg-card);
  backdrop-filter: blur(12px);
  border: 1px solid var(--frost-border);
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
}
.record-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
.record-poster {
  width: 100%;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  background: linear-gradient(135deg, #e8e4e0 0%, #d8d4d0 100%);
}
.record-poster img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.record-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  padding: 14px 14px 10px;
}
.record-title {
  font-family: var(--font-display);
  font-size: 15px;
  font-weight: 600;
  color: var(--ink);
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.record-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.record-meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--ink-muted);
}
.record-meta-item.rating {
  color: var(--accent);
  font-weight: 600;
  font-size: 13px;
}
.record-status {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}
.record-status.watched {
  background: rgba(40, 167, 69, 0.1);
  color: #28a745;
}
.record-status.wishlist {
  background: rgba(255, 193, 7, 0.1);
  color: #ffc107;
}
.record-actions {
  display: flex;
  gap: 6px;
  padding: 0 14px 14px;
  margin-top: auto;
}
.record-btn {
  flex: 1;
  height: 34px;
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
.record-btn:hover {
  background: var(--frost-hover);
  color: var(--ink);
}
.record-btn.write {
  color: var(--accent);
  border-color: var(--accent-border);
  background: var(--accent-bg);
}
.record-btn.write:hover {
  background: rgba(184, 134, 11, 0.15);
  color: var(--accent);
}
.record-btn.delete:hover {
  background: rgba(220, 53, 69, 0.1);
  border-color: rgba(220, 53, 69, 0.2);
  color: #dc3545;
}
</style>
