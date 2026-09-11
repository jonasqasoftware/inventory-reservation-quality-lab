package com.jonasqasoftware.inventory.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class HealthHandler implements HttpHandler {

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    HttpSupport.sendJson(exchange, 200, "{\"status\":\"ok\"}");
  }
}
