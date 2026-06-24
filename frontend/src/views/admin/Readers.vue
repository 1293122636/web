<template>
  <div>
    <n-h1 prefix="bar" style="margin-bottom: 20px;"><n-text type="primary">读者管理</n-text></n-h1>

    <n-data-table
      :columns="columns"
      :data="readers"
      :loading="loading"
      :row-key="(r: DataRow) => r.id"
      :expanded-row-keys="expandedKeys"
      @update:expanded-row-keys="onExpand"
    />

    <n-modal v-model:show="showModal" title="编辑读者" preset="card" style="width: 420px;">
      <n-form :model="form">
        <n-form-item label="姓名"><n-input v-model:value="form.name" /></n-form-item>
        <n-form-item label="手机号"><n-input v-model:value="form.phone" /></n-form-item>
        <n-form-item label="邮箱"><n-input v-model:value="form.email" /></n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="saving" @click="handleSave">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import type { DataRow } from '../../types/api'
import { ref, reactive, onMounted, h } from 'vue'
import { useMessage, NButton, NTag } from 'naive-ui'
import { api } from '../../api'
import type { ReaderResponse, BorrowRecordResponse } from '../../types/api'
import type { DataTableColumns } from 'naive-ui'

const message = useMessage()
const readers = ref<ReaderResponse[]>([])
const loading = ref(false)
const expandedKeys = ref<number[]>([])

const columns: DataTableColumns<Record<string, unknown>> = [
  { type: 'expand', renderExpand: (row) => h(ExpandPanel, { readerId: row.id }) },
  { title: '用户名', key: 'username', width: 120 },
  { title: '姓名', key: 'name', width: 100 },
  { title: '手机', key: 'phone', width: 130 },
  { title: '邮箱', key: 'email', ellipsis: { tooltip: true } },
  { title: '借阅数', key: '_count.borrowRecords', width: 70 },
  { title: '注册时间', key: 'createdAt', width: 160, render: (r) => new Date(r.createdAt).toLocaleDateString('zh-CN') },
  {
    title: '操作', key: 'actions', width: 80,
    render(row) {
      return h(NButton, { size: 'small', onClick: () => openEdit(row) }, () => '编辑')
    }
  }
]

async function fetchReaders() {
  loading.value = true
  try { readers.value = (await api.get<{ readers: ReaderResponse[]; total: number }>('/readers')).readers } catch {}
  loading.value = false
}

function onExpand(keys: number[]) { expandedKeys.value = keys }

const showModal = ref(false)
const saving = ref(false)
const form = reactive<{ id: number | null; name: string; phone: string; email: string }>({ id: null, name: '', phone: '', email: '' })

function openEdit(row: DataRow) { Object.assign(form, { id: row.id, name: row.name, phone: row.phone, email: row.email }); showModal.value = true }

async function handleSave() {
  saving.value = true
  try {
    await api.put(`/readers/${form.id}`, { name: form.name, phone: form.phone, email: form.email })
    message.success('已更新')
    showModal.value = false
    fetchReaders()
  } catch (e: unknown) { message.error((e as Error).message) }
  saving.value = false
}

// Expand panel component (inline)
const ExpandPanel = {
  props: { readerId: Number },
  setup(props: Record<string, unknown>) {
    const records = ref<BorrowRecordResponse[]>([])
    const load = async () => {
      try {
        const res = await api.get<ReaderResponse>(`/readers/${props.readerId}`)
        records.value = res.borrowRecords || []
      } catch {}
    }
    load()
    return () => records.value.length === 0
      ? h('div', { style: 'padding:12px;color:#8a8f98;' }, '暂无借阅记录')
      : h('div', { style: 'padding:8px 0;' },
          records.value.map((r: BorrowRecordResponse) =>
            h('div', { style: 'display:flex;gap:16px;padding:6px 12px;font-size:13px;color:#d0d6e0;' }, [
              h('span', r.book?.title || '未知'),
              h('span', { style: 'color:#8a8f98;' }, new Date(r.borrowDate).toLocaleDateString('zh-CN')),
              h(NTag, { size: 'tiny', type: r.status === 'active' ? 'success' : 'default' }, () => r.status === 'active' ? '在借' : '已还')
            ])
          )
        )
  }
}

onMounted(() => fetchReaders())
</script>
