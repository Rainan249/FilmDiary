<template>
  <div class="discover-page">
    <div class="discover-hero" v-if="currentMovie || loading">
      <div class="discover-poster" ref="posterEl">
        <img v-if="currentMovie?.poster_path" :src="`${imgBase}${currentMovie.poster_path}`" :alt="currentMovie?.title">
        <div v-else-if="currentMovie" class="discover-poster-fallback">🎬</div>
      </div>
      <div class="discover-info" ref="infoEl">
        <div class="discover-actions">
          <button class="discover-btn discover-btn-primary" @click="handleWatch">☆</button>
          <button class="discover-btn discover-btn-secondary" @click="handleWrite">✎</button>
          <button class="discover-btn discover-btn-refresh" @click="switchToNext">→</button>
        </div>
        <div class="discover-badge">随机推荐</div>
        <h1 class="discover-title">{{ currentMovie?.title }}</h1>
        <div class="discover-meta">
          <span class="discover-rating">{{ currentMovie?.vote_average?.toFixed(1) }}</span>
          <span class="discover-year">{{ currentMovie?.release_date?.slice(0, 4) || '未知' }}</span>
          <span class="discover-genres">{{ genreText }}</span>
        </div>
        <p class="discover-overview">{{ currentMovie?.overview || '暂无简介' }}</p>
      </div>
    </div>
    <div class="discover-loading" v-if="loading && !currentMovie">
      <div class="loading-spinner"></div>
      <p>正在寻找好电影...</p>
    </div>

    <ReviewFormModal v-model:visible="reviewVisible" :movie="reviewMovie" @saved="reviewVisible = false" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useMovieStore } from '@/stores/movies'
import { useToast } from '@/composables/useToast'
import ReviewFormModal from '@/components/review/ReviewFormModal.vue'

const movieStore = useMovieStore()
const { show: toastShow } = useToast()
const imgBase = 'https://image.tmdb.org/t/p/w500'

const DISCOVER_GENRES = {
  28: '动作', 12: '冒险', 16: '动漫', 35: '喜剧', 80: '犯罪',
  18: '剧情', 14: '奇幻', 27: '恐怖', 9648: '悬疑', 10749: '爱情',
  878: '科幻', 53: '惊悚'
}

const currentMovie = ref(null)
const queue = ref([])
const loading = ref(false)
const isTransitioning = ref(false)
const posterEl = ref(null)
const infoEl = ref(null)
const reviewVisible = ref(false)
const reviewMovie = ref(null)

const genreText = computed(() => {
  if (!currentMovie.value?.genre_ids) return ''
  return currentMovie.value.genre_ids.map(id => DISCOVER_GENRES[id] || '').filter(Boolean).join(' / ')
})

const TMDB_KEY = import.meta.env.VITE_TMDB_API_KEY
const TMDB_BASE = 'https://api.themoviedb.org/3'
const pageCache = new Map()

async function fetchPage(page) {
  if (pageCache.has(page)) return pageCache.get(page)
  const res = await fetch(`${TMDB_BASE}/movie/popular?api_key=${TMDB_KEY}&language=zh-CN&page=${page}`)
  if (!res.ok) throw new Error(`HTTP ${res.status}`)
  const data = await res.json()
  const movies = data.results.filter(m => m.poster_path && m.overview && m.overview.trim())
  pageCache.set(page, movies)
  return movies
}

function shuffle(arr) {
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [arr[i], arr[j]] = [arr[j], arr[i]]
  }
  return arr
}

function preloadImage(url) {
  return new Promise(resolve => {
    const img = new Image()
    img.onload = resolve
    img.onerror = resolve
    img.src = url
  })
}

function preloadQueue() {
  queue.value.slice(0, 2).forEach(m => {
    if (m.poster_path) preloadImage(`${imgBase}${m.poster_path}`)
  })
}

function refillQueue() {
  if (queue.value.length < 2) {
    const page = Math.floor(Math.random() * 20) + 1
    fetchPage(page).then(movies => {
      queue.value.push(...shuffle(movies))
      preloadQueue()
    }).catch(() => {})
  }
}

async function loadRandomMovie(fade) {
  if (isTransitioning.value) return

  if (fade) {
    isTransitioning.value = true
    const poster = posterEl.value
    const info = infoEl.value
    if (poster && info) {
      poster.style.transition = 'transform 0.35s ease, opacity 0.35s ease'
      info.style.transition = 'transform 0.35s ease, opacity 0.35s ease'
      poster.style.transform = 'translateX(-50px)'
      poster.style.opacity = '0'
      info.style.transform = 'translateX(-50px)'
      info.style.opacity = '0'
      await new Promise(r => setTimeout(r, 350))
    }
  } else {
    loading.value = true
  }

  try {
    if (queue.value.length === 0) {
      const movies = await fetchPage(Math.floor(Math.random() * 20) + 1)
      queue.value.push(...shuffle(movies))
    }
    if (queue.value.length === 0) {
      loading.value = false
      return
    }

    const nextMovie = queue.value.pop()
    if (nextMovie?.poster_path) {
      await preloadImage(`${imgBase}${nextMovie.poster_path}`)
    }
    currentMovie.value = nextMovie
    refillQueue()
    preloadQueue()

    if (fade) {
      const poster = posterEl.value
      const info = infoEl.value
      if (poster && info) {
        poster.style.transition = 'none'
        poster.style.transform = 'translateX(50px)'
        poster.style.opacity = '0'
        info.style.transition = 'none'
        info.style.transform = 'translateX(50px)'
        info.style.opacity = '0'
        requestAnimationFrame(() => {
          requestAnimationFrame(() => {
            poster.style.transition = 'transform 0.35s ease, opacity 0.35s ease'
            poster.style.transform = 'translateX(0)'
            poster.style.opacity = '1'
            info.style.transition = 'transform 0.35s ease, opacity 0.35s ease'
            info.style.transform = 'translateX(0)'
            info.style.opacity = '1'
            isTransitioning.value = false
          })
        })
      } else {
        isTransitioning.value = false
      }
    } else {
      loading.value = false
    }
  } catch (e) {
    console.error('加载推荐失败:', e)
    loading.value = false
    if (fade) {
      const poster = posterEl.value
      const info = infoEl.value
      if (poster) { poster.style.opacity = '1'; poster.style.transform = 'translateX(0)' }
      if (info) { info.style.opacity = '1'; info.style.transform = 'translateX(0)' }
      isTransitioning.value = false
    }
  }
}

function switchToNext() {
  loadRandomMovie(true)
}

function handleWatch() {
  if (!currentMovie.value) return
  movieStore.addToWatchlist(currentMovie.value)
  toastShow('已添加到想看列表')
}

function handleWrite() {
  if (!currentMovie.value) return
  reviewMovie.value = currentMovie.value
  reviewVisible.value = true
}

onMounted(() => loadRandomMovie(false))
</script>
