import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { CssBaseline, ThemeProvider } from '@mui/material'

import theme from './theme'
import { AuthProvider } from './context/AuthContext'
import Layout from './components/Layout'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import MovieReviewsPage from './pages/MovieReviewsPage'
import NotFoundPage from './pages/NotFoundPage'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ThemeProvider theme={theme}>
      {/* azzera gli stili di default del browser e applica lo sfondo del tema */}
      <CssBaseline />

      {/* AuthProvider avvolge il Router: cosi' QUALSIASI componente, comprese
          le pagine e il Layout, puo' chiamare useAuth(). Se stesse dentro una
          rotta, i componenti fuori da quella rotta non vedrebbero il context. */}
      <AuthProvider>
        {/* basename: l'app e' servita sotto /reviews, non alla radice del sito.
            Deve combaciare con "base" in vite.config.ts. */}
        <BrowserRouter basename="/reviews">
          <Routes>
            <Route path="/" element={<Layout />}>
              <Route index element={<HomePage />} />
              {/* la lettura delle recensioni e' pubblica (traccia §4.1):
                  questa rotta NON va dentro <PrivateRoute /> */}
              <Route path="movies/:movieId" element={<MovieReviewsPage />} />
              <Route path="login" element={<LoginPage />} />
              <Route path="*" element={<NotFoundPage />} />
            </Route>
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </ThemeProvider>
  </StrictMode>,
)
