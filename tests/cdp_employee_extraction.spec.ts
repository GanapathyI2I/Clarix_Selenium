import { test, expect } from '@playwright/test';
import fs from 'fs';

test('Extract CDP employee details and save as JSON', async ({ page }) => {
  // 1. Login
  await page.goto('https://iassistant.ideas2it.com/login');
  await page.fill('input[type="email"]', 'ganapathy.radhakrishnan@ideas2it.com');
  await page.fill('input[type="password"]', 'Gana@0505');
  await page.click('button[type="submit"]');
  await page.waitForSelector('text=Employee Lookup', { timeout: 60000 });

  // 2. Navigate to Employee Lookup > Projects
  await page.click('text=Employee Lookup');
  await page.click('text=Projects');
  await page.waitForSelector('input[placeholder="Search Projects"]', { timeout: 10000 });

  // 3. Search and select "Common Deployable Pool"
  await page.fill('input[placeholder="Search Projects"]', 'Common Deployable Pool');
  await page.focus('input[placeholder="Search Projects"]');
  await page.keyboard.press('Enter');
  await page.waitForTimeout(1000); // Wait for search results

  // Extract the expected employee count from the <p class="text-xl ml-1"> element before clicking
  const countText = await page.locator('p.text-xl.ml-1').first().textContent();
  const countMatch = countText && countText.match(/(\d+)\s*members/);
  const expectedCount = countMatch ? parseInt(countMatch[1], 10) : undefined;
  if (!expectedCount) throw new Error('Could not extract expected employee count from project card');

  // Now click the project card
  const projectCard = await page.getByText('Common Deployable Pool', { exact: false }).first();
  await projectCard.click();

  // 4. Scroll to load all employees
  const employeeContainer = page.locator('#projectEmployee');
const cardSelector = 'div.cursor-pointer.mb-2.w-full.relative.p-2.bg-white.rounded-2xl.shadow-sidebar';
  await employeeContainer.locator(cardSelector).first().waitFor({ timeout: 30000 });

  // Debug: Save screenshot and HTML
  await page.screenshot({ path: 'debug_employees.png', fullPage: true });
  const html = await page.content();
  fs.writeFileSync('debug_employees.html', html);

  let prevCount = 0;
  let sameCountTimes = 0;
  while (sameCountTimes < 3) {
    const cards = await employeeContainer.locator(cardSelector).count();
    await employeeContainer.evaluate((el: HTMLElement) => el.scrollBy(0, el.clientHeight));
    await page.waitForTimeout(1000);
    const newCount = await employeeContainer.locator(cardSelector).count();
    if (newCount === prevCount) {
      sameCountTimes++;
    } else {
      sameCountTimes = 0;
      prevCount = newCount;
    }
  }

  // 5. Extract employee details
  const employees = await employeeContainer.locator('div.cursor-pointer.mb-2.w-full.relative.p-2.bg-white.rounded-2xl.shadow-sidebar').evaluateAll((cards) =>
    cards.map(card => {
      const getText = (selector: string) => card.querySelector(selector)?.textContent?.trim() || '';
      const id = getText('p.text-base.text-dark.opacity-60');
      const name = getText('h5');
      let email = '';
      card.querySelectorAll('p').forEach(p => { if (p.textContent?.includes('@')) email = p.textContent.trim(); });
      let designation = '';
      card.querySelectorAll('div').forEach(div => {
        const txt = div.textContent?.toLowerCase() || '';
        if (
          txt.includes('engineer') || txt.includes('analyst') ||
          txt.includes('manager') || txt.includes('architect') ||
          txt.includes('specialist') || txt.includes('data engineer')
        ) {
          designation = div.textContent?.trim() || '';
        }
      });
      // Improved experience extraction
      let overallExp = '';
      let ideas2itExp = '';
      card.querySelectorAll('div.text-center').forEach(div => {
        const ps = div.querySelectorAll('p');
        if (ps[0]?.textContent?.toLowerCase().includes('overall experience')) {
          overallExp = ps[1]?.textContent?.trim() || '';
        }
        if (ps[0]?.textContent?.toLowerCase().includes('ideas2it experience')) {
          ideas2itExp = ps[1]?.textContent?.trim() || '';
        }
      });
      return { id, name, email, designation, overallExp, ideas2itExp };
    })
  );

  // Print the count of CDP employees
  console.log('Extracted CDP employee count:', employees.length);

  // Assert the count matches the expected count
  expect(employees.length).toBe(expectedCount);

  // 6. Save as JSON
  fs.writeFileSync('tests/cdp_employees.json', JSON.stringify(employees, null, 2), 'utf-8');
  console.log('Employee details saved to tests/cdp_employees.json');

  // 7. Log out
  await page.click('#headerDropDownContainer');
  await page.click('text=Logout');
}, 120000); // 2 minutes 