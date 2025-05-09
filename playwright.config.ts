import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  use: {
    headless: false,
    // You can add other options here if needed
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    // {
    //   name: 'firefox',
    //   use: { ...devices['Desktop Firefox'] },
    // },
    // {
    //   name: 'webkit',
    //   use: { ...devices['Desktop Safari'] },
    // },
  ],
  timeout: 60000,
}); 