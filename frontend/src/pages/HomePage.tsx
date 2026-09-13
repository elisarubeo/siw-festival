import { Link as RouterLink } from 'react-router-dom'
import { Button, Container, Stack, Typography } from '@mui/material'

export default function HomePage() {
  return (
    <Container maxWidth="sm" sx={{ py: 8 }}>
      <Typography variant="h1" sx={{ mb: 2 }}>Recensioni</Typography>
      <Typography color="text.secondary" sx={{ mb: 4 }}>
        A questa parte dell'applicazione si arriva dalla pagina di un film.
        Durante lo sviluppo puoi entrare direttamente da qui.
      </Typography>
      <Stack direction="row" spacing={2}>
        <Button component={RouterLink} to="/movies/1" variant="contained">
          Recensioni del film 1
        </Button>
        <Button href="/movies" variant="outlined">Elenco film</Button>
      </Stack>
    </Container>
  )
}
