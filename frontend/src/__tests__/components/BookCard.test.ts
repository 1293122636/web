import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import BookCard from '../../components/BookCard.vue'

describe('BookCard', () => {
  const baseBook = {
    id: 1, isbn: '978-test', title: 'Test Book', author: 'Test Author',
    total: 3, available: 2, status: 'available' as const,
    category: { id: 1, name: 'Test' },
  }

  it('renders title and author', () => {
    const wrapper = mount(BookCard, { props: { book: baseBook } })
    expect(wrapper.text()).toContain('Test Book')
    expect(wrapper.text()).toContain('Test Author')
  })

  it('shows available count', () => {
    const wrapper = mount(BookCard, { props: { book: baseBook } })
    expect(wrapper.text()).toContain('2/3可借')
  })

  it('renders StatusBadge for available book', () => {
    const wrapper = mount(BookCard, { props: { book: baseBook } })
    expect(wrapper.text()).toContain('在架')
  })

  it('renders StatusBadge for borrowed book', () => {
    const wrapper = mount(BookCard, {
      props: { book: { ...baseBook, available: 0, status: 'borrowed' as const } }
    })
    expect(wrapper.text()).toContain('借出')
  })

  it('shows cover placeholder (first letter) when no cover URL', () => {
    const wrapper = mount(BookCard, { props: { book: baseBook } })
    expect(wrapper.find('.cover-placeholder').exists()).toBe(true)
    expect(wrapper.find('.cover-placeholder').text()).toBe('T')
  })

  it('renders cover image when cover URL is provided', () => {
    const wrapper = mount(BookCard, {
      props: { book: { ...baseBook, cover: 'https://example.com/cover.jpg' } }
    })
    expect(wrapper.find('.cover-img').exists()).toBe(true)
  })

  it('emits click event when card is clicked', async () => {
    const wrapper = mount(BookCard, { props: { book: baseBook } })
    await wrapper.find('.book-card').trigger('click')
    expect(wrapper.emitted('click')).toBeTruthy()
  })
})
