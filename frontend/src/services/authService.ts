import api, { messaggioErrore } from './api'
import type { LoginResponse } from '../types'

/**
 * Chiama POST /api/auth/login e restituisce token, username e ruolo.
 * Lancia un Error con un messaggio gia' leggibile se le credenziali non vanno.
 */
export async function login(username: string, password: string): Promise<LoginResponse> {
  try {
    const { data } = await api.post<LoginResponse>('/auth/login', { username, password })
    return data
  } catch (error) {
    throw new Error(messaggioErrore(error, 'Login non riuscito.'))
  }
}
