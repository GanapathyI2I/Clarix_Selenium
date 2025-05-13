package tests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.*;
import pages.HomePage;
import pages.SalesPage;
import utils.ConfigUtil;
import utils.DBUtil;
import utils.TestResultLogger;
import base.BaseTest;
import java.util.List;

public class LastMonth extends BaseTest {
   
    @Test
    public void testLastMonth() throws Exception {
        // Get UI values
        List<WebElement> ticks = driver.findElements(By.cssSelector("g.tick text"));
        List<WebElement> elements = driver.findElements(By.cssSelector("g.label-container text tspan"));

        if (ticks.size() > 4 && elements.size() > 1) {
            String fifthLabel = ticks.get(4).getText(); // 5th tick
            String actual = elements.get(5).getText(); // 6th label value as actual value
            System.out.println("The Last Month data is " + fifthLabel + " = " + actual);

            // Get DB value using your query
            String sql = "SELECT (COALESCE((SELECT SUM(qty) FROM commondatamodel.vehicles_sales WHERE invoice_date BETWEEN '2025-04-01 00:00:00' AND '2025-04-30 23:59:59' AND qty IN (1)), 0) + COALESCE((SELECT SUM(qty) FROM commondatamodel.vehicles_sales WHERE invoice_cancellation_date BETWEEN '2025-04-01 00:00:00' AND '2025-04-30 23:59:59' AND qty IN (-1, 0)), 0)) AS net_qty;";
            String expected = DBUtil.getExpectedValue(sql, "net_qty");

            // Log the comparison
            System.out.println("Last Month in UI: " + actual + ", Last Month in DB: " + expected);
            
            // Log the test result
            String status = actual.equals(expected) ? "pass" : "fail";
            TestResultLogger.log("Last Month", expected, actual, status);

            // Assert the values match
            Assert.assertEquals(actual, expected, "Last Month value mismatch between UI and DB");
        } else {
            throw new Exception("Required UI elements not found");
        }
    }
} 