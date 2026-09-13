
export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  username: string
  userId: number
  role: string
}

export interface ApiError {
  message: string
  fieldErrors?: Record<string, string>
}

export interface Review {
  id: number
  text: string
  rating: number
  reviewDate: string
  authorId: number
  authorName: string
  authorSurname: string
}

export interface ReviewRequest {
  text: string
  rating: number
}

export interface ReviewStats {
  averageRating: number | null
  totalReviews: number
  ratingDistribution: Record<number, number>
}
