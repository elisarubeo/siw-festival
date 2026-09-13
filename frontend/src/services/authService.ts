import api, { messaggioErrore } from './api'
import type { LoginResponse } from '../types'

export async function login(username: string, password: string): Promise<LoginResponse> {
  try {
    const { data } = await api.post<LoginResponse>('/auth/login', { username, password })
    return data
  } catch (error) {
    throw new Error(messaggioErrore(error, 'Login non riuscito.'))
  }
}
