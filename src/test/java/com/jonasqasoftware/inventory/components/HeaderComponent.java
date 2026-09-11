package com.jonasqasoftware.inventory.components;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Navegação compartilhada entre as páginas Estoque e Reservas. */
public class HeaderComponent {

  private final WebDriverWait wait;

  public HeaderComponent(WebDriverWait wait) {
    this.wait = wait;
  }

  public void goToInventory() {
    wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("nav a[href='/inventory']"))).click();
    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='inventory-page']")));
  }

  public void goToReservations() {
    wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("nav a[href='/reservations']"))).click();
    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='reservations-page']")));
  }
}
