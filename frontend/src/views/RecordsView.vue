<template>
  <div class="records-header">
    <h1 class="records-title">观影记录</h1>
    <div class="records-tabs">
      <button v-for="tab in tabs" :key="tab.value"
        class="records-tab" :class="{ active: recordStore.currentFilter === tab.value }"
        @click="switchTab(tab.value)">{{ tab.label }}</button>
    </div>
  </div>

  <div v-if="recordStore.isLoading" class="loading-state">
    <div class="loading-spinner"></div>
    <p>加载中...</p>
  </div>

  <div v-else-if="recordStore.records.length === 0" class="empty-state">
    <div class="empty-state-icon">🎬</div>
    <div class="empty-state-text">还没有观影记录</div>
  </div>

  <div v-else class="records-list">
    <RecordCard v-for="record in recordStore.records" :key="record.id" :record="record"
      @write="writeReview" @delete="recordStore.deleteRecord(record.id, record.title)" />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useRecordStore } from '@/stores/records'
import RecordCard from '@/components/record/RecordCard.vue'
import '@/styles/records.css'

const router = useRouter()
const recordStore = useRecordStore()

const tabs = [
  { label: '全部', value: 'all' },
  { label: '已看', value: 'watched' },
  { label: '想看', value: 'wishlist' },
]

function switchTab(value) {
  recordStore.currentFilter = value
  recordStore.loadRecords()
}

function writeReview(record) {
  router.push({
    path: '/reviews',
    query: {
      movieId: record.movieId,
      title: record.title,
      poster: record.posterPath,
      rating: record.tmdbRating,
      releaseDate: record.releaseDate,
    },
  })
}

onMounted(() => recordStore.loadRecords())
</script>
