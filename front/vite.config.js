import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
    plugins: [vue()],
    define: {
        global: 'globalThis',
    },
    resolve: {
        alias: {
            './runtimeConfig': './runtimeConfig.browser',
        }
    },
    optimizeDeps: {
        include: ['sockjs-client']
    }
})