<template>
  <div class="modal">
    <div class="card">
      <h3>{{ mode === 'create' ? 'Create Movie' : 'Edit Movie' }}</h3>

      <div class="form-grid">
        <div class="form-field">
          <label>Name</label>
          <input v-model="form.name" class="input" />
          <div v-if="errors.name" class="error">{{errors.name}}</div>
        </div>

        <div class="form-field">
          <label>Genre</label>
          <select v-model="form.genre" class="input">
            <option v-for="g in enums.genres" :key="g" :value="g">{{g}}</option>
          </select>
        </div>

        <div class="form-field">
          <label>MPAA</label>
          <select v-model="form.mpaaRating" class="input">
            <option v-for="m in enums.mpaa" :key="m" :value="m">{{m}}</option>
          </select>
        </div>

        <div class="form-field">
          <label>Length (min)</label>
          <input v-model.number="form.length" class="input" type="number" />
        </div>

        <div class="form-field">
          <label>Budget</label>
          <input v-model.number="form.budget" class="input" type="number" step="0.01" />
        </div>

        <div class="form-field">
          <label>Total Box Office</label>
          <input v-model.number="form.totalBoxOffice" class="input" type="number" />
        </div>

        <div class="form-field">
          <label>USA Box Office</label>
          <input v-model.number="form.usaBoxOffice" class="input" type="number" />
        </div>

        <div class="form-field">
          <label>Golden Palm Count</label>
          <input v-model.number="form.goldenPalmCount" class="input" type="number" />
          <div v-if="errors.goldenPalmCount" class="error">{{errors.goldenPalmCount}}</div>
        </div>

        <div class="form-field">
          <label>Oscars Count</label>
          <input v-model.number="form.oscarsCount" class="input" type="number" />
        </div>

        <div class="form-field" style="grid-column:1/3">
          <label>Coordinates</label>
          <select v-model="ids.coordinatesId" class="input">
            <option v-for="c in coordinatesOptions" :key="c.value" :value="c.value">{{ c.label }}</option>
          </select>
          <div v-if="errors.coordinatesId" class="error">{{errors.coordinatesId}}</div>
        </div>

        <div class="form-field">
          <label>Director</label>
          <select v-model="ids.directorId" class="input">
            <option v-for="p in personsOptions" :key="p.value" :value="p.value">{{ p.label }}</option>
          </select>
          <div v-if="errors.directorId" class="error">{{errors.directorId}}</div>
        </div>

        <div class="form-field">
          <label>Screenwriter</label>
          <select v-model="ids.screenwriterId" class="input">
            <option :value="null">--</option>
            <option v-for="p in personsOptions" :key="p.value" :value="p.value">{{ p.label }}</option>
          </select>
        </div>

        <div class="form-field">
          <label>Operator</label>
          <select v-model="ids.operatorId" class="input">
            <option v-for="p in personsOptions" :key="p.value" :value="p.value">{{ p.label }}</option>
          </select>
          <div v-if="errors.operatorId" class="error">{{errors.operatorId}}</div>
        </div>
      </div>

      <div class="mt-12" style="display:flex; gap:8px; justify-content:flex-end">
        <button class="btn btn-primary" @click="submit">Save</button>
        <button class="btn btn-secondary" @click="$emit('close')">Close</button>
      </div>

      <div v-if="serverError" class="error" style="margin-top:8px">{{serverError}}</div>
    </div>
  </div>
</template>

<script>
import { ref, watch } from 'vue'
import { createObject, updateObject, fetchObject } from '../api'
import { list } from '../api'

