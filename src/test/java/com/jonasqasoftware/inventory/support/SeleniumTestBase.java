package com.jonasqasoftware.inventory.support;

import com.jonasqasoftware.inventory.InventoryApplication;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;

public abstract class SeleniumTestBase {

  private static InventoryApplication app;
  private static TestStateClient stateClient;
  protected static String baseUrl;

  protected WebDriver driver;

  @BeforeAll
  static void startApplication() throws IOException {
    app = new InventoryApplication();
    app.start(0, true);
    baseUrl = "http://127.0.0.1:" + app.port();
    stateClient = new TestStateClient(baseUrl);
  }

  @AfterAll
  static void stopApplication() {
    app.stop();
  }

  @BeforeEach
  void resetStateAndOpenBrowser() {
    stateClient.reset();
    boolean headless = Boolean.parseBoolean(System.getProperty("headless", "true"));
    driver = BrowserFactory.createChromeDriver(headless);
  }

  @AfterEach
  void closeBrowser() {
    if (driver != null) {
      driver.quit();
    }
  }
}
