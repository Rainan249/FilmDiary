<template>
  <header class="header">
    <div class="welcome">
      <h1>欢迎回来, <em>{{ authStore.username || '影迷' }}</em></h1>
      <p>今天想看什么类型的电影? 探索你的观影世界。</p>
    </div>
    <div class="search-wrap">
      <div class="search-box">
        <span class="search-icon">⌕</span>
        <input type="text" v-model="searchInput" @input="debouncedSearch" placeholder="搜索电影名称...">
        <button class="search-btn" @click="clearSearch">清空</button>
      </div>
    </div>
  </header>

  <div class="stats-row">
    <div class="stat-card">
      <div class="stat-value">{{ stats.watched }}</div>
      <div class="stat-label">已看影片</div>
      <div class="stat-icon">◎</div>
    </div>
    <div class="stat-card">
      <div class="stat-value">{{ stats.wishlist }}</div>
      <div class="stat-label">待看清单</div>
      <div class="stat-icon">◻</div>
    </div>
    <div class="stat-card">
      <div class="stat-value">{{ stats.reviewCount }}</div>
      <div class="stat-label">观后笔记</div>
      <div class="stat-icon">▢</div>
    </div>
    <div class="stat-card">
      <div class="stat-value">{{ stats.avgRating }}</div>
      <div class="stat-label">平均评分</div>
      <div class="stat-icon">★</div>
    </div>
  </div>

  <div class="filter-section">
    <div class="section-title">影片库</div>
    <div class="filter-area">
      <div class="filter-group" v-for="fg in filterGroups" :key="fg.key">
        <span class="filter-group-label">{{ fg.label }}</span>
        <div class="filter-options">
          <button v-for="opt in fg.options" :key="opt.value"
            class="filter-chip"
            :class="{ 'filter-chip-active': movieStore.filters[fg.key].includes(opt.value) }"
            @click="toggleFilter(fg.key, opt.value)">{{ opt.label }}</button>
        </div>
      </div>
      <div class="filter-group filter-group-reset">
        <div class="filter-options">
          <button class="filter-chip filter-chip-reset" @click="resetAllFilters">重置</button>
        </div>
      </div>
    </div>
  </div>

  <div class="movie-grid">
    <MovieCard v-for="(movie, i) in movieStore.movies" :key="movie.id" :movie="movie" :delay="i * 50"
      @detail="openDetail" @watch="handleWatch" @write="handleWrite" />
  </div>

  <div v-if="movieStore.isLoading && movieStore.movies.length === 0" class="loading-state">
    <div class="loading-spinner"></div>
    <p>正在从 TMDB 获取电影数据...</p>
  </div>

  <div v-if="movieStore.isLoading && movieStore.movies.length > 0" class="load-more">
    <div class="loading-spinner"></div>
    <span>加载更多...</span>
  </div>

  <div v-if="!movieStore.isLoading && movieStore.movies.length > 0 && movieStore.currentPage > movieStore.totalPages" class="no-more">
    <span>— 已经到底了 —</span>
  </div>

  <MovieDetailModal v-model:visible="detailVisible" :movie="detailMovie" :movies="movieStore.movies"
    @watch="handleWatch" @write="handleWriteFromDetail" />

  <ReviewFormModal v-model:visible="reviewVisible" :movie="reviewMovie" @saved="onReviewSaved" />
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useMovieStore } from '@/stores/movies'
import { useReviewStore } from '@/stores/reviews'
import { useInfiniteScroll } from '@/composables/useInfiniteScroll'
import { useToast } from '@/composables/useToast'
import MovieCard from '@/components/movie/MovieCard.vue'
import MovieDetailModal from '@/components/movie/MovieDetailModal.vue'
import ReviewFormModal from '@/components/review/ReviewFormModal.vue'

const authStore = useAuthStore()
const movieStore = useMovieStore()
const reviewStore = useReviewStore()
const { show: toastShow } = useToast()

const stats = reactive({ watched: 0, wishlist: 0, reviewCount: 0, avgRating: '0' })
const searchInput = ref('')
const detailVisible = ref(false)
const detailMovie = ref(null)
const reviewVisible = ref(false)
const reviewMovie = ref(null)

const filterGroups = [
  { key: 'genre', label: '类型', options: [
    { label: '动作', value: '28' }, { label: '冒险', value: '12' }, { label: '动漫', value: '16' },
    { label: '喜剧', value: '35' }, { label: '犯罪', value: '80' }, { label: '剧情', value: '18' },
    { label: '奇幻', value: '14' }, { label: '恐怖', value: '27' }, { label: '悬疑', value: '9648' },
    { label: '爱情', value: '10749' }, { label: '科幻', value: '878' }, { label: '惊悚', value: '53' },
  ]},
  { key: 'rating', label: '评分', options: [
    { label: '6 分以上', value: '6' }, { label: '7 分以上', value: '7' },
    { label: '8 分以上', value: '8' }, { label: '9 分以上', value: '9' },
  ]},
  { key: 'region', label: '地区', options: [
    { label: '中国大陆', value: 'CN' }, { label: '日本', value: 'JP' }, { label: '韩国', value: 'KR' },
    { label: '美国', value: 'US' }, { label: '英国', value: 'GB' }, { label: '法国', value: 'FR' },
    { label: '德国', value: 'DE' }, { label: '印度', value: 'IN' }, { label: '意大利', value: 'IT' },
    { label: '西班牙', value: 'ES' }, { label: '泰国', value: 'TH' }, { label: '俄罗斯', value: 'RU' },
    { label: '巴西', value: 'BR' }, { label: '瑞典', value: 'SE' }, { label: '澳大利亚', value: 'AU' },
  ]},
  { key: 'year', label: '年份', options: [
    { label: '2026', value: '2026' }, { label: '2025', value: '2025' }, { label: '2024', value: '2024' },
    { label: '2023', value: '2023' }, { label: '2022', value: '2022' }, { label: '2021', value: '2021' },
    { label: '2020', value: '2020' },
  ]},
  { key: 'sort', label: '排序', options: [
    { label: '评分', value: 'vote_average.desc' }, { label: '热度', value: 'popularity.desc' },
    { label: '最新上映', value: 'primary_release_date.desc' }, { label: '票房最高', value: 'revenue.desc' },
  ]},
]

