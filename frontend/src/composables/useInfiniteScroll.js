import { onMounted, onUnmounted } from 'vue'

export function useInfiniteScroll(callback, options = {}) {
  const { threshold = 600 } = options

  function getScrollContainer() {
    return document.querySelector('.main') || window
  }

  function handleScroll() {
    const el = getScrollContainer()
    const scrollBottom = el === window
      ? window.innerHeight + window.scrollY
      : el.scrollTop + el.clientHeight
    const docHeight = el === window
      ? document.documentElement.scrollHeight
      : el.scrollHeight
    if (scrollBottom >= docHeight - threshold) {
      callback()
    }
  }

  onMounted(() => {
    const el = getScrollContainer()
    el.addEventListener('scroll', handleScroll, { passive: true })
  })

  onUnmounted(() => {
    const el = getScrollContainer()
    el.removeEventListener('scroll', handleScroll)
  })
}
