package com.jonasqasoftware.inventory.components;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Representa uma linha da tabela de estoque, localizada pelo SKU do
 * produto — nunca por índice de linha. Não faz cache do WebElement entre
 * chamadas: reservar dispara uma navegação (redirect do servidor), então
 * cada método relocaliza o elemento na página atual.
 */
public class InventoryRowComponent {

  private final WebDriverWait wait;
  private final String sku;

  public InventoryRowComponent(WebDriverWait wait, String sku) {
    this.wait = wait;
    this.sku = sku;
  }

  public String sku() {
    return sku;
  }

  public int availableStock() {
    return Integer.parseInt(textOf(availableStockSelector()).trim());
  }

  /**
   * Localiza e lê o texto do elemento dentro da mesma iteração de polling do
   * wait: se o elemento ficar stale entre a localização e a leitura (por
   * exemplo, logo após uma navegação), o wait simplesmente tenta de novo em
   * vez de propagar StaleElementReferenceException.
   */
  private String textOf(String cssSelector) {
    return wait.until(
        driver -> {
          try {
            WebElement element = driver.findElement(By.cssSelector(cssSelector));
            return element.isDisplayed() ? element.getText() : null;
          } catch (StaleElementReferenceException e) {
            return null;
          }
        });
  }

  public void reserve(int quantity) {
    var quantityInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(quantityInputSelector())));
    quantityInput.clear();
    quantityInput.sendKeys(String.valueOf(quantity));

    // Captura o root da página atual antes do clique: como reservar já
    // acontece a partir de /inventory, aguardar apenas a presença desse
    // mesmo marcador depois seria uma condição self-satisfying (ela já é
    // verdadeira antes da navegação). Esperar a página antiga ficar stale
    // detecta de forma determinística que o POST → 303 → reload aconteceu.
    var oldPage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='inventory-page']")));

    var reserveButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(reserveButtonSelector())));
    reserveButton.click();

    wait.until(ExpectedConditions.stalenessOf(oldPage));
    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='inventory-page']")));
  }

  private String rowSelector() {
    return "tr[data-testid='inventory-row-" + sku + "']";
  }

  private String availableStockSelector() {
    return rowSelector() + " [data-testid='available-" + sku + "']";
  }

  private String quantityInputSelector() {
    return rowSelector() + " [data-testid='quantity-" + sku + "']";
  }

  private String reserveButtonSelector() {
    return rowSelector() + " [data-testid='reserve-" + sku + "']";
  }
}
