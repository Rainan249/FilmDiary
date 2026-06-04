<template>
  <div class="reviews-header">
    <h1 class="reviews-title">我的影评</h1>
  </div>

  <LoadingSpinner v-if="reviewStore.isLoading" />

  <div v-else-if="reviewStore.reviews.length === 0" class="empty-state">
    <div class="empty-icon">✎</div>
    <p>暂无影评</p>
  </div>

  <div v-else class="reviews-list">
    <ReviewCard v-for="review in reviewStore.reviews" :key="review.id" :review="review"
      @edit="openEdit" @delete="reviewStore.deleteReview(review.id, review.title)" />
  </div>

  <ReviewFormModal v-model:visible="formVisible" :movie="formMovie" :review="formReview" @saved="onSaved" />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useReviewStore } from '@/stores/reviews'
import ReviewCard from '@/components/review/ReviewCard.vue'
import ReviewFormModal from '@/components/review/ReviewFormModal.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import '@/styles/reviews.css'

const route = useRoute()
const router = useRouter()
const reviewStore = useReviewStore()

const formVisible = ref(false)
const formMovie = ref(null)
const formReview = ref(null)

function openEdit(review) {
  formMovie.value = {
    id: review.movieId,
    movieId: review.movieId,
    title: review.title,
    poster_path: review.posterPath,
    posterPath: review.posterPath,
    tmdb_rating: review.tmdbRating,
    vote_average: review.tmdbRating,
    release_date: review.releaseDate,
    releaseDate: review.releaseDate,
  }
  formReview.value = review
  formVisible.value = true
}

function onSaved() {
  reviewStore.loadReviews()
  formReview.value = null
}

onMounted(async () => {
  await reviewStore.loadReviews()

  const { movieId, title, poster, rating, releaseDate } = route.query
  if (movieId) {
    const existing = await reviewStore.getReviewByMovieId(parseInt(movieId))
    formMovie.value = {
      id: parseInt(movieId),
      movieId: parseInt(movieId),
      title,
      poster_path: poster,
      posterPath: poster,
      tmdb_rating: rating ? parseFloat(rating) : null,
      vote_average: rating ? parseFloat(rating) : null,
      release_date: releaseDate,
      releaseDate: releaseDate,
    }
    formReview.value = existing
    formVisible.value = true
    router.replace({ query: {} })
  }
})
</script>

<style scoped>
.reviews-header { margin-bottom: 32px; }
.reviews-title { font-family: var(--font-display); font-size: 32px; font-weight: 700; }
.reviews-list { display: flex; flex-direction: column; gap: 16px; }
.empty-state { text-align: center; padding: 80px 20px; color: var(--ink-muted); }
.empty-icon { font-size: 64px; margin-bottom: 16px; opacity: 0.3; }
</style>
