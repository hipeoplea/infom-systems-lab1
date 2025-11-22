<template>
  <div>
    <div class="container" style="margin-bottom: 16px">
      <div class="card toolbar" style="align-items: center">
        <div style="font-weight: 600; letter-spacing: 0.2px; color: #cbd5e1">
          IS Lab 1
        </div>
        <div class="ml-auto" style="display: flex; gap: 8px; flex-wrap: wrap">
          <router-link class="btn btn-secondary" to="/movies"
            >Фильмы</router-link
          >
          <router-link class="btn btn-secondary" to="/persons"
            >Персоны</router-link
          >
          <router-link class="btn btn-secondary" to="/coordinates"
            >Координаты</router-link
          >
          <router-link class="btn btn-secondary" to="/locations"
            >Локации</router-link
          >
        </div>
      </div>
    </div>

    <router-view />
  </div>
</template>

<script>
export default {};
</script>
