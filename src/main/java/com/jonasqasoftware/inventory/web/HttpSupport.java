package com.jonasqasoftware.inventory.web;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HttpSupport {

  private HttpSupport() {}

  public static void sendHtml(HttpExchange exchange, int statusCode, String html) throws IOException {
    byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
    exchange.sendResponseHeaders(statusCode, bytes.length);
    exchange.getResponseBody().write(bytes);
    exchange.close();
  }

  public static void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
    byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
    exchange.sendResponseHeaders(statusCode, bytes.length);
    exchange.getResponseBody().write(bytes);
    exchange.close();
  }

  public static void redirect(HttpExchange exchange, String location) throws IOException {
    exchange.getResponseHeaders().set("Location", location);
    exchange.sendResponseHeaders(303, -1);
    exchange.close();
  }

  public static Map<String, String> readFormBody(HttpExchange exchange) throws IOException {
    try (InputStream body = exchange.getRequestBody()) {
      String raw = new String(body.readAllBytes(), StandardCharsets.UTF_8);
      return parseFormEncoded(raw);
    }
  }

  public static Map<String, String> parseQuery(String query) {
    return parseFormEncoded(query);
  }

  private static Map<String, String> parseFormEncoded(String raw) {
    Map<String, String> values = new LinkedHashMap<>();
    if (raw == null || raw.isBlank()) {
      return values;
    }
    for (String pair : raw.split("&")) {
      int separator = pair.indexOf('=');
      if (separator == -1) {
        continue;
      }
      String key = URLDecoder.decode(pair.substring(0, separator), StandardCharsets.UTF_8);
      String value = URLDecoder.decode(pair.substring(separator + 1), StandardCharsets.UTF_8);
      values.put(key, value);
    }
    return values;
  }
}
