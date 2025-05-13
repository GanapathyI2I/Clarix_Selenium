package tests;

import org.testng.Assert;
import org.testng.annotations.*;
import base.BaseTest;
import utils.DBUtil;
import utils.TestResultLogger;

public class VehiclesInvoicedYesterdayTest extends BaseTest {
  
    @Test
    public void testVehiclesInvoicedYesterday() throws Exception {
        String actual = salesPage.getValueByIndex(7); // Adjust index as needed
        String sql = "SELECT (COALESCE((SELECT SUM(qty) FROM commondatamodel.vehicles_sales WHERE invoice_date BETWEEN '2025-05-01 00:00:00' AND '2025-05-06 23:59:59' AND qty IN (1)), 0) + COALESCE((SELECT SUM(qty) FROM commondatamodel.vehicles_sales WHERE invoice_cancellation_date BETWEEN '2025-05-01 00:00:00' AND '2025-05-06 23:59:59' AND qty IN (-1, 0)), 0)) AS net_qty;";
        String expected = DBUtil.getExpectedValue(sql, "net_qty");
        System.out.println("vehiclesInvoicedYesterday in App: " + actual + ", vehiclesInvoicedYesterday in DB: " + expected);
        String status = actual.equals(expected) ? "pass" : "fail";
        TestResultLogger.log("vehiclesInvoicedYesterday", expected, actual, status);
        Assert.assertEquals(actual, expected, "vehiclesInvoicedYesterday");
    }
    }
