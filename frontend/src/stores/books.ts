import { defineStore } from 'pinia'
import { ref } from 'vue'
import { bookApi } from '../api/books'
import type { BookSummary, BookDetail, BookListParams, FacetValue, FacetsResponse } from '../types/api'

export const useBookStore = defineStore('books', () => {
  const results = ref<BookSummary[]>([])
  const total = ref(0)
  const page = ref(1)
  const limit = ref(20)
  const loading = ref(false)
  const currentBook = ref<BookDetail | null>(null)
  const facets = ref<FacetsResponse['facets'] | null>(null)
  const searchQuery = ref('')
  const activeFilters = ref<BookListParams>({})

  async function search(params: BookListParams = {}) {
    loading.value = true
    searchQuery.value = params.search || ''
    activeFilters.value = params
    try {
      const res = await bookApi.list({ ...params, page: page.value, limit: limit.value })
      results.value = res.books
      total.value = res.total
      page.value = res.page
      limit.value = res.limit
    } finally { loading.value = false }
  }

  async function goTo(p: number) {
    page.value = p
    await search({ ...activeFilters.value, search: searchQuery.value })
  }

  async function updateFacets(search?: string) {
    const res = await bookApi.getFacets(search)
    facets.value = res.facets
  }

  async function getBook(id: number) {
    currentBook.value = await bookApi.getById(id)
  }

  async function applyFilter(filter: Partial<BookListParams>) {
    page.value = 1
    activeFilters.value = { ...activeFilters.value, ...filter }
    await search({ ...activeFilters.value, search: searchQuery.value })
  }

  return { results, total, page, limit, loading, currentBook, facets, searchQuery, activeFilters,
    search, goTo, updateFacets, getBook, applyFilter }
})
