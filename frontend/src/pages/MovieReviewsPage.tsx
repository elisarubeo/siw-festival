import { useParams } from 'react-router-dom'
import { Alert, AlertTitle, Container, Typography } from '@mui/material'
import { useAuth } from '../context/AuthContext'

/**
 * ========================================================================
 *  QUESTA PAGINA E' DA SCRIVERE — e' il cuore del caso d'uso.
 * ========================================================================
 *
 * Tutto cio' che sta intorno funziona gia': rotte, tema, autenticazione,
 * istanza axios con il token allegato in automatico. Qui manca la logica.
 *
 * Cosa deve fare, nell'ordine:
 *
 *  1. leggere movieId da useParams (c'e' gia' qui sotto)
 *  2. useState per: recensioni (Review[]), statistiche (ReviewStats | null),
 *     caricamento (boolean), errore (string | null)
 *  3. useEffect con dipendenze [movieId] che chiama getReviews e getStats
 *     dal servizio che scriverai in src/services/reviewService.ts
 *  4. disegnare <ReviewStats /> e la lista di <ReviewCard />, con la prop key
 *  5. decidere cosa mostrare in fondo:
 *       - non autenticato          -> invito ad accedere, niente form
 *       - autenticato, nessuna sua recensione -> bottone che apre il dialog
 *       - ha gia' recensito        -> niente form; sulla SUA card compaiono
 *                                     Modifica ed Elimina
 *
 * Per sapere quale recensione e' dell'utente collegato basta confrontare
 * review.authorUsername con lo username che arriva da useAuth(): nessuna
 * chiamata in piu' al server.
 *
 * Dopo una POST o una PUT andata a buon fine NON rifare la GET della lista:
 * aggiorna lo stato con l'oggetto restituito dal server, come fa
 * MovieFilterGrid nelle slide. Le statistiche invece vanno ricaricate,
 * perche' la media cambia.
 */
export default function MovieReviewsPage() {
  const { movieId } = useParams<{ movieId: string }>()
  const { isAuthenticated, username } = useAuth()

  return (
    <Container maxWidth="md" sx={{ py: 5 }}>
      <Typography variant="h1" sx={{ mb: 3 }}>Recensioni</Typography>

      <Alert severity="info">
        <AlertTitle>Da implementare</AlertTitle>
        Pagina delle recensioni del film <strong>{movieId}</strong>.<br />
        Utente: {isAuthenticated ? <strong>{username}</strong> : 'non autenticato'}.
        <br /><br />
        L'impalcatura è pronta: le istruzioni sono nel commento in cima a
        <code> src/pages/MovieReviewsPage.tsx</code>.
      </Alert>
    </Container>
  )
}
