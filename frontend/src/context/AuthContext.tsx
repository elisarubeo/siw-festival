import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { login as loginRequest } from '../services/authService'
import { TOKEN_KEY } from '../services/api'

/**
 * Stato di autenticazione condiviso da tutta l'app.
 *
 * E' il caso d'uso da manuale di useContext (slide 32, lezione 1): l'utente
 * collegato serve in punti lontani dell'albero dei componenti — la navbar, il
 * form delle recensioni, il pulsante Elimina — e passarlo di prop in prop
 * sarebbe ingestibile.
 */

const USERNAME_KEY = 'siw.username'
const ROLE_KEY = 'siw.role'
const USERID_KEY = 'siw.userId'

interface AuthContextValue {
  token: string | null
  username: string | null
  /** si confronta con ReviewDto.authorId per riconoscere le proprie recensioni */
  userId: number | null
  role: string | null
  isAuthenticated: boolean
  isAdmin: boolean
  login: (username: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(null)
  const [username, setUsername] = useState<string | null>(null)
  const [userId, setUserId] = useState<number | null>(null)
  const [role, setRole] = useState<string | null>(null)

  /**
   * Token recovery: al primo render rilegge quello che c'e' in localStorage.
   * Senza questo effetto, ogni ricarica della pagina sloggherebbe l'utente
   * anche con un token ancora valido.
   */
  useEffect(() => {
    setToken(localStorage.getItem(TOKEN_KEY))
    setUsername(localStorage.getItem(USERNAME_KEY))
    const salvato = localStorage.getItem(USERID_KEY)
    setUserId(salvato === null ? null : Number(salvato))
    setRole(localStorage.getItem(ROLE_KEY))
  }, [])

  async function login(u: string, p: string) {
    const risposta = await loginRequest(u, p)
    /* localStorage per sopravvivere alla ricarica, lo stato React per far
       ridisegnare i componenti: servono entrambi, non e' una ripetizione. */
    localStorage.setItem(TOKEN_KEY, risposta.token)
    localStorage.setItem(USERNAME_KEY, risposta.username)
    localStorage.setItem(USERID_KEY, String(risposta.userId))
    localStorage.setItem(ROLE_KEY, risposta.role)
    setToken(risposta.token)
    setUsername(risposta.username)
    setUserId(risposta.userId)
    setRole(risposta.role)
  }

  function logout() {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USERNAME_KEY)
    localStorage.removeItem(USERID_KEY)
    localStorage.removeItem(ROLE_KEY)
    setToken(null)
    setUsername(null)
    setUserId(null)
    setRole(null)
  }

  const value: AuthContextValue = {
    token,
    username,
    userId,
    role,
    isAuthenticated: token !== null,
    isAdmin: role === 'ADMIN',
    login,
    logout,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

/**
 * Hook di accesso al context. Incapsularlo cosi' evita di importare
 * useContext e AuthContext in ogni componente, ed e' la convenzione che usano
 * anche le slide: const { isAuthenticated } = useAuth()
 */
export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth va usato dentro <AuthProvider>')
  }
  return context
}
