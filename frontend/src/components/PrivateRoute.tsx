import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

/**
 * Rotta protetta (slide 15, lezione 3).
 *
 * E' una "layout route": non disegna nulla di suo, decide soltanto se mostrare
 * la rotta figlia tramite <Outlet /> oppure rimandare al login.
 *
 * `replace` sostituisce la voce nella cronologia invece di aggiungerne una:
 * senza, il tasto Indietro riporterebbe l'utente sulla pagina protetta,
 * che lo rispedirebbe al login, all'infinito.
 *
 * NOTA: la pagina delle recensioni NON va protetta con questo — la traccia
 * (§4.1) chiede che le recensioni siano leggibili da chiunque. E' il form
 * dentro la pagina a comparire o meno a seconda che ci sia un utente.
 */
export default function PrivateRoute() {
  const { isAuthenticated } = useAuth()

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  return <Outlet />
}
