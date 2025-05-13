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

public class CurrentMonth extends BaseTest {
   
    @Test
    public void testCurrentMonth() throws Exception {
        // Get UI values
        List<WebElement> ticks = driver.findElements(By.cssSelector("g.tick text"));
        List<WebElement> elements = driver.findElements(By.cssSelector("g.label-container text tspan"));

        if (ticks.size() > 5 && elements.size() > 1) {
            String sixthLabel = ticks.get(5).getText(); // 6th tick
            String actual = elements.get(1).getText(); // 2nd label value as actual value
            System.out.println("The current Month data is " + sixthLabel + " = " + actual);

            // Get DB value using your query
            String sql = "SELECT (COALESCE((SELECT SUM(qty) FROM commondatamodel.vehicles_sales WHERE invoice_date BETWEEN '2025-05-01 00:00:00' AND '2025-05-06 23:59:59' AND qty IN (1)), 0) + COALESCE((SELECT SUM(qty) FROM commondatamodel.vehicles_sales WHERE invoice_cancellation_date BETWEEN '2025-05-01 00:00:00' AND '2025-05-06 23:59:59' AND qty IN (-1, 0)), 0)) AS net_qty;";
            String expected = DBUtil.getExpectedValue(sql, "net_qty");

            // Log the comparison
            System.out.println("Current Month in UI: " + actual + ", Current Month in DB: " + expected);
            
            // Log the test result
            String status = actual.equals(expected) ? "pass" : "fail";
            TestResultLogger.log("Current Month", expected, actual, status);

            // Assert the values match
            Assert.assertEquals(actual, expected, "Current Month value mismatch between UI and DB");
        } else {
            throw new Exception("Required UI elements not found");
        }
    }
} 