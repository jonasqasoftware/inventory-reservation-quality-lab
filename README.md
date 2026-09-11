# Inventory Reservation Quality Lab

**Selenium · Java · JUnit 5 · Quality Engineering**

Um laboratório de Quality Engineering com Selenium 4 e Java, focado em
automação E2E orientada a risco para fluxos de reserva e cancelamento de
estoque.

## O que é

Um pequeno sistema onde um operador consulta produtos, reserva uma
quantidade, consulta reservas e pode cancelar uma reserva ativa — contra um
backend real em Java puro (`com.sun.net.httpserver.HttpServer`), sem
persistência entre reinícios.

## O problema de qualidade

Reserva de estoque é uma operação onde erros silenciosos custam caro: o
produto errado sendo reservado, uma reserva aceita acima do estoque
disponível, ou um cancelamento que não restaura o estoque corretamente. Este
laboratório usa Selenium para provar esses comportamentos através da
interface real, não apenas via API.

## O que este projeto demonstra

- Selenium 4 com WebDriver real (Chrome);
- Java 21 e JUnit 5;
- Page Object Model e Page Component Objects usados de forma real, não
  ornamental;
- `WebDriverWait` + `ExpectedConditions` para sincronização explícita;
- Selenium Manager para resolução de driver, sem WebDriverManager;
- Risk-Based Testing;
- CI/CD com GitHub Actions.

## Modelo de risco

| Risco                                                          | Prioridade | Cenário |
| ------------------------------------------------------------------ | ---------- | ------- |
| Produto ou quantidade errados serem reservados                     | P0         | S1      |
| Reserva acima do estoque disponível ser aceita                     | P0         | S2      |
| Cancelamento não restaurar corretamente o estoque                  | P0         | S3      |
| Seleção do produto errado em uma tabela com múltiplos produtos     | P1         | S4      |
| Alteração de estoque não persistir após reload/navegação           | P1         | S5      |
| Reserva cancelada continuar oferecendo ação de cancelamento        | P2         | S6      |

## Cenários

| ID  | Prioridade | Cenário                                                       |
| --- | ---------- | ---------------------------------------------------------------- |
| S1  | P0         | Reserva válida reduz o estoque e cria uma reserva ativa           |
| S2  | P0         | Reserva acima do estoque disponível é bloqueada                  |
| S3  | P0         | Cancelamento de uma reserva ativa restaura o estoque              |
| S4  | P1         | Busca por SKU seleciona o produto correto para reserva            |
| S5  | P1         | Estoque atualizado permanece após reload e navegação              |
| S6  | P2         | Reserva cancelada permanece no histórico sem opção de cancelar    |

Detalhes completos em [`docs/TEST_STRATEGY.md`](docs/TEST_STRATEGY.md).

## Selenium na prática

- **Page Objects** (`InventoryPage`, `ReservationsPage`) representam as duas
  páginas reais da aplicação; não contêm assertions.
- **Page Components** (`InventoryRowComponent`, `ReservationRowComponent`,
  `HeaderComponent`) encapsulam linhas de tabela repetidas, sempre
  localizadas pelo SKU — nunca por índice de linha.
- Sem `BasePage`: as duas páginas não compartilham comportamento suficiente
  para justificar herança.
- Sincronização 100% explícita via `WebDriverWait`/`ExpectedConditions`;
  implicit wait é `Duration.ZERO`.
- Selenium Manager resolve o ChromeDriver compatível com o Chrome instalado
  automaticamente.

## Como executar

Requer Java 21 e Maven.

```bash
mvn -B -ntp test -Dheadless=true
```

Para ver o browser durante a execução local:

```bash
mvn -B -ntp test -Dheadless=false
```

## Arquitetura

```
inventory-reservation-quality-lab/
├── .github/workflows/tests.yml
├── docs/TEST_STRATEGY.md
├── src/
│   ├── main/java/com/jonasqasoftware/inventory/
│   │   ├── InventoryApplication.java
│   │   ├── InventoryService.java
│   │   ├── Reservation.java, ProductView.java, ReservationStatus.java
│   │   └── web/...
│   └── test/java/com/jonasqasoftware/inventory/
│       ├── e2e/InventoryReservationE2ETest.java
│       ├── pages/
│       ├── components/
│       └── support/
├── pom.xml
└── README.md
```

O backend é deliberadamente pequeno: `InventoryService` concentra as regras
de negócio (thread-safe via métodos sincronizados); o servidor HTTP é Java
puro, sem framework web.

## Evidência de qualidade

- Regra de estoque insuficiente validada no servidor, nunca só no cliente.
- Estado determinístico e isolado por teste via `POST /__test/reset`,
  disponível apenas em modo de teste.
- Seleção sempre por identificador de domínio (SKU), nunca por índice de
  linha.
- Varredura anti-flakiness confirmando ausência de `Thread.sleep`, XPath,
  `PageFactory`, implicit wait positivo e seleção por índice.

## Limitações

- Estado em memória do processo: sem persistência entre reinícios.
- Sem autenticação, sem multi-usuário real, sem concorrência distribuída.
- Escopo deliberadamente pequeno: três produtos sintéticos, um único fluxo
  de reserva/cancelamento.

## Stack

Java 21, Selenium 4.49.0, JUnit Jupiter 5.14.4, Maven, HTML/CSS
server-rendered.

## Licença

MIT — veja [LICENSE](LICENSE).
