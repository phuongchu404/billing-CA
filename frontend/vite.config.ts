import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  build: {
    outDir: '../backend/plan-mng-web/src/main/resources/static',
    emptyOutDir: true,
  },
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ['vue', 'vue-router', 'pinia'],
      dts: 'src/auto-imports.d.ts',
    }),
    Components({
      resolvers: [ElementPlusResolver()],
      dts: 'src/components.d.ts',
    }),
  ],
  resolve: {
    alias: { '@': resolve(__dirname, 'src') },
  },
  server: {
    port: 3000,
    // Proxy disabled for offline mode
    /*
    proxy: {
      '/api': { target: 'http://localhost:9292', changeOrigin: true },
      '/internal': { target: 'http://localhost:9292', changeOrigin: true },
      '/webhooks': { target: 'http://localhost:9292', changeOrigin: true },
    },
    */
  },
})
