import api, { messaggioErrore } from './api'
import type { Review, ReviewRequest, ReviewStats } from '../types'

export async function getReviews(movieId: number): Promise<Review[]> {
  try {
    const { data } = await api.get<Review[]>(`/movies/${movieId}/reviews`)
    return data
  } catch (error) {
    throw new Error(messaggioErrore(error, 'Impossibile caricare le recensioni.'))
  }
}

export async function getStats(movieId: number): Promise<ReviewStats> {
  try {
    const { data } = await api.get<ReviewStats>(`/movies/${movieId}/reviews/stats`)
    return data
  } catch (error) {
    throw new Error(messaggioErrore(error, 'Impossibile caricare le statistiche.'))
  }
}

export async function createReview(movieId: number, request: ReviewRequest): Promise<Review> {
  try {
    const { data } = await api.post<Review>(`/movies/${movieId}/reviews`, request)
    return data
  } catch (error) {
    throw new Error(messaggioErrore(error, 'Impossibile salvare la recensione.'))
  }
}

export async function updateReview(reviewId: number, request: ReviewRequest): Promise<Review> {
  try {
    const { data } = await api.put<Review>(`/reviews/${reviewId}`, request)
    return data
  } catch (error) {
    throw new Error(messaggioErrore(error, 'Impossibile aggiornare la recensione.'))
  }
}

export async function deleteReview(reviewId: number): Promise<void> {
  try {
    await api.delete(`/reviews/${reviewId}`)
  } catch (error) {
    throw new Error(messaggioErrore(error, 'Impossibile eliminare la recensione.'))
  }
}
