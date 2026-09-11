package com.jonasqasoftware.inventory.support;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Cliente HTTP para resetar o estado do SUT via a fronteira do sistema
 * (POST /__test/reset), em vez de chamar métodos internos do service
 * diretamente a partir do teste E2E.
 */
public class TestStateClient {

  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final String baseUrl;

  public TestStateClient(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public void reset() {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create(baseUrl + "/__test/reset"))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();
    try {
      HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
      if (response.statusCode() != 200) {
        throw new IllegalStateException("Reset do estado de teste falhou: HTTP " + response.statusCode());
      }
    } catch (IOException | InterruptedException e) {
      throw new IllegalStateException("Não foi possível resetar o estado de teste.", e);
    }
  }
}
