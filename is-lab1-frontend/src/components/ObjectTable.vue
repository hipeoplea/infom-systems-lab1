<template>
  <div class="container">
    <h1>Objects</h1>

    <div class="card toolbar">
      <input class="input" v-model="filter" placeholder="Search (supports partial match)" @input="debouncedLoad" />
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
            <td v-for="col in columns" :key="col" :data-label="col">
              <template v-if="col==='creationDate'">{{ new Date(item[col]).toLocaleDateString() }}</template>
              <template v-else>{{ item[col] }}</template>
            </td>
            <td class="actions" data-label="Actions">
              <button class="btn btn-secondary" @click="$emit('view', item.id)">View</button>
              <button class="btn btn-secondary" @click="$emit('edit', item.id)">Edit</button>
              <button class="btn btn-danger" @click="remove(item.id)">Delete</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

  </div>
</template>

<script>
import { ref } from 'vue'
import { fetchObjects, deleteObject } from '../api'
import debounce from 'lodash/debounce'

export default {
  name: 'ObjectTable',
  props: { columns: { type: Array, required: true } },
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
        items.value = data.items
        total.value = data.total
        totalPages.value = Math.max(1, Math.ceil(data.total / pageSize.value))
      } catch (e) {
        emit('error', e.response?.data || e.message)
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
      } catch (e) { emit('error', e.response?.data || e.message) }
    }

    load()

    return { items, page, pageSize, total, totalPages, filter, sortBy, sortDir, load, debouncedLoad, refresh, openCreate, remove }
  }
}
</script>

<style scoped>
.table-wrap { width: 100%; }
</style>
