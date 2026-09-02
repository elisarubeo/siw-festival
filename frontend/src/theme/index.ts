import { createTheme } from '@mui/material/styles'

/**
 * Tema MUI dell'app. Personalizzare qui palette, font e forme significa
 * cambiarli ovunque in una volta sola, invece di ritoccare i singoli
 * componenti.
 */
const theme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: '#0d6e5c' },
    secondary: { main: '#a15421' },
    background: { default: '#f4f6f5' },
  },
  typography: {
    fontFamily: '"Source Sans 3", system-ui, -apple-system, sans-serif',
    h1: { fontSize: '2rem', fontWeight: 600 },
    h2: { fontSize: '1.5rem', fontWeight: 600 },
  },
  shape: { borderRadius: 6 },
})

export default theme
