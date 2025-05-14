import { defineConfig, UserConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'
import path from "path"
import tailwindcss from "@tailwindcss/vite"

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  build: {
    outDir: 'build',
  },
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  
  test: {
    globals: true, // Enables globals like `expect`, `vi`
    environment: 'jsdom', // Makes sure jsdom is used for the tests
    setupFiles: './src/setupTests.ts', // Points to setup file
    include: ['src/**/*.test.tsx', 'src/**/*.test.ts'],
  },
} as UserConfig)
