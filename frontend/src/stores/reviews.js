import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useConfirm } from '@/composables/useConfirm'
import { useToast } from '@/composables/useToast'

export const useReviewStore = defineStore('reviews', () => {
  const reviews = ref([])
  const isLoading = ref(false)
  const { show: confirmShow } = useConfirm()
  const { show: toastShow } = useToast()

  async function loadReviews() {
    isLoading.value = true
    try {
      const res = await fetch('/api/reviews')
      reviews.value = await res.json()
    } catch (e) {
      console.error('加载影评失败:', e)
    } finally {
      isLoading.value = false
    }
  }

  async function addReview(data) {
    const res = await fetch('/api/reviews', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    })
    return await res.json()
  }

  async function updateReview(id, data) {
    await fetch(`/api/reviews/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    })
  }

  async function deleteReview(id, title) {
    const ok = await confirmShow('删除影评', `确定要删除「${title}」的影评吗？`)
    if (!ok) return
    try {
      await fetch(`/api/reviews/${id}`, { method: 'DELETE' })
      toastShow('影评已删除')
      loadReviews()
    } catch (e) {
      toastShow('删除失败', 'error')
    }
  }

  async function getReviewByMovieId(movieId) {
    try {
      const res = await fetch(`/api/reviews/movie/${movieId}`)
      if (res.status === 404) return null
      return await res.json()
    } catch (e) {
      return null
    }
  }

  async function getStats() {
    try {
      const res = await fetch('/api/reviews')
      const data = await res.json()
      const count = data.length
      const avgRating = count > 0
        ? (data.reduce((s, r) => s + (r.userRating || 0), 0) / count).toFixed(1)
        : '0.0'
      return { count, avgRating }
    } catch (e) {
      return { count: 0, avgRating: '0.0' }
    }
  }

  return { reviews, isLoading, loadReviews, addReview, updateReview, deleteReview, getReviewByMovieId, getStats }
})
