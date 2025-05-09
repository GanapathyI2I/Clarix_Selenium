import { test } from '@playwright/test';
import { expect } from '@playwright/test';

test('LoginTest_2025-05-08', async ({ page, context }) => {
  
    // Navigate to URL
    await page.goto('https://sks.clarix.in/home');

    // Fill input field
    await page.fill('#username', 'user');

    // Fill input field
    await page.fill('#password', 'user');

    // Click element
    await page.click("button[type='submit']");
    await page.waitForTimeout(25000);
    
    // 1. Wait for the PowerBI iframe and switch context
    const iframeElement = await page.waitForSelector('iframe[src*="app.powerbi.com/reportEmbed"]', { timeout: 60000 });
    const frame = await iframeElement.contentFrame();
    if (!frame) throw new Error('iframe contentFrame is null');

    // 2. Wait for the 7th SVG tile and click it
    await frame.waitForSelector('svg g.tile', { timeout: 60000 });
    await frame.click('svg g.tile >> nth=6', { position: { x: 39.97, y: 34.57 } });

    await frame.waitForSelector('svg[aria-label*="New Vehicle Qty_"]', { timeout: 60000 });

  // 4. Try to extract the number from the aria-label attribute
  const ariaLabel = await frame.getAttribute('svg[aria-label*="New Vehicle Qty_"]', 'aria-label');
  let vehicleQty = ariaLabel?.match(/New Vehicle Qty_ *(\d+)/)?.[1];

  // 5. Fallback: Try to extract from the <text class="value"> element
  if (!vehicleQty) {
    const textValue = await frame.textContent('svg[aria-label*="New Vehicle Qty_"] text.value');
    vehicleQty = textValue?.trim() || undefined;
  }

  // Extract Vehicle Sales value
  await frame.waitForSelector('svg[aria-label*="New Sales Valuesk_"]', { timeout: 60000 });
  const salesAriaLabel = await frame.getAttribute('svg[aria-label*="New Sales Valuesk_"]', 'aria-label');
  let vehicleSales = salesAriaLabel?.match(/New Sales Valuesk_ *([\d.,₹ ]+L)/)?.[1];
  if (!vehicleSales) {
    const salesTextValue = await frame.textContent('svg[aria-label*="New Sales Valuesk_"] text.value');
    vehicleSales = salesTextValue?.trim() || undefined;
  }

  console.log('Vehicle Sold:', vehicleQty);
  console.log('Vehicle Sales:', vehicleSales);
});