package com.jonasqasoftware.inventory.support;

import java.time.Duration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Cria instâncias de ChromeDriver usando o Selenium Manager (integrado ao
 * Selenium 4) para resolver o driver — sem WebDriverManager, sem download
 * manual de binário.
 */
public final class BrowserFactory {

  private BrowserFactory() {}

  public static WebDriver createChromeDriver(boolean headless) {
    ChromeOptions options = new ChromeOptions();
    if (headless) {
      options.addArguments("--headless=new");
      options.addArguments("--window-size=1440,1000");
    }

    WebDriver driver = new ChromeDriver(options);
    driver.manage().timeouts().implicitlyWait(Duration.ZERO);
    return driver;
  }
}
