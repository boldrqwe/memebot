package ru.boldr.memebot.configuration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.annotation.PreDestroy;

@Configuration
public class SeleniumConfig {
    private WebDriver driver;
    @Bean
    @Scope("singleton")  // Указывает, что бин должен быть создан как singleton
    public WebDriver webDriver() {
        System.setProperty("webdriver.chrome.driver", "G:/chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        driver.manage().window();  // Максимизируем окно
        return driver;
    }

    @PreDestroy
    public void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }
}
