import axios from 'axios'

const BASE = 'http://localhost:7000/api'

export const api = axios.create({
    baseURL: BASE,
    withCredentials: true,
})

export const fetchObjects = ({page=1, pageSize=5, filter='', sortBy='id', sortDir='asc'}) =>
    api.get('/movies', { params: { page, pageSize, filter, sortBy, sortDir } }).then(r=>r.data)

export const fetchObject = (id) => api.get(`/movies/${id}`).then(r=>r.data)
export const createObject = (payload) => api.post('/movies', payload).then(r=>r.data)
export const updateObject = (id, payload) => api.put(`/movies/${id}`, payload).then(r=>r.data)
export const deleteObject = (id) => api.delete(`/movies/${id}`).then(r=>r.data)

export const fetchAuxiliary = (auxName, q='') => api.get(`/aux/${auxName}`, { params: { q } }).then(r=>r.data)

export const list = (resource, {page=1, pageSize=5, filter='', sortBy='id', sortDir='asc'} = {}) =>
    api.get(`/${resource}`, { params: { page, pageSize, filter, sortBy, sortDir } }).then(r=>r.data)
export const getOne = (resource, id) => api.get(`/${resource}/${id}`).then(r=>r.data)
export const createOne = (resource, payload) => api.post(`/${resource}`, payload).then(r=>r.data)
export const updateOne = (resource, id, payload) => api.put(`/${resource}/${id}`, payload).then(r=>r.data)
export const deleteOne = (resource, id) => api.delete(`/${resource}/${id}`).then(r=>r.data)

export const op_countGoldenPalm = (value) => api.get(`/ops/goldenPalmCount`, { params: { value } }).then(r=>r.data)
export const op_usaBoxOfficeGreater = (value) => api.get(`/ops/usaBoxOfficeGreater`, { params: { value } }).then(r=>r.data)
export const op_uniqueGenres = () => api.get(`/ops/uniqueGenres`).then(r=>r.data)
export const op_addOscarToR = () => api.post(`/ops/addOscarToR`).then(r=>r.data)
export const op_removeOscarsByDirectorsGenre = (genre) => api.post(`/ops/removeOscarsByDirectorsGenre`, { genre }).then(r=>r.data)
