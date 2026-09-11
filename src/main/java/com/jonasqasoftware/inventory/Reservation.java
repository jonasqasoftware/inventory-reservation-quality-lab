package com.jonasqasoftware.inventory;

public record Reservation(long id, String sku, int quantity, ReservationStatus status) {}
