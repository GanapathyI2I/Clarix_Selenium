package tests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.*;
import pages.HomePage;
import pages.SalesPage;
import utils.ConfigUtil;
import utils.DBUtil;
import utils.TestResultLogger;

public class VehiclesInvoicedButNotDeliveredTest {
    private WebDriver driver;
    private HomePage homePage;
    private SalesPage salesPage;

    @BeforeClass
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", ConfigUtil.get("chrome.driver.path"));
        driver = new ChromeDriver();
        homePage = new HomePage(driver);
        salesPage = new SalesPage(driver);

        homePage.login(ConfigUtil.get("url"), ConfigUtil.get("username"), ConfigUtil.get("password"));
        homePage.switchToPowerBIFrame();
        homePage.clickTile(6); // Adjust index as needed
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void testVehiclesInvoicedButNotDelivered() throws Exception {
        String actual = salesPage.getValueByIndex(9); // Adjust index as needed
        String sql = "SELECT (COALESCE((SELECT SUM(qty) FROM commondatamodel.vehicles_sales WHERE invoice_date BETWEEN '2025-05-01 00:00:00' AND '2025-05-06 23:59:59' AND qty IN (1)), 0) + COALESCE((SELECT SUM(qty) FROM commondatamodel.vehicles_sales WHERE invoice_cancellation_date BETWEEN '2025-05-01 00:00:00' AND '2025-05-06 23:59:59' AND qty IN (-1, 0)), 0)) AS net_qty;";
        String expected = DBUtil.getExpectedValue(sql, "net_qty");
        System.out.println("vehiclesInvoicedButNotDelivered in App: " + actual + ", vehiclesInvoicedButNotDelivered in DB: " + expected);
        String status = actual.equals(expected) ? "pass" : "fail";
        TestResultLogger.log("vehiclesInvoicedButNotDelivered", expected, actual, status);
        Assert.assertEquals(actual, expected, "vehiclesInvoicedButNotDelivered");
    }
} 