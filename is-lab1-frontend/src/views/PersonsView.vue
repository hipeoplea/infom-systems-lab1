<template>
  <div>
    <ResourceTable
      resource="persons"
      title="Persons"
      :columns="columns"
      :formatters="formatters"
      @create="onCreate"
      @edit="onEdit"
      @error="onError"
      ref="tableRef"
    />

    <ResourceForm
      v-if="formVisible"
      resource="persons"
      title="Person"
      :fields="fields"
      :mode="formMode"
      :id="editingId"
      :toPayload="toPayload"
      :fromEntity="fromEntity"
      @saved="onSaved"
      @close="closeForm"
      @error="onError"
    />

    <div v-if="toast" class="toast">{{ toast }}</div>
  </div>
</template>

<script>
import { ref } from 'vue'
import ResourceTable from '../components/ResourceTable.vue'
import ResourceForm from '../components/ResourceForm.vue'
import { createWS } from '../websocket'
import { list } from '../api'

export default {
  components: { ResourceTable, ResourceForm },
  setup(){
    const columns = ['id','name','eyeColor','hairColor','nationality','passportID','location']
    const fields = ref([
      { key: 'name', label: 'Name', required: true },
      { key: 'eyeColor', label: 'Eye Color', type: 'select', required: true, options: ['RED','BLACK','BLUE','ORANGE'] },
      { key: 'hairColor', label: 'Hair Color', type: 'select', options: ['RED','BLACK','BLUE','ORANGE'] },
      { key: 'nationality', label: 'Nationality', type: 'select', options: ['USA','GERMANY','SPAIN','CHINA','VATICAN'] },
      { key: 'passportID', label: 'Passport ID', required: false },
      { key: 'locationId', label: 'Location', type: 'select', options: [], nullable: true },
    ])

    const formVisible = ref(false)
    const formMode = ref('create')
    const editingId = ref(null)
    const toast = ref('')
    const tableRef = ref(null)

    const onCreate = () => { formMode.value = 'create'; editingId.value = null; formVisible.value = true }
    const onEdit = (id) => { formMode.value = 'edit'; editingId.value = id; formVisible.value = true }
    const closeForm = () => { formVisible.value = false }
    const onSaved = async () => {
      formVisible.value = false
      tableRef.value?.load?.()
      toast.value = 'Saved'
      setTimeout(()=>toast.value='',2000)
    }
    const onError = (err) => {
      toast.value = typeof err === 'string' ? err : JSON.stringify(err)
      setTimeout(()=>toast.value='',4000)
    }

    const toPayload = (f) => ({
      name: f.name,
      eyeColor: f.eyeColor,
      hairColor: f.hairColor || null,
      nationality: f.nationality || null,
      passportID: f.passportID || null,
      location: f.locationId ? { id: f.locationId } : null,
    })
    const fromEntity = (e) => ({
      name: e.name,
      eyeColor: e.eyeColor,
      hairColor: e.hairColor ?? null,
      nationality: e.nationality ?? null,
      passportID: e.passportID ?? '',
      locationId: e.location?.id ?? null,
    })
    const formatters = { location: (v, item) => item.location?.id ?? '' }

    const ws = createWS((msg)=>{
      if (tableRef.value && tableRef.value.load) tableRef.value.load()
      toast.value = `Event: ${msg.type} id=${msg.id}`
      setTimeout(()=>toast.value='', 2000)
    })

    // Load locations for select options
    ;(async () => {
      try {
        const data = await list('locations', { page: 1, pageSize: 1000 })
        const items = Array.isArray(data) ? data : (data.items || [])
        const opts = items.map(l => ({ value: l.id, label: `${l.id}${l.name?': '+l.name:''}` }))
        const idx = fields.value.findIndex(f => f.key === 'locationId')
        if (idx >= 0) fields.value[idx] = { ...fields.value[idx], options: opts, nullable: true }
      } catch (e) { /* ignore load errors; form will show no options */ }
    })()

    return { columns, fields, formVisible, formMode, editingId, onCreate, onEdit, closeForm, onSaved, onError, toast, tableRef, toPayload, fromEntity, formatters }
  }
}
</script>

