import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],

  /* L'app non sta alla radice del sito ma sotto /reviews: senza questo, i
     riferimenti ai file JS e CSS generati punterebbero a /assets/... e in
     produzione darebbero 404. Deve combaciare con il basename passato a
     BrowserRouter in main.tsx e con il resource handler di ReactAppConfig.java. */
  base: '/reviews/',

  build: {
    /* Il build finisce direttamente fra le risorse statiche di Spring Boot,
       cosi' l'applicazione e' una sola: un processo, un JAR, un repository. */
    outDir: '../src/main/resources/static/reviews',
    emptyOutDir: true,
  },

  server: {
    port: 5173,
    /* In sviluppo React gira su :5173 e Spring su :8080. Inoltrando /api al
       backend, per il browser tutto arriva dalla stessa origine: niente CORS,
       niente preflight, e in React si scrivono solo percorsi relativi che
       funzionano identici anche in produzione. */
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
