import { Box, Button, Card, CardContent, Chip, Rating, Stack, Typography } from '@mui/material'
import type { Review } from '../types'

interface ReviewCardProps {
  review: Review
  isMine: boolean
  onEdit: (review: Review) => void
  onDelete: (review: Review) => void
}

export default function ReviewCard({ review, isMine, onEdit, onDelete }: ReviewCardProps) {

  const data = new Date(review.reviewDate).toLocaleDateString('it-IT', {
    day: 'numeric', month: 'long', year: 'numeric',
  })

  return (
    <Card variant="outlined">
      <CardContent>

        <Stack direction="row" spacing={2}
               sx={{ justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <Box>
            <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
              <Typography sx={{ fontWeight: 600 }}>
                {review.authorName} {review.authorSurname}
              </Typography>
              {isMine && <Chip label="la tua" size="small" color="primary" variant="outlined" />}
            </Stack>
            <Typography variant="body2" color="text.secondary">{data}</Typography>
          </Box>

          <Rating value={review.rating} readOnly size="small" />
        </Stack>

        <Typography sx={{ mt: 2, whiteSpace: 'pre-wrap' }}>{review.text}</Typography>

        {isMine && (
          <Stack direction="row" spacing={1} sx={{ mt: 2 }}>
            <Button size="small" onClick={() => onEdit(review)}>Modifica</Button>
            <Button size="small" color="error" onClick={() => onDelete(review)}>Elimina</Button>
          </Stack>
        )}

      </CardContent>
    </Card>
  )
}
