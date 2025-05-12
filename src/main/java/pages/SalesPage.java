package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.util.List;

public class SalesPage {
    private WebDriver driver;

    public SalesPage(WebDriver driver) {
        this.driver = driver;
    }

    public String getValueByIndex(int index) {
        List<WebElement> values = driver.findElements(By.xpath("//*[@class='value']"));
        return (values.size() >= index) ? values.get(index - 1).getText().trim() : "Not found";
    }
} 