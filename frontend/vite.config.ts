import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: 'dist',
    rollupOptions: {
      output: {
        manualChunks: {
          'vendor-antd': ['antd', '@ant-design/icons'],
          'vendor-recharts': ['recharts'],
          'vendor-query': ['@tanstack/react-query'],
          'vendor-react': ['react', 'react-dom', 'react-router-dom'],
        },
      },
    },
  },
})
