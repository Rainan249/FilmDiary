<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="modal-overlay show" @click.self="$emit('update:visible', false)">
        <div class="modal">
          <h3 class="modal-title">{{ review ? '编辑影评' : '写影评' }}</h3>
          <div class="modal-movie-info">
            <div class="modal-movie-poster">
              <img v-if="movie?.posterPath || movie?.poster_path" :src="`${imgBase}w200${movie.posterPath || movie.poster_path}`" :alt="movie?.title">
              <div v-else style="width:100%;height:100%;display:flex;align-items:center;justify-content:center;">🎬</div>
            </div>
            <div class="modal-movie-details">
              <h4 class="modal-movie-title">{{ movie?.title }}</h4>
              <p class="modal-movie-rating">TMDB评分: {{ (movie?.tmdbRating || movie?.tmdb_rating) ? parseFloat(movie.tmdbRating || movie.tmdb_rating).toFixed(1) : '—' }}</p>
            </div>
          </div>
          <div class="modal-form-group">
            <label class="modal-label">我的评分</label>
            <StarRating v-model="selectedRating" />
          </div>
          <div class="modal-form-group">
            <label class="modal-label">影评内容</label>
            <textarea class="modal-textarea" v-model="content" placeholder="写下你的感想..." rows="5"></textarea>
          </div>
          <div class="modal-actions-center">
            <button class="modal-btn modal-btn-cancel" @click="$emit('update:visible', false)">取消</button>
            <button class="modal-btn modal-btn-primary" @click="submit" :disabled="!content.trim()">保存</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, watch } from 'vue'
import StarRating from '@/components/ui/StarRating.vue'
import { useToast } from '@/composables/useToast'
import { useRecordStore } from '@/stores/records'

const props = defineProps({
  movie: Object,
  review: { type: Object, default: null },
  visible: Boolean,
})
const emit = defineEmits(['update:visible', 'saved'])

const { show: toastShow } = useToast()
const recordStore = useRecordStore()
const imgBase = 'https://image.tmdb.org/t/p/'
const selectedRating = ref(0)
const content = ref('')

watch(() => props.visible, (v) => {
  if (v) {
    selectedRating.value = props.review?.user_rating || 0
    content.value = props.review?.content || ''
  }
})

async function submit() {
  if (!content.value.trim()) return
  const data = {
    movieId: parseInt(props.movie.movieId || props.movie.id),
    title: props.movie.title,
    posterPath: props.movie.poster_path || props.movie.posterPath,
    tmdbRating: parseFloat(props.movie.tmdb_rating || props.movie.vote_average) || null,
    releaseDate: props.movie.release_date || props.movie.releaseDate,
    userRating: selectedRating.value || null,
    content: content.value.trim(),
  }
  try {
    if (props.review) {
      await fetch(`/api/reviews/${props.review.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userRating: selectedRating.value, content: content.value.trim() }),
      })
    } else {
      await fetch('/api/reviews', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data),
      })
    }
    toastShow('影评已保存')
    recordStore.markAsWatched(data.movieId)
    emit('update:visible', false)
    emit('saved')
  } catch (e) {
    toastShow('保存失败', 'error')
  }
}
</script>

<style scoped>
.modal-movie-info { display: flex; gap: 16px; padding: 16px; background: rgba(255,255,255,0.6); border-radius: 12px; border: 1px solid rgba(0,0,0,0.04); margin-bottom: 24px; }
.modal-movie-poster { width: 85px; height: 120px; border-radius: 10px; overflow: hidden; flex-shrink: 0; background: linear-gradient(135deg, #e8e4e0, #d8d4d0); }
.modal-movie-poster img { width: 100%; height: 100%; object-fit: cover; }
.modal-movie-details { flex: 1; display: flex; flex-direction: column; justify-content: center; }
.modal-movie-title { font-family: var(--font-display); font-size: 18px; font-weight: 600; margin-bottom: 8px; }
.modal-movie-rating { font-size: 14px; color: var(--accent); font-weight: 600; }
.modal-form-group { margin-bottom: 20px; }
.modal-label { display: block; font-size: 13px; font-weight: 600; color: var(--ink-secondary); margin-bottom: 10px; text-transform: uppercase; letter-spacing: 0.05em; }
.modal-textarea { width: 100%; min-height: 120px; padding: 14px 16px; background: var(--frost); border: 1.5px solid var(--frost-border); border-radius: 10px; font-size: 15px; font-family: var(--font-body); color: var(--ink); outline: none; resize: vertical; transition: all 0.3s ease; line-height: 1.6; }
.modal-textarea:focus { border-color: var(--accent); box-shadow: 0 0 0 3px rgba(184,134,11,0.1); }
.modal-actions-center { display: flex; justify-content: center; gap: 16px; margin-top: 24px; }
.modal-enter-active, .modal-leave-active { transition: opacity 0.3s ease; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
</style>
