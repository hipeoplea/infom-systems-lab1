<template>
  <div>
    <ResourceTable
      ref="tableRef"
      resource="locations"
      title="Локации"
      :columns="columns"
      @create="onCreate"
      @edit="onEdit"
      @error="onError"
    />

    <ResourceForm
      v-if="formVisible"
      :id="editingId"
      resource="locations"
      title="локацию"
      :fields="fields"
      :mode="formMode"
      :to-payload="(f)=>f"
      :from-entity="(e)=>({ x: e.x, y: e.y, name: e.name })"
      @saved="onSaved"
      @close="closeForm"
      @error="onError"
    />

    <div v-if="toast" class="toast">{{toast}}</div>
  </div>
</template>

<script>
import { ref } from 'vue'
import ResourceTable from '../components/ResourceTable.vue'
import ResourceForm from '../components/ResourceForm.vue'
import { createWS } from '../websocket'

export default {
  components: { ResourceTable, ResourceForm },
  setup(){
    const columns = ['id','x','y','name']
    const fields = [
      { key: 'x', label: 'X', type: 'number', required: true },
      { key: 'y', label: 'Y', type: 'number', required: true },
      { key: 'name', label: 'Название', required: true },
    ]

    const formVisible = ref(false)
    const formMode = ref('create')
    const editingId = ref(null)
    const toast = ref('')
    const tableRef = ref(null)

    const onCreate = () => { formMode.value = 'create'; editingId.value = null; formVisible.value = true }
    const onEdit = (id) => { formMode.value = 'edit'; editingId.value = id; formVisible.value = true }
    const closeForm = () => formVisible.value = false
    const onSaved = async () => { formVisible.value = false; tableRef.value?.load?.(); toast.value = 'Сохранено'; setTimeout(()=>toast.value='',2000) }
    const onError = (err) => { toast.value = typeof err === 'string' ? err : (err?.message || String(err)); setTimeout(()=>toast.value='',4000) }

    const ws = createWS((msg)=>{
      if (tableRef.value && tableRef.value.load) tableRef.value.load()
      toast.value = `Event: ${msg.type} id=${msg.id}`
      setTimeout(()=>toast.value='',2000)
    })

    return { columns, fields, formVisible, formMode, editingId, onCreate, onEdit, closeForm, onSaved, onError, toast, tableRef }
  }
}
</script>
