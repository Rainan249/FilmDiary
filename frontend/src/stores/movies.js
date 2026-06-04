import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'

const TMDB_API_KEY = import.meta.env.VITE_TMDB_API_KEY
const TMDB_BASE = 'https://api.themoviedb.org/3'
const IMG_BASE = 'https://image.tmdb.org/t/p/'
const PREFETCH_COUNT = 6
const INITIAL_PAGES = 2
const ROWS_PER_LOAD = 2

export const useMovieStore = defineStore('movies', () => {
  const movies = ref([])
  const currentPage = ref(1)
  const totalPages = ref(500)
  const isLoading = ref(false)
  const isFiltered = ref(false)
  const isSearching = ref(false)
  const searchQuery = ref('')
  const recordMovieIds = ref(new Set())
  const pageCache = new Map()
  const inflight = new Map()

  const filters = reactive({
    rating: [],
    region: [],
    year: [],
    genre: [],
    sort: ['vote_average.desc'],
  })

  function buildUrl(endpoint, params = {}) {
    const url = new URL(`${TMDB_BASE}${endpoint}`)
    url.searchParams.set('api_key', TMDB_API_KEY)
    url.searchParams.set('language', 'zh-CN')
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') url.searchParams.set(k, v)
    })
    return url.toString()
  }

  function getFilters() {
    const params = {}
    if (filters.rating.length) params['vote_average.gte'] = Math.min(...filters.rating)
    if (filters.region.length) params.with_origin_country = filters.region.join('|')
    if (filters.year.length) params.primary_release_date.gte = `${Math.min(...filters.year)}-01-01`
    if (filters.year.length) params.primary_release_date.lte = `${Math.max(...filters.year)}-12-31`
    if (filters.genre.length) params.with_genres = filters.genre.join('|')
    if (filters.sort.length) params.sort_by = filters.sort[0]
    return params
  }

  function hasActiveFilters() {
    return filters.rating.length > 0 || filters.region.length || filters.year.length || filters.genre.length || filters.sort[0] !== 'vote_average.desc'
  }

  async function fetchPageRaw(page, filterParams = {}) {
    let endpoint
    const hasFilters = Object.keys(filterParams).length > 0
    if (hasFilters) {
      endpoint = '/discover/movie'
    } else {
      const sort = filters.sort[0] || 'vote_average.desc'
      if (sort === 'popularity.desc') endpoint = '/movie/popular'
      else if (sort === 'vote_average.desc') endpoint = '/movie/top_rated'
      else endpoint = '/discover/movie'
    }
    const url = buildUrl(endpoint, { ...filterParams, page })
    const res = await fetch(url)
    if (!res.ok) throw new Error(`TMDB request failed: ${res.status}`)
    const data = await res.json()
    return data.results.filter(m => m.poster_path)
  }

  async function fetchPage(page, filterParams = {}) {
    const key = `${page}_${JSON.stringify(filterParams)}`
    if (pageCache.has(key)) return pageCache.get(key)
    if (inflight.has(key)) return inflight.get(key)
    const promise = fetchPageRaw(page, filterParams).finally(() => inflight.delete(key))
    inflight.set(key, promise)
    const result = await promise
    pageCache.set(key, result)
    return result
  }

  async function loadRecordIds() {
    try {
      const res = await fetch('/api/records')
      const data = await res.json()
      recordMovieIds.value = new Set(data.map(r => r.movieId))
    } catch (e) { /* ignore */ }
  }

  async function loadFirstPage() {
    isLoading.value = true
    movies.value = []
    currentPage.value = 1
    isFiltered.value = hasActiveFilters()
    isSearching.value = false

    const filterParams = isFiltered.value ? getFilters() : {}

    try {
      const pages = await Promise.all(
        Array.from({ length: INITIAL_PAGES }, (_, i) => fetchPage(i + 1, filterParams))
      )
      const flat = pages.flat()
      movies.value = flat
      currentPage.value = INITIAL_PAGES + 1

      for (let i = currentPage.value; i < currentPage.value + PREFETCH_COUNT; i++) {
        fetchPage(i, filterParams).catch(() => {})
      }
    } catch (e) {
      console.error('加载首页失败:', e)
    } finally {
      isLoading.value = false
    }
  }

  async function loadNextPage() {
    if (isLoading.value || isFiltered.value || isSearching.value) return
    if (currentPage.value > totalPages.value) return
    isLoading.value = true
    try {
      const batch = []
      for (let i = 0; i < ROWS_PER_LOAD; i++) {
        if (currentPage.value + i > totalPages.value) break
        batch.push(fetchPage(currentPage.value + i))
      }
      const results = await Promise.all(batch)
      const flat = results.flat()
      if (flat.length) movies.value = [...movies.value, ...flat]
      currentPage.value += ROWS_PER_LOAD
    } catch (e) {
      console.error('加载下一页失败:', e)
    } finally {
      isLoading.value = false
    }
  }

  async function doSearch(query) {
    if (!query.trim()) return clearSearch()
    isSearching.value = true
    searchQuery.value = query
    isLoading.value = true
    try {
      const res = await fetch(`/api/tmdb/search?query=${encodeURIComponent(query)}&page=1`)
      const data = await res.json()
      const results = (data.results || []).filter(m => m.poster_path)
      movies.value = results
    } catch (e) {
      console.error('搜索失败:', e)
    } finally {
      isLoading.value = false
    }
  }

  function clearSearch() {
    isSearching.value = false
    searchQuery.value = ''
    loadFirstPage()
  }

  async function addToWatchlist(movie) {
    try {
      await fetch('/api/records', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          movieId: parseInt(movie.id),
          title: movie.title,
          posterPath: movie.poster_path,
          tmdbRating: parseFloat(movie.vote_average) || null,
          releaseDate: movie.release_date,
          overview: movie.overview,
          status: 'wishlist',
        }),
      })
      recordMovieIds.value.add(parseInt(movie.id))
    } catch (e) {
      console.error('添加失败:', e)
    }
  }

  return {
    movies, currentPage, totalPages, isLoading, isFiltered, isSearching,
    searchQuery, recordMovieIds, filters,
    loadFirstPage, loadNextPage, doSearch, clearSearch, loadRecordIds, addToWatchlist,
    IMG_BASE,
  }
})
