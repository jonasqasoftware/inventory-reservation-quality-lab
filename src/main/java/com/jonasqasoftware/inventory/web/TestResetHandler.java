package com.jonasqasoftware.inventory.web;

import com.jonasqasoftware.inventory.InventoryService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

/** Disponível somente em testMode; reseta o estado para a seed conhecida. */
public class TestResetHandler implements HttpHandler {

  private final InventoryService inventoryService;

  public TestResetHandler(InventoryService inventoryService) {
    this.inventoryService = inventoryService;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
      HttpSupport.sendJson(exchange, 405, "{\"error\":\"method not allowed\"}");
      return;
    }
    inventoryService.reset();
    HttpSupport.sendJson(exchange, 200, "{\"status\":\"reset\"}");
  }
}
