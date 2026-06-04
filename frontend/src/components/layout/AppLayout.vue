<template>
  <div class="atmosphere"></div>
  <div class="texture"></div>

  <button class="mobile-toggle" @click="sidebarToggle">☰</button>
  <div class="overlay" :class="{ active: sidebarOpen }" @click="sidebarClose"></div>

  <AppSidebar />

  <main class="main">
    <router-view />
  </main>

  <ToastContainer />
  <ConfirmModal />
</template>

<script setup>
import AppSidebar from './AppSidebar.vue'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import ConfirmModal from '@/components/ui/ConfirmModal.vue'
import { useSidebar } from '@/composables/useSidebar'

const { isOpen: sidebarOpen, toggle: sidebarToggle, close: sidebarClose } = useSidebar()
</script>
