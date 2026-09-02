import { Link as RouterLink } from 'react-router-dom'
import { Button, Container, Typography } from '@mui/material'

export default function NotFoundPage() {
  return (
    <Container maxWidth="sm" sx={{ py: 10, textAlign: 'center' }}>
      <Typography variant="h1" sx={{ mb: 1 }}>Pagina non trovata</Typography>
      <Typography color="text.secondary" sx={{ mb: 3 }}>
        L'indirizzo richiesto non corrisponde a nessuna pagina.
      </Typography>
      <Button component={RouterLink} to="/" variant="contained">Torna all'inizio</Button>
    </Container>
  )
}
