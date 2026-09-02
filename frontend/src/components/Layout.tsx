import { Link as RouterLink, Outlet } from 'react-router-dom'
import { AppBar, Box, Button, Container, Toolbar, Typography } from '@mui/material'
import { useAuth } from '../context/AuthContext'

/**
 * Cornice comune a tutte le pagine: barra in alto piu' <Outlet />, che e' il
 * punto in cui React Router inserisce la rotta figlia attiva (slide 15, lez. 3).
 */
export default function Layout() {
  const { isAuthenticated, username, logout } = useAuth()

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <AppBar position="static" color="default" elevation={1}>
        <Container>
          <Toolbar disableGutters sx={{ gap: 2 }}>
            <Typography variant="h6" component={RouterLink} to="/"
                        sx={{ flexGrow: 1, textDecoration: 'none', color: 'inherit' }}>
              SIW Festival · Recensioni
            </Typography>

            {/* Torna al sito Thymeleaf: e' un link normale e non un Link di
                React Router, perche' esce dall'app React. */}
            <Button href="/movies" color="inherit" size="small">Film</Button>

            {isAuthenticated ? (
              <>
                <Typography variant="body2" color="text.secondary">{username}</Typography>
                <Button onClick={logout} size="small">Esci</Button>
              </>
            ) : (
              <Button component={RouterLink} to="/login" variant="outlined" size="small">
                Accedi
              </Button>
            )}
          </Toolbar>
        </Container>
      </AppBar>

      <Box component="main" sx={{ flexGrow: 1 }}>
        <Outlet />
      </Box>
    </Box>
  )
}
