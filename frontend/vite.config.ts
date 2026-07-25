import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { VitePWA } from 'vite-plugin-pwa'
import { resolve } from 'path'

export default defineConfig({
  plugins: [
    vue(),
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['favicon.ico', 'apple-touch-icon.png'],
      manifest: {
        name: '祥雲智方中醫診症',
        short_name: '祥雲智方',
        description: '中醫線上問診、電子處方、代煎配送一站式平台',
        theme_color: '#2d6a4f',
        background_color: '#f8faf8',
        display: 'standalone',
        icons: [
          { src: '/icon-192.png', sizes: '192x192', type: 'image/png' },
          { src: '/icon-512.png', sizes: '512x512', type: 'image/png' }
        ]
      },
      workbox: {
        globPatterns: ['**/*.{js,css,html,ico,png,svg,woff2}'],
        runtimeCaching: [
          {
            urlPattern: /^https:\/\/api\.xiangyun.*\/api\/v1\/.*/i,
            handler: 'NetworkFirst',
            options: { cacheName: 'api-cache', expiration: { maxEntries: 50, maxAgeSeconds: 300 } }
          }
        ]
      }
    })
  ],
  resolve: {
    alias: {
      '@ui': resolve(__dirname, 'packages/ui/src'),
      '@utils': resolve(__dirname, 'packages/utils/src'),
      '@patient': resolve(__dirname, 'apps/patient/src'),
      '@doctor': resolve(__dirname, 'apps/doctor/src'),
      '@admin': resolve(__dirname, 'apps/admin/src')
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `$primary: #2d6a4f; $accent: #d4a373; $bg: #f8faf8;`
      }
    }
  }
})
