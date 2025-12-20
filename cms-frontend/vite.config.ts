import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src') // 让 @ 指向 src 目录
    }
  },
  server: {
    port: 5173,
    proxy: {
      // 只要是 /api 开头的请求，都转发给 Tomcat/Spring Boot
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // rewrite: (path) => path.replace(/^\/api/, '') // 如果你后端Controller里写了 /api 这里就不用 rewrite，保留即可
      }
    }
  }
})