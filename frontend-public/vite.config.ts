import { defineConfig, loadEnv } from "vite";
import vue from "@vitejs/plugin-vue";
import { resolve } from "path";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const apiTarget = env.VITE_API_URL || "http://localhost:15555";

  return {
    base: mode === "production" ? "/subfolder/" : "/",
    build: {
      outDir: "dist",
      assetsDir: "static",
      emptyOutDir: true,
    },
    plugins: [vue()],
    resolve: {
      alias: { "@": resolve(__dirname, "src") },
    },
    server: {
      port: 8097,
      proxy: {
        "/api": { target: apiTarget, changeOrigin: true, secure: false },
        "/actuator": { target: apiTarget, changeOrigin: true, secure: false },
        "/stylesheets": { target: apiTarget, changeOrigin: true, secure: false },
        "/javascripts": { target: apiTarget, changeOrigin: true, secure: false },
        "/images": { target: apiTarget, changeOrigin: true, secure: false },
      },
    },
  };
});
