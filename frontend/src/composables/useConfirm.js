import { ref } from 'vue'

const isVisible = ref(false)
const title = ref('')
const message = ref('')
let resolvePromise = null

export function useConfirm() {
  function show(t, msg) {
    title.value = t
    message.value = msg
    isVisible.value = true
    return new Promise(resolve => { resolvePromise = resolve })
  }
  function confirm() { isVisible.value = false; resolvePromise?.(true); resolvePromise = null }
  function cancel() { isVisible.value = false; resolvePromise?.(false); resolvePromise = null }
  return { isVisible, title, message, show, confirm, cancel }
}
