import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import wasm from 'vite-plugin-wasm';
import topLevelAwait from 'vite-plugin-top-level-await';

// https://vite.dev/config/
export default defineConfig({
  base: '/vendingmachine/', // important for nginx
  plugins: [react(),
    wasm(),
    topLevelAwait(),
  ],
  server: {
    port: 3000,
    host: true, // Allows access from other devices on the network
    strictPort: true, // Prevents the server from trying to use another port if 3000 is already in use
    allowedHosts: ['localhost', 'iamaurimas.xyz'],
  },
})
