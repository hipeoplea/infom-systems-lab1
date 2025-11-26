<template>
  <div>
    <div class="container" style="margin-bottom: 16px">
      <div class="card toolbar" style="align-items: center">
        <div style="font-weight: 600; letter-spacing: 0.2px; color: #cbd5e1">
          Работа с фильмами
        </div>
        <div class="ml-auto" style="display: flex; gap: 8px; align-items: center; flex-wrap: wrap">
          <input
            v-model="currentUser"
            class="input"
            placeholder="Имя пользователя"
            style="max-width: 180px"
          />
          <label style="display: flex; gap: 6px; align-items: center; color: #cbd5e1">
            <input type="checkbox" v-model="adminMode" />
            Админ
          </label>
          <button class="btn" @click="openHistory">
            История импорта
          </button>
          <button class="btn btn-secondary" @click="triggerImport">
            Импорт JSON
          </button>
        </div>
        <div class="ml-auto" style="display: flex; gap: 8px; flex-wrap: wrap">
          <button class="btn btn-secondary" @click="runOps.uniqueGenres">
            Уникальные жанры
          </button>
          <button class="btn btn-secondary" @click="runOps.countGolden">
            Count goldenPalm
          </button>
          <button class="btn btn-secondary" @click="runOps.countUsaGreater">
            Count USA boxOffice >
          </button>
          <button class="btn btn-primary" @click="runOps.addOscarR">
            +1 Оскар для R
          </button>
          <button class="btn btn-danger" @click="openRemoveOscarsModal">
            Снять Оскары по жанру
          </button>
        </div>
      </div>
    </div>

    <ObjectTable
      ref="tableRef"
      :columns="columns"
      :formatters="formatters"
      @view="onView"
      @edit="onEdit"
      @create="onCreate"
      @error="onError"
    />
    <input
      ref="fileInput"
      type="file"
      accept="application/json"
      style="display: none"
      @change="onFileSelected"
    />

    <ObjectForm
      v-if="formVisible"
      :id="editingId"
      :mode="formMode"
      @saved="onSaved"
      @close="closeForm"
      @error="onError"
    />
    <ObjectDetail v-if="detailVisible" :id="detailId" @close="closeDetail" />

    <div v-if="toast" class="toast">{{ toast }}</div>

    <div v-if="removeOscarsOpen" class="modal">
      <div class="card">
        <h3>Снять Оскары у фильмов режиссёров жанра</h3>
        <div class="form-field" style="margin-top: 10px">
          <label>Жанр</label>
          <select v-model="selectedGenre" class="input">
            <option v-for="g in genres" :key="g" :value="g">{{ g }}</option>
          </select>
        </div>
        <div
          class="mt-12"
          style="display: flex; gap: 8px; justify-content: flex-end"
        >
          <button
            class="btn btn-primary"
            :disabled="!selectedGenre"
            @click="confirmRemoveOscars"
          >
            Выполнить
          </button>
          <button class="btn btn-secondary" @click="removeOscarsOpen = false">
            Закрыть
          </button>
        </div>
      </div>
    </div>

    <div v-if="historyOpen" class="modal">
      <div class="card history-card">
        <div class="history-header">
          <div>
            <div class="history-title">История импорта</div>
            <div class="history-subtitle">
              {{ adminMode ? "Администратор: все операции" : `Пользователь: ${currentUser || "—"}` }}
            </div>
          </div>
          <button class="btn btn-secondary" @click="historyOpen = false">Закрыть</button>
        </div>

        <div class="history-grid head">
          <div>ID</div>
          <div>Статус</div>
          <div>Пользователь</div>
          <div>Импортировано</div>
          <div>Время</div>
        </div>
        <div
          v-for="op in history"
          :key="op.id"
          class="history-grid row"
          :class="{
            success: op.status === 'SUCCESS',
            failed: op.status === 'FAILED',
            pending: op.status === 'IN_PROGRESS',
          }"
        >
          <div class="mono">#{{ op.id }}</div>
          <div class="badge" :class="op.status.toLowerCase()">{{ op.status }}</div>
          <div>{{ op.user }}</div>
          <div>{{ op.importedCount ?? "—" }}</div>
          <div class="mono">{{ formatDate(op.createdAt) }}</div>
        </div>
        <div v-if="!history.length" class="history-empty">
          Нет записей
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onBeforeUnmount } from "vue";
import ObjectTable from "../components/ObjectTable.vue";
import ObjectForm from "../components/ObjectForm.vue";
import ObjectDetail from "../components/ObjectDetail.vue";
import { createWS } from "../websocket";
import * as api from "../api";

