package com.jonasqasoftware.inventory.components;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Representa uma linha da tabela de reservas, localizada pelo SKU do
 * produto reservado.
 */
public class ReservationRowComponent {

  private final WebDriver driver;
  private final WebDriverWait wait;
  private final String sku;

  public ReservationRowComponent(WebDriver driver, WebDriverWait wait, String sku) {
    this.driver = driver;
    this.wait = wait;
    this.sku = sku;
  }

  public String sku() {
    return sku;
  }

  public int quantity() {
    return Integer.parseInt(textOf(quantitySelector()).trim());
  }

  public String status() {
    return textOf(statusSelector()).trim();
  }

  /**
   * Localiza e lê o texto do elemento dentro da mesma iteração de polling do
   * wait: se o elemento ficar stale entre a localização e a leitura (por
   * exemplo, logo após uma navegação), o wait simplesmente tenta de novo em
   * vez de propagar StaleElementReferenceException.
   */
  private String textOf(String cssSelector) {
    return wait.until(
        currentDriver -> {
          try {
            WebElement element = currentDriver.findElement(By.cssSelector(cssSelector));
            return element.isDisplayed() ? element.getText() : null;
          } catch (StaleElementReferenceException e) {
            return null;
          }
        });
  }

  public boolean isCancelAvailable() {
    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(rowSelector())));
    return !driver.findElements(By.cssSelector(cancelButtonSelector())).isEmpty();
  }

  public void cancel() {
    // Mesmo raciocínio de InventoryRowComponent.reserve(): cancelar já
    // acontece a partir de /reservations, então aguardar apenas a presença
    // desse marcador depois do clique seria self-satisfying (a condição já
    // era verdadeira antes da navegação). Esperar a página antiga ficar
    // stale detecta de forma determinística o POST → 303 → reload.
    var oldPage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='reservations-page']")));

    var cancelButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(cancelButtonSelector())));
    cancelButton.click();

    wait.until(ExpectedConditions.stalenessOf(oldPage));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='reservations-page']")));
  }

  private String rowSelector() {
    return "tr[data-testid='reservation-row-" + sku + "']";
  }

  private String quantitySelector() {
    return rowSelector() + " [data-testid='reservation-quantity-" + sku + "']";
  }

  private String statusSelector() {
    return rowSelector() + " [data-testid='reservation-status-" + sku + "']";
  }

  private String cancelButtonSelector() {
    return rowSelector() + " [data-testid^='cancel-reservation-']";
  }
}
