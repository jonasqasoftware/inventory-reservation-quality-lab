package com.jonasqasoftware.inventory.web;

public final class HtmlSupport {

  private HtmlSupport() {}

  public static String escape(String value) {
    if (value == null) {
      return "";
    }
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }

  public static String navigation(String active) {
    return """
        <nav aria-label="Navegação principal">
          <ul>
            <li><a href="/inventory"%s>Estoque</a></li>
            <li><a href="/reservations"%s>Reservas</a></li>
          </ul>
        </nav>
        """
        .formatted(
            "inventory".equals(active) ? " aria-current=\"page\"" : "",
            "reservations".equals(active) ? " aria-current=\"page\"" : "");
  }

  public static String page(String title, String active, String pageTestId, String bodyContent) {
    return """
        <!doctype html>
        <html lang="pt-BR">
          <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>%s</title>
            <link rel="stylesheet" href="/styles.css">
          </head>
          <body>
            <header>
              <h1>Inventory Reservation Quality Lab</h1>
              %s
            </header>
            <main data-testid="%s">
        %s
            </main>
          </body>
        </html>
        """
        .formatted(escape(title), navigation(active), pageTestId, bodyContent);
  }
}
