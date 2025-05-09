import { Page, Frame } from '@playwright/test';

export class VehicleSalesPage {
  readonly page: Page;
  frame: Frame | null = null;

  constructor(page: Page) {
    this.page = page;
  }

  async login(username: string, password: string) {
    await this.page.goto('https://sks.clarix.in/home');
    await this.page.fill('#username', username);
    await this.page.fill('#password', password);
    await this.page.click("button[type='submit']");
    await this.page.waitForTimeout(25000);
  }

  async gotoSalesMenu() {
    await this.page.click('a:has-text("Sales")');
  }

  async switchToPowerBIFrame() {
    const iframeElement = await this.page.waitForSelector('iframe[src*="app.powerbi.com/reportEmbed"]', { timeout: 60000 });
    this.frame = await iframeElement.contentFrame();
    if (!this.frame) throw new Error('iframe contentFrame is null');
  }

  async clickSeventhTile() {
    if (!this.frame) throw new Error('Frame not initialized');
    await this.frame.waitForSelector('svg g.tile', { timeout: 60000 });
    await this.frame.click('svg g.tile >> nth=6', { position: { x: 39.97, y: 34.57 } });
  }

  async extractVehicleSold() {
    if (!this.frame) throw new Error('Frame not initialized');
    await this.frame.waitForSelector('svg[aria-label*="New Vehicle Qty_"]', { timeout: 60000 });
    const ariaLabel = await this.frame.getAttribute('svg[aria-label*="New Vehicle Qty_"]', 'aria-label');
    let vehicleQty = ariaLabel?.match(/New Vehicle Qty_ *(\d+)/)?.[1];
    if (!vehicleQty) {
      const textValue = await this.frame.textContent('svg[aria-label*="New Vehicle Qty_"] text.value');
      vehicleQty = textValue?.trim() || undefined;
    }
    return vehicleQty;
  }

  async extractVehicleSales() {
    if (!this.frame) throw new Error('Frame not initialized');
    await this.frame.waitForSelector('svg[aria-label*="New Sales Valuesk_"]', { timeout: 60000 });
    const salesAriaLabel = await this.frame.getAttribute('svg[aria-label*="New Sales Valuesk_"]', 'aria-label');
    let vehicleSales = salesAriaLabel?.match(/New Sales Valuesk_ *([\d.,₹ ]+L)/)?.[1];
    if (!vehicleSales) {
      const salesTextValue = await this.frame.textContent('svg[aria-label*="New Sales Valuesk_"] text.value');
      vehicleSales = salesTextValue?.trim() || undefined;
    }
    return vehicleSales;
  }
} 