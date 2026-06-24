import { ref, computed, type Ref } from 'vue'

export function usePagination(totalRef: Ref<number>, pageSize = ref(20)) {
  const page = ref(1)
  const totalPages = computed(() => Math.max(1, Math.ceil(totalRef.value / pageSize.value)))
  const hasNext = computed(() => page.value < totalPages.value)
  const hasPrev = computed(() => page.value > 1)

  function goTo(p: number) { page.value = Math.max(1, Math.min(p, totalPages.value)) }
  function next() { if (hasNext.value) goTo(page.value + 1) }
  function prev() { if (hasPrev.value) goTo(page.value - 1) }

  return { page, pageSize, totalPages, hasNext, hasPrev, goTo, next, prev }
}

export function useDebounce(fn: (...args: any[]) => void, delay = 300) {
  let timer: ReturnType<typeof setTimeout>
  return (...args: any[]) => {
    clearTimeout(timer)
    timer = setTimeout(() => fn(...args), delay)
  }
}
