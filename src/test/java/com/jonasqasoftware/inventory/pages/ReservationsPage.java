package com.jonasqasoftware.inventory.pages;

import com.jonasqasoftware.inventory.components.HeaderComponent;
import com.jonasqasoftware.inventory.components.ReservationRowComponent;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Page Object para /reservations. Não contém assertions. */
public class ReservationsPage {

  private final WebDriver driver;
  private final WebDriverWait wait;
  private final String baseUrl;

  public ReservationsPage(WebDriver driver, String baseUrl) {
    this.driver = driver;
    this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    this.baseUrl = baseUrl;
  }

  public ReservationsPage open() {
    driver.get(baseUrl + "/reservations");
    waitForLoad();
    return this;
  }

  public ReservationRowComponent reservation(String sku) {
    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='reservation-row-" + sku + "']")));
    return new ReservationRowComponent(driver, wait, sku);
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
    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='reservations-page']")));
  }
}
