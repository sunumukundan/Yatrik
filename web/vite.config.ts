import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// The dashboard talks to the gateway on :8080 — the only public entry point.
// In dev, Vite proxies /auth, /vehicles, /trips, ... so the browser sees one origin
// and CORS never enters the picture.
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    proxy: {
      '/auth': { target: 'http://localhost:8080', changeOrigin: true },
      '/vehicles': { target: 'http://localhost:8080', changeOrigin: true },
      '/drivers': { target: 'http://localhost:8080', changeOrigin: true },
      '/trips': { target: 'http://localhost:8080', changeOrigin: true },
      '/parties': { target: 'http://localhost:8080', changeOrigin: true },
      '/reminders': { target: 'http://localhost:8080', changeOrigin: true },
      '/maintenance': { target: 'http://localhost:8080', changeOrigin: true },
      '/work-orders': { target: 'http://localhost:8080', changeOrigin: true },
      '/compliance': { target: 'http://localhost:8080', changeOrigin: true },
      '/fleet': { target: 'http://localhost:8080', changeOrigin: true },
      '/reports': { target: 'http://localhost:8080', changeOrigin: true },
      '/ledger': { target: 'http://localhost:8080', changeOrigin: true },
      '/reporting': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
})
