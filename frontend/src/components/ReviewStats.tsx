import { Box, LinearProgress, Paper, Rating, Stack, Typography } from '@mui/material'
import type { ReviewStats as Stats } from '../types'

export default function ReviewStats({ stats }: { stats: Stats }) {

  if (stats.totalReviews === 0 || stats.averageRating === null) {
    return (
      <Paper variant="outlined" sx={{ p: 3 }}>
        <Typography color="text.secondary">
          Questo film non ha ancora recensioni.
        </Typography>
      </Paper>
    )
  }

  const media = stats.averageRating

  return (
    <Paper variant="outlined" sx={{ p: 3 }}>
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={4} sx={{ alignItems: { sm: 'center' } }}>

        <Box sx={{ textAlign: 'center', minWidth: 120 }}>
          <Typography variant="h3" component="p" sx={{ fontWeight: 600, lineHeight: 1 }}>
            {media.toFixed(1)}
          </Typography>
          <Rating value={media} precision={0.1} readOnly sx={{ mt: 1 }} />
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
            {stats.totalReviews} {stats.totalReviews === 1 ? 'recensione' : 'recensioni'}
          </Typography>
        </Box>

        <Stack spacing={0.5} sx={{ flexGrow: 1, width: '100%' }}>
          {[5, 4, 3, 2, 1].map((voto) => {
            const quante = stats.ratingDistribution[voto] ?? 0
            const percentuale = (quante / stats.totalReviews) * 100

            return (
              <Stack key={voto} direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
                <Typography variant="body2" sx={{ minWidth: 16, textAlign: 'right' }}>
                  {voto}
                </Typography>
                <LinearProgress
                  variant="determinate"
                  value={percentuale}
                  sx={{ flexGrow: 1, height: 8, borderRadius: 4 }}
                />
                <Typography variant="body2" color="text.secondary"
                            sx={{ minWidth: 24, fontVariantNumeric: 'tabular-nums' }}>
                  {quante}
                </Typography>
              </Stack>
            )
          })}
        </Stack>

      </Stack>
    </Paper>
  )
}
