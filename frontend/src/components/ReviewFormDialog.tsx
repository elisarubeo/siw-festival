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
  /** se valorizzata il dialog e' in modifica e precompila i campi, altrimenti crea */
  initial: Review | null
  onClose: () => void
  onSaved: (review: Review) => void
}

/**
 * Form di inserimento e modifica, dentro un Dialog MUI.
 *
 * Stesso schema di MovieCreateDialog visto a lezione: il componente non sa
 * nulla di chi lo usa, riceve open/onClose/onSaved come props, chiama il
 * servizio e segnala l'esito al genitore.
 *
 * Lo stesso componente serve sia a creare sia a modificare: cambia solo quale
 * funzione del servizio viene chiamata, perche' i campi modificabili sono gli
 * stessi (e' anche il motivo per cui il backend usa un unico ReviewRequest).
 */
export default function ReviewFormDialog({
  open, movieId, initial, onClose, onSaved,
}: ReviewFormDialogProps) {

  const [text, setText] = useState('')
  const [rating, setRating] = useState<number | null>(null)
  const [errore, setErrore] = useState<string | null>(null)
  const [inCorso, setInCorso] = useState(false)

  /* Il dialog resta montato anche da chiuso, quindi lo stato va risincronizzato
     ogni volta che si apre: senza, riaprendolo per modificare una recensione
     diversa si vedrebbero ancora i valori di quella precedente. */
  useEffect(() => {
    if (open) {
      setText(initial?.text ?? '')
      setRating(initial?.rating ?? null)
      setErrore(null)
    }
  }, [open, initial])

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()

    /* Validazione al momento del submit, non prima: mostrare errori mentre
       l'utente sta ancora scrivendo e' fastidioso. Il backend ricontrolla
       comunque tutto — questa e' solo cortesia, non sicurezza. */
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
      /* L'Alert compare DENTRO il dialog e non lo chiude: quello che l'utente
         ha scritto resta dov'e', pronto per essere corretto. */
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
            /* 2000 e' il limite della colonna nel database, ripetuto in
               ReviewRequest con @Size: qui serve solo a non far scrivere
               all'utente un testo che verrebbe respinto. */
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
