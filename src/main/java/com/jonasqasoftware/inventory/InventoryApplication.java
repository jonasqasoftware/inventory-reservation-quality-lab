package com.jonasqasoftware.inventory;

import com.jonasqasoftware.inventory.web.HealthHandler;
import com.jonasqasoftware.inventory.web.InventoryPageHandler;
import com.jonasqasoftware.inventory.web.RedirectHandler;
import com.jonasqasoftware.inventory.web.ReservationsHandler;
import com.jonasqasoftware.inventory.web.TestResetHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class InventoryApplication {

  private final InventoryService inventoryService = new InventoryService();
  private HttpServer server;

  public void start(int port, boolean testMode) throws IOException {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
    server.createContext("/", new RedirectHandler("/inventory"));
    server.createContext("/health", new HealthHandler());
    server.createContext("/inventory", new InventoryPageHandler(inventoryService));
    server.createContext("/reservations", new ReservationsHandler(inventoryService));
    server.createContext("/styles.css", this::serveStylesheet);

    if (testMode) {
      server.createContext("/__test/reset", new TestResetHandler(inventoryService));
    }

    server.setExecutor(Executors.newFixedThreadPool(4));
    server.start();
  }

  private void serveStylesheet(HttpExchange exchange) throws IOException {
    byte[] css;
    try (var stream = getClass().getResourceAsStream("/static/styles.css")) {
      css = stream == null ? new byte[0] : stream.readAllBytes();
    }
    exchange.getResponseHeaders().set("Content-Type", "text/css; charset=utf-8");
    exchange.sendResponseHeaders(200, css.length);
    exchange.getResponseBody().write(css);
    exchange.close();
  }

  public int port() {
    return server.getAddress().getPort();
  }

  public void stop() {
    if (server != null) {
      server.stop(0);
    }
  }

  public static void main(String[] args) throws IOException {
    InventoryApplication app = new InventoryApplication();
    app.start(3300, false);
    System.out.println(
        "Inventory Reservation Quality Lab ouvindo em http://127.0.0.1:" + app.port());
  }
}
