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

/**
 * Le recensioni di un film: elenco, statistiche, e inserimento/modifica/
 * eliminazione della propria.
 *
 * La pagina e' PUBBLICA in lettura, come chiede il §4.1 della traccia: non sta
 * dentro PrivateRoute. E' il form a comparire o meno a seconda che ci sia un
 * utente collegato.
 */
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

  /* Le statistiche si ricaricano dopo ogni scrittura, perche' media e
     distribuzione cambiano. La lista invece no: si aggiorna in memoria con
     l'oggetto che il server ha appena restituito. */
  const ricaricaStats = useCallback(async () => {
    try {
      setStats(await getStats(id))
    } catch {
      /* Le statistiche sono un di piu': se falliscono si nascondono e basta,
         senza far sparire le recensioni che l'utente sta leggendo. */
      setStats(null)
    }
  }, [id])

  /* Una sola chiamata al mount, poi tutto vive nello stato. Le dipendenze
     sono [id]: cambiando film si ricarica, altrimenti no. */
  useEffect(() => {
    let annullato = false

    async function carica() {
      setCaricamento(true)
      setErrore(null)
      try {
        const [lista, statistiche] = await Promise.all([getReviews(id), getStats(id)])
        /* Se nel frattempo il componente e' stato smontato (o l'id e'
           cambiato), non si tocca piu' lo stato: scriverlo darebbe un
           aggiornamento su un componente che non esiste piu'. */
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

  /* Confronto fra id, non fra username: ReviewDto espone authorId e il login
     restituisce userId. Il backend non puo' esporre lo username dell'autore
     perche' User non ha un riferimento verso Credentials. */
  const miaRecensione = reviews.find((r) => r.authorId === userId) ?? null

  function handleSaved(salvata: Review) {
    setReviews((prev) => {
      const esisteGia = prev.some((r) => r.id === salvata.id)
      /* Mai mutare l'array: si costruisce sempre una nuova referenza,
         altrimenti React non si accorge del cambiamento. */
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
        {/* Link normale e non di React Router: esce dall'app verso Thymeleaf */}
        <Link href={`/movies/${id}`}>Torna alla scheda del film</Link>
      </Typography>

      {errore && <Alert severity="error" sx={{ mb: 3 }} onClose={() => setErrore(null)}>{errore}</Alert>}

      {stats && <Box sx={{ mb: 4 }}><ReviewStats stats={stats} /></Box>}

      {/* Tre casi: non autenticato, autenticato senza recensione, ha gia' recensito */}
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
            /* key obbligatoria: senza, React non sa quali elementi sono
               cambiati e ridisegna tutto */
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
