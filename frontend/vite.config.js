import { defineConfig } from 'vite';

// 상대경로 /api 호출을 Spring Boot(8080)로 전달하는 개발용 프록시
// 운영에서는 nginx가 동일 역할을 수행하므로 프론트 코드 변경 불필요
export default defineConfig({
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/actuator': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: 'dist',
    // nginx가 SPA 라우팅을 처리하므로 상대 경로로 빌드
    assetsDir: 'assets',
  },
});
