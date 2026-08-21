import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// Proxy /api sang backend Spring Boot: trình duyệt tưởng gọi cùng origin nên không dính CORS,
// và code frontend không bao giờ hardcode http://localhost:8080.
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    // 5173 mặc định đã bị container pengbot-client chiếm trên máy này — ghim cổng khác
    // và strictPort để lỗi ngay thay vì lặng lẽ nhảy cổng gây khó tìm.
    port: 5174,
    strictPort: true,
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
