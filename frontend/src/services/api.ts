import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
})

export const TOKEN_KEY = 'siw.token'

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem('siw.username')
      localStorage.removeItem('siw.userId')
      localStorage.removeItem('siw.role')
      if (!window.location.pathname.endsWith('/login')) {
        window.location.href = '/reviews/login'
      }
    }
    return Promise.reject(error)
  },
)

export function messaggioErrore(error: unknown, fallback: string): string {
  if (axios.isAxiosError(error)) {
    return error.response?.data?.message ?? error.message ?? fallback
  }
  return fallback
}

export default api
