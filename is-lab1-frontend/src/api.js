import axios from 'axios'


// Use same-origin in production; dev can override via VITE_API_BASE
const BASE = import.meta.env.VITE_API_BASE || '/api'


export const api = axios.create({ baseURL: BASE, withCredentials: true })


// Movies CRUD with pagination/filter/sort
export const fetchObjects = ({page=1, pageSize=10, filter='', sortBy='id', sortDir='asc'}) =>
    api.get('/movies', { params: { page, pageSize, filter, sortBy, sortDir } }).then(r=>r.data)

export const fetchObject = (id) => api.get(`/movies/${id}`).then(r=>r.data)
export const createObject = (payload) => api.post('/movies', payload).then(r=>r.data)
export const updateObject = (id, payload) => api.put(`/movies/${id}`, payload).then(r=>r.data)
export const deleteObject = (id) => api.delete(`/movies/${id}`).then(r=>r.data)


// Auxiliary fetch (future use)
export const fetchAuxiliary = (auxName, q='') => api.get(`/aux/${auxName}`, { params: { q } }).then(r=>r.data)

// Generic CRUD for other resources (expects backend endpoints like /persons, /coordinates, /locations)
export const list = (resource, {page=1, pageSize=10, filter='', sortBy='id', sortDir='asc'} = {}) =>
    api.get(`/${resource}`, { params: { page, pageSize, filter, sortBy, sortDir } }).then(r=>r.data)
export const getOne = (resource, id) => api.get(`/${resource}/${id}`).then(r=>r.data)
export const createOne = (resource, payload) => api.post(`/${resource}`, payload).then(r=>r.data)
export const updateOne = (resource, id, payload) => api.put(`/${resource}/${id}`, payload).then(r=>r.data)
export const deleteOne = (resource, id) => api.delete(`/${resource}/${id}`).then(r=>r.data)


// Special operations
export const op_countGoldenPalm = (value) => api.get(`/ops/goldenPalmCount`, { params: { value } }).then(r=>r.data)
export const op_usaBoxOfficeGreater = (value) => api.get(`/ops/usaBoxOfficeGreater`, { params: { value } }).then(r=>r.data)
export const op_uniqueGenres = () => api.get(`/ops/uniqueGenres`).then(r=>r.data)
export const op_addOscarToR = () => api.post(`/ops/addOscarToR`).then(r=>r.data)
export const op_removeOscarsByDirectorsGenre = (genre) => api.post(`/ops/removeOscarsByDirectorsGenre`, { genre }).then(r=>r.data)
