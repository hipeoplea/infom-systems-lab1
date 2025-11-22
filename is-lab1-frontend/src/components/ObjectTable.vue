<template>
  <div class="container">
    <h1>Objects</h1>

    <div class="card toolbar">
      <input v-model="filter" class="input" placeholder="Search (supports partial match)" @input="debouncedLoad" />
      <select v-model="sortBy" @change="load">
        <option value="">-- sort by --</option>
        <option v-for="c in columns" :key="c" :value="c">{{ c }}</option>
      </select>
      <select v-model="sortDir" @change="load">
        <option value="asc">asc</option>
        <option value="desc">desc</option>
      </select>
      <button class="btn btn-primary" @click="openCreate">Create</button>
      <div style="margin-left:auto">
        <button class="btn" @click="refresh">Refresh</button>
      </div>
    </div>

    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th v-for="col in columns" :key="col">{{ col }}</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in items" :key="item.id">
            <td v-for="col in columns" :key="col" :data-label="col">{{ format(item, col) }}</td>
            <td class="actions" data-label="Actions">
              <button class="btn btn-secondary" @click="$emit('view', item.id)">View</button>
              <button class="btn btn-secondary" @click="$emit('edit', item.id)">Edit</button>
              <button class="btn btn-danger" @click="remove(item.id)">Delete</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div style="display:flex; gap:8px; align-items:center; margin-top:12px">
      <button class="btn" :disabled="page===1" @click="page--, load()">Prev</button>
      <div>Page {{ page }} / {{ totalPages }}</div>
      <button class="btn" :disabled="page>=totalPages" @click="page++, load()">Next</button>
      <div class="small-muted">Total: {{ total }}</div>
    </div>

  </div>
</template>

<script>
import { ref } from 'vue'
import { fetchObjects, deleteObject } from '../api'
import debounce from 'lodash/debounce'

export default {
  name: 'ObjectTable',
  props: {
    columns: { type: Array, required: true },
    formatters: { type: Object, default: () => ({}) }
  },
  emits: ['view','edit','create','error'],
  setup(props, { emit }){
    const items = ref([])
    const page = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const totalPages = ref(1)
    const filter = ref('')
    const sortBy = ref('')
    const sortDir = ref('asc')

    const load = async () => {
      try {
        const data = await fetchObjects({
          page: page.value,
          pageSize: pageSize.value,
          filter: filter.value,
          sortBy: sortBy.value,
          sortDir: sortDir.value
        })
        if (Array.isArray(data)) {
          items.value = data
          total.value = data.length
          totalPages.value = 1
          page.value = 1
        } else {
          const arr = data.items || []
          const tot = typeof data.total === 'number' ? data.total : arr.length
          items.value = arr
          total.value = tot
          const pages = Math.ceil((tot || 0) / (pageSize.value || 1))
          totalPages.value = Math.max(1, pages || 1)
          if (page.value > totalPages.value) page.value = totalPages.value
        }
      } catch (e) {
        emit('error', e.response?.data?.message || e.message)
      }
    }

    const debouncedLoad = debounce(() => { page.value = 1; load() }, 350)
    const refresh = () => load()
    const openCreate = () => emit('create')
    const remove = async (id) => {
      if (!confirm('Delete item?')) return
      try {
        await deleteObject(id)
        await load()
      } catch (e) { emit('error', e.response?.data?.message || e.message) }
    }

    const format = (item, col) => {
      const f = props.formatters[col]
      if (typeof f === 'function') return f(item[col], item)
      if (col === 'creationDate') return item[col] ? new Date(item[col]).toLocaleDateString() : ''
      return item[col]
    }

    load()

    return { items, page, pageSize, total, totalPages, filter, sortBy, sortDir, load, debouncedLoad, refresh, openCreate, remove, format }
  }
}
</script>

<style scoped>
.table-wrap { width: 100%; }
</style>
