import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import HoldingsTable from '../../components/HoldingsTable.vue'

describe('HoldingsTable', () => {
  it('renders empty state when no items', () => {
    const wrapper = mount(HoldingsTable, { props: { items: [] } })
    expect(wrapper.text()).toContain('No Data')
  })

  it('renders all columns for item row', () => {
    const wrapper = mount(HoldingsTable, {
      props: {
        items: [{
          id: 1, barcode: 'LIB-001', callNumber: 'TP/1',
          location: 'Shelf A', campus: '青岛', status: 'available', requests: 3, price: 35.0
        }]
      }
    })
    expect(wrapper.text()).toContain('TP/1')       // 索书号
    expect(wrapper.text()).toContain('Shelf A')    // 馆藏地
    expect(wrapper.text()).toContain('青岛')       // 校区
    expect(wrapper.text()).toContain('3')          // 请求数
    expect(wrapper.text()).toContain('在架')       // StatusBadge
  })

  it('renders StatusBadge for borrowed item', () => {
    const wrapper = mount(HoldingsTable, {
      props: {
        items: [{
          id: 2, barcode: 'LIB-002', callNumber: 'TP/2',
          location: 'Shelf B', campus: '泰安', status: 'borrowed', requests: 0
        }]
      }
    })
    expect(wrapper.text()).toContain('借出')
  })

  it('renders StatusBadge for on_hold item', () => {
    const wrapper = mount(HoldingsTable, {
      props: {
        items: [{
          id: 3, barcode: 'LIB-003', callNumber: 'TP/3',
          location: 'Shelf C', campus: '济南', status: 'on_hold', requests: 1
        }]
      }
    })
    expect(wrapper.text()).toContain('预约中')
  })

  it('renders multiple items', () => {
    const wrapper = mount(HoldingsTable, {
      props: {
        items: [
          { id: 1, barcode: 'LIB-001', callNumber: 'TP/1', location: 'A', campus: '青岛', status: 'available', requests: 0 },
          { id: 2, barcode: 'LIB-002', callNumber: 'TP/2', location: 'B', campus: '泰安', status: 'borrowed', requests: 1 },
        ]
      }
    })
    expect(wrapper.text()).toContain('TP/1')
    expect(wrapper.text()).toContain('TP/2')
  })
})
