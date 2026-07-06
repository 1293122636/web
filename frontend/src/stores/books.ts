import { defineStore } from 'pinia'
import { ref } from 'vue'
import { bookApi } from '../api/books'
import type { BookSummary, BookDetail, BookListParams, FacetsResponse } from '../types/api'

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
      const { data } = await bookApi.list({ ...params, page: page.value, limit: limit.value })
      results.value = data.books || []
      total.value = data.total || 0
      page.value = data.page || 1
      limit.value = data.limit || 20
    } finally { loading.value = false }
  }

  async function goTo(p: number) {
    page.value = p
    await search({ ...activeFilters.value, search: searchQuery.value })
  }

  async function updateFacets(params: BookListParams = {}) {
    try {
      const { data } = await bookApi.getFacets(params)
      facets.value = data.facets || null
    } catch {
      facets.value = null
    }
  }

  async function getBook(id: number) {
    try {
      const { data } = await bookApi.getById(id)
      currentBook.value = data
    } catch {
      currentBook.value = null
    }
  }

  async function applyFilter(filter: Partial<BookListParams>) {
    page.value = 1
    activeFilters.value = { ...activeFilters.value, ...filter }
    await search({ ...activeFilters.value, search: searchQuery.value })
  }

  return { results, total, page, limit, loading, currentBook, facets, searchQuery, activeFilters,
    search, goTo, updateFacets, getBook, applyFilter }
})
