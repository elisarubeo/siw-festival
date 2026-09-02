import { useState, type FormEvent } from 'react'
import { useNavigate, useLocation, Link as RouterLink } from 'react-router-dom'
import { Alert, Box, Button, CircularProgress, Container, Link, Paper, TextField, Typography } from '@mui/material'
import { useAuth } from '../context/AuthContext'

/**
 * Login della parte React.
 *
 * I due campi sono "controllati": il valore vive nello stato React, non nel
 * DOM, e ogni carattere digitato passa da onChange (slide 14, lezione 2).
 */
export default function LoginPage() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [errore, setErrore] = useState<string | null>(null)
  const [inCorso, setInCorso] = useState(false)

  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  /* Se PrivateRoute ha dirottato l'utente qui, si torna da dove veniva. */
  const destinazione = (location.state as { from?: string } | null)?.from ?? '/'

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setErrore(null)
    setInCorso(true)
    try {
      await login(username, password)
      navigate(destinazione, { replace: true })
    } catch (e) {
      setErrore(e instanceof Error ? e.message : 'Login non riuscito.')
    } finally {
      /* nel finally: deve tornare cliccabile sia dopo un successo sia dopo un errore */
      setInCorso(false)
    }
  }

  return (
    <Container maxWidth="xs" sx={{ py: 8 }}>
      <Paper sx={{ p: 4 }}>
        <Typography variant="h1" sx={{ mb: 1 }}>Accedi</Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Serve per scrivere una recensione. Per leggerle non è necessario.
        </Typography>

        {errore && <Alert severity="error" sx={{ mb: 2 }}>{errore}</Alert>}

        <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <TextField
            label="Username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            disabled={inCorso}
            autoFocus
            required
          />
          <TextField
            label="Password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            disabled={inCorso}
            required
          />
          <Button type="submit" variant="contained" disabled={inCorso}>
            {inCorso ? <CircularProgress size={22} color="inherit" /> : 'Accedi'}
          </Button>
        </Box>

        <Typography variant="body2" sx={{ mt: 3 }}>
          <Link component={RouterLink} to="/">Torna alle recensioni</Link>
        </Typography>
      </Paper>
    </Container>
  )
}