export default {
  props: { mode: { type: String, default: 'create' }, id: { type: Number, default: null } },
  emits: ['saved','close','error'],
  setup(props, { emit }){
    const form = ref({
      name: '',
      genre: '',
      mpaaRating: '',
      length: null,
      budget: null,
      totalBoxOffice: null,
      usaBoxOffice: null,
      oscarsCount: null,
      goldenPalmCount: null,
      creationDate: null,
    })
    const ids = ref({ coordinatesId:null, directorId:null, screenwriterId:null, operatorId:null })
    const errors = ref({})
    const serverError = ref(null)
    const coordinatesOptions = ref([])
    const personsOptions = ref([])
    const handleError = (error) => {
      console.error(error)
      emit('error', error)
    }

    const load = async () => {
      if (props.mode === 'edit' && props.id) {
        try {
          const data = await fetchObject(props.id)
          form.value = {
            name: data.name,
            genre: data.genre,
            mpaaRating: data.mpaaRating,
            length: data.length ?? null,
            budget: data.budget ?? null,
            totalBoxOffice: data.totalBoxOffice ?? null,
            usaBoxOffice: data.usaBoxOffice ?? null,
            oscarsCount: data.oscarsCount ?? null,
            goldenPalmCount: data.goldenPalmCount ?? null,
            creationDate: data.creationDate ?? null,
          }
          ids.value = {
            coordinatesId: data.coordinates?.id ?? null,
            directorId: data.director?.id ?? null,
            screenwriterId: data.screenwriter?.id ?? null,
            operatorId: data.operator?.id ?? null,
          }
        } catch (e) {
          handleError(e)
        }
      }
    }


    watch(()=>props.id, load, { immediate:true })

    ;(async () => {
      try {
        const coords = await list('coordinates', { page: 1, pageSize: 1000 })
        const coordsItems = Array.isArray(coords) ? coords : (coords.items || [])
        coordinatesOptions.value = coordsItems.map(c => ({ value: c.id, label: `${c.id}: (${c.x}, ${c.y})` }))
      } catch(e) { handleError(e) }
      try {
        const persons = await list('persons', { page: 1, pageSize: 1000 })
        const personItems = Array.isArray(persons) ? persons : (persons.items || [])
        personsOptions.value = personItems.map(p => ({ value: p.id, label: `${p.id}${p.name?': '+p.name:''}` }))
      } catch(e) { handleError(e) }
    })()

    const validate = () =>{
      errors.value = {}

      if (!form.value.name || form.value.name.trim().length < 2)
        errors.value.name = 'Name must be at least 2 chars'
      if (!form.value.genre) errors.value.genre = 'Select genre'
      if (!form.value.mpaaRating) errors.value.mpaaRating = 'Select MPAA'

      if (!ids.value.coordinatesId) errors.value.coordinatesId = 'Select coordinates'
      if (!ids.value.directorId) errors.value.directorId = 'Select director'
      if (!ids.value.operatorId) errors.value.operatorId = 'Select operator'

      if (form.value.goldenPalmCount == null || form.value.goldenPalmCount <= 0)
        errors.value.goldenPalmCount = 'Golden Palm Count must be > 0'

      const pos = [
        ['length', 'Length must be > 0'],
        ['budget', 'Budget must be > 0'],
        ['totalBoxOffice', 'Total Box Office must be > 0'],
        ['usaBoxOffice', 'USA Box Office must be > 0'],
      ]
      for (const [k, msg] of pos) {
        const v = form.value[k]
        if (v != null && v <= 0) errors.value[k] = msg
      }

      return Object.keys(errors.value).length === 0
    }


    const submit = async () =>{
      serverError.value = null
      if(!validate()) return
      try{
        if(props.mode === 'create'){
          await createObject({
            ...form.value,
            coordinates: { id: ids.value.coordinatesId },
            director: { id: ids.value.directorId },
            screenwriter: ids.value.screenwriterId ? { id: ids.value.screenwriterId } : null,
            operator: { id: ids.value.operatorId },
          })
        }else{
          await updateObject(props.id, {
            ...form.value,
            coordinates: { id: ids.value.coordinatesId },
            director: { id: ids.value.directorId },
            screenwriter: ids.value.screenwriterId ? { id: ids.value.screenwriterId } : null,
            operator: { id: ids.value.operatorId },
          })
        }
        emit('saved')
      }catch(e){
        serverError.value = e.response?.data?.message || e.message
        handleError(e)
      }
    }

    const enums = { genres: ['ADVENTURE','WESTERN','TRAGEDY','HORROR', 'SCIENCE_FICTION'].filter(Boolean), mpaa: ['G','PG','PG_13','R'] }
    return { form, ids, errors, serverError, submit, enums, coordinatesOptions, personsOptions }
  }
}
</script>
