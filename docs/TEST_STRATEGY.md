# Estratégia de Testes

## Contexto

O Inventory Reservation Quality Lab é um laboratório de portfólio que
demonstra Quality Engineering com Selenium 4 e Java: um operador consulta
produtos em estoque, reserva quantidades e pode cancelar reservas ativas,
contra um backend real (Java puro, `com.sun.net.httpserver.HttpServer`).

## Objetivo

Demonstrar uma arquitetura Selenium/Java defensável tecnicamente, com:

- WebDriver real, sem esconder o Selenium atrás de abstrações desnecessárias;
- separação Page Object / Page Component onde há repetição real de estrutura
  e comportamento;
- sincronização explícita via `WebDriverWait`/`ExpectedConditions`;
- resolução de driver via Selenium Manager, sem gerenciamento manual de
  binário;
- controle determinístico de estado entre testes.

O objetivo não é maximizar a quantidade de testes nem copiar a arquitetura
de outros laboratórios do portfólio (Playwright, Cypress).

## Modelo de risco

| Risco                                                                 | Impacto | Prioridade | Cenário | Sinal de decisão                                          |
| ---------------------------------------------------------------------- | ------- | ---------- | ------- | ----------------------------------------------------------- |
| R1. Produto ou quantidade errados serem reservados                    | Alto    | P0         | S1      | Cobrir o fluxo completo de reserva contra o backend real     |
| R2. Sistema permitir reserva acima do estoque disponível               | Alto    | P0         | S2      | Backend real deve rejeitar; regra não é stubada              |
| R3. Cancelamento não restaurar corretamente o estoque                  | Alto    | P0         | S3      | Validar estoque antes e depois do cancelamento                |
| R4. Seleção do produto errado em uma tabela com múltiplos produtos    | Médio   | P1         | S4      | Localizar e agir sempre pelo SKU, nunca por índice de linha   |
| R5. Alteração de estoque não persistir após reload/navegação          | Médio   | P1         | S5      | Validar estado após refresh e após navegação entre páginas    |
| R6. Reserva cancelada continuar oferecendo ação de cancelamento        | Baixo   | P2         | S6      | Validar histórico e ausência da ação após reload              |

## Por que Selenium

- **WebDriver real**: os testes controlam um browser real (Chrome), exercitando
  a aplicação exatamente como um usuário final.
- **Selenium Manager**: integrado ao Selenium 4, resolve o ChromeDriver
  compatível com o Chrome instalado sem download manual nem dependência de
  WebDriverManager.
- **Separação Page Object / Page Component**: há duas páginas reais
  (`/inventory`, `/reservations`) e estruturas de linha repetidas com
  comportamento próprio — a abstração reduz duplicação sem esconder
  expectativas dos testes.
- **Sincronização explícita**: Selenium não possui a mesma retryability
  automática do Cypress nem o mesmo auto-waiting do Playwright. Por isso a
  sincronização aqui é deliberadamente explícita, via `WebDriverWait`
  orientado a estados observáveis (elemento visível, clicável, presente).
- **Maturidade Java/JUnit**: Selenium + JUnit 5 é uma combinação amplamente
  usada na indústria para automação E2E em contextos Java.

## Cenários

| ID  | Prioridade | Método                                       | Real/Stub |
| --- | ---------- | ---------------------------------------------- | --------- |
| S1  | P0         | `reservesValidQuantity`                        | Real      |
| S2  | P0         | `blocksReservationAboveAvailableStock`         | Real      |
| S3  | P0         | `cancellingReservationRestoresStock`           | Real      |
| S4  | P1         | `searchBySkuSelectsCorrectProduct`             | Real      |
| S5  | P1         | `stockUpdateSurvivesReloadAndNavigation`       | Real      |
| S6  | P2         | `cancelledReservationStaysInHistoryWithoutCancelAction` | Real |

Todos os cenários exercitam o backend real: não há stub de rede neste
laboratório (diferente do Cypress Lab, onde um cenário de resiliência de
rede é o próprio objeto de estudo). Aqui o foco é a mecânica Selenium/Java.

## Arquitetura dos testes

```
src/test/java/.../
├── e2e/InventoryReservationE2ETest.java   (S1-S6, uma única classe)
├── pages/InventoryPage.java, ReservationsPage.java
├── components/HeaderComponent.java, InventoryRowComponent.java, ReservationRowComponent.java
└── support/SeleniumTestBase.java, BrowserFactory.java, TestStateClient.java
```

