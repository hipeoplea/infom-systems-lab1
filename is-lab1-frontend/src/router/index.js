import { createRouter, createWebHistory } from 'vue-router'
import MoviesView from '../views/MoviesView.vue'
import PersonsView from '../views/PersonsView.vue'
import CoordinatesView from '../views/CoordinatesView.vue'
import LocationsView from '../views/LocationsView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/movies' },
    { path: '/movies', component: MoviesView },
    { path: '/persons', component: PersonsView },
    { path: '/coordinates', component: CoordinatesView },
    { path: '/locations', component: LocationsView },
  ],
})

export default router
