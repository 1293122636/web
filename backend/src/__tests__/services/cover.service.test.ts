import { describe, it, expect, vi } from 'vitest'
import { resolveCover } from '../../services/cover.service.js'

describe('resolveCover', () => {
  it('OpenLibrary 可访问时返回 URL', async () => {
    // Mock fetch
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      headers: { get: () => 'image/jpeg' },
    })

    const url = await resolveCover('9787111584445')
    expect(url).toContain('openlibrary.org')
    expect(url).toContain('9787111584445')
  })

  it('OpenLibrary 失败返回 null', async () => {
    global.fetch = vi.fn().mockRejectedValue(new Error('network'))

    const url = await resolveCover('0000000000000')
    expect(url).toBeNull()
  })

  it('非图片响应返回 null', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      headers: { get: () => 'text/html' },
    })

    const url = await resolveCover('0000000000000')
    expect(url).toBeNull()
  })
})
