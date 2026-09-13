import { useCallback, useEffect, useState } from 'react'
import { Link as RouterLink, useParams } from 'react-router-dom'
import {
  Alert, Box, Button, CircularProgress, Container, Link, Stack, Typography,
} from '@mui/material'

import { useAuth } from '../context/AuthContext'
import { deleteReview, getReviews, getStats } from '../services/reviewService'
import ReviewCard from '../components/ReviewCard'
import ReviewFormDialog from '../components/ReviewFormDialog'
import ReviewStats from '../components/ReviewStats'
import type { Review, ReviewStats as Stats } from '../types'

export default function MovieReviewsPage() {
  const { movieId } = useParams<{ movieId: string }>()
  const { isAuthenticated, userId } = useAuth()

  const [reviews, setReviews] = useState<Review[]>([])
  const [stats, setStats] = useState<Stats | null>(null)
  const [caricamento, setCaricamento] = useState(true)
  const [errore, setErrore] = useState<string | null>(null)

  const [dialogAperto, setDialogAperto] = useState(false)
  const [inModifica, setInModifica] = useState<Review | null>(null)

  const id = Number(movieId)

  const ricaricaStats = useCallback(async () => {
    try {
      setStats(await getStats(id))
    } catch {
      setStats(null)
    }
  }, [id])

  useEffect(() => {
    let annullato = false

    async function carica() {
      setCaricamento(true)
      setErrore(null)
      try {
        const [lista, statistiche] = await Promise.all([getReviews(id), getStats(id)])
        if (annullato) return
        setReviews(lista)
        setStats(statistiche)
      } catch (e) {
        if (!annullato) setErrore(e instanceof Error ? e.message : 'Errore di caricamento.')
      } finally {
        if (!annullato) setCaricamento(false)
      }
    }

    if (Number.isNaN(id)) {
      setErrore('Indirizzo non valido.')
      setCaricamento(false)
    } else {
      carica()
    }

    return () => { annullato = true }
  }, [id])

  const miaRecensione = reviews.find((r) => r.authorId === userId) ?? null

  function handleSaved(salvata: Review) {
    setReviews((prev) => {
      const esisteGia = prev.some((r) => r.id === salvata.id)
      return esisteGia
        ? prev.map((r) => (r.id === salvata.id ? salvata : r))
        : [salvata, ...prev]
    })
    ricaricaStats()
  }

  async function handleDelete(review: Review) {
    if (!window.confirm('Vuoi eliminare la tua recensione?')) return
    try {
      await deleteReview(review.id)
      setReviews((prev) => prev.filter((r) => r.id !== review.id))
      ricaricaStats()
    } catch (e) {
      setErrore(e instanceof Error ? e.message : 'Eliminazione non riuscita.')
    }
  }

  function apriPerNuova() {
    setInModifica(null)
    setDialogAperto(true)
  }

  function apriPerModifica(review: Review) {
    setInModifica(review)
    setDialogAperto(true)
  }

  if (caricamento) {
    return (
      <Container sx={{ py: 8, textAlign: 'center' }}>
        <CircularProgress />
      </Container>
    )
  }

  return (
    <Container maxWidth="md" sx={{ py: 5 }}>

      <Typography variant="h1" sx={{ mb: 1 }}>Recensioni</Typography>
      <Typography variant="body2" sx={{ mb: 4 }}>
        <Link href={`/movies/${id}`}>Torna alla scheda del film</Link>
      </Typography>

      {errore && <Alert severity="error" sx={{ mb: 3 }} onClose={() => setErrore(null)}>{errore}</Alert>}

      {stats && <Box sx={{ mb: 4 }}><ReviewStats stats={stats} /></Box>}

      {!isAuthenticated && (
        <Alert severity="info" sx={{ mb: 4 }}
               action={<Button component={RouterLink} to="/login" size="small">Accedi</Button>}>
          Accedi per scrivere una recensione.
        </Alert>
      )}

      {isAuthenticated && !miaRecensione && (
        <Button variant="contained" onClick={apriPerNuova} sx={{ mb: 4 }}>
          Scrivi una recensione
        </Button>
      )}

      {isAuthenticated && miaRecensione && (
        <Alert severity="success" sx={{ mb: 4 }}>
          Hai già recensito questo film. Puoi modificarla o eliminarla dalla tua recensione qui sotto.
        </Alert>
      )}

      {reviews.length === 0 ? (
        <Typography color="text.secondary">
          Nessuno ha ancora recensito questo film.
        </Typography>
      ) : (
        <Stack spacing={2}>
          {reviews.map((review) => (
            <ReviewCard
              key={review.id}
              review={review}
              isMine={review.authorId === userId}
              onEdit={apriPerModifica}
              onDelete={handleDelete}
            />
          ))}
        </Stack>
      )}

      <ReviewFormDialog
        open={dialogAperto}
        movieId={id}
        initial={inModifica}
        onClose={() => setDialogAperto(false)}
        onSaved={handleSaved}
      />

    </Container>
  )
}
