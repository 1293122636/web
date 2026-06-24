<template>
  <div>
    <n-h1 prefix="bar" style="margin-bottom: 20px;"><n-text type="primary">分类管理</n-text></n-h1>

    <n-space justify="end" style="margin-bottom: 16px;">
      <n-button type="primary" @click="openCreate">添加分类</n-button>
    </n-space>

    <n-data-table :columns="columns" :data="categories" :loading="loading" :row-key="(r: DataRow) => r.id" />

    <n-modal v-model:show="showModal" :title="editingId ? '编辑分类' : '添加分类'" preset="card" style="width: 420px;">
      <n-form ref="formRef" :model="form" :rules="rulesData">
        <n-form-item label="名称" path="name"><n-input v-model:value="form.name" placeholder="分类名称" /></n-form-item>
        <n-form-item label="描述"><n-input v-model:value="form.desc" placeholder="可选" /></n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="saving" @click="handleSave">{{ editingId ? '保存' : '添加' }}</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { useMessage, NButton, NPopconfirm } from 'naive-ui'
import { api } from '../../api'
import type { CategoryResponse, DataRow } from '../../types/api'
import type { DataTableColumns } from 'naive-ui'

const message = useMessage()
const categories = ref<CategoryResponse[]>([])
const loading = ref(false)

const columns: DataTableColumns<Record<string, unknown>> = [
  { title: '名称', key: 'name', width: 200 },
  { title: '描述', key: 'desc', ellipsis: { tooltip: true } },
  { title: '图书数', key: '_count.books', width: 80 },
  {
    title: '操作', key: 'actions', width: 140,
    render(row) {
      return h('div', { style: 'display:flex;gap:8px' }, [
        h(NButton, { size: 'small', onClick: () => openEdit(row) }, () => '编辑'),
        h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, {
          trigger: () => h(NButton, { size: 'small', type: 'error', text: true }, () => '删除'),
          default: () => '确认删除？若该分类下有图书则无法删除。'
        })
      ])
    }
  }
]

async function fetchCategories() {
  loading.value = true
  try { categories.value = await api.get('/categories') } catch {}
  loading.value = false
}

const showModal = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const form = reactive({ name: '', desc: '' })
const rulesData = { name: [{ required: true, message: '必填' }] }

function openCreate() { editingId.value = null; form.name = ''; form.desc = ''; showModal.value = true }
function openEdit(row: DataRow) { editingId.value = row.id; form.name = row.name; form.desc = row.desc || ''; showModal.value = true }

async function handleSave() {
  saving.value = true
  try {
    if (editingId.value) {
      await api.put(`/categories/${editingId.value}`, { name: form.name, desc: form.desc })
      message.success('已更新')
    } else {
      await api.post('/categories', { name: form.name, desc: form.desc })
      message.success('已添加')
    }
    showModal.value = false
    fetchCategories()
  } catch (e: unknown) { message.error((e as Error).message) }
  saving.value = false
}

async function handleDelete(id: number) {
  try { await api.delete(`/categories/${id}`); message.success('已删除'); fetchCategories() }
  catch (e: unknown) { message.error((e as Error).message) }
}

onMounted(() => fetchCategories())
</script>