export default {
  components: { ObjectTable, ObjectForm, ObjectDetail },
  setup() {
    const columns = [
      "name",
      "genre",
      "mpaaRating",
      "oscarsCount",
      "goldenPalmCount",
      "usaBoxOffice",
      "length",
      "budget",
    ];
    const formVisible = ref(false);
    const formMode = ref("create");
    const editingId = ref(null);
    const detailVisible = ref(false);
    const detailId = ref(null);
    const toast = ref("");
    const tableRef = ref(null);
    const currentUser = ref("");
    const adminMode = ref(false);
    const fileInput = ref(null);
    const removeOscarsOpen = ref(false);
    const genres = ref([]);
    const selectedGenre = ref("");
    const historyOpen = ref(false);
    const history = ref([]);

    const ws = createWS((msg) => {
      if (tableRef.value && tableRef.value.load) tableRef.value.load();
      toast.value = `Событие: ${msg.type} id=${msg.id}`;
      setTimeout(() => (toast.value = ""), 2500);
    });
    onBeforeUnmount(() => ws?.close?.());

    const onCreate = () => {
      formMode.value = "create";
      editingId.value = null;
      formVisible.value = true;
    };
    const onEdit = (id) => {
      formMode.value = "edit";
      editingId.value = id;
      formVisible.value = true;
    };
    const onView = (id) => {
      detailId.value = id;
      detailVisible.value = true;
    };
    const closeForm = () => (formVisible.value = false);
    const closeDetail = () => (detailVisible.value = false);
    const onSaved = async () => {
      formVisible.value = false;
      await tableRef.value.load();
      toast.value = "Сохранено";
      setTimeout(() => (toast.value = ""), 2000);
    };
    const onError = (err) => {
      toast.value = typeof err === "string" ? err : err?.message || String(err);
      setTimeout(() => (toast.value = ""), 4000);
    };

    const triggerImport = () => {
      if (!currentUser.value.trim()) {
        toast.value = "Укажите имя пользователя для импорта";
        setTimeout(() => (toast.value = ""), 2500);
        return;
      }
      fileInput.value?.click?.();
    };
    const onFileSelected = async (e) => {
      const file = e.target.files?.[0];
      e.target.value = "";
      if (!file) return;
      try {
        const res = await api.importMovies(file, currentUser.value.trim());
        toast.value = `Импортировано: ${res.imported}`;
        await tableRef.value?.load?.();
      } catch (err) {
        onError(err.response?.data?.message || err.message);
      }
    };

    const openHistory = async () => {
      try {
        if (adminMode.value) {
          history.value = await api.importHistoryAdmin();
        } else {
          if (!currentUser.value.trim()) {
            toast.value = "Укажите имя пользователя для истории";
            setTimeout(() => (toast.value = ""), 2500);
            return;
          }
          history.value = await api.importHistory(currentUser.value.trim());
        }
        historyOpen.value = true;
      } catch (err) {
        onError(err.response?.data?.message || err.message);
      }
    };

    const formatDate = (val) => {
      if (!val) return "";
      const d = new Date(val);
      if (Number.isNaN(d.getTime())) return val;
      return d.toLocaleString();
    };

    const runOps = {
      uniqueGenres: async () => {
        try {
          const res = await api.op_uniqueGenres();
          toast.value = `Genres: ${res.join(", ")}`;
        } catch (e) {
          onError(e);
        }
      },
      countGolden: async () => {
        const v = prompt("Введите значение goldenPalmCount");
        if (v == null) return;
        try {
          const res = await api.op_countGoldenPalm(parseInt(v));
          toast.value = `Count = ${res}`;
        } catch (e) {
          onError(e);
        }
      },
      countUsaGreater: async () => {
        const v = prompt("Введите порог USA boxOffice");
        if (v == null) return;
        try {
          const res = await api.op_usaBoxOfficeGreater(parseInt(v));
          toast.value = `Count = ${res}`;
        } catch (e) {
          onError(e);
        }
      },
      addOscarR: async () => {
        if (!confirm("Добавить 1 оскар всем фильмам с рейтингом R?")) return;
        try {
          await api.op_addOscarToR();
          toast.value = "Операция выполнена";
          await tableRef.value?.load?.();
        } catch (e) {
          onError(e);
        }
      },
    };

    const openRemoveOscarsModal = async () => {
      try {
        genres.value = await api.op_uniqueGenres();
        selectedGenre.value = genres.value[0] || "";
        removeOscarsOpen.value = true;
      } catch (e) {
        toast.value = e.response?.data?.message || e.message;
        setTimeout(() => (toast.value = ""), 3000);
      }
    };

    const confirmRemoveOscars = async () => {
      if (!selectedGenre.value) return;
      try {
        await api.op_removeOscarsByDirectorsGenre(selectedGenre.value);
        toast.value = "Операция выполнена";
        removeOscarsOpen.value = false;
        tableRef.value?.load?.();
      } catch (e) {
        onError(e);
      }
    };

    const formatters = {};

    return {
      columns,
      formatters,
      formVisible,
      formMode,
      editingId,
      detailVisible,
      detailId,
      onCreate,
      onEdit,
      onView,
      closeForm,
      closeDetail,
      onSaved,
      onError,
      toast,
      tableRef,
      runOps,
      removeOscarsOpen,
      openRemoveOscarsModal,
      confirmRemoveOscars,
      genres,
      selectedGenre,
      fileInput,
      triggerImport,
      onFileSelected,
      currentUser,
      adminMode,
      historyOpen,
      history,
      openHistory,
      formatDate,
    };
  },
};
</script>
