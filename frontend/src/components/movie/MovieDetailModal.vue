<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="modal-overlay show" @click.self="$emit('update:visible', false)">
        <div class="modal modal-detail">
          <div class="detail-poster">
            <img v-if="movie?.poster_path" :src="`${imgBase}w500${movie.poster_path}`" :alt="movie?.title">
            <div v-else class="poster-fallback">🎬</div>
          </div>
          <div class="detail-body">
            <h2 class="detail-title">{{ movie?.title }}</h2>
            <div class="detail-meta">
              <span class="meta-item rating">★ {{ movie?.vote_average?.toFixed(1) || '—' }}</span>
              <span class="meta-dot"></span>
              <span class="meta-item">{{ movie?.release_date }}</span>
            </div>
            <p class="detail-overview">{{ movie?.overview }}</p>
            <div class="detail-actions">
              <button class="modal-btn modal-btn-primary" @click="$emit('write', movie)">✎ 写影评</button>
              <button class="modal-btn modal-btn-cancel" @click="$emit('watch', movie)">+ 想看</button>
              <button class="modal-btn modal-btn-cancel" @click="$emit('update:visible', false)">关闭</button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { watch } from 'vue'

const props = defineProps({
  movie: Object,
  visible: Boolean,
  movies: { type: Array, default: () => [] },
})
defineEmits(['update:visible', 'watch', 'write'])

const imgBase = 'https://image.tmdb.org/t/p/'
let preloaded = []

function preloadNext() {
  preloaded.forEach(img => { img.onload = null })
  preloaded = []
  if (!props.movie || !props.movies.length) return
  const idx = props.movies.findIndex(m => m.id === props.movie.id)
  for (let i = 1; i <= 2; i++) {
    const next = props.movies[idx + i]
    if (next?.poster_path) {
      const img = new Image()
      img.src = `${imgBase}w500${next.poster_path}`
      preloaded.push(img)
    }
  }
}

watch(() => props.visible, v => { if (v) preloadNext() })
watch(() => props.movie, () => { if (props.visible) preloadNext() })
</script>

<style scoped>
.detail-poster { flex: 0 0 260px; align-self: stretch; background: linear-gradient(135deg, #e8e4e0 0%, #d8d4d0 100%); overflow: hidden; display: flex; align-items: center; justify-content: center; }
.detail-poster img { width: 100%; height: 100%; object-fit: cover; }
.detail-body { flex: 1; padding: 32px; display: flex; flex-direction: column; overflow-y: auto; }
.detail-title { font-family: var(--font-display); font-size: 24px; font-weight: 700; margin-bottom: 12px; }
.detail-meta { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; font-size: 14px; color: var(--ink-muted); }
.detail-overview { font-size: 15px; line-height: 1.8; color: var(--ink-secondary); flex: 1; overflow-y: auto; }
.detail-actions { display: flex; gap: 12px; margin-top: 20px; flex-wrap: wrap; }
.modal-enter-active, .modal-leave-active { transition: opacity 0.3s ease, transform 0.3s ease; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
.modal-enter-from .modal-detail, .modal-leave-to .modal-detail { transform: scale(0.95) translateY(10px); }
</style>
