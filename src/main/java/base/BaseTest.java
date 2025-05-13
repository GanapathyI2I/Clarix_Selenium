package base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.*;
import pages.HomePage;
import pages.SalesPage;
import utils.ConfigUtil;

/**
 * Base test class with static WebDriver instance to share across all tests.
 */
public class BaseTest {
    protected static WebDriver driver;
    protected static HomePage homePage;
    protected static SalesPage salesPage;
    protected static boolean isInitialized = false;
    
    /**
     * Setup method that will run only once at the beginning of the test suite.
     * It initializes the WebDriver and logs into the application.
     */
    @BeforeSuite
    public void setUp() {
        if (!isInitialized) {
            System.out.println("Initializing WebDriver and logging in...");
            System.setProperty("webdriver.chrome.driver", ConfigUtil.get("chrome.driver.path"));
            driver = new ChromeDriver();
            homePage = new HomePage(driver);
            salesPage = new SalesPage(driver);

            homePage.login(ConfigUtil.get("url"), ConfigUtil.get("username"), ConfigUtil.get("password"));
            homePage.switchToPowerBIFrame();
            homePage.clickTile(6); // Adjust index as needed
            
            isInitialized = true;
        }
    }
    
    /**
     * Teardown method that will run only once at the end of the test suite.
     * It closes the browser and logs out of the application.
     */
    @AfterSuite
    public void tearDown() {
        if (driver != null) {
            System.out.println("Closing WebDriver and logging out...");
            driver.quit();
            driver = null;
            isInitialized = false;
        }
    }
} 