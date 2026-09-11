package com.jonasqasoftware.inventory.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class RedirectHandler implements HttpHandler {

  private final String location;

  public RedirectHandler(String location) {
    this.location = location;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    HttpSupport.redirect(exchange, location);
  }
}
