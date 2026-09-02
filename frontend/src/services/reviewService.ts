import api, { messaggioErrore } from './api'
import type { Review, ReviewRequest, ReviewStats } from '../types'

/**
 * Tutte le chiamate HTTP sulle recensioni stanno qui.
 *
 * I componenti importano queste funzioni e non usano mai api.get(...)
 * direttamente: e' la stessa separazione che nel backend c'e' fra controller
 * e service. Ogni funzione ha il suo try/catch e trasforma l'errore HTTP nel
 * messaggio leggibile preparato dal backend, cosi' i componenti ricevono una
 * stringa gia' pronta invece di un oggetto errore da interpretare.
 *
 * I percorsi sono relativi: la baseURL '/api' e' scritta una volta sola
 * nell'istanza axios, e il token lo allega l'interceptor.
 */

/** GET /api/movies/{movieId}/reviews — pubblica, non serve il token. */
export async function getReviews(movieId: number): Promise<Review[]> {
  try {
    const { data } = await api.get<Review[]>(`/movies/${movieId}/reviews`)
    return data
  } catch (error) {
    throw new Error(messaggioErrore(error, 'Impossibile caricare le recensioni.'))
  }
}

/** GET /api/movies/{movieId}/reviews/stats — pubblica. */
export async function getStats(movieId: number): Promise<ReviewStats> {
  try {
    const { data } = await api.get<ReviewStats>(`/movies/${movieId}/reviews/stats`)
    return data
  } catch (error) {
    throw new Error(messaggioErrore(error, 'Impossibile caricare le statistiche.'))
  }
}

/**
 * POST /api/movies/{movieId}/reviews — richiede il token.
 * Il backend risponde 201 con la recensione creata: la si restituisce al
 * chiamante, che la inserisce nello stato senza ricaricare l'intera lista.
 */
export async function createReview(movieId: number, request: ReviewRequest): Promise<Review> {
  try {
    const { data } = await api.post<Review>(`/movies/${movieId}/reviews`, request)
    return data
  } catch (error) {
    throw new Error(messaggioErrore(error, 'Impossibile salvare la recensione.'))
  }
}

/** PUT /api/reviews/{reviewId} — solo l'autore, altrimenti il backend risponde 403. */
export async function updateReview(reviewId: number, request: ReviewRequest): Promise<Review> {
  try {
    const { data } = await api.put<Review>(`/reviews/${reviewId}`, request)
    return data
  } catch (error) {
    throw new Error(messaggioErrore(error, 'Impossibile aggiornare la recensione.'))
  }
}

/** DELETE /api/reviews/{reviewId} — risponde 204, nessun corpo da leggere. */
export async function deleteReview(reviewId: number): Promise<void> {
  try {
    await api.delete(`/reviews/${reviewId}`)
  } catch (error) {
    throw new Error(messaggioErrore(error, 'Impossibile eliminare la recensione.'))
  }
}
