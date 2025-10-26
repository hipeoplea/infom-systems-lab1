<template>
  <div class="container">
    <h1>{{ title }}</h1>

    <div class="card toolbar">
      <input class="input" v-model="filter" :placeholder="placeholder" @input="debouncedLoad" />
      <select v-model="sortBy" @change="load">
        <option value="">-- sort by --</option>
        <option v-for="c in columns" :key="c" :value="c">{{ c }}</option>
      </select>
      <select v-model="sortDir" @change="load">
        <option value="asc">asc</option>
        <option value="desc">desc</option>
      </select>
      <button class="btn btn-primary" @click="$emit('create')">Create</button>
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
            <td v-for="col in columns" :key="col">{{ format(item, col) }}</td>
            <td>
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
import debounce from 'lodash/debounce'
import { list, deleteOne } from '../api'

export default {
  name: 'ResourceTable',
  props: {
    resource: { type: String, required: true },
    columns: { type: Array, required: true },
    title: { type: String, default: '' },
    placeholder: { type: String, default: 'Search' },
    formatters: { type: Object, default: () => ({}) }
  },
  emits: ['create','edit','error'],
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
        const data = await list(props.resource, {
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
        } else {
          items.value = data.items
          total.value = data.total
          totalPages.value = Math.max(1, Math.ceil(data.total / pageSize.value))
        }
      } catch (e) {
        emit('error', e.response?.data || e.message)
      }
    }

    const debouncedLoad = debounce(() => { page.value = 1; load() }, 350)
    const refresh = () => load()
    const remove = async (id) => {
      if (!confirm('Delete item?')) return
      try {
        await deleteOne(props.resource, id)
        await load()
      } catch (e) { emit('error', e.response?.data || e.message) }
    }

    const format = (item, col) => {
      const f = props.formatters[col]
      if (typeof f === 'function') return f(item[col], item)
      return item[col]
    }

    load()

    return { items, page, pageSize, total, totalPages, filter, sortBy, sortDir, load, debouncedLoad, refresh, remove, format }
  }
}
</script>

