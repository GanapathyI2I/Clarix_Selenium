
import { test } from '@playwright/test';
import { expect } from '@playwright/test';

test('MCPLoginPowerBITest_2025-05-08', async ({ page, context }) => {
  
    // Navigate to URL
    await page.goto('https://sks.clarix.in/home');

    // Fill input field
    await page.fill('#username', 'user');

    // Fill input field
    await page.fill('#password', 'user');

    // Click element
    await page.click('button[type='submit']');

    // Fill input field
    await page.fill('body', '');
});