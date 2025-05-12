package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;

public class HomePage {
    private WebDriver driver;
    private WebDriverWait wait;

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(60));
    }

    public void login(String url, String username, String password) {
        driver.get(url);
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }

    
    public void switchToPowerBIFrame() {
        WebElement iframe = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("iframe[src*='app.powerbi.com/reportEmbed']")
        ));
        driver.switchTo().frame(iframe);
    }

    public void clickTile(int index) {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("svg g.tile")));
        List<WebElement> tiles = driver.findElements(By.cssSelector("svg g.tile"));
        if (tiles.size() > index) {
            tiles.get(index).click();
        } else {
            throw new RuntimeException("Not enough tiles found!");
        }
    }
} 