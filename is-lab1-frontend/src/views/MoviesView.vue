<template>
  <div>
    <div class="container" style="margin-bottom: 16px">
      <div class="card toolbar" style="align-items:center">
        <div style="font-weight:600; letter-spacing:.2px; color:#cbd5e1">Работа с фильмами</div>
        <div class="ml-auto" style="display:flex; gap:8px; flex-wrap:wrap">
          <button class="btn btn-secondary" @click="runOps.uniqueGenres">Уникальные жанры</button>
          <button class="btn btn-secondary" @click="runOps.countGolden">Count goldenPalm</button>
          <button class="btn btn-primary" @click="runOps.addOscarR">+1 Оскар для R</button>
        </div>
      </div>
    </div>

    <ObjectTable :columns="columns" @view="onView" @edit="onEdit" @create="onCreate" @error="onError" ref="tableRef" />

    <ObjectForm v-if="formVisible" :mode="formMode" :id="editingId" @saved="onSaved" @close="closeForm" @error="onError" />
    <ObjectDetail v-if="detailVisible" :id="detailId" @close="closeDetail" />

    <div v-if="toast" class="toast">{{toast}}</div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import ObjectTable from '../components/ObjectTable.vue'
import ObjectForm from '../components/ObjectForm.vue'
import ObjectDetail from '../components/ObjectDetail.vue'
import { createWS } from '../websocket'
import * as api from '../api'

export default {
  components: { ObjectTable, ObjectForm, ObjectDetail },
  setup(){
    const columns = ['id','name','genre','mpaaRating','goldenPalmCount','usaBoxOffice','totalBoxOffice','length','budget','creationDate']
    const formVisible = ref(false)
    const formMode = ref('create')
    const editingId = ref(null)
    const detailVisible = ref(false)
    const detailId = ref(null)
    const toast = ref('')
    const tableRef = ref(null)

    const ws = createWS((msg)=>{
      if(tableRef.value && tableRef.value.load) tableRef.value.load()
      toast.value = `Событие: ${msg.type} id=${msg.id}`
      setTimeout(()=>toast.value='', 2500)
    })

    const onCreate = () => { formMode.value = 'create'; editingId.value = null; formVisible.value = true }
    const onEdit = (id) => { formMode.value = 'edit'; editingId.value = id; formVisible.value = true }
    const onView = (id) => { detailId.value = id; detailVisible.value = true }
    const closeForm = () => formVisible.value = false
    const closeDetail = () => detailVisible.value = false
    const onSaved = async () => { formVisible.value = false; await tableRef.value.load(); toast.value = 'Сохранено'; setTimeout(()=>toast.value='',2000) }
    const onError = (err) => { toast.value = JSON.stringify(err); setTimeout(()=>toast.value='',4000) }

    const runOps = {
      uniqueGenres: async ()=>{ const res = await api.op_uniqueGenres(); toast.value = `Genres: ${res.join(', ')}` },
      countGolden: async ()=>{ const v = prompt('Введите значение goldenPalmCount'); if(v!=null){ const res = await api.op_countGoldenPalm(parseInt(v)); toast.value = `Count = ${res}` } },
      addOscarR: async ()=>{ if(confirm('Добавить 1 оскар всем фильмам с рейтингом R?')){ await api.op_addOscarToR(); toast.value = 'Операция выполнена'; tableRef.value.load() } }
    }

    onMounted(()=>{})

    return { columns, formVisible, formMode, editingId, detailVisible, detailId, onCreate, onEdit, onView, closeForm, closeDetail, onSaved, onError, toast, tableRef, runOps }
  }
}
</script>

