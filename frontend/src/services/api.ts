import axios from 'axios'

/**
 * Istanza Axios condivisa da tutti i servizi.
 *
 * La baseURL si scrive UNA VOLTA SOLA qui: nei servizi si passano solo i
 * percorsi relativi ('/movies/1/reviews'). E' un percorso assoluto e non un
 * URL completo, quindi in sviluppo lo intercetta il proxy di Vite e in
 * produzione punta da solo allo stesso server che ha servito la pagina.
 */
const api = axios.create({
  baseURL: '/api',
})

export const TOKEN_KEY = 'siw.token'

/**
 * Interceptor di RICHIESTA: allega il JWT a ogni chiamata, se c'e'.
 * Senza, ogni scrittura tornerebbe 401 e bisognerebbe ricordarsi di passare
 * l'header a mano in ogni singola funzione di servizio.
 */
api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

/**
 * Interceptor di RISPOSTA: il token dura 24 ore, prima o poi scade.
 * Quando il backend risponde 401 il token non vale piu': lo si butta e si
 * manda l'utente al login. Senza questo, l'app resterebbe bloccata su errori
 * incomprensibili finche' non si svuota la cache del browser a mano.
 */
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem('siw.username')
      localStorage.removeItem('siw.role')
      /* Non si usa useNavigate: qui siamo fuori da un componente React.
         Il controllo evita un ciclo infinito se e' il login stesso a fallire. */
      if (!window.location.pathname.endsWith('/login')) {
        window.location.href = '/reviews/login'
      }
    }
    return Promise.reject(error)
  },
)

/**
 * Traduce un errore di Axios nel messaggio leggibile preparato dal backend.
 * I servizi la usano nei loro catch, cosi' i componenti ricevono una stringa
 * gia' pronta da mostrare e non un oggetto errore da interpretare.
 */
export function messaggioErrore(error: unknown, fallback: string): string {
  if (axios.isAxiosError(error)) {
    return error.response?.data?.message ?? error.message ?? fallback
  }
  return fallback
}

export default api
