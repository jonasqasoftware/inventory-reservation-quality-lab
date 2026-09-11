package com.jonasqasoftware.inventory.web;

import com.jonasqasoftware.inventory.InventoryService;
import com.jonasqasoftware.inventory.ProductView;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class InventoryPageHandler implements HttpHandler {

  private final InventoryService inventoryService;

  public InventoryPageHandler(InventoryService inventoryService) {
    this.inventoryService = inventoryService;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
      HttpSupport.sendJson(exchange, 405, "{\"error\":\"method not allowed\"}");
      return;
    }

    Map<String, String> query = HttpSupport.parseQuery(exchange.getRequestURI().getRawQuery());
    String search = query.getOrDefault("search", "").trim();
    String feedback = feedbackMessage(query.get("feedback"));

    List<ProductView> products = inventoryService.listProducts().stream()
        .filter(product -> search.isBlank() || product.sku().toLowerCase().contains(search.toLowerCase()))
        .toList();

    StringBuilder rows = new StringBuilder();
    for (ProductView product : products) {
      rows.append(renderRow(product));
    }

    String body =
        """
            <h2>Estoque</h2>
        %s
            <form method="get" action="/inventory">
              <label for="inventory-search">Buscar por SKU</label>
              <input type="text" id="inventory-search" data-testid="inventory-search" name="search" value="%s">
              <button type="submit" data-testid="inventory-search-submit">Buscar</button>
            </form>
            <table>
              <thead>
                <tr>
                  <th scope="col">SKU</th>
                  <th scope="col">Produto</th>
                  <th scope="col">Disponível</th>
                  <th scope="col">Quantidade</th>
                  <th scope="col">Ação</th>
                </tr>
              </thead>
              <tbody>
        %s
              </tbody>
            </table>
        """
            .formatted(feedback, HtmlSupport.escape(search), rows);

    HttpSupport.sendHtml(exchange, 200, HtmlSupport.page("Estoque", "inventory", "inventory-page", body));
  }

  private String renderRow(ProductView product) {
    String sku = HtmlSupport.escape(product.sku());
    return """
              <tr data-testid="inventory-row-%s">
                <td>%s</td>
                <td>%s</td>
                <td data-testid="available-%s">%d</td>
                <td>
                  <form method="post" action="/reservations">
                    <label for="quantity-%s">Quantidade para %s</label>
                    <input type="number" min="1" id="quantity-%s" data-testid="quantity-%s" name="quantity" value="1">
                    <input type="hidden" name="sku" value="%s">
                    <button type="submit" data-testid="reserve-%s">Reservar</button>
                  </form>
                </td>
              </tr>
        """
        .formatted(
            sku,
            sku,
            HtmlSupport.escape(product.name()),
            sku,
            product.availableStock(),
            sku,
            sku,
            sku,
            sku,
            sku,
            sku);
  }

  private String feedbackMessage(String feedback) {
    if (feedback == null) {
      return "";
    }
    String message =
        switch (feedback) {
          case "reserved" -> "Reserva criada com sucesso.";
          case "insufficient" -> "Estoque insuficiente.";
          default -> null;
        };
    if (message == null) {
      return "";
    }
    return """
            <p role="alert" data-testid="feedback">%s</p>
        """
        .formatted(HtmlSupport.escape(message));
  }
}
