import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginTest {
    public static void main(String[] args) {
        // Set up ChromeDriver (make sure chromedriver is in your PATH)
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\ganapathy.r\\Chrome\\chromedriver-win64\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));

        try {
            // 1. Navigate to URL
            driver.get("https://sks.clarix.in/home");

            // 2. Fill input fields
            driver.findElement(By.id("username")).sendKeys("user");
            driver.findElement(By.id("password")).sendKeys("user");

            // 3. Click submit button
            driver.findElement(By.cssSelector("button[type='submit']")).click();

            // 4. Wait for PowerBI iframe and switch context
            WebElement iframe = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("iframe[src*='app.powerbi.com/reportEmbed']")
            ));
            driver.switchTo().frame(iframe);

            // 5. Wait for the 7th SVG tile and click it
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("svg g.tile")));
            java.util.List<WebElement> tiles = driver.findElements(By.cssSelector("svg g.tile"));
            if (tiles.size() > 6) {
                tiles.get(6).click();
            }

            // 6. Wait for the "New Vehicle Qty_" SVG
            WebElement vehicleQtySvg = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("svg[aria-label*='New Vehicle Qty_']")
            ));

            // 7. Try to extract the number from the aria-label attribute
            String ariaLabel = vehicleQtySvg.getAttribute("aria-label");
            String vehicleQty = null;
            if (ariaLabel != null) {
                Matcher m = Pattern.compile("New Vehicle Qty_ *(\\d+)").matcher(ariaLabel);
                if (m.find()) {
                    vehicleQty = m.group(1);
                }
            }

            // 8. Fallback: Try to extract from the <text class="value"> element
            if (vehicleQty == null) {
                try {
                    WebElement valueText = vehicleQtySvg.findElement(By.cssSelector("text.value"));
                    vehicleQty = valueText.getText().trim();
                } catch (Exception e) {
                    vehicleQty = null;
                }
            }

            // 9. Extract Vehicle Sales value
            WebElement salesSvg = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("svg[aria-label*='New Sales Valuesk_']")
            ));
            String salesAriaLabel = salesSvg.getAttribute("aria-label");
            String vehicleSales = null;
            if (salesAriaLabel != null) {
                Matcher m = Pattern.compile("New Sales Valuesk_ *([\\d.,₹ ]+L)").matcher(salesAriaLabel);
                if (m.find()) {
                    vehicleSales = m.group(1);
                }
            }
            if (vehicleSales == null) {
                try {
                    WebElement salesValueText = salesSvg.findElement(By.cssSelector("text.value"));
                    vehicleSales = salesValueText.getText().trim();
                } catch (Exception e) {
                    vehicleSales = null;
                }
            }

            // Wait for the label to appear
            WebElement label = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(text(), 'No of Vehicles Invoiced Yesterday')]")
            ));

            // Go up to the container (adjust the ancestor as needed, e.g., div[@class='textbox'] or visual-modern)
            WebElement container = label.findElement(By.xpath("./ancestor::*[contains(@class, 'visual')]"));

            // Now find the <text class="value"> inside this container
            WebElement valueText = container.findElement(By.cssSelector("text.value"));
            String invoicedYesterday = valueText.getText().trim();

            System.out.println("Vehicle Sold: " + vehicleQty);
            System.out.println("Vehicle Sales: " + vehicleSales);
            System.out.println("No of Vehicles Invoiced Yesterday: " + invoicedYesterday);

        } finally {
            driver.quit();
        }
    }
} 