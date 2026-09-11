package com.jonasqasoftware.inventory.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jonasqasoftware.inventory.components.InventoryRowComponent;
import com.jonasqasoftware.inventory.components.ReservationRowComponent;
import com.jonasqasoftware.inventory.pages.InventoryPage;
import com.jonasqasoftware.inventory.pages.ReservationsPage;
import com.jonasqasoftware.inventory.support.SeleniumTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InventoryReservationE2ETest extends SeleniumTestBase {

  @Test
  @DisplayName("S1 — reserva válida reduz o estoque e cria uma reserva ativa")
  void reservesValidQuantity() {
    InventoryPage inventoryPage = new InventoryPage(driver, baseUrl).open();

    InventoryRowComponent monitor = inventoryPage.product("MONITOR-27");
    assertEquals(3, monitor.availableStock());

    monitor.reserve(2);

    assertEquals("Reserva criada com sucesso.", inventoryPage.feedbackMessage());
    assertEquals(1, inventoryPage.product("MONITOR-27").availableStock());

    ReservationsPage reservationsPage = new ReservationsPage(driver, baseUrl).open();
    ReservationRowComponent reservation = reservationsPage.reservation("MONITOR-27");
    assertEquals(2, reservation.quantity());
    assertEquals("Ativa", reservation.status());
  }

  @Test
  @DisplayName("S2 — bloqueia reserva acima do estoque disponível")
  void blocksReservationAboveAvailableStock() {
    InventoryPage inventoryPage = new InventoryPage(driver, baseUrl).open();

    InventoryRowComponent monitor = inventoryPage.product("MONITOR-27");
    monitor.reserve(4);

    assertEquals("Estoque insuficiente.", inventoryPage.feedbackMessage());
    assertEquals(3, inventoryPage.product("MONITOR-27").availableStock());
  }

  @Test
  @DisplayName("S3 — cancelamento de uma reserva ativa restaura o estoque")
  void cancellingReservationRestoresStock() {
    InventoryPage inventoryPage = new InventoryPage(driver, baseUrl).open();
    inventoryPage.product("MONITOR-27").reserve(2);
    assertEquals(1, inventoryPage.product("MONITOR-27").availableStock());

    ReservationsPage reservationsPage = new ReservationsPage(driver, baseUrl).open();
    reservationsPage.reservation("MONITOR-27").cancel();

    assertEquals("Reserva cancelada.", reservationsPage.feedbackMessage());
    assertEquals("Cancelada", reservationsPage.reservation("MONITOR-27").status());

    inventoryPage.open();
    assertEquals(3, inventoryPage.product("MONITOR-27").availableStock());
  }

  @Test
  @DisplayName("S4 — busca por SKU seleciona o produto correto para reserva")
  void searchBySkuSelectsCorrectProduct() {
    InventoryPage inventoryPage = new InventoryPage(driver, baseUrl).open();

    inventoryPage.searchBySku("HEADSET-USB");

    assertTrue(inventoryPage.isProductVisible("HEADSET-USB"));
    assertFalse(inventoryPage.isProductVisible("LAPTOP-14"));
    assertFalse(inventoryPage.isProductVisible("MONITOR-27"));

    InventoryRowComponent headset = inventoryPage.product("HEADSET-USB");
    assertEquals(8, headset.availableStock());
    headset.reserve(1);

    ReservationsPage reservationsPage = new ReservationsPage(driver, baseUrl).open();
    ReservationRowComponent reservation = reservationsPage.reservation("HEADSET-USB");
    assertEquals(1, reservation.quantity());
    assertEquals("Ativa", reservation.status());
  }

  @Test
  @DisplayName("S5 — estoque atualizado permanece após reload e navegação")
  void stockUpdateSurvivesReloadAndNavigation() {
    InventoryPage inventoryPage = new InventoryPage(driver, baseUrl).open();
    inventoryPage.product("LAPTOP-14").reserve(1);
    assertEquals(4, inventoryPage.product("LAPTOP-14").availableStock());

    inventoryPage.refresh();
    assertEquals(4, inventoryPage.product("LAPTOP-14").availableStock());

    inventoryPage.header().goToReservations();
    inventoryPage.header().goToInventory();
    assertEquals(4, inventoryPage.product("LAPTOP-14").availableStock());
  }

  @Test
  @DisplayName("S6 — reserva cancelada permanece no histórico sem opção de cancelar novamente")
  void cancelledReservationStaysInHistoryWithoutCancelAction() {
    InventoryPage inventoryPage = new InventoryPage(driver, baseUrl).open();
    inventoryPage.product("MONITOR-27").reserve(1);

    ReservationsPage reservationsPage = new ReservationsPage(driver, baseUrl).open();
    reservationsPage.reservation("MONITOR-27").cancel();

    reservationsPage.refresh();

    ReservationRowComponent reservation = reservationsPage.reservation("MONITOR-27");
    assertEquals("Cancelada", reservation.status());
    assertFalse(reservation.isCancelAvailable());
  }
}
