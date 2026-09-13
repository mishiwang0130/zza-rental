import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 开发期通过 Vite 代理访问后端，避免跨域；SSE 需要把代理的缓冲关掉。
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        // 后端地址跟随 application-local.yml 的 server.port
        target: process.env.VITE_PROXY_TARGET ?? 'http://localhost:8083',
        changeOrigin: true,
        ws: false,
      },
    },
  },
})
