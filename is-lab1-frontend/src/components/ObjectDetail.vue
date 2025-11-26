<template>
  <div class="modal">
    <div class="card">
      <h3>Детали фильма</h3>
      <div v-if="errorMessage" class="error" style="margin-bottom: 8px">
        {{ errorMessage }}
      </div>
      <div v-if="item">
        <div><strong>ID</strong> {{ item.id }}</div>
        <div><strong>Название</strong> {{ item.name }}</div>
        <div><strong>Жанр</strong> {{ item.genre }}</div>
        <div><strong>MPAA</strong> {{ item.mpaaRating }}</div>
        <div><strong>Длина</strong> {{ item.length }}</div>
        <div><strong>Бюджет</strong> {{ item.budget }}</div>
        <div><strong>Сборы (всего)</strong> {{ item.totalBoxOffice }}</div>
        <div><strong>Сборы в США</strong> {{ item.usaBoxOffice }}</div>
        <div><strong>Оскары</strong> {{ item.oscarsCount }}</div>
        <div>
          <strong>Создан</strong>
          {{ new Date(item.creationDate).toLocaleString() }}
        </div>

        <div style="margin-top: 12px">
          <h4>Связанные объекты</h4>
          <div class="small-muted">
            Координаты: {{ item.coordinates?.id }} (x={{ item.coordinates?.x }},
            y={{ item.coordinates?.y }})
          </div>
          <div class="small-muted">
            Режиссёр: {{ item.director?.id }} — {{ item.director?.name }}
          </div>
          <div class="small-muted">
            Сценарист: {{ item.screenwriter?.id }} —
            {{ item.screenwriter?.name }}
          </div>
          <div class="small-muted">
            Оператор: {{ item.operator?.id }} — {{ item.operator?.name }}
          </div>
        </div>
      </div>

      <div
        style="
          margin-top: 12px;
          display: flex;
          gap: 8px;
          justify-content: flex-end;
        "
      >
        <button class="btn btn-secondary" @click="$emit('close')">
          Закрыть
        </button>
      </div>
    </div>
  </div>
</template>
<script>
import { ref, watch } from "vue";
import { fetchObject } from "../api";

export default {
  props: { id: { type: Number, default: null } },
  emits: ["close", "error"],
  setup(props, { emit }) {
    const item = ref(null);
    const errorMessage = ref(null);
    const load = async () => {
      if (!props.id) return;
      errorMessage.value = null;
      item.value = null;
      try {
        item.value = await fetchObject(props.id);
      } catch (e) {
        console.error(e);
        errorMessage.value = e.response?.data?.message || e.message;
        emit("error", e);
      }
    };
    watch(() => props.id, load, { immediate: true });
    return { item, errorMessage };
  },
};
</script>
