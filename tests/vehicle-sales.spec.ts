import { test, expect } from '@playwright/test';
import { VehicleSalesPage } from './vehicleSalesPage';

test.describe('Vehicle Sales Extraction', () => {
  test('should extract vehicle sold and sales values from PowerBI', async ({ page }) => {
    const salesPage = new VehicleSalesPage(page);
    await salesPage.login('user', 'user');
    await salesPage.gotoSalesMenu();
    await salesPage.switchToPowerBIFrame();
    await salesPage.clickSeventhTile();
    const vehicleQty = await salesPage.extractVehicleSold();
    const vehicleSales = await salesPage.extractVehicleSales();

    expect(vehicleQty).toBeDefined();
    expect(vehicleSales).toBeDefined();
    console.log('Vehicle Sold:', vehicleQty);
    console.log('Vehicle Sales:', vehicleSales);
  });
}); 