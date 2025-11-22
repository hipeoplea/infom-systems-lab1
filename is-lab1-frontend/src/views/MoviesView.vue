<template>
  <div>
    <div class="container" style="margin-bottom: 16px">
      <div class="card toolbar" style="align-items:center">
        <div style="font-weight:600; letter-spacing:.2px; color:#cbd5e1">Работа с фильмами</div>
        <div class="ml-auto" style="display:flex; gap:8px; flex-wrap:wrap">
          <button class="btn btn-secondary" @click="runOps.uniqueGenres">Уникальные жанры</button>
          <button class="btn btn-secondary" @click="runOps.countGolden">Count goldenPalm</button>
          <button class="btn btn-secondary" @click="runOps.countUsaGreater">Count USA boxOffice ></button>
          <button class="btn btn-primary" @click="runOps.addOscarR">+1 Оскар для R</button>
          <button class="btn btn-danger" @click="openRemoveOscarsModal">Снять Оскары по жанру</button>
        </div>
      </div>
    </div>

    <ObjectTable ref="tableRef" :columns="columns" :formatters="formatters" @view="onView" @edit="onEdit" @create="onCreate" @error="onError" />

    <ObjectForm v-if="formVisible" :id="editingId" :mode="formMode" @saved="onSaved" @close="closeForm" @error="onError" />
    <ObjectDetail v-if="detailVisible" :id="detailId" @close="closeDetail" />

    <div v-if="toast" class="toast">{{toast}}</div>

    <div v-if="removeOscarsOpen" class="modal">
      <div class="card">
        <h3>Снять Оскары у фильмов режиссёров жанра</h3>
        <div class="form-field" style="margin-top:10px">
          <label>Жанр</label>
          <select v-model="selectedGenre" class="input">
            <option v-for="g in genres" :key="g" :value="g">{{ g }}</option>
          </select>
        </div>
        <div class="mt-12" style="display:flex; gap:8px; justify-content:flex-end">
          <button class="btn btn-primary" :disabled="!selectedGenre" @click="confirmRemoveOscars">Выполнить</button>
          <button class="btn btn-secondary" @click="removeOscarsOpen=false">Закрыть</button>
        </div>
      </div>
    </div>
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
    const columns = ['name','genre','mpaaRating','oscarsCount','goldenPalmCount','usaBoxOffice','length','budget']
    const formVisible = ref(false)
    const formMode = ref('create')
    const editingId = ref(null)
    const detailVisible = ref(false)
    const detailId = ref(null)
    const toast = ref('')
    const tableRef = ref(null)
    const removeOscarsOpen = ref(false)
    const genres = ref([])
    const selectedGenre = ref('')

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
    const onError = (err) => { toast.value = typeof err === 'string' ? err : (err?.message || String(err)); setTimeout(()=>toast.value='',4000) }

    const runOps = {
      uniqueGenres: async ()=>{ const res = await api.op_uniqueGenres(); toast.value = `Genres: ${res.join(', ')}` },
      countGolden: async ()=>{ const v = prompt('Введите значение goldenPalmCount'); if(v!=null){ const res = await api.op_countGoldenPalm(parseInt(v)); toast.value = `Count = ${res}` } },
      countUsaGreater: async ()=>{ const v = prompt('Введите порог USA boxOffice'); if(v!=null){ const res = await api.op_usaBoxOfficeGreater(parseInt(v)); toast.value = `Count = ${res}` } },
      addOscarR: async ()=>{ if(confirm('Добавить 1 оскар всем фильмам с рейтингом R?')){ await api.op_addOscarToR(); toast.value = 'Операция выполнена'; tableRef.value.load() } },
    }

    const openRemoveOscarsModal = async () => {
      try {
        genres.value = await api.op_uniqueGenres()
        selectedGenre.value = genres.value[0] || ''
        removeOscarsOpen.value = true
      } catch(e) {
        toast.value = e.response?.data?.message || e.message
        setTimeout(()=>toast.value='', 3000)
      }
    }

    const confirmRemoveOscars = async () => {
      if (!selectedGenre.value) return
      await api.op_removeOscarsByDirectorsGenre(selectedGenre.value)
      toast.value = 'Операция выполнена'
      removeOscarsOpen.value = false
      tableRef.value?.load?.()
    }

    const formatters = {}

    onMounted(()=>{})

    return { columns, formatters, formVisible, formMode, editingId, detailVisible, detailId, onCreate, onEdit, onView, closeForm, closeDetail, onSaved, onError, toast, tableRef, runOps, removeOscarsOpen, openRemoveOscarsModal, confirmRemoveOscars, genres, selectedGenre }
  }
}
</script>
