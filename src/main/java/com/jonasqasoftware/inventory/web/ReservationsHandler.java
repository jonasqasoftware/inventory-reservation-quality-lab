package com.jonasqasoftware.inventory.web;

import com.jonasqasoftware.inventory.InsufficientStockException;
import com.jonasqasoftware.inventory.InventoryService;
import com.jonasqasoftware.inventory.Reservation;
import com.jonasqasoftware.inventory.ReservationStatus;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReservationsHandler implements HttpHandler {

  private static final Pattern CANCEL_PATH = Pattern.compile("^/reservations/(\\d+)/cancel$");

  private final InventoryService inventoryService;

  public ReservationsHandler(InventoryService inventoryService) {
    this.inventoryService = inventoryService;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String method = exchange.getRequestMethod();
    String path = exchange.getRequestURI().getPath();
    Matcher cancelMatch = CANCEL_PATH.matcher(path);

    if ("GET".equalsIgnoreCase(method) && "/reservations".equals(path)) {
      renderList(exchange);
    } else if ("POST".equalsIgnoreCase(method) && "/reservations".equals(path)) {
      createReservation(exchange);
    } else if ("POST".equalsIgnoreCase(method) && cancelMatch.matches()) {
      cancelReservation(exchange, Long.parseLong(cancelMatch.group(1)));
    } else {
      HttpSupport.sendJson(exchange, 404, "{\"error\":\"not found\"}");
    }
  }

  private void createReservation(HttpExchange exchange) throws IOException {
    Map<String, String> form = HttpSupport.readFormBody(exchange);
    String sku = form.getOrDefault("sku", "");
    try {
      int quantity = Integer.parseInt(form.getOrDefault("quantity", "0"));
      inventoryService.reserve(sku, quantity);
      HttpSupport.redirect(exchange, "/inventory?feedback=reserved&sku=" + sku);
    } catch (InsufficientStockException | IllegalArgumentException e) {
      HttpSupport.redirect(exchange, "/inventory?feedback=insufficient&sku=" + sku);
    }
  }

  private void cancelReservation(HttpExchange exchange, long id) throws IOException {
    try {
      inventoryService.cancel(id);
      HttpSupport.redirect(exchange, "/reservations?feedback=cancelled");
    } catch (RuntimeException e) {
      HttpSupport.redirect(exchange, "/reservations");
    }
  }

  private void renderList(HttpExchange exchange) throws IOException {
    Map<String, String> query = HttpSupport.parseQuery(exchange.getRequestURI().getRawQuery());
    String feedback = feedbackMessage(query.get("feedback"));

    List<Reservation> reservations = inventoryService.listReservations();
    StringBuilder rows = new StringBuilder();
    for (Reservation reservation : reservations) {
      rows.append(renderRow(reservation));
    }

    String body =
        """
            <h2>Reservas</h2>
        %s
            <table>
              <thead>
                <tr>
                  <th scope="col">SKU</th>
                  <th scope="col">Quantidade</th>
                  <th scope="col">Status</th>
                  <th scope="col">Ação</th>
                </tr>
              </thead>
              <tbody>
        %s
              </tbody>
            </table>
        """
            .formatted(feedback, rows);

    HttpSupport.sendHtml(exchange, 200, HtmlSupport.page("Reservas", "reservations", "reservations-page", body));
  }

  private String renderRow(Reservation reservation) {
    String sku = HtmlSupport.escape(reservation.sku());
    boolean active = reservation.status() == ReservationStatus.ACTIVE;
    String statusLabel = active ? "Ativa" : "Cancelada";
    String cancelAction =
        active
            ? """
                  <form method="post" action="/reservations/%d/cancel">
                    <button type="submit" data-testid="cancel-reservation-%d">Cancelar</button>
                  </form>
              """
                .formatted(reservation.id(), reservation.id())
            : "";

    return """
              <tr data-testid="reservation-row-%s">
                <td>%s</td>
                <td data-testid="reservation-quantity-%s">%d</td>
                <td data-testid="reservation-status-%s">%s</td>
                <td>%s</td>
              </tr>
        """
        .formatted(sku, sku, sku, reservation.quantity(), sku, statusLabel, cancelAction);
  }

  private String feedbackMessage(String feedback) {
    if (!"cancelled".equals(feedback)) {
      return "";
    }
    return """
            <p role="alert" data-testid="feedback">Reserva cancelada.</p>
        """;
  }
}
