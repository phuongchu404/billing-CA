import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import AutoImport from "unplugin-auto-import/vite";
import Components from "unplugin-vue-components/vite";
import { ElementPlusResolver } from "unplugin-vue-components/resolvers";

export default defineConfig({
  build: {
    outDir: "./dist",
    emptyOutDir: true,
  },
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ["vue", "vue-router", "pinia"],
      dts: "src/auto-imports.d.ts",
    }),
    Components({
      resolvers: [ElementPlusResolver()],
      dts: "src/components.d.ts",
    }),
  ],
  resolve: {
    alias: { "@": fileURLToPath(new URL("./src", import.meta.url)) },
  },
  server: {
    port: 3000,
    // Proxy disabled for offline mode

    proxy: {
      "/api": { target: "http://10.30.21.123:9292", changeOrigin: true },
      "/internal": { target: "http://10.30.21.123:9292", changeOrigin: true },
      "/webhooks": { target: "http://10.30.21.123:9292", changeOrigin: true },
    },
  },
});
