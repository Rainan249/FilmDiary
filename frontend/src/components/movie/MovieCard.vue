<template>
  <div class="movie-card" :style="{ animationDelay: `${delay}ms` }" @click="$emit('detail', movie)">
    <div class="poster">
      <img v-if="movie.poster_path" :src="`${imgBase}w500${movie.poster_path}`" :alt="movie.title" class="poster-img" loading="lazy">
      <div v-else class="poster-fallback">🎬</div>
      <span class="poster-label">{{ movie.release_date?.substring(0, 4) || '—' }}</span>
    </div>
    <div class="card-body">
      <h3 class="movie-title">{{ movie.title }}</h3>
      <div class="movie-meta">
        <span class="meta-item rating">★ {{ movie.vote_average?.toFixed(1) || '—' }}</span>
        <span class="meta-dot"></span>
        <span class="meta-item">{{ movie.release_date?.substring(0, 4) || '未知' }}</span>
      </div>
      <p class="movie-overview" v-if="movie.overview">{{ movie.overview.substring(0, 60) }}...</p>
      <div class="card-btns">
        <button class="btn-watch" :class="{ added: isAdded }" :disabled="isAdded"
          @click.stop="$emit('watch', movie)">
          {{ isAdded ? '✓' : '+' }}
        </button>
        <button class="btn-write" @click.stop="$emit('write', movie)">✎</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useMovieStore } from '@/stores/movies'

const props = defineProps({
  movie: { type: Object, required: true },
  delay: { type: Number, default: 0 },
})
defineEmits(['detail', 'watch', 'write'])

const movieStore = useMovieStore()
const imgBase = 'https://image.tmdb.org/t/p/'
const isAdded = computed(() => movieStore.recordMovieIds.has(parseInt(props.movie.id)))
</script>

<style scoped>
.movie-overview {
  font-size: 13px;
  color: var(--ink-muted);
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.btn-watch.added {
  background: var(--accent-bg);
  border-color: var(--accent-border);
  color: var(--accent);
  cursor: not-allowed;
}
</style>
