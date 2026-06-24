<template>
  <div class="circulation">
    <n-h2>流通台</n-h2>
    <n-grid :cols="2" :x-gap="16">
      <n-grid-item>
        <n-card title="扫描输入" size="small">
          <BarcodeInput placeholder="扫描读者条码或图书条码..." @scan="onScan" @clear="clearCurrent" />
          <n-divider />
          <n-descriptions v-if="currentItem" :column="1" size="small" bordered>
            <n-descriptions-item label="条码">{{ currentItem.barcode }}</n-descriptions-item>
            <n-descriptions-item label="书名">{{ currentItem.book?.title }}</n-descriptions-item>
            <n-descriptions-item label="状态">
              <StatusBadge :status="currentItem.status" />
            </n-descriptions-item>
            <n-descriptions-item label="当前位置">{{ currentBorrow ? '借出 (' + currentBorrow.userId + ')' : '在架' }}</n-descriptions-item>
          </n-descriptions>
          <n-space v-if="currentItem" style="margin-top:12px">
            <n-button v-if="!currentBorrow" type="success" @click="addToQueue('borrow')">借书</n-button>
            <n-button v-if="currentBorrow" type="warning" @click="addToQueue('return')">还书</n-button>
          </n-space>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card title="操作队列" size="small">
          <div v-if="queue.length === 0" class="empty">扫描条码开始操作</div>
          <n-list v-else>
            <n-list-item v-for="(op, i) in queue" :key="i">
              <n-space align="center">
                <n-tag :type="op.action === 'borrow' ? 'success' : 'warning'" size="small">{{ op.action === 'borrow' ? '借' : '还' }}</n-tag>
                <span>{{ op.item?.book?.title }} ({{ op.barcode }})</span>
                <n-button text size="tiny" @click="queue.splice(i,1)">✕</n-button>
              </n-space>
            </n-list-item>
          </n-list>
          <n-space v-if="queue.length > 0" style="margin-top:12px">
            <n-button type="primary" @click="commitAll">确认全部</n-button>
            <n-button @click="queue = []">清空</n-button>
          </n-space>
        </n-card>
      </n-grid-item>
    </n-grid>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { NH2, NGrid, NGridItem, NCard, NDescriptions, NDescriptionsItem, NButton, NSpace, NTag, NList, NListItem, NDivider, useMessage } from 'naive-ui'
import BarcodeInput from '../../components/BarcodeInput.vue'
import StatusBadge from '../../components/StatusBadge.vue'
import { request } from '../../api'

const message = useMessage()
const currentItem = ref<any>(null)
const currentBorrow = ref<any>(null)
const queue = ref<any[]>([])

async function onScan(barcode: string) {
  try {
    const data = await request<any>(`/api/book-items/${encodeURIComponent(barcode)}`)
    currentItem.value = data.item
    currentBorrow.value = data.currentBorrow
    playBeep(data.currentBorrow ? 'return' : 'borrow')
  } catch { message.error('条码未找到') }
}

function addToQueue(action: string) {
  if (!currentItem.value) return
  queue.value.push({ action, barcode: currentItem.value.barcode, item: currentItem.value, currentBorrow: currentBorrow.value })
  currentItem.value = null
  currentBorrow.value = null
}

function clearCurrent() { currentItem.value = null; currentBorrow.value = null }

async function commitAll() {
  for (const op of queue.value) {
    try {
      if (op.action === 'borrow') {
        await request('/api/borrows/borrow', { method: 'POST', body: { bookItemId: op.item.id }, auth: true })
      } else {
        const data = await request<any>(`/api/borrows/return/${op.currentBorrow?.id}`, { method: 'POST', auth: true })
        if (data.fine) message.warning(`逾期罚款: ¥${data.fine.amount}`)
      }
      message.success(op.action === 'borrow' ? '借书成功' : '还书成功')
    } catch { message.error(`操作失败: ${op.barcode}`) }
  }
  queue.value = []
}

function playBeep(type: string) {
  try {
    const ctx = new AudioContext()
    const osc = ctx.createOscillator()
    osc.frequency.value = type === 'borrow' ? 880 : 660
    osc.connect(ctx.destination); osc.start(); osc.stop(ctx.currentTime + 0.1)
  } catch (e) { console.error('scan failed:', e) }
}
</script>

<style scoped>
.circulation { padding: 16px; }
.empty { text-align: center; color: var(--n-text-color-3); padding: 24px; }
</style>
