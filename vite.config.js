import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api/portfolios': 'http://localhost:8082',
      '/api/prices': 'http://localhost:8082',
      '/api/watchlists': 'http://localhost:8083',
      '/api/alerts': 'http://localhost:8084',
      '/ws': { target: 'ws://localhost:8080', ws: true },
    },
  },
})