Uma única classe de testes concentra os 6 cenários: mantém o lifecycle do
servidor simples, a rastreabilidade clara e evita infraestrutura artificial
para um laboratório deste tamanho.

## Page Objects

- **InventoryPage** representa `/inventory`: navega, busca por SKU, expõe
  produtos (`product(sku)`) e a mensagem de feedback.
- **ReservationsPage** representa `/reservations`: navega, expõe reservas
  (`reservation(sku)`) e a mensagem de feedback.
- Nenhum Page Object contém assertions — elas pertencem exclusivamente aos
  testes.
- **Por que não BasePage?** Não existe comportamento comum suficiente entre
  as duas páginas (além de campos triviais como `driver`/`wait`) para
  justificar herança. Cada página mantém seu próprio `WebDriverWait`.

## Page Components

- **InventoryRowComponent**: representa uma linha da tabela de estoque,
  localizada pelo SKU. Expõe `availableStock()` e `reserve(quantity)`.
- **ReservationRowComponent**: representa uma linha da tabela de reservas,
  localizada pelo SKU. Expõe `quantity()`, `status()`,
  `isCancelAvailable()` e `cancel()`.
- **HeaderComponent**: navegação compartilhada entre Estoque e Reservas.
- Nenhum componente expõe o `WebDriver` bruto, locators ou `WebElement` aos
  testes — apenas comportamento e estado do domínio.

**Por que Page Components?** As tabelas de estoque e reservas têm linhas
repetidas que representam entidades do domínio (produto, reserva) com ações
e estado próprios. Um componente por linha evita duplicar a lógica de
localização/interação em cada teste.

## Sincronização

Todo `WebDriverWait` usa um timeout explícito (15s) e `ExpectedConditions`
orientadas ao estado esperado: elemento visível, clicável ou presente após
uma navegação. O implicit wait é explicitamente `Duration.ZERO`
(`BrowserFactory`) — não há espera implícita positiva escondendo problemas
de sincronização.

## Estado e isolamento

Cada teste começa com `TestStateClient.reset()`, que faz
`POST /__test/reset` contra o backend real, executado no `@BeforeEach` de
`SeleniumTestBase`. Essa rota só existe quando a aplicação é iniciada com
`testMode = true`. O reset acontece pela fronteira HTTP do sistema, não por
chamada direta a métodos internos do `InventoryService`.

Nenhum teste depende da execução ou da ordem de outro — confirmado
executando S1, S2 e S3 individualmente via filtro do Maven Surefire.

## Browser lifecycle

- `@BeforeAll`: inicia o `InventoryApplication` em `testMode` em uma porta
  dinâmica (`0`), captura a porta real.
- `@BeforeEach`: reseta o estado via HTTP e cria uma nova instância de
  `ChromeDriver` (`BrowserFactory`).
- `@AfterEach`: `driver.quit()`.
- `@AfterAll`: `app.stop()`.

Um `WebDriver` novo por teste evita vazamento de estado de sessão do browser
entre cenários.

## CI

`.github/workflows/tests.yml` roda em `pull_request` e `push` para `main`,
usando `actions/setup-java` com Temurin 21 e cache Maven. Etapa única:
`mvn -B -ntp test -Dheadless=true`. O runner `ubuntu-latest` já possui
Chrome instalado; o Selenium Manager resolve o ChromeDriver compatível sem
nenhuma instalação adicional.

## Anti-flakiness

- `Thread.sleep`: 0
- XPath: 0
- Implicit wait positivo: 0 (`Duration.ZERO` explícito)
- `PageFactory`/`@FindBy`: 0 (locators explícitos com `By.cssSelector`)
- `findElements(...).get(0)`: 0 — seleção sempre pelo SKU (produtos) ou pelo
  SKU dentro da linha (reservas), nunca por índice
- Retries automáticos: nenhum, nem em JUnit nem no CI

## Riscos residuais

- Estado em memória do processo: sem persistência entre reinícios do SUT.
- Sem autenticação, sem múltiplos usuários simultâneos reais.
- Uma única classe de testes concentra os 6 cenários; se o laboratório
  crescer significativamente, pode justificar dividir por fluxo.
- Resolução de driver depende de conectividade de rede na primeira execução
  em uma máquina nova (Selenium Manager); em ambientes com rede muito lenta
  ou instável, a primeira resolução pode ser lenta até que o driver seja
  armazenado em cache local.
