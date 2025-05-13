package tests;

import org.testng.Assert;
import org.testng.annotations.*;
import base.BaseTest;
import utils.DBUtil;
import utils.TestResultLogger;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VehicleSalesTest extends BaseTest {
    
    // Constants for conversion factors
    private static final Map<String, Double> UNIT_FACTORS = Map.of(
        "L", 100000.0,     // Lakh
        "CR", 10000000.0,  // Crore
        "C", 10000000.0,   // Crore (alternate)
        "K", 1000.0,       // Thousand
        "M", 1000000.0,    // Million
        "T", 1000000000000.0 // Trillion
    );
    
    // Compiled patterns for better performance
    private static final Pattern NUMBER_UNIT_PATTERN = Pattern.compile("(\\d+\\.?\\d*)\\s*([KLCMTcr]+)?");
    private static final Pattern UNIT_PATTERN = Pattern.compile("[\\d\\s\\.]+([KLCMTcr]+)");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("(\\d+)(?:\\.(\\d+))?");

    @Test
    public void testVehicleSales() throws Exception {
        // Get and clean app value
        String appValueRaw = salesPage.getValueByIndex(5);
        String cleanValue = appValueRaw.replace("?", "").trim();
        
        // Extract values for comparison
        double appNumericValue = extractNumericValue(cleanValue);
        double dbNumericValue = getDBValue();
        double roundedDbValue = roundValueToMatchAppPrecision(dbNumericValue, cleanValue);
        
        // Create formatted values for logging
        DecimalFormat df = new DecimalFormat("0.00");
        
        // Log for debugging
        System.out.println("App value (raw): " + appValueRaw);
        System.out.println("App value (numeric): " + df.format(appNumericValue));
        System.out.println("DB value (original): " + df.format(dbNumericValue));
        System.out.println("DB value (rounded): " + df.format(roundedDbValue));
        
        // Compare and assert
        boolean valuesMatch = Math.abs(appNumericValue - roundedDbValue) < 1.0;
        
        // Log with raw values directly in the DB and APP fields
        TestResultLogger.log(
            "Vehicle Sales", 
            String.valueOf(dbNumericValue),            // DB value (raw)
            appValueRaw,                               // APP value (raw)
            valuesMatch ? "pass" : "fail"
        );
        
        Assert.assertTrue(valuesMatch, 
            String.format("Values don't match - App: %s, DB (rounded): %s", 
                        df.format(appNumericValue), df.format(roundedDbValue)));
    }
    
    /**
     * Gets value from database
     */
    private double getDBValue() throws Exception {
        String sql = "SELECT " +
                  "(COALESCE((SELECT SUM(invoice_amount) " +
                  "FROM commondatamodel.vehicles_sales " +
                  "WHERE invoice_date BETWEEN '2025-05-01 00:00:00' AND '2025-05-06 23:59:59' " +
                  "AND qty IN (1)), 0) - " +
                  "COALESCE((SELECT SUM(invoice_amount) " +
                  "FROM commondatamodel.vehicles_sales " +
                  "WHERE invoice_cancellation_date BETWEEN '2025-05-01 00:00:00' AND '2025-05-06 23:59:59' " +
                  "AND qty IN (-1)), 0)) AS net_qty;";
        
        String dbValue = DBUtil.getExpectedValue(sql, "net_qty");
        return Double.parseDouble(dbValue);
    }
    
    /**
     * Rounds the DB value to match app display precision
     */
    private double roundValueToMatchAppPrecision(double dbValue, String appDisplayFormat) {
        // Get unit and precision info from app display
        String unit = extractUnit(appDisplayFormat);
        int decimalPlaces = extractDecimalPlaces(appDisplayFormat);
        
        // Convert to display unit (e.g., convert to lakhs)
        Double factor = UNIT_FACTORS.getOrDefault(unit, 1.0);
        double valueInDisplayUnit = dbValue / factor;
        
        // Round to proper precision
        BigDecimal bd = BigDecimal.valueOf(valueInDisplayUnit)
                                .setScale(decimalPlaces, RoundingMode.HALF_UP);
        
        // Convert back to raw value
        return bd.doubleValue() * factor;
    }
    
    /**
     * Extracts the unit symbol from a formatted value
     */
    private String extractUnit(String formattedValue) {
        Matcher matcher = UNIT_PATTERN.matcher(formattedValue);
        return matcher.find() ? matcher.group(1).toUpperCase() : "";
    }
    
    /**
     * Extracts number of decimal places in the formatted value
     */
    private int extractDecimalPlaces(String formattedValue) {
        Matcher matcher = DECIMAL_PATTERN.matcher(formattedValue);
        return matcher.find() && matcher.group(2) != null ? 
               matcher.group(2).length() : 2; // Default to 2 if not found
    }
    
    /**
     * Extracts numeric value from a formatted string with units
     */
    private double extractNumericValue(String formattedValue) {
        Matcher matcher = NUMBER_UNIT_PATTERN.matcher(formattedValue);
        
        if (matcher.find()) {
            // Extract numeric value and unit
            double value = Double.parseDouble(matcher.group(1));
            String unit = matcher.group(2) != null ? matcher.group(2).toUpperCase() : "";
            
            // Apply conversion factor if unit exists
            Double factor = UNIT_FACTORS.getOrDefault(unit, 1.0);
            return value * factor;
        } 
        
        // Fallback: try parsing as plain number
        try {
            return Double.parseDouble(formattedValue);
        } catch (NumberFormatException e) {
            System.err.println("Unable to parse value: " + formattedValue);
            return 0.0;
        }
    }
} 