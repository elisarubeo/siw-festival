/**
 * Le interfacce TypeScript ricalcano i DTO Java del backend.
 *
 * Se il backend cambia, si aggiorna qui e il compilatore segnala tutti i punti
 * rotti: e' il motivo per cui vale la pena tenerle allineate a mano invece di
 * usare `any`.
 *
 * Attenzione ai tipi: un LocalDate Java arriva come stringa "YYYY-MM-DD",
 * un Long come number.
 */

/* ---------- autenticazione (gia' implementata lato backend) ---------- */

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  username: string
  /* i ruoli nel database sono "DEFAULT" e "ADMIN", non "USER": vedi
     Credentials.DEFAULT_ROLE nel backend */
  role: string
}

/** Forma unica del corpo di errore restituito da /api quando qualcosa fallisce. */
export interface ApiError {
  message: string
  fieldErrors?: Record<string, string>
}

/* ---------- recensioni ----------
   Questi tipi sono il CONTRATTO con i record Java che scriverai:
   ReviewDto, ReviewRequest e ReviewStatsDto. Tieni i nomi dei campi
   identici da una parte e dall'altra, altrimenti Jackson serializza
   qualcosa che qui risulta undefined senza che nessuno se ne accorga. */

export interface Review {
  id: number
  text: string
  rating: number
  /** LocalDate lato Java: "2026-09-02" */
  reviewDate: string
  authorName: string
  authorSurname: string
  /** serve a capire se la recensione e' dell'utente collegato */
  authorUsername: string
}

export interface ReviewRequest {
  text: string
  rating: number
}

export interface ReviewStats {
  average: number | null
  total: number
  /** voto (1..5) -> quante recensioni con quel voto */
  distribution: Record<number, number>
}
