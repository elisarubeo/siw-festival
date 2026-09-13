import { useEffect, useState, type FormEvent } from 'react'
import {
  Alert, Box, Button, CircularProgress, Dialog, DialogActions,
  DialogContent, DialogTitle, Rating, TextField, Typography,
} from '@mui/material'

import { createReview, updateReview } from '../services/reviewService'
import type { Review } from '../types'

interface ReviewFormDialogProps {
  open: boolean
  movieId: number
  initial: Review | null
  onClose: () => void
  onSaved: (review: Review) => void
}

export default function ReviewFormDialog({
  open, movieId, initial, onClose, onSaved,
}: ReviewFormDialogProps) {

  const [text, setText] = useState('')
  const [rating, setRating] = useState<number | null>(null)
  const [errore, setErrore] = useState<string | null>(null)
  const [inCorso, setInCorso] = useState(false)

  useEffect(() => {
    if (open) {
      setText(initial?.text ?? '')
      setRating(initial?.rating ?? null)
      setErrore(null)
    }
  }, [open, initial])

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()

    if (text.trim() === '') {
      setErrore('Scrivi il testo della recensione.')
      return
    }
    if (rating === null) {
      setErrore('Assegna un voto da 1 a 5.')
      return
    }

    setErrore(null)
    setInCorso(true)
    try {
      const richiesta = { text: text.trim(), rating }
      const salvata = initial
        ? await updateReview(initial.id, richiesta)
        : await createReview(movieId, richiesta)

      onSaved(salvata)
      onClose()
    } catch (e) {
      setErrore(e instanceof Error ? e.message : 'Operazione non riuscita.')
    } finally {
      setInCorso(false)
    }
  }

  return (
    <Dialog open={open} onClose={inCorso ? undefined : onClose} fullWidth maxWidth="sm">
      <Box component="form" onSubmit={handleSubmit}>

        <DialogTitle>
          {initial ? 'Modifica la tua recensione' : 'Scrivi una recensione'}
        </DialogTitle>

        <DialogContent>
          {errore && <Alert severity="error" sx={{ mb: 2 }}>{errore}</Alert>}

          <Typography component="legend" variant="body2" color="text.secondary">
            Voto
          </Typography>
          <Rating
            value={rating}
            onChange={(_, nuovo) => setRating(nuovo)}
            disabled={inCorso}
            sx={{ mb: 2 }}
          />

          <TextField
            label="La tua recensione"
            value={text}
            onChange={(e) => setText(e.target.value)}
            disabled={inCorso}
            multiline
            minRows={4}
            fullWidth
            slotProps={{ htmlInput: { maxLength: 2000 } }}
            helperText={`${text.length}/2000`}
          />
        </DialogContent>

        <DialogActions>
          <Button onClick={onClose} disabled={inCorso}>Annulla</Button>
          <Button type="submit" variant="contained" disabled={inCorso}>
            {inCorso ? <CircularProgress size={22} color="inherit" /> : 'Salva'}
          </Button>
        </DialogActions>

      </Box>
    </Dialog>
  )
}
