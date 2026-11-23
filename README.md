# Trabalho Prático - Qualidade e Teste de Software

> **Disciplina:** Qualidade e Teste de Software  
> **Sistema Sob Teste (SUT):** Space Invaders (Java)  
> **Repositório Original:** [Upstream](https://github.com/repo-software-testing-courses/gameSpaceInvaders)  
> **Repositório Fork / Grupo:** [Trabalho](https://github.com/noidnoidnoid/Trabalho_QTS-SpaceInvaders)

---

## Equipe e Responsabilidades

| Integrante | Matrícula | Responsabilidade Principal | Classe Complexa Atribuída ($\text{CC} \ge 10$) |
| :--- | :--- | :--- | :--- |
| **Breno Carvalho** | 123083015 | `Testador` | `Level2State` |
| **Erick Bessa** | 122083063 | `Testador/Realizador dos slides` | `Level1State` |
| **Leon Stevans** | 123083047 | `Projetista de Teste/Testador` | `Game` (CC 14)|
| **Luiz Eduardo** | 123083019 | `Testador` (Entrega 2: inspeção de código com SonarQube) | `Ship` |
| **Roger Egito** | 220083118 | `Testador` (Entrega 2: unitários com isolamento, integração e sistema) | `MenuState` (CC 24, a partir da Entrega 2) e `AudioPlayer` |

---

## Visão Geral do Sistema Sob Teste (SUT)

O projeto escolhido é o jogo **Space Invaders**, desenvolvido em Java com Arquitetura Orientada a Objetos e manipulação de multithreading para renderização contínua de gráficos em tempo real via Canvas/JFrame (`60 FPS`).

---

## Índice de Artefatos e Entregáveis

Todas as entregas estão organizadas na branch principal (`main` / `master`) conforme os requisitos da disciplina.

### Documentação Geral e Logs
* **Plano de Teste:** [Docs](https://docs.google.com/document/d/1kktI82aWBj7p4GrCMSzNBMWDEArWQh54ub-EJuK4X20/edit?usp=sharing)
  * Seções do Roger (Entrega 2) também em Markdown: [`docs/plano-de-teste/secoes-roger-entrega2.md`](docs/plano-de-teste/secoes-roger-entrega2.md)
* **Registro de Uso de IA:** [AI-LOG](docs/ai/AI-LOG.md)
* **Slides de Apresentação:** [Slides](https://docs.google.com/presentation/d/1qsA4xxGPf2P7VD_Pt7r_hr5ZSvUkgkzlxxyO3kw0Bk0/edit?usp=sharing)

---

### Entrega 1 (Peso 3) — *Prazo: 21/09/2026*
* [x] **Descrição do Escopo:** Incluído no documento do [Plano de Teste](https://docs.google.com/document/d/1kktI82aWBj7p4GrCMSzNBMWDEArWQh54ub-EJuK4X20/edit?usp=sharing).
* [x] **Código-fonte Original:** Preservado na estrutura inicial do repositório.
* [x] **Casos de Teste Unitários Iniciais:** Localizados em [`src/test/java/`](src/test/java/)
  * [x] `Game` - [`GameTest`](src/test/java/br/GameTest.java) · 5 casos (`start()`, `stop()`, `run()` e controle de thread via Reflection)
  * [x] `Ship` — [`ShipTest`](src/test/java/br/characters/ShipTest.java) · 32 casos · **100% de cobertura de arestas** · 89% de escore de mutação  
  * [x] `AudioPlayer`- [`AudioPlayerTest`](src/test/java/br/audio/AudioPlayerTest.java) · 5 casos (`play()`, `stop()` e isolamento da API Sound via Mock/Reflection)
  * [x] `Level1State` - [`Level1StateTest`](src/test/java/br/states/Level1StateTest.java) · 6 casos (`init()`, `update()`, colisão nave-alien e tiro-alien via Reflection)
  * [x] `Level2State` — [`Level2StateTest`](src/test/java/br/states/Level2StateTest.java) · 11 casos (`init()`, `update()`, `checarColisoes()` via `render()`) · 19 casos na Entrega 2, ver Cobertura Estrutural e Teste Baseado em Defeitos
* [x] **Casos de Testes Manuais:**
  * [x] Cenário exportado do **TestLink** da classe `Game`: [`PDF`](docs/manual-tests/CT01-GameLoop-TestLink.pdf)
  * [x] Caso de teste de sistema da classe `Ship`: [`PDF`](docs/manual-tests/CT02-Ship-CasoDeTesteSistema.pdf) — 20 passos executados em 20/09/2026, 19 passaram e 1 falhou
  * [x] Caso de teste de sistema da classe `AudioPlayer`: [`PDF`](docs/manual-tests/CT04-AudioPlayer-CasoDeTesteSistema.pdf) — 5 passos executados em 21/09/2026, 4 passaram e 1 falhou
  * [x] Caso de teste de sistema da classe `Level1State`: [`PDF`](docs/manual-tests/CT05-Level1State-CasoDeTesteSistema.pdf) — 6 passos executados em 21/09/2026, todos passaram
  * [x] Caso de teste de sistema da classe `Level2State`: [`PDF`](docs/manual-tests/CT03-Level2State-CasoDeTesteSistema.pdf) — 7 passos executados em 21/09/2026, todos passaram
* [x] **Registro de Bugs / Bugs Tracking:** [Aba Issues do Repositório](../../issues?q=is%3Aissue)

---

### Entrega 2 (Peso 5) — *23/11/2026*
* [x] **Testes Unitários Evoluídos — `Game` (`br.Game`, CC 14)** (Leon)
  * `Game`: [`GameTest`](src/test/java/br/GameTest.java) · 13 casos (Partição de Equivalência, Análise de Valor Limite e critério Todas-Arestas) · isolamento de `Display`, `Canvas`, `BufferStrategy`, `Graphics` e `StateManager` via Mockito (`mockConstruction`, `mockStatic`, `spy`) sem alterar o SUT
  * Cobertura estrutural (JaCoCo): **100% de instruções (154/154)** e **100% de arestas (14/14)**
  * Teste de mutação (PIT): **93% de escore de mutação (28/30 mutantes mortos)**
  * Correção da falha de execução *headless*: issue [#7](../../issues/7) (fechada)
* [x] **Testes Unitários Evoluídos & Testes de Integração** (Roger)
  * Unitários com isolamento de dependências (`mock`, `mockStatic`, `mockConstruction` e injeção pelo construtor):
    * `MenuState`: [`MenuStateTest`](src/test/java/br/states/MenuStateTest.java) · 29 casos · arestas 26/26 · mutação 83,3% bruto, 100% ajustado (sobreviventes de arte classificados em [`revisao.md`](docs/ai/roger/revisao.md))
    * `AudioPlayer`: [`AudioPlayerTest`](src/test/java/br/audio/AudioPlayerTest.java) · 21 casos · arestas 9/10 (a restante é gerada pelo compilador e inalcançável, ver [`docs/inspecao/`](docs/inspecao/LEIA-ME.md)) · mutação 100%. Substitui a suíte da Entrega 1, que nunca executou ([#6](../../issues/6))
    * Adaptador web: [`WebServerTest`](src/test/java/br/web/WebServerTest.java), [`StateSnapshotTest`](src/test/java/br/states/StateSnapshotTest.java), [`SpritesTest`](src/test/java/br/display/SpritesTest.java)
  * Integração (22 casos, `*IT.java`, rodam com `mvn verify`): [`MenuNavigationIT`](src/test/java/br/states/MenuNavigationIT.java), [`PlayerInputIT`](src/test/java/br/states/PlayerInputIT.java), [`LevelProgressionIT`](src/test/java/br/states/LevelProgressionIT.java), [`ShotDefectsIT`](src/test/java/br/states/ShotDefectsIT.java), [`ThemeAudioIT`](src/test/java/br/audio/ThemeAudioIT.java)
  * Projeto dos casos (técnica, cenário e resultado esperado): [`docs/plano-de-teste/secoes-roger-entrega2.md`](docs/plano-de-teste/secoes-roger-entrega2.md)
  * Alterações no código do jogo e motivo de cada uma: [`docs/alteracoes-no-sut.md`](docs/alteracoes-no-sut.md)
  * Defeitos encontrados: issues [#2](../../issues/2) a [#12](../../issues/12)
  * Validação do uso de IA (versões iniciais, finais e evidências): [`docs/ai/roger/`](docs/ai/roger/revisao.md)
* [x] **Métricas de Qualidade ISO 25010** (Breno): medidas de 0 a 100% das 9 características da ISO/IEC 25010:2023, antes (jogo desktop, commit `bbb57f3`) e depois (versão web, `main`), com a justificativa de cada número em [`docs/qualidade/iso25010-medidas.md`](docs/qualidade/iso25010-medidas.md). Nota geral ponderada pela importância definida pelo grupo ([`importancia-caracteristicas.md`](docs/iso25010/importancia-caracteristicas.md)): 45% antes e 79% depois
* [x] **Correção da issue [#10](../../issues/10) no `Level2State`** (Breno): laços de remoção trocados por `removeIf` e 3 casos novos em [`Level2StateTest`](src/test/java/br/states/Level2StateTest.java). A falha continua no `Level1State`. Evidência de antes e depois em [`docs/ai/breno/evidencias/`](docs/ai/breno/evidencias/2026-11-22-defeito-10-level2state.txt)
* [x] **Testes Automatizados de Sistema** (Roger): Selenium WebDriver em [`src/test/java/br/system/`](src/test/java/br/system/), rodam com `mvn verify -Psystem`
  * Funcionais: RF01 a RF07 (menu, fases, Game Over) em [`MenuSystemIT`](src/test/java/br/system/MenuSystemIT.java) e [`GameplaySystemIT`](src/test/java/br/system/GameplaySystemIT.java)
  * Não funcionais (Eficiência de Desempenho): FPS médio, fluidez e tempo de resposta em [`PerformanceSystemIT`](src/test/java/br/system/PerformanceSystemIT.java)
  * Como o jogo é desktop (AWT), os testes usam uma versão web que roda a mesma lógica: [`src/br/web/`](src/br/web/). Detalhes em [`docs/alteracoes-no-sut.md`](docs/alteracoes-no-sut.md)
  * Evidências (screenshots, medições, relatórios de 3 execuções): [`docs/evidencias/sistema/`](docs/evidencias/sistema/)
* [ ] **Cobertura Estrutural ($\ge 80\%$ Todas-Arestas):** Relatório JaCoCo/HTML disponível em [`docs/reports/coverage/`](docs/reports/coverage/).
  * `Ship` (Luiz): [`ShipTest`](src/test/java/br/characters/ShipTest.java) · 32 casos · arestas 8/8 (100%)
  * `Level2State` (Breno): [`Level2StateTest`](src/test/java/br/states/Level2StateTest.java) · 19 casos · arestas 34/34 (100%). Relatório e prints em [`docs/reports/level2state/`](docs/reports/level2state/LEIA-ME.md)
  * `Level1State` (Roger, reserva da parte do Erick): [`Level1StateTest`](src/test/java/br/states/Level1StateTest.java), [`Level1StateCoberturaMutacaoTest`](src/test/java/br/states/Level1StateCoberturaMutacaoTest.java) e [`Level1StateTransicoesTest`](src/test/java/br/states/Level1StateTransicoesTest.java) · 25 casos · arestas 20/20 (100%)
* [ ] **Teste Baseado em Defeitos ($\ge 80\%$ Escore de Mutação):** Relatório Pitest disponível em [`docs/reports/mutation/`](docs/reports/mutation/).
  * `Ship` (Luiz): 16/18 (88,9%) antes da correção do Sonar e 14/16 (87,5%) depois. Os 2 sobreviventes são equivalentes ([`docs/sonar/ship.md`](docs/sonar/ship.md))
  * `Level2State` (Breno): 19/29 (66%) na primeira execução e 29/29 (100%) depois de 5 casos novos para os 10 sobreviventes. Análise dos sobreviventes em [`docs/reports/level2state/`](docs/reports/level2state/LEIA-ME.md)
  * `Level1State` (Roger, reserva da parte do Erick): 66,7% com as suítes do Erick e 21/21 (100%) depois de `Level1StateTransicoesTest`, que cobre a troca de tela e o que `render()` desenha. Evidência em [`docs/ai/roger/evidencias/`](docs/ai/roger/evidencias/2026-11-23-pit-level1state.txt)
* [ ] **Inspeção de Código (SonarQube/SonarCloud):**
  * Evidências antes das correções: [`docs/sonar/before/`](docs/sonar/before/)
  * Evidências após as correções: [`docs/sonar/after/`](docs/sonar/after/)
  * **Roger** (`MenuState` e `AudioPlayer`), com PMD 7.7.0 no lugar do Sonar (sem servidor nem conta, `mvn pmd:pmd`): 8 problemas antes, 0 depois. Prints, relatórios e justificativas em [`docs/inspecao/`](docs/inspecao/LEIA-ME.md)
  * **Luiz** (`Ship`), com SonarQube (perfil comprehensive) como base e PMD como complemento: 7 issues antes, 0 depois (PMD: 1 antes, 0 depois). Resultado em [`docs/sonar/ship.md`](docs/sonar/ship.md); método e comparação PMD x Sonar em [`docs/sonar/`](docs/sonar/README.md). Uso de IA: [`AI-LOG`](docs/ai/AI-LOG.md), interações #013 e #014
  * **`br.Game` (Leon):** Inspeção estática realizada via PMD (`mvn pmd:pmd`), confirmando ausência de vulnerabilidades críticas ou *code smells* bloqueantes na classe principal e na suíte `GameTest`.
  * **Breno** (`Level2State`), com PMD 7.7.0 (`mvn pmd:pmd`) e o Sonar do Luiz como complemento: 11 problemas antes, 0 depois (projeto de 48 para 37 no PMD). Os 14 testes da classe continuam passando, e 150 partidas simuladas deram o mesmo resultado antes e depois. Prints, relatórios e a tabela de correções em [`docs/inspecao/level2state/`](docs/inspecao/level2state/LEIA-ME.md)
  * **`Level1State`** (Roger, reserva da parte do Erick), com PMD 7.7.0 e o Sonar do Luiz como complemento: 4 problemas no PMD antes, 0 depois (projeto de 37 para 33), e os 13 apontados pelo Sonar tratados. Inclui a correção da issue [#10](../../issues/10) na classe. Prints, relatórios e a tabela de correções em [`docs/inspecao/level1state/`](docs/inspecao/level1state/LEIA-ME.md)

---

## Ferramentas Utilizadas

* **Linguagem:** Java (JDK 17+)
* **Build & Dependências:** Maven ([`pom.xml`](pom.xml) na raiz) — *a confirmar com o grupo*
* **Framework de Testes:** JUnit 5, Mockito, PIT Mutation Testing, JaCoCo
* **Automação de Sistema:** Selenium WebDriver 4 (Chrome headless)
* **Análise Estática:** SonarQube / SonarCloud, PMD 7.7.0 (`mvn pmd:pmd`)
* **Gestão de Testes Manuais:** TestLink

---

## Como Executar

Requer **JDK 17+** e **Maven**. Todos os comandos rodam a partir da raiz do repositório —
o SUT carrega as imagens com caminhos relativos (`new File("src/*.png")`), então o
diretório de trabalho importa.

```bash
mvn clean test                                    # executa a suíte de testes unitários
mvn clean verify                                  # unitários + integração (*IT.java)
mvn clean verify -Psystem                         # + testes de sistema com Selenium (precisa do Google Chrome)
mvn clean test jacoco:report                      # + relatório de cobertura
mvn test-compile org.pitest:pitest-maven:mutationCoverage   # teste de mutação
mvn compile exec:java -Dexec.mainClass=br.Launch  # abre o jogo
mvn compile exec:java -Dexec.mainClass=br.web.WebLaunch   # versão web, em http://localhost:8080
```

### Relatórios

Os relatórios são **regerados a cada build** e saem em `target/`, que não é versionado —
HTML gerado em repositório compartilhado só produz conflito de merge.

| Relatório | Caminho gerado |
| :--- | :--- |
| Cobertura (JaCoCo) | `target/site/jacoco/index.html` |
| Mutação (PIT) | `target/pit-reports/index.html` |

Na **hora de entregar**, copiar para os caminhos que este README linka como artefatos:

```bash
mkdir -p docs/reports && rm -rf docs/reports/coverage docs/reports/mutation
cp -r target/site/jacoco docs/reports/coverage
cp -r target/pit-reports docs/reports/mutation
```

### Notas sobre o build

* **Encoding misto.** A maior parte dos `.java` está em **ISO-8859-1**, mas
  `Display.java` e `Background.java` foram convertidos para UTF-8. O `pom.xml` declara
  ISO-8859-1, que é o único valor sob o qual tudo compila — trocar para UTF-8 quebra os
  demais arquivos. Vale padronizar o conjunto em algum momento.
* Os testes rodam em modo *headless* e desenham em `BufferedImage`, sem abrir janela.
* Os padrões de `includes`/`excludes` do Failsafe precisam começar com `**/`. Com `br/system/**/*IT.java` a execução dos testes de sistema encontrava 0 testes, sem erro nenhum.
* Para adicionar os testes das demais classes, basta criar os arquivos em
  `src/test/java/br/...`. Nenhuma alteração no `pom.xml` é necessária.
