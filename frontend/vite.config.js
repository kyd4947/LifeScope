import { defineConfig } from 'vite';

// Pages 배포 시 저장소 이름이 URL 경로에 포함된다.
// 로컬/nginx 배포에서는 루트 경로를 사용한다.
const basePath = process.env.VITE_BASE_PATH || '/';

export default defineConfig({
  base: basePath,
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
    // 정적 자산은 Pages 경로 또는 nginx 경로에 맞게 생성된다.
    assetsDir: 'assets',
  },
});
