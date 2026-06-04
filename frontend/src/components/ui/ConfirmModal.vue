<template>
  <Teleport to="body">
    <div v-if="confirmState.isVisible.value" class="modal-overlay show" @click.self="confirmState.cancel()">
      <div class="modal modal-confirm">
        <div class="modal-icon">⚠️</div>
        <h3 class="modal-title">{{ confirmState.title.value }}</h3>
        <p class="modal-subtitle" v-html="confirmState.message.value"></p>
        <div class="modal-actions">
          <button class="modal-btn modal-btn-cancel" @click="confirmState.cancel()">取消</button>
          <button class="modal-btn modal-btn-primary" @click="confirmState.confirm()">确定</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { useConfirm } from '@/composables/useConfirm'
const confirmState = useConfirm()
</script>
