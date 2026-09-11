package com.jonasqasoftware.inventory.pages;

import com.jonasqasoftware.inventory.components.HeaderComponent;
import com.jonasqasoftware.inventory.components.InventoryRowComponent;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Page Object para /inventory. Não contém assertions. */
public class InventoryPage {

  private final WebDriver driver;
  private final WebDriverWait wait;
  private final String baseUrl;

  public InventoryPage(WebDriver driver, String baseUrl) {
    this.driver = driver;
    this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    this.baseUrl = baseUrl;
  }

  public InventoryPage open() {
    driver.get(baseUrl + "/inventory");
    waitForLoad();
    return this;
  }

  public void searchBySku(String term) {
    var searchInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-testid='inventory-search']")));
    searchInput.clear();
    searchInput.sendKeys(term);
    wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-testid='inventory-search-submit']"))).click();
    waitForLoad();
  }

  public InventoryRowComponent product(String sku) {
    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='inventory-row-" + sku + "']")));
    return new InventoryRowComponent(wait, sku);
  }

  public boolean isProductVisible(String sku) {
    return !driver.findElements(By.cssSelector("[data-testid='inventory-row-" + sku + "']")).isEmpty();
  }

  public String feedbackMessage() {
    var feedback = driver.findElements(By.cssSelector("[data-testid='feedback']"));
    return feedback.isEmpty() ? "" : feedback.get(0).getText().trim();
  }

  public HeaderComponent header() {
    return new HeaderComponent(wait);
  }

  public void refresh() {
    driver.navigate().refresh();
    waitForLoad();
  }

  private void waitForLoad() {
    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='inventory-page']")));
  }
}