let searchTimer = null
/**
 * 防抖搜索函数
 * 延迟 300ms 执行搜索，避免频繁请求
 * 用户输入时清空之前的定时器，重新计时
 */
function debouncedSearch() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    if (searchInput.value.trim()) movieStore.doSearch(searchInput.value)
    else movieStore.clearSearch()
  }, 300)
}

/**
 * 清空搜索框并重置电影列表
 * 清除搜索关键词，调用 store 的 clearSearch 恢复默认列表
 */
function clearSearch() {
  searchInput.value = ''
  movieStore.clearSearch()
}

/**
 * 切换筛选条件
 * @param {string} key - 筛选项类型（genre/rating/region/year/sort）
 * @param {string} value - 筛选值
 *
 * 逻辑：
 * - sort 和 rating 为单选模式（选中则取消，未选中则替换）
 * - 其他为多选模式（已存在则移除，不存在则添加）
 * 筛选变更后重新加载第一页数据
 */
function toggleFilter(key, value) {
  const arr = movieStore.filters[key]
  if (key === 'sort' || key === 'rating') {
    movieStore.filters[key] = arr.includes(value) ? [] : [value]
  } else {
    const idx = arr.indexOf(value)
    if (idx >= 0) arr.splice(idx, 1)
    else arr.push(value)
  }
  movieStore.loadFirstPage()
}

/**
 * 重置所有筛选条件到默认状态
 * 清空所有多选筛选项，排序恢复为"评分降序"
 * 同时清空搜索框并重新加载首屏数据
 */
function resetAllFilters() {
  movieStore.filters.genre = []
  movieStore.filters.rating = []
  movieStore.filters.region = []
  movieStore.filters.year = []
  movieStore.filters.sort = ['vote_average.desc']
  searchInput.value = ''
  movieStore.clearSearch()
}

/**
 * 打开电影详情弹窗
 * @param {Object} movie - 选中的电影对象
 */
function openDetail(movie) {
  detailMovie.value = movie
  detailVisible.value = true
}

/**
 * 处理"想看"按钮点击
 * 将电影添加到用户的待看清单，并显示 Toast 提示
 * @param {Object} movie - 要添加的电影对象
 */
function handleWatch(movie) {
  movieStore.addToWatchlist(movie)
  toastShow('已添加到想看列表')
}

/**
 * 处理"写影评"按钮点击（从卡片直接触发）
 * 关闭可能存在的详情弹窗，打开影评表单
 * @param {Object} movie - 要写影评的电影对象
 */
function handleWrite(movie) {
  reviewMovie.value = movie
  reviewVisible.value = true
  detailVisible.value = false
}

/**
 * 处理从详情页触发的"写影评"操作
 * 先关闭详情弹窗，延迟 300ms 后打开影评表单（避免动画冲突）
 * @param {Object} movie - 要写影评的电影对象
 */
function handleWriteFromDetail(movie) {
  detailVisible.value = false
  setTimeout(() => {
    reviewMovie.value = movie
    reviewVisible.value = true
  }, 300)
}

/**
 * 影评保存成功后的回调
 * 刷新顶部统计数据（观后笔记数、平均评分等）
 */
function onReviewSaved() {
  loadStats()
}

/**
 * 加载用户观影统计数据
 * 并行请求记录统计和影评统计，更新顶部四个统计卡片
 * 包含：已看影片数、待看清单数、观后笔记数、平均评分
 */
async function loadStats() {
  try {
    const [recordRes, reviewData] = await Promise.all([
      fetch('/api/records/stats'),
      reviewStore.getStats(),
    ])
    const recordData = await recordRes.json()
    stats.watched = recordData.watched || 0
    stats.wishlist = recordData.wishlist || 0
    stats.reviewCount = reviewData.count
    stats.avgRating = reviewData.avgRating
  } catch (e) { /* ignore */ }
}

onMounted(() => {
  movieStore.loadRecordIds()
  movieStore.loadFirstPage()
  loadStats()
})

useInfiniteScroll(() => movieStore.loadNextPage())
</script>

<style scoped>
.search-btn {
  padding: 8px 16px;
  border-radius: 8px;
  border: none;
  background: linear-gradient(135deg, var(--accent-light) 0%, var(--accent) 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  font-family: var(--font-body);
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}
.search-btn:hover {
  box-shadow: 0 4px 12px rgba(184, 134, 11, 0.3);
  transform: translateY(-1px);
}
</style>
