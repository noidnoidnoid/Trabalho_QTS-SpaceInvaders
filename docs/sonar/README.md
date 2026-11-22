# Inspeção de código com SonarQube (Entrega 2, item 6)

- **Ferramenta base:** SonarQube, perfil Sonar way comprehensive.
- **Complemento:** PMD (`quickstart`), usado pelo Roger em [`docs/inspecao/`](../inspecao/LEIA-ME.md).
- **Inspeção da `Ship` (Luiz):** [`ship.md`](ship.md).

## Por que essa escolha

Problemas por classe, no mesmo código (commit `bbb57f3`). Tabela com todas as classes: [`comparacao-pmd-sonar.md`](before/comparacao-pmd-sonar.md).

| Classe | Integrante | PMD | Sonar |
| :--- | :--- | ---: | ---: |
| `Level1State` | Erick | 4 | 13 |
| `Game` | Leon | 6 | 12 |
| `Level2State` | Breno | 9 | 11 |
| `MenuState` | Roger | 5 | 7 |
| `Ship` | Luiz | 1 | 7 |
| `AudioPlayer` | Roger | 3 | 1 |
| **Total** | | **51** | **100** |

- **Sonar comprehensive como base:** é a ferramenta vista em aula e acha o dobro do PMD, inclusive defeitos que o PMD não pega (ex.: `List.remove()` dentro de `for`, issue [#10](https://github.com/noidnoidnoid/Trabalho_QTS-SpaceInvaders/issues/10)).
- **PMD como complemento:** as duas ferramentas só se sobrepõem em parte (na `AudioPlayer`, o PMD acha 3 problemas que o Sonar não aponta).
- **Por que não o perfil padrão do Sonar (core):** ele deixa três classes sob teste com zero problemas.

## Resultado "antes" (commit `bbb57f3`)

- [`sonar-antes-comprehensive.md`](before/sonar-antes-comprehensive.md): 106 issues (100 no jogo, 6 nos testes).
- A `Ship.java` não mudou depois desse commit. Outras classes foram alteradas pelo Roger ([`alteracoes-no-sut.md`](../alteracoes-no-sut.md)).

Observações:

- O painel soma 124 issues porque uma mesma issue pode contar em mais de uma qualidade. Sem repetição, são 106.
- O aviso "analysis has warnings" vem de um defeito real: em `Background.java:23`, os acentos da mensagem "Não foi possível carregar a imagem de fundo" estão corrompidos no próprio arquivo.
- A cobertura deu 41,2% na 1ª análise e 41,4% na 2ª, com o mesmo código. As execuções seguintes deram sempre o mesmo valor.

## Prints

| Arquivo | Tela |
| :--- | :--- |
| [01-visao-geral.png](before/01-visao-geral.png) | Visão geral do projeto |
| [02-issues-Ship.png](before/02-issues-Ship.png) | Issues da `Ship` (7) |
| [03-issues-Level1State.png](before/03-issues-Level1State.png) | Issues da `Level1State` (13) |
| [04-issues-Level2State.png](before/04-issues-Level2State.png) | Issues da `Level2State` (11) |
| [05-issues-Game.png](before/05-issues-Game.png) | Issues da `Game` (12) |
| [06-issues-AudioPlayer.png](before/06-issues-AudioPlayer.png) | Issues da `AudioPlayer` (1) |
| [07-complexidade-por-arquivo.png](before/07-complexidade-por-arquivo.png) | Complexidade de cada arquivo |
| [08-perfil-de-regras.png](before/08-perfil-de-regras.png) | Perfil usado (Sonar way comprehensive) |
| [09-eclipse-problemas-Ship.png](before/09-eclipse-problemas-Ship.png) | Plugin do Sonar no Eclipse: as mesmas 7 issues da `Ship` |
| [10-pmd-Ship.png](before/10-pmd-Ship.png) | PMD na `Ship` (1 problema) |

**Depois** (por enquanto só a `Ship`, análise de 22/11, 18:30): prints em [`after/`](after/) e resultado em [`ship.md`](ship.md).

## Como refazer

SonarQube Community Build 26.11 (precisa de JDK 21), com o perfil comprehensive associado ao projeto. Na raiz do repositório, com o token em `SONAR_TOKEN`:

```bash
mvn -B verify org.sonarsource.scanner.maven:sonar-maven-plugin:5.9.0.7382:sonar \
  -Dmaven.test.failure.ignore=true \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.projectKey=space-invaders-comprehensive \
  "-Dsonar.exclusions=src/test/**" \
  -Dsonar.sourceEncoding=ISO-8859-1
```

- `sonar.exclusions`: os testes ficam dentro de `src/`; sem isso o Sonar lê os testes duas vezes e para.
- `maven.test.failure.ignore`: só é necessária para refazer o "antes" (commit `bbb57f3`), em que o `GameTest` ainda falhava no Maven (issue [#7](https://github.com/noidnoidnoid/Trabalho_QTS-SpaceInvaders/issues/7), corrigida depois pelo Leon).

Scripts em [`scripts/`](scripts/): `sonar_baseline.py` (relatórios em texto) e `capturar-prints.cjs` (prints).
