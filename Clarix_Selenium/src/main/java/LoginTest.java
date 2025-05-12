import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class LoginTest {
    private static final String DRIVER_PATH = "C:\\Users\\ganapathy.r\\Chrome\\chromedriver-win64\\chromedriver.exe";
    private static final String URL = "https://sks.clarix.in/home";
    private static final String USERNAME = "user";
    private static final String PASSWORD = "user";

    // Add your DB connection details
    private static final String DB_URL = "jdbc:postgresql://43.205.155.214:5432/analytics";
    private static final String DB_USER = "scheduler";
    private static final String DB_PASS = "I$eame$5";

    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", DRIVER_PATH);
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));

        try {
            driver.get(URL);
            driver.findElement(By.id("username")).sendKeys(USERNAME);
            driver.findElement(By.id("password")).sendKeys(PASSWORD);
            driver.findElement(By.cssSelector("button[type='submit']")).click();

            WebElement iframe = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("iframe[src*='app.powerbi.com/reportEmbed']")
            ));
            driver.switchTo().frame(iframe);

            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("svg g.tile")));
            List<WebElement> tiles = driver.findElements(By.cssSelector("svg g.tile"));
            if (tiles.size() > 6) {
                tiles.get(6).click();
            } else {
                System.err.println("Not enough tiles found!");
            }

            List<WebElement> values = driver.findElements(By.xpath("//*[@class='value']"));
            String vehicleSold = (values.size() >= 4) ? values.get(3).getText().trim() : "Not found";
            System.out.println("vehicleSold (actual): " + vehicleSold);

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 Statement stmt = conn.createStatement()) {

                String sql = "SELECT ("
                    + " COALESCE(("
                    + "   SELECT SUM(qty) FROM commondatamodel.vehicles_sales "
                    + "   WHERE invoice_date BETWEEN '2025-05-01 00:00:00' AND '2025-05-06 23:59:59' AND qty IN (1)"
                    + " ), 0)"
                    + " + "
                    + " COALESCE(("
                    + "   SELECT SUM(qty) FROM commondatamodel.vehicles_sales "
                    + "   WHERE invoice_cancellation_date BETWEEN '2025-05-01 00:00:00' AND '2025-05-06 23:59:59' AND qty IN (-1, 0)"
                    + " ), 0)"
                    + ") AS net_qty;";
                ResultSet rs = stmt.executeQuery(sql);

                if (rs.next()) {
                    String expectedVehicleSold = rs.getString("net_qty");
                    assertValue("vehicleSold", vehicleSold, expectedVehicleSold);
                } else {
                    System.out.println("No expected data found in DB for the given condition.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    private static void assertValue(String field, String actual, String expected) {
        if (actual != null && actual.equals(expected)) {
            System.out.println("PASS: " + field + " matches expected value: " + actual);
        } else {
            System.out.println("FAIL: " + field + " actual: " + actual + ", expected: " + expected);
        }
    }
} 