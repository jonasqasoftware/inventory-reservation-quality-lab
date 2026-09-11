package com.jonasqasoftware.inventory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Autoridade das regras de negócio de estoque e reserva. Estado mantido em
 * memória do processo; métodos sincronizados porque o HttpServer pode
 * atender requisições concorrentes.
 */
public class InventoryService {

  private record ProductSeed(String sku, String name, int initialStock) {}

  private static final List<ProductSeed> SEED =
      List.of(
          new ProductSeed("LAPTOP-14", "Notebook 14", 5),
          new ProductSeed("MONITOR-27", "Monitor 27", 3),
          new ProductSeed("HEADSET-USB", "Headset USB", 8));

  private final Map<String, String> productNames = new LinkedHashMap<>();
  private final Map<String, Integer> stock = new LinkedHashMap<>();
  private final Map<Long, Reservation> reservations = new LinkedHashMap<>();
  private final AtomicLong nextReservationId = new AtomicLong(1);

  public InventoryService() {
    reset();
  }

  public synchronized void reset() {
    productNames.clear();
    stock.clear();
    reservations.clear();
    nextReservationId.set(1);
    for (ProductSeed seed : SEED) {
      productNames.put(seed.sku(), seed.name());
      stock.put(seed.sku(), seed.initialStock());
    }
  }

  public synchronized List<ProductView> listProducts() {
    List<ProductView> products = new ArrayList<>();
    for (String sku : productNames.keySet()) {
      products.add(new ProductView(sku, productNames.get(sku), stock.get(sku)));
    }
    return products;
  }

  public synchronized Reservation reserve(String sku, int quantity) {
    if (!productNames.containsKey(sku)) {
      throw new NoSuchElementException("Produto não encontrado: " + sku);
    }
    if (quantity < 1) {
      throw new IllegalArgumentException("Quantidade deve ser pelo menos 1.");
    }

    int available = stock.get(sku);
    if (quantity > available) {
      throw new InsufficientStockException(
          "Estoque insuficiente para " + sku + ". Disponível: " + available + ".");
    }

    stock.put(sku, available - quantity);
    long id = nextReservationId.getAndIncrement();
    Reservation reservation = new Reservation(id, sku, quantity, ReservationStatus.ACTIVE);
    reservations.put(id, reservation);
    return reservation;
  }

  public synchronized List<Reservation> listReservations() {
    return new ArrayList<>(reservations.values());
  }

  public synchronized Reservation cancel(long id) {
    Reservation reservation = reservations.get(id);
    if (reservation == null) {
      throw new NoSuchElementException("Reserva não encontrada: " + id);
    }
    if (reservation.status() != ReservationStatus.ACTIVE) {
      throw new IllegalStateException("Reserva já está cancelada.");
    }

    stock.merge(reservation.sku(), reservation.quantity(), Integer::sum);
    Reservation cancelled =
        new Reservation(reservation.id(), reservation.sku(), reservation.quantity(), ReservationStatus.CANCELLED);
    reservations.put(id, cancelled);
    return cancelled;
  }
}
